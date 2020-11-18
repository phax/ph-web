package com.helger.dns.naptr;

import java.net.InetAddress;
import java.time.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.callback.CallbackList;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.timing.StopWatch;
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
  private static final Logger LOGGER = LoggerFactory.getLogger (NaptrLookup.class);

  private final Name m_aDomainName;
  private final ICommonsList <InetAddress> m_aCustomDNSServers;
  private final int m_nMaxRetries;
  private final Duration m_aTimeout;
  private final Duration m_aExecutionDurationWarn;
  private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers;

  public NaptrLookup (@Nonnull final Name aDomainName,
                      @Nullable final ICommonsList <InetAddress> aCustomDNSServers,
                      @Nonnegative final int nMaxRetries,
                      @Nullable final Duration aTimeout,
                      @Nullable final Duration aExecutionDurationWarn,
                      @Nullable final CallbackList <INaptrLookupTimeExceededCallback> aExecutionTimeExceededHandlers)
  {
    ValueEnforcer.notNull (aDomainName, "DomainName");
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");

    m_aDomainName = aDomainName;
    m_aCustomDNSServers = new CommonsArrayList <> (aCustomDNSServers);
    m_nMaxRetries = nMaxRetries;
    m_aTimeout = aTimeout;
    m_aExecutionDurationWarn = aExecutionDurationWarn;
    m_aExecutionTimeExceededHandlers = new CallbackList <> (aExecutionTimeExceededHandlers);
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

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Trying to look up NAPTR on '" + sDomainName + "'");

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

      // By default try UDP
      // Stumbled upon an issue, where UDP datagram size was too small for MTU
      // size of 1500
      Record [] aRecords;
      int nLeft = m_nMaxRetries;
      do
      {
        aRecords = aLookup.run ();
        --nLeft;
      } while (aLookup.getResult () == Lookup.TRY_AGAIN && nLeft >= 0);

      if (aLookup.getResult () == Lookup.TRY_AGAIN)
      {
        // Retry with TCP instead of UDP
        aResolver.setTCP (true);

        nLeft = m_nMaxRetries;
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
          LOGGER.warn ("Error looking up '" + sDomainName + "': " + aLookup.getErrorString ());
        return null;
      }

      final ICommonsList <NAPTRRecord> ret = new CommonsArrayList <> ();
      for (final Record aRecord : aRecords)
        ret.add ((NAPTRRecord) aRecord);

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Returning " + ret.size () + " NAPTR records for '" + sDomainName + "'");

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
        m_aExecutionTimeExceededHandlers.forEach (x -> x.onLookupTimeExceeded (sMessage, aDuration, m_aExecutionDurationWarn));
      }
    }
  }

  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  @NotThreadSafe
  public static class Builder
  {
    public static final int DEFAULT_MAX_RETRIES = 1;
    public static final Duration DEFAULT_EXECUTION_DURATION_WARN = Duration.ofSeconds (1);

    private Name m_aDomainName;
    private final ICommonsList <InetAddress> m_aCustomDNSServers = new CommonsArrayList <> ();
    private int m_nMaxRetries = DEFAULT_MAX_RETRIES;
    private Duration m_aTimeout;
    private Duration m_aExecutionDurationWarn = DEFAULT_EXECUTION_DURATION_WARN;
    private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers = new CallbackList <> ();

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
    public final Builder timeoutMS (final long n)
    {
      return timeout (Duration.ofMillis (n));
    }

    @Nonnull
    public final Builder timeout (@Nullable final Duration a)
    {
      m_aTimeout = a;
      return this;
    }

    @Nonnull
    public final Builder noRetries ()
    {
      return maxRetries (0);
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
    public NaptrLookup build ()
    {
      if (m_aDomainName == null)
        throw new IllegalStateException ("The domain name is required");
      if (m_nMaxRetries < 0)
        throw new IllegalStateException ("The maximum number of retries must be >= 0");

      return new NaptrLookup (m_aDomainName,
                              m_aCustomDNSServers,
                              m_nMaxRetries,
                              m_aTimeout,
                              m_aExecutionDurationWarn,
                              m_aExecutionTimeExceededHandlers);
    }

    @Nonnull
    public ICommonsList <NAPTRRecord> lookup ()
    {
      return build ().lookup ();
    }
  }
}
