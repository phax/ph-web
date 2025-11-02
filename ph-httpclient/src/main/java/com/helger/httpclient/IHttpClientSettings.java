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

import java.time.Duration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.hc.core5.util.Timeout;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonnegative;
import com.helger.base.string.StringHelper;
import com.helger.http.tls.ITLSConfigurationMode;

/**
 * Read-only interface for {@link HttpClientSettings}
 *
 * @author Philip Helger
 * @since 9.1.8
 */
public interface IHttpClientSettings
{
  /**
   * @return <code>true</code> if DNS client caching is enabled (default), <code>false</code> if it
   *         is disabled.
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
   * @return The current hostname verifier to be used. Default to <code>null</code>.
   */
  @Nullable
  HostnameVerifier getHostnameVerifier ();

  /**
   * @return The TLS configuration mode to be used. <code>null</code> means to use the default
   *         settings without specific cipher suites.
   */
  @Nullable
  ITLSConfigurationMode getTLSConfigurationMode ();

  /**
   * @return The general HTTP proxy settings to be used. These settings apply to any protocol,
   *         except they are overridden in the more specific "http" and "https" proxy settings.
   *         Never <code>null</code>.
   * @see #getHttpProxy()
   * @see #getHttpsProxy()
   * @since 10.5.0
   */
  @NonNull
  IHttpProxySettings getGeneralProxy ();

  /**
   * @return The HTTP proxy settings to be used exclusively for the "http" protocol. Never
   *         <code>null</code>.
   * @see #getGeneralProxy()
   * @see #getHttpsProxy()
   * @since 10.5.0
   */
  @NonNull
  IHttpProxySettings getHttpProxy ();

  /**
   * @return The HTTP proxy settings to be used exclusively for the "https" protocol. Never
   *         <code>null</code>.
   * @see #getGeneralProxy()
   * @see #getHttpProxy()
   * @since 10.5.0
   */
  @NonNull
  IHttpProxySettings getHttpsProxy ();

  /**
   * @return The number of retries. Defaults to none.
   */
  @Nonnegative
  int getRetryCount ();

  /**
   * @return <code>true</code> if retries are enabled, <code>false</code> if not.
   */
  default boolean hasRetries ()
  {
    return getRetryCount () > 0;
  }

  /**
   * @return The retry interval (the duration after which a retry is performed). Never
   *         <code>null</code>.
   */
  @NonNull
  Duration getRetryInterval ();

  /**
   * @return <code>true</code> if retries should also be performed for non-idempotent requests.
   * @since 9.7.1
   */
  boolean isRetryAlways ();

  /**
   * @return The connection request timeout in milliseconds. A value of 0 means "indefinite". Never
   *         <code>null</code>.
   */
  @NonNull
  Timeout getConnectionRequestTimeout ();

  /**
   * @return The connect timeout. Never <code>null</code>.
   */
  @NonNull
  Timeout getConnectTimeout ();

  /**
   * @return The response/read/request/socket timeout. Never <code>null</code>.
   */
  @NonNull
  Timeout getResponseTimeout ();

  /**
   * @return The user agent header to be send. May be <code>null</code>.
   * @since 9.1.9
   */
  @Nullable
  String getUserAgent ();

  /**
   * @return <code>true</code> if a user agent is defined, <code>false</code> if not.
   * @since 9.1.9
   */
  default boolean hasUserAgent ()
  {
    return StringHelper.isNotEmpty (getUserAgent ());
  }

  /**
   * @return <code>true</code> if HTTP redirects (status codes 3xx) should be followed,
   *         <code>false</code> if not.
   * @since 9.1.9
   */
  boolean isFollowRedirects ();

  /**
   * @return <code>true</code> if the HTTP Connection "Keep-Alive" should be used,
   *         <code>false</code> if not.
   * @since 9.6.1
   */
  boolean isUseKeepAlive ();

  /**
   * @return <code>true</code> if a protocol upgrade e.g. from http to https should be done
   *         automatically or not. Since Apache HttpClient 5.4 this became the default.
   * @since 10.5.0
   */
  boolean isProtocolUpgradeEnabled ();
}
