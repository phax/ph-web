/**
 * Copyright (C) 2016-2018 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.debug.GlobalDebug;

/**
 * Some debugging for HTTP requests. Currently used in
 * {@link HttpClientManager}.
 *
 * @author Philip Helger
 */
@Immutable
public final class HttpDebugger
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (HttpDebugger.class);
  private static final AtomicBoolean s_aEnabled = new AtomicBoolean (GlobalDebug.isDebugMode ());

  private HttpDebugger ()
  {}

  public static boolean isEnabled ()
  {
    return s_aEnabled.get ();
  }

  public static void setEnabled (final boolean bEnabled)
  {
    s_aEnabled.set (bEnabled);
  }

  /**
   * Call before an invocation
   *
   * @param aRequest
   *        The request to be executed. May not be <code>null</code>.
   * @param aHttpContext
   *        The special HTTP content for this call. May be <code>null</code>.
   */
  public static void beforeRequest (@Nonnull final HttpUriRequest aRequest, @Nullable final HttpContext aHttpContext)
  {
    if (isEnabled ())
      s_aLogger.info ("Before HTTP call: " +
                      aRequest.getMethod () +
                      " " +
                      aRequest.getURI () +
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
  public static void afterRequest (@Nonnull final HttpUriRequest aRequest,
                                   @Nullable final Object aResponse,
                                   @Nullable final Throwable aCaughtException)
  {
    if (isEnabled ())
    {
      final HttpResponseException aHex = aCaughtException instanceof HttpResponseException ? (HttpResponseException) aCaughtException
                                                                                           : null;
      s_aLogger.info ("After HTTP call: " +
                      aRequest.getMethod () +
                      (aResponse != null ? ". Response: " + aResponse : "") +
                      (aHex != null ? ". Status " + aHex.getStatusCode () : ""),
                      aHex != null ? null : aCaughtException);
    }
  }
}
