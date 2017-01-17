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
    this (new HttpClientFactory ());
  }

  @Deprecated
  public HttpClientManager (@Nonnull final Supplier <? extends CloseableHttpClient> aHttpClientSupplier)
  {
    this ((IHttpClientProvider) () -> aHttpClientSupplier.get ());
  }

  public HttpClientManager (@Nonnull final IHttpClientProvider aHttpClientSupplier)
  {
    ValueEnforcer.notNull (aHttpClientSupplier, "HttpClientSupplier");
    m_aHttpClient = aHttpClientSupplier.createHttpClient ();
    if (m_aHttpClient == null)
      throw new IllegalArgumentException ("The provided HttpClient factory created an invalid HttpClient!");
  }

  public void close () throws IOException
  {
    StreamHelper.close (m_aHttpClient);
    m_aHttpClient = null;
  }

  private void _checkClosed ()
  {
    if (m_aHttpClient == null)
      throw new IllegalStateException ("This HttpClientManager was already closed!");
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
    _checkClosed ();
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
    _checkClosed ();
    HttpDebugger.beforeRequest (aRequest, aHttpContext);
    return m_aHttpClient.execute (aRequest, aResponseHandler, aHttpContext);
  }
}
