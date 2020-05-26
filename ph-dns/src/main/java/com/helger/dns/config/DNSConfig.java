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
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.system.SystemProperties;
import com.helger.dns.dnsjava.DnsjavaInit;
import com.helger.dns.ip.IPV4Addr;

/**
 * Global DNS client configuration
 *
 * @author Philip Helger
 */
@Immutable
public final class DNSConfig
{
  // Taken from ExtendedResolver.DEFAULT_TIMEOUT
  private static final Duration DEFAULT_RESOLVER_TIMEOUT = Duration.ofSeconds (5);

  // Taken from ExtendedResolver.retries field
  private static final int DEFAULT_RESOLVER_RETRIES = 3;

  private static final ICommonsList <InetAddress> CUSTOM_DEFAULT_DNS_SERVERS = new CommonsArrayList <> ();
  static
  {
    // CloudFlare DNS
    CUSTOM_DEFAULT_DNS_SERVERS.add (IPV4Addr.getAsInetAddress (1, 1, 1, 1));
    CUSTOM_DEFAULT_DNS_SERVERS.add (IPV4Addr.getAsInetAddress (1, 0, 0, 1));
    // Google DNS
    CUSTOM_DEFAULT_DNS_SERVERS.add (IPV4Addr.getAsInetAddress (8, 8, 8, 8));
    CUSTOM_DEFAULT_DNS_SERVERS.add (IPV4Addr.getAsInetAddress (8, 8, 4, 4));
  }

  private static final Logger LOGGER = LoggerFactory.getLogger (DNSConfig.class);

  @PresentForCodeCoverage
  private static final DNSConfig s_aInstance = new DNSConfig ();

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

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <InetAddress> getDefaultCustomServers ()
  {
    return CUSTOM_DEFAULT_DNS_SERVERS.getClone ();
  }

  static
  {
    DnsjavaInit.initWithCustomDNSServers (getDefaultCustomServers ());
  }

  public static void ensureInited ()
  {
    /* empty - just to ensure the DnsjavaInit is called */
  }
}
