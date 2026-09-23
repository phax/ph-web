/*
 * Copyright (C) 2020-2026 Philip Helger (www.helger.com)
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
import java.time.Duration;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.SimpleResolver;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.dns.config.DNSConfig;

@Immutable
public final class ResolverHelper
{
  /**
   * The divisor to derive the timeout of a single resolver contained in an {@link ExtendedResolver}
   * from the overall timeout of that {@link ExtendedResolver}. dnsjava requires the timeout of the
   * {@link ExtendedResolver} to be larger than the timeout of the contained resolvers - if they are
   * equal, the overall timeout is already reached when the first resolver times out, so that no
   * second resolver and no retry is ever tried. The value matches the ratio of the dnsjava defaults
   * {@link ExtendedResolver#DEFAULT_TIMEOUT} and {@link ExtendedResolver#DEFAULT_RESOLVER_TIMEOUT}.
   *
   * @since 11.4.6
   */
  public static final int SINGLE_RESOLVER_TIMEOUT_DIVISOR = 2;

  private ResolverHelper ()
  {}

  public static void defaultCustomizeResolver (@NonNull final Resolver aResolver)
  {
    // Set the default query timeout
    aResolver.setTimeout (DNSConfig.getResolverTimeout ());
  }

  /**
   * Get the timeout to be used for a single resolver contained in an {@link ExtendedResolver}, based
   * on the overall timeout of that {@link ExtendedResolver}.
   *
   * @param aOverallTimeout
   *        The overall timeout of the {@link ExtendedResolver}. May not be <code>null</code>.
   * @return The timeout to be used for each contained resolver. Never <code>null</code>.
   * @since 11.4.6
   */
  @NonNull
  public static Duration getSingleResolverTimeout (@NonNull final Duration aOverallTimeout)
  {
    ValueEnforcer.notNull (aOverallTimeout, "OverallTimeout");
    return aOverallTimeout.dividedBy (SINGLE_RESOLVER_TIMEOUT_DIVISOR);
  }

  /**
   * Set the overall timeout of the provided {@link ExtendedResolver} as well as the timeout of all
   * the resolvers contained within. This method is needed, because
   * {@link ExtendedResolver#setTimeout(Duration)} only alters the timeout of the
   * {@link ExtendedResolver} itself and deliberately does not propagate it to the contained
   * resolvers.
   *
   * @param aResolver
   *        The extended resolver to set the timeout of. May not be <code>null</code>.
   * @param aOverallTimeout
   *        The overall timeout to be used. May not be <code>null</code>.
   * @see #getSingleResolverTimeout(Duration)
   * @since 11.4.6
   */
  public static void setTimeout (@NonNull final ExtendedResolver aResolver, @NonNull final Duration aOverallTimeout)
  {
    ValueEnforcer.notNull (aResolver, "Resolver");
    ValueEnforcer.notNull (aOverallTimeout, "OverallTimeout");

    final Duration aSingleTimeout = getSingleResolverTimeout (aOverallTimeout);
    for (final Resolver aSingleResolver : aResolver.getResolvers ())
      aSingleResolver.setTimeout (aSingleTimeout);
    aResolver.setTimeout (aOverallTimeout);
  }

  public static void defaultCustomizeExtendedResolver (@NonNull final ExtendedResolver aResolver)
  {
    // Set the default query timeout on the ExtendedResolver AND on all the
    // contained resolvers
    setTimeout (aResolver, DNSConfig.getResolverTimeout ());
    // Set the default retries
    aResolver.setRetries (DNSConfig.getResolverRetryCount ());
  }

  public static void forEachDefaultResolver (@NonNull final Consumer <? super SimpleResolver> aConsumer)
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
                                      @NonNull final Consumer <? super SimpleResolver> aConsumer)
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

  private static boolean _isContained (@NonNull final ICommonsList <Resolver> aResolvers,
                                       @NonNull final SimpleResolver aResolver)
  {
    // SimpleResolver doesn't have equals
    final InetSocketAddress aSearchAddr = aResolver.getAddress ();
    return aResolvers.containsAny (x -> ((SimpleResolver) x).getAddress ().equals (aSearchAddr));
  }

  @NonNull
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

    // Note: this constructor deliberately does NOT alter the timeout of the
    // contained resolvers - that is done by the customization below
    final ExtendedResolver ret = new ExtendedResolver (aResolvers);

    // And now apply the default customization
    defaultCustomizeExtendedResolver (ret);
    return ret;
  }
}
