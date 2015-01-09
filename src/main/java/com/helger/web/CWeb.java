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
package com.helger.web;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.web.port.DefaultNetworkPorts;

/**
 * Contains some global web constants
 * 
 * @author Philip Helger
 */
@Immutable
public final class CWeb
{
  /** Default FTP port */
  public static final int DEFAULT_PORT_FTP = DefaultNetworkPorts.TCP_21_ftp.getPort ();
  /** Default HTTP port */
  public static final int DEFAULT_PORT_HTTP = DefaultNetworkPorts.TCP_80_http.getPort ();
  /** Default HTTPS port */
  public static final int DEFAULT_PORT_HTTPS = DefaultNetworkPorts.TCP_443_https.getPort ();
  /** Default SMTP port */
  public static final int DEFAULT_PORT_SMTP = DefaultNetworkPorts.TCP_25_smtp.getPort ();
  /** Default POP3 port */
  public static final int DEFAULT_PORT_POP3 = DefaultNetworkPorts.TCP_110_pop3.getPort ();
  /** Default SSMTP port */
  public static final int DEFAULT_PORT_SECURE_SMTP = 465;
  /** Default secure IMAP4-SSL port */
  public static final int DEFAULT_PORT_SECURE_IMAP = 585;
  /** Default IMAP4 over SSL port */
  public static final int DEFAULT_PORT_IMAP_SSL = 993;
  /** Default Secure POP3 port */
  public static final int DEFAULT_PORT_SECURE_POP3 = 995;

  /** The scheme for HTTP */
  public static final String SCHEME_HTTP = "http";
  /** The scheme for HTTPS */
  public static final String SCHEME_HTTPS = "https";

  @PresentForCodeCoverage
  @SuppressWarnings ("unused")
  private static final CWeb s_aInstance = new CWeb ();

  private CWeb ()
  {}
}
