package com.helger.network.proxy;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.lang.priviledged.IPrivilegedAction;

public class DefaultAuthenticator extends Authenticator
{
  public static final DefaultAuthenticator INSTANCE = new DefaultAuthenticator ();

  private DefaultAuthenticator ()
  {}

  /**
   * Set the {@link DefaultAuthenticator} as the default {@link Authenticator}.
   */
  public static void install ()
  {
    IPrivilegedAction.authenticatorSetDefault (INSTANCE).invokeSafe ();
  }

  @Nullable
  protected PasswordAuthentication findPasswordAuthentication (@Nullable final String sRequestingHost,
                                                               @Nullable final InetAddress aRequestingSite,
                                                               final int nRequestingPort,
                                                               @Nonnull final String sRequestingProtocol,
                                                               @Nullable final String sRequestingPrompt,
                                                               @Nonnull final String sRequestingScheme,
                                                               @Nullable final URL aRequestingURL,
                                                               @Nonnull final RequestorType eRequestorType)
  {
    if (eRequestorType == RequestorType.PROXY)
    {
      // TODO
      // search in proxy list
    }
    else
    {
      // TODO
      // Search in other list
    }

    return null;
  }

  @Override
  @Nullable
  protected final PasswordAuthentication getPasswordAuthentication ()
  {
    return findPasswordAuthentication (getRequestingHost (),
                                       getRequestingSite (),
                                       getRequestingPort (),
                                       getRequestingProtocol (),
                                       getRequestingPrompt (),
                                       getRequestingScheme (),
                                       getRequestingURL (),
                                       getRequestorType ());
  }
}
