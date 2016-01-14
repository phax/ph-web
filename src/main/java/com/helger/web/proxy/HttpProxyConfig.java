/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.system.SystemProperties;
import com.helger.web.port.DefaultNetworkPorts;

/**
 * HTTP proxy configuration.<br>
 * Attention when using userName and password, make sure to call
 * <code>Authenticator.setDefault (...)</code> with the same username and
 * password as well!
 *
 * @author Philip Helger
 */
@Immutable
public class HttpProxyConfig implements IProxyConfig
{
  private final EHttpProxyType m_eProxyType;
  private final String m_sHost;
  private final int m_nPort;
  private final String m_sUserName;
  private final String m_sPassword;
  private final List <String> m_aNonProxyHosts = new ArrayList <String> ();

  public HttpProxyConfig (@Nonnull final EHttpProxyType eProxyType,
                          @Nonnull @Nonempty final String sHost,
                          @Nonnegative final int nPort)
  {
    this (eProxyType, sHost, nPort, (String) null, (String) null, (List <String>) null);
  }

  public HttpProxyConfig (@Nonnull final EHttpProxyType eProxyType,
                          @Nonnull @Nonempty final String sHost,
                          @Nonnegative final int nPort,
                          @Nullable final List <String> aNonProxyHosts)
  {
    this (eProxyType, sHost, nPort, (String) null, (String) null, aNonProxyHosts);
  }

  public HttpProxyConfig (@Nonnull final EHttpProxyType eProxyType,
                          @Nonnull @Nonempty final String sHost,
                          @Nonnegative final int nPort,
                          @Nullable final String sUserName,
                          @Nullable final String sPassword,
                          @Nullable final List <String> aNonProxyHosts)
  {
    if (!DefaultNetworkPorts.isValidPort (nPort))
      throw new IllegalArgumentException ("The passed port is invalid: " + nPort);
    m_eProxyType = ValueEnforcer.notNull (eProxyType, "ProxyType");
    m_sHost = ValueEnforcer.notEmpty (sHost, "HostName");
    m_nPort = nPort;
    m_sUserName = sUserName;
    m_sPassword = sPassword;
    if (aNonProxyHosts != null)
      for (final String sNonProxyHost : aNonProxyHosts)
        if (StringHelper.hasText (sNonProxyHost))
          m_aNonProxyHosts.add (sNonProxyHost);
  }

  @Nonnull
  public EHttpProxyType getType ()
  {
    return m_eProxyType;
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

  @Nullable
  public String getUserName ()
  {
    return m_sUserName;
  }

  @Nullable
  public String getPassword ()
  {
    return m_sPassword;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <String> getNonProxyHosts ()
  {
    return CollectionHelper.newList (m_aNonProxyHosts);
  }

  @Nullable
  public Authenticator getAsAuthenticator ()
  {
    // If no user name is set, no Authenticator needs to be created
    if (StringHelper.hasNoText (m_eProxyType.getProxyUser ()))
      return null;

    return new HttpProxyAuthenticator (m_eProxyType);
  }

  public void activateGlobally ()
  {
    // Deactivate other proxy configurations
    SocksProxyConfig.deactivateGlobally ();
    UseSystemProxyConfig.deactivateGlobally ();

    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameProxyHost (), m_sHost);
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameProxyPort (), Integer.toString (m_nPort));
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameProxyUser (), m_sUserName);
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameProxyPassword (), m_sPassword);
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameNoProxyHosts (),
                                       StringHelper.getImploded ("|", m_aNonProxyHosts));
  }

  public static void deactivateGlobally ()
  {
    for (final EHttpProxyType eProxyType : EHttpProxyType.values ())
    {
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyHost ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyPort ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyUser ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyPassword ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameNoProxyHosts ());
    }
  }

  @Nonnull
  public Proxy getAsProxy ()
  {
    return new Proxy (Proxy.Type.HTTP, new InetSocketAddress (m_sHost, m_nPort));
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("host", m_sHost)
                                       .append ("port", m_nPort)
                                       .append ("userName", m_sUserName)
                                       .appendPassword ("password")
                                       .append ("nonProxyHosts", m_aNonProxyHosts)
                                       .toString ();
  }
}
