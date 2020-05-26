/**
 * Copyright (C) 2015-2020 Philip Helger
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
package com.helger.dns.naptr;

import java.net.InetAddress;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.dns.resolve.ResolverHelper;

/**
 * Helper class to resolve NAPTR DNS records for BDMSL
 *
 * @author Philip Helger
 * @since 5.1.5
 */
@Immutable
public final class NaptrResolver
{
  private static final Logger LOGGER = LoggerFactory.getLogger (NaptrResolver.class);

  private NaptrResolver ()
  {}

  @Nullable
  private static String _getAppliedNAPTRRegEx (@Nonnull final String sRegEx, @Nonnull final String sDomainName)
  {
    final char cSep = sRegEx.charAt (0);
    final int nSecond = sRegEx.indexOf (cSep, 1);
    if (nSecond < 0)
      return null;
    final String sEre = sRegEx.substring (1, nSecond);
    final int nThird = sRegEx.indexOf (cSep, nSecond + 1);
    if (nThird < 0)
      return null;
    final String sRepl = sRegEx.substring (nSecond + 1, nThird);
    final String sFlags = sRegEx.substring (nThird + 1);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("NAPTR regex: '" + sEre + "' - '" + sRepl + "' - '" + sFlags + "'");

    final int nOptions = "i".equalsIgnoreCase (sFlags) ? Pattern.CASE_INSENSITIVE : 0;
    final String ret = RegExHelper.stringReplacePattern (sEre, nOptions, sDomainName, sRepl);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("  NAPTR replacement: '" + sDomainName + "' -> '" + ret + "'");
    return ret;
  }

  @Nullable
  public static ICommonsList <NAPTRRecord> lookupNAPTRRecords (@Nullable final String sDNSName,
                                                               @Nullable final Iterable <? extends InetAddress> aCustomDNSServers) throws TextParseException
  {
    if (StringHelper.hasNoText (sDNSName))
      return null;

    final int nMaxRetries = 1;

    // Use the default (static) cache that is used by default
    final Lookup aLookup = new Lookup (sDNSName, Type.NAPTR);
    final ExtendedResolver aResolver = ResolverHelper.createExtendedResolver (aCustomDNSServers);
    aResolver.setRetries (nMaxRetries);
    aLookup.setResolver (aResolver);

    // By default try UDP
    // Stumbled upon an issue, where UDP datagram size was too small for MTU
    // size of 1500
    Record [] aRecords;
    int nLeft = nMaxRetries;
    do
    {
      aRecords = aLookup.run ();
      --nLeft;
    } while (aLookup.getResult () == Lookup.TRY_AGAIN && nLeft >= 0);

    if (aLookup.getResult () == Lookup.TRY_AGAIN)
    {
      // Retry with TCP instead of UDP
      aResolver.setTCP (true);

      nLeft = nMaxRetries;
      do
      {
        aRecords = aLookup.run ();
        --nLeft;
      } while (aLookup.getResult () == Lookup.TRY_AGAIN && nLeft >= 0);
    }

    if (aLookup.getResult () != Lookup.SUCCESSFUL)
    {
      // Wrong domain name
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Error looking up '" + sDNSName + "': " + aLookup.getErrorString ());
      return null;
    }

    final ICommonsList <NAPTRRecord> ret = new CommonsArrayList <> ();
    for (final Record aRecord : aRecords)
      ret.add ((NAPTRRecord) aRecord);
    return ret;
  }

  /**
   * Look up the passed DNS name (usually a dynamic DNS name that was created by
   * an algorithm) and resolve any U-NAPTR records matching the provided service
   * name.
   *
   * @param sDNSName
   *        The created DNS name. May be <code>null</code>.
   * @param aCustomDNSServers
   *        Optional primary DNS server addresses to be used for resolution. May
   *        be <code>null</code>. If present, these servers have precedence.
   * @param sServiceName
   *        The service name (inside the U NAPTR) to query. May neither be
   *        <code>null</code> nor empty. For e-SENS/PEPPOL use "Meta:SMP"
   * @return <code>null</code> if no U-NAPTR was found or could not be resolved.
   *         If non-<code>null</code> the fully qualified domain name, including
   *         and protocol (like http://) is returned.
   * @throws TextParseException
   *         In case the original DNS name does not constitute a valid DNS name
   *         and could not be parsed
   */
  @Nullable
  public static String resolveFromUNAPTR (@Nullable final String sDNSName,
                                          @Nullable final Iterable <? extends InetAddress> aCustomDNSServers,
                                          @Nonnull @Nonempty final String sServiceName) throws TextParseException
  {
    ValueEnforcer.notEmpty (sServiceName, "ServiceName");

    final ICommonsList <NAPTRRecord> aNaptrRecords = lookupNAPTRRecords (sDNSName, aCustomDNSServers);
    if (aNaptrRecords == null)
      return null;

    return resolveUNAPTR (sDNSName, aNaptrRecords, sServiceName);
  }

  @Nullable
  public static String resolveUNAPTR (@Nonnull final String sDNSName,
                                      @Nonnull final ICommonsList <NAPTRRecord> aNaptrRecords,
                                      @Nonnull @Nonempty final String sServiceName)
  {
    ValueEnforcer.notNull (sDNSName, "DNSName");
    ValueEnforcer.notNull (aNaptrRecords, "NAPTRRecords");
    ValueEnforcer.notEmpty (sServiceName, "ServiceName");

    final ICommonsList <NAPTRRecord> aMatchingRecords = new CommonsArrayList <> ();
    for (final NAPTRRecord aRecord : aNaptrRecords)
    {
      /**
       * RFC 2915: Flags are single characters from the set [A-Z0-9]. The case
       * of the alphabetic characters is not significant. <br>
       * The labels for service requests shall be formed from the set of
       * characters [A-Z0-9]. The case of the alphabetic characters is not
       * significant.<br>
       * RFC 3404: allows "+" in service names.<br>
       * RFC 4848: allow many chars: service-parms = [ [app-service] *(":"
       * app-protocol)]<br>
       * ; The service-parms are considered case-insensitive.
       */
      if ("U".equalsIgnoreCase (aRecord.getFlags ()) && sServiceName.equalsIgnoreCase (aRecord.getService ()))
        aMatchingRecords.add (aRecord);
    }

    if (aMatchingRecords.isEmpty ())
    {
      // No matching NAPTR present
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("No matching DNS U-NAPTR records returned for '" + sDNSName + "'");
      return null;
    }

    // Sort by order than by preference according to RFC 2915
    aMatchingRecords.sort ( (x, y) -> {
      int ret = x.getOrder () - y.getOrder ();
      if (ret == 0)
        ret = x.getPreference () - y.getPreference ();
      return ret;
    });
    for (final NAPTRRecord aRecord : aMatchingRecords)
    {
      // The "U" record is terminal, so a RegExp must be present
      final String sRegEx = aRecord.getRegexp ();
      // At least 3 separator chars must be present :)
      if (StringHelper.getLength (sRegEx) > 3)
      {
        final String sFinalDNSName = _getAppliedNAPTRRegEx (sRegEx, sDNSName);
        if (sFinalDNSName != null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Using '" + sFinalDNSName + "' for original DNS name '" + sDNSName + "'");

          return sFinalDNSName;
        }
      }
    }

    // Weird - no regexp present
    if (LOGGER.isWarnEnabled ())
      LOGGER.warn ("None of the matching DNS NAPTR records for '" +
                   sDNSName +
                   "' has a valid regular expression. Details: " +
                   aMatchingRecords);
    return null;
  }
}
