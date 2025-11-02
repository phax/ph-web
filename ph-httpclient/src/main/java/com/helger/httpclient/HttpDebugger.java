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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;

/**
 * Some debugging for HTTP requests. Currently used in
 * {@link HttpClientManager}.
 *
 * @author Philip Helger
 */
@Immutable
public final class HttpDebugger
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HttpDebugger.class);
  private static final AtomicBoolean ENABLED = new AtomicBoolean (false);

  private HttpDebugger ()
  {}

  public static boolean isEnabled ()
  {
    return ENABLED.get ();
  }

  public static void setEnabled (final boolean bEnabled)
  {
    ENABLED.set (bEnabled);
  }

  /**
   * Call before an invocation
   *
   * @param aRequest
   *        The request to be executed. May not be <code>null</code>.
   * @param aHttpContext
   *        The special HTTP content for this call. May be <code>null</code>.
   */
  public static void beforeRequest (@NonNull final ClassicHttpRequest aRequest,
                                    @Nullable final HttpContext aHttpContext)
  {
    if (isEnabled ())
      LOGGER.info ("Before HTTP call: " +
                   aRequest.toString () +
                   (aHttpContext != null ? " (with special HTTP context)" : ""));
  }

  /**
   * Call after an invocation.
   *
   * @param aRequest
   *        The source request. May not be modified internally. May not be
   *        <code>null</code>.
   * @param aResponse
   *        The response object retrieved. May be anything including
   *        <code>null</code> (e.g. in case of exception).
   * @param aCaughtException
   *        The caught exception. May be <code>null</code>.
   * @since 8.8.2
   */
  public static void afterRequest (@NonNull final ClassicHttpRequest aRequest,
                                   @Nullable final Object aResponse,
                                   @Nullable final Throwable aCaughtException)
  {
    if (isEnabled ())
    {
      final HttpResponseException aHttpEx = aCaughtException instanceof HttpResponseException ? (HttpResponseException) aCaughtException
                                                                                              : null;
      LOGGER.info ("After HTTP call: " +
                   aRequest.getMethod () +
                   (aResponse != null ? ". Response: " + aResponse : ". No response") +
                   (aHttpEx != null ? ". Status: " + aHttpEx.getStatusCode () : ""),
                   aHttpEx != null ? null : aCaughtException);
    }
  }
}
