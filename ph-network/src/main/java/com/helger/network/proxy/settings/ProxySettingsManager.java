/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.state.EChange;
import com.helger.commons.state.EHandled;

/**
 * Static manager class for {@link IProxySettingsProvider}.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class ProxySettingsManager
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ProxySettingsManager.class);
  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static final ICommonsList <IProxySettingsProvider> LIST = new CommonsArrayList <> ();

  private ProxySettingsManager ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <IProxySettingsProvider> getAllProviders ()
  {
    return RW_LOCK.readLockedGet (LIST::getClone);
  }

  public static void registerProvider (@Nonnull final IProxySettingsProvider aProvider)
  {
    ValueEnforcer.notNull (aProvider, "Provider");
    RW_LOCK.writeLocked ( () -> LIST.add (aProvider));

    LOGGER.info ("Registered proxy settings provider " + aProvider);
  }

  @Nonnull
  public static EChange unregisterProvider (@Nullable final IProxySettingsProvider aProvider)
  {
    if (aProvider == null)
      return EChange.UNCHANGED;

    final EChange eChange = RW_LOCK.writeLockedGet ( () -> LIST.removeObject (aProvider));
    if (eChange.isChanged ())
      LOGGER.info ("Unregistered proxy settings provider " + aProvider);
    return eChange;
  }

  @Nonnull
  public static EChange removeAllProviders ()
  {
    final EChange eChange = RW_LOCK.writeLockedGet (LIST::removeAll);
    if (eChange.isChanged ())
      LOGGER.info ("Removed all proxy settings provider");
    return eChange;
  }

  /**
   * Find all proxy settings matching the provided parameters.
   *
   * @param aURI
   *        Destination URI
   * @return A non-<code>null</code> set with all matching proxy settings. A set
   *         is used to avoid that the same settings are used more than once.
   * @see #findAllProxySettings(String, String, int)
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedSet <IProxySettings> findAllProxySettings (@Nonnull final URI aURI)
  {
    return findAllProxySettings (aURI.getScheme (), aURI.getHost (), aURI.getPort ());
  }

  /**
   * Find all proxy settings matching the provided parameters.
   *
   * @param sProtocol
   *        Destination server protocol.
   * @param sHostName
   *        Destination host name
   * @param nPort
   *        Destination port
   * @return A non-<code>null</code> set with all matching proxy settings. A set
   *         is used to avoid that the same settings are used more than once.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedSet <IProxySettings> findAllProxySettings (@Nullable final String sProtocol,
                                                                          @Nullable final String sHostName,
                                                                          @CheckForSigned final int nPort)
  {
    final ICommonsOrderedSet <IProxySettings> ret = new CommonsLinkedHashSet <> ();
    for (final IProxySettingsProvider aProvider : getAllProviders ())
    {
      final ICommonsList <IProxySettings> aAll = aProvider.getAllProxySettings (sProtocol, sHostName, nPort);
      if (aAll != null)
        ret.addAll (aAll);
    }
    return ret;
  }

  @Nonnull
  public static EHandled onConnectionFailed (@Nonnull final URI aURI,
                                             @Nonnull final SocketAddress aAddr,
                                             @Nonnull final IOException ex)
  {
    final String sProtocol = aURI.getScheme ();
    final String sHostName = aURI.getHost ();
    final int nPort = aURI.getPort ();

    int nInvokedProviders = 0;

    // For all providers
    for (final IProxySettingsProvider aProvider : getAllProviders ())
    {
      final ICommonsList <IProxySettings> aMatches = aProvider.getAllProxySettings (sProtocol, sHostName, nPort);
      // For all matching proxies
      if (aMatches != null)
        for (final IProxySettings aProxySettings : aMatches)
          if (aProxySettings.hasSocketAddress (aAddr))
          {
            // Found a matching proxy
            aProvider.onConnectionFailed (aProxySettings, aURI, aAddr, ex);
            nInvokedProviders++;
          }
    }

    return EHandled.valueOf (nInvokedProviders > 0);
  }
}
