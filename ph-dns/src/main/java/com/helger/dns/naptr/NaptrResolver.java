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
package com.helger.dns.naptr;

import java.net.InetAddress;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.VisibleForTesting;
import com.helger.commons.builder.IBuilder;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.compare.CompareHelper;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;

/**
 * Helper class to resolve NAPTR DNS records for BDMSL
 *
 * @author Philip Helger
 * @since 5.1.5
 */
@Immutable
public class NaptrResolver
{
  private static final Logger LOGGER = LoggerFactory.getLogger (NaptrResolver.class);

  private final String m_sDomainName;
  private final ICommonsList <NAPTRRecord> m_aNaptrRecords;
  private final Predicate <? super String> m_aServiceNameMatcher;

  public NaptrResolver (@Nonnull final String sDomainName,
                        @Nonnull final ICommonsList <NAPTRRecord> aNaptrRecords,
                        @Nonnull final Predicate <? super String> aServiceNameMatcher)
  {
    ValueEnforcer.notNull (sDomainName, "DomainName");
    ValueEnforcer.notNull (aNaptrRecords, "NAPTRRecords");
    ValueEnforcer.notNull (aServiceNameMatcher, "ServiceNameMatcher");

    m_sDomainName = sDomainName;
    m_aNaptrRecords = aNaptrRecords;
    m_aServiceNameMatcher = aServiceNameMatcher;
  }

  @Nonnull
  public static Predicate <String> getDefaultServiceNameMatcher (@Nonnull final String sServiceName)
  {
    return x -> sServiceName.equalsIgnoreCase (x);
  }

  // NaptrRegex is e.g. <code>!^.*$!http://test-infra.peppol.at!</code>
  @Nullable
  @VisibleForTesting
  static String getAppliedNAPTRRegEx (@Nonnull final String sNaptrRegEx, @Nonnull final String sDomainName)
  {
    final char cSep = sNaptrRegEx.charAt (0);
    final int nSecond = sNaptrRegEx.indexOf (cSep, 1);
    if (nSecond < 0)
    {
      LOGGER.warn ("NAPTR regex '" + sNaptrRegEx + "' - failed to find second separator");
      return null;
    }
    String sRegEx = sNaptrRegEx.substring (1, nSecond);
    if (!sRegEx.startsWith ("^"))
      sRegEx = '^' + sRegEx;
    if (!sRegEx.endsWith ("$"))
      sRegEx = sRegEx + '$';

    final int nThird = sNaptrRegEx.indexOf (cSep, nSecond + 1);
    if (nThird < 0)
    {
      LOGGER.warn ("NAPTR regex '" + sNaptrRegEx + "' - failed to find third separator");
      return null;
    }
    final String sReplacement = sNaptrRegEx.substring (nSecond + 1, nThird);
    final String sFlags = sNaptrRegEx.substring (nThird + 1);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("NAPTR regex: '" + sRegEx + "' - '" + sReplacement + "' - '" + sFlags + "'");

    final int nOptions = "i".equalsIgnoreCase (sFlags) ? Pattern.CASE_INSENSITIVE : 0;
    final String ret = RegExHelper.stringReplacePattern (sRegEx, nOptions, sDomainName, sReplacement);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("  NAPTR replacement: '" + sDomainName + "' -> '" + ret + "'");
    return ret;
  }

  @Nullable
  public String resolveUNAPTR ()
  {
    final ICommonsList <NAPTRRecord> aMatchingRecords = new CommonsArrayList <> ();
    for (final NAPTRRecord aRecord : m_aNaptrRecords)
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
      if ("U".equalsIgnoreCase (aRecord.getFlags ()) && m_aServiceNameMatcher.test (aRecord.getService ()))
        aMatchingRecords.add (aRecord);
    }

    if (aMatchingRecords.isEmpty ())
    {
      // No matching NAPTR present
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("No matching DNS U-NAPTR records returned for '" + m_sDomainName + "'");
      return null;
    }

    // Sort by order than by preference according to RFC 2915
    aMatchingRecords.sort ( (x, y) -> {
      int ret = CompareHelper.compare (x.getOrder (), y.getOrder ());
      if (ret == 0)
        ret = CompareHelper.compare (x.getPreference (), y.getPreference ());
      return ret;
    });
    for (final NAPTRRecord aRecord : aMatchingRecords)
    {
      // The "U" record is terminal, so a RegExp must be present
      final String sRegEx = aRecord.getRegexp ();
      // At least 3 separator chars must be present :)
      if (StringHelper.getLength (sRegEx) > 3)
      {
        final String sFinalDNSName = getAppliedNAPTRRegEx (sRegEx, m_sDomainName);
        if (sFinalDNSName != null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Using '" + sFinalDNSName + "' for original domain name '" + m_sDomainName + "'");

          return sFinalDNSName;
        }
      }
    }

    // Weird - no regexp present
    if (LOGGER.isWarnEnabled ())
      LOGGER.warn ("None of the matching DNS NAPTR records for '" +
                   m_sDomainName +
                   "' has a valid regular expression. Details: " +
                   aMatchingRecords);
    return null;
  }

  @Nullable
  @Deprecated
  public static ICommonsList <NAPTRRecord> lookupNAPTRRecords (@Nullable final String sDNSName,
                                                               @Nullable final Iterable <? extends InetAddress> aCustomDNSServers) throws TextParseException
  {
    return NaptrLookup.builder ().domainName (sDNSName).customDNSServers (aCustomDNSServers).maxRetries (1).lookup ();
  }

  @Nullable
  @Deprecated
  public static String resolveFromUNAPTR (@Nullable final String sDNSName,
                                          @Nullable final Iterable <? extends InetAddress> aCustomDNSServers,
                                          @Nonnull @Nonempty final String sServiceName) throws TextParseException
  {
    return resolveFromUNAPTR (sDNSName, aCustomDNSServers, getDefaultServiceNameMatcher (sServiceName));
  }

  /**
   * Look up the passed DNS name (usually a dynamic DNS name that was created by
   * an algorithm) and resolve any U-NAPTR records matching the provided service
   * name.
   *
   * @param sDomainName
   *        The domain name to resolve. May be <code>null</code>.
   * @param aCustomDNSServers
   *        Optional primary DNS server addresses to be used for resolution. May
   *        be <code>null</code>. If present, these servers have precedence.
   * @param aServiceNameMatcher
   *        A matcher for service names (inside the U NAPTR) to query. May not
   *        be <code>null</code>. The service name needs to be matched
   *        case-insensitive. For e-SENS/Peppol test for "Meta:SMP"
   * @return <code>null</code> if no U-NAPTR was found or could not be resolved.
   *         If non-<code>null</code> the fully qualified domain name, including
   *         and protocol (like http://) is returned.
   * @throws TextParseException
   *         In case the original DNS name does not constitute a valid DNS name
   *         and could not be parsed
   */
  @Nullable
  @Deprecated
  public static String resolveFromUNAPTR (@Nullable final String sDomainName,
                                          @Nullable final Iterable <? extends InetAddress> aCustomDNSServers,
                                          @Nonnull @Nonempty final Predicate <? super String> aServiceNameMatcher) throws TextParseException
  {
    return builder ().domainName (sDomainName)
                     .naptrRecords (NaptrLookup.builder ()
                                               .domainName (sDomainName)
                                               .customDNSServers (aCustomDNSServers))
                     .serviceName (aServiceNameMatcher)
                     .build ()
                     .resolveUNAPTR ();
  }

  @Nullable
  @Deprecated
  public static String resolveUNAPTR (@Nonnull final String sDNSName,
                                      @Nonnull final ICommonsList <NAPTRRecord> aNaptrRecords,
                                      @Nonnull @Nonempty final String sServiceName)
  {
    return builder ().domainName (sDNSName)
                     .naptrRecords (aNaptrRecords)
                     .serviceName (sServiceName)
                     .build ()
                     .resolveUNAPTR ();
  }

  @Nullable
  @Deprecated
  public static String resolveUNAPTR (@Nonnull final String sDNSName,
                                      @Nonnull final ICommonsList <NAPTRRecord> aNaptrRecords,
                                      @Nonnull final Predicate <? super String> aServiceNameMatcher)
  {
    return builder ().domainName (sDNSName)
                     .naptrRecords (aNaptrRecords)
                     .serviceName (aServiceNameMatcher)
                     .build ()
                     .resolveUNAPTR ();
  }

  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  @NotThreadSafe
  public static class Builder implements IBuilder <NaptrResolver>
  {
    private String m_sDomainName;
    private final ICommonsList <NAPTRRecord> m_aNaptrRecords = new CommonsArrayList <> ();
    private boolean m_bNaptrLookupProvided = false;
    private Predicate <? super String> m_aServiceNameMatcher;

    public Builder ()
    {}

    @Nonnull
    public final Builder domainName (@Nullable final NaptrLookup.Builder a)
    {
      return domainName (a == null ? null : a.domainName ());
    }

    @Nonnull
    public final Builder domainName (@Nullable final Name a)
    {
      return domainName (a == null ? null : a.toString (false));
    }

    @Nonnull
    public final Builder domainName (@Nullable final String s)
    {
      m_sDomainName = s;
      return this;
    }

    @Nonnull
    public final Builder naptrRecords (@Nullable final NaptrLookup.Builder a)
    {
      return naptrRecords (a == null ? null : a.build ());
    }

    @Nonnull
    public final Builder naptrRecords (@Nullable final NaptrLookup a)
    {
      if (a != null)
        m_bNaptrLookupProvided = true;
      return naptrRecords (a == null ? null : a.lookup ());
    }

    @Nonnull
    public final Builder naptrRecord (@Nullable final NAPTRRecord a)
    {
      if (a == null)
        m_aNaptrRecords.clear ();
      else
        m_aNaptrRecords.set (a);
      return this;
    }

    @Nonnull
    public final Builder naptrRecords (@Nullable final NAPTRRecord... a)
    {
      if (a == null)
        m_aNaptrRecords.clear ();
      else
        m_aNaptrRecords.setAll (a);
      return this;
    }

    @Nonnull
    public final Builder naptrRecords (@Nullable final Iterable <? extends NAPTRRecord> a)
    {
      if (a == null)
        m_aNaptrRecords.clear ();
      else
        m_aNaptrRecords.setAll (a);
      return this;
    }

    @Nonnull
    public final Builder addNaptrRecord (@Nullable final NAPTRRecord a)
    {
      if (a != null)
        m_aNaptrRecords.add (a);
      return this;
    }

    @Nonnull
    public final Builder addNaptrRecords (@Nullable final NAPTRRecord... a)
    {
      if (a != null)
        m_aNaptrRecords.addAll (a);
      return this;
    }

    @Nonnull
    public final Builder addNaptrRecords (@Nullable final Iterable <? extends NAPTRRecord> a)
    {
      if (a != null)
        m_aNaptrRecords.addAll (a);
      return this;
    }

    @Nonnull
    public final Builder serviceName (@Nullable final String s)
    {
      return serviceName (s == null ? null : getDefaultServiceNameMatcher (s));
    }

    @Nonnull
    public final Builder serviceName (@Nullable final Predicate <? super String> a)
    {
      m_aServiceNameMatcher = a;
      return this;
    }

    @Nonnull
    public NaptrResolver build ()
    {
      if (StringHelper.hasNoText (m_sDomainName))
        throw new IllegalStateException ("Domain name is required");
      if (m_aNaptrRecords.isEmpty ())
      {
        LOGGER.warn ("No NAPTR records are provided." + (m_bNaptrLookupProvided ? "" : " Using the default lookup."));

        // If no NAPTR Records are present and no lookup was yet performed, do a
        // simple default lookup
        if (!m_bNaptrLookupProvided)
          try
          {
            naptrRecords (NaptrLookup.builder ().domainName (m_sDomainName));
          }
          catch (final TextParseException ex)
          {
            LOGGER.error ("Creepy domain found", ex);
          }
      }
      if (m_aServiceNameMatcher == null)
        throw new IllegalStateException ("The service name predicate is required");

      return new NaptrResolver (m_sDomainName, m_aNaptrRecords, m_aServiceNameMatcher);
    }
  }
}
