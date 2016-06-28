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
package com.helger.httpclient;

import java.nio.charset.CodingErrorAction;
import java.security.GeneralSecurityException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.helger.commons.annotation.OverrideOnDemand;

@Immutable
public class HttpClientWrapper
{
  public HttpClientWrapper ()
  {}

  /**
   * Create a custom SSLContext to use for the SSL Socket factory.
   *
   * @return <code>null</code> if no custom context is present.
   * @throws GeneralSecurityException
   *         In case key management problems occur.
   */
  @Nullable
  @OverrideOnDemand
  public SSLContext createSSLContext () throws GeneralSecurityException
  {
    return null;
  }

  @Nonnull
  public LayeredConnectionSocketFactory createSSLFactory ()
  {
    LayeredConnectionSocketFactory aSSLFactory = null;

    // First try with a custom SSL context
    try
    {
      final SSLContext aSSLContext = createSSLContext ();
      if (aSSLContext != null)
        try
        {
          aSSLFactory = new SSLConnectionSocketFactory (aSSLContext);
        }
        catch (final SSLInitializationException ex)
        {
          // Fall through
        }
    }
    catch (final GeneralSecurityException ex)
    {
      // Fall through
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
    final LayeredConnectionSocketFactory aSSLFactory = createSSLFactory ();
    if (aSSLFactory == null)
      throw new IllegalStateException ("Failed to create SSL SocketFactory");

    final Registry <ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory> create ()
                                                                  .register ("http",
                                                                             PlainConnectionSocketFactory.getSocketFactory ())
                                                                  .register ("https", aSSLFactory)
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
  public HttpRoutePlanner createRoutePlanner ()
  {
    return new DefaultRoutePlanner (DefaultSchemePortResolver.INSTANCE);
  }

  @Nonnull
  public HttpClientBuilder createHttpClientBuilder ()
  {
    final HttpClientConnectionManager aConnMgr = createConnectionManager ();
    final RequestConfig aRequestConfig = createRequestConfig ();
    final HttpRoutePlanner aRoutePlanner = createRoutePlanner ();
    final HttpHost aProxy = createProxyHost ();

    return HttpClientBuilder.create ()
                            .setConnectionManager (aConnMgr)
                            .setDefaultRequestConfig (aRequestConfig)
                            .setProxy (aProxy)
                            .setRoutePlanner (aRoutePlanner);
  }

  @Nonnull
  public CloseableHttpClient createHttpClient ()
  {
    return createHttpClientBuilder ().build ();
  }
}
