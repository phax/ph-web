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
package com.helger.network.proxy.config;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.base.system.SystemProperties;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.network.port.NetworkPortHelper;

/**
 * HTTP proxy configuration.<br>
 * Attention when using userName and password, make sure to call
 * <code>Authenticator.setDefault (...)</code> with the same username and password as well!
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
  private final ICommonsList <String> m_aNonProxyHosts = new CommonsArrayList <> ();

  public HttpProxyConfig (@NonNull final EHttpProxyType eProxyType,
                          @NonNull @Nonempty final String sHost,
                          @Nonnegative final int nPort)
  {
    this (eProxyType, sHost, nPort, (String) null, (String) null, (List <String>) null);
  }

  public HttpProxyConfig (@NonNull final EHttpProxyType eProxyType,
                          @NonNull @Nonempty final String sHost,
                          @Nonnegative final int nPort,
                          @Nullable final List <String> aNonProxyHosts)
  {
    this (eProxyType, sHost, nPort, (String) null, (String) null, aNonProxyHosts);
  }

  public HttpProxyConfig (@NonNull final EHttpProxyType eProxyType,
                          @NonNull @Nonempty final String sHost,
                          @Nonnegative final int nPort,
                          @Nullable final String sUserName,
                          @Nullable final String sPassword,
                          @Nullable final List <String> aNonProxyHosts)
  {
    ValueEnforcer.notNull (eProxyType, "ProxyType");
    ValueEnforcer.notEmpty (sHost, "HostName");
    ValueEnforcer.isTrue (NetworkPortHelper.isValidPort (nPort), () -> "The passed port is invalid: " + nPort);
    m_eProxyType = eProxyType;
    m_sHost = sHost;
    m_nPort = nPort;
    m_sUserName = sUserName;
    m_sPassword = sPassword;
    if (aNonProxyHosts != null)
      for (final String sNonProxyHost : aNonProxyHosts)
        if (StringHelper.isNotEmpty (sNonProxyHost))
          m_aNonProxyHosts.add (sNonProxyHost);
  }

  @NonNull
  public final EHttpProxyType getType ()
  {
    return m_eProxyType;
  }

  @NonNull
  public final String getHost ()
  {
    return m_sHost;
  }

  @Nonnegative
  public final int getPort ()
  {
    return m_nPort;
  }

  public boolean hasUserNameOrPassword ()
  {
    return StringHelper.isNotEmpty (m_sUserName) || m_sPassword != null;
  }

  @Nullable
  public final String getUserName ()
  {
    return m_sUserName;
  }

  @Nullable
  public final String getPassword ()
  {
    return m_sPassword;
  }

  @Nullable
  public final char [] getPasswordAsCharArray ()
  {
    return m_sPassword == null ? null : m_sPassword.toCharArray ();
  }

  @NonNull
  @ReturnsMutableCopy
  public final ICommonsList <String> getNonProxyHosts ()
  {
    return m_aNonProxyHosts.getClone ();
  }

  @Nullable
  public Authenticator getAsAuthenticator ()
  {
    // If no user name is set, no Authenticator needs to be created
    if (StringHelper.isEmpty (m_eProxyType.getProxyUserName ()))
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
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameProxyUserName (), m_sUserName);
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameProxyPassword (), m_sPassword);
    SystemProperties.setPropertyValue (m_eProxyType.getPropertyNameNoProxyHosts (),
                                       StringImplode.getImploded ('|', m_aNonProxyHosts));
  }

  public static void deactivateGlobally ()
  {
    for (final EHttpProxyType eProxyType : EHttpProxyType.values ())
    {
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyHost ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyPort ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyUserName ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameProxyPassword ());
      SystemProperties.removePropertyValue (eProxyType.getPropertyNameNoProxyHosts ());
    }
  }

  @NonNull
  public InetSocketAddress getAsInetSocketAddress ()
  {
    return new InetSocketAddress (m_sHost, m_nPort);
  }

  @NonNull
  public Proxy getAsProxy ()
  {
    return new Proxy (Proxy.Type.HTTP, getAsInetSocketAddress ());
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final HttpProxyConfig rhs = (HttpProxyConfig) o;
    return m_eProxyType.equals (rhs.m_eProxyType) &&
           m_sHost.equals (rhs.m_sHost) &&
           m_nPort == rhs.m_nPort &&
           EqualsHelper.equals (m_sUserName, rhs.m_sUserName) &&
           EqualsHelper.equals (m_sPassword, rhs.m_sPassword) &&
           EqualsHelper.equals (m_aNonProxyHosts, rhs.m_aNonProxyHosts);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_eProxyType)
                                       .append (m_sHost)
                                       .append (m_nPort)
                                       .append (m_sUserName)
                                       .append (m_sPassword)
                                       .append (m_aNonProxyHosts)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ProxyType", m_eProxyType)
                                       .append ("Host", m_sHost)
                                       .append ("Port", m_nPort)
                                       .append ("UserName", m_sUserName)
                                       .appendPassword ("Password")
                                       .append ("NonProxyHosts", m_aNonProxyHosts)
                                       .getToString ();
  }
}
