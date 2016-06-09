/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.smtp.EmailGlobalSettings;

/**
 * Read-only implementation of the {@link ISMTPSettings} interface. It is
 * implemented by using a writable version and only accessing the read-methods.
 * THis is done to avoid code duplication.
 *
 * @author Philip Helger
 */
@Immutable
public class ReadOnlySMTPSettings implements ISMTPSettings
{
  private final SMTPSettings m_aSettings;

  /**
   * Constructor for internal deserialization only
   *
   * @param aOther
   *        The settings to use. May not be <code>null</code>. They are not
   *        copied, because the new object is directly created in the
   *        serialization context.
   */
  ReadOnlySMTPSettings (@Nonnull final SMTPSettings aOther)
  {
    m_aSettings = ValueEnforcer.notNull (aOther, "Other");
  }

  /**
   * Constructor which copies settings from another object
   *
   * @param aOther
   *        The settings to use. May not be <code>null</code>.
   */
  public ReadOnlySMTPSettings (@Nonnull final ISMTPSettings aOther)
  {
    this (aOther.getHostName (),
          aOther.getPort (),
          aOther.getUserName (),
          aOther.getPassword (),
          aOther.getCharset (),
          aOther.isSSLEnabled (),
          aOther.isSTARTTLSEnabled (),
          aOther.getConnectionTimeoutMilliSecs (),
          aOther.getTimeoutMilliSecs (),
          aOther.isDebugSMTP ());
  }

  /**
   * Constructor with default port, and no authentication
   *
   * @param sHostName
   *        SMTP server name or IP address. May neither be <code>null</code> nor
   *        empty.
   */
  public ReadOnlySMTPSettings (@Nonnull final String sHostName)
  {
    this (sHostName, -1, null, null, null, EmailGlobalSettings.isUseSSL ());
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
  public ReadOnlySMTPSettings (@Nonnull final String sHostName,
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
   * @param sCharset
   *        The charset to use. May be <code>null</code>.
   * @param bSSLEnabled
   *        <code>true</code> to enable SSL communications
   * @param bSTARTTLSEnabled
   *        <code>true</code> to enable STARTTLS communications
   * @param nConnectTimeoutMilliSecs
   *        the connection timeout in milliseconds.
   * @param nTimeoutMilliSecs
   *        the socket timeout in milliseconds.
   * @param bIsDebugSMTP
   *        <code>true</code> to enable SMTP debugging.
   */
  public ReadOnlySMTPSettings (@Nonnull final String sHostName,
                               final int nPort,
                               @Nullable final String sUserName,
                               @Nullable final String sPassword,
                               @Nullable final String sCharset,
                               final boolean bSSLEnabled,
                               final boolean bSTARTTLSEnabled,
                               final long nConnectTimeoutMilliSecs,
                               final long nTimeoutMilliSecs,
                               final boolean bIsDebugSMTP)
  {
    m_aSettings = new SMTPSettings (sHostName,
                                    nPort,
                                    sUserName,
                                    sPassword,
                                    sCharset,
                                    bSSLEnabled,
                                    bSTARTTLSEnabled,
                                    nConnectTimeoutMilliSecs,
                                    nTimeoutMilliSecs,
                                    bIsDebugSMTP);
  }

  @Nonnull
  public String getHostName ()
  {
    return m_aSettings.getHostName ();
  }

  public int getPort ()
  {
    return m_aSettings.getPort ();
  }

  @Nullable
  public String getUserName ()
  {
    return m_aSettings.getUserName ();
  }

  @Nullable
  public String getPassword ()
  {
    return m_aSettings.getPassword ();
  }

  @Nonnull
  public String getCharset ()
  {
    return m_aSettings.getCharset ();
  }

  @Nonnull
  public Charset getCharsetObj ()
  {
    return m_aSettings.getCharsetObj ();
  }

  public boolean isSSLEnabled ()
  {
    return m_aSettings.isSSLEnabled ();
  }

  public boolean isSTARTTLSEnabled ()
  {
    return m_aSettings.isSTARTTLSEnabled ();
  }

  public long getConnectionTimeoutMilliSecs ()
  {
    return m_aSettings.getConnectionTimeoutMilliSecs ();
  }

  public long getTimeoutMilliSecs ()
  {
    return m_aSettings.getTimeoutMilliSecs ();
  }

  public boolean isDebugSMTP ()
  {
    return m_aSettings.isDebugSMTP ();
  }

  public boolean areRequiredFieldsSet ()
  {
    return m_aSettings.areRequiredFieldsSet ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (this == o)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final ReadOnlySMTPSettings rhs = (ReadOnlySMTPSettings) o;
    return m_aSettings.equals (rhs.m_aSettings);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aSettings).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("settings", m_aSettings).toString ();
  }
}
