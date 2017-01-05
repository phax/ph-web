/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import java.io.Serializable;
import java.nio.charset.Charset;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface representing the basic SMTP settings required to login to a user.
 *
 * @author Philip Helger
 */
public interface ISMTPSettings extends Serializable
{
  /**
   * @return The SMTP server host name
   */
  @Nonnull
  String getHostName ();

  /**
   * @return The SMTP server port to use. May be -1 to use the default port.
   */
  int getPort ();

  /**
   * @return The server user name. May be <code>null</code>.
   */
  @Nullable
  String getUserName ();

  /**
   * @return The server user's password. May be <code>null</code>.
   */
  @Nullable
  String getPassword ();

  /**
   * @return The mail encoding to be used. May be <code>null</code>.
   */
  @Nullable
  Charset getCharsetObj ();

  @Nullable
  default String getCharsetName ()
  {
    final Charset aCharset = getCharsetObj ();
    return aCharset == null ? null : aCharset.name ();
  }

  /**
   * @return <code>true</code> if SSL is enabled, <code>false</code> if SSL is
   *         disabled
   */
  boolean isSSLEnabled ();

  /**
   * @return <code>true</code> if STARTTLS is enabled, <code>false</code> if
   *         STARTTLS is disabled
   */
  boolean isSTARTTLSEnabled ();

  /**
   * Get the connection timeout in milliseconds.
   *
   * @return If the value is &le; 0 than there should be no connection timeout.
   */
  @CheckForSigned
  long getConnectionTimeoutMilliSecs ();

  /**
   * Get the socket timeout in milliseconds.
   *
   * @return If the value is &le; 0 than there should be no connection timeout.
   */
  @CheckForSigned
  long getTimeoutMilliSecs ();

  /**
   * @return <code>true</code> to debug the SMTP transactions. By default this
   *         is <code>GlobalDebug.isDebugMode()</code>.
   */
  boolean isDebugSMTP ();

  /**
   * @return <code>true</code> if the minimum number of fields are defined, that
   *         are required for sending.
   */
  boolean areRequiredFieldsSet ();
}
