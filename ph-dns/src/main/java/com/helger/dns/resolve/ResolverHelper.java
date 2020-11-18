/**
 * Copyright (C) 2020 Philip Helger (www.helger.com)
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
package com.helger.dns.resolve;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.SimpleResolver;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.dns.config.DNSConfig;

@Immutable
public final class ResolverHelper
{
  private ResolverHelper ()
  {}

  public static void defaultCustomizeResolver (@Nonnull final Resolver aResolver)
  {
    // Set the default query timeout
    aResolver.setTimeout (DNSConfig.getResolverTimeout ());
  }

  public static void defaultCustomizeExtendedResolver (@Nonnull final ExtendedResolver aResolver)
  {
    defaultCustomizeResolver (aResolver);
    // Set the default retries
    aResolver.setRetries (DNSConfig.getResolverRetries ());
  }

  public static void forEachDefaultResolver (@Nonnull final Consumer <? super SimpleResolver> aConsumer)
  {
    ValueEnforcer.notNull (aConsumer, "Consumer");

    for (final InetSocketAddress aISA : ResolverConfig.getCurrentConfig ().servers ())
      if (aISA != null)
      {
        final SimpleResolver aResolver = new SimpleResolver (aISA);
        defaultCustomizeResolver (aResolver);
        aConsumer.accept (aResolver);
      }
  }

  public static void forEachResolver (@Nullable final Iterable <? extends InetAddress> aServerAddrs,
                                      @Nonnull final Consumer <? super SimpleResolver> aConsumer)
  {
    if (aServerAddrs != null)
      for (final InetAddress aAddr : aServerAddrs)
        if (aAddr != null)
        {
          // Use the default port
          final SimpleResolver aResolver = new SimpleResolver (aAddr);
          defaultCustomizeResolver (aResolver);
          aConsumer.accept (aResolver);
        }
  }

  private static boolean _isContained (final ICommonsList <Resolver> aResolvers, final SimpleResolver aResolver)
  {
    // SimpleResolver doesn't have equals
    final InetSocketAddress aSearchAddr = aResolver.getAddress ();
    return aResolvers.containsAny (x -> ((SimpleResolver) x).getAddress ().equals (aSearchAddr));
  }

  @Nonnull
  public static ExtendedResolver createExtendedResolver (@Nullable final Iterable <? extends InetAddress> aCustomServerAddrs)
  {
    final ICommonsList <Resolver> aResolvers = new CommonsArrayList <> ();
    // Add optional custom servers first
    forEachResolver (aCustomServerAddrs, x -> {
      if (!_isContained (aResolvers, x))
        aResolvers.add (x);
    });
    // Add default servers as fallbacks
    forEachDefaultResolver (x -> {
      if (!_isContained (aResolvers, x))
        aResolvers.add (x);
    });
    // This overrides the timeout of all contained resolvers
    final ExtendedResolver ret = new ExtendedResolver (aResolvers);
    // And now apply the default customization
    defaultCustomizeExtendedResolver (ret);
    return ret;
  }
}
