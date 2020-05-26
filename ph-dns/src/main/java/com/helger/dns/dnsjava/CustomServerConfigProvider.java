package com.helger.dns.dnsjava;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.xbill.DNS.Name;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.config.ResolverConfigProvider;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsList;

public class CustomServerConfigProvider implements ResolverConfigProvider
{
  private final ICommonsList <InetSocketAddress> m_aServers;

  public CustomServerConfigProvider (@Nonnull @Nonempty final ICommonsList <InetSocketAddress> aServers)
  {
    m_aServers = aServers.getClone ();
  }

  public void initialize ()
  {}

  public List <InetSocketAddress> servers ()
  {
    return m_aServers.getClone ();
  }

  public List <Name> searchPaths ()
  {
    return Collections.emptyList ();
  }

  @Nonnull
  public static CustomServerConfigProvider createFromInetAddressList (@Nonnull @Nonempty final ICommonsList <InetAddress> aServers)
  {
    return new CustomServerConfigProvider (aServers.getAllMapped (x -> new InetSocketAddress (x, SimpleResolver.DEFAULT_PORT)));
  }
}
