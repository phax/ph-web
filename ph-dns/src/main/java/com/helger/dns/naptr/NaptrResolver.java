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
package com.helger.dns.naptr;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.base.builder.IBuilder;
import com.helger.base.compare.CompareHelper;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.cache.regex.RegExHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Helper class to resolve U-NAPTR DNS records
 *
 * @author Philip Helger
 * @since 5.1.5
 */
@Immutable
public class NaptrResolver
{
  public static final String DEFAULT_FLAGS = "U";
  private static final Logger LOGGER = LoggerFactory.getLogger (NaptrResolver.class);

  private final String m_sDomainName;
  private final ICommonsList <NAPTRRecord> m_aNaptrRecords;
  private final Predicate <? super String> m_aFlagsMatcher;
  private final Predicate <? super String> m_aServiceNameMatcher;

  /**
   * The matcher to be used to compare flags. By default a case-insensitive string compare with the
   * provided flags is performed.
   *
   * @param sFlags
   *        The flags to compare to case insensitive.
   * @return The non-<code>null</code> matcher.
   * @since 11.1.2
   */
  @Nonnull
  public static Predicate <String> getDefaultFlagsMatcher (@Nonnull final String sFlags)
  {
    return sFlags::equalsIgnoreCase;
  }

  /**
   * The matcher to be used to compare service names. By default a case-insensitive string compare
   * is performed.
   *
   * @param sServiceName
   *        The service name to compare to case insensitive.
   * @return The non-<code>null</code> matcher.
   */
  @Nonnull
  public static Predicate <String> getDefaultServiceNameMatcher (@Nonnull final String sServiceName)
  {
    return sServiceName::equalsIgnoreCase;
  }

  @Deprecated (forRemoval = true, since = "11.1.2")
  public NaptrResolver (@Nonnull final String sDomainName,
                        @Nonnull final ICommonsList <NAPTRRecord> aNaptrRecords,
                        @Nonnull final Predicate <? super String> aServiceNameMatcher)
  {
    this (sDomainName, aNaptrRecords, getDefaultFlagsMatcher (DEFAULT_FLAGS), aServiceNameMatcher);
  }

  public NaptrResolver (@Nonnull final String sDomainName,
                        @Nonnull final ICommonsList <NAPTRRecord> aNaptrRecords,
                        @Nonnull final Predicate <? super String> aFlagsMatcher,
                        @Nonnull final Predicate <? super String> aServiceNameMatcher)
  {
    ValueEnforcer.notNull (sDomainName, "DomainName");
    ValueEnforcer.notNull (aNaptrRecords, "NAPTRRecords");
    ValueEnforcer.notNull (aFlagsMatcher, "aFlagsMatcher");
    ValueEnforcer.notNull (aServiceNameMatcher, "ServiceNameMatcher");

    m_sDomainName = sDomainName;
    m_aNaptrRecords = aNaptrRecords;
    m_aFlagsMatcher = aFlagsMatcher;
    m_aServiceNameMatcher = aServiceNameMatcher;
  }

  /**
   * Apply a NAPTR regular expression on the provided domain name and return the result. Example
   * regular expression is e.g. <code>!^.*$!http://test-infra.peppol.at!</code>. The first and last
   * character are expected to be the same and also the separator char.
   *
   * @param sNaptrRegEx
   *        The regular expression to use.
   * @param sDomainName
   *        The source domain name to apply.
   * @return <code>null</code> if the regular expression is invalid
   */
  @Nullable
  public static String getAppliedNAPTRRegEx (@Nonnull final String sNaptrRegEx, @Nonnull final String sDomainName)
  {
    final char cSep = sNaptrRegEx.charAt (0);
    final int nSecond = sNaptrRegEx.indexOf (cSep, 1);
    if (nSecond < 0)
    {
      LOGGER.warn ("NAPTR regex '" + sNaptrRegEx + "' - failed to find second separator");
      return null;
    }
    String sRegEx = sNaptrRegEx.substring (1, nSecond);

    // Make sure regex works for Java
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
       * RFC 2915: Flags are single characters from the set [A-Z0-9]. The case of the alphabetic
       * characters is not significant. <br>
       * The labels for service requests shall be formed from the set of characters [A-Z0-9]. The
       * case of the alphabetic characters is not significant.<br>
       * RFC 3404: allows "+" in service names.<br>
       * RFC 4848: allow many chars: service-parms = [ [app-service] *(":" app-protocol)]<br>
       * ; The service-parms are considered case-insensitive.
       */
      if (m_aFlagsMatcher.test (aRecord.getFlags ()) && m_aServiceNameMatcher.test (aRecord.getService ()))
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Found a matching U-NAPTR record: " + aRecord);
        aMatchingRecords.add (aRecord);
      }
    }
    if (aMatchingRecords.isEmpty ())
    {
      // No matching NAPTR present
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
    // Weird - no regex present
    LOGGER.warn ("None of the matching DNS NAPTR records for '" +
                 m_sDomainName +
                 "' has a valid regular expression. Details: " +
                 aMatchingRecords);
    return null;
  }

  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  /**
   * Builder class for a {@link NaptrResolver}.
   *
   * @author Philip Helger
   */
  @NotThreadSafe
  public static class Builder implements IBuilder <NaptrResolver>
  {
    private String m_sDomainName;
    private final ICommonsList <NAPTRRecord> m_aNaptrRecords = new CommonsArrayList <> ();
    private boolean m_bNaptrLookupProvided = false;
    private Predicate <? super String> m_aFlagsMatcher;
    private Predicate <? super String> m_aServiceNameMatcher;

    public Builder ()
    {
      flags (getDefaultFlagsMatcher (DEFAULT_FLAGS));
    }

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

      // This performs the main DNS NAPTR lookup
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
    public final Builder flags (@Nullable final String s)
    {
      return flags (s == null ? null : getDefaultFlagsMatcher (s));
    }

    @Nonnull
    public final Builder flags (@Nullable final Predicate <? super String> a)
    {
      m_aFlagsMatcher = a;
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
      if (StringHelper.isEmpty (m_sDomainName))
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

      if (m_aFlagsMatcher == null)
        throw new IllegalStateException ("The flags matcher is required");

      if (m_aServiceNameMatcher == null)
        throw new IllegalStateException ("The service name matcher is required");

      return new NaptrResolver (m_sDomainName, m_aNaptrRecords, m_aFlagsMatcher, m_aServiceNameMatcher);
    }
  }
}
