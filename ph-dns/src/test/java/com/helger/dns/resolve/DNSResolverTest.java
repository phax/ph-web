/**
 * Copyright (C) 2020 Philip Helger (www.helger.com)
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
package com.helger.dns.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DNSResolverTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DNSResolverTest.class);

  @Nullable
  private static InetAddress _resolveByName (@Nonnull final String sHostName)
  {
    LOGGER.info ("Trying to resolve '" + sHostName + "'");
    return DNSResolver.resolveByName (sHostName);
  }

  @Test
  public void testValid ()
  {
    // Is null when Internet connection is not present, non-null otherwise
    _resolveByName ("www.dnsjava.org");

    // In Copenhagen I resolved "bogus.host.foobar" to "67.215.65.132" which was
    // opendns.com -> maybe has anything to do with the DNS server I was using
    // there
    final InetAddress aAddr = _resolveByName ("bogus.host.foobar");
    if (aAddr != null)
    {
      // A DNS server resolving anything was used
      assertEquals (aAddr, _resolveByName ("jh<adsjkhd<a asd kjh "));
    }

    assertNotNull (DNSResolver.getMyIpAddress ());
    LOGGER.info ("My IP address: " + DNSResolver.getMyIpAddress ());
  }
}
