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
package com.helger.smtp.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.serialize.convert.SerializationConverter;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.network.port.NetworkPortHelper;
import com.helger.smtp.CSMTP;
import com.helger.smtp.EmailGlobalSettings;

/**
 * Writable implementation of the {@link ISMTPSettings} interface.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class SMTPSettings implements ISMTPSettings, ICloneable <SMTPSettings>
{
  private String m_sHostName;
  private int m_nPort;
  private String m_sUserName;
  private String m_sPassword;
  private transient Charset m_aCharset;
  private boolean m_bSSLEnabled;
  private boolean m_bSTARTTLSEnabled;
  private long m_nConnectionTimeoutMilliSecs;
  private long m_nTimeoutMilliSecs;
  private boolean m_bDebugSMTP;

  private void writeObject (@Nonnull final ObjectOutputStream aOOS) throws IOException
  {
    StreamHelper.writeSafeUTF (aOOS, m_sHostName);
    aOOS.writeInt (m_nPort);
    StreamHelper.writeSafeUTF (aOOS, m_sUserName);
    StreamHelper.writeSafeUTF (aOOS, m_sPassword);
    SerializationConverter.writeConvertedObject (m_aCharset, aOOS);
    aOOS.writeBoolean (m_bSSLEnabled);
    aOOS.writeBoolean (m_bSTARTTLSEnabled);
    aOOS.writeLong (m_nConnectionTimeoutMilliSecs);
    aOOS.writeLong (m_nTimeoutMilliSecs);
    aOOS.writeBoolean (m_bDebugSMTP);
  }

  private void readObject (@Nonnull final ObjectInputStream aOIS) throws IOException
  {
    m_sHostName = StreamHelper.readSafeUTF (aOIS);
    m_nPort = aOIS.readInt ();
    m_sUserName = StreamHelper.readSafeUTF (aOIS);
    m_sPassword = StreamHelper.readSafeUTF (aOIS);
    m_aCharset = SerializationConverter.readConvertedObject (aOIS, Charset.class);
    m_bSSLEnabled = aOIS.readBoolean ();
    m_bSTARTTLSEnabled = aOIS.readBoolean ();
    m_nConnectionTimeoutMilliSecs = aOIS.readLong ();
    m_nTimeoutMilliSecs = aOIS.readLong ();
    m_bDebugSMTP = aOIS.readBoolean ();
  }

  /**
   * Constructor which copies settings from another object
   *
   * @param aOther
   *        The settings to use. May not be <code>null</code>.
   */
  public SMTPSettings (@Nonnull final ISMTPSettings aOther)
  {
    this (aOther.getHostName (),
          aOther.getPort (),
          aOther.getUserName (),
          aOther.getPassword (),
          aOther.getCharsetObj (),
          aOther.isSSLEnabled (),
          aOther.isSTARTTLSEnabled (),
          aOther.getConnectionTimeoutMilliSecs (),
          aOther.getTimeoutMilliSecs (),
          aOther.isDebugSMTP ());
  }

  /**
   * Constructor with default port, and no authentication
   *
   * @param sHost
   *        SMTP server name or IP address. May neither be <code>null</code> nor
   *        empty.
   */
  public SMTPSettings (@Nonnull final String sHost)
  {
    this (sHost, -1, null, null, (Charset) null, EmailGlobalSettings.isUseSSL ());
  }

  /**
   * Constructor
   *
   * @param sHostName
   *        SMTP server name or IP address. May neither be <code>null</code> nor
   *        empty.
   * @param nPort
   *        Port to use. May be <code>-1</code> for the default port.
   * @param sUserName
   *        The username to use. May be <code>null</code>.
   * @param sPassword
   *        The password to use. May be <code>null</code>.
   * @param aCharset
   *        The charset to use. May be <code>null</code>.
   * @param bSSLEnabled
   *        <code>true</code> to enable SSL communications
   */
  public SMTPSettings (@Nonnull final String sHostName,
                       final int nPort,
                       @Nullable final String sUserName,
                       @Nullable final String sPassword,
                       @Nullable final Charset aCharset,
                       final boolean bSSLEnabled)
  {
    this (sHostName,
          nPort,
          sUserName,
          sPassword,
          aCharset,
          bSSLEnabled,
          EmailGlobalSettings.isUseSTARTTLS (),
          EmailGlobalSettings.getConnectionTimeoutMilliSecs (),
          EmailGlobalSettings.getTimeoutMilliSecs (),
          EmailGlobalSettings.isDebugSMTP ());
  }

  /**
   * Constructor
   *
   * @param sHostName
   *        SMTP server name or IP address. May neither be <code>null</code> nor
   *        empty.
   * @param nPort
   *        Port to use. May be <code>-1</code> for the default port.
   * @param sUserName
   *        The username to use. May be <code>null</code>.
   * @param sPassword
   *        The password to use. May be <code>null</code>.
   * @param aCharset
   *        The charset to use. May be <code>null</code>.
   * @param bSSLEnabled
   *        <code>true</code> to enable SSL communications
   * @param bSTARTTLSEnabled
   *        <code>true</code> to enable STARTTLS communications
   * @param nConnectionTimeoutMilliSecs
   *        the connection timeout in milliseconds.
   * @param nTimeoutMilliSecs
   *        the socket timeout in milliseconds.
   * @param bDebugSMTP
   *        <code>true</code> to enable SMTP debugging, <code>false</code> to
   *        disable it.
   */
  public SMTPSettings (@Nonnull final String sHostName,
                       final int nPort,
                       @Nullable final String sUserName,
                       @Nullable final String sPassword,
                       @Nullable final Charset aCharset,
                       final boolean bSSLEnabled,
                       final boolean bSTARTTLSEnabled,
                       final long nConnectionTimeoutMilliSecs,
                       final long nTimeoutMilliSecs,
                       final boolean bDebugSMTP)
  {
    setHostName (sHostName);
    setPort (nPort);
    setUserName (sUserName);
    setPassword (sPassword);
    setCharset (aCharset != null ? aCharset : CSMTP.CHARSET_SMTP_OBJ);
    setSSLEnabled (bSSLEnabled);
    setSTARTTLSEnabled (bSTARTTLSEnabled);
    setConnectionTimeoutMilliSecs (nConnectionTimeoutMilliSecs);
    setTimeoutMilliSecs (nTimeoutMilliSecs);
    setDebugSMTP (bDebugSMTP);
  }

  @Nonnull
  public String getHostName ()
  {
    return m_sHostName;
  }

  @Nonnull
  public EChange setHostName (@Nonnull final String sHostName)
  {
    ValueEnforcer.notNull (sHostName, "Host");

    if (sHostName.equals (m_sHostName))
      return EChange.UNCHANGED;
    m_sHostName = sHostName;
    return EChange.CHANGED;
  }

  public int getPort ()
  {
    return m_nPort;
  }

  @Nonnull
  public EChange setPort (final int nPort)
  {
    if (nPort != -1 && !NetworkPortHelper.isValidPort (nPort))
      throw new IllegalArgumentException ("Port must either be -1 or must be in the valid range!");

    if (nPort == m_nPort)
      return EChange.UNCHANGED;
    m_nPort = nPort;
    return EChange.CHANGED;
  }

  @Nullable
  public String getUserName ()
  {
    return m_sUserName;
  }

  @Nonnull
  public EChange setUserName (@Nullable final String sUserName)
  {
    final String sRealUserName = StringHelper.hasNoText (sUserName) ? null : sUserName;
    if (EqualsHelper.equals (sRealUserName, m_sUserName))
      return EChange.UNCHANGED;
    m_sUserName = sRealUserName;
    return EChange.CHANGED;
  }

  @Nullable
  public String getPassword ()
  {
    return m_sPassword;
  }

  @Nonnull
  public EChange setPassword (@Nullable final String sPassword)
  {
    final String sRealPassword = StringHelper.hasNoText (sPassword) ? null : sPassword;
    if (EqualsHelper.equals (sRealPassword, m_sPassword))
      return EChange.UNCHANGED;
    m_sPassword = sRealPassword;
    return EChange.CHANGED;
  }

  @Nonnull
  public Charset getCharsetObj ()
  {
    return m_aCharset;
  }

  @Nonnull
  public EChange setCharset (@Nullable final Charset aCharset)
  {
    if (EqualsHelper.equals (aCharset, m_aCharset))
      return EChange.UNCHANGED;
    m_aCharset = aCharset;
    return EChange.CHANGED;
  }

  public boolean isSSLEnabled ()
  {
    return m_bSSLEnabled;
  }

  @Nonnull
  public EChange setSSLEnabled (final boolean bSSLEnabled)
  {
    if (m_bSSLEnabled == bSSLEnabled)
      return EChange.UNCHANGED;
    m_bSSLEnabled = bSSLEnabled;
    return EChange.CHANGED;
  }

  public boolean isSTARTTLSEnabled ()
  {
    return m_bSTARTTLSEnabled;
  }

  @Nonnull
  public EChange setSTARTTLSEnabled (final boolean bSTARTTLSEnabled)
  {
    if (m_bSTARTTLSEnabled == bSTARTTLSEnabled)
      return EChange.UNCHANGED;
    m_bSTARTTLSEnabled = bSTARTTLSEnabled;
    return EChange.CHANGED;
  }

  public long getConnectionTimeoutMilliSecs ()
  {
    return m_nConnectionTimeoutMilliSecs;
  }

  /**
   * Set the connection timeout in milliseconds. Values &le; 0 are interpreted
   * as indefinite timeout which is not recommended!
   *
   * @param nMilliSecs
   *        The milliseconds timeout
   * @return {@link EChange}
   */
  @Nonnull
  public EChange setConnectionTimeoutMilliSecs (final long nMilliSecs)
  {
    if (m_nConnectionTimeoutMilliSecs == nMilliSecs)
      return EChange.UNCHANGED;
    m_nConnectionTimeoutMilliSecs = nMilliSecs;
    return EChange.CHANGED;
  }

  public long getTimeoutMilliSecs ()
  {
    return m_nTimeoutMilliSecs;
  }

  /**
   * Set the socket timeout in milliseconds. Values &le; 0 are interpreted as
   * indefinite timeout which is not recommended!
   *
   * @param nMilliSecs
   *        The milliseconds timeout
   * @return {@link EChange}
   */
  @Nonnull
  public EChange setTimeoutMilliSecs (final long nMilliSecs)
  {
    if (m_nTimeoutMilliSecs == nMilliSecs)
      return EChange.UNCHANGED;
    m_nTimeoutMilliSecs = nMilliSecs;
    return EChange.CHANGED;
  }

  /**
   * @return <code>true</code> if SMTP debugging is active, <code>false</code>
   *         if not.
   * @since 1.0.1
   */
  public boolean isDebugSMTP ()
  {
    return m_bDebugSMTP;
  }

  /**
   * @param bDebugSMTP
   *        <code>true</code> to activate SMTP debugging, <code>false</code> to
   *        disable it.
   * @return {@link EChange}
   * @since 1.0.1
   */
  @Nonnull
  public EChange setDebugSMTP (final boolean bDebugSMTP)
  {
    if (m_bDebugSMTP == bDebugSMTP)
      return EChange.UNCHANGED;
    m_bDebugSMTP = bDebugSMTP;
    return EChange.CHANGED;
  }

  public boolean areRequiredFieldsSet ()
  {
    return StringHelper.hasText (m_sHostName);
  }

  @Nonnull
  public SMTPSettings getClone ()
  {
    return new SMTPSettings (this);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (this == o)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final SMTPSettings rhs = (SMTPSettings) o;
    return m_sHostName.equals (rhs.m_sHostName) &&
           m_nPort == rhs.m_nPort &&
           EqualsHelper.equals (m_sUserName, rhs.m_sUserName) &&
           EqualsHelper.equals (m_sPassword, rhs.m_sPassword) &&
           m_aCharset.equals (rhs.m_aCharset) &&
           m_bSSLEnabled == rhs.m_bSSLEnabled &&
           m_bSTARTTLSEnabled == rhs.m_bSTARTTLSEnabled &&
           m_nConnectionTimeoutMilliSecs == rhs.m_nConnectionTimeoutMilliSecs &&
           m_nTimeoutMilliSecs == rhs.m_nTimeoutMilliSecs &&
           m_bDebugSMTP == rhs.m_bDebugSMTP;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sHostName)
                                       .append (m_nPort)
                                       .append (m_sUserName)
                                       .append (m_sPassword)
                                       .append (m_aCharset)
                                       .append (m_bSSLEnabled)
                                       .append (m_bSTARTTLSEnabled)
                                       .append (m_nConnectionTimeoutMilliSecs)
                                       .append (m_nTimeoutMilliSecs)
                                       .append (m_bDebugSMTP)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("hostName", m_sHostName)
                                       .append ("port", m_nPort)
                                       .append ("userName", m_sUserName)
                                       .appendPassword ("password")
                                       .append ("charset", m_aCharset)
                                       .append ("SSL", m_bSSLEnabled)
                                       .append ("STARTTLS", m_bSTARTTLSEnabled)
                                       .append ("connectionTimeout", m_nConnectionTimeoutMilliSecs)
                                       .append ("timeout", m_nTimeoutMilliSecs)
                                       .append ("debugSMTP", m_bDebugSMTP)
                                       .getToString ();
  }
}
