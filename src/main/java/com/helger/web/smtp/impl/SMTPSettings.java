/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.smtp.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ICloneable;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.charset.CharsetManager;
import com.helger.commons.equals.EqualsUtils;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.serialize.convert.SerializationConverter;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.CWebCharset;
import com.helger.web.port.DefaultNetworkPorts;
import com.helger.web.smtp.EmailGlobalSettings;
import com.helger.web.smtp.ISMTPSettings;

/**
 * Writable implementation of the {@link ISMTPSettings} interface.
 * 
 * @author Philip Helger
 */
@Immutable
public final class SMTPSettings implements ISMTPSettings, ICloneable <SMTPSettings>
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (SMTPSettings.class);

  private String m_sHostName;
  private int m_nPort;
  private String m_sUserName;
  private String m_sPassword;
  private Charset m_aCharset;
  private boolean m_bSSLEnabled;
  private boolean m_bSTARTTLSEnabled;
  private long m_nConnectionTimeoutMilliSecs;
  private long m_nTimeoutMilliSecs;

  private void writeObject (@Nonnull final ObjectOutputStream aOOS) throws IOException
  {
    aOOS.writeUTF (m_sHostName);
    aOOS.writeInt (m_nPort);
    aOOS.writeUTF (m_sUserName);
    aOOS.writeUTF (m_sPassword);
    SerializationConverter.writeConvertedObject (m_aCharset, aOOS);
    aOOS.writeBoolean (m_bSSLEnabled);
    aOOS.writeBoolean (m_bSTARTTLSEnabled);
    aOOS.writeLong (m_nConnectionTimeoutMilliSecs);
    aOOS.writeLong (m_nTimeoutMilliSecs);
  }

  private void readObject (@Nonnull final ObjectInputStream aOIS) throws IOException
  {
    m_sHostName = aOIS.readUTF ();
    m_nPort = aOIS.readInt ();
    m_sUserName = aOIS.readUTF ();
    m_sPassword = aOIS.readUTF ();
    m_aCharset = SerializationConverter.readConvertedObject (aOIS, Charset.class);
    m_bSSLEnabled = aOIS.readBoolean ();
    m_bSTARTTLSEnabled = aOIS.readBoolean ();
    m_nConnectionTimeoutMilliSecs = aOIS.readLong ();
    m_nTimeoutMilliSecs = aOIS.readLong ();
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
          aOther.getCharset (),
          aOther.isSSLEnabled (),
          aOther.isSTARTTLSEnabled (),
          aOther.getConnectionTimeoutMilliSecs (),
          aOther.getTimeoutMilliSecs ());
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
    this (sHost, -1, null, null, null, EmailGlobalSettings.isUseSSL ());
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
   * @param sCharset
   *        The charset to use. May be <code>null</code>.
   * @param bSSLEnabled
   *        <code>true</code> to enable SSL communications
   */
  public SMTPSettings (@Nonnull final String sHostName,
                       final int nPort,
                       @Nullable final String sUserName,
                       @Nullable final String sPassword,
                       @Nullable final String sCharset,
                       final boolean bSSLEnabled)
  {
    this (sHostName,
          nPort,
          sUserName,
          sPassword,
          sCharset,
          bSSLEnabled,
          EmailGlobalSettings.isUseSTARTTLS (),
          EmailGlobalSettings.getConnectionTimeoutMilliSecs (),
          EmailGlobalSettings.getTimeoutMilliSecs ());
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
   * @param sCharset
   *        The charset to use. May be <code>null</code>.
   * @param bSSLEnabled
   *        <code>true</code> to enable SSL communications
   * @param bSTARTTLSEnabled
   *        <code>true</code> to enable STARTTLS communications
   * @param nConnectionTimeoutMilliSecs
   *        the connection timeout in milliseconds.
   * @param nTimeoutMilliSecs
   *        the socket timeout in milliseconds.
   */
  public SMTPSettings (@Nonnull final String sHostName,
                       final int nPort,
                       @Nullable final String sUserName,
                       @Nullable final String sPassword,
                       @Nullable final String sCharset,
                       final boolean bSSLEnabled,
                       final boolean bSTARTTLSEnabled,
                       final long nConnectionTimeoutMilliSecs,
                       final long nTimeoutMilliSecs)
  {
    setHostName (sHostName);
    setPort (nPort);
    setUserName (sUserName);
    setPassword (sPassword);
    setCharset (sCharset);
    setSSLEnabled (bSSLEnabled);
    setSTARTTLSEnabled (bSTARTTLSEnabled);
    setConnectionTimeoutMilliSecs (nConnectionTimeoutMilliSecs);
    setTimeoutMilliSecs (nTimeoutMilliSecs);
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
    if (nPort != -1 && !DefaultNetworkPorts.isValidPort (nPort))
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
    if (EqualsUtils.equals (sRealUserName, m_sUserName))
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
    if (EqualsUtils.equals (sRealPassword, m_sPassword))
      return EChange.UNCHANGED;
    m_sPassword = sRealPassword;
    return EChange.CHANGED;
  }

  @Nonnull
  public String getCharset ()
  {
    return m_aCharset.name ();
  }

  @Nonnull
  public Charset getCharsetObj ()
  {
    return m_aCharset;
  }

  @Nonnull
  public EChange setCharset (@Nullable final String sCharset)
  {
    try
    {
      final String sRealCharset = StringHelper.hasNoText (sCharset) ? CWebCharset.CHARSET_SMTP : sCharset;
      final Charset aRealCharset = CharsetManager.getCharsetFromName (sRealCharset);
      if (EqualsUtils.equals (aRealCharset, m_aCharset))
        return EChange.UNCHANGED;
      m_aCharset = aRealCharset;
      return EChange.CHANGED;
    }
    catch (final IllegalArgumentException ex)
    {
      s_aLogger.error (ex.getMessage ());
      return EChange.UNCHANGED;
    }
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
    if (!(o instanceof SMTPSettings))
      return false;
    final SMTPSettings rhs = (SMTPSettings) o;
    return m_sHostName.equals (rhs.m_sHostName) &&
           m_nPort == rhs.m_nPort &&
           EqualsUtils.equals (m_sUserName, rhs.m_sUserName) &&
           EqualsUtils.equals (m_sPassword, rhs.m_sPassword) &&
           m_aCharset.equals (rhs.m_aCharset) &&
           m_bSSLEnabled == rhs.m_bSSLEnabled &&
           m_bSTARTTLSEnabled == rhs.m_bSTARTTLSEnabled &&
           m_nConnectionTimeoutMilliSecs == rhs.m_nConnectionTimeoutMilliSecs &&
           m_nTimeoutMilliSecs == rhs.m_nTimeoutMilliSecs;
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
                                       .toString ();
  }
}
