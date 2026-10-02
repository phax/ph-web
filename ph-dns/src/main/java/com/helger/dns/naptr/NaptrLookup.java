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
import com.helger.dns.dnssec.EDnsSecValidationStatus;
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
  private final boolean m_bDNSSECValidation;
  private final String m_sDNSSECTrustAnchors;

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
          DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS);
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
   * @param bDNSSECValidation
   *        <code>true</code> to require a DNSSEC validated (secure) response.
   * @param sDNSSECTrustAnchors
   *        The DNSSEC trust anchors in DNS master file format. May neither be <code>null</code> nor
   *        empty.
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
                      final boolean bDNSSECValidation,
                      @NonNull @Nonempty final String sDNSSECTrustAnchors)
  {
    ValueEnforcer.notNull (aDomainName, "DomainName");
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");
    ValueEnforcer.notNull (eLookupMode, "LookupMode");
    ValueEnforcer.notEmpty (sDNSSECTrustAnchors, "DNSSECTrustAnchors");

    m_aDomainName = aDomainName;
    m_aCustomDNSServers = new CommonsArrayList <> (aCustomDNSServers);
    m_nMaxRetries = nMaxRetries;
    m_aTimeout = aTimeout;
    m_eLookupMode = eLookupMode;
    m_aExecutionDurationWarn = aExecutionDurationWarn;
    m_aExecutionTimeExceededHandlers = new CallbackList <> (aExecutionTimeExceededHandlers);
    m_bDebugMode = bDebugMode;
    m_bDNSSECValidation = bDNSSECValidation;
    m_sDNSSECTrustAnchors = sDNSSECTrustAnchors;
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
   * the configured trust anchors, and the shared dnsjava cache is not used. Every response that is
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
                            (m_bDNSSECValidation ? " with DNSSEC validation" : "") +
                            (m_aCustomDNSServers.isNotEmpty () ? " and the custom DNS server(s) " +
                                                                 StringImplode.imploder ()
                                                                              .separator (", ")
                                                                              .source (m_aCustomDNSServers,
                                                                                       InetAddress::getHostAddress)
                                                                              .build () : ""));

    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      // Use the default (static) cache that is used by default
      final ExtendedResolver aResolver = ResolverHelper.createExtendedResolver (m_aCustomDNSServers);

      // Retries are handled internally by the ExtendedResolver
      aResolver.setRetries (m_nMaxRetries);
      if (m_aTimeout != null)
      {
        // Note: ExtendedResolver.setTimeout alone would only alter the timeout
        // of the ExtendedResolver but not the one of the contained resolvers
        ResolverHelper.setTimeout (aResolver, m_aTimeout);
      }

      final Lookup aLookup = new Lookup (m_aDomainName, Type.NAPTR);
      DnsSecStatusRecordingResolver aDNSSECResolver = null;
      if (m_bDNSSECValidation)
      {
        try
        {
          aDNSSECResolver = new DnsSecStatusRecordingResolver (DnsSecHelper.createValidatingResolver (aResolver,
                                                                                                    m_sDNSSECTrustAnchors));
        }
        catch (final IOException ex)
        {
          LOGGER.error ("Failed to load the DNSSEC trust anchors: " + ex.getMessage ());
          return NaptrLookupResult.failure (ENaptrLookupStatus.DNSSEC_VALIDATION_FAILED,
                                            "Failed to load the DNSSEC trust anchors: " + ex.getMessage ());
        }
        aLookup.setResolver (aDNSSECResolver);
        // Use a temporary cache, as the shared cache may contain records that were not validated
        aLookup.setCache (null);
      }
      else
        aLookup.setResolver (aResolver);

      int nLookupRuns = 0;
      boolean bCanTryAgain = true;
      Record [] aRecords = null;

      if (m_eLookupMode.isUDP ())
      {
        aCondLogger.info (() -> "  Trying UDP for NAPTR lookup");

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
        if (aDNSSECResolver != null && aDNSSECResolver.getValidationStatus ().isBogus ())
          bCanTryAgain = false;
      }

      if (bCanTryAgain && m_eLookupMode.isTCP ())
      {
        final int nFinalLookupRuns = nLookupRuns;
        aCondLogger.info (() -> "  Trying TCP for NAPTR lookup after " +
                                nFinalLookupRuns +
                                " unsuccessful UDP lookup(s)");

        // Retry with TCP instead of UDP
        aResolver.setTCP (true);
        if (aDNSSECResolver != null)
          aDNSSECResolver.reset ();
        aRecords = aLookup.run ();
        nLookupRuns++;
        aCondLogger.info (() -> "    Result of TCP lookup: " + aLookup.getErrorString ());
      }

      final EDnsSecValidationStatus eDNSSECStatus = aDNSSECResolver == null ? EDnsSecValidationStatus.NOT_VALIDATED
                                                                            : aDNSSECResolver.getValidationStatus ();
      // NOT_VALIDATED means that no response was received at all - that is a technical failure
      if (aDNSSECResolver != null &&
          !eDNSSECStatus.isSecure () &&
          (eDNSSECStatus != EDnsSecValidationStatus.NOT_VALIDATED || aLookup.getResult () == Lookup.SUCCESSFUL))
      {
        final String sBogusReason = aDNSSECResolver.getBogusReason ();
        final String sErrorMessage = "DNSSEC validation of '" +
                                     sDomainName +
                                     "' failed with status " +
                                     eDNSSECStatus +
                                     (sBogusReason != null ? ": " + sBogusReason : "");
        LOGGER.warn (sErrorMessage);
        return NaptrLookupResult.failure (ENaptrLookupStatus.DNSSEC_VALIDATION_FAILED, sErrorMessage, eDNSSECStatus);
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
        return NaptrLookupResult.failure (eStatus, aLookup.getErrorString (), eDNSSECStatus);
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
      return NaptrLookupResult.success (ret, eDNSSECStatus);
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
    private boolean m_bDNSSECValidation = DEFAULT_DNSSEC_VALIDATION;
    private String m_sDNSSECTrustAnchors = DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS;

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
    public final NaptrLookupBuilder dnssecValidation (final boolean b)
    {
      m_bDNSSECValidation = b;
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
    public final NaptrLookupBuilder dnssecTrustAnchors (@Nullable final String s)
    {
      m_sDNSSECTrustAnchors = s;
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
                              m_bDNSSECValidation,
                              StringHelper.isNotEmpty (m_sDNSSECTrustAnchors) ? m_sDNSSECTrustAnchors
                                                                              : DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS);
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
