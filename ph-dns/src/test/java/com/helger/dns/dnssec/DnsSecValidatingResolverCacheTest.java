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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Duration;

import org.junit.Test;
import org.xbill.DNS.dnssec.ValidatingResolver;

/**
 * Test class for class {@link DnsSecValidatingResolverCache}.
 *
 * @author Philip Helger
 */
public final class DnsSecValidatingResolverCacheTest
{
  private static DnsSecValidatingResolverKey _key (final boolean bTcp, final int nMaxRetries)
  {
    return new DnsSecValidatingResolverKey (null,
                                            nMaxRetries,
                                            null,
                                            bTcp,
                                            DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS,
                                            DnsSecHelper.DEFAULT_KEY_CACHE_MAX_TTL);
  }

  @Test
  public void testSameKeySameInstance () throws IOException
  {
    final DnsSecValidatingResolverCache aCache = new DnsSecValidatingResolverCache ();
    assertEquals (0, aCache.getSize ());

    final ValidatingResolver aUdp = aCache.getValidatingResolver (_key (false, 1));
    assertNotNull (aUdp);
    // Equal key, different instance
    assertSame (aUdp, aCache.getValidatingResolver (_key (false, 1)));
    assertEquals (1, aCache.getSize ());

    // TCP and UDP must not share a validating resolver
    final ValidatingResolver aTcp = aCache.getValidatingResolver (_key (true, 1));
    assertNotSame (aUdp, aTcp);
    assertEquals (2, aCache.getSize ());

    aCache.clear ();
    assertEquals (0, aCache.getSize ());
    assertNotSame (aUdp, aCache.getValidatingResolver (_key (false, 1)));
  }

  @Test
  public void testLeastRecentlyUsedEviction () throws IOException
  {
    final DnsSecValidatingResolverCache aCache = new DnsSecValidatingResolverCache (2);
    final ValidatingResolver a1 = aCache.getValidatingResolver (_key (false, 1));
    aCache.getValidatingResolver (_key (false, 2));
    // Access 1, so that 2 is the least recently used one
    assertSame (a1, aCache.getValidatingResolver (_key (false, 1)));
    aCache.getValidatingResolver (_key (false, 3));
    assertEquals (2, aCache.getSize ());
    // Still cached
    assertSame (a1, aCache.getValidatingResolver (_key (false, 1)));
  }

  @Test
  public void testInvalidTrustAnchorsAreNotCached ()
  {
    final DnsSecValidatingResolverCache aCache = new DnsSecValidatingResolverCache ();
    try
    {
      aCache.getValidatingResolver (new DnsSecValidatingResolverKey (null,
                                                                     1,
                                                                     null,
                                                                     false,
                                                                     "example.org. IN A 192.0.2.1",
                                                                     Duration.ofMinutes (5)));
      fail ();
    }
    catch (final IOException ex)
    {
      // expected
    }
    assertEquals (0, aCache.getSize ());
  }

  @Test
  public void testDefaultInstance ()
  {
    final IDnsSecValidatingResolverCache aOld = DnsSecValidatingResolverCache.getDefaultInstance ();
    assertNotNull (aOld);
    try
    {
      final DnsSecValidatingResolverCache aNew = new DnsSecValidatingResolverCache (4);
      DnsSecValidatingResolverCache.setDefaultInstance (aNew);
      assertSame (aNew, DnsSecValidatingResolverCache.getDefaultInstance ());
    }
    finally
    {
      DnsSecValidatingResolverCache.setDefaultInstance (aOld);
    }
  }
}
