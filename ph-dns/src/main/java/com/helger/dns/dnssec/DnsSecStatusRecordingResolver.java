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
package com.helger.dns.dnssec;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.xbill.DNS.EDNSOption;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.dnssec.ValidatingResolver;

import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringImplode;

/**
 * A {@link Resolver} wrapping a {@link ValidatingResolver} that records the DNSSEC validation
 * status of all responses passing through it. This is needed, because dnsjava's
 * {@link org.xbill.DNS.Lookup} does not expose the response header (AD flag) and maps bogus
 * responses (SERVFAIL) to the same result as transient network errors.<br>
 * The aggregated status is the worst status of all recorded responses, so when using this with a
 * {@link org.xbill.DNS.Lookup}, absolute domain names should be used to avoid queries for search
 * path entries.<br>
 * The wrapped validating resolver is usually shared between lookups (see
 * {@link IDnsSecValidatingResolverCache}), therefore all modifying methods throw an
 * {@link UnsupportedOperationException}. Create one instance of this class per lookup.
 *
 * @author Philip Helger
 * @since 11.4.7
 */
@ThreadSafe
public class DnsSecStatusRecordingResolver implements Resolver
{
  private static final String READ_ONLY_MSG = "The shared validating resolver must not be modified";

  private final ValidatingResolver m_aValidatingResolver;
  private final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("m_aRWLock")
  private EDnsSecValidationStatus m_eStatus = EDnsSecValidationStatus.NOT_VALIDATED;
  @GuardedBy ("m_aRWLock")
  private String m_sBogusReason;

  public DnsSecStatusRecordingResolver (@NonNull final ValidatingResolver aValidatingResolver)
  {
    ValueEnforcer.notNull (aValidatingResolver, "ValidatingResolver");
    m_aValidatingResolver = aValidatingResolver;
  }

  @Nullable
  private static String _getValidationReason (@NonNull final Message aResponse)
  {
    for (final Record aRecord : aResponse.getSection (Section.ADDITIONAL))
      if (aRecord instanceof final TXTRecord aTXT &&
          aRecord.getDClass () == ValidatingResolver.VALIDATION_REASON_QCLASS)
        return StringImplode.getImploded (aTXT.getStrings ());
    return null;
  }

  private void _recordResponse (@NonNull final Message aResponse)
  {
    final EDnsSecValidationStatus eResponseStatus;
    String sBogusReason = null;
    if (aResponse.getHeader ().getFlag (Flags.AD))
      eResponseStatus = EDnsSecValidationStatus.SECURE;
    else
    {
      // The ValidatingResolver turns bogus responses into SERVFAIL and adds the reason
      sBogusReason = aResponse.getRcode () == Rcode.SERVFAIL ? _getValidationReason (aResponse) : null;
      eResponseStatus = sBogusReason != null ? EDnsSecValidationStatus.BOGUS : EDnsSecValidationStatus.INSECURE;
    }

    final String sFinalBogusReason = sBogusReason;
    m_aRWLock.writeLocked (() -> {
      // Keep the worst status - relies on the declaration order of the enum
      if (eResponseStatus.ordinal () > m_eStatus.ordinal ())
      {
        m_eStatus = eResponseStatus;
        if (sFinalBogusReason != null)
          m_sBogusReason = sFinalBogusReason;
      }
    });
  }

  /**
   * Reset the recorded status to {@link EDnsSecValidationStatus#NOT_VALIDATED}.
   */
  public void reset ()
  {
    m_aRWLock.writeLocked (() -> {
      m_eStatus = EDnsSecValidationStatus.NOT_VALIDATED;
      m_sBogusReason = null;
    });
  }

  /**
   * @return The worst DNSSEC validation status of all responses received since creation or the
   *         last {@link #reset()}. {@link EDnsSecValidationStatus#NOT_VALIDATED} if no response was
   *         received. Never <code>null</code>.
   */
  @NonNull
  public EDnsSecValidationStatus getValidationStatus ()
  {
    return m_aRWLock.readLockedGet (() -> m_eStatus);
  }

  /**
   * @return The reason provided by the validating resolver, why a response is bogus. Only set if
   *         the status is {@link EDnsSecValidationStatus#BOGUS}. May be <code>null</code>.
   */
  @Nullable
  public String getBogusReason ()
  {
    return m_aRWLock.readLockedGet (() -> m_sBogusReason);
  }

  public void setPort (final int nPort)
  {
    throw new UnsupportedOperationException (READ_ONLY_MSG);
  }

  public void setTCP (final boolean bFlag)
  {
    throw new UnsupportedOperationException (READ_ONLY_MSG);
  }

  public void setIgnoreTruncation (final boolean bFlag)
  {
    throw new UnsupportedOperationException (READ_ONLY_MSG);
  }

  public void setEDNS (final int nVersion,
                       final int nPayloadSize,
                       final int nFlags,
                       @Nullable final List <EDNSOption> aOptions)
  {
    throw new UnsupportedOperationException (READ_ONLY_MSG);
  }

  public void setTSIGKey (@Nullable final TSIG aKey)
  {
    throw new UnsupportedOperationException (READ_ONLY_MSG);
  }

  public void setTimeout (@NonNull final Duration aTimeout)
  {
    throw new UnsupportedOperationException (READ_ONLY_MSG);
  }

  public Duration getTimeout ()
  {
    return m_aValidatingResolver.getTimeout ();
  }

  public CompletionStage <Message> sendAsync (@NonNull final Message aQuery, @NonNull final Executor aExecutor)
  {
    return m_aValidatingResolver.sendAsync (aQuery, aExecutor).thenApply (aResponse -> {
      _recordResponse (aResponse);
      return aResponse;
    });
  }
}
