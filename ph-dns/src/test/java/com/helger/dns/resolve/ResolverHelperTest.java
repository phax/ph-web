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
package com.helger.dns.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.Test;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Resolver;

import com.helger.dns.config.DNSConfig;

/**
 * Test class for class {@link ResolverHelper}.
 *
 * @author Philip Helger
 */
public final class ResolverHelperTest
{
  @Test
  public void testGetSingleResolverTimeout ()
  {
    assertEquals (Duration.ofSeconds (5), ResolverHelper.getSingleResolverTimeout (Duration.ofSeconds (10)));
    assertEquals (Duration.ofMillis (500), ResolverHelper.getSingleResolverTimeout (Duration.ofSeconds (1)));
    assertEquals (Duration.ZERO, ResolverHelper.getSingleResolverTimeout (Duration.ZERO));
  }

  @Test
  public void testDefaultTimeoutIsPropagated ()
  {
    final ExtendedResolver aResolver = ResolverHelper.createExtendedResolver (null);
    assertNotNull (aResolver);
    assertEquals (DNSConfig.getResolverTimeout (), aResolver.getTimeout ());

    // The contained resolvers must have a smaller timeout than the
    // ExtendedResolver, because otherwise the overall timeout is already
    // reached when the first contained resolver times out
    final Duration aSingleTimeout = ResolverHelper.getSingleResolverTimeout (DNSConfig.getResolverTimeout ());
    for (final Resolver aSingleResolver : aResolver.getResolvers ())
    {
      assertEquals (aSingleTimeout, aSingleResolver.getTimeout ());
      assertTrue (aSingleResolver.getTimeout ().compareTo (aResolver.getTimeout ()) < 0);
    }
  }

  @Test
  public void testSetTimeout ()
  {
    final ExtendedResolver aResolver = ResolverHelper.createExtendedResolver (null);
    assertNotNull (aResolver);

    ResolverHelper.setTimeout (aResolver, Duration.ofSeconds (20));
    assertEquals (Duration.ofSeconds (20), aResolver.getTimeout ());
    for (final Resolver aSingleResolver : aResolver.getResolvers ())
      assertEquals (Duration.ofSeconds (10), aSingleResolver.getTimeout ());
  }
}
