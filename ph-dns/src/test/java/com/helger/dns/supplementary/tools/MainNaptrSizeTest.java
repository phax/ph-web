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
package com.helger.dns.supplementary.tools;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

public class MainNaptrSizeTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainNaptrSizeTest.class);

  public static void main (final String [] args) throws Exception
  {
    final String domain = "imlmdqeh6sltqxo3o4sikexczj5b5gznirosjf7vmxhwlql6reua.iso6523-actorid-upis.acc.edelivery.tech.ec.europa.eu";

    // Query directly against a public resolver that supports TCP,
    // bypassing your local stub entirely
    final SimpleResolver resolver = new SimpleResolver (new InetSocketAddress ("8.8.8.8", 53));

    final Name name = Name.fromString (domain + ".");
    final Message query = Message.newQuery (Record.newRecord (name, Type.NAPTR, DClass.IN));

    // --- Test 0: Query local stub directly (UDP) ---
    final SimpleResolver localResolver = new SimpleResolver (new InetSocketAddress ("127.0.0.1", 53));
    localResolver.setTCP (false);
    try
    {
      final Message localUdpResponse = localResolver.send (query);
      final boolean localTruncated = localUdpResponse.getHeader ().getFlag (Flags.TC);
      LOGGER.info ("=== Local Stub UDP Response (127.0.0.1:53) ===");
      LOGGER.info ("Truncated (TC flag set): " + localTruncated);
      LOGGER.info ("RCODE: " + Rcode.string (localUdpResponse.getRcode ()));
      LOGGER.info ("NAPTR records returned: " + localUdpResponse.getSection (Section.ANSWER).size ());
    }
    catch (final Exception e)
    {
      LOGGER.info ("Local stub UDP failed: " + e);
    }

    // --- Test 1: Send over UDP, check if response is truncated ---
    resolver.setTCP (false);
    final Message udpResponse = resolver.send (query);
    final boolean truncated = udpResponse.getHeader ().getFlag (Flags.TC);
    final byte [] udpWire = udpResponse.toWire ();

    LOGGER.info ("=== UDP Response ===");
    LOGGER.info ("Truncated (TC flag set): " + truncated);
    LOGGER.info ("Wire size (bytes):        " + udpWire.length);
    LOGGER.info ("NAPTR records returned:   " + udpResponse.getSection (Section.ANSWER).size ());

    // --- Test 2: Send over TCP, get the full response ---
    resolver.setTCP (true);
    final Message tcpResponse = resolver.send (query);
    final byte [] tcpWire = tcpResponse.toWire ();

    LOGGER.info ("\n=== TCP Response ===");
    LOGGER.info ("Wire size (bytes):      " + tcpWire.length);
    LOGGER.info ("NAPTR records returned: " + tcpResponse.getSection (Section.ANSWER).size ());

    for (final Record r : tcpResponse.getSection (Section.ANSWER))
    {
      LOGGER.info ("  " + r);
    }

    // --- Conclusion ---
    LOGGER.info ("\n=== Conclusion ===");
    if (truncated)
    {
      LOGGER.info ("UDP response was truncated — this is why dnsjava falls back to TCP.");
    }
    else
      if (udpResponse.getSection (Section.ANSWER).isEmpty ())
      {
        LOGGER.info ("UDP returned no records — possible local resolver issue, not a size problem.");
      }
      else
      {
        LOGGER.info ("UDP response was NOT truncated (" +
                     udpWire.length +
                     " bytes) — the TCP fallback has a different cause.");
        LOGGER.info ("Likely your local stub at 127.0.0.1:53 refuses TCP regardless of record size.");
      }
  }
}
