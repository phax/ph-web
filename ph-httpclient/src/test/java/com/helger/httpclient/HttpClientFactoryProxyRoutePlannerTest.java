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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.jspecify.annotations.NonNull;
import org.junit.Test;

/**
 * Comprehensive test class for {@link HttpClientFactory} focusing on proxy route planner
 * functionality. This test suite validates the following key aspects:
 * <ol>
 * <li>Protocol-specific proxy selection (HTTP vs HTTPS vs general)
 * <li>
 * <li>Non-proxy host lists and their protocol-specific application</li>
 * <li>Fallback behavior when protocol-specific proxies are not configured</li>
 * <li>Credentials handling and provider creation</li>
 * <li>Edge cases like null configurations and mixed scenarios</li>
 * <li>Scheme case-insensitivity in proxy selection</li>
 * <li>Complex multi-proxy configurations with overlapping aSettings</li>
 * </ol>
 * The tests use reflection to access the protected determineProxy() method in the
 * DefaultRoutePlanner to verify actual proxy selection logic.
 *
 * @author Philip Helger
 */
public final class HttpClientFactoryProxyRoutePlannerTest
{
  private static final HttpHost PROXY_GENERAL = new HttpHost ("general-proxy.example.com", 8080);
  private static final HttpHost PROXY_HTTP = new HttpHost ("http-proxy.example.com", 8081);
  private static final HttpHost PROXY_HTTPS = new HttpHost ("https-proxy.example.com", 8082);

  private static final HttpHost TARGET_HTTP = new HttpHost ("http", "example.com", 80);
  private static final HttpHost TARGET_HTTPS = new HttpHost ("https", "example.com", 443);
  private static final HttpHost TARGET_FTP = new HttpHost ("ftp", "example.com", 21);

  /**
   * Helper method to extract the proxy determination from a route planner. Uses reflection to call
   * the protected determineProxy method.
   */
  private static HttpHost _getProxyForTarget (@NonNull final HttpRoutePlanner aPlanner, @NonNull final HttpHost aTarget)
                                                                                                                         throws Exception
  {
    if (aPlanner instanceof DefaultProxyRoutePlanner)
    {
      // For DefaultProxyRoutePlanner, all requests go through the same proxy
      final Method aMethod = DefaultProxyRoutePlanner.class.getDeclaredMethod ("determineProxy",
                                                                               HttpHost.class,
                                                                               HttpContext.class);
      aMethod.setAccessible (true);
      return (HttpHost) aMethod.invoke (aPlanner, aTarget, HttpCoreContext.create ());
    }
    else
      if (aPlanner instanceof DefaultRoutePlanner)
      {
        // For DefaultRoutePlanner, use the determineProxy method
        final Method aMethod = DefaultRoutePlanner.class.getDeclaredMethod ("determineProxy",
                                                                            HttpHost.class,
                                                                            HttpContext.class);
        aMethod.setAccessible (true);
        return (HttpHost) aMethod.invoke (aPlanner, aTarget, HttpCoreContext.create ());
      }
      else
      {
        throw new UnsupportedOperationException ("Unsupported route planner type: " + aPlanner.getClass ());
      }
  }

  @Test
  public void testNoProxySettings ()
  {
    // Test with no proxy aSettings at all
    final HttpClientSettings aSettings = new HttpClientSettings ();
    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);

    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);
    assertNull ("No proxy settings should result in null route planner", aPlanner);
  }

  @Test
  public void testSimpleGeneralProxy () throws Exception
  {
    // Test simple case: only general proxy, no non-proxy hosts
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull ("General proxy should result in route planner", aPlanner);
    assertTrue ("Should be DefaultProxyRoutePlanner for simple case", aPlanner instanceof DefaultProxyRoutePlanner);

    // All should go through the general proxy
    assertSame (PROXY_GENERAL, _getProxyForTarget (aPlanner, TARGET_HTTP));
    assertSame (PROXY_GENERAL, _getProxyForTarget (aPlanner, TARGET_HTTPS));
    assertSame (PROXY_GENERAL, _getProxyForTarget (aPlanner, TARGET_FTP));
  }

  @Test
  public void testGeneralProxyWithNonProxyHosts () throws Exception
  {
    // Test general proxy with non-proxy hosts
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().nonProxyHosts ().add ("localhost");
    aSettings.getGeneralProxy ().nonProxyHosts ().add ("internal.example.com");

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull ("General proxy with non-proxy hosts should result in route planner", aPlanner);
    assertTrue ("Should be DefaultRoutePlanner for non-proxy hosts case", aPlanner instanceof DefaultRoutePlanner);

    // Test proxy routes
    final HttpHost eExternalTarget = new HttpHost ("http", "external.example.com", 80);
    assertSame (PROXY_GENERAL, _getProxyForTarget (aPlanner, eExternalTarget));

    // Test non-proxy routes
    final HttpHost aLocalhostTarget = new HttpHost ("http", "localhost", 80);
    assertNull ("localhost should bypass proxy", _getProxyForTarget (aPlanner, aLocalhostTarget));

    final HttpHost aInternalTarget = new HttpHost ("https", "internal.example.com", 443);
    assertNull ("internal.example.com should bypass proxy", _getProxyForTarget (aPlanner, aInternalTarget));
  }

  @Test
  public void testProtocolSpecificProxies () throws Exception
  {
    // Test protocol-specific proxy aSettings
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getHttpProxy ().setProxyHost (PROXY_HTTP);
    aSettings.getHttpsProxy ().setProxyHost (PROXY_HTTPS);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull ("Protocol-specific proxies should result in route planner", aPlanner);
    assertTrue ("Should be DefaultRoutePlanner for protocol-specific case", aPlanner instanceof DefaultRoutePlanner);

    // Protocol-specific proxies should be used correctly
    assertSame ("HTTP target should use HTTP-specific proxy", PROXY_HTTP, _getProxyForTarget (aPlanner, TARGET_HTTP));
    assertSame ("HTTPS target should use HTTPS-specific proxy",
                PROXY_HTTPS,
                _getProxyForTarget (aPlanner, TARGET_HTTPS));
    assertSame ("FTP target should use general proxy", PROXY_GENERAL, _getProxyForTarget (aPlanner, TARGET_FTP));
  }

  @Test
  public void testHttpProxyOnly () throws Exception
  {
    // Test only HTTP-specific proxy (no general proxy)
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.getHttpProxy ().setProxyHost (PROXY_HTTP);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull ("HTTP-only proxy should result in route planner", aPlanner);
    assertTrue ("Should be DefaultRoutePlanner", aPlanner instanceof DefaultRoutePlanner);

    // HTTP target should use HTTP proxy, HTTPS has no proxy (no general proxy)
    assertSame ("HTTP target should use HTTP proxy", PROXY_HTTP, _getProxyForTarget (aPlanner, TARGET_HTTP));
    assertNull ("HTTPS target should have no proxy (no general proxy available)",
                _getProxyForTarget (aPlanner, TARGET_HTTPS));
  }

  @Test
  public void testHttpsProxyOnly () throws Exception
  {
    // Test only HTTPS-specific proxy (no general proxy)
    final HttpClientSettings aSettings = new HttpClientSettings ();
    aSettings.getHttpsProxy ().setProxyHost (PROXY_HTTPS);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull ("HTTPS-only proxy should result in route planner", aPlanner);
    assertTrue ("Should be DefaultRoutePlanner", aPlanner instanceof DefaultRoutePlanner);

    // HTTP has no proxy (no general proxy), HTTPS should use HTTPS proxy
    assertNull ("HTTP target should have no proxy (no general proxy available)",
                _getProxyForTarget (aPlanner, TARGET_HTTP));
    assertSame ("HTTPS target should use HTTPS proxy", PROXY_HTTPS, _getProxyForTarget (aPlanner, TARGET_HTTPS));
  }

  @Test
  public void testMixedProxyAndNonProxyHosts () throws Exception
  {
    // Test complex scenario with protocol-specific proxies and non-proxy hosts
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().nonProxyHosts ().add ("general-bypass.example.com");

    aSettings.getHttpProxy ().setProxyHost (PROXY_HTTP);
    aSettings.getHttpProxy ().nonProxyHosts ().add ("http-bypass.example.com");

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull (aPlanner);
    assertTrue (aPlanner instanceof DefaultRoutePlanner);

    // Test normal routing (should use HTTP-specific proxy for HTTP targets)
    final HttpHost normalTarget = new HttpHost ("http", "normal.example.com", 80);
    assertSame ("Normal HTTP target should use HTTP proxy", PROXY_HTTP, _getProxyForTarget (aPlanner, normalTarget));

    // Test non-proxy host bypass - HTTP target will use HTTP-specific non-proxy list
    final HttpHost generalBypassTarget = new HttpHost ("http", "general-bypass.example.com", 80);
    assertSame ("General bypass host for HTTP should still use HTTP proxy (not in HTTP non-proxy list)",
                PROXY_HTTP,
                _getProxyForTarget (aPlanner, generalBypassTarget));

    final HttpHost httpBypassTarget = new HttpHost ("http", "http-bypass.example.com", 80);
    assertNull ("HTTP bypass host should not use proxy", _getProxyForTarget (aPlanner, httpBypassTarget));
  }

  @Test
  public void testProxyCredentials () throws Exception
  {
    // Test that proxy credentials are handled correctly
    final HttpClientSettings aSettings = new HttpClientSettings ();
    final UsernamePasswordCredentials aCredentials = new UsernamePasswordCredentials ("testuser",
                                                                                      "testpass".toCharArray ());

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().setProxyCredentials (aCredentials);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);

    // Test that credentials provider is created
    assertNotNull ("Credentials provider should be created", aFactory.createCredentialsProvider ());

    // Test that route aPlanner is created
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);
    assertNotNull ("Route planner should be created", aPlanner);
  }

  @Test
  public void testPipeStringNonProxyHosts () throws Exception
  {
    // Test non-proxy hosts set via pipe string
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().setNonProxyHostsFromPipeString ("localhost|127.0.0.1|*.internal.com");

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull (aPlanner);
    assertTrue (aPlanner instanceof DefaultRoutePlanner);

    // Test that hosts in the pipe string bypass proxy
    final HttpHost aLocalhostTarget = new HttpHost ("http", "localhost", 80);
    assertNull ("localhost should bypass proxy", _getProxyForTarget (aPlanner, aLocalhostTarget));

    final HttpHost aIpTarget = new HttpHost ("http", "127.0.0.1", 80);
    assertNull ("127.0.0.1 should bypass proxy", _getProxyForTarget (aPlanner, aIpTarget));

    // Note: Wildcard matching would depend on implementation details
    // For now, test exact matches only
  }

  @Test
  public void testEmptyNonProxyHostsList () throws Exception
  {
    // Test that empty non-proxy hosts list works correctly
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().setNonProxyHostsFromPipeString ("");

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    // Should use simple DefaultProxyRoutePlanner since non-proxy hosts is empty
    assertNotNull (aPlanner);
    assertTrue ("Should be DefaultProxyRoutePlanner for empty non-proxy hosts",
                aPlanner instanceof DefaultProxyRoutePlanner);
  }

  @Test
  public void testNullNonProxyHostsList () throws Exception
  {
    // Test behavior with null non-proxy hosts
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().setNonProxyHostsFromPipeString (null);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    // Should use simple DefaultProxyRoutePlanner since non-proxy hosts is empty
    assertNotNull (aPlanner);
    assertTrue ("Should be DefaultProxyRoutePlanner for null non-proxy hosts",
                aPlanner instanceof DefaultProxyRoutePlanner);
  }

  @Test
  public void testComplexScenarioWithAllProxyTypes () throws Exception
  {
    // Test complex scenario with all three proxy types and mixed non-proxy hosts
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getGeneralProxy ().nonProxyHosts ().add ("general-bypass.example.com");

    aSettings.getHttpProxy ().setProxyHost (PROXY_HTTP);
    aSettings.getHttpProxy ().nonProxyHosts ().add ("http-bypass.example.com");

    aSettings.getHttpsProxy ().setProxyHost (PROXY_HTTPS);
    aSettings.getHttpsProxy ().nonProxyHosts ().add ("https-bypass.example.com");

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNotNull (aPlanner);
    assertTrue (aPlanner instanceof DefaultRoutePlanner);

    // Test normal routing - each protocol should use its specific proxy
    assertSame ("HTTP should use HTTP proxy",
                PROXY_HTTP,
                _getProxyForTarget (aPlanner, new HttpHost ("http", "example.com", 80)));
    assertSame ("HTTPS should use HTTPS proxy",
                PROXY_HTTPS,
                _getProxyForTarget (aPlanner, new HttpHost ("https", "example.com", 443)));
    assertSame ("FTP should use general proxy",
                PROXY_GENERAL,
                _getProxyForTarget (aPlanner, new HttpHost ("ftp", "example.com", 21)));

    // Test non-proxy host bypassing - each protocol checks its effective non-proxy list
    assertNull ("HTTP bypass should work",
                _getProxyForTarget (aPlanner, new HttpHost ("http", "http-bypass.example.com", 80)));
    assertNull ("HTTPS bypass should work",
                _getProxyForTarget (aPlanner, new HttpHost ("https", "https-bypass.example.com", 443)));
    assertNull ("General bypass for FTP should work",
                _getProxyForTarget (aPlanner, new HttpHost ("ftp", "general-bypass.example.com", 21)));

    // Test cross-protocol non-proxy host behavior
    assertSame ("General bypass host should not affect HTTP (uses HTTP-specific list)",
                PROXY_HTTP,
                _getProxyForTarget (aPlanner, new HttpHost ("http", "general-bypass.example.com", 80)));
    assertSame ("HTTP bypass host should not affect HTTPS",
                PROXY_HTTPS,
                _getProxyForTarget (aPlanner, new HttpHost ("https", "http-bypass.example.com", 443)));
  }

  @Test
  public void testSchemeDetection () throws Exception
  {
    // Test that scheme detection works correctly for various protocols
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getHttpProxy ().setProxyHost (PROXY_HTTP);
    aSettings.getHttpsProxy ().setProxyHost (PROXY_HTTPS);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    // Test exact scheme matching
    assertSame ("HTTP scheme should use HTTP proxy",
                PROXY_HTTP,
                _getProxyForTarget (aPlanner, new HttpHost ("http", "example.com", 80)));
    assertSame ("HTTPS scheme should use HTTPS proxy",
                PROXY_HTTPS,
                _getProxyForTarget (aPlanner, new HttpHost ("https", "example.com", 443)));

    // Test other schemes fall back to general proxy
    assertSame ("FTP should use general proxy",
                PROXY_GENERAL,
                _getProxyForTarget (aPlanner, new HttpHost ("ftp", "example.com", 21)));
    assertSame ("SMTP should use general proxy",
                PROXY_GENERAL,
                _getProxyForTarget (aPlanner, new HttpHost ("smtp", "example.com", 25)));
    assertSame ("Custom scheme should use general proxy",
                PROXY_GENERAL,
                _getProxyForTarget (aPlanner, new HttpHost ("custom", "example.com", 1234)));
  }

  @Test
  public void testCaseInsensitiveSchemeHandling () throws Exception
  {
    // Test that scheme matching is case-insensitive
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    aSettings.getHttpProxy ().setProxyHost (PROXY_HTTP);
    aSettings.getHttpsProxy ().setProxyHost (PROXY_HTTPS);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    // Test that uppercase schemes match HTTP-specific aSettings (case-insensitive)
    assertSame ("Uppercase HTTP should use HTTP proxy",
                PROXY_HTTP,
                _getProxyForTarget (aPlanner, new HttpHost ("HTTP", "example.com", 80)));
    assertSame ("Mixed case Http should use HTTP proxy",
                PROXY_HTTP,
                _getProxyForTarget (aPlanner, new HttpHost ("Http", "example.com", 80)));
    assertSame ("Uppercase HTTPS should use HTTPS proxy",
                PROXY_HTTPS,
                _getProxyForTarget (aPlanner, new HttpHost ("HTTPS", "example.com", 443)));
    assertSame ("Mixed case Https should use HTTPS proxy",
                PROXY_HTTPS,
                _getProxyForTarget (aPlanner, new HttpHost ("Https", "example.com", 443)));
  }

  @Test
  public void testNoProxyEffectiveSettings () throws Exception
  {
    // Test edge case where proxy host is set but effectively null
    final HttpClientSettings aSettings = new HttpClientSettings ();

    // Set proxy host but then set it to null
    aSettings.getGeneralProxy ().setProxyHost (new HttpHost ("temp.example.com", 8080));
    aSettings.getGeneralProxy ().setProxyHost (null);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);

    assertNull ("No effective proxy host should result in null route planner", aPlanner);
  }

  @Test
  public void testProxyWithoutCredentials () throws Exception
  {
    // Test proxy configuration without credentials
    final HttpClientSettings aSettings = new HttpClientSettings ();

    aSettings.getGeneralProxy ().setProxyHost (PROXY_GENERAL);
    // Explicitly ensure no credentials
    aSettings.getGeneralProxy ().setProxyCredentials (null);

    final HttpClientFactory aFactory = new HttpClientFactory (aSettings);

    // Credentials provider should be null when no credentials are set
    assertNull ("No credentials should result in null credentials provider", aFactory.createCredentialsProvider ());

    // Route aPlanner should still be created
    final HttpRoutePlanner aPlanner = aFactory.createRoutePlanner (DefaultSchemePortResolver.INSTANCE);
    assertNotNull ("Route planner should be created even without credentials", aPlanner);
  }
}
