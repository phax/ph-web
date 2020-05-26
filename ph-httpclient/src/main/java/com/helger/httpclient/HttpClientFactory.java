/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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

import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.http.tls.ITLSConfigurationMode;
import com.helger.httpclient.HttpClientRetryHandler.ERetryMode;

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
          aHostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier ();

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
      LOGGER.warn ("Failed to init custom SSLConnectionSocketFactory - falling back to default SSLConnectionSocketFactory", ex);
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
   * @return The connection builder used by the
   *         {@link PoolingHttpClientConnectionManager} to create the default
   *         connection configuration.
   */
  @Nonnull
  public ConnectionConfig.Builder createConnectionConfigBuilder ()
  {
    return ConnectionConfig.custom ()
                           .setMalformedInputAction (CodingErrorAction.IGNORE)
                           .setUnmappableInputAction (CodingErrorAction.IGNORE)
                           .setCharset (StandardCharsets.UTF_8);
  }

  /**
   * @return The default connection configuration used by the
   *         {@link PoolingHttpClientConnectionManager}.
   */
  @Nonnull
  public ConnectionConfig createConnectionConfig ()
  {
    return createConnectionConfigBuilder ().build ();
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
    final Registry <ConnectionSocketFactory> aConSocketRegistry = RegistryBuilder.<ConnectionSocketFactory> create ()
                                                                                 .register ("http",
                                                                                            PlainConnectionSocketFactory.getSocketFactory ())
                                                                                 .register ("https", aSSLFactory)
                                                                                 .build ();
    final DnsResolver aDNSResolver = createDNSResolver ();
    final PoolingHttpClientConnectionManager aConnMgr = new PoolingHttpClientConnectionManager (aConSocketRegistry, aDNSResolver);
    aConnMgr.setDefaultMaxPerRoute (100);
    aConnMgr.setMaxTotal (200);
    aConnMgr.setValidateAfterInactivity (1000);

    final ConnectionConfig aConnectionConfig = createConnectionConfig ();
    aConnMgr.setDefaultConnectionConfig (aConnectionConfig);

    return aConnMgr;
  }

  @Nonnull
  public RequestConfig.Builder createRequestConfigBuilder ()
  {
    return RequestConfig.custom ()
                        .setCookieSpec (CookieSpecs.DEFAULT)
                        .setConnectionRequestTimeout (m_aSettings.getConnectionRequestTimeoutMS ())
                        .setConnectTimeout (m_aSettings.getConnectionTimeoutMS ())
                        .setSocketTimeout (m_aSettings.getSocketTimeoutMS ())
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
      final CredentialsProvider aCredentialsProvider = new BasicCredentialsProvider ();
      aCredentialsProvider.setCredentials (new AuthScope (aProxyHost), aProxyCredentials);
      return aCredentialsProvider;
    }
    return null;
  }

  @Nullable
  public HttpRequestRetryHandler createRequestRetryHandler (@Nonnegative final int nMaxRetries, @Nonnull final ERetryMode eRetryMode)
  {
    return new HttpClientRetryHandler (nMaxRetries, eRetryMode);
  }

  @Nonnull
  public HttpClientBuilder createHttpClientBuilder ()
  {
    final LayeredConnectionSocketFactory aSSLFactory = createSSLFactory ();
    if (aSSLFactory == null)
      throw new IllegalStateException ("Failed to create SSL SocketFactory");

    final SchemePortResolver aSchemePortResolver = createSchemePortResolver ();
    final HttpClientConnectionManager aConnMgr = createConnectionManager (aSSLFactory);
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
                                             @Nonnull final HttpRequest aRequest,
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

    final HttpClientBuilder aHCB = HttpClients.custom ()
                                              .setSchemePortResolver (aSchemePortResolver)
                                              .setConnectionManager (aConnMgr)
                                              .setDefaultRequestConfig (aRequestConfig)
                                              .setDefaultCredentialsProvider (aCredentialsProvider)
                                              .setRoutePlanner (aRoutePlanner);

    // Allow gzip,compress
    aHCB.addInterceptorLast (new RequestAcceptEncoding ());
    // Add cookies
    aHCB.addInterceptorLast (new RequestAddCookies ());
    // Un-gzip or uncompress
    aHCB.addInterceptorLast (new ResponseContentEncoding ());

    // Enable usage of Java networking system properties
    if (m_aSettings.isUseSystemProperties ())
      aHCB.useSystemProperties ();

    // Set retry handler (if needed)
    if (m_aSettings.hasRetries ())
      aHCB.setRetryHandler (createRequestRetryHandler (m_aSettings.getRetryCount (), m_aSettings.getRetryMode ()));

    // Set user agent (if any)
    if (m_aSettings.hasUserAgent ())
      aHCB.setUserAgent (m_aSettings.getUserAgent ());

    return aHCB;
  }

  @Nonnull
  public CloseableHttpClient createHttpClient ()
  {
    final HttpClientBuilder aBuilder = createHttpClientBuilder ();
    return aBuilder.build ();
  }
}
