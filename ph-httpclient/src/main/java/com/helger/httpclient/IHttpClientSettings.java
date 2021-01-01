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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;

import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.string.StringHelper;
import com.helger.http.tls.ITLSConfigurationMode;
import com.helger.httpclient.HttpClientRetryHandler.ERetryMode;

/**
 * Read-only interface for {@link HttpClientSettings}
 *
 * @author Philip Helger
 * @since 9.1.8
 */
public interface IHttpClientSettings
{
  /**
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
   * @return <code>true</code> if system properties for HTTP client should be
   *         used, <code>false</code> if not. Default is <code>false</code>.
   */
  boolean isUseSystemProperties ();

  /**
   * @return <code>true</code> if DNS client caching is enabled (default),
   *         <code>false</code> if it is disabled.
   */
  boolean isUseDNSClientCache ();

  /**
   * Create a custom SSLContext to use for the SSL Socket factory.
   *
   * @return <code>null</code> if no custom context is present.
   */
  @Nullable
  SSLContext getSSLContext ();

  /**
   * @return The current hostname verifier to be used. Default to
   *         <code>null</code>.
   */
  @Nullable
  HostnameVerifier getHostnameVerifier ();

  /**
   * @return The TLS configuration mode to be used. <code>null</code> means to
   *         use the default settings without specific cipher suites.
   */
  @Nullable
  ITLSConfigurationMode getTLSConfigurationMode ();

  /**
   * @return The proxy host to be used. May be <code>null</code>.
   */
  @Nullable
  HttpHost getProxyHost ();

  /**
   * @return The proxy server credentials to be used. May be <code>null</code>.
   */
  @Nullable
  Credentials getProxyCredentials ();

  /**
   * @return The set of all host names and IP addresses for which no proxy
   *         should be used. Never <code>null</code> and mutable.
   */
  @Nonnull
  @ReturnsMutableObject
  ICommonsSet <String> nonProxyHosts ();

  /**
   * @return The number of retries. Defaults to none.
   */
  @Nonnegative
  int getRetryCount ();

  /**
   * @return <code>true</code> if retries are enabled, <code>false</code> if
   *         not.
   */
  default boolean hasRetries ()
  {
    return getRetryCount () > 0;
  }

  /**
   * @return The retry-mode. Never <code>null</code>.
   */
  @Nonnull
  ERetryMode getRetryMode ();

  /**
   * @return The connection request timeout in milliseconds. A value of 0 means
   *         "indefinite". Values &lt; 0 means "system default".
   */
  int getConnectionRequestTimeoutMS ();

  /**
   * @return The connection timeout in milliseconds. A value of 0 means
   *         "indefinite". Values &lt; 0 means "system default".
   */
  int getConnectionTimeoutMS ();

  /**
   * @return The read/request/socket timeout in milliseconds. A value of 0 means
   *         "indefinite". Values &lt; 0 means "system default".
   */
  int getSocketTimeoutMS ();

  /**
   * @return The user agent header to be send. May be <code>null</code>.
   * @since 9.1.9
   */
  @Nullable
  String getUserAgent ();

  /**
   * @return <code>true</code> if a user agent is defined, <code>false</code> if
   *         not.
   * @since 9.1.9
   */
  default boolean hasUserAgent ()
  {
    return StringHelper.hasText (getUserAgent ());
  }

  /**
   * @return <code>true</code> if HTTP redirects (status codes 3xx) should be
   *         followed, <code>false</code> if not.
   * @since 9.1.9
   */
  boolean isFollowRedirects ();
}
