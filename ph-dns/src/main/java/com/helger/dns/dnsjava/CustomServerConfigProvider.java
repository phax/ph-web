/*
 * Copyright (C) 2020-2022 Philip Helger (www.helger.com)
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
package com.helger.dns.dnsjava;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.xbill.DNS.Name;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.config.ResolverConfigProvider;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * A simple implementation of {@link ResolverConfigProvider} that uses a
 * constant list of {@link InetSocketAddress} as the server list.
 *
 * @author Philip Helger
 */
public class CustomServerConfigProvider implements ResolverConfigProvider
{
  private final ICommonsList <InetSocketAddress> m_aServers;

  public CustomServerConfigProvider (@Nonnull final ICommonsList <InetSocketAddress> aServers)
  {
    ValueEnforcer.notNull (aServers, "Servers");
    m_aServers = aServers.getClone ();
  }

  public void initialize ()
  {}

  @Nonnull
  public List <InetSocketAddress> servers ()
  {
    return m_aServers.getClone ();
  }

  @Nonnull
  public List <Name> searchPaths ()
  {
    return Collections.emptyList ();
  }

  @Nonnull
  public static CustomServerConfigProvider createFromInetAddressList (@Nonnull final ICommonsList <InetAddress> aServers)
  {
    ValueEnforcer.notNull (aServers, "Servers");
    return new CustomServerConfigProvider (aServers.getAllMapped (x -> new InetSocketAddress (x, SimpleResolver.DEFAULT_PORT)));
  }
}
