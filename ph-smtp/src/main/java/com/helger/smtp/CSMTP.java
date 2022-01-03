/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.network.port.DefaultNetworkPorts;

/**
 * Contains some global web constants
 *
 * @author Philip Helger
 */
@Immutable
public final class CSMTP
{
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

  /** Default charset for SMTP: UTF-8 */
  public static final Charset CHARSET_SMTP_OBJ = StandardCharsets.UTF_8;

  @PresentForCodeCoverage
  private static final CSMTP INSTANCE = new CSMTP ();

  private CSMTP ()
  {}
}
