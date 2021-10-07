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
package com.helger.dns.resolve;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.dns.ip.IPV4Addr;

/**
 * A simple DNS resolver, using the dnsjava library.
 *
 * @author Philip Helger
 */
@Immutable
public final class DNSResolver
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DNSResolver.class);

  private static final InetAddress [] IA0 = new InetAddress [0];

  @PresentForCodeCoverage
  private static final DNSResolver INSTANCE = new DNSResolver ();

  private DNSResolver ()
  {}

  @Nullable
  public static InetAddress resolveByName (@Nullable final String sHostName)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("resolveByName '" + sHostName + "'");

    InetAddress ret = null;
    if (sHostName != null)
      try
      {
        // Checks "A" and "AAAA" records
        ret = Address.getByName (sHostName);
      }
      catch (final UnknownHostException ex)
      {
        // Fall through
      }

    if (ret == null)
    {
      if (sHostName != null)
        LOGGER.warn ("resolveByName '" + sHostName + "' failed");
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("resolveByName '" + sHostName + "' resolved to " + ret);
    }

    return ret;
  }

  @Nonnull
  public static InetAddress [] resolveAllByName (@Nullable final String sHostName)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("resolveAllByName '" + sHostName + "'");

    InetAddress [] ret = null;
    if (sHostName != null)
      try
      {
        // Checks "A" and "AAAA" records
        ret = Address.getAllByName (sHostName);
      }
      catch (final UnknownHostException ex)
      {
        // Fall through
      }

    if (ret == null)
    {
      if (sHostName != null)
        LOGGER.warn ("resolveAllByName '" + sHostName + "' failed");
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("resolveAllByName '" + sHostName + "' resolved to " + Arrays.toString (ret));
    }

    return ret != null ? ret : IA0;
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

  /**
   * JavaScript callback function! Do not rename!
   *
   * @param sHostName
   *        The host name.
   * @return The resolved IP addresses as String or <code>null</code> if the
   *         host name could not be resolved.
   */
  @Nonnull
  public static String dnsResolveEx (final String sHostName)
  {
    final StringBuilder aSB = new StringBuilder ();
    final InetAddress [] aAddrs = resolveAllByName (sHostName);
    for (final InetAddress aInetAddress : aAddrs)
    {
      if (aSB.length () > 0)
        aSB.append ("; ");
      aSB.append (aInetAddress.getHostAddress ());
    }
    return aSB.toString ();
  }

  /**
   * JavaScript callback function! Do not rename!
   *
   * @return My IP address as a String and never <code>null</code>.
   */
  @Nonnull
  public static String getMyIpAddress ()
  {
    return getMyIpAddressOrDefault ("127.0.0.1");
  }

  @Nullable
  public static String getMyIpAddressOrDefault (@Nullable final String sDefault)
  {
    try
    {
      final InetAddress aAddress = InetAddress.getLocalHost ();
      if (aAddress != null)
        return aAddress.getHostAddress ();
    }
    catch (final UnknownHostException ex)
    {
      // fall through
    }
    return sDefault;
  }
}
