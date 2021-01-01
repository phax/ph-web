/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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

import java.security.GeneralSecurityException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.ws.HostnameVerifierVerifyAll;
import com.helger.commons.ws.TrustManagerTrustAll;
import com.helger.http.tls.ETLSVersion;
import com.helger.http.tls.ITLSConfigurationMode;
import com.helger.http.tls.TLSConfigurationMode;
import com.helger.httpclient.HttpClientRetryHandler.ERetryMode;

/**
 * All the easily configurable settings for an {@link HttpClientFactory}
 *
 * @author Philip Helger
 * @since 9.1.8
 */
@NotThreadSafe
public class HttpClientSettings implements IHttpClientSettings, ICloneable <HttpClientSettings>
{
  /**
   * Default configuration modes uses TLS 1.2, 1.1 or 1.0 and no specific cipher
   * suites
   */
  public static final ITLSConfigurationMode DEFAULT_TLS_CONFIG_MODE = new TLSConfigurationMode (new ETLSVersion [] { ETLSVersion.TLS_12,
                                                                                                                     ETLSVersion.TLS_11,
                                                                                                                     ETLSVersion.TLS_10 },
                                                                                                new String [0]);
  public static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = false;
  public static final boolean DEFAULT_USE_DNS_CACHE = true;
  public static final int DEFAULT_RETRIES = 0;
  public static final ERetryMode DEFAULT_RETRY_MODE = ERetryMode.RETRY_IDEMPOTENT_ONLY;
  public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT_MS = 5_000;
  public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 5_000;
  public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10_000;
  public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

  private static final Logger LOGGER = LoggerFactory.getLogger (HttpClientSettings.class);

  private boolean m_bUseSystemProperties = DEFAULT_USE_SYSTEM_PROPERTIES;
  private boolean m_bUseDNSClientCache = DEFAULT_USE_DNS_CACHE;
  private SSLContext m_aSSLContext;
  private ITLSConfigurationMode m_aTLSConfigurationMode;
  private HostnameVerifier m_aHostnameVerifier;
  private HttpHost m_aProxyHost;
  private Credentials m_aProxyCredentials;
  private final ICommonsOrderedSet <String> m_aNonProxyHosts = new CommonsLinkedHashSet <> ();
  private int m_nRetryCount = DEFAULT_RETRIES;
  private ERetryMode m_eRetryMode = DEFAULT_RETRY_MODE;
  private int m_nConnectionRequestTimeoutMS = DEFAULT_CONNECTION_REQUEST_TIMEOUT_MS;
  private int m_nConnectionTimeoutMS = DEFAULT_CONNECTION_TIMEOUT_MS;
  private int m_nSocketTimeoutMS = DEFAULT_SOCKET_TIMEOUT_MS;
  private String m_sUserAgent;
  private boolean m_bFollowRedirects = DEFAULT_FOLLOW_REDIRECTS;

  /**
   * Default constructor.
   */
  public HttpClientSettings ()
  {}

  /**
   * "Copy" constructor.
   *
   * @param aSource
   *        The source settings to copy from. May not be <code>null</code>.
   */
  public HttpClientSettings (@Nonnull final IHttpClientSettings aSource)
  {
    setAllFrom (aSource);
  }

  /**
   * Apply all settings from the provided HTTP client settings
   *
   * @param aSource
   *        The source settings to copy from. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setAllFrom (@Nonnull final IHttpClientSettings aSource)
  {
    ValueEnforcer.notNull (aSource, "Source");
    setUseSystemProperties (aSource.isUseSystemProperties ());
    setUseDNSClientCache (aSource.isUseDNSClientCache ());
    setSSLContext (aSource.getSSLContext ());
    setTLSConfigurationMode (aSource.getTLSConfigurationMode ());
    setHostnameVerifier (aSource.getHostnameVerifier ());
    setProxyHost (aSource.getProxyHost ());
    setProxyCredentials (aSource.getProxyCredentials ());
    nonProxyHosts ().setAll (aSource.nonProxyHosts ());
    setRetryCount (aSource.getRetryCount ());
    setRetryMode (aSource.getRetryMode ());
    setConnectionRequestTimeoutMS (aSource.getConnectionRequestTimeoutMS ());
    setConnectionTimeoutMS (aSource.getConnectionTimeoutMS ());
    setSocketTimeoutMS (aSource.getSocketTimeoutMS ());
    setUserAgent (aSource.getUserAgent ());
    setFollowRedirects (aSource.isFollowRedirects ());
    return this;
  }

  /**
   * @return <code>true</code> if system properties for HTTP client should be
   *         used, <code>false</code> if not. Default is <code>false</code>.
   */
  public final boolean isUseSystemProperties ()
  {
    return m_bUseSystemProperties;
  }

  /**
   * Enable the usage of system properties in the HTTP client?
   *
   * @param bUseSystemProperties
   *        <code>true</code> if system properties should be used,
   *        <code>false</code> if not.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setUseSystemProperties (final boolean bUseSystemProperties)
  {
    m_bUseSystemProperties = bUseSystemProperties;
    if (bUseSystemProperties && m_aProxyHost != null)
    {
      LOGGER.warn ("Since the proxy properties should be used, the explicit Proxy host is removed.");
      m_aProxyHost = null;
    }
    return this;
  }

  /**
   * @return <code>true</code> if DNS client caching is enabled (default),
   *         <code>false</code> if it is disabled.
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
   */
  @Nonnull
  public final HttpClientSettings setUseDNSClientCache (final boolean bUseDNSClientCache)
  {
    m_bUseDNSClientCache = bUseDNSClientCache;
    return this;
  }

  /**
   * Create a custom SSLContext to use for the SSL Socket factory.
   *
   * @return <code>null</code> if no custom context is present.
   */
  @Nullable
  public final SSLContext getSSLContext ()
  {
    return m_aSSLContext;
  }

  /**
   * Set the SSL Context to be used. By default no SSL context is present.
   *
   * @param aSSLContext
   *        The SSL context to be used. May be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setSSLContext (@Nullable final SSLContext aSSLContext)
  {
    m_aSSLContext = aSSLContext;
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
   */
  @Nonnull
  public final HttpClientSettings setSSLContextTrustAll () throws GeneralSecurityException
  {
    final SSLContext aSSLContext = SSLContext.getInstance ("TLS");
    aSSLContext.init (null, new TrustManager [] { new TrustManagerTrustAll (false) }, null);
    return setSSLContext (aSSLContext);
  }

  /**
   * @return The current hostname verifier to be used. Default to
   *         <code>null</code>.
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
   */
  @Nonnull
  public final HttpClientSettings setHostnameVerifier (@Nullable final HostnameVerifier aHostnameVerifier)
  {
    m_aHostnameVerifier = aHostnameVerifier;
    return this;
  }

  /**
   * Attention: INSECURE METHOD!<br>
   * Set a hostname verifier that trusts all host names.
   *
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setHostnameVerifierVerifyAll ()
  {
    return setHostnameVerifier (new HostnameVerifierVerifyAll (false));
  }

  /**
   * @return The TLS configuration mode to be used. <code>null</code> means to
   *         use the default settings without specific cipher suites.
   */
  @Nullable
  public final ITLSConfigurationMode getTLSConfigurationMode ()
  {
    return m_aTLSConfigurationMode;
  }

  /**
   * Set the TLS configuration mode to use.
   *
   * @param aTLSConfigurationMode
   *        The configuration mode to use. <code>null</code> means use system
   *        default.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setTLSConfigurationMode (@Nullable final ITLSConfigurationMode aTLSConfigurationMode)
  {
    m_aTLSConfigurationMode = aTLSConfigurationMode;
    return this;
  }

  /**
   * @return The proxy host to be used. May be <code>null</code>.
   */
  @Nullable
  public final HttpHost getProxyHost ()
  {
    return m_aProxyHost;
  }

  /**
   * Set a proxy host without proxy server credentials.
   *
   * @param aProxyHost
   *        The proxy host to be used. May be <code>null</code>.
   * @return this for chaining
   * @see #setProxyCredentials(Credentials)
   */
  @Nonnull
  public final HttpClientSettings setProxyHost (@Nullable final HttpHost aProxyHost)
  {
    m_aProxyHost = aProxyHost;
    if (aProxyHost != null && m_bUseSystemProperties)
    {
      LOGGER.warn ("Since an explicit Proxy host for is defined, the usage of the system properties is disabled.");
      m_bUseSystemProperties = false;
    }
    return this;
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
   * Set proxy credentials.
   *
   * @param aProxyCredentials
   *        The proxy server credentials to be used. May be <code>null</code>.
   *        They are only used if a proxy host is present! Usually they are of
   *        type {@link org.apache.http.auth.UsernamePasswordCredentials}.
   * @return this for chaining
   * @see #setProxyHost(HttpHost)
   */
  @Nonnull
  public final HttpClientSettings setProxyCredentials (@Nullable final Credentials aProxyCredentials)
  {
    m_aProxyCredentials = aProxyCredentials;
    return this;
  }

  /**
   * @return The set of all host names and IP addresses for which no proxy
   *         should be used. Never <code>null</code> and mutable.
   */
  @Nonnull
  @ReturnsMutableObject
  public final ICommonsOrderedSet <String> nonProxyHosts ()
  {
    return m_aNonProxyHosts;
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
   */
  @Nonnull
  public final HttpClientSettings addNonProxyHostsFromPipeString (@Nullable final String sDefinition)
  {
    if (StringHelper.hasText (sDefinition))
      StringHelper.explode ('|', sDefinition, sHost -> {
        final String sTrimmedHost = sHost.trim ();
        if (StringHelper.hasText (sTrimmedHost))
          m_aNonProxyHosts.add (sTrimmedHost);
      });
    return this;
  }

  /**
   * @return The number of retries. Defaults to {@link #DEFAULT_RETRIES}.
   */
  @Nonnegative
  public final int getRetryCount ()
  {
    return m_nRetryCount;
  }

  /**
   * Set the number of internal retries.
   *
   * @param nRetries
   *        Retries to use. Must be &ge; 0.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setRetryCount (@Nonnegative final int nRetries)
  {
    ValueEnforcer.isGE0 (nRetries, "Retries");
    m_nRetryCount = nRetries;
    return this;
  }

  /**
   * @return The retry-mode. Never <code>null</code>. The default is
   *         {@link #DEFAULT_RETRY_MODE}.
   */
  @Nonnull
  public final ERetryMode getRetryMode ()
  {
    return m_eRetryMode;
  }

  /**
   * Set the retry mode to use.
   *
   * @param eRetryMode
   *        Retry mode to use. Must not be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setRetryMode (@Nonnull final ERetryMode eRetryMode)
  {
    ValueEnforcer.notNull (eRetryMode, "RetryMode");
    m_eRetryMode = eRetryMode;
    return this;
  }

  public final int getConnectionRequestTimeoutMS ()
  {
    return m_nConnectionRequestTimeoutMS;
  }

  /**
   * Set the connection request timeout in milliseconds to use.
   *
   * @param nConnectionRequestTimeoutMS
   *        Timeout in milliseconds. The value 0 means "indefinite". Values &lt;
   *        0 mean "system default".
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setConnectionRequestTimeoutMS (final int nConnectionRequestTimeoutMS)
  {
    m_nConnectionRequestTimeoutMS = nConnectionRequestTimeoutMS;
    return this;
  }

  public final int getConnectionTimeoutMS ()
  {
    return m_nConnectionTimeoutMS;
  }

  /**
   * Set the connection timeout in milliseconds to use.
   *
   * @param nConnectionTimeoutMS
   *        Timeout in milliseconds. The value 0 means "indefinite". Values &lt;
   *        0 mean "system default".
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setConnectionTimeoutMS (final int nConnectionTimeoutMS)
  {
    m_nConnectionTimeoutMS = nConnectionTimeoutMS;
    return this;
  }

  public final int getSocketTimeoutMS ()
  {
    return m_nSocketTimeoutMS;
  }

  /**
   * Set the read/socket/request timeout in milliseconds to use.
   *
   * @param nSocketTimeoutMS
   *        Timeout in milliseconds. The value 0 means "indefinite". Values &lt;
   *        0 mean "system default".
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setSocketTimeoutMS (final int nSocketTimeoutMS)
  {
    m_nSocketTimeoutMS = nSocketTimeoutMS;
    return this;
  }

  @Nullable
  public final String getUserAgent ()
  {
    return m_sUserAgent;
  }

  /**
   * Set the optional user agent to be used. This is "just" a special HTTP
   * header.
   *
   * @param sUserAgent
   *        The user agent to be used. May be <code>null</code>.
   * @return this for chaining
   * @since 9.1.9
   */
  @Nonnull
  public final HttpClientSettings setUserAgent (@Nullable final String sUserAgent)
  {
    m_sUserAgent = sUserAgent;
    return this;
  }

  public final boolean isFollowRedirects ()
  {
    return m_bFollowRedirects;
  }

  /**
   * Enable or disable if HTTP redirects (HTTP status code 3xx) should be
   * followed or not.
   *
   * @param bFollowRedirects
   *        <code>true</code> to follow redirects, <code>false</code> if not.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setFollowRedirects (final boolean bFollowRedirects)
  {
    m_bFollowRedirects = bFollowRedirects;
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public HttpClientSettings getClone ()
  {
    return new HttpClientSettings (this);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("UseSystemProperties", m_bUseSystemProperties)
                                       .append ("UseDNSClientCache", m_bUseDNSClientCache)
                                       .append ("SSLContext", m_aSSLContext)
                                       .append ("TLSConfigurationMode", m_aTLSConfigurationMode)
                                       .append ("HostnameVerifier", m_aHostnameVerifier)
                                       .append ("ProxyHost", m_aProxyHost)
                                       .append ("ProxyCredentials", m_aProxyCredentials)
                                       .append ("NonProxyHosts", m_aNonProxyHosts)
                                       .append ("RetryCount", m_nRetryCount)
                                       .append ("RetryMode", m_eRetryMode)
                                       .append ("ConnectionRequestTimeoutMS", m_nConnectionRequestTimeoutMS)
                                       .append ("ConnectionTimeoutMS", m_nConnectionTimeoutMS)
                                       .append ("SocketTimeoutMS", m_nSocketTimeoutMS)
                                       .append ("UserAgent", m_sUserAgent)
                                       .append ("FollowRedirects", m_bFollowRedirects)
                                       .getToString ();
  }
}
