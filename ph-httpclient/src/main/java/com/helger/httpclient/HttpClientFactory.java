/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
import java.security.GeneralSecurityException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
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
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;

/**
 * A factory for creating {@link CloseableHttpClient} that is e.g. to be used in
 * the {@link HttpClientManager}.
 *
 * @author Philip Helger
 */
@Immutable
public class HttpClientFactory implements IHttpClientProvider
{
  public static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = false;
  public static final boolean DEFAULT_USE_DNS_CACHE = true;

  private static final Logger s_aLogger = LoggerFactory.getLogger (HttpClientFactory.class);

  private boolean m_bUseSystemProperties = DEFAULT_USE_SYSTEM_PROPERTIES;
  private boolean m_bUseDNSClientCache = DEFAULT_USE_DNS_CACHE;
  private SSLContext m_aDefaultSSLContext;
  private HostnameVerifier m_aHostnameVerifier;
  private HttpHost m_aProxy;
  private Credentials m_aProxyCredentials;
  private int m_nRetries = 0;

  /**
   * Default constructor.
   */
  public HttpClientFactory ()
  {}

  /**
   * @return <code>true</code> if system properties for HTTP client should be
   *         used, <code>false</code> if not. Default is <code>false</code>.
   * @since 8.7.1
   */
  public final boolean isUseSystemProperties ()
  {
    return m_bUseSystemProperties;
  }

  /**
   * Enable the usage of system properties in the HTTP client?<br>
   * Supported properties are (source:
   * http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/client/HttpClientBuilder.html):
   * <ul>
   * <li>ssl.TrustManagerFactory.algorithm</li>
   * <li>javax.net.ssl.trustStoreType</li>
   * <li>javax.net.ssl.trustStore</li>
   * <li>javax.net.ssl.trustStoreProvider</li>
   * <li>javax.net.ssl.trustStorePassword</li>
   * <li>ssl.KeyManagerFactory.algorithm</li>
   * <li>javax.net.ssl.keyStoreType</li>
   * <li>javax.net.ssl.keyStore</li>
   * <li>javax.net.ssl.keyStoreProvider</li>
   * <li>javax.net.ssl.keyStorePassword</li>
   * <li>https.protocols</li>
   * <li>https.cipherSuites</li>
   * <li>http.proxyHost</li>
   * <li>http.proxyPort</li>
   * <li>http.nonProxyHosts</li>
   * <li>http.keepAlive</li>
   * <li>http.maxConnections</li>
   * <li>http.agent</li>
   * </ul>
   *
   * @param bUseSystemProperties
   *        <code>true</code> if system properties should be used,
   *        <code>false</code> if not.
   * @return this for chaining
   * @since 8.7.1
   */
  @Nonnull
  public final HttpClientFactory setUseSystemProperties (final boolean bUseSystemProperties)
  {
    m_bUseSystemProperties = bUseSystemProperties;
    return this;
  }

  /**
   * @return <code>true</code> if DNS client caching is enabled (default),
   *         <code>false</code> if it is disabled.
   * @since 8.8.0
   */
  public final boolean isUseDNSClientCache ()
  {
    return m_bUseDNSClientCache;
  }

  /**
   * Enable or disable DNS client caching. By default caching is enabled.
   *
   * @param bUseDNSClientCache
   *        <code>true</code> to use DNS caching, <code>false</code> to disable
   *        it.
   * @return this for chaining
   * @since 8.8.0
   */
  @Nonnull
  public final HttpClientFactory setUseDNSClientCache (final boolean bUseDNSClientCache)
  {
    m_bUseDNSClientCache = bUseDNSClientCache;
    return this;
  }

  /**
   * Create a custom SSLContext to use for the SSL Socket factory.
   *
   * @return <code>null</code> if no custom context is present.
   * @throws GeneralSecurityException
   *         In case key management problems occur.
   */
  @Nullable
  public final SSLContext getSSLContext () throws GeneralSecurityException
  {
    return m_aDefaultSSLContext;
  }

  /**
   * Set the SSL Context to be used. By default no SSL context is present.
   *
   * @param aSSLContext
   *        The SSL context to be used. May be <code>null</code>-
   * @return this for chaining
   * @since 9.0.0
   */
  @Nonnull
  public final HttpClientFactory setSSLContext (@Nullable final SSLContext aSSLContext)
  {
    m_aDefaultSSLContext = aSSLContext;
    return this;
  }

  /**
   * @return The current hostname verifier to be used. Default to
   *         <code>null</code>.
   * @since 8.8.2
   */
  @Nullable
  public final HostnameVerifier getHostnameVerifier ()
  {
    return m_aHostnameVerifier;
  }

  /**
   * Set the hostname verifier to be used.
   *
   * @param aHostnameVerifier
   *        Verifier to be used. May be <code>null</code>.
   * @return this for chaining
   * @since 8.8.2
   */
  @Nonnull
  public final HttpClientFactory setHostnameVerifier (@Nullable final HostnameVerifier aHostnameVerifier)
  {
    m_aHostnameVerifier = aHostnameVerifier;
    return this;
  }

  /**
   * @return The proxy host to be used. May be <code>null</code>.
   */
  @Nullable
  public final HttpHost getProxyHost ()
  {
    return m_aProxy;
  }

  /**
   * @return The proxy server credentials to be used. May be <code>null</code>.
   */
  @Nullable
  public final Credentials getProxyCredentials ()
  {
    return m_aProxyCredentials;
  }

  /**
   * Set a proxy host without proxy server credentials.
   *
   * @param aProxy
   *        The proxy host to be used. May be <code>null</code>.
   * @since 8.8.0
   * @see #setProxy(HttpHost, Credentials)
   */
  public final void setProxy (@Nullable final HttpHost aProxy)
  {
    setProxy (aProxy, (Credentials) null);
  }

  /**
   * Set proxy host and proxy credentials.
   *
   * @param aProxy
   *        The proxy host to be used. May be <code>null</code>.
   * @param aProxyCredentials
   *        The proxy server credentials to be used. May be <code>null</code>.
   *        They are only used if a proxy host is present! Usually they are of
   *        type {@link org.apache.http.auth.UsernamePasswordCredentials}.
   * @since 8.8.0
   * @see #setProxy(HttpHost)
   */
  public final void setProxy (@Nullable final HttpHost aProxy, @Nullable final Credentials aProxyCredentials)
  {
    m_aProxy = aProxy;
    m_aProxyCredentials = aProxyCredentials;
  }

  /**
   * @return The number of retries. Defaults to 0.
   * @since 9.0.0
   */
  @Nonnegative
  public final int getRetries ()
  {
    return m_nRetries;
  }

  /**
   * Set the number of internal retries.
   *
   * @param nRetries
   *        Retries to use. Must be &ge; 0.
   * @return this for chaining
   * @since 9.0.0
   */
  @Nonnull
  public final HttpClientFactory setRetries (@Nonnegative final int nRetries)
  {
    ValueEnforcer.isGE0 (nRetries, "Retries");
    m_nRetries = nRetries;
    return this;
  }

  @Nullable
  public LayeredConnectionSocketFactory createSSLFactory ()
  {
    LayeredConnectionSocketFactory aSSLFactory = null;

    // Custom hostname verifier preferred
    HostnameVerifier aHostnameVerifier = m_aHostnameVerifier;
    if (aHostnameVerifier == null)
      aHostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier ();

    // First try with a custom SSL context
    try
    {
      final SSLContext aSSLContext = getSSLContext ();
      if (aSSLContext != null)
      {
        aSSLFactory = new SSLConnectionSocketFactory (aSSLContext,
                                                      new String [] { "TLSv1.2", "TLSv1.1", "TLSv1" },
                                                      null,
                                                      aHostnameVerifier);
      }
    }
    catch (final GeneralSecurityException | SSLInitializationException ex)
    {
      // Fall through
      s_aLogger.warn ("Failed to init custom SSLConnectionSocketFactory - falling back to default SSLConnectionSocketFactory",
                      ex);
    }

    if (aSSLFactory == null)
    {
      // No custom SSL context present - use system defaults
      try
      {
        aSSLFactory = SSLConnectionSocketFactory.getSystemSocketFactory ();
      }
      catch (final SSLInitializationException ex)
      {
        try
        {
          aSSLFactory = SSLConnectionSocketFactory.getSocketFactory ();
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
   * @see #isUseDNSClientCache()
   * @see #setUseDNSClientCache(boolean)
   * @since 8.8.0
   */
  @Nullable
  public DnsResolver createDNSResolver ()
  {
    // If caching is active, use the default System resolver
    return m_bUseDNSClientCache ? SystemDefaultDnsResolver.INSTANCE : NonCachingDnsResolver.INSTANCE;
  }

  @Nonnull
  public HttpClientConnectionManager createConnectionManager ()
  {
    final LayeredConnectionSocketFactory aSSLFactory = createSSLFactory ();
    if (aSSLFactory == null)
      throw new IllegalStateException ("Failed to create SSL SocketFactory");

    final Registry <ConnectionSocketFactory> aConSocketRegistry = RegistryBuilder.<ConnectionSocketFactory> create ()
                                                                                 .register ("http",
                                                                                            PlainConnectionSocketFactory.getSocketFactory ())
                                                                                 .register ("https", aSSLFactory)
                                                                                 .build ();
    final DnsResolver aDNSResolver = createDNSResolver ();
    final PoolingHttpClientConnectionManager aConnMgr = new PoolingHttpClientConnectionManager (aConSocketRegistry,
                                                                                                aDNSResolver);
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
                        .setSocketTimeout (10_000)
                        .setConnectTimeout (5_000)
                        .setConnectionRequestTimeout (5_000)
                        .setCircularRedirectsAllowed (false)
                        .setRedirectsEnabled (true);
  }

  @Nonnull
  public RequestConfig createRequestConfig ()
  {
    return createRequestConfigBuilder ().build ();
  }

  @Nonnull
  public HttpClientBuilder createHttpClientBuilder ()
  {
    final HttpClientConnectionManager aConnMgr = createConnectionManager ();
    final RequestConfig aRequestConfig = createRequestConfig ();
    final HttpHost aProxyHost = getProxyHost ();
    final Credentials aProxyCredentials = getProxyCredentials ();

    final HttpClientBuilder aHCB = HttpClients.custom ()
                                              .setConnectionManager (aConnMgr)
                                              .setDefaultRequestConfig (aRequestConfig)
                                              .setProxy (aProxyHost);

    if (aProxyHost != null && aProxyCredentials != null)
    {
      final CredentialsProvider aCredentialsProvider = new BasicCredentialsProvider ();
      aCredentialsProvider.setCredentials (new AuthScope (aProxyHost), aProxyCredentials);
      aHCB.setDefaultCredentialsProvider (aCredentialsProvider);
    }

    // Allow gzip,compress
    aHCB.addInterceptorLast (new RequestAcceptEncoding ());
    // Add cookies
    aHCB.addInterceptorLast (new RequestAddCookies ());
    // Un-gzip or uncompress
    aHCB.addInterceptorLast (new ResponseContentEncoding ());

    // Enable usage of Java networking system properties
    if (m_bUseSystemProperties)
      aHCB.useSystemProperties ();

    // Set retry handler (if needed)
    if (m_nRetries > 0)
      aHCB.setRetryHandler (new HttpClientRetryHandler (m_nRetries));

    return aHCB;
  }

  @Nonnull
  public CloseableHttpClient createHttpClient ()
  {
    final HttpClientBuilder aBuilder = createHttpClientBuilder ();
    return aBuilder.build ();
  }
}
