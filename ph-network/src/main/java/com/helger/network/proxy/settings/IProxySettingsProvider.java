package com.helger.network.proxy.settings;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.net.URI;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.impl.ICommonsList;

public interface IProxySettingsProvider extends Serializable
{
  /**
   * @param sProtocol
   *        The protocol that's requesting the connection
   * @param sHostName
   *        The hostname of the site requesting authentication.
   * @param nPort
   *        the port for the requested connection
   * @return The proxy settings to be used. May be <code>null</code> to indicate
   *         that none was found.
   */
  @Nullable
  ICommonsList <IProxySettings> getAllProxySettings (@Nullable String sProtocol,
                                                     @Nullable String sHostName,
                                                     @CheckForSigned int nPort);

  /**
   * Invoked if the connection to a proxy server failed. The action to take
   * depends on your requirements.
   * 
   * @param aProxySettings
   *        The proxy settings that contains the failed proxy. Never
   *        <code>null</code>.
   * @param aURI
   *        The URI that the proxy failed to serve. Never <code>null</code>.
   * @param aAddr
   *        The socket address of the proxy/SOCKS server. Never
   *        <code>null</code>.
   * @param ex
   *        The I/O exception thrown when the connect failed. Never
   *        <code>null</code>.
   */
  default void onConnectionFailed (@Nonnull final IProxySettings aProxySettings,
                                   @Nonnull final URI aURI,
                                   @Nonnull final SocketAddress aAddr,
                                   @Nonnull final IOException ex)
  {}
}
