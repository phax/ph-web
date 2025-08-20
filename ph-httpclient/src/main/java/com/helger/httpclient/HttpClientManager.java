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

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.io.stream.StreamHelper;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A small wrapper around {@link CloseableHttpClient}.
 *
 * @author Philip Helger
 */
public class HttpClientManager implements AutoCloseable
{
  private CloseableHttpClient m_aHttpClient;

  public HttpClientManager ()
  {
    this (new HttpClientFactory ());
  }

  public HttpClientManager (@Nonnull final IHttpClientProvider aHttpClientSupplier)
  {
    ValueEnforcer.notNull (aHttpClientSupplier, "HttpClientSupplier");
    m_aHttpClient = aHttpClientSupplier.createHttpClient ();
    if (m_aHttpClient == null)
      throw new IllegalArgumentException ("The provided HttpClient factory created an invalid (null) HttpClient!");
  }

  public void close ()
  {
    StreamHelper.close (m_aHttpClient);
    m_aHttpClient = null;
  }

  /**
   * @return <code>true</code> if this manager is already closed, and no further
   *         requests can be executed, <code>false</code> if this manager is not
   *         yet closed.
   * @since 8.8.2
   */
  public final boolean isClosed ()
  {
    return m_aHttpClient == null;
  }

  protected final void checkIfClosed ()
  {
    if (isClosed ())
      throw new IllegalStateException ("This HttpClientManager was already closed!");
  }

  /**
   * Execute the provided request without any special context. The response
   * handler is invoked as a callback. This method automatically cleans up all
   * used resources and as such is preferred over the execute methods returning
   * the CloseableHttpResponse.
   *
   * @param aRequest
   *        The request to be executed. May not be <code>null</code>.
   * @param aResponseHandler
   *        The response handler to be executed. May not be <code>null</code>.
   * @return The evaluated response of the response handler. May be
   *         <code>null</code>.
   * @throws IOException
   *         In case of error
   * @throws IllegalStateException
   *         If this manager was already closed!
   * @param <T>
   *        return type
   */
  @Nullable
  public <T> T execute (@Nonnull final HttpUriRequest aRequest,
                        @Nonnull final HttpClientResponseHandler <? extends T> aResponseHandler) throws IOException
  {
    return execute (aRequest, (HttpClientContext) null, aResponseHandler);
  }

  /**
   * Execute the provided request with an optional special context. The response
   * handler is invoked as a callback. This method automatically cleans up all
   * used resources and as such is preferred over the execute methods returning
   * the CloseableHttpResponse.
   *
   * @param aRequest
   *        The request to be executed. May not be <code>null</code>.
   * @param aHttpContext
   *        The optional context to be used. May be <code>null</code> to
   * @param aResponseHandler
   *        The response handler to be executed. May not be <code>null</code>.
   * @return The evaluated response of the response handler. May be
   *         <code>null</code>.
   * @throws IOException
   *         In case of error
   * @throws IllegalStateException
   *         If this manager was already closed!
   * @param <T>
   *        return type
   */
  @Nullable
  public <T> T execute (@Nonnull final ClassicHttpRequest aRequest,
                        @Nullable final HttpContext aHttpContext,
                        @Nonnull final HttpClientResponseHandler <? extends T> aResponseHandler) throws IOException
  {
    checkIfClosed ();
    HttpDebugger.beforeRequest (aRequest, aHttpContext);
    T ret = null;
    Exception aCaughtException = null;
    try
    {
      ret = m_aHttpClient.execute (aRequest, aHttpContext, aResponseHandler);
      return ret;
    }
    catch (final RuntimeException | IOException ex)
    {
      aCaughtException = ex;
      throw ex;
    }
    finally
    {
      HttpDebugger.afterRequest (aRequest, ret, aCaughtException);
    }
  }

  @Nonnull
  public static HttpClientManager create (@Nonnull final HttpClientSettings aHttpClientSettings)
  {
    return new HttpClientManager (new HttpClientFactory (aHttpClientSettings));
  }
}
