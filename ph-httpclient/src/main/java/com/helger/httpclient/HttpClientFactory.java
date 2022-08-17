/*
 * Copyright (C) 2016-2022 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.RequestAddCookies;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLInitializationException;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.http.tls.ITLSConfigurationMode;

/**
 * A factory for creating {@link CloseableHttpClient} that is e.g. to be used in
 * the {@link HttpClientManager}.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class HttpClientFactory implements IHttpClientProvider
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HttpClientFactory.class);

  private final HttpClientSettings m_aSettings;

  /**
   * Default constructor.
   */
  public HttpClientFactory ()
  {
    this (new HttpClientSettings ());
  }

  /**
   * Constructor with explicit settings.
   *
   * @param aSettings
   *        The settings to be used. May not be <code>null</code>.
   */
  public HttpClientFactory (@Nonnull final HttpClientSettings aSettings)
  {
    ValueEnforcer.notNull (aSettings, "Settings");
    m_aSettings = aSettings;
  }

  /**
   * @return The underlying HTTP client settings. Never <code>null</code>.
   *         Changes to the returned object impact this HTTP client factory.
   * @since 9.6.4
   */
  @Nonnull
  public final HttpClientSettings httpClientSettings ()
  {
    return m_aSettings;
  }

  /**
   * Create the scheme to port resolver.
   *
   * @return Never <code>null</code>.
   * @since 9.1.1
   */
  @Nonnull
  public SchemePortResolver createSchemePortResolver ()
  {
    return DefaultSchemePortResolver.INSTANCE;
  }

  @Nullable
  public LayeredConnectionSocketFactory createSSLFactory ()
  {
    LayeredConnectionSocketFactory aSSLFactory = null;

    try
    {
      // First try with a custom SSL context
      final SSLContext aSSLContext = m_aSettings.getSSLContext ();
      if (aSSLContext != null)
      {
        // Choose correct TLS configuration mode
        ITLSConfigurationMode aTLSConfigMode = m_aSettings.getTLSConfigurationMode ();
        if (aTLSConfigMode == null)
          aTLSConfigMode = HttpClientSettings.DEFAULT_TLS_CONFIG_MODE;

        // Custom hostname verifier preferred
        HostnameVerifier aHostnameVerifier = m_aSettings.getHostnameVerifier ();
        if (aHostnameVerifier == null)
          aHostnameVerifier = HttpsSupport.getDefaultHostnameVerifier ();

        if (LOGGER.isDebugEnabled ())
        {
          LOGGER.debug ("Using the following TLS versions: " + aTLSConfigMode.getAllTLSVersionIDs ());
          LOGGER.debug ("Using the following TLS cipher suites: " + aTLSConfigMode.getAllCipherSuites ());
          LOGGER.debug ("Using the following hostname verifier: " + aHostnameVerifier);
        }

        aSSLFactory = new SSLConnectionSocketFactory (aSSLContext,
                                                      aTLSConfigMode.getAllTLSVersionIDsAsArray (),
                                                      aTLSConfigMode.getAllCipherSuitesAsArray (),
                                                      aHostnameVerifier);
      }
    }
    catch (final SSLInitializationException ex)
    {
      // Fall through
      LOGGER.warn ("Failed to init custom SSLConnectionSocketFactory - falling back to default SSLConnectionSocketFactory",
                   ex);
    }

    if (aSSLFactory == null)
    {
      // No custom SSL context present - use system defaults
      try
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Trying SSLConnectionSocketFactory.getSystemSocketFactory ()");
        aSSLFactory = SSLConnectionSocketFactory.getSystemSocketFactory ();
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Using SSL socket factory with an SSL context based on system propertiesas described in JSSE Reference Guide.");
      }
      catch (final SSLInitializationException ex)
      {
        try
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Trying SSLConnectionSocketFactory.getSocketFactory ()");
          aSSLFactory = SSLConnectionSocketFactory.getSocketFactory ();
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Using SSL socket factory with an SSL context based on the standard JSSEtrust material (cacerts file in the security properties directory).System properties are not taken into consideration.");
        }
        catch (final SSLInitializationException ex2)
        {
          // Fall through
        }
      }
    }
    return aSSLFactory;
  }

  /**
   * @return The socket configuration builder used by the
   *         {@link PoolingHttpClientConnectionManager} to create the default
   *         socket configuration.
   */
  @Nonnull
  public SocketConfig.Builder createSocketConfigBuilder ()
  {
    return SocketConfig.custom ();
  }

  /**
   * @return The default connection configuration used by the
   *         {@link PoolingHttpClientConnectionManager}.
   */
  @Nonnull
  public SocketConfig createSocketConfig ()
  {
    return createSocketConfigBuilder ().build ();
  }

  /**
   * @return The DNS resolver to be used for
   *         {@link PoolingHttpClientConnectionManager}. May be
   *         <code>null</code> to use the default.
   * @since 8.8.0
   */
  @Nullable
  public DnsResolver createDNSResolver ()
  {
    // If caching is active, use the default System resolver
    return m_aSettings.isUseDNSClientCache () ? SystemDefaultDnsResolver.INSTANCE : NonCachingDnsResolver.INSTANCE;
  }

  @Nonnull
  public HttpClientConnectionManager createConnectionManager (@Nonnull final LayeredConnectionSocketFactory aSSLFactory)
  {
    final DnsResolver aDNSResolver = createDNSResolver ();
    final PoolingHttpClientConnectionManager aConnMgr = PoolingHttpClientConnectionManagerBuilder.create ()
                                                                                                 .setSSLSocketFactory (aSSLFactory)
                                                                                                 .setDnsResolver (aDNSResolver)
                                                                                                 .build ();
    aConnMgr.setDefaultMaxPerRoute (100);
    aConnMgr.setMaxTotal (200);
    aConnMgr.setValidateAfterInactivity (TimeValue.ofSeconds (1));

    final SocketConfig aSocketConfig = createSocketConfig ();
    aConnMgr.setDefaultSocketConfig (aSocketConfig);

    return aConnMgr;
  }

  @Nullable
  public ConnectionReuseStrategy createConnectionReuseStrategy ()
  {
    if (m_aSettings.isUseKeepAlive ())
      return DefaultClientConnectionReuseStrategy.INSTANCE;

    // No connection reuse
    return (request, response, context) -> false;
  }

  @Nonnull
  public RequestConfig.Builder createRequestConfigBuilder ()
  {
    return RequestConfig.custom ()
                        .setCookieSpec (StandardCookieSpec.STRICT)
                        .setConnectionRequestTimeout (m_aSettings.getConnectionRequestTimeout ())
                        .setConnectTimeout (m_aSettings.getConnectionTimeout ())
                        .setResponseTimeout (m_aSettings.getSocketTimeout ())
                        .setCircularRedirectsAllowed (false)
                        .setRedirectsEnabled (m_aSettings.isFollowRedirects ());
  }

  @Nonnull
  public RequestConfig createRequestConfig ()
  {
    return createRequestConfigBuilder ().build ();
  }

  @Nullable
  public CredentialsProvider createCredentialsProvider ()
  {
    final HttpHost aProxyHost = m_aSettings.getProxyHost ();
    final Credentials aProxyCredentials = m_aSettings.getProxyCredentials ();
    if (aProxyHost != null && aProxyCredentials != null)
    {
      final BasicCredentialsProvider aCredentialsProvider = new BasicCredentialsProvider ();
      aCredentialsProvider.setCredentials (new AuthScope (aProxyHost), aProxyCredentials);
      return aCredentialsProvider;
    }
    return null;
  }

  @Nullable
  public HttpRequestRetryStrategy createRequestRetryStrategy (@Nonnegative final int nMaxRetries,
                                                              @Nonnull final TimeValue aRetryInterval)
  {
    return new HttpClientRetryStrategy (nMaxRetries, aRetryInterval);
  }

  @Nonnull
  public HttpClientBuilder createHttpClientBuilder ()
  {
    final LayeredConnectionSocketFactory aSSLFactory = createSSLFactory ();
    if (aSSLFactory == null)
      throw new IllegalStateException ("Failed to create SSL SocketFactory");

    final SchemePortResolver aSchemePortResolver = createSchemePortResolver ();
    final HttpClientConnectionManager aConnMgr = createConnectionManager (aSSLFactory);
    final ConnectionReuseStrategy aConnectionReuseStrategy = createConnectionReuseStrategy ();
    final RequestConfig aRequestConfig = createRequestConfig ();
    final HttpHost aProxyHost = m_aSettings.getProxyHost ();
    final CredentialsProvider aCredentialsProvider = createCredentialsProvider ();

    HttpRoutePlanner aRoutePlanner = null;
    if (aProxyHost != null)
    {
      // If a route planner is used, the HttpClientBuilder MUST NOT use the
      // proxy, because this would have precedence
      if (m_aSettings.nonProxyHosts ().isEmpty ())
      {
        // Proxy for all
        aRoutePlanner = new DefaultProxyRoutePlanner (aProxyHost, aSchemePortResolver);
      }
      else
      {
        // Proxy for all but non-proxy hosts
        // Clone set here to avoid concurrent modification
        final ICommonsSet <String> aNonProxyHosts = m_aSettings.nonProxyHosts ().getClone ();
        aRoutePlanner = new DefaultRoutePlanner (aSchemePortResolver)
        {
          @Override
          protected HttpHost determineProxy (@Nonnull final HttpHost aTarget,
                                             @Nonnull final HttpContext aContext) throws HttpException
          {
            final String sHostname = aTarget.getHostName ();
            if (aNonProxyHosts.contains (sHostname))
            {
              // Return direct route
              if (LOGGER.isInfoEnabled ())
                LOGGER.info ("Not using proxy host for route to '" + sHostname + "'");
              return null;
            }
            return aProxyHost;
          }
        };
      }
    }

    final HttpClientBuilder ret = HttpClients.custom ()
                                             .setSchemePortResolver (aSchemePortResolver)
                                             .setConnectionManager (aConnMgr)
                                             .setDefaultRequestConfig (aRequestConfig)
                                             .setDefaultCredentialsProvider (aCredentialsProvider)
                                             .setRoutePlanner (aRoutePlanner)
                                             .setConnectionReuseStrategy (aConnectionReuseStrategy);

    // Add cookies
    ret.addRequestInterceptorLast (new RequestAddCookies ());

    // Enable usage of Java networking system properties
    if (m_aSettings.isUseSystemProperties ())
      ret.useSystemProperties ();

    // Set retry handler (if needed)
    if (m_aSettings.hasRetries ())
      ret.setRetryStrategy (createRequestRetryStrategy (m_aSettings.getRetryCount (),
                                                        m_aSettings.getRetryIntervalAsTimeValue ()));

    // Set user agent (if any)
    if (m_aSettings.hasUserAgent ())
      ret.setUserAgent (m_aSettings.getUserAgent ());

    return ret;
  }

  @Nonnull
  public CloseableHttpClient createHttpClient ()
  {
    final HttpClientBuilder aBuilder = createHttpClientBuilder ();
    return aBuilder.build ();
  }
}
