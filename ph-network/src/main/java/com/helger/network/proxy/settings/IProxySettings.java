/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.CGlobal;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.string.StringHelper;

/**
 * Generic proxy settings interface.
 *
 * @author Philip Helger
 * @since 9.0.2
 */
public interface IProxySettings
{
  /**
   * @return The proxy type to be used. May not be <code>null</code>.
   */
  Proxy.@NonNull Type getProxyType ();

  /**
   * @return The proxy host name of IP address. May be <code>null</code> if proxy type is DIRECT.
   */
  @Nullable
  String getProxyHost ();

  /**
   * @return The proxy port for this HTTP proxy type. Should be &gt; 0. May be &le; 0 if the proxy
   *         type is DIRECT.
   */
  int getProxyPort ();

  /**
   * @return The proxy user name. May be <code>null</code>.
   */
  @Nullable
  String getProxyUserName ();

  default boolean hasProxyUserName ()
  {
    return StringHelper.isNotEmpty (getProxyUserName ());
  }

  /**
   * @return The proxy password for the provided user. May be <code>null</code>. Note: an empty
   *         password may be valid. Only <code>null</code> indicates "no password".
   */
  @Nullable
  String getProxyPassword ();

  default boolean hasProxyPassword ()
  {
    return getProxyPassword () != null;
  }

  /**
   * Check if hostname and port match the ones from the provided {@link InetSocketAddress}.
   *
   * @param aAddr
   *        The address to compare with. May be <code>null</code>.
   * @return <code>true</code> if the unresolved hostname and the port match.
   */
  default boolean hasInetSocketAddress (@Nullable final InetSocketAddress aAddr)
  {
    return aAddr != null &&
           EqualsHelper.equals (aAddr.getHostString (), getProxyHost ()) &&
           getProxyPort () == aAddr.getPort ();
  }

  /**
   * Check if these settings have the provided socket address.
   *
   * @param aAddr
   *        The socket address to compare to. May be <code>null</code>.
   * @return <code>true</code> if the proxy type is DIRECT and the address is <code>null</code>, or
   *         if the object is of type {@link InetSocketAddress} and the values match.
   * @see #hasInetSocketAddress(InetSocketAddress)
   */
  boolean hasSocketAddress (@Nullable SocketAddress aAddr);

  /**
   * @return A non-<code>null</code> {@link Proxy} instance. Only uses proxy host and port.
   * @see #getProxyHost()
   * @see #getProxyPort()
   */
  @NonNull
  default Proxy getAsProxy ()
  {
    return getAsProxy (true);
  }

  /**
   * @param bResolveHostname
   *        <code>true</code> to resolve host names (needed in production) or <code>false</code> to
   *        not resolve them (mainly for testing purposes). This flag has no impact if the proxy
   *        type is DIRECT.
   * @return A non-<code>null</code> {@link Proxy} instance. Only uses proxy host and port.
   * @see #getProxyHost()
   * @see #getProxyPort()
   */
  @NonNull
  Proxy getAsProxy (boolean bResolveHostname);

  /**
   * @return The {@link PasswordAuthentication} instances matching the credentials contained in this
   *         object or <code>null</code> if no username is present.
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
                                       sProxyPassword == null ? CGlobal.EMPTY_CHAR_ARRAY : sProxyPassword
                                                                                                         .toCharArray ());
  }
}
