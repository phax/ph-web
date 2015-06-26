/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.proxy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.system.SystemProperties;
import com.helger.commons.url.EURLProtocol;
import com.helger.commons.url.IURLProtocol;
import com.helger.web.CWeb;

/**
 * Proxy type determination.<br>
 * Source:
 * http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 * 
 * @author Philip Helger
 */
public enum EHttpProxyType implements IHasID <String>
{
  HTTP ("http", EURLProtocol.HTTP, CWeb.DEFAULT_PORT_HTTP),
  HTTPS ("https", EURLProtocol.HTTPS, CWeb.DEFAULT_PORT_HTTPS),
  // Default proxy port for FTP is also 80! This is not a copy/paste error!
  FTP ("ftp", EURLProtocol.FTP, CWeb.DEFAULT_PORT_HTTP);

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
   * @return The name of the system property for getting and setting the proxy
   *         host
   */
  @Nonnull
  public String getPropertyNameProxyHost ()
  {
    return m_sID + ".proxyHost";
  }

  /**
   * @return The current proxy host for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getProxyHost ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyHost ());
  }

  /**
   * @return The name of the system property for getting and setting the proxy
   *         port
   */
  @Nonnull
  public String getPropertyNameProxyPort ()
  {
    return m_sID + ".proxyPort";
  }

  /**
   * @return The current proxy port for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getProxyPort ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyPort ());
  }

  /**
   * @return The name of the system property for getting and setting the proxy
   *         user name
   */
  @Nonnull
  public String getPropertyNameProxyUser ()
  {
    return m_sID + ".proxyUser";
  }

  /**
   * @return The current proxy user for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getProxyUser ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyUser ());
  }

  /**
   * @return The name of the system property for getting and setting the proxy
   *         password
   */
  @Nonnull
  public String getPropertyNameProxyPassword ()
  {
    return m_sID + ".proxyPassword";
  }

  /**
   * @return The current proxy password for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getProxyPassword ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameProxyPassword ());
  }

  /**
   * @return The name of the system property for getting and setting the
   *         non-proxy hosts
   */
  @Nonnull
  public String getPropertyNameNoProxyHosts ()
  {
    // HTTPS uses the http noProxyHosts property
    return this == HTTPS ? HTTP.getPropertyNameNoProxyHosts () : m_sID + ".noProxyHosts";
  }

  /**
   * @return The current non-proxy hosts for this HTTP proxy type. May be
   *         <code>null</code>.
   */
  @Nullable
  public String getNoProxyHosts ()
  {
    return SystemProperties.getPropertyValueOrNull (getPropertyNameNoProxyHosts ());
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
    for (final EHttpProxyType eProxyType : values ())
      if (eProxyType.m_aURLProtocol.equals (aURLProtocol))
        return eProxyType;
    return eDefault;
  }
}
