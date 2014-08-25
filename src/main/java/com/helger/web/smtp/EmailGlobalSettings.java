/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.smtp;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.SystemProperties;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.state.EChange;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Global settings for the mail transport.
 * 
 * @author Philip Helger
 */
@ThreadSafe
public final class EmailGlobalSettings
{
  public static final int DEFAULT_MAX_QUEUE_LENGTH = 500;
  public static final int DEFAULT_MAX_SEND_COUNT = 100;
  /** Don't use SSL by default */
  public static final boolean DEFAULT_USE_SSL = false;
  /** Don't use STARTTLS by default */
  public static final boolean DEFAULT_USE_STARTTLS = false;
  public static final long DEFAULT_CONNECT_TIMEOUT_MILLISECS = 5 * CGlobal.MILLISECONDS_PER_SECOND;
  public static final long DEFAULT_TIMEOUT_MILLISECS = 10 * CGlobal.MILLISECONDS_PER_SECOND;

  private static final Logger s_aLogger = LoggerFactory.getLogger (EmailGlobalSettings.class);
  private static final ReadWriteLock s_aRWLock = new ReentrantReadWriteLock ();

  // Mail queue settings
  @GuardedBy ("s_aRWLock")
  private static int s_nMaxMailQueueLen = DEFAULT_MAX_QUEUE_LENGTH;
  @GuardedBy ("s_aRWLock")
  private static int s_nMaxMailSendCount = DEFAULT_MAX_SEND_COUNT;

  // SMTP connection settings
  @GuardedBy ("s_aRWLock")
  private static boolean s_bUseSSL = DEFAULT_USE_SSL;
  @GuardedBy ("s_aRWLock")
  private static boolean s_bUseSTARTTLS = DEFAULT_USE_STARTTLS;
  @GuardedBy ("s_aRWLock")
  private static long s_nConnectionTimeoutMilliSecs = DEFAULT_CONNECT_TIMEOUT_MILLISECS;
  @GuardedBy ("s_aRWLock")
  private static long s_nTimeoutMilliSecs = DEFAULT_TIMEOUT_MILLISECS;

  // Transport settings
  @GuardedBy ("s_aRWLock")
  private static ConnectionListener s_aConnectionListener;
  @GuardedBy ("s_aRWLock")
  private static TransportListener s_aTransportListener;
  @GuardedBy ("s_aRWLock")
  private static IEmailDataTransportListener s_aEmailDataTransportListener;

  private EmailGlobalSettings ()
  {}

  /**
   * Set mail queue settings. Changing these settings has no effect on existing
   * mail queues!
   * 
   * @param nMaxMailQueueLen
   *        The maximum number of mails that can be queued. Must be &gt; 0.
   * @param nMaxMailSendCount
   *        The maximum number of mails that are send out in one mail session.
   *        Must be &gt; 0 but &le; than {@link #getMaxMailQueueLength()}.
   * @return {@link EChange}.
   */
  @Nonnull
  public static EChange setMailQueueSize (@Nonnegative final int nMaxMailQueueLen,
                                          @Nonnegative final int nMaxMailSendCount)
  {
    ValueEnforcer.isGT0 (nMaxMailQueueLen, "MaxMailQueueLen");
    ValueEnforcer.isGT0 (nMaxMailSendCount, "MaxMailSendCount");
    if (nMaxMailSendCount > nMaxMailQueueLen)
      throw new IllegalArgumentException ("MaxMailQueueLen (" +
                                          nMaxMailQueueLen +
                                          ") must be >= than MaxMailSendCount (" +
                                          nMaxMailSendCount +
                                          ")");

    s_aRWLock.writeLock ().lock ();
    try
    {
      if (nMaxMailQueueLen == s_nMaxMailQueueLen && nMaxMailSendCount == s_nMaxMailSendCount)
        return EChange.UNCHANGED;
      s_nMaxMailQueueLen = nMaxMailQueueLen;
      s_nMaxMailSendCount = nMaxMailSendCount;
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return The maximum number of mails that can be queued. Always &gt; 0.
   */
  @Nonnegative
  public static int getMaxMailQueueLength ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_nMaxMailQueueLen;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * @return The maximum number of mails that are send out in one mail session.
   *         Always &gt; 0 but &le; than {@link #getMaxMailQueueLength()}.
   */
  @Nonnegative
  public static int getMaxMailSendCount ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_nMaxMailSendCount;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Use SSL by default?
   * 
   * @param bUseSSL
   *        <code>true</code> to use it by default, <code>false</code> if not.
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setUseSSL (final boolean bUseSSL)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_bUseSSL == bUseSSL)
        return EChange.UNCHANGED;
      s_bUseSSL = bUseSSL;
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> to use SSL by default
   */
  public static boolean isUseSSL ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bUseSSL;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Use STARTTLS by default?
   * 
   * @param bUseSTARTTLS
   *        <code>true</code> to use it by default, <code>false</code> if not.
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setUseSTARTTLS (final boolean bUseSTARTTLS)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_bUseSTARTTLS == bUseSTARTTLS)
        return EChange.UNCHANGED;
      s_bUseSTARTTLS = bUseSTARTTLS;
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> to use STARTTLS by default
   */
  public static boolean isUseSTARTTLS ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bUseSTARTTLS;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Set the connection timeout in milliseconds. Values &le; 0 are interpreted
   * as indefinite timeout which is not recommended! Changing these settings has
   * no effect on existing mail queues!
   * 
   * @param nMilliSecs
   *        The milliseconds timeout
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setConnectionTimeoutMilliSecs (final long nMilliSecs)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_nConnectionTimeoutMilliSecs == nMilliSecs)
        return EChange.UNCHANGED;
      if (nMilliSecs <= 0)
        s_aLogger.warn ("You are setting an indefinite connection timeout for the mail transport api: " + nMilliSecs);
      s_nConnectionTimeoutMilliSecs = nMilliSecs;
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Get the connection timeout in milliseconds.
   * 
   * @return If the value is &le; 0 than there should be no connection timeout.
   */
  @CheckForSigned
  public static long getConnectionTimeoutMilliSecs ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_nConnectionTimeoutMilliSecs;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Set the socket timeout in milliseconds. Values &le; 0 are interpreted as
   * indefinite timeout which is not recommended! Changing these settings has no
   * effect on existing mail queues!
   * 
   * @param nMilliSecs
   *        The milliseconds timeout
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setTimeoutMilliSecs (final long nMilliSecs)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_nTimeoutMilliSecs == nMilliSecs)
        return EChange.UNCHANGED;
      if (nMilliSecs <= 0)
        s_aLogger.warn ("You are setting an indefinite socket timeout for the mail transport api: " + nMilliSecs);
      s_nTimeoutMilliSecs = nMilliSecs;
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Get the socket timeout in milliseconds.
   * 
   * @return If the value is &le; 0 than there should be no connection timeout.
   */
  @CheckForSigned
  public static long getTimeoutMilliSecs ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_nTimeoutMilliSecs;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Set a new mail connection listener. Changing these settings has no effect
   * on existing mail queues!
   * 
   * @param aConnectionListener
   *        The new connection listener to set. May be <code>null</code>.
   */
  public static void setConnectionListener (@Nullable final ConnectionListener aConnectionListener)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      s_aConnectionListener = aConnectionListener;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return The default mail connection listener. May be <code>null</code>.
   */
  @Nullable
  public static ConnectionListener getConnectionListener ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_aConnectionListener;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Set a new mail transport listener. Changing these settings has no effect on
   * existing mail queues!
   * 
   * @param aTransportListener
   *        The new transport listener to set. May be <code>null</code>.
   */
  public static void setTransportListener (@Nullable final TransportListener aTransportListener)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      s_aTransportListener = aTransportListener;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return The default mail transport listener. May be <code>null</code>.
   */
  @Nullable
  public static TransportListener getTransportListener ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_aTransportListener;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Set a new mail transport listener. Changing these settings has no effect on
   * existing mail queues!
   * 
   * @param aEmailDataTransportListener
   *        The new transport listener to set. May be <code>null</code>.
   */
  public static void setEmailDataTransportListener (@Nullable final IEmailDataTransportListener aEmailDataTransportListener)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      s_aEmailDataTransportListener = aEmailDataTransportListener;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return The default mail transport listener. May be <code>null</code>.
   */
  @Nullable
  public static IEmailDataTransportListener getEmailDataTransportListener ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_aEmailDataTransportListener;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Enable or disable javax.mail debugging. By default debugging is disabled.
   * 
   * @param bDebug
   *        <code>true</code> to enabled debugging, <code>false</code> to
   *        disable it.
   */
  @SuppressFBWarnings ("LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE")
  public static void enableJavaxMailDebugging (final boolean bDebug)
  {
    java.util.logging.Logger.getLogger ("com.sun.mail.smtp").setLevel (bDebug ? Level.FINEST : Level.INFO);
    java.util.logging.Logger.getLogger ("com.sun.mail.smtp.protocol").setLevel (bDebug ? Level.FINEST : Level.INFO);
    SystemProperties.setPropertyValue ("mail.socket.debug", Boolean.toString (bDebug));
    SystemProperties.setPropertyValue ("java.security.debug", bDebug ? "certpath" : null);
    SystemProperties.setPropertyValue ("javax.net.debug", bDebug ? "trustmanager" : null);
  }

  /**
   * @return <code>true</code> if javax.mail debugging is enabled,
   *         <code>false</code> if not.
   */
  public static boolean isJavaxMailDebuggingEnabled ()
  {
    return java.util.logging.Logger.getLogger ("com.sun.mail.smtp").getLevel ().equals (Level.FINEST);
  }
}
