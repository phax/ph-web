/*
 * Copyright (C) 2020-2025 Philip Helger (www.helger.com)
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
package com.helger.dns.config;

import java.net.InetAddress;
import java.security.Security;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.base.system.SystemProperties;
import com.helger.dns.ip.IPV4Addr;

import jakarta.annotation.Nonnull;

/**
 * Global DNS client configuration
 *
 * @author Philip Helger
 */
@Immutable
public final class DNSConfig
{
  // Some predefined DNS servers from cloud services
  public static final InetAddress DNS_GOOGLE_1 = IPV4Addr.getAsInetAddress (8, 8, 8, 8);
  public static final InetAddress DNS_GOOGLE_2 = IPV4Addr.getAsInetAddress (8, 8, 4, 4);
  public static final InetAddress DNS_CLOUDFLARE_1 = IPV4Addr.getAsInetAddress (1, 1, 1, 1);
  public static final InetAddress DNS_CLOUDFLARE_2 = IPV4Addr.getAsInetAddress (1, 0, 0, 1);

  // Taken from ExtendedResolver.DEFAULT_TIMEOUT
  public static final Duration DEFAULT_RESOLVER_TIMEOUT = Duration.ofSeconds (5);

  // Taken from ExtendedResolver.retries field
  public static final int DEFAULT_RESOLVER_RETRY_COUNT = 3;
  // Taken from ExtendedResolver.retries field
  /**
   * @deprecated Use {@link #DEFAULT_RESOLVER_RETRY_COUNT} instead
   */
  @Deprecated (forRemoval = true, since = "10.4.3")
  public static final int DEFAULT_RESOLVER_RETRIES = DEFAULT_RESOLVER_RETRY_COUNT;

  private static final Logger LOGGER = LoggerFactory.getLogger (DNSConfig.class);

  @PresentForCodeCoverage
  private static final DNSConfig INSTANCE = new DNSConfig ();

  private DNSConfig ()
  {}

  /**
   * Set special DNS client properties that have influence on the DNS client behavior. This method
   * should be called as soon as possible on startup. In most cases it may be beneficiary if the
   * respective system properties are provided as system properties on the commandline!
   *
   * @param nSeconds
   *        DNS client caching time in seconds.
   */
  public static void setDNSCacheTime (final int nSeconds)
  {
    final String sValue = Integer.toString (nSeconds);
    try
    {
      Security.setProperty ("networkaddress.cache.ttl", sValue);
    }
    catch (final SecurityException ex)
    {
      LOGGER.warn ("Failed to set Security property 'networkaddress.cache.ttl' to '" + sValue + "'");
    }
    try
    {
      Security.setProperty ("networkaddress.cache.negative.ttl", sValue);
    }
    catch (final SecurityException ex)
    {
      LOGGER.warn ("Failed to set Security property 'networkaddress.cache.negative.ttl' to '" + sValue + "'");
    }
    SystemProperties.setPropertyValue ("disableWSAddressCaching", nSeconds == 0);
  }

  @Nonnull
  public static Duration getResolverTimeout ()
  {
    return DEFAULT_RESOLVER_TIMEOUT;
  }

  /**
   * @return The number of retries for the DNS resolver. Must be &ge; 0.
   * @deprecated Use {@link #getResolverRetryCount()} instead
   */
  @Nonnegative
  @Deprecated (forRemoval = true, since = "10.4.3")
  public static int getResolverRetries ()
  {
    return getResolverRetryCount ();
  }

  /**
   * @return The number of retries for the DNS resolver. Must be &ge; 0.
   * @since 10.4.3
   */
  @Nonnegative
  public static int getResolverRetryCount ()
  {
    return DEFAULT_RESOLVER_RETRY_COUNT;
  }
}
