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

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;

/**
 * Outcome of a DNS NAPTR lookup. The values mirror the result codes of dnsjava's
 * {@link org.xbill.DNS.Lookup} but expose semantic groupings via convenience predicates so that
 * callers can distinguish a missing DNS entry ("addressee is not registered") from a technical DNS
 * problem ("DNS infrastructure is currently unreachable").
 *
 * @author Philip Helger
 * @since 11.4.0
 */
public enum ENaptrLookupStatus
{
  /** The lookup was successful and at least one NAPTR record was returned. */
  SUCCESSFUL (Lookup.SUCCESSFUL),
  /**
   * The lookup failed due to a non-recoverable DNS error (e.g. server-side data error). Repeating
   * the lookup is unlikely to help.
   */
  UNRECOVERABLE (Lookup.UNRECOVERABLE),
  /** The lookup failed due to a transient network error. Repeating the lookup later may help. */
  TRY_AGAIN (Lookup.TRY_AGAIN),
  /** The host does not exist (NXDOMAIN). The addressee is not registered in DNS. */
  HOST_NOT_FOUND (Lookup.HOST_NOT_FOUND),
  /** The host exists, but no NAPTR records are associated with it (NODATA). */
  TYPE_NOT_FOUND (Lookup.TYPE_NOT_FOUND);

  private static final Logger LOGGER = LoggerFactory.getLogger (ENaptrLookupStatus.class);
  private final int m_nDnsJavaCode;

  ENaptrLookupStatus (final int nDnsJavaCode)
  {
    m_nDnsJavaCode = nDnsJavaCode;
  }

  /**
   * @return The raw dnsjava result code this status maps to. See {@link org.xbill.DNS.Lookup}.
   */
  public int getDnsJavaResultCode ()
  {
    return m_nDnsJavaCode;
  }

  /**
   * @return <code>true</code> only for {@link #SUCCESSFUL}.
   */
  public boolean isSuccess ()
  {
    return this == SUCCESSFUL;
  }

  /**
   * @return <code>true</code> for {@link #HOST_NOT_FOUND} or {@link #TYPE_NOT_FOUND}. These are
   *         "functional" not-found cases meaning the addressee is genuinely not registered in DNS;
   *         the caller can immediately report this as such.
   */
  public boolean isFunctionalNotFound ()
  {
    return this == HOST_NOT_FOUND || this == TYPE_NOT_FOUND;
  }

  /**
   * @return <code>true</code> for {@link #TRY_AGAIN} or {@link #UNRECOVERABLE}. These indicate a
   *         technical DNS infrastructure problem rather than the absence of a NAPTR record. The
   *         caller should not interpret this as "addressee not registered".
   * @see #isRetryable()
   */
  public boolean isTechnicalFailure ()
  {
    return this == TRY_AGAIN || this == UNRECOVERABLE;
  }

  /**
   * @return <code>true</code> only for {@link #TRY_AGAIN}, where dnsjava itself indicates that
   *         repeating the lookup later may succeed. {@link #UNRECOVERABLE} returns
   *         <code>false</code> because dnsjava explicitly states that repeating the lookup will not
   *         help.
   */
  public boolean isRetryable ()
  {
    return this == TRY_AGAIN;
  }

  /**
   * Map a dnsjava {@link org.xbill.DNS.Lookup} result code to the corresponding enum value. Unknown
   * codes fall back to {@link #UNRECOVERABLE}.
   *
   * @param nCode
   *        The dnsjava result code (e.g. {@link org.xbill.DNS.Lookup#SUCCESSFUL}).
   * @return The matching status, or {@link #UNRECOVERABLE} for unknown codes.
   */
  @NonNull
  public static ENaptrLookupStatus fromDnsJavaResultCode (final int nCode)
  {
    for (final ENaptrLookupStatus e : values ())
      if (e.m_nDnsJavaCode == nCode)
        return e;
    LOGGER.warn ("The DNSJava result code " + nCode + " is unknown and is mapped to UNRECOVERABLE");
    return UNRECOVERABLE;
  }
}
