/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import javax.annotation.Nonnull;

import org.apache.hc.core5.util.Timeout;
import org.junit.Test;

import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.ws.HostnameVerifierVerifyAll;
import com.helger.config.fallback.ConfigWithFallback;
import com.helger.config.fallback.IConfigWithFallback;
import com.helger.config.source.appl.ConfigurationSourceFunction;

/**
 * Test class for {@link HttpClientSettingsConfig}.
 *
 * @author Philip Helger
 */
public final class HttpClientSettingsConfigTest
{
  @Nonnull
  private static IConfigWithFallback _createConfig ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("test10.http.dnsclientcache.use", "true");
    aMap.put ("test10.http.proxy.enabled", "true");
    aMap.put ("test10.http.proxy.host", "1.2.3.4");
    aMap.put ("test10.http.proxy.port", "8080");
    aMap.put ("test10.http.proxy.username", "user");
    aMap.put ("test10.http.proxy.password", "pw");
    aMap.put ("test10.http.proxy.nonProxyHosts", "5.5.5.5|6.6.6.6");
    aMap.put ("test10.http.retry.count", "3");
    aMap.put ("test10.http.retry.interval.millis", "5432");
    aMap.put ("test10.http.retry.always", "true");
    aMap.put ("test10.http.timeout.connectionrequest.millis", "4321");
    aMap.put ("test10.http.timeout.connect.seconds", "21");
    aMap.put ("test10.http.timeout.response.minutes", "34");
    aMap.put ("test10.http.useragent", "egal");
    aMap.put ("test10.http.follow-redirects", "true");
    aMap.put ("test10.http.keep-alive", "true");
    aMap.put ("test10.http.tls.hostname-check.disabled", "true");
    aMap.put ("test10.http.tls.certificate-check.disabled", "true");

    aMap.put ("test9.http.proxy.enabled", "false");
    aMap.put ("test9.http.retry.count", "4");
    aMap.put ("test9.http.retry.interval.hours", "2");
    aMap.put ("test9.http.useragent", "super");
    aMap.put ("test9.http.tls.checks.disabled", "false");
    aMap.put ("test9.http.tls.hostname-check.disabled", "false");
    aMap.put ("test9.http.tls.certificate-check.disabled", "false");

    return new ConfigWithFallback (new ConfigurationSourceFunction (aMap::get));
  }

  @Test
  public void testSimple ()
  {
    final HttpClientSettings aHCS = new HttpClientSettings ();
    HttpClientSettingsConfig.assignConfigValues (aHCS, _createConfig (), "test10");
    assertTrue (aHCS.isUseDNSClientCache ());

    assertNotNull (aHCS.getProxyHost ());
    assertNotNull (aHCS.getProxyCredentials ());
    assertEquals (new CommonsLinkedHashSet <> ("5.5.5.5", "6.6.6.6"), aHCS.nonProxyHosts ());

    assertEquals (3, aHCS.getRetryCount ());
    assertEquals (Duration.ofMillis (5432), aHCS.getRetryInterval ());
    assertTrue (aHCS.isRetryAlways ());

    assertEquals (Timeout.ofMilliseconds (4321), aHCS.getConnectionRequestTimeout ());
    assertEquals (Timeout.ofSeconds (21), aHCS.getConnectTimeout ());
    assertEquals (Timeout.ofMinutes (34), aHCS.getResponseTimeout ());

    assertEquals ("egal", aHCS.getUserAgent ());
    assertTrue (aHCS.isFollowRedirects ());
    assertTrue (aHCS.isUseKeepAlive ());
    assertNotNull (aHCS.getHostnameVerifier ());
    assertTrue (aHCS.getHostnameVerifier () instanceof HostnameVerifierVerifyAll);
    assertNotNull (aHCS.getSSLContext ());
  }

  @Test
  public void testWithDifferentPrefixes ()
  {
    final HttpClientSettings aHCS = new HttpClientSettings ();
    HttpClientSettingsConfig.assignConfigValues (aHCS, _createConfig (), "test9", "test10");
    assertTrue (aHCS.isUseDNSClientCache ());

    // Difference
    assertNull (aHCS.getProxyHost ());
    // Difference (because proxy is disabled)
    assertNull (aHCS.getProxyCredentials ());
    // Difference (because proxy is disabled)
    assertEquals (new CommonsLinkedHashSet <> (), aHCS.nonProxyHosts ());

    // Difference
    assertEquals (4, aHCS.getRetryCount ());
    // Difference
    assertEquals (Duration.ofHours (2), aHCS.getRetryInterval ());
    assertTrue (aHCS.isRetryAlways ());

    assertEquals (Timeout.ofMilliseconds (4321), aHCS.getConnectionRequestTimeout ());
    assertEquals (Timeout.ofSeconds (21), aHCS.getConnectTimeout ());
    assertEquals (Timeout.ofMinutes (34), aHCS.getResponseTimeout ());

    // Difference
    assertEquals ("super", aHCS.getUserAgent ());
    assertTrue (aHCS.isFollowRedirects ());
    assertTrue (aHCS.isUseKeepAlive ());
    // Difference
    assertNull (aHCS.getHostnameVerifier ());
    // Difference
    assertNull (aHCS.getSSLContext ());
  }
}
