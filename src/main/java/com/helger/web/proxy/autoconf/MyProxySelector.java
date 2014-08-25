/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.proxy.autoconf;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.WorkInProgress;
import com.helger.commons.collections.ContainerHelper;
import com.helger.web.CWeb;

/**
 * This class is not yet working!
 * 
 * @author Philip Helger
 */
@WorkInProgress
@Deprecated
public class MyProxySelector extends ProxySelector
{
  private final ProxySelector m_aWrappedProxySelector;

  public MyProxySelector ()
  {
    this (ProxySelector.getDefault ());
  }

  public MyProxySelector (@Nullable final ProxySelector aProxySelector)
  {
    m_aWrappedProxySelector = aProxySelector;
  }

  @Override
  public List <Proxy> select (@Nonnull final URI aURI)
  {
    ValueEnforcer.notNull (aURI, "URI");

    final String sScheme = aURI.getScheme ();
    if (CWeb.SCHEME_HTTP.equalsIgnoreCase (sScheme) || CWeb.SCHEME_HTTPS.equalsIgnoreCase (sScheme))
    {
      final ArrayList <Proxy> l = new ArrayList <Proxy> ();
      // TODO Populate the ArrayList with proxies
      return l;
    }

    // pass to wrapped proxy selector
    if (m_aWrappedProxySelector != null)
      return m_aWrappedProxySelector.select (aURI);

    // Don't use a proxy
    return ContainerHelper.newList (Proxy.NO_PROXY);
  }

  @Override
  public void connectFailed (@Nonnull final URI aURI,
                             @Nonnull final SocketAddress aSocketAddress,
                             @Nonnull final IOException aException)
  {
    ValueEnforcer.notNull (aURI, "Uri");
    ValueEnforcer.notNull (aSocketAddress, "SocketAddress");
    ValueEnforcer.notNull (aException, "Exception");

    if (m_aWrappedProxySelector != null)
      m_aWrappedProxySelector.connectFailed (aURI, aSocketAddress, aException);
  }
}
