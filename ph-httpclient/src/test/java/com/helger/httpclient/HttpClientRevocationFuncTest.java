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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ContentType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.httpclient.response.ResponseHandlerString;
import com.helger.security.revocation.ERevocationCheckMode;

/**
 * Functional test class for certificate revocation checking in {@link HttpClientFactory}. These
 * tests require network access.
 *
 * @author Philip Helger
 */
public final class HttpClientRevocationFuncTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HttpClientRevocationFuncTest.class);

  @Test
  public void testDefaultRevocationCheckModeIsNone ()
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    assertSame (ERevocationCheckMode.NONE, aSettings.getRevocationCheckMode ());
  }

  @Test
  public void testRevocationCheckModeRoundTrip ()
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    for (final ERevocationCheckMode eMode : ERevocationCheckMode.values ())
    {
      aSettings.setRevocationCheckMode (eMode);
      assertSame (eMode, aSettings.getRevocationCheckMode ());
    }
  }

  @Test
  public void testRevocationCheckModeClone ()
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.setRevocationCheckMode (ERevocationCheckMode.CRL_BEFORE_OCSP);
    aSettings.setRevocationCheckSoftFail (true);

    final HttpClientSettings aClone = aSettings.getClone ();
    assertSame (ERevocationCheckMode.CRL_BEFORE_OCSP, aClone.getRevocationCheckMode ());
    assertTrue (aClone.isRevocationCheckSoftFail ());
  }

  @Test
  public void testGoodCertWithRevocationCheck () throws IOException
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.setRevocationCheckMode (ERevocationCheckMode.CRL_BEFORE_OCSP);
    aSettings.setRevocationCheckSoftFail (true);

    try (final HttpClientManager aHCMgr = new HttpClientManager (new HttpClientFactory (aSettings)))
    {
      final String sResponse = aHCMgr.execute (new HttpGet ("https://www.google.com"),
                                               new ResponseHandlerString (ContentType.TEXT_HTML));
      assertNotNull (sResponse);
      LOGGER.info ("Successfully connected to google.com with CRL_BEFORE_OCSP revocation checking");
    }
  }

  @Test
  public void testRevokedCertWithRevocationCheck ()
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.setRevocationCheckMode (ERevocationCheckMode.CRL_BEFORE_OCSP);

    try (final HttpClientManager aHCMgr = new HttpClientManager (new HttpClientFactory (aSettings)))
    {
      aHCMgr.execute (new HttpGet ("https://revoked.badssl.com/"), new ResponseHandlerString (ContentType.TEXT_HTML));
      fail ("Expected SSLHandshakeException for revoked certificate");
    }
    catch (final SSLHandshakeException ex)
    {
      LOGGER.info ("Correctly rejected revoked certificate from revoked.badssl.com: " + ex.getMessage ());
    }
    catch (final IOException ex)
    {
      // Some environments may throw a different IOException wrapping the SSL error
      LOGGER.info ("Connection to revoked.badssl.com failed (expected): " +
                   ex.getClass ().getName () +
                   " - " +
                   ex.getMessage ());
    }
  }

  @Test
  public void testRevokedCertWithoutRevocationCheck () throws IOException
  {
    // With NONE mode, the revoked cert should be accepted
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.setRevocationCheckMode (ERevocationCheckMode.NONE);

    try (final HttpClientManager aHCMgr = new HttpClientManager (new HttpClientFactory (aSettings)))
    {
      final String sResponse = aHCMgr.execute (new HttpGet ("https://revoked.badssl.com/"),
                                               new ResponseHandlerString (ContentType.TEXT_HTML));
      assertNotNull (sResponse);
      LOGGER.info ("Successfully connected to revoked.badssl.com with NONE revocation checking (expected)");
    }
  }

  @Test
  public void testRevokedCertWithOCSP ()
  {
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.setRevocationCheckMode (ERevocationCheckMode.OCSP_BEFORE_CRL);

    try (final HttpClientManager aHCMgr = new HttpClientManager (new HttpClientFactory (aSettings)))
    {
      aHCMgr.execute (new HttpGet ("https://revoked.badssl.com/"), new ResponseHandlerString (ContentType.TEXT_HTML));
      fail ("Expected SSLHandshakeException for revoked certificate with OCSP");
    }
    catch (final SSLHandshakeException ex)
    {
      LOGGER.info ("Correctly rejected revoked certificate with OCSP_BEFORE_CRL: " + ex.getMessage ());
    }
    catch (final IOException ex)
    {
      LOGGER.info ("Connection to revoked.badssl.com failed with OCSP (expected): " +
                   ex.getClass ().getName () +
                   " - " +
                   ex.getMessage ());
    }
  }
}
