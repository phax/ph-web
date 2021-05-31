/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.xservlet.requesttrack;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.concurrent.SimpleReadWriteLock;

/**
 * Settings class for the Request tracker. Must be set before the first
 * invocation.
 *
 * @author Philip Helger
 * @since 9.6.1
 */
@ThreadSafe
public final class RequestTrackerSettings
{
  /** By default the long running checks are enabled */
  public static final boolean DEFAULT_LONG_RUNNING_CHECK_ENABLED = true;
  /** By default the long running checks are executed every 2 seconds */
  public static final long DEFAULT_LONG_RUNNING_REQUESTS_CHECK_INTERVAL_MILLISECONDS = 2 * CGlobal.MILLISECONDS_PER_SECOND;
  /**
   * The number of a seconds a request needs to run before it is considered
   * "long running". Defaults to 30.
   */
  public static final long DEFAULT_LONG_RUNNING_NOTIFICATION_MILLISECONDS = 30 * CGlobal.MILLISECONDS_PER_SECOND;

  /** By default the parallel running checks are enabled */
  public static final boolean DEFAULT_PARALLEL_RUNNING_REQUESTS_CHECK_ENABLED = true;
  /** The minimum number of parallel requests that trigger a warning message */
  public static final int DEFAULT_PARALLEL_RUNNING_REQUESTS_BARRIER = 60;

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  private static boolean s_bLRCheckEnabled = DEFAULT_LONG_RUNNING_CHECK_ENABLED;
  private static long s_nLRCheckMilliseconds = DEFAULT_LONG_RUNNING_REQUESTS_CHECK_INTERVAL_MILLISECONDS;
  private static long s_nLRMinDurationMilliseconds = DEFAULT_LONG_RUNNING_NOTIFICATION_MILLISECONDS;

  private static boolean s_bPRCheckEnabled = DEFAULT_PARALLEL_RUNNING_REQUESTS_CHECK_ENABLED;
  private static int s_nParallelRunningRequestBarrier = DEFAULT_PARALLEL_RUNNING_REQUESTS_BARRIER;

  private RequestTrackerSettings ()
  {}

  /**
   * @return <code>true</code> if long running checks are enabled,
   *         <code>false</code> if not.
   */
  public static boolean isLongRunningRequestsCheckEnabled ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bLRCheckEnabled);
  }

  public static void setLongRunningRequestsCheckEnabled (final boolean bEnabled)
  {
    RW_LOCK.writeLocked ( () -> s_bLRCheckEnabled = bEnabled);
  }

  /**
   * @return The interval in milliseconds, in which the system should check for
   *         long running requests. Always &gt; 0.
   */
  @Nonnegative
  public static long getLongRunningRequestCheckIntervalMilliseconds ()
  {
    return RW_LOCK.readLockedLong ( () -> s_nLRCheckMilliseconds);
  }

  public static void setLongRunningRequestCheckIntervalMilliseconds (@Nonnegative final long nSeconds)
  {
    ValueEnforcer.isGT0 (nSeconds, "Seconds");
    RW_LOCK.writeLocked ( () -> s_nLRCheckMilliseconds = nSeconds);
  }

  /**
   * @return The milliseconds that need to pass by, before a request is
   *         considered "long running".
   */
  @Nonnegative
  public static long getLongRunningRequestWarnDurationMillis ()
  {
    return RW_LOCK.readLockedLong ( () -> s_nLRMinDurationMilliseconds);
  }

  public static void setLongRunningRequestWarnDurationMillis (@Nonnegative final long nMilliseconds)
  {
    ValueEnforcer.isGT0 (nMilliseconds, "Milliseconds");
    RW_LOCK.writeLocked ( () -> s_nLRMinDurationMilliseconds = nMilliseconds);
  }

  /**
   * @return <code>true</code> if the check for parallel requests is enabled,
   *         <code>false</code> if not.
   */
  public static boolean isParallelRunningRequestsCheckEnabled ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bPRCheckEnabled);
  }

  public static void setParallelRunningRequestsCheckEnabled (final boolean bEnabled)
  {
    RW_LOCK.writeLocked ( () -> s_bPRCheckEnabled = bEnabled);
  }

  /**
   * @return The minimum number of parallel requests that need to be exceeded,
   *         before a "parallel requests" warning is emitted. Always &gt; 0.
   */
  @Nonnegative
  public static int getParallelRunningRequestBarrier ()
  {
    return RW_LOCK.readLockedInt ( () -> s_nParallelRunningRequestBarrier);
  }

  public static void setParallelRunningRequestBarrier (@Nonnegative final int nParallelRunningRequestBarrier)
  {
    ValueEnforcer.isGT0 (nParallelRunningRequestBarrier, "ParallelRunningRequestBarrier");

    RW_LOCK.writeLocked ( () -> s_nParallelRunningRequestBarrier = nParallelRunningRequestBarrier);
  }
}
