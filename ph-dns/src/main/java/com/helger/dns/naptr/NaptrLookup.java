package com.helger.dns.naptr;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.helger.commons.CGlobal;
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
public class NaptrLookup
{
  private static final Logger LOGGER = LoggerFactory.getLogger (NaptrLookup.class);

  private final Name m_aDomainName;
  private final ICommonsList <InetAddress> m_aCustomDNSServers;
  private final int m_nMaxRetries;
  private final long m_nExecutionDurationWarnMS;
  private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers;

  public NaptrLookup (@Nonnull final Name aDomainName,
                      @Nullable final ICommonsList <InetAddress> aCustomDNSServers,
                      @Nonnegative final int nMaxRetries,
                      @CheckForSigned final long nExecutionDurationWarnMS,
                      @Nullable final CallbackList <INaptrLookupTimeExceededCallback> aExecutionTimeExceededHandlers)
  {
    ValueEnforcer.notNull (aDomainName, "DNSName");
    ValueEnforcer.isGE0 (nMaxRetries, "MAxRetries");

    m_aDomainName = aDomainName;
    m_aCustomDNSServers = new CommonsArrayList <> (aCustomDNSServers);
    m_nMaxRetries = nMaxRetries;
    m_nExecutionDurationWarnMS = nExecutionDurationWarnMS;
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
      final Lookup aLookup = new Lookup (m_aDomainName, Type.NAPTR);
      final ExtendedResolver aResolver = ResolverHelper.createExtendedResolver (m_aCustomDNSServers);
      aResolver.setRetries (m_nMaxRetries);
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
      if (m_nExecutionDurationWarnMS > 0 && aSW.getMillis () > m_nExecutionDurationWarnMS)
      {
        final String sMessage = "Looking up NAPTR record of '" +
                                sDomainName +
                                "'" +
                                (m_nMaxRetries > 0 ? " with " + m_nMaxRetries + " retries" : "");
        m_aExecutionTimeExceededHandlers.forEach (x -> x.onLookupTimeExceeded (sMessage, aSW.getMillis (), m_nExecutionDurationWarnMS));
      }
    }
  }

  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  public static class Builder
  {
    public static final int DEFAULT_MAX_RETRIES = 1;
    public static final long DEFAULT_EXECUTION_DURATION_WARN_MS = CGlobal.MILLISECONDS_PER_SECOND;

    private Name m_aDomainName;
    private final ICommonsList <InetAddress> m_aCustomDNSServers = new CommonsArrayList <> ();
    private int m_nMaxRetries = DEFAULT_MAX_RETRIES;
    private long m_nExecutionDurationWarnMS = DEFAULT_EXECUTION_DURATION_WARN_MS;
    private final CallbackList <INaptrLookupTimeExceededCallback> m_aExecutionTimeExceededHandlers = new CallbackList <> ();

    public Builder ()
    {
      // add a default handler
      m_aExecutionTimeExceededHandlers.add (new LoggingNaptrLookupTimeExceededCallback (false));
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
    public final Builder customDNSServers (@Nullable final InetAddress a)
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
    public final Builder maxRetries (@Nonnegative final int n)
    {
      ValueEnforcer.isGE0 (n, "MaxRetries");
      m_nMaxRetries = n;
      return this;
    }

    @Nonnull
    public final Builder executionTimeWarnMS (@Nullable final TimeUnit eTimeUnit, final long nDuration)
    {
      return executionTimeWarnMS (eTimeUnit == null ? 0 : eTimeUnit.toSeconds (nDuration));
    }

    @Nonnull
    public final Builder executionTimeWarnMS (final long n)
    {
      m_nExecutionDurationWarnMS = n;
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
        throw new IllegalStateException ("DomainName is required");

      return new NaptrLookup (m_aDomainName,
                              m_aCustomDNSServers,
                              m_nMaxRetries,
                              m_nExecutionDurationWarnMS,
                              m_aExecutionTimeExceededHandlers);
    }

    @Nonnull
    public ICommonsList <NAPTRRecord> lookup ()
    {
      return build ().lookup ();
    }
  }
}
