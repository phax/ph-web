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
package com.helger.network.proxy.settings;

import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.string.StringHelper;

/**
 * Generic proxy settings interface.
 *
 * @author Philip Helger
 * @since 9.1.3
 */
public interface IProxySettings extends Serializable
{
  /**
   * @return The proxy type to be used. May not be <code>null</code>.
   */
  @Nonnull
  Proxy.Type getProxyType ();

  /**
   * @return The proxy host name of IP address. May be <code>null</code>.
   */
  @Nullable
  String getProxyHost ();

  /**
   * @return The proxy port for this HTTP proxy type. Should be &gt; 0.
   */
  @Nonnegative
  int getProxyPort ();

  /**
   * @return The proxy user name. May be <code>null</code>.
   */
  @Nullable
  String getProxyUserName ();

  default boolean hasProxyUserName ()
  {
    return StringHelper.hasText (getProxyUserName ());
  }

  /**
   * @return The proxy password for the provided user. May be <code>null</code>.
   *         Note: an empty password may be valid. Only <code>null</code>
   *         indicates "no password".
   */
  @Nullable
  String getProxyPassword ();

  default boolean hasProxyPassword ()
  {
    return getProxyPassword () != null;
  }

  /**
   * @return A non-<code>null</code> {@link Proxy} instance. Only uses proxy
   *         host and port.
   * @see #getProxyHost()
   * @see #getProxyPort()
   */
  @Nonnull
  default Proxy getAsProxy ()
  {
    return getAsProxy (true);
  }

  /**
   * @param bResolveHostname
   *        <code>true</code> to resolve host names (needed in production) or
   *        <code>false</code> to not resolve them (mainly for testing
   *        purposes).
   * @return A non-<code>null</code> {@link Proxy} instance. Only uses proxy
   *         host and port.
   * @see #getProxyHost()
   * @see #getProxyPort()
   */
  @Nonnull
  Proxy getAsProxy (boolean bResolveHostname);

  /**
   * @return The {@link PasswordAuthentication} instances matching the
   *         credentials contained in this object or <code>null</code> if no
   *         username is present.
   */
  @Nullable
  default PasswordAuthentication getAsPasswordAuthentication ()
  {
    // If no user name is set, no Authenticator needs to be created
    if (!hasProxyUserName ())
      return null;

    final String sProxyPassword = getProxyPassword ();
    // Constructor does not take null password!
    return new PasswordAuthentication (getProxyUserName (),
                                       sProxyPassword == null ? new char [0] : sProxyPassword.toCharArray ());
  }
}
