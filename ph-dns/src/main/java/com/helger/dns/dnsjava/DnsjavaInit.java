/*
 * Copyright (C) 2020-2021 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.config.AndroidResolverConfigProvider;
import org.xbill.DNS.config.JndiContextResolverConfigProvider;
import org.xbill.DNS.config.PropertyResolverConfigProvider;
import org.xbill.DNS.config.ResolvConfResolverConfigProvider;
import org.xbill.DNS.config.ResolverConfigProvider;
import org.xbill.DNS.config.SunJvmResolverConfigProvider;
import org.xbill.DNS.config.WindowsResolverConfigProvider;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * Simplification for setting DnsJava global config providers.
 *
 * @author Philip Helger
 */
@Immutable
public final class DnsjavaInit
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DnsjavaInit.class);

  private DnsjavaInit ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ResolverConfigProvider> getDefaultResolverConfigProviders ()
  {
    final ICommonsList <ResolverConfigProvider> aConfigProviders = new CommonsArrayList <> ();
    aConfigProviders.add (new PropertyResolverConfigProvider ());
    aConfigProviders.add (new ResolvConfResolverConfigProvider ());
    aConfigProviders.add (new WindowsResolverConfigProvider ());
    aConfigProviders.add (new AndroidResolverConfigProvider ());
    aConfigProviders.add (new JndiContextResolverConfigProvider ());
    aConfigProviders.add (new SunJvmResolverConfigProvider ());
    return aConfigProviders;
  }

  public static void initWithCustomDNSServers (@Nonnull @Nonempty final ICommonsList <InetAddress> aCustomDNSServers)
  {
    ValueEnforcer.notEmptyNoNullValue (aCustomDNSServers, "CustomDNSServers");

    // Default ones
    final ICommonsList <ResolverConfigProvider> aConfigProviders = getDefaultResolverConfigProviders ();

    // Add the custom ones as the last resort
    aConfigProviders.add (CustomServerConfigProvider.createFromInetAddressList (aCustomDNSServers));

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Overwriting dnsjava default ResolverConfiguration. Adding the following custom DNS servers: " + aCustomDNSServers);

    // And set the globally
    ResolverConfig.setConfigProviders (aConfigProviders);
  }

  public static void setDefaultConfig ()
  {
    // Default ones
    final ICommonsList <ResolverConfigProvider> aConfigProviders = getDefaultResolverConfigProviders ();

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Setting dnsjava default ResolverConfiguration.");

    // And set the globally
    ResolverConfig.setConfigProviders (aConfigProviders);
  }
}
