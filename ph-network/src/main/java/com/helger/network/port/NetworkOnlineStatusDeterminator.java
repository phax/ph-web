/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.network.port;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.concurrent.ExecutorServiceHelper;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.datetime.PDTFactory;

/**
 * Global class to determine if the client is offline or not. The state is
 * cached for a configurable duration (by default 1 minute) and than
 * re-evaluated.
 *
 * @author Philip Helger
 * @since 9.1.2
 */
@ThreadSafe
public final class NetworkOnlineStatusDeterminator
{
  /** Default cache time is 1 minute */
  public static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes (1);
  /** Default connection timeout of 2 seconds */
  public static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 2_000;

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static Duration s_aCacheDuration = DEFAULT_CACHE_DURATION;
  @GuardedBy ("RW_LOCK")
  private static int s_nConnectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
  @GuardedBy ("RW_LOCK")
  private static LocalDateTime s_aLastCheckDT = null;
  @GuardedBy ("RW_LOCK")
  private static ENetworkOnlineStatus s_eStatus = ENetworkOnlineStatus.UNDEFINED;

  @PresentForCodeCoverage
  private static final NetworkOnlineStatusDeterminator INSTANCE = new NetworkOnlineStatusDeterminator ();

  private NetworkOnlineStatusDeterminator ()
  {}

  /**
   * @return The current caching duration. Never <code>null</code>.
   */
  @Nonnull
  public static Duration getCacheDuration ()
  {
    return RW_LOCK.readLockedGet ( () -> s_aCacheDuration);
  }

  /**
   * Set the caching duration for the offline state.
   *
   * @param aCacheDuration
   *        The duration to use. May not be <code>null</code>.
   */
  public static void setCacheDuration (@Nonnull final Duration aCacheDuration)
  {
    ValueEnforcer.notNull (aCacheDuration, "CacheDuration");
    RW_LOCK.writeLockedGet ( () -> s_aCacheDuration = aCacheDuration);
  }

  /**
   * @return The connection timeout milliseconds. Always &gt; 0.
   */
  @Nonnegative
  public static int getConnectionTimeoutMilliseconds ()
  {
    return RW_LOCK.readLockedInt ( () -> s_nConnectionTimeout);
  }

  /**
   * Set the connection timeout in milliseconds. Value &le; 0 are not allowed.
   *
   * @param nConnectionTimeout
   *        The connection timeout in milliseconds. Must be &gt; 0.
   */
  public static void setConnectionTimeoutMilliseconds (final int nConnectionTimeout)
  {
    ValueEnforcer.isGT0 (nConnectionTimeout, "ConnectionTimeout");
    RW_LOCK.writeLockedInt ( () -> s_nConnectionTimeout = nConnectionTimeout);
  }

  /**
   * @return The last date time when the offline state was checked. May be
   *         <code>null</code> if the check was not performed yet.
   */
  @Nullable
  public static LocalDateTime getLastCheckDT ()
  {
    return RW_LOCK.readLockedGet ( () -> s_aLastCheckDT);
  }

  /**
   * @return The current offline state from cache only. No update is performed.
   *         Never <code>null</code>.
   */
  @Nonnull
  public static ENetworkOnlineStatus getCachedNetworkStatus ()
  {
    return RW_LOCK.readLockedGet ( () -> s_eStatus);
  }

  /**
   * Reset the cache status and therefore enforce an explicit lookup in the next
   * call to {@link #getNetworkStatus()}.
   */
  public static void resetCachedStatus ()
  {
    RW_LOCK.writeLocked ( () -> {
      s_aLastCheckDT = null;
      s_eStatus = ENetworkOnlineStatus.UNDEFINED;
    });
  }

  /**
   * Check if the system is offline or not. This method uses the cache.
   *
   * @return The online/offline status. Never <code>null</code>.
   */
  @Nonnull
  public static ENetworkOnlineStatus getNetworkStatus ()
  {
    RW_LOCK.readLock ().lock ();
    try
    {
      if (s_eStatus.isDefined ())
      {
        // Can we use the cached value?
        final LocalDateTime aNow = PDTFactory.getCurrentLocalDateTime ();
        if (s_aLastCheckDT != null && s_aLastCheckDT.plus (s_aCacheDuration).isAfter (aNow))
          return s_eStatus;
      }
    }
    finally
    {
      RW_LOCK.readLock ().unlock ();
    }

    // An update is needed
    return getNetworkStatusNoCache ();
  }

  /**
   * Check if the system is offline or not. This method does NOT use the cache.
   *
   * @return The online/offline status. Never <code>null</code>.
   */
  @Nonnull
  public static ENetworkOnlineStatus getNetworkStatusNoCache ()
  {
    final LocalDateTime aNow = PDTFactory.getCurrentLocalDateTime ();
    RW_LOCK.writeLock ().lock ();
    try
    {
      // Check all host names in parallel, if they are reachable
      final ICommonsList <String> aHostNames = new CommonsArrayList <> ("www.google.com", "www.facebook.com", "www.microsoft.com");
      final ExecutorService aES = Executors.newFixedThreadPool (aHostNames.size ());
      final AtomicInteger aReachable = new AtomicInteger (0);
      for (final String sHostName : aHostNames)
        aES.submit ( () -> {
          // Silent mode, configured timeout
          if (NetworkPortHelper.checkPortOpen (sHostName, 80, s_nConnectionTimeout, true).isPortOpen ())
            aReachable.incrementAndGet ();
        });
      ExecutorServiceHelper.shutdownAndWaitUntilAllTasksAreFinished (aES);
      s_eStatus = aReachable.intValue () > 0 ? ENetworkOnlineStatus.ONLINE : ENetworkOnlineStatus.OFFLINE;
      s_aLastCheckDT = aNow;
      return s_eStatus;
    }
    finally
    {
      RW_LOCK.writeLock ().unlock ();
    }
  }
}
