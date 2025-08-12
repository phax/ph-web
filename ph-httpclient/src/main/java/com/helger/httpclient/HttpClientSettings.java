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

import java.security.GeneralSecurityException;
import java.time.Duration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.ws.HostnameVerifierVerifyAll;
import com.helger.commons.ws.TrustManagerTrustAll;
import com.helger.http.tls.ETLSVersion;
import com.helger.http.tls.ITLSConfigurationMode;
import com.helger.http.tls.TLSConfigurationMode;

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
   * Default configuration modes uses TLS 1.3, TLS 1.2, 1.1 or 1.0 and no specific cipher suites
   */
  public static final ITLSConfigurationMode DEFAULT_TLS_CONFIG_MODE = new TLSConfigurationMode (new ETLSVersion [] { ETLSVersion.TLS_13,
                                                                                                                     ETLSVersion.TLS_12,
                                                                                                                     ETLSVersion.TLS_11,
                                                                                                                     ETLSVersion.TLS_10 },
                                                                                                ArrayHelper.EMPTY_STRING_ARRAY);
  public static final boolean DEFAULT_USE_SYSTEM_PROPERTIES = false;
  public static final boolean DEFAULT_USE_DNS_CACHE = true;
  public static final int DEFAULT_RETRY_COUNT = 0;
  @Deprecated (since = "10.0.0", forRemoval = true)
  public static final int DEFAULT_RETRIES = DEFAULT_RETRY_COUNT;
  public static final Duration DEFAULT_RETRY_INTERVAL = Duration.ofSeconds (1);
  public static final boolean DEFAULT_RETRY_ALWAYS = false;
  public static final Timeout DEFAULT_CONNECTION_REQUEST_TIMEOUT = Timeout.ofSeconds (5);
  public static final Timeout DEFAULT_CONNECT_TIMEOUT = Timeout.ofSeconds (5);
  public static final Timeout DEFAULT_RESPONSE_TIMEOUT = Timeout.ofSeconds (10);
  public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;
  public static final boolean DEFAULT_USE_KEEP_ALIVE = true;
  // Default from Apache HttpClient since v5.4
  public static final boolean DEFAULT_PROTOCOL_UPGRADE_ENABLED = true;

  private static final Logger LOGGER = LoggerFactory.getLogger (HttpClientSettings.class);

  private boolean m_bUseSystemProperties = DEFAULT_USE_SYSTEM_PROPERTIES;
  private boolean m_bUseDNSClientCache = DEFAULT_USE_DNS_CACHE;

  private SSLContext m_aSSLContext;
  private ITLSConfigurationMode m_aTLSConfigurationMode;
  private HostnameVerifier m_aHostnameVerifier;

  // A proxy for all URLs
  private final HttpProxySettings m_aGeneralProxy = new HttpProxySettings ();
  // A proxy only for "http" URLs
  private final HttpProxySettings m_aHttpProxy = new HttpProxySettings ();
  // A proxy only for "https" URLs
  private final HttpProxySettings m_aHttpsProxy = new HttpProxySettings ();

  private int m_nRetryCount = DEFAULT_RETRY_COUNT;
  private Duration m_aRetryInterval = DEFAULT_RETRY_INTERVAL;
  private boolean m_bRetryAlways = DEFAULT_RETRY_ALWAYS;

  private Timeout m_aConnectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;
  private Timeout m_aConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
  private Timeout m_aResponseTimeout = DEFAULT_RESPONSE_TIMEOUT;

  private String m_sUserAgent;
  private boolean m_bFollowRedirects = DEFAULT_FOLLOW_REDIRECTS;
  private boolean m_bUseKeepAlive = DEFAULT_USE_KEEP_ALIVE;
  private boolean m_bProtocolUpgradeEnabled = DEFAULT_PROTOCOL_UPGRADE_ENABLED;

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
  @SuppressWarnings ("removal")
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
    setRetryInterval (aSource.getRetryInterval ());
    setRetryAlways (aSource.isRetryAlways ());
    setConnectionRequestTimeout (aSource.getConnectionRequestTimeout ());
    setConnectTimeout (aSource.getConnectTimeout ());
    setResponseTimeout (aSource.getResponseTimeout ());
    setUserAgent (aSource.getUserAgent ());
    setFollowRedirects (aSource.isFollowRedirects ());
    setUseKeepAlive (aSource.isUseKeepAlive ());
    setProtocolUpgradeEnabled (aSource.isProtocolUpgradeEnabled ());
    return this;
  }

  /**
   * @return <code>true</code> if system properties for HTTP client should be used,
   *         <code>false</code> if not. Default is <code>false</code>.
   */
  @Deprecated (since = "10.0.0", forRemoval = true)
  public final boolean isUseSystemProperties ()
  {
    return m_bUseSystemProperties;
  }

  /**
   * Enable the usage of system properties in the HTTP client?
   *
   * @param bUseSystemProperties
   *        <code>true</code> if system properties should be used, <code>false</code> if not.
   * @return this for chaining
   */
  @Nonnull
  @Deprecated (since = "10.0.0", forRemoval = true)
  public final HttpClientSettings setUseSystemProperties (final boolean bUseSystemProperties)
  {
    m_bUseSystemProperties = bUseSystemProperties;
    if (bUseSystemProperties)
    {
      if (m_aGeneralProxy.hasProxyHost ())
      {
        LOGGER.warn ("Since the proxy properties should be used, the explicit Proxy host is removed.");
        m_aGeneralProxy.setProxyHost (null);
      }
    }
    return this;
  }

  /**
   * @return <code>true</code> if DNS client caching is enabled (default), <code>false</code> if it
   *         is disabled.
   */
  public final boolean isUseDNSClientCache ()
  {
    return m_bUseDNSClientCache;
  }

  /**
   * Enable or disable DNS client caching. By default caching is enabled.
   *
   * @param bUseDNSClientCache
   *        <code>true</code> to use DNS caching, <code>false</code> to disable it.
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
   * Set the a special TLS/SSL Context that does not expect any specific server certificate. To be
   * totally loose, you should also set a hostname verifier that accepts all host names.
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
   * @return The current hostname verifier to be used. Default to <code>null</code>.
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
   * @return The TLS configuration mode to be used. <code>null</code> means to use the default
   *         settings without specific cipher suites.
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
   *        The configuration mode to use. <code>null</code> means use system default.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setTLSConfigurationMode (@Nullable final ITLSConfigurationMode aTLSConfigurationMode)
  {
    m_aTLSConfigurationMode = aTLSConfigurationMode;
    return this;
  }

  @Nonnull
  public final HttpProxySettings getGeneralProxy ()
  {
    return m_aGeneralProxy;
  }

  /**
   * Set a proxy host without proxy server credentials.
   *
   * @param aProxyHost
   *        The proxy host to be used. May be <code>null</code>.
   * @return this for chaining
   * @see #setProxyCredentials(Credentials)
   * @deprecated Use the method through {@link #getGeneralProxy()} instead
   */
  @Nonnull
  @Deprecated (forRemoval = true, since = "10.5.0")
  public final HttpClientSettings setProxyHost (@Nullable final HttpHost aProxyHost)
  {
    m_aGeneralProxy.setProxyHost (aProxyHost);
    if (aProxyHost != null && m_bUseSystemProperties)
    {
      LOGGER.warn ("Since an explicit Proxy host for is defined, the usage of the system properties is disabled.");
      m_bUseSystemProperties = false;
    }
    return this;
  }

  /**
   * Set proxy credentials.
   *
   * @param aProxyCredentials
   *        The proxy server credentials to be used. May be <code>null</code>. They are only used if
   *        a proxy host is present! Usually they are of type
   *        {@link org.apache.hc.client5.http.auth.UsernamePasswordCredentials}.
   * @return this for chaining
   * @see #setProxyHost(HttpHost)
   * @deprecated Use the method through {@link #getGeneralProxy()} instead
   */
  @Nonnull
  @Deprecated (forRemoval = true, since = "10.5.0")
  public final HttpClientSettings setProxyCredentials (@Nullable final Credentials aProxyCredentials)
  {
    m_aGeneralProxy.setProxyCredentials (aProxyCredentials);
    return this;
  }

  /**
   * @return The set of all host names and IP addresses for which no proxy should be used. Never
   *         <code>null</code> and mutable.
   * @deprecated Use the method through {@link #getGeneralProxy()} instead
   */
  @Nonnull
  @ReturnsMutableObject
  @Deprecated (forRemoval = true, since = "10.5.0")
  public final ICommonsOrderedSet <String> nonProxyHosts ()
  {
    return m_aGeneralProxy.nonProxyHosts ();
  }

  /**
   * Add all non-proxy hosts from a piped string as in <code>127.0.0.1 | localhost</code>. Every
   * entry must be separated by a pipe, and the values are trimmed.
   *
   * @param sDefinition
   *        The definition string. May be <code>null</code> or empty or invalid. Every non-empty
   *        trimmed text between pipes is interpreted as a host name.
   * @return this for chaining
   * @deprecated Use the method through {@link #getGeneralProxy()} instead
   */
  @Nonnull
  @Deprecated (forRemoval = true, since = "10.5.0")
  public final HttpClientSettings addNonProxyHostsFromPipeString (@Nullable final String sDefinition)
  {
    m_aGeneralProxy.addNonProxyHostsFromPipeString (sDefinition);
    return this;
  }

  /**
   * Set all non-proxy hosts from a piped string as in <code>127.0.0.1 | localhost</code>. Every
   * entry must be separated by a pipe, and the values are trimmed.<br>
   * This is a shortcut for first clearing the list and then calling
   * {@link #addNonProxyHostsFromPipeString(String)}
   *
   * @param sDefinition
   *        The definition string. May be <code>null</code> or empty or invalid. Every non-empty
   *        trimmed text between pipes is interpreted as a host name.
   * @return this for chaining
   * @see #addNonProxyHostsFromPipeString(String)
   * @since 10.0.0
   * @deprecated Use the method through {@link #getGeneralProxy()} instead
   */
  @Nonnull
  @Deprecated (forRemoval = true, since = "10.5.0")
  public final HttpClientSettings setNonProxyHostsFromPipeString (@Nullable final String sDefinition)
  {
    m_aGeneralProxy.setNonProxyHostsFromPipeString (sDefinition);
    return this;
  }

  @Nonnull
  public final HttpProxySettings getHttpProxy ()
  {
    return m_aHttpProxy;
  }

  @Nonnull
  public final HttpProxySettings getHttpsProxy ()
  {
    return m_aHttpsProxy;
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

  @Nonnull
  public final Duration getRetryInterval ()
  {
    return m_aRetryInterval;
  }

  @Nonnull
  public final TimeValue getRetryIntervalAsTimeValue ()
  {
    return TimeValue.ofMilliseconds (m_aRetryInterval.toMillis ());
  }

  /**
   * Set the retry interval to use.
   *
   * @param aRetryInterval
   *        Retry interval to use. Must not be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setRetryInterval (@Nonnull final Duration aRetryInterval)
  {
    ValueEnforcer.notNull (aRetryInterval, "RetryInterval");
    m_aRetryInterval = aRetryInterval;
    return this;
  }

  public final boolean isRetryAlways ()
  {
    return m_bRetryAlways;
  }

  /**
   * Enable or disable to retry always. By default non-idempotent requests are not retried.
   *
   * @param bRetryAlways
   *        <code>true</code> to retry always
   * @return this for chaining
   * @since 9.7.1
   */
  @Nonnull
  public final HttpClientSettings setRetryAlways (final boolean bRetryAlways)
  {
    m_bRetryAlways = bRetryAlways;
    return this;
  }

  @Nonnull
  public final Timeout getConnectionRequestTimeout ()
  {
    return m_aConnectionRequestTimeout;
  }

  /**
   * Set the connection request timeout to use.
   *
   * @param aConnectionRequestTimeout
   *        Timeout to be used. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setConnectionRequestTimeout (@Nonnull final Timeout aConnectionRequestTimeout)
  {
    ValueEnforcer.notNull (aConnectionRequestTimeout, "ConnectionRequestTimeout");
    m_aConnectionRequestTimeout = aConnectionRequestTimeout;
    return this;
  }

  @Nonnull
  public final Timeout getConnectTimeout ()
  {
    return m_aConnectTimeout;
  }

  /**
   * Set the connect timeout to use.
   *
   * @param aConnectTimeout
   *        Timeout to be used. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setConnectTimeout (@Nonnull final Timeout aConnectTimeout)
  {
    ValueEnforcer.notNull (aConnectTimeout, "ConnectTimeout");
    m_aConnectTimeout = aConnectTimeout;
    return this;
  }

  @Nonnull
  public final Timeout getResponseTimeout ()
  {
    return m_aResponseTimeout;
  }

  /**
   * Set the read/socket/request timeout to use.
   *
   * @param aResponseTimeout
   *        Timeout to be used. May not be <code>null</code>.
   * @return this for chaining.
   */
  @Nonnull
  public final HttpClientSettings setResponseTimeout (@Nonnull final Timeout aResponseTimeout)
  {
    ValueEnforcer.notNull (aResponseTimeout, "SocketTimeout");
    m_aResponseTimeout = aResponseTimeout;
    return this;
  }

  @Nullable
  public final String getUserAgent ()
  {
    return m_sUserAgent;
  }

  /**
   * Set the optional user agent to be used. This is "just" a special HTTP header.
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
   * Enable or disable if HTTP redirects (HTTP status code 3xx) should be followed or not.
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

  public final boolean isUseKeepAlive ()
  {
    return m_bUseKeepAlive;
  }

  /**
   * Enable or disable if use of the HTTP Connection "Keep-alive".
   *
   * @param bUseKeepAlive
   *        <code>true</code> to use keep-alive, <code>false</code> if not.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setUseKeepAlive (final boolean bUseKeepAlive)
  {
    m_bUseKeepAlive = bUseKeepAlive;
    return this;
  }

  public final boolean isProtocolUpgradeEnabled ()
  {
    return m_bProtocolUpgradeEnabled;
  }

  /**
   * Enable or disable if use of the HTTP Connection "Keep-alive".
   *
   * @param bProtocolUpgradeEnabled
   *        <code>true</code> to enable protocol upgrade, <code>false</code> to disable it.
   * @return this for chaining
   */
  @Nonnull
  public final HttpClientSettings setProtocolUpgradeEnabled (final boolean bProtocolUpgradeEnabled)
  {
    m_bProtocolUpgradeEnabled = bProtocolUpgradeEnabled;
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
                                       .append ("GeneralProxy", m_aGeneralProxy)
                                       .append ("HttpProxy", m_aHttpProxy)
                                       .append ("HttpsProxy", m_aHttpsProxy)
                                       .append ("RetryCount", m_nRetryCount)
                                       .append ("RetryInterval", m_aRetryInterval)
                                       .append ("RetryAlways", m_bRetryAlways)
                                       .append ("ConnectionRequestTimeout", m_aConnectionRequestTimeout)
                                       .append ("ConnectionTimeout", m_aConnectTimeout)
                                       .append ("ResponseTimeout", m_aResponseTimeout)
                                       .append ("UserAgent", m_sUserAgent)
                                       .append ("FollowRedirects", m_bFollowRedirects)
                                       .append ("UseKeepAlive", m_bUseKeepAlive)
                                       .append ("ProtocolUpgradeEnabled", m_bProtocolUpgradeEnabled)
                                       .getToString ();
  }
}
