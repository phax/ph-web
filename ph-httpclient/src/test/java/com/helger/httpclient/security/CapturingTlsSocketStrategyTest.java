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
package com.helger.httpclient.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.collection.commons.ICommonsList;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerString;

/**
 * Test class for class {@link CapturingTlsSocketStrategy}.
 *
 * @author Philip Helger
 */
public final class CapturingTlsSocketStrategyTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CapturingTlsSocketStrategyTest.class);

  @Test
  public void testInstantiation ()
  {
    final CapturingTlsSocketStrategy aStrategy = new CapturingTlsSocketStrategy (DefaultClientTlsStrategy.createDefault ());
    assertNotNull (aStrategy);
  }

  @Test
  public void testGetRemoteTLSCertificatesWithNullContext ()
  {
    assertNull (CapturingTlsSocketStrategy.getRemoteTLSCertificates (null));
  }

  @Test
  public void testGetRemoteTLSCertificatesWithEmptyContext ()
  {
    assertNull (CapturingTlsSocketStrategy.getRemoteTLSCertificates (HttpCoreContext.create ()));
  }

  @Test
  public void testCaptureRemoteTLSCerts () throws IOException
  {
    try (final HttpClientManager aHCMgr = new HttpClientManager ())
    {
      final HttpClientContext aCtx = HttpClientContext.create ();
      aHCMgr.execute (new HttpGet ("https://www.google.com"), aCtx, new ResponseHandlerString (ContentType.TEXT_HTML));

      final ICommonsList <X509Certificate> aCerts = CapturingTlsSocketStrategy.getRemoteTLSCertificates (aCtx);
      assertNotNull ("Remote TLS certificates should have been captured", aCerts);
      assertTrue ("At least one certificate expected", aCerts.isNotEmpty ());

      LOGGER.info ("Captured " + aCerts.size () + " remote TLS certificate(s):");
      int nIdx = 0;
      for (final X509Certificate aCert : aCerts)
        LOGGER.info ("  [" +
                     (++nIdx) +
                     "] Subject: " +
                     aCert.getSubjectX500Principal ().getName () +
                     " (issued by " +
                     aCert.getIssuerX500Principal ().getName () +
                     ")");
    }
  }
}
