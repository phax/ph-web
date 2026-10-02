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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.dnssec.ValidatingResolver;

/**
 * Test class for class {@link DnsSecHelper}.
 *
 * @author Philip Helger
 */
public final class DnsSecHelperTest
{
  @Test
  public void testDefaultRootTrustAnchors () throws IOException
  {
    final ValidatingResolver aResolver = DnsSecHelper.createValidatingResolver (new SimpleResolver (),
                                                                                DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS);
    assertNotNull (aResolver);
    // Both DS records form a single RRset
    assertEquals (1, aResolver.getTrustAnchors ().items ().size ());
    assertEquals (2, aResolver.getTrustAnchors ().items ().iterator ().next ().size ());
    assertTrue (aResolver.isAddReasonToAdditional ());
  }

  @Test
  public void testNoUsableTrustAnchor ()
  {
    try
    {
      // Syntactically valid, but neither DS nor DNSKEY
      DnsSecHelper.createValidatingResolver (new SimpleResolver (), "example.org. IN A 192.0.2.1\n");
      fail ();
    }
    catch (final IOException ex)
    {
      // expected
    }
  }

  @Test
  public void testInvalidTrustAnchor ()
  {
    try
    {
      DnsSecHelper.createValidatingResolver (new SimpleResolver (), ". IN DS this is not a DS record\n");
      fail ();
    }
    catch (final IOException ex)
    {
      // expected
    }
  }
}
