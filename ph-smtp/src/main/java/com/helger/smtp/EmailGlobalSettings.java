/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.smtp;

import java.util.logging.Level;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.mail.event.ConnectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.state.EChange;
import com.helger.commons.system.SystemProperties;
import com.helger.smtp.listener.IEmailDataTransportListener;

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

  private static final Logger LOGGER = LoggerFactory.getLogger (EmailGlobalSettings.class);
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();

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
  @GuardedBy ("s_aRWLock")
  private static boolean s_bDebugSMTP = GlobalDebug.isDebugMode ();

  // Transport settings
  @GuardedBy ("s_aRWLock")
  private static ICommonsList <ConnectionListener> s_aConnectionListeners = new CommonsArrayList <> ();
  @GuardedBy ("s_aRWLock")
  private static ICommonsList <IEmailDataTransportListener> s_aEmailDataTransportListeners = new CommonsArrayList <> ();

  private EmailGlobalSettings ()
  {}

  /**
   * @return The maximum number of mails that can be queued. Always &gt; 0.
   */
  @Nonnegative
  public static int getMaxMailQueueLength ()
  {
    return s_aRWLock.readLockedInt ( () -> s_nMaxMailQueueLen);
  }

  /**
   * @return The maximum number of mails that are send out in one mail session.
   *         Always &gt; 0 but &le; than {@link #getMaxMailQueueLength()}.
   */
  @Nonnegative
  public static int getMaxMailSendCount ()
  {
    return s_aRWLock.readLockedInt ( () -> s_nMaxMailSendCount);
  }

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
  public static EChange setMailQueueSize (@Nonnegative final int nMaxMailQueueLen, @Nonnegative final int nMaxMailSendCount)
  {
    ValueEnforcer.isGT0 (nMaxMailQueueLen, "MaxMailQueueLen");
    ValueEnforcer.isGT0 (nMaxMailSendCount, "MaxMailSendCount");
    ValueEnforcer.isTrue (nMaxMailQueueLen >= nMaxMailSendCount,
                          () -> "MaxMailQueueLen (" + nMaxMailQueueLen + ") must be >= than MaxMailSendCount (" + nMaxMailSendCount + ")");

    return s_aRWLock.writeLockedGet ( () -> {
      if (nMaxMailQueueLen == s_nMaxMailQueueLen && nMaxMailSendCount == s_nMaxMailSendCount)
        return EChange.UNCHANGED;
      s_nMaxMailQueueLen = nMaxMailQueueLen;
      s_nMaxMailSendCount = nMaxMailSendCount;
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> to use SSL by default
   */
  public static boolean isUseSSL ()
  {
    return s_aRWLock.readLockedBoolean ( () -> s_bUseSSL);
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
    return s_aRWLock.writeLockedGet ( () -> {
      if (s_bUseSSL == bUseSSL)
        return EChange.UNCHANGED;
      s_bUseSSL = bUseSSL;
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> to use STARTTLS by default
   */
  public static boolean isUseSTARTTLS ()
  {
    return s_aRWLock.readLockedBoolean ( () -> s_bUseSTARTTLS);
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
    return s_aRWLock.writeLockedGet ( () -> {
      if (s_bUseSTARTTLS == bUseSTARTTLS)
        return EChange.UNCHANGED;
      s_bUseSTARTTLS = bUseSTARTTLS;
      return EChange.CHANGED;
    });
  }

  /**
   * Get the connection timeout in milliseconds.
   *
   * @return If the value is &le; 0 than there should be no connection timeout.
   */
  @CheckForSigned
  public static long getConnectionTimeoutMilliSecs ()
  {
    return s_aRWLock.readLockedLong ( () -> s_nConnectionTimeoutMilliSecs);
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
    return s_aRWLock.writeLockedGet ( () -> {
      if (s_nConnectionTimeoutMilliSecs == nMilliSecs)
        return EChange.UNCHANGED;
      if (nMilliSecs <= 0)
        LOGGER.warn ("You are setting an indefinite connection timeout for the mail transport api: " + nMilliSecs);
      s_nConnectionTimeoutMilliSecs = nMilliSecs;
      return EChange.CHANGED;
    });
  }

  /**
   * Get the socket timeout in milliseconds.
   *
   * @return If the value is &le; 0 than there should be no connection timeout.
   */
  @CheckForSigned
  public static long getTimeoutMilliSecs ()
  {
    return s_aRWLock.readLockedLong ( () -> s_nTimeoutMilliSecs);
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
    return s_aRWLock.writeLockedGet ( () -> {
      if (s_nTimeoutMilliSecs == nMilliSecs)
        return EChange.UNCHANGED;
      if (nMilliSecs <= 0)
        LOGGER.warn ("You are setting an indefinite socket timeout for the mail transport api: " + nMilliSecs);
      s_nTimeoutMilliSecs = nMilliSecs;
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> if SMTP debugging is active, <code>false</code>
   *         if not.
   * @since 1.0.1
   */
  public static boolean isDebugSMTP ()
  {
    return s_aRWLock.readLockedBoolean ( () -> s_bDebugSMTP);
  }

  /**
   * @param bDebugSMTP
   *        <code>true</code> to activate SMTP debugging, <code>false</code> to
   *        disable it.
   * @return {@link EChange}
   * @since 1.0.1
   */
  @Nonnull
  public static EChange setDebugSMTP (final boolean bDebugSMTP)
  {
    return s_aRWLock.writeLockedGet ( () -> {
      if (s_bDebugSMTP == bDebugSMTP)
        return EChange.UNCHANGED;
      s_bDebugSMTP = bDebugSMTP;
      return EChange.CHANGED;
    });
  }

  /**
   * Add a new mail connection listener.
   *
   * @param aConnectionListener
   *        The new connection listener to add. May not be <code>null</code>.
   * @since 1.1.0
   */
  public static void addConnectionListener (@Nonnull final ConnectionListener aConnectionListener)
  {
    ValueEnforcer.notNull (aConnectionListener, "ConnectionListener");
    s_aRWLock.writeLockedBoolean ( () -> s_aConnectionListeners.add (aConnectionListener));
  }

  /**
   * Remove an existing mail connection listener.
   *
   * @param aConnectionListener
   *        The new connection listener to set. May not be <code>null</code>.
   * @return {@link EChange}
   * @since 1.1.0
   */
  @Nonnull
  public static EChange removeConnectionListener (@Nullable final ConnectionListener aConnectionListener)
  {
    if (aConnectionListener == null)
      return EChange.UNCHANGED;

    return s_aRWLock.writeLockedGet ( () -> s_aConnectionListeners.removeObject (aConnectionListener));
  }

  /**
   * Remove all connection listeners
   *
   * @return {@link EChange}
   * @since 1.1.0
   */
  @Nonnull
  public static EChange removeAllConnectionListeners ()
  {
    return s_aRWLock.writeLockedGet (s_aConnectionListeners::removeAll);
  }

  /**
   * @return <code>true</code> if at least one connection listener is present,
   *         <code>false</code> otherwise.
   * @since 1.1.0
   */
  public static boolean hasConnectionListeners ()
  {
    return s_aRWLock.readLockedBoolean (s_aConnectionListeners::isNotEmpty);
  }

  /**
   * @return All mail connection listeners. Never <code>null</code>.
   * @since 1.1.0
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ConnectionListener> getAllConnectionListeners ()
  {
    return s_aRWLock.readLockedGet (s_aConnectionListeners::getClone);
  }

  /**
   * Add a new mail transport listener.
   *
   * @param aEmailDataTransportListener
   *        The new transport listener to add. May not be <code>null</code>.
   * @since 1.1.0
   */
  public static void addEmailDataTransportListener (@Nonnull final IEmailDataTransportListener aEmailDataTransportListener)
  {
    ValueEnforcer.notNull (aEmailDataTransportListener, "EmailDataTransportListener");
    s_aRWLock.writeLockedBoolean ( () -> s_aEmailDataTransportListeners.add (aEmailDataTransportListener));
  }

  /**
   * Remove an existing mail transport listener.
   *
   * @param aEmailDataTransportListener
   *        The new transport listener to set. May not be <code>null</code>.
   * @return {@link EChange}
   * @since 1.1.0
   */
  @Nonnull
  public static EChange removeEmailDataTransportListener (@Nullable final IEmailDataTransportListener aEmailDataTransportListener)
  {
    if (aEmailDataTransportListener == null)
      return EChange.UNCHANGED;

    return s_aRWLock.writeLockedGet ( () -> s_aEmailDataTransportListeners.removeObject (aEmailDataTransportListener));
  }

  /**
   * Remove all transport listeners
   *
   * @return {@link EChange}
   * @since 1.1.0
   */
  @Nonnull
  public static EChange removeAllEmailDataTransportListeners ()
  {
    return s_aRWLock.writeLockedGet (s_aEmailDataTransportListeners::removeAll);
  }

  /**
   * @return <code>true</code> if at least one transport listener is present,
   *         <code>false</code> otherwise.
   * @since 1.1.0
   */
  public static boolean hasEmailDataTransportListeners ()
  {
    return s_aRWLock.readLockedBoolean (s_aEmailDataTransportListeners::isNotEmpty);
  }

  /**
   * @return All mail transport listeners. Never <code>null</code>.
   * @since 1.1.0
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <IEmailDataTransportListener> getAllEmailDataTransportListeners ()
  {
    return s_aRWLock.readLockedGet (s_aEmailDataTransportListeners::getClone);
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
    SystemProperties.setPropertyValue ("mail.socket.debug", bDebug);
    SystemProperties.setPropertyValue (GlobalDebug.SYSTEM_PROPERTY_JAVA_SECURITY_DEBUG, bDebug ? "certpath" : null);
    SystemProperties.setPropertyValue (GlobalDebug.SYSTEM_PROPERTY_JAVAX_NET_DEBUG, bDebug ? "trustmanager" : null);
  }

  /**
   * @return <code>true</code> if javax.mail debugging is enabled,
   *         <code>false</code> if not.
   */
  public static boolean isJavaxMailDebuggingEnabled ()
  {
    return java.util.logging.Logger.getLogger ("com.sun.mail.smtp").getLevel ().equals (Level.FINEST);
  }

  /**
   * Set all settings to the default. This is helpful for testing.
   *
   * @since 3.0.0
   */
  public static void setToDefault ()
  {
    s_aRWLock.writeLocked ( () -> {
      s_nMaxMailQueueLen = DEFAULT_MAX_QUEUE_LENGTH;
      s_nMaxMailSendCount = DEFAULT_MAX_SEND_COUNT;
      s_bUseSSL = DEFAULT_USE_SSL;
      s_bUseSTARTTLS = DEFAULT_USE_STARTTLS;
      s_nConnectionTimeoutMilliSecs = DEFAULT_CONNECT_TIMEOUT_MILLISECS;
      s_nTimeoutMilliSecs = DEFAULT_TIMEOUT_MILLISECS;
      s_bDebugSMTP = GlobalDebug.isDebugMode ();
      s_aConnectionListeners.clear ();
      s_aEmailDataTransportListeners.clear ();
    });
  }
}
