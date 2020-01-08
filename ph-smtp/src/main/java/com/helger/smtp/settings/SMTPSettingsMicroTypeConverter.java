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

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.ContainsSoftMigration;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.string.StringParser;
import com.helger.smtp.CSMTP;
import com.helger.smtp.EmailGlobalSettings;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroElement;
import com.helger.xml.microdom.convert.IMicroTypeConverter;

public final class SMTPSettingsMicroTypeConverter implements IMicroTypeConverter <SMTPSettings>
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
  private static final String ATTR_DEBUG_SMTP = "debugsmtp";

  @Nonnull
  public static IMicroElement convertToMicroElementStatic (@Nonnull final ISMTPSettings aSMTPSettings,
                                                           @Nullable final String sNamespaceURI,
                                                           @Nonnull final String sTagName)
  {
    final IMicroElement eSMTPSettings = new MicroElement (sNamespaceURI, sTagName);
    eSMTPSettings.setAttribute (ATTR_HOST, aSMTPSettings.getHostName ());
    eSMTPSettings.setAttribute (ATTR_PORT, aSMTPSettings.getPort ());
    eSMTPSettings.setAttribute (ATTR_USER, aSMTPSettings.getUserName ());
    eSMTPSettings.setAttribute (ATTR_PASSWORD, aSMTPSettings.getPassword ());
    eSMTPSettings.setAttribute (ATTR_CHARSET, aSMTPSettings.getCharsetName ());
    eSMTPSettings.setAttribute (ATTR_SSLENABLED, aSMTPSettings.isSSLEnabled ());
    eSMTPSettings.setAttribute (ATTR_STARTTLSENABLED, aSMTPSettings.isSTARTTLSEnabled ());
    eSMTPSettings.setAttribute (ATTR_CONNECTIONTIMEOUT, aSMTPSettings.getConnectionTimeoutMilliSecs ());
    eSMTPSettings.setAttribute (ATTR_TIMEOUT, aSMTPSettings.getTimeoutMilliSecs ());
    eSMTPSettings.setAttribute (ATTR_DEBUG_SMTP, aSMTPSettings.isDebugSMTP ());
    return eSMTPSettings;
  }

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final SMTPSettings aSource,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    return convertToMicroElementStatic (aSource, sNamespaceURI, sTagName);
  }

  @Nonnull
  public static SMTPSettings convertToSMTPSettings (@Nonnull final IMicroElement eSMTPSettings)
  {
    final String sHost = eSMTPSettings.getAttributeValue (ATTR_HOST);

    final String sPort = eSMTPSettings.getAttributeValue (ATTR_PORT);
    final int nPort = StringParser.parseInt (sPort, CSMTP.DEFAULT_PORT_SMTP);

    final String sUser = eSMTPSettings.getAttributeValue (ATTR_USER);

    final String sPassword = eSMTPSettings.getAttributeValue (ATTR_PASSWORD);

    final String sCharset = eSMTPSettings.getAttributeValue (ATTR_CHARSET);
    Charset aCharset = null;
    if (sCharset != null)
      aCharset = CharsetHelper.getCharsetFromName (sCharset);

    final String sSSLEnabled = eSMTPSettings.getAttributeValue (ATTR_SSLENABLED);
    final boolean bSSLEnabled = StringParser.parseBool (sSSLEnabled, EmailGlobalSettings.isUseSSL ());

    final String sSTARTTLSEnabled = eSMTPSettings.getAttributeValue (ATTR_STARTTLSENABLED);
    final boolean bSTARTTLSEnabled = StringParser.parseBool (sSTARTTLSEnabled, EmailGlobalSettings.isUseSTARTTLS ());

    final String sConnectionTimeoutMilliSecs = eSMTPSettings.getAttributeValue (ATTR_CONNECTIONTIMEOUT);
    final long nConnectionTimeoutMilliSecs = StringParser.parseLong (sConnectionTimeoutMilliSecs,
                                                                     EmailGlobalSettings.getConnectionTimeoutMilliSecs ());

    final String sTimeoutMilliSecs = eSMTPSettings.getAttributeValue (ATTR_TIMEOUT);
    final long nTimeoutMilliSecs = StringParser.parseLong (sTimeoutMilliSecs,
                                                           EmailGlobalSettings.getTimeoutMilliSecs ());

    final String sDebugSMTP = eSMTPSettings.getAttributeValue (ATTR_DEBUG_SMTP);
    final boolean bDebugSMTP = StringParser.parseBool (sDebugSMTP, EmailGlobalSettings.isDebugSMTP ());

    return new SMTPSettings (sHost,
                             nPort,
                             sUser,
                             sPassword,
                             aCharset,
                             bSSLEnabled,
                             bSTARTTLSEnabled,
                             nConnectionTimeoutMilliSecs,
                             nTimeoutMilliSecs,
                             bDebugSMTP);
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
