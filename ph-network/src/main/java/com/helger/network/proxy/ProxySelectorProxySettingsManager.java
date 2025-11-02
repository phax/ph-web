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
package com.helger.network.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.style.OverrideOnDemand;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.EHandled;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsOrderedSet;
import com.helger.network.proxy.settings.IProxySettings;
import com.helger.network.proxy.settings.ProxySettingsManager;

/**
 * An implementation of {@link ProxySelector} that uses {@link ProxySettingsManager} to fetch the
 * data. To install this proxy selector globally, use the method {@link #setAsDefault(boolean)}.
 *
 * @author Philip Helger
 */
public class ProxySelectorProxySettingsManager extends ProxySelector
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ProxySelectorProxySettingsManager.class);

  private final ProxySelector m_aFallback;

  /**
   * Constructor
   *
   * @param aFallback
   *        Fallback {@link ProxySelector} to be used, if no matches are found in
   *        {@link ProxySettingsManager}. May be <code>null</code>.
   */
  public ProxySelectorProxySettingsManager (@Nullable final ProxySelector aFallback)
  {
    m_aFallback = aFallback;
  }

  /**
   * @return The fallback instance as provided in the constructor.
   */
  @Nullable
  public final ProxySelector getFallbackProxySelector ()
  {
    return m_aFallback;
  }

  /**
   * @param aURI
   *        The URI that a connection is required to. Never <code>null</code>.
   * @return May be <code>null</code> in which case no proxy will be used.
   */
  @Nullable
  @OverrideOnDemand
  protected List <Proxy> selectProxies (@NonNull final URI aURI)
  {
    // 1. search from ProxySettingsManager
    final ICommonsOrderedSet <IProxySettings> aProxySettings = ProxySettingsManager.findAllProxySettings (aURI);
    if (aProxySettings.isNotEmpty ())
      return new CommonsArrayList <> (aProxySettings, IProxySettings::getAsProxy);

    // 2. fallback to previous selector
    if (m_aFallback != null)
      return m_aFallback.select (aURI);

    // None at all
    return null;
  }

  @Override
  @NonNull
  public List <Proxy> select (@NonNull final URI aURI)
  {
    ValueEnforcer.notNull (aURI, "URI");

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Selecting proxies for '" + aURI + "'");

    List <Proxy> ret = selectProxies (aURI);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("  For '" + aURI + "' the following proxies were selected: " + ret);

    if (ret == null || ret.isEmpty ())
    {
      // Fall back to "no proxy"
      ret = new CommonsArrayList <> (Proxy.NO_PROXY);
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("  Using no proxy for '" + aURI + "'");
    }

    return ret;
  }

  /**
   * @param aURI
   *        The URI that the proxy failed to serve. Never <code>null</code>.
   * @param aAddr
   *        The socket address of the proxy/SOCKS server. Never <code>null</code>.
   * @param ex
   *        The I/O exception thrown when the connect failed. Never <code>null</code>.
   * @return {@link EHandled}
   */
  @NonNull
  @OverrideOnDemand
  protected EHandled handleConnectFailed (@NonNull final URI aURI,
                                          @NonNull final SocketAddress aAddr,
                                          @NonNull final IOException ex)
  {
    // Logging is done inside
    return ProxySettingsManager.onConnectionFailed (aURI, aAddr, ex);
  }

  @Override
  public void connectFailed (@NonNull final URI aURI, @NonNull final SocketAddress aAddr, @NonNull final IOException ex)
  {
    ValueEnforcer.notNull (aURI, "URI");
    ValueEnforcer.notNull (aAddr, "SockerAddr");
    ValueEnforcer.notNull (ex, "Exception");

    LOGGER.info ("Connection to '" + aURI + "' using proxy " + aAddr + " failed", ex);

    if (handleConnectFailed (aURI, aAddr, ex).isUnhandled ())
    {
      // Pass to default (if present)
      if (m_aFallback != null)
        m_aFallback.connectFailed (aURI, aAddr, ex);
    }
  }

  public static boolean isDefault ()
  {
    return ProxySelector.getDefault () instanceof ProxySelectorProxySettingsManager;
  }

  public static void setAsDefault (final boolean bUseOldAsFallback)
  {
    final ProxySelector aDefault = ProxySelector.getDefault ();
    if (!(aDefault instanceof ProxySelectorProxySettingsManager))
    {
      ProxySelector.setDefault (new ProxySelectorProxySettingsManager (bUseOldAsFallback ? aDefault : null));
      LOGGER.info ("Using ProxySelectorProxySettingsManager as the default ProxySelector");
    }
  }
}
