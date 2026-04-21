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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.junit.Test;

import com.helger.http.tls.ETLSConfigurationMode_2020_02;

/**
 * Test class for class {@link HttpClientSettings}.
 *
 * @author Philip Helger
 */
public final class HttpClientSettingsTest
{
  @Test
  public void testClone ()
  {
    final HttpClientSettings x = new HttpClientSettings ();
    assertNotNull (x.getClone ());

    x.setTLSConfigurationMode (ETLSConfigurationMode_2020_02.MODERN);
    assertSame (ETLSConfigurationMode_2020_02.MODERN, x.getClone ().getTLSConfigurationMode ());

    x.setUserAgent ("bla");
    assertEquals ("bla", x.getClone ().getUserAgent ());
  }

  @Test
  public void testSetSSLContextTrustAllCreatesInsecureContext () throws GeneralSecurityException
  {
    // Demonstrates that setSSLContextTrustAll() creates a fully permissive context
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.setSSLContextTrustAll ();

    final SSLContext aCtx = aSettings.getSSLContext ();
    assertNotNull ("SSLContext should be set after setSSLContextTrustAll()", aCtx);
  }

  @Test
  public void testDefaultSettingsDoNotBypassTLS ()
  {
    // Verify that default settings are secure
    final HttpClientSettings aSettings = new HttpClientSettings ();
    assertNull ("Default SSLContext should be null (use JVM default)", aSettings.getSSLContext ());
    assertNull ("Default HostnameVerifier should be null (use JVM default)", aSettings.getHostnameVerifier ());
  }
}
