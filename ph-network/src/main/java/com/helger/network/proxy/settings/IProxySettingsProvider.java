package com.helger.network.proxy.settings;

import java.io.Serializable;

import javax.annotation.CheckForSigned;
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
}
