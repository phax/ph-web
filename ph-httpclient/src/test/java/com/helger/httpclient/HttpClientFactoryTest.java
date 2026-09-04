/*
 * Copyright (C) 2016-2026 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.security.KeyStore;

import org.junit.Test;

import com.helger.base.state.EChange;

/**
 * Test class for class {@link HttpClientFactory}.
 *
 * @author Philip Helger
 */
public final class HttpClientFactoryTest
{
  @Test
  public void testSystemDefaultTrustStoreCaching ()
  {
    HttpClientFactory.clearSystemDefaultTrustStoreCache ();

    final KeyStore aTrustStore = HttpClientFactory.getSystemDefaultTrustStore ();
    assertNotNull (aTrustStore);

    // The second call must be served from the cache
    assertSame (aTrustStore, HttpClientFactory.getSystemDefaultTrustStore ());

    assertSame (EChange.CHANGED, HttpClientFactory.clearSystemDefaultTrustStoreCache ());
    assertSame (EChange.UNCHANGED, HttpClientFactory.clearSystemDefaultTrustStoreCache ());

    // After clearing the cache, a new object must be created
    final KeyStore aTrustStore2 = HttpClientFactory.getSystemDefaultTrustStore ();
    assertNotNull (aTrustStore2);
    assertNotSame (aTrustStore, aTrustStore2);
  }

  @Test
  public void testLoadSystemDefaultTrustStoreIsNotCached ()
  {
    final KeyStore aTrustStore = HttpClientFactory.loadSystemDefaultTrustStore ();
    assertNotNull (aTrustStore);

    // This method always reads from the underlying source
    assertNotSame (aTrustStore, HttpClientFactory.loadSystemDefaultTrustStore ());
  }
}
