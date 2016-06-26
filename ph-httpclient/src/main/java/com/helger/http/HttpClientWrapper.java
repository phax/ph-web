/**
 * Copyright (C) 2012-2016 winenet GmbH - www.winenet.at
 * All Rights Reserved
 *
 * This file is part of the winenet-Kellerbuch software.
 *
 * Proprietary and confidential.
 *
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited.
 */
package com.helger.http;

import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.helger.commons.random.RandomHelper;

@Immutable
public class HttpClientWrapper
{
  public HttpClientWrapper ()
  {}

  @Nonnull
  public LayeredConnectionSocketFactory createSSLFactory ()
  {
    LayeredConnectionSocketFactory aSSLFactory = null;
    try
    {
      aSSLFactory = SSLConnectionSocketFactory.getSystemSocketFactory ();
    }
    catch (final SSLInitializationException ex)
    {
      final SSLContext aSSLContext;
      try
      {
        aSSLContext = SSLContext.getInstance (SSLConnectionSocketFactory.TLS);
        final KeyManager [] aKeyManagers = null;
        final TrustManager [] aTrustManagers = null;
        aSSLContext.init (aKeyManagers, aTrustManagers, RandomHelper.getSecureRandom ());
        aSSLFactory = new SSLConnectionSocketFactory (aSSLContext);
      }
      catch (final SecurityException | KeyManagementException | NoSuchAlgorithmException ignore)
      {}
    }
    if (aSSLFactory != null)
      return aSSLFactory;

    return SSLConnectionSocketFactory.getSocketFactory ();
  }

  @Nonnull
  public ConnectionConfig createConnectionConfig ()
  {
    return ConnectionConfig.custom ()
                           .setMalformedInputAction (CodingErrorAction.IGNORE)
                           .setUnmappableInputAction (CodingErrorAction.IGNORE)
                           .setCharset (Consts.UTF_8)
                           .build ();
  }

  @Nonnull
  public HttpClientConnectionManager createConnectionManager ()
  {
    final Registry <ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory> create ()
                                                                  .register ("http",
                                                                             PlainConnectionSocketFactory.getSocketFactory ())
                                                                  .register ("https", createSSLFactory ())
                                                                  .build ();

    final PoolingHttpClientConnectionManager aConnMgr = new PoolingHttpClientConnectionManager (sfr);
    aConnMgr.setDefaultMaxPerRoute (100);
    aConnMgr.setMaxTotal (200);
    aConnMgr.setValidateAfterInactivity (1000);
    final ConnectionConfig aConnectionConfig = createConnectionConfig ();
    aConnMgr.setDefaultConnectionConfig (aConnectionConfig);
    return aConnMgr;
  }

  @Nonnull
  public RequestConfig createRequestConfig ()
  {
    return RequestConfig.custom ()
                        .setSocketTimeout (5000)
                        .setConnectTimeout (5000)
                        .setCircularRedirectsAllowed (false)
                        .setRedirectsEnabled (true)
                        .build ();
  }

  @Nullable
  public HttpHost createProxyHost ()
  {
    return null;
  }

  @Nonnull
  public HttpClientBuilder createHttpClientBuilder ()
  {
    final HttpClientConnectionManager aConnMgr = createConnectionManager ();
    final RequestConfig aRequestConfig = createRequestConfig ();
    final HttpHost aProxy = createProxyHost ();

    return HttpClientBuilder.create ()
                            .setConnectionManager (aConnMgr)
                            .setDefaultRequestConfig (aRequestConfig)
                            .setProxy (aProxy);
  }

  @Nonnull
  public CloseableHttpClient createHttpClient ()
  {
    return createHttpClientBuilder ().build ();
  }
}
