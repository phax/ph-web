/**
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
package com.helger.dns.config;

import java.net.InetAddress;
import java.security.Security;
import java.time.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.system.SystemProperties;
import com.helger.dns.ip.IPV4Addr;

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
  public static final int DEFAULT_RESOLVER_RETRIES = 3;

  private static final Logger LOGGER = LoggerFactory.getLogger (DNSConfig.class);

  @PresentForCodeCoverage
  private static final DNSConfig INSTANCE = new DNSConfig ();

  private DNSConfig ()
  {}

  /**
   * Set special DNS client properties that have influence on the DNS client
   * behavior. This method should be called as soon as possible on startup. In
   * most cases it may be beneficiary if the respective system properties are
   * provided as system properties on the commandline!
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

  @Nonnegative
  public static int getResolverRetries ()
  {
    return DEFAULT_RESOLVER_RETRIES;
  }
}
