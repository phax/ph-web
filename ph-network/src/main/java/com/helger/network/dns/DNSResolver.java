/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.network.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * A simple DNS resolver, using the dnsjava library.
 *
 * @author Philip Helger
 */
@Immutable
public final class DNSResolver
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DNSResolver.class);

  @PresentForCodeCoverage
  private static final DNSResolver s_aInstance = new DNSResolver ();

  private DNSResolver ()
  {}

  @Nullable
  public static InetAddress resolveByName (@Nonnull final String sHostName)
  {
    try
    {
      return Address.getByName (sHostName);
    }
    catch (final UnknownHostException ex)
    {
      return null;
    }
  }

  /**
   * JavaScript callback function! Do not rename!
   *
   * @param sHostName
   *        The host name.
   * @return The resolved IP address as String or <code>null</code> if the host
   *         name could not be resolved.
   */
  @Nullable
  public static String dnsResolve (@Nonnull final String sHostName)
  {
    final InetAddress aAddress = resolveByName (sHostName);
    if (aAddress == null)
      return null;
    return new IPV4Addr (aAddress.getAddress ()).getAsString ();
  }

  @Nonnull
  public static String dnsResolveEx (final String sHostName)
  {
    final StringBuilder aSB = new StringBuilder ();
    try
    {
      final InetAddress [] list = InetAddress.getAllByName (sHostName);
      for (final InetAddress inetAddress : list)
      {
        if (aSB.length () > 0)
          aSB.append ("; ");
        aSB.append (inetAddress.getHostAddress ());
      }
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("DNS name not resolvable " + sHostName, e);
    }
    return aSB.toString ();
  }

  @Nonnull
  public static String getMyIpAddress ()
  {
    try
    {
      final InetAddress aAddress = InetAddress.getLocalHost ();
      if (aAddress != null)
        return new IPV4Addr (aAddress.getAddress ()).getAsString ();
    }
    catch (final UnknownHostException ex)
    {
      // fall through
    }
    return "127.0.0.1";
  }
}
