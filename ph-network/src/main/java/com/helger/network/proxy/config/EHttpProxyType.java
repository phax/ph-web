/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import java.net.Proxy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.url.EURLProtocol;
import com.helger.commons.url.IURLProtocol;
import com.helger.network.port.SchemeDefaultPortMapper;

/**
 * Proxy type determination.<br>
 * Source:
 * http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 *
 * @author Philip Helger
 */
public enum EHttpProxyType implements IProxySettingsPerProtocol
{
  HTTP ("http", EURLProtocol.HTTP, SchemeDefaultPortMapper.getDefaultPortOrThrow (SchemeDefaultPortMapper.SCHEME_HTTP)),
  HTTPS ("https",
         EURLProtocol.HTTPS,
         SchemeDefaultPortMapper.getDefaultPortOrThrow (SchemeDefaultPortMapper.SCHEME_HTTPS)),
  // Default proxy port for FTP is also 80! This is not a copy/paste error!
  FTP ("ftp", EURLProtocol.FTP, SchemeDefaultPortMapper.getDefaultPortOrThrow (SchemeDefaultPortMapper.SCHEME_HTTP));

  private final String m_sID;
  private final IURLProtocol m_aURLProtocol;
  private final int m_nDefaultPort;

  private EHttpProxyType (@Nonnull @Nonempty final String sID,
                          @Nonnull final IURLProtocol aURLProtocol,
                          @Nonnegative final int nDefaultPort)
  {
    m_sID = sID;
    m_aURLProtocol = aURLProtocol;
    m_nDefaultPort = nDefaultPort;
  }

  @Nonnull
  public Proxy.Type getProxyType ()
  {
    return Proxy.Type.HTTP;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nonnull
  public IURLProtocol getURLProtocol ()
  {
    return m_aURLProtocol;
  }

  @Nonnegative
  public int getDefaultPort ()
  {
    return m_nDefaultPort;
  }

  /**
   * @return The name of the system property for getting and setting the
   *         non-proxy hosts
   */
  @Nonnull
  public String getPropertyNameNoProxyHosts ()
  {
    // HTTPS uses the http noProxyHosts property
    if (this == HTTPS)
      return HTTP.getPropertyNameNoProxyHosts ();
    return m_sID + ".noProxyHosts";
  }

  @Nullable
  public static EHttpProxyType getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EHttpProxyType.class, sID);
  }

  @Nullable
  public static EHttpProxyType getFromURLProtocolOrDefault (@Nullable final IURLProtocol aURLProtocol,
                                                            @Nullable final EHttpProxyType eDefault)
  {
    return EnumHelper.findFirst (EHttpProxyType.class, x -> x.m_aURLProtocol.equals (aURLProtocol), eDefault);
  }
}
