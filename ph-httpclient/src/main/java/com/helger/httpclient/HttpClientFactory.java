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
import java.security.GeneralSecurityException;

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
import com.helger.commons.annotation.ReturnsMutableObject;
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
  /**
   * Default configuration modes uses TLS 1.2, 1.1 or 1.0 and no specific cipher
   * suites
   */
  @Deprecated
  public static final ITLSConfigurationMode DEFAULT_TLS_CONFIG_MODE = HttpClientSettings.DEFAULT_TLS_CONFIG_MODE;
  @Deprecated
  public static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = HttpClientSettings.DEFAULT_USE_SYSTEM_PROPERTIES;
  @Deprecated
  public static final boolean DEFAULT_USE_DNS_CACHE = HttpClientSettings.DEFAULT_USE_DNS_CACHE;
  @Deprecated
  public static final int DEFAULT_RETRIES = HttpClientSettings.DEFAULT_RETRIES;
  @Deprecated
  public static final ERetryMode DEFAULT_RETRY_MODE = HttpClientSettings.DEFAULT_RETRY_MODE;

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
   * @return <code>true</code> if system properties for HTTP client should be
   *         used, <code>false</code> if not. Default is <code>false</code>.
   * @since 8.7.1
   */
  @Deprecated
  public final boolean isUseSystemProperties ()
  {
    return m_aSettings.isUseSystemProperties ();
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
  @Deprecated
  public final HttpClientFactory setUseSystemProperties (final boolean bUseSystemProperties)
  {
    m_aSettings.setUseSystemProperties (bUseSystemProperties);
    return this;
  }

  /**
   * @return <code>true</code> if DNS client caching is enabled (default),
   *         <code>false</code> if it is disabled.
   * @since 8.8.0
   */
  @Deprecated
  public final boolean isUseDNSClientCache ()
  {
    return m_aSettings.isUseDNSClientCache ();
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
  @Deprecated
  public final HttpClientFactory setUseDNSClientCache (final boolean bUseDNSClientCache)
  {
    m_aSettings.setUseDNSClientCache (bUseDNSClientCache);
    return this;
  }

  /**
   * Create a custom SSLContext to use for the SSL Socket factory.
   *
   * @return <code>null</code> if no custom context is present.
   */
  @Nullable
  @Deprecated
  public final SSLContext getSSLContext ()
  {
    return m_aSettings.getSSLContext ();
  }

  /**
   * Set the SSL Context to be used. By default no SSL context is present.
   *
   * @param aSSLContext
   *        The SSL context to be used. May be <code>null</code>.
   * @return this for chaining
   * @since 9.0.0
   */
  @Nonnull
  @Deprecated
  public final HttpClientFactory setSSLContext (@Nullable final SSLContext aSSLContext)
  {
    m_aSettings.setSSLContext (aSSLContext);
    return this;
  }

  /**
   * Attention: INSECURE METHOD!<br>
   * Set the a special SSL Context that does not expect any specific server
   * certificate. To be totally loose, you should also set a hostname verifier
   * that accepts all host names.
   *
   * @return this for chaining
   * @throws GeneralSecurityException
   *         In case TLS initialization fails
   * @since 9.0.1
   */
  @Nonnull
  @Deprecated
  public final HttpClientFactory setSSLContextTrustAll () throws GeneralSecurityException
  {
    m_aSettings.setSSLContextTrustAll ();
    return this;
  }

  /**
   * @return The current hostname verifier to be used. Default to
   *         <code>null</code>.
   * @since 8.8.2
   */
  @Nullable
  @Deprecated
  public final HostnameVerifier getHostnameVerifier ()
  {
    return m_aSettings.getHostnameVerifier ();
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
  @Deprecated
  public final HttpClientFactory setHostnameVerifier (@Nullable final HostnameVerifier aHostnameVerifier)
  {
    m_aSettings.setHostnameVerifier (aHostnameVerifier);
    return this;
  }

  /**
   * Attention: INSECURE METHOD!<br>
   * Set a hostname verifier that trusts all host names.
   *
   * @return this for chaining
   * @since 9.0.1
   */
  @Nonnull
  @Deprecated
  public final HttpClientFactory setHostnameVerifierVerifyAll ()
  {
    m_aSettings.setHostnameVerifierVerifyAll ();
    return this;
  }

  /**
   * @return The TLS configuration mode to be used. <code>null</code> means to
   *         use the default settings without specific cipher suites.
   * @since 9.0.5
   */
  @Nullable
  @Deprecated
  public final ITLSConfigurationMode getTLSConfigurationMode ()
  {
    return m_aSettings.getTLSConfigurationMode ();
  }

  /**
   * Set the TLS configuration mode to use.
   *
   * @param aTLSConfigurationMode
   *        The configuration mode to use. <code>null</code> means use system
   *        default.
   * @return this for chaining
   * @since 9.0.5
   */
  @Nonnull
  @Deprecated
  public final HttpClientFactory setTLSConfigurationMode (@Nullable final ITLSConfigurationMode aTLSConfigurationMode)
  {
    m_aSettings.setTLSConfigurationMode (aTLSConfigurationMode);
    return this;
  }

  /**
   * @return The proxy host to be used. May be <code>null</code>.
   */
  @Nullable
  @Deprecated
  public final HttpHost getProxyHost ()
  {
    return m_aSettings.getProxyHost ();
  }

  /**
   * @return The proxy server credentials to be used. May be <code>null</code>.
   */
  @Nullable
  @Deprecated
  public final Credentials getProxyCredentials ()
  {
    return m_aSettings.getProxyCredentials ();
  }

  /**
   * Set a proxy host without proxy server credentials.
   *
   * @param aProxy
   *        The proxy host to be used. May be <code>null</code>.
   * @since 8.8.0
   * @see #setProxyCredentials(Credentials)
   * @see #setProxy(HttpHost, Credentials)
   * @deprecated Use {@link #setProxyHost(HttpHost)} instead. Since 9.1.7
   */
  @Deprecated
  public final void setProxy (@Nullable final HttpHost aProxy)
  {
    setProxyHost (aProxy);
  }

  /**
   * Set a proxy host without proxy server credentials.
   *
   * @param aProxy
   *        The proxy host to be used. May be <code>null</code>.
   * @since 9.1.7
   * @see #setProxyCredentials(Credentials)
   * @see #setProxy(HttpHost, Credentials)
   */
  @Deprecated
  public final void setProxyHost (@Nullable final HttpHost aProxy)
  {
    m_aSettings.setProxyHost (aProxy);
  }

  /**
   * Set proxy credentials.
   *
   * @param aProxyCredentials
   *        The proxy server credentials to be used. May be <code>null</code>.
   *        They are only used if a proxy host is present! Usually they are of
   *        type {@link org.apache.http.auth.UsernamePasswordCredentials}.
   * @since 9.1.7
   * @see #setProxyHost(HttpHost)
   * @see #setProxy(HttpHost, Credentials)
   */
  @Deprecated
  public final void setProxyCredentials (@Nullable final Credentials aProxyCredentials)
  {
    m_aSettings.setProxyCredentials (aProxyCredentials);
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
   * @see #setProxyHost(HttpHost)
   * @see #setProxyCredentials(Credentials)
   * @deprecated Call the stuff separately (since 9.1.7)
   */
  @Deprecated
  public final void setProxy (@Nullable final HttpHost aProxy, @Nullable final Credentials aProxyCredentials)
  {
    setProxyHost (aProxy);
    setProxyCredentials (aProxyCredentials);
  }

  /**
   * @return The set of all host names and IP addresses for which no proxy
   *         should be used. Never <code>null</code> and mutable.
   * @since 9.1.1
   */
  @Nonnull
  @ReturnsMutableObject
  @Deprecated
  public final ICommonsSet <String> nonProxyHosts ()
  {
    return m_aSettings.nonProxyHosts ();
  }

  /**
   * All non-proxy hosts from a piped string as in
   * <code>127.0.0.1 | localhost</code>. Every entry must be separated by a
   * pipe, and the values are trimmed.
   *
   * @param sDefinition
   *        The definition string. May be <code>null</code> or empty or invalid.
   *        Every non-empty trimmed text between pipes is interpreted as a host
   *        name.
   * @return this for chaining
   * @since 9.1.1
   */
  @Nonnull
  @Deprecated
  public final HttpClientFactory addNonProxyHostsFromPipeString (@Nullable final String sDefinition)
  {
    m_aSettings.addNonProxyHostsFromPipeString (sDefinition);
    return this;
  }

  /**
   * @return The number of retries. Defaults to {@link #DEFAULT_RETRIES}.
   * @since 9.0.0
   */
  @Nonnegative
  @Deprecated
  public final int getRetries ()
  {
    return m_aSettings.getRetryCount ();
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
  @Deprecated
  public final HttpClientFactory setRetries (@Nonnegative final int nRetries)
  {
    m_aSettings.setRetryCount (nRetries);
    return this;
  }

  /**
   * @return The retry-mode. Never <code>null</code>. The default is
   *         {@link #DEFAULT_RETRY_MODE}.
   * @since 9.0.0
   */
  @Nonnull
  @Deprecated
  public final ERetryMode getRetryMode ()
  {
    return m_aSettings.getRetryMode ();
  }

  /**
   * Set the retry mode to use.
   *
   * @param eRetryMode
   *        Retry mode to use. Must not be <code>null</code>.
   * @return this for chaining
   * @since 9.0.0
   */
  @Nonnull
  @Deprecated
  public final HttpClientFactory setRetryMode (@Nonnull final ERetryMode eRetryMode)
  {
    m_aSettings.setRetryMode (eRetryMode);
    return this;
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
      LOGGER.warn ("Failed to init custom SSLConnectionSocketFactory - falling back to default SSLConnectionSocketFactory",
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
                        .setConnectionRequestTimeout (m_aSettings.getConnectionRequestTimeoutMS ())
                        .setConnectTimeout (m_aSettings.getConnectionTimeoutMS ())
                        .setSocketTimeout (m_aSettings.getSocketTimeoutMS ())
                        .setCircularRedirectsAllowed (false)
                        .setRedirectsEnabled (true);
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
  public HttpRequestRetryHandler createRequestRetryHandler (@Nonnegative final int nMaxRetries,
                                                            @Nonnull final ERetryMode eRetryMode)
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
