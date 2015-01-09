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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotations.ContainsSoftMigration;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.convert.IMicroTypeConverter;
import com.helger.commons.microdom.impl.MicroElement;
import com.helger.commons.string.StringParser;
import com.helger.web.CWeb;
import com.helger.web.CWebCharset;
import com.helger.web.smtp.EmailGlobalSettings;
import com.helger.web.smtp.ISMTPSettings;

public final class SMTPSettingsMicroTypeConverter implements IMicroTypeConverter
{
  private static final String ATTR_HOST = "host";
  private static final String ATTR_PORT = "port";
  private static final String ATTR_USER = "user";
  private static final String ATTR_PASSWORD = "password";
  private static final String ATTR_CHARSET = "charset";
  private static final String ATTR_SSLENABLED = "sslenabled";
  private static final String ATTR_STARTTLSENABLED = "starttlsenabled";
  private static final String ATTR_CONNECTIONTIMEOUT = "connectiontimeout";
  private static final String ATTR_TIMEOUT = "timeout";

  @Nonnull
  public static IMicroElement convertToMicroElement (@Nonnull final ISMTPSettings aSMTPSettings,
                                                     @Nullable final String sNamespaceURI,
                                                     @Nonnull final String sTagName)
  {
    final IMicroElement eSMTPSettings = new MicroElement (sNamespaceURI, sTagName);
    eSMTPSettings.setAttribute (ATTR_HOST, aSMTPSettings.getHostName ());
    eSMTPSettings.setAttribute (ATTR_PORT, Integer.toString (aSMTPSettings.getPort ()));
    eSMTPSettings.setAttribute (ATTR_USER, aSMTPSettings.getUserName ());
    eSMTPSettings.setAttribute (ATTR_PASSWORD, aSMTPSettings.getPassword ());
    eSMTPSettings.setAttribute (ATTR_CHARSET, aSMTPSettings.getCharset ());
    eSMTPSettings.setAttribute (ATTR_SSLENABLED, Boolean.toString (aSMTPSettings.isSSLEnabled ()));
    eSMTPSettings.setAttribute (ATTR_STARTTLSENABLED, Boolean.toString (aSMTPSettings.isSTARTTLSEnabled ()));
    eSMTPSettings.setAttribute (ATTR_CONNECTIONTIMEOUT, aSMTPSettings.getConnectionTimeoutMilliSecs ());
    eSMTPSettings.setAttribute (ATTR_TIMEOUT, aSMTPSettings.getTimeoutMilliSecs ());
    return eSMTPSettings;
  }

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final Object aSource,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final ISMTPSettings aSMTPSettings = (ISMTPSettings) aSource;
    return convertToMicroElement (aSMTPSettings, sNamespaceURI, sTagName);
  }

  /*
   * The alternative attributes are used to be consistent with old failed mail
   * conversions, as they did the transformation manually!
   */
  @Nonnull
  @ContainsSoftMigration
  public static SMTPSettings convertToSMTPSettings (@Nonnull final IMicroElement eSMTPSettings)
  {
    String sHost = eSMTPSettings.getAttributeValue (ATTR_HOST);
    if (sHost == null)
      sHost = eSMTPSettings.getAttributeValue ("hostname");

    final String sPort = eSMTPSettings.getAttributeValue (ATTR_PORT);
    final int nPort = StringParser.parseInt (sPort, CWeb.DEFAULT_PORT_SMTP);

    String sUser = eSMTPSettings.getAttributeValue (ATTR_USER);
    if (sUser == null)
      sUser = eSMTPSettings.getAttributeValue ("username");

    final String sPassword = eSMTPSettings.getAttributeValue (ATTR_PASSWORD);

    String sCharset = eSMTPSettings.getAttributeValue (ATTR_CHARSET);
    if (sCharset == null)
      sCharset = CWebCharset.CHARSET_SMTP;
    String sSSLEnabled = eSMTPSettings.getAttributeValue (ATTR_SSLENABLED);

    if (sSSLEnabled == null)
      sSSLEnabled = eSMTPSettings.getAttributeValue ("usessl");
    final boolean bSSLEnabled = StringParser.parseBool (sSSLEnabled, EmailGlobalSettings.isUseSSL ());

    final String sSTARTTLSEnabled = eSMTPSettings.getAttributeValue (ATTR_STARTTLSENABLED);
    final boolean bSTARTTLSEnabled = StringParser.parseBool (sSTARTTLSEnabled, EmailGlobalSettings.isUseSTARTTLS ());

    final String sConnectionTimeoutMilliSecs = eSMTPSettings.getAttributeValue (ATTR_CONNECTIONTIMEOUT);
    final long nConnectionTimeoutMilliSecs = StringParser.parseLong (sConnectionTimeoutMilliSecs,
                                                                     EmailGlobalSettings.getConnectionTimeoutMilliSecs ());

    final String sTimeoutMilliSecs = eSMTPSettings.getAttributeValue (ATTR_TIMEOUT);
    final long nTimeoutMilliSecs = StringParser.parseLong (sTimeoutMilliSecs,
                                                           EmailGlobalSettings.getTimeoutMilliSecs ());

    return new SMTPSettings (sHost,
                             nPort,
                             sUser,
                             sPassword,
                             sCharset,
                             bSSLEnabled,
                             bSTARTTLSEnabled,
                             nConnectionTimeoutMilliSecs,
                             nTimeoutMilliSecs);
  }

  /*
   * The alternative attributes are used to be consistent with old failed mail
   * conversions, as they did the transformation manually!
   */
  @Nonnull
  @ContainsSoftMigration
  public SMTPSettings convertToNative (@Nonnull final IMicroElement eSMTPSettings)
  {
    return convertToSMTPSettings (eSMTPSettings);
  }
}
