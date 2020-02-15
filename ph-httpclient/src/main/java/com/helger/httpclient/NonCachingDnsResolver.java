/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.apache.http.conn.DnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.lang.ClassHelper;

/**
 * A special implementation of {@link DnsResolver} that tries to disable caching
 * of DNS resolution as much as possible.
 *
 * @author Philip Helger
 * @since 8.8.0
 */
@Immutable
public class NonCachingDnsResolver implements DnsResolver
{
  /**
   * No need to create more than one instance.
   */
  public static final NonCachingDnsResolver INSTANCE = new NonCachingDnsResolver ();

  private static final Logger LOGGER = LoggerFactory.getLogger (NonCachingDnsResolver.class);

  public NonCachingDnsResolver ()
  {}

  @Nonnull
  protected Lookup createLookup (@Nonnull final String sHost) throws TextParseException
  {
    final Lookup aDNSLookup = new Lookup (sHost, Type.ANY);
    try
    {
      aDNSLookup.setResolver (new SimpleResolver ());
    }
    catch (final UnknownHostException ex)
    {
      // Shit happens - no special resolver needed
    }
    // No cache!
    aDNSLookup.setCache (null);
    return aDNSLookup;
  }

  @Nonnull
  public InetAddress [] resolve (@Nonnull final String sHost) throws UnknownHostException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("DNS resolving host '" + sHost + "'");

    Record [] aRecords = null;
    try
    {
      final Lookup aDNSLookup = createLookup (sHost);
      aRecords = aDNSLookup.run ();
    }
    catch (final TextParseException ex)
    {
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("Failed to parse host '" + sHost + "'", ex);
    }

    final InetAddress [] ret;
    if (aRecords == null || aRecords.length == 0)
    {
      // E.g. for IP addresses - use system resolution
      ret = InetAddress.getAllByName (sHost);
    }
    else
    {
      // Names found
      final ICommonsList <InetAddress> aAddrs = new CommonsArrayList <> ();
      for (final Record aRecord : aRecords)
      {
        if (aRecord instanceof CNAMERecord)
        {
          // It's a CName - so a name pointing to a name
          // recursively resolve :)
          final InetAddress [] aNested = resolve (((CNAMERecord) aRecord).getAlias ().toString ());
          if (aNested != null)
            aAddrs.addAll (aNested);
        }
        else
          if (aRecord instanceof ARecord)
          {
            // It's an address record
            final InetAddress aInetAddress = ((ARecord) aRecord).getAddress ();
            aAddrs.add (aInetAddress);
          }
          else
          {
            if (LOGGER.isErrorEnabled ())
              LOGGER.info ("Unknown record type found for host '" +
                           sHost +
                           "': " +
                           ClassHelper.getClassLocalName (aRecord));
          }
      }
      ret = aAddrs.toArray (new InetAddress [aAddrs.size ()]);
    }

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Return for '" + sHost + "': " + Arrays.toString (ret));
    return ret;
  }
}
