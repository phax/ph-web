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

    // Add the custom one down below
    aConfigProviders.add (CustomServerConfigProvider.createFromInetAddressList (aCustomDNSServers));

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Overwriting dnsjava default ResolverConfiguration. Adding the following custom DNS servers: " + aCustomDNSServers);

    // And set the globally
    ResolverConfig.setConfigProviders (aConfigProviders);
  }

  public static void detDefaultConfig ()
  {
    // Default ones
    final ICommonsList <ResolverConfigProvider> aConfigProviders = getDefaultResolverConfigProviders ();

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Setting dnsjava default ResolverConfiguration.");

    // And set the globally
    ResolverConfig.setConfigProviders (aConfigProviders);
  }
}
