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

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.stream.StreamHelper;

/**
 * A small wrapper around {@link CloseableHttpClient}.
 *
 * @author Philip Helger
 */
public class HttpClientManager implements Closeable
{
  private CloseableHttpClient m_aHttpClient;

  public HttpClientManager ()
  {
    this ( () -> new HttpClientFactory ().createHttpClient ());
  }

  public HttpClientManager (@Nonnull final Supplier <CloseableHttpClient> aHttpClientSupplier)
  {
    ValueEnforcer.notNull (aHttpClientSupplier, "HttpClientSupplier");
    m_aHttpClient = aHttpClientSupplier.get ();
  }

  public void close () throws IOException
  {
    StreamHelper.close (m_aHttpClient);
    m_aHttpClient = null;
  }

  @Nonnull
  public CloseableHttpResponse execute (@Nonnull final HttpUriRequest aRequest) throws IOException
  {
    return execute (aRequest, (HttpContext) null);
  }

  @Nonnull
  public CloseableHttpResponse execute (@Nonnull final HttpUriRequest aRequest,
                                        @Nullable final HttpContext aHttpContext) throws IOException
  {
    HttpDebugger.beforeRequest (aRequest, aHttpContext);
    return m_aHttpClient.execute (aRequest, aHttpContext);
  }

  @Nullable
  public <T> T execute (@Nonnull final HttpUriRequest aRequest,
                        @Nonnull final ResponseHandler <T> aResponseHandler) throws IOException
  {
    return execute (aRequest, (HttpContext) null, aResponseHandler);
  }

  @Nullable
  public <T> T execute (@Nonnull final HttpUriRequest aRequest,
                        @Nullable final HttpContext aHttpContext,
                        @Nonnull final ResponseHandler <T> aResponseHandler) throws IOException
  {
    HttpDebugger.beforeRequest (aRequest, aHttpContext);
    return m_aHttpClient.execute (aRequest, aResponseHandler, aHttpContext);
  }
}
