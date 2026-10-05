/*
 * Copyright (C) 2020-2026 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.base.builder.IBuilder;
import com.helger.base.callback.CallbackList;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.log.ConditionalLogger;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.base.timing.StopWatch;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.dns.dnssec.DnsSecHelper;
import com.helger.dns.dnssec.DnsSecStatusRecordingResolver;
import com.helger.dns.dnssec.DnsSecValidatingResolverCache;
import com.helger.dns.dnssec.DnsSecValidatingResolverKey;
import com.helger.dns.dnssec.EDnsSecValidationStatus;
import com.helger.dns.dnssec.IDnsSecValidatingResolverCache;
import com.helger.dns.resolve.ResolverHelper;

/**
 * A new flexible class to perform NAPTR DNS lookups.
 *
 * @author Philip Helger
 * @since 9.5.0
 */
@Immutable
public class NaptrLookup
{
  public enum ELookupNetworkMode
  {
    /** First UDP then TCP */
    UDP_TCP (true, true),
    /** Only UDP */
    UDP (true, false),
    /** Only TCP */
    TCP (false, true);

    private final boolean m_bUDP;
    private final boolean m_bTCP;

    ELookupNetworkMode (final boolean bUDP, final boolean bTCP)
    {
      m_bUDP = bUDP;
      m_bTCP = bTCP;
    }

    public boolean isUDP ()
    {
      return m_bUDP;
    }

    public boolean isTCP ()
    {
      return m_bTCP;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger (NaptrLookup.class);

  private final Name m_aDomainName;
  private final ICommonsList <InetAddress> m_aCustomDNSServers;
  private final int m_nMaxRetries;
  private final Duration m_aTimeout;
  private final ELookupNetworkMode m_eLookupMode;
  private final Duration m_aExecutionDurationWarn;
  private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers;
  private final boolean m_bDebugMode;
  private final boolean m_bDnsSecValidation;
  private final String m_sDnsSecTrustAnchors;
  private final Duration m_aDnsSecKeyCacheMaxTtl;
  private final IDnsSecValidatingResolverCache m_aDnsSecResolverCache;

  public NaptrLookup (@NonNull final Name aDomainName,
                      @Nullable final ICommonsList <InetAddress> aCustomDNSServers,
                      @Nonnegative final int nMaxRetries,
                      @Nullable final Duration aTimeout,
                      @NonNull final ELookupNetworkMode eLookupMode,
                      @Nullable final Duration aExecutionDurationWarn,
                      @Nullable final CallbackList <INaptrLookupTimeExceededCallback> aExecutionTimeExceededHandlers,
                      final boolean bDebugMode)
  {
    this (aDomainName,
          aCustomDNSServers,
          nMaxRetries,
          aTimeout,
          eLookupMode,
          aExecutionDurationWarn,
          aExecutionTimeExceededHandlers,
          bDebugMode,
          false,
          DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS,
          DnsSecHelper.DEFAULT_KEY_CACHE_MAX_TTL,
          null);
  }

  /**
   * Constructor
   *
   * @param aDomainName
   *        The domain name to look up. May not be <code>null</code>.
   * @param aCustomDNSServers
   *        Optional custom DNS servers to use. May be <code>null</code>.
   * @param nMaxRetries
   *        The maximum number of retries. Must be &ge; 0.
   * @param aTimeout
   *        The optional overall timeout. May be <code>null</code>.
   * @param eLookupMode
   *        The network lookup mode. May not be <code>null</code>.
   * @param aExecutionDurationWarn
   *        The execution duration after which the callbacks are invoked. May be <code>null</code>.
   * @param aExecutionTimeExceededHandlers
   *        The callbacks to invoke if the execution duration was exceeded. May be
   *        <code>null</code>.
   * @param bDebugMode
   *        <code>true</code> to enable debug logging.
   * @param bDnsSecValidation
   *        <code>true</code> to require a DNSSEC validated (secure) response.
   * @param sDnsSecTrustAnchors
   *        The DNSSEC trust anchors in DNS master file format. May neither be <code>null</code> nor
   *        empty.
   * @param aDnsSecKeyCacheMaxTtl
   *        The maximum time to cache validated DNSSEC keys. May not be <code>null</code>.
   * @param aDnsSecResolverCache
   *        The cache for the DNSSEC validating resolvers. May be <code>null</code> to use the
   *        global default from {@link DnsSecValidatingResolverCache#getDefaultInstance()} at the
   *        time of the lookup.
   * @since 11.4.7
   */
  public NaptrLookup (@NonNull final Name aDomainName,
                      @Nullable final ICommonsList <InetAddress> aCustomDNSServers,
                      @Nonnegative final int nMaxRetries,
                      @Nullable final Duration aTimeout,
                      @NonNull final ELookupNetworkMode eLookupMode,
                      @Nullable final Duration aExecutionDurationWarn,
                      @Nullable final CallbackList <INaptrLookupTimeExceededCallback> aExecutionTimeExceededHandlers,
                      final boolean bDebugMode,
                      final boolean bDnsSecValidation,
                      @NonNull @Nonempty final String sDnsSecTrustAnchors,
                      @NonNull final Duration aDnsSecKeyCacheMaxTtl,
                      @Nullable final IDnsSecValidatingResolverCache aDnsSecResolverCache)
  {
    ValueEnforcer.notNull (aDomainName, "DomainName");
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");
    ValueEnforcer.notNull (eLookupMode, "LookupMode");
    ValueEnforcer.notEmpty (sDnsSecTrustAnchors, "DnsSecTrustAnchors");
    ValueEnforcer.notNull (aDnsSecKeyCacheMaxTtl, "DnsSecKeyCacheMaxTtl");

    m_aDomainName = aDomainName;
    m_aCustomDNSServers = new CommonsArrayList <> (aCustomDNSServers);
    m_nMaxRetries = nMaxRetries;
    m_aTimeout = aTimeout;
    m_eLookupMode = eLookupMode;
    m_aExecutionDurationWarn = aExecutionDurationWarn;
    m_aExecutionTimeExceededHandlers = new CallbackList <> (aExecutionTimeExceededHandlers);
    m_bDebugMode = bDebugMode;
    m_bDnsSecValidation = bDnsSecValidation;
    m_sDnsSecTrustAnchors = sDnsSecTrustAnchors;
    m_aDnsSecKeyCacheMaxTtl = aDnsSecKeyCacheMaxTtl;
    m_aDnsSecResolverCache = aDnsSecResolverCache;
  }

  @NonNull
  private DnsSecStatusRecordingResolver _getDnsSecResolver (final boolean bTcp) throws IOException
  {
    final IDnsSecValidatingResolverCache aCache = m_aDnsSecResolverCache != null ? m_aDnsSecResolverCache
                                                                                : DnsSecValidatingResolverCache.getDefaultInstance ();
    final DnsSecValidatingResolverKey aKey = new DnsSecValidatingResolverKey (m_aCustomDNSServers,
                                                                              m_nMaxRetries,
                                                                              m_aTimeout,
                                                                              bTcp,
                                                                              m_sDnsSecTrustAnchors,
                                                                              m_aDnsSecKeyCacheMaxTtl);
    // One recording resolver per lookup, around the shared validating resolver
    return new DnsSecStatusRecordingResolver (aCache.getValidatingResolver (aKey));
  }

  /**
   * Perform the DNS lookup based on the parameters provided in the constructor and return only the
   * records list. Note: this method cannot distinguish between "no NAPTR record exists" and "DNS
   * infrastructure failure" — both return an empty list. For that distinction use
   * {@link #lookupResult()}.
   *
   * @return A never <code>null</code> but maybe empty list of records.
   * @see #lookupResult()
   */
  @NonNull
  public ICommonsList <NAPTRRecord> lookup ()
  {
    return lookupResult ().getRecords ();
  }

  /**
   * Perform the DNS lookup based on the parameters provided in the constructor.<br>
   * If DNSSEC validation is enabled, the DNSSEC chain of trust is validated locally, starting at
   * the configured trust anchors, and the shared dnsjava cache is not used. The validating
   * resolvers, including the validated keys, are reused via an
   * {@link IDnsSecValidatingResolverCache}. Every response that is
   * not validated as secure (bogus or unsigned) leads to
   * {@link ENaptrLookupStatus#DNSSEC_VALIDATION_FAILED}. This also applies to "not found"
   * responses.
   *
   * @return A {@link NaptrLookupResult} carrying the status, the records (possibly empty), and an
   *         optional error message. Never <code>null</code>.
   * @since 11.4.0
   */
  @NonNull
  public NaptrLookupResult lookupResult ()
  {
    // Omit the final dot
    final String sDomainName = m_aDomainName.toString (true);

    final ConditionalLogger aCondLogger = new ConditionalLogger (LOGGER, m_bDebugMode);
    aCondLogger.info (() -> "Trying to look up NAPTR on '" +
                            sDomainName +
                            "'" +
                            (m_nMaxRetries > 0 ? " with " + m_nMaxRetries + " retries" : "") +
                            " using network mode " +
                            m_eLookupMode +
                            (m_bDnsSecValidation ? " with DNSSEC validation" : "") +
                            (m_aCustomDNSServers.isNotEmpty () ? " and the custom DNS server(s) " +
                                                                 StringImplode.imploder ()
                                                                              .separator (", ")
                                                                              .source (m_aCustomDNSServers,
                                                                                       InetAddress::getHostAddress)
                                                                              .build () : ""));

    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      final Lookup aLookup = new Lookup (m_aDomainName, Type.NAPTR);

      // Only used without DNSSEC validation
      ExtendedResolver aResolver = null;
      // Only used with DNSSEC validation
      DnsSecStatusRecordingResolver aDnsSecResolver = null;
      if (m_bDnsSecValidation)
      {
        // Use a temporary cache, as the shared cache may contain records that were not validated.
        // Additionally a cache hit would not pass the resolver and could therefore not be validated
        aLookup.setCache (null);
      }
      else
      {
        // Use the default (static) cache that is used by default
        aResolver = ResolverHelper.createExtendedResolver (m_aCustomDNSServers);

        // Retries are handled internally by the ExtendedResolver
        aResolver.setRetries (m_nMaxRetries);
        if (m_aTimeout != null)
        {
          // Note: ExtendedResolver.setTimeout alone would only alter the timeout
          // of the ExtendedResolver but not the one of the contained resolvers
          ResolverHelper.setTimeout (aResolver, m_aTimeout);
        }
        aLookup.setResolver (aResolver);
      }

      int nLookupRuns = 0;
      boolean bCanTryAgain = true;
      Record [] aRecords = null;

      if (m_eLookupMode.isUDP ())
      {
        aCondLogger.info (() -> "  Trying UDP for NAPTR lookup");

        if (m_bDnsSecValidation)
        {
          aDnsSecResolver = _getDnsSecResolver (false);
          aLookup.setResolver (aDnsSecResolver);
        }

        // By default try UDP
        // Stumbled upon an issue, where UDP datagram size was too small for MTU
        // size of 1500
        aRecords = aLookup.run ();
        nLookupRuns++;
        aCondLogger.info (() -> "    Result of UDP lookup: " + aLookup.getErrorString ());

        // Only a transient failure is worth a retry via TCP. All the definitive
        // results (SUCCESSFUL, HOST_NOT_FOUND, TYPE_NOT_FOUND and UNRECOVERABLE)
        // are not improved by asking the very same servers again via TCP.
        // Note: a truncated UDP response is already retried via TCP by dnsjava
        // itself, inside SimpleResolver
        bCanTryAgain = ENaptrLookupStatus.fromDnsJavaResultCode (aLookup.getResult ()).isRetryable ();

        // A bogus response is reported as SERVFAIL - asking again via TCP does not help
        if (aDnsSecResolver != null && aDnsSecResolver.getValidationStatus ().isBogus ())
          bCanTryAgain = false;
      }

      if (bCanTryAgain && m_eLookupMode.isTCP ())
      {
        final int nFinalLookupRuns = nLookupRuns;
        aCondLogger.info (() -> "  Trying TCP for NAPTR lookup after " +
                                nFinalLookupRuns +
                                " unsuccessful UDP lookup(s)");

        // Retry with TCP instead of UDP
        if (m_bDnsSecValidation)
        {
          // The shared validating resolver must not be modified - use the TCP one instead
          aDnsSecResolver = _getDnsSecResolver (true);
          aLookup.setResolver (aDnsSecResolver);
        }
        else
          aResolver.setTCP (true);
        aRecords = aLookup.run ();
        nLookupRuns++;
        aCondLogger.info (() -> "    Result of TCP lookup: " + aLookup.getErrorString ());
      }

      final EDnsSecValidationStatus eDnsSecStatus = aDnsSecResolver == null ? EDnsSecValidationStatus.NOT_VALIDATED
                                                                            : aDnsSecResolver.getValidationStatus ();
      // NOT_VALIDATED means that no response was received at all - that is a technical failure
      if (aDnsSecResolver != null &&
          !eDnsSecStatus.isSecure () &&
          (eDnsSecStatus != EDnsSecValidationStatus.NOT_VALIDATED || aLookup.getResult () == Lookup.SUCCESSFUL))
      {
        final String sBogusReason = aDnsSecResolver.getBogusReason ();
        final String sErrorMessage = "DNSSEC validation of '" +
                                     sDomainName +
                                     "' failed with status " +
                                     eDnsSecStatus +
                                     (sBogusReason != null ? ": " + sBogusReason : "");
        LOGGER.warn (sErrorMessage);
        return NaptrLookupResult.failure (ENaptrLookupStatus.DNSSEC_VALIDATION_FAILED, sErrorMessage, eDnsSecStatus);
      }

      if (aLookup.getResult () != Lookup.SUCCESSFUL)
      {
        final ENaptrLookupStatus eStatus = ENaptrLookupStatus.fromDnsJavaResultCode (aLookup.getResult ());
        aCondLogger.warn (() -> "Error looking up '" +
                                sDomainName +
                                "' [" +
                                aLookup.getResult () +
                                "]: " +
                                aLookup.getErrorString ());
        return NaptrLookupResult.failure (eStatus, aLookup.getErrorString (), eDnsSecStatus);
      }

      final ICommonsList <NAPTRRecord> ret = new CommonsArrayList <> ();
      for (final Record aRecord : aRecords)
        ret.add ((NAPTRRecord) aRecord);

      final int nFinalLookupRuns = nLookupRuns;
      aCondLogger.info (() -> "  Returning " +
                              ret.size () +
                              " NAPTR record(s) for '" +
                              sDomainName +
                              "' after " +
                              nFinalLookupRuns +
                              " lookups");
      return NaptrLookupResult.success (ret, eDnsSecStatus);
    }
    catch (final IOException ex)
    {
      // Can only happen when creating the DNSSEC validating resolver
      LOGGER.error ("Failed to create the DNSSEC validating resolver: " + ex.getMessage ());
      return NaptrLookupResult.failure (ENaptrLookupStatus.DNSSEC_VALIDATION_FAILED,
                                        "Failed to create the DNSSEC validating resolver: " + ex.getMessage ());
    }
    finally
    {
      // Check execution time
      aSW.stop ();
      final Duration aDuration = aSW.getDuration ();
      if (m_aExecutionDurationWarn != null && aDuration.compareTo (m_aExecutionDurationWarn) > 0)
      {
        final String sMessage = "Looking up NAPTR record of '" +
                                sDomainName +
                                "'" +
                                (m_nMaxRetries > 0 ? " with " + m_nMaxRetries + " retries" : "");
        m_aExecutionTimeExceededHandlers.forEach (x -> x.onLookupTimeExceeded (sMessage,
                                                                               aDuration,
                                                                               m_aExecutionDurationWarn));
      }
    }
  }

  @NonNull
  public static NaptrLookupBuilder builder ()
  {
    return new NaptrLookupBuilder ();
  }

  /**
   * Builder class for {@link NaptrLookup} objects.
   *
   * @author Philip Helger
   */
  @NotThreadSafe
  public static class NaptrLookupBuilder implements IBuilder <NaptrLookup>
  {
    public static final int DEFAULT_MAX_RETRIES = 1;
    public static final Duration DEFAULT_EXECUTION_DURATION_WARN = Duration.ofSeconds (1);
    public static final ELookupNetworkMode DEFAULT_LOOKUP_MODE = ELookupNetworkMode.UDP_TCP;
    /** @since 11.4.7 */
    public static final boolean DEFAULT_DNSSEC_VALIDATION = false;

    private Name m_aDomainName;
    private final ICommonsList <InetAddress> m_aCustomDNSServers = new CommonsArrayList <> ();
    private int m_nMaxRetries = DEFAULT_MAX_RETRIES;
    private Duration m_aTimeout;
    private Duration m_aExecutionDurationWarn = DEFAULT_EXECUTION_DURATION_WARN;
    private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers = new CallbackList <> ();
    private ELookupNetworkMode m_eLookupMode = DEFAULT_LOOKUP_MODE;
    private boolean m_bDebugMode;
    private boolean m_bDnsSecValidation = DEFAULT_DNSSEC_VALIDATION;
    private String m_sDnsSecTrustAnchors = DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS;
    private Duration m_aDnsSecKeyCacheMaxTtl = DnsSecHelper.DEFAULT_KEY_CACHE_MAX_TTL;
    private IDnsSecValidatingResolverCache m_aDnsSecResolverCache;

    public NaptrLookupBuilder ()
    {
      // add a default handler
      m_aExecutionTimeExceededHandlers.add (new LoggingNaptrLookupTimeExceededCallback (false));
      debugMode (false);
    }

    @Nullable
    public final Name domainName ()
    {
      return m_aDomainName;
    }

    @Nullable
    public final String domainNameString ()
    {
      return m_aDomainName == null ? null : m_aDomainName.toString (false);
    }

    @NonNull
    public final NaptrLookupBuilder domainName (@Nullable final String s) throws TextParseException
    {
      return domainName (Name.fromString (s));
    }

    @NonNull
    public final NaptrLookupBuilder domainName (@Nullable final Name a)
    {
      m_aDomainName = a;
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder customDNSServer (@Nullable final InetAddress a)
    {
      if (a == null)
        m_aCustomDNSServers.clear ();
      else
        m_aCustomDNSServers.set (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder customDNSServers (@Nullable final InetAddress... a)
    {
      if (a == null)
        m_aCustomDNSServers.clear ();
      else
        m_aCustomDNSServers.setAll (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder customDNSServers (@Nullable final Iterable <? extends InetAddress> a)
    {
      if (a == null)
        m_aCustomDNSServers.clear ();
      else
        m_aCustomDNSServers.setAll (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder addCustomDNSServer (@Nullable final InetAddress a)
    {
      if (a != null)
        m_aCustomDNSServers.add (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder addCustomDNSServers (@Nullable final InetAddress... a)
    {
      if (a != null)
        m_aCustomDNSServers.addAll (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder addCustomDNSServers (@Nullable final Iterable <? extends InetAddress> a)
    {
      if (a != null)
        m_aCustomDNSServers.addAll (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder maxRetries (final int n)
    {
      m_nMaxRetries = n;
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder noRetries ()
    {
      return maxRetries (0);
    }

    @NonNull
    public final NaptrLookupBuilder timeoutMS (final long n)
    {
      return timeout (n < 0 ? null : Duration.ofMillis (n));
    }

    @NonNull
    public final NaptrLookupBuilder timeout (@Nullable final Duration a)
    {
      m_aTimeout = a;
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder lookupMode (@Nullable final ELookupNetworkMode e)
    {
      m_eLookupMode = e;
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder executionDurationWarnMS (final long nMillis)
    {
      return executionDurationWarn (nMillis < 0 ? null : Duration.ofMillis (nMillis));
    }

    @NonNull
    public final NaptrLookupBuilder executionDurationWarn (@Nullable final Duration a)
    {
      m_aExecutionDurationWarn = a;
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder addExecutionTimeExceededHandler (@Nullable final INaptrLookupTimeExceededCallback a)
    {
      if (a != null)
        m_aExecutionTimeExceededHandlers.add (a);
      return this;
    }

    @NonNull
    public final NaptrLookupBuilder debugMode (final boolean b)
    {
      m_bDebugMode = b;
      return this;
    }

    /**
     * Enable or disable DNSSEC validation. If enabled, only responses that are validated as secure
     * are accepted.
     *
     * @param b
     *        <code>true</code> to enable DNSSEC validation, <code>false</code> to disable it.
     * @return this for chaining
     * @since 11.4.7
     */
    @NonNull
    public final NaptrLookupBuilder dnsSecValidation (final boolean b)
    {
      m_bDnsSecValidation = b;
      return this;
    }

    /**
     * Set the DNSSEC trust anchors to be used. Only relevant if DNSSEC validation is enabled.
     *
     * @param s
     *        The trust anchors in DNS master file format (DS or DNSKEY records). May be
     *        <code>null</code> or empty to use the default root trust anchors.
     * @return this for chaining
     * @see DnsSecHelper#DEFAULT_ROOT_TRUST_ANCHORS
     * @since 11.4.7
     */
    @NonNull
    public final NaptrLookupBuilder dnsSecTrustAnchors (@Nullable final String s)
    {
      m_sDnsSecTrustAnchors = s;
      return this;
    }

    /**
     * Set the maximum time to cache the validated DNSSEC keys of a zone. The effective time is the
     * minimum of this value and the TTL of the keys. Only relevant if DNSSEC validation is enabled.
     *
     * @param a
     *        The maximum time. May be <code>null</code> to use the default.
     * @return this for chaining
     * @see DnsSecHelper#DEFAULT_KEY_CACHE_MAX_TTL
     * @since 11.4.7
     */
    @NonNull
    public final NaptrLookupBuilder dnsSecKeyCacheMaxTtl (@Nullable final Duration a)
    {
      m_aDnsSecKeyCacheMaxTtl = a;
      return this;
    }

    /**
     * Set the cache for the DNSSEC validating resolvers to be used. Only relevant if DNSSEC
     * validation is enabled.
     *
     * @param a
     *        The cache to use. May be <code>null</code> to use the global default from
     *        {@link DnsSecValidatingResolverCache#getDefaultInstance()}.
     * @return this for chaining
     * @since 11.4.7
     */
    @NonNull
    public final NaptrLookupBuilder dnsSecResolverCache (@Nullable final IDnsSecValidatingResolverCache a)
    {
      m_aDnsSecResolverCache = a;
      return this;
    }

    @NonNull
    public NaptrLookup build ()
    {
      if (m_aDomainName == null)
        throw new IllegalStateException ("The domain name is required");
      if (m_nMaxRetries < 0)
        throw new IllegalStateException ("The maximum number of retries must be >= 0");
      if (m_eLookupMode == null)
        throw new IllegalStateException ("The network lookup mode must be provided");

      return new NaptrLookup (m_aDomainName,
                              m_aCustomDNSServers,
                              m_nMaxRetries,
                              m_aTimeout,
                              m_eLookupMode,
                              m_aExecutionDurationWarn,
                              m_aExecutionTimeExceededHandlers,
                              m_bDebugMode,
                              m_bDnsSecValidation,
                              StringHelper.isNotEmpty (m_sDnsSecTrustAnchors) ? m_sDnsSecTrustAnchors
                                                                              : DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS,
                              m_aDnsSecKeyCacheMaxTtl != null ? m_aDnsSecKeyCacheMaxTtl
                                                              : DnsSecHelper.DEFAULT_KEY_CACHE_MAX_TTL,
                              m_aDnsSecResolverCache);
    }

    @NonNull
    public ICommonsList <NAPTRRecord> lookup ()
    {
      return build ().lookup ();
    }

    /**
     * Build and execute the lookup, returning a {@link NaptrLookupResult}.
     *
     * @return Never <code>null</code>.
     * @since 11.4.0
     */
    @NonNull
    public NaptrLookupResult lookupResult ()
    {
      return build ().lookupResult ();
    }
  }
}
