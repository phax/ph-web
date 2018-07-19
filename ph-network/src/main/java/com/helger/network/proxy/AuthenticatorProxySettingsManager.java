package com.helger.network.proxy;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

  protected AuthenticatorProxySettingsManager ()
  {}

  /**
   * Set the {@link AuthenticatorProxySettingsManager} as the default
   * {@link Authenticator}.
   */
  public static void setAsDefault ()
  {
    IPrivilegedAction.authenticatorSetDefault (INSTANCE).invokeSafe ();
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
   * @return
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
      // We only care about proxy requestors
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
}
