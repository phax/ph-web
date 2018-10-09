/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.http.tls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;

/**
 * TLS cipher suite configuration modes according to
 * https://wiki.mozilla.org/Security/Server_Side_TLS from 2018-10-09
 *
 * @author Philip Helger
 * @since 9.0.5
 */
public enum ETLSConfigurationMode implements IHasID <String>, ITLSConfigurationMode
{
  /**
   * For services that don't need backward compatibility, the parameters below
   * provide a higher level of security. This configuration is compatible with
   * Firefox 27, Chrome 30, IE 11 on Windows 7, Edge, Opera 17, Safari 9, Android
   * 5.0, and Java 8.
   */
  MODERN ("modern",
          new ETLSVersion [] { ETLSVersion.TLS_12 },
          new String [] { "ECDHE-ECDSA-AES256-GCM-SHA384",
                          "ECDHE-RSA-AES256-GCM-SHA384",
                          "ECDHE-ECDSA-CHACHA20-POLY1305",
                          "ECDHE-RSA-CHACHA20-POLY1305",
                          "ECDHE-ECDSA-AES128-GCM-SHA256",
                          "ECDHE-RSA-AES128-GCM-SHA256",
                          "ECDHE-ECDSA-AES256-SHA384",
                          "ECDHE-RSA-AES256-SHA384",
                          "ECDHE-ECDSA-AES128-SHA256",
                          "ECDHE-RSA-AES128-SHA256" }),
  /**
   * For services that don't need compatibility with legacy clients (mostly
   * WinXP), but still need to support a wide range of clients, this configuration
   * is recommended. It is is compatible with Firefox 1, Chrome 1, IE 7, Opera 5
   * and Safari 1.
   */
  INTERMEDIATE ("intermediate",
                new ETLSVersion [] { ETLSVersion.TLS_12, ETLSVersion.TLS_11, ETLSVersion.TLS_10 },
                new String [] { "ECDHE-ECDSA-CHACHA20-POLY1305",
                                "ECDHE-RSA-CHACHA20-POLY1305",
                                "ECDHE-ECDSA-AES128-GCM-SHA256",
                                "ECDHE-RSA-AES128-GCM-SHA256",
                                "ECDHE-ECDSA-AES256-GCM-SHA384",
                                "ECDHE-RSA-AES256-GCM-SHA384",
                                "DHE-RSA-AES128-GCM-SHA256",
                                "DHE-RSA-AES256-GCM-SHA384",
                                "ECDHE-ECDSA-AES128-SHA256",
                                "ECDHE-RSA-AES128-SHA256",
                                "ECDHE-ECDSA-AES128-SHA",
                                "ECDHE-RSA-AES256-SHA384",
                                "ECDHE-RSA-AES128-SHA",
                                "ECDHE-ECDSA-AES256-SHA384",
                                "ECDHE-ECDSA-AES256-SHA",
                                "ECDHE-RSA-AES256-SHA",
                                "DHE-RSA-AES128-SHA256",
                                "DHE-RSA-AES128-SHA",
                                "DHE-RSA-AES256-SHA256",
                                "DHE-RSA-AES256-SHA",
                                "ECDHE-ECDSA-DES-CBC3-SHA",
                                "ECDHE-RSA-DES-CBC3-SHA",
                                "EDH-RSA-DES-CBC3-SHA",
                                "AES128-GCM-SHA256",
                                "AES256-GCM-SHA384",
                                "AES128-SHA256",
                                "AES256-SHA256",
                                "AES128-SHA",
                                "AES256-SHA",
                                "DES-CBC3-SHA",
                                "!DSS" }),
  /**
   * This is the old ciphersuite that works with all clients back to Windows
   * XP/IE6. It should be used as a last resort only.
   */
  OLD ("old",
       new ETLSVersion [] { ETLSVersion.TLS_12, ETLSVersion.TLS_11, ETLSVersion.TLS_10, ETLSVersion.SSL_V3 },
       new String [] { "ECDHE-ECDSA-CHACHA20-POLY1305",
                       "ECDHE-RSA-CHACHA20-POLY1305",
                       "ECDHE-RSA-AES128-GCM-SHA256",
                       "ECDHE-ECDSA-AES128-GCM-SHA256",
                       "ECDHE-RSA-AES256-GCM-SHA384",
                       "ECDHE-ECDSA-AES256-GCM-SHA384",
                       "DHE-RSA-AES128-GCM-SHA256",
                       "DHE-DSS-AES128-GCM-SHA256",
                       "kEDH+AESGCM",
                       "ECDHE-RSA-AES128-SHA256",
                       "ECDHE-ECDSA-AES128-SHA256",
                       "ECDHE-RSA-AES128-SHA",
                       "ECDHE-ECDSA-AES128-SHA",
                       "ECDHE-RSA-AES256-SHA384",
                       "ECDHE-ECDSA-AES256-SHA384",
                       "ECDHE-RSA-AES256-SHA",
                       "ECDHE-ECDSA-AES256-SHA",
                       "DHE-RSA-AES128-SHA256",
                       "DHE-RSA-AES128-SHA",
                       "DHE-DSS-AES128-SHA256",
                       "DHE-RSA-AES256-SHA256",
                       "DHE-DSS-AES256-SHA",
                       "DHE-RSA-AES256-SHA",
                       "ECDHE-RSA-DES-CBC3-SHA",
                       "ECDHE-ECDSA-DES-CBC3-SHA",
                       "EDH-RSA-DES-CBC3-SHA",
                       "AES128-GCM-SHA256",
                       "AES256-GCM-SHA384",
                       "AES128-SHA256",
                       "AES256-SHA256",
                       "AES128-SHA",
                       "AES256-SHA",
                       "AES",
                       "DES-CBC3-SHA",
                       "HIGH",
                       "SEED",
                       "!aNULL",
                       "!eNULL",
                       "!EXPORT",
                       "!DES",
                       "!RC4",
                       "!MD5",
                       "!PSK",
                       "!RSAPSK",
                       "!aDH",
                       "!aECDH",
                       "!EDH-DSS-DES-CBC3-SHA",
                       "!KRB5-DES-CBC3-SHA",
                       "!SRP" });

  private String m_sID;
  private TLSConfigurationMode m_aMode;

  private ETLSConfigurationMode (@Nonnull @Nonempty final String sID,
                                 @Nonnull @Nonempty final ETLSVersion [] aTLSVersions,
                                 @Nonnull @Nonempty final String [] aCipherSuites)
  {
    m_sID = sID;
    m_aMode = new TLSConfigurationMode (aTLSVersions, aCipherSuites);
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <String> getAllCipherSuites ()
  {
    return m_aMode.getAllCipherSuites ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public String [] getAllCipherSuitesAsArray ()
  {
    return m_aMode.getAllCipherSuitesAsArray ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <ETLSVersion> getAllTLSVersions ()
  {
    return m_aMode.getAllTLSVersions ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <String> getAllTLSVersionIDs ()
  {
    return m_aMode.getAllTLSVersionIDs ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public String [] getAllTLSVersionIDsAsArray ()
  {
    return m_aMode.getAllTLSVersionIDsAsArray ();
  }

  @Nullable
  public static ETLSConfigurationMode getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (ETLSConfigurationMode.class, sID);
  }
}
