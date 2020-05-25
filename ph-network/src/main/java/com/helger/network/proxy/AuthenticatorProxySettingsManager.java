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
package com.helger.network.proxy;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.lang.priviledged.IPrivilegedAction;
import com.helger.network.proxy.settings.IProxySettings;
import com.helger.network.proxy.settings.ProxySettingsManager;

/**
 * {@link Authenticator} implementation based in {@link ProxySettingsManager}.
 *
 * @author Philip Helger
 */
public class AuthenticatorProxySettingsManager extends Authenticator
{
  public static final AuthenticatorProxySettingsManager INSTANCE = new AuthenticatorProxySettingsManager ();
  private static final Logger LOGGER = LoggerFactory.getLogger (AuthenticatorProxySettingsManager.class);

  public AuthenticatorProxySettingsManager ()
  {}

  /**
   * Set the {@link AuthenticatorProxySettingsManager} as the default
   * {@link Authenticator}.
   */
  public static void setAsDefault ()
  {
    IPrivilegedAction.authenticatorSetDefault (INSTANCE).invokeSafe ();

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Using AuthenticatorProxySettingsManager as the default Authenticator");
  }

  /**
   * @param sRequestingHost
   *        Requesting host. May be <code>null</code>.
   * @param aRequestingSite
   *        Requesting site. May be <code>null</code>.
   * @param nRequestingPort
   *        Requesting port. May be &le; 0.
   * @param sRequestingProtocol
   *        Requesting protocol. May be <code>null</code>.
   * @param sRequestingPrompt
   *        User query to show. May be <code>null</code>.
   * @param sRequestingScheme
   *        Authentication scheme to use. May be <code>null</code>.
   * @param aRequestingURL
   *        The full requesting URL. May be <code>null</code>.
   * @return <code>null</code> if no match was found
   */
  @Nullable
  protected PasswordAuthentication findProxyPasswordAuthentication (@Nullable final String sRequestingHost,
                                                                    @Nullable final InetAddress aRequestingSite,
                                                                    final int nRequestingPort,
                                                                    @Nonnull final String sRequestingProtocol,
                                                                    @Nullable final String sRequestingPrompt,
                                                                    @Nonnull final String sRequestingScheme,
                                                                    @Nullable final URL aRequestingURL)
  {
    final ICommonsOrderedSet <IProxySettings> aMatching = ProxySettingsManager.findAllProxySettings (sRequestingProtocol,
                                                                                                     sRequestingHost,
                                                                                                     nRequestingPort);
    for (final IProxySettings aProxySettings : aMatching)
    {
      // Not every proxy settings contains a PA
      final PasswordAuthentication aPA = aProxySettings.getAsPasswordAuthentication ();
      if (aPA != null)
        return aPA;
    }
    return null;
  }

  @Override
  @Nullable
  protected final PasswordAuthentication getPasswordAuthentication ()
  {
    if (getRequestorType () != RequestorType.PROXY)
    {
      // We only care about proxy requests
      return null;
    }

    return findProxyPasswordAuthentication (getRequestingHost (),
                                            getRequestingSite (),
                                            getRequestingPort (),
                                            getRequestingProtocol (),
                                            getRequestingPrompt (),
                                            getRequestingScheme (),
                                            getRequestingURL ());
  }

  /**
   * Shortcut method for requesting proxy password authentication. This method
   * can also be used, if this class is NOT the default Authenticator.
   *
   * @param sHostName
   *        Hostname to query
   * @param nPort
   *        Port to query
   * @param sProtocol
   *        Protocol to use
   * @return <code>null</code> if nothing is found.
   */
  @Nullable
  public static PasswordAuthentication requestProxyPasswordAuthentication (@Nullable final String sHostName,
                                                                           @Nullable final int nPort,
                                                                           @Nullable final String sProtocol)
  {
    return requestPasswordAuthentication (sHostName,
                                          (InetAddress) null,
                                          nPort,
                                          sProtocol,
                                          (String) null,
                                          (String) null,
                                          (URL) null,
                                          RequestorType.PROXY);
  }
}
