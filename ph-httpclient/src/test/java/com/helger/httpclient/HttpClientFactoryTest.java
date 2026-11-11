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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.jspecify.annotations.NonNull;
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

  @Test
  public void testConnectionPoolSettings () throws IOException
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    // Default of Apache HttpClient is kept
    assertNull (new HttpClientFactory (aSettings).createConnectionConfig ().getValidateAfterInactivity ());

    aSettings.setValidateAfterInactivity (Duration.ofSeconds (3))
             .setEvictIdleConnectionsAfter (Duration.ofMinutes (1))
             .setEvictExpiredConnections (true);
    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final ConnectionConfig aConnectionConfig = aFactory.createConnectionConfig ();
    assertEquals (TimeValue.ofSeconds (3), aConnectionConfig.getValidateAfterInactivity ());

    // The eviction thread is started and stopped with the client
    try (final CloseableHttpClient aClient = aFactory.createHttpClient ())
    {
      assertNotNull (aClient);
    }
  }

  private static int _countRequestsFor503 (@NonNull final HttpClientSettings aSettings) throws IOException
  {
    final AtomicInteger aRequestCount = new AtomicInteger (0);
    final HttpServer aServer = ServerBootstrap.bootstrap ()
                                              .setLocalAddress (InetAddress.getLoopbackAddress ())
                                              .setListenerPort (0)
                                              .setCanonicalHostName ("127.0.0.1")
                                              .register ("*", (aRequest, aResponse, aContext) -> {
                                                aRequestCount.incrementAndGet ();
                                                aResponse.setCode (503);
                                                aResponse.addHeader (HttpHeaders.RETRY_AFTER, "0");
                                              })
                                              .create ();
    aServer.start ();
    try (final CloseableHttpClient aClient = new HttpClientFactory (aSettings).createHttpClient ())
    {
      final int nStatusCode = aClient.execute (new HttpGet ("http://127.0.0.1:" + aServer.getLocalPort () + '/'),
                                               aResponse -> {
                                                               EntityUtils.consume (aResponse.getEntity ());
                                                               return Integer.valueOf (aResponse.getCode ());
                                                             }).intValue ();
      assertEquals (503, nStatusCode);
      return aRequestCount.get ();
    }
    finally
    {
      aServer.close (CloseMode.IMMEDIATE);
    }
  }

  @Test
  public void testDisableAutomaticRetries () throws IOException
  {
    // By default Apache HttpClient retries once, even if no retries are configured
    assertEquals (2, _countRequestsFor503 (new HttpClientSettings ()));

    assertEquals (1, _countRequestsFor503 (new HttpClientSettings ().setDisableAutomaticRetries (true)));
  }
}
