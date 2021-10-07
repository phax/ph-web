/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.network.proxy.config;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.id.IHasID;
import com.helger.commons.string.StringParser;
import com.helger.commons.system.SystemProperties;
import com.helger.commons.url.IURLProtocol;

/**
 * Proxy type determination interface.<br>
 * Source:
 * http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 *
 * @author Philip Helger
 */
public interface IProxySettingsPerProtocol extends IHasID <String>
{
  /**
   * @return The URL protocol for which the proxy can be used. May not be
   *         <code>null</code>.
   */
  @Nonnull
  IURLProtocol getURLProtocol ();

  @Nonnegative
  int getDefaultPort ();

  /**
   * @return The name of the system property for getting and setting the proxy
   *         host
   */
  @Nonnull
  default String getPropertyNameProxyHost ()
  {
    return getID () + ".proxyHost";
  }

  /**
   * @return The current proxy host for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  default String getProxyHost ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyHost ());
  }

  /**
   * @return The name of the system property for getting and setting the proxy
   *         port
   */
  @Nonnull
  default String getPropertyNameProxyPort ()
  {
    return getID () + ".proxyPort";
  }

  /**
   * @return The current proxy port for this HTTP proxy type. May be
   *         <code>-1</code> for "undefined".
   */
  @CheckForSigned
  default int getProxyPort ()
  {
    return StringParser.parseInt (SystemProperties.getPropertyValueOrNull (getPropertyNameProxyPort ()), -1);
  }

  /**
   * @return The name of the system property for getting and setting the proxy
   *         user name
   */
  @Nonnull
  default String getPropertyNameProxyUserName ()
  {
    return getID () + ".proxyUser";
  }

  /**
   * @return The current proxy user for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  default String getProxyUserName ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyUserName ());
  }

  /**
   * @return The name of the system property for getting and setting the proxy
   *         password
   */
  @Nonnull
  default String getPropertyNameProxyPassword ()
  {
    return getID () + ".proxyPassword";
  }

  /**
   * @return The current proxy password for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  default String getProxyPassword ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyPassword ());
  }

  /**
   * @return The name of the system property for getting and setting the
   *         non-proxy hosts
   */
  @Nonnull
  default String getPropertyNameNoProxyHosts ()
  {
    return getID () + ".noProxyHosts";
  }

  /**
   * @return The current non-proxy hosts for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  default String getNoProxyHosts ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameNoProxyHosts ());
  }
}
