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
package com.helger.network.proxy.config;

import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.system.SystemProperties;
import com.helger.network.port.NetworkPortHelper;

/**
 * SOCKS proxy configuration.
 *
 * @author Philip Helger
 */
@Immutable
public class SocksProxyConfig implements IProxyConfig
{
  public static final String SYSPROP_SOCKS_PROXY_HOST = "socksProxyHost";
  public static final String SYSPROP_SOCKS_PROXY_PORT = "socksProxyPort";
  public static final int DEFAULT_SOCKS_PROXY_PORT = 1080;

  private final String m_sHost;
  private final int m_nPort;

  /**
   * Create a SOCKS proxy config object based on the default port
   * {@link #DEFAULT_SOCKS_PROXY_PORT}.
   *
   * @param sHost
   *        The SOCKS proxy host. May not be <code>null</code>.
   */
  public SocksProxyConfig (@Nonnull final String sHost)
  {
    this (sHost, DEFAULT_SOCKS_PROXY_PORT);
  }

  /**
   * Create a SOCKS proxy config object based on the given port.
   *
   * @param sHost
   *        The SOCKS proxy host. May not be <code>null</code>.
   * @param nPort
   *        The port to use for communication. Must be &ge; 0.
   */
  public SocksProxyConfig (@Nonnull final String sHost, @Nonnegative final int nPort)
  {
    ValueEnforcer.notEmpty (sHost, "Host");
    ValueEnforcer.isTrue (NetworkPortHelper.isValidPort (nPort), () -> "The passed port " + nPort + " is invalid");
    m_sHost = sHost;
    m_nPort = nPort;
  }

  @Nonnull
  public String getHost ()
  {
    return m_sHost;
  }

  @Nonnegative
  public int getPort ()
  {
    return m_nPort;
  }

  /**
   * @return The current proxy host for SOCKS proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getProxyHost ()
  {
    return SystemProperties.getPropertyValueOrNull (SYSPROP_SOCKS_PROXY_HOST);
  }

  /**
   * @return The current proxy port for SOCKS proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getProxyPort ()
  {
    return SystemProperties.getPropertyValueOrNull (SYSPROP_SOCKS_PROXY_PORT);
  }

  public void activateGlobally ()
  {
    // Deactivate other proxy configurations
    HttpProxyConfig.deactivateGlobally ();
    UseSystemProxyConfig.deactivateGlobally ();

    SystemProperties.setPropertyValue (SYSPROP_SOCKS_PROXY_HOST, m_sHost);
    SystemProperties.setPropertyValue (SYSPROP_SOCKS_PROXY_PORT, Integer.toString (m_nPort));
  }

  public static void deactivateGlobally ()
  {
    SystemProperties.removePropertyValue (SYSPROP_SOCKS_PROXY_HOST);
    SystemProperties.removePropertyValue (SYSPROP_SOCKS_PROXY_PORT);
  }

  @Nonnull
  public Proxy getAsProxy ()
  {
    return new Proxy (Proxy.Type.SOCKS, new InetSocketAddress (m_sHost, m_nPort));
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final SocksProxyConfig rhs = (SocksProxyConfig) o;
    return m_sHost.equals (rhs.m_sHost) && m_nPort == rhs.m_nPort;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sHost).append (m_nPort).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("host", m_sHost).append ("port", m_nPort).getToString ();
  }
}
