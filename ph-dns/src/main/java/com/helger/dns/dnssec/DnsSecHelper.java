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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.dnssec.ValidatingResolver;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;

/**
 * Helper class for DNSSEC validation based on the dnsjava {@link ValidatingResolver}.
 *
 * @author Philip Helger
 * @since 11.4.7
 */
@Immutable
public final class DnsSecHelper
{
  /**
   * The DS records of the DNS root zone Key Signing Keys, as published by IANA at
   * https://data.iana.org/root-anchors/root-anchors.xml. Contains KSK-2017 (key tag 20326) and
   * KSK-2024 (key tag 38696), so that validation keeps working across the root KSK rollover.
   */
  public static final String DEFAULT_ROOT_TRUST_ANCHORS = ". IN DS 20326 8 2 E06D44B80B8F1D39A95C0B0D7C65D08458E880409BBC683457104237C7F8EC8D\n" +
                                                          ". IN DS 38696 8 2 683D2D0ACB8C9B712A1948B27F741219298D0A450D612C483AF444A4C0FB2B16\n";

  private DnsSecHelper ()
  {}

  /**
   * Create a new DNSSEC validating resolver on top of the provided resolver. The provided resolver
   * must point to recursive DNS servers. The validating resolver requests the DNSSEC records itself
   * and performs the validation locally - the AD flag of the upstream server is not trusted.
   *
   * @param aHeadResolver
   *        The resolver to send the DNS queries to. May not be <code>null</code>.
   * @param sTrustAnchors
   *        The trust anchors in DNS master file format (DS or DNSKEY records). May neither be
   *        <code>null</code> nor empty. Usually {@link #DEFAULT_ROOT_TRUST_ANCHORS}.
   * @return The new validating resolver. Never <code>null</code>.
   * @throws IOException
   *         If the trust anchors could not be parsed
   */
  @NonNull
  public static ValidatingResolver createValidatingResolver (@NonNull final Resolver aHeadResolver,
                                                             @NonNull @Nonempty final String sTrustAnchors) throws IOException
  {
    ValueEnforcer.notNull (aHeadResolver, "HeadResolver");
    ValueEnforcer.notEmpty (sTrustAnchors, "TrustAnchors");

    final ValidatingResolver ret = new ValidatingResolver (aHeadResolver);
    ret.loadTrustAnchors (new NonBlockingByteArrayInputStream (sTrustAnchors.getBytes (StandardCharsets.US_ASCII)));
    if (ret.getTrustAnchors ().items ().isEmpty ())
      throw new IOException ("No DNSSEC trust anchor could be read from the provided string");

    // Required to get the reason why a response is bogus
    ret.setAddReasonToAdditional (true);
    return ret;
  }
}
