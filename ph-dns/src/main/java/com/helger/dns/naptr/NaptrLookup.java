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

import java.net.InetAddress;
import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.base.builder.IBuilder;
import com.helger.base.callback.CallbackList;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.timing.StopWatch;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.dns.resolve.ResolverHelper;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
    /** First UDP than TCP */
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

  public NaptrLookup (@Nonnull final Name aDomainName,
                      @Nullable final ICommonsList <InetAddress> aCustomDNSServers,
                      @Nonnegative final int nMaxRetries,
                      @Nullable final Duration aTimeout,
                      @Nonnull final ELookupNetworkMode eLookupMode,
                      @Nullable final Duration aExecutionDurationWarn,
                      @Nullable final CallbackList <INaptrLookupTimeExceededCallback> aExecutionTimeExceededHandlers,
                      final boolean bDebugMode)
  {
    ValueEnforcer.notNull (aDomainName, "DomainName");
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");
    ValueEnforcer.notNull (eLookupMode, "LookupMode");

    m_aDomainName = aDomainName;
    m_aCustomDNSServers = new CommonsArrayList <> (aCustomDNSServers);
    m_nMaxRetries = nMaxRetries;
    m_aTimeout = aTimeout;
    m_eLookupMode = eLookupMode;
    m_aExecutionDurationWarn = aExecutionDurationWarn;
    m_aExecutionTimeExceededHandlers = new CallbackList <> (aExecutionTimeExceededHandlers);
    m_bDebugMode = bDebugMode;
  }

  /**
   * Perform the DNS lookup based on the parameters provided in the constructor.
   *
   * @return A never <code>null</code> but maybe empty list of records.
   */
  @Nonnull
  public ICommonsList <NAPTRRecord> lookup ()
  {
    // Omit the final dot
    final String sDomainName = m_aDomainName.toString (true);

    LOGGER.info ("Trying to look up NAPTR on '" +
                 sDomainName +
                 "'" +
                 (m_nMaxRetries > 0 ? " with " + m_nMaxRetries + " retries" : "") +
                 " using network mode " +
                 m_eLookupMode);

    final BooleanSupplier aIsEnabled = m_bDebugMode ? LOGGER::isInfoEnabled : LOGGER::isDebugEnabled;
    final Consumer <String> aLogger = m_bDebugMode ? LOGGER::info : LOGGER::debug;

    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      // Use the default (static) cache that is used by default
      final ExtendedResolver aResolver = ResolverHelper.createExtendedResolver (m_aCustomDNSServers);
      aResolver.setRetries (m_nMaxRetries);
      if (m_aTimeout != null)
        aResolver.setTimeout (m_aTimeout);

      final Lookup aLookup = new Lookup (m_aDomainName, Type.NAPTR);
      aLookup.setResolver (aResolver);

      int nLookupRuns = 0;
      boolean bCanTryAgain = true;
      Record [] aRecords = null;
      if (m_eLookupMode.isUDP ())
      {
        if (aIsEnabled.getAsBoolean ())
          aLogger.accept ("  Trying UDP for NAPTR lookup after " + nLookupRuns + " unsuccessful lopkups");

        // By default try UDP
        // Stumbled upon an issue, where UDP datagram size was too small for MTU
        // size of 1500
        int nLeft = m_nMaxRetries;
        do
        {
          aRecords = aLookup.run ();
          if (aIsEnabled.getAsBoolean ())
            aLogger.accept ("    Result of UDP lookup " + nLookupRuns + ": " + aLookup.getErrorString ());

          nLeft--;
          nLookupRuns++;
        } while (aLookup.getResult () == Lookup.TRY_AGAIN && nLeft >= 0);
        if (aLookup.getResult () != Lookup.TRY_AGAIN)
          bCanTryAgain = false;
      }
      if (bCanTryAgain && m_eLookupMode.isTCP ())
      {
        if (aIsEnabled.getAsBoolean ())
          aLogger.accept ("  Trying TCP for NAPTR lookup after " + nLookupRuns + " unsuccessful lopkups");

        // Retry with TCP instead of UDP
        aResolver.setTCP (true);

        // Restore max retries for TCP
        int nLeft = m_nMaxRetries;
        do
        {
          aRecords = aLookup.run ();
          if (aIsEnabled.getAsBoolean ())
            aLogger.accept ("    Result of TCP lookup " + nLookupRuns + ": " + aLookup.getErrorString ());

          nLeft--;
          nLookupRuns++;
        } while (aLookup.getResult () == Lookup.TRY_AGAIN && nLeft >= 0);
      }
      if (aLookup.getResult () != Lookup.SUCCESSFUL)
      {
        // Wrong domain name
        LOGGER.warn ("Error looking up '" +
                     sDomainName +
                     "' [" +
                     aLookup.getResult () +
                     "]: " +
                     aLookup.getErrorString ());
        return new CommonsArrayList <> ();
      }
      final ICommonsList <NAPTRRecord> ret = new CommonsArrayList <> ();
      for (final Record aRecord : aRecords)
        ret.add ((NAPTRRecord) aRecord);

      if (aIsEnabled.getAsBoolean ())
        aLogger.accept ("  Returning " +
                        ret.size () +
                        " NAPTR record(s) for '" +
                        sDomainName +
                        "' after " +
                        nLookupRuns +
                        " lookups");

      return ret;
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

  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  @NotThreadSafe
  public static class Builder implements IBuilder <NaptrLookup>
  {
    public static final int DEFAULT_MAX_RETRIES = 1;
    public static final Duration DEFAULT_EXECUTION_DURATION_WARN = Duration.ofSeconds (1);
    public static final ELookupNetworkMode DEFAULT_LOOKUP_MODE = ELookupNetworkMode.UDP_TCP;

    private Name m_aDomainName;
    private final ICommonsList <InetAddress> m_aCustomDNSServers = new CommonsArrayList <> ();
    private int m_nMaxRetries = DEFAULT_MAX_RETRIES;
    private Duration m_aTimeout;
    private Duration m_aExecutionDurationWarn = DEFAULT_EXECUTION_DURATION_WARN;
    private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers = new CallbackList <> ();
    private ELookupNetworkMode m_eLookupMode = DEFAULT_LOOKUP_MODE;
    private boolean m_bDebugMode = false;

    public Builder ()
    {
      // add a default handler
      m_aExecutionTimeExceededHandlers.add (new LoggingNaptrLookupTimeExceededCallback (false));
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

    @Nonnull
    public final Builder domainName (@Nullable final String s) throws TextParseException
    {
      return domainName (Name.fromString (s));
    }

    @Nonnull
    public final Builder domainName (@Nullable final Name a)
    {
      m_aDomainName = a;
      return this;
    }

    @Nonnull
    public final Builder customDNSServer (@Nullable final InetAddress a)
    {
      if (a == null)
        m_aCustomDNSServers.clear ();
      else
        m_aCustomDNSServers.set (a);
      return this;
    }

    @Nonnull
    public final Builder customDNSServers (@Nullable final InetAddress... a)
    {
      if (a == null)
        m_aCustomDNSServers.clear ();
      else
        m_aCustomDNSServers.setAll (a);
      return this;
    }

    @Nonnull
    public final Builder customDNSServers (@Nullable final Iterable <? extends InetAddress> a)
    {
      if (a == null)
        m_aCustomDNSServers.clear ();
      else
        m_aCustomDNSServers.setAll (a);
      return this;
    }

    @Nonnull
    public final Builder addCustomDNSServer (@Nullable final InetAddress a)
    {
      if (a != null)
        m_aCustomDNSServers.add (a);
      return this;
    }

    @Nonnull
    public final Builder addCustomDNSServers (@Nullable final InetAddress... a)
    {
      if (a != null)
        m_aCustomDNSServers.addAll (a);
      return this;
    }

    @Nonnull
    public final Builder addCustomDNSServers (@Nullable final Iterable <? extends InetAddress> a)
    {
      if (a != null)
        m_aCustomDNSServers.addAll (a);
      return this;
    }

    @Nonnull
    public final Builder maxRetries (final int n)
    {
      m_nMaxRetries = n;
      return this;
    }

    @Nonnull
    public final Builder noRetries ()
    {
      return maxRetries (0);
    }

    @Nonnull
    public final Builder timeoutMS (final long n)
    {
      return timeout (n < 0 ? null : Duration.ofMillis (n));
    }

    @Nonnull
    public final Builder timeout (@Nullable final Duration a)
    {
      m_aTimeout = a;
      return this;
    }

    @Nonnull
    public final Builder lookupMode (@Nullable final ELookupNetworkMode e)
    {
      m_eLookupMode = e;
      return this;
    }

    @Nonnull
    public final Builder executionDurationWarnMS (final long nMillis)
    {
      return executionDurationWarn (nMillis < 0 ? null : Duration.ofMillis (nMillis));
    }

    @Nonnull
    public final Builder executionDurationWarn (@Nullable final Duration a)
    {
      m_aExecutionDurationWarn = a;
      return this;
    }

    @Nonnull
    public final Builder addExecutionTimeExceededHandler (@Nullable final INaptrLookupTimeExceededCallback a)
    {
      if (a != null)
        m_aExecutionTimeExceededHandlers.add (a);
      return this;
    }

    @Nonnull
    public final Builder debugMode (final boolean b)
    {
      m_bDebugMode = b;
      return this;
    }

    @Nonnull
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
                              m_bDebugMode);
    }

    @Nonnull
    public ICommonsList <NAPTRRecord> lookup ()
    {
      return build ().lookup ();
    }
  }
}
