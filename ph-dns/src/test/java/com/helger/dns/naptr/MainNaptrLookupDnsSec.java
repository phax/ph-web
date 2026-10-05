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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;

import com.helger.base.timing.StopWatch;
import com.helger.dns.config.DNSConfig;

/**
 * Manual test for DNSSEC validating NAPTR lookups. Requires network access - therefore not a unit
 * test.
 *
 * @author Philip Helger
 */
public final class MainNaptrLookupDnsSec
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainNaptrLookupDnsSec.class);

  public static void main (final String [] args) throws Exception
  {
    // Signed zone without NAPTR records - expect TYPE_NOT_FOUND / SECURE
    // Unsigned zone - expect DNSSEC_VALIDATION_FAILED / INSECURE
    // Deliberately broken signatures - expect DNSSEC_VALIDATION_FAILED / BOGUS
    // The second round reuses the cached validating resolver and should therefore be a lot faster
    for (int nRound = 0; nRound < 2; ++nRound)
      for (final String sDomain : new String [] { "example.com", "helger.com", "dnssec-failed.org" })
      {
        final StopWatch aSW = StopWatch.createdStarted ();
        final NaptrLookupResult aResult = NaptrLookup.builder ()
                                                     .domainName (Name.fromString (sDomain, Name.root))
                                                     // The DNS server must forward the DNSSEC records
                                                     .customDNSServer (DNSConfig.DNS_CLOUDFLARE_1)
                                                     .dnsSecValidation (true)
                                                     .lookupResult ();
        LOGGER.info ("[" + nRound + "] " + sDomain + " (" + aSW.stopAndGetMillis () + " ms): " + aResult);
      }
  }
}
