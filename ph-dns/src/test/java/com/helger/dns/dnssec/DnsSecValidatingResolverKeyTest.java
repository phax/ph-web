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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.time.Duration;

import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.dns.config.DNSConfig;

/**
 * Test class for class {@link DnsSecValidatingResolverKey}.
 *
 * @author Philip Helger
 */
public final class DnsSecValidatingResolverKeyTest
{
  private static DnsSecValidatingResolverKey _key (final InetAddress... aServers)
  {
    return new DnsSecValidatingResolverKey (new CommonsArrayList <> (aServers),
                                            2,
                                            Duration.ofSeconds (5),
                                            false,
                                            DnsSecHelper.DEFAULT_ROOT_TRUST_ANCHORS,
                                            DnsSecHelper.DEFAULT_KEY_CACHE_MAX_TTL);
  }

  @Test
  public void testEqualsHashCode ()
  {
    final DnsSecValidatingResolverKey a = _key (DNSConfig.DNS_GOOGLE_1, DNSConfig.DNS_CLOUDFLARE_1);
    final DnsSecValidatingResolverKey b = _key (DNSConfig.DNS_GOOGLE_1, DNSConfig.DNS_CLOUDFLARE_1);
    assertEquals (a, b);
    assertEquals (a.hashCode (), b.hashCode ());
    assertNotNull (a.toString ());

    // The order of the DNS servers matters
    assertFalse (a.equals (_key (DNSConfig.DNS_CLOUDFLARE_1, DNSConfig.DNS_GOOGLE_1)));
    assertFalse (a.equals (new DnsSecValidatingResolverKey (a.getAllCustomDnsServers (),
                                                            a.getMaxRetries (),
                                                            a.getTimeout (),
                                                            true,
                                                            a.getTrustAnchors (),
                                                            a.getKeyCacheMaxTtl ())));
    assertFalse (a.equals (new DnsSecValidatingResolverKey (a.getAllCustomDnsServers (),
                                                            a.getMaxRetries (),
                                                            a.getTimeout (),
                                                            a.isTcp (),
                                                            a.getTrustAnchors (),
                                                            Duration.ofMinutes (5))));
  }
}
