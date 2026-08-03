/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
package com.helger.servlet.async;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.SafeHttpServletRequest;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Extended {@link AsyncContext} type.
 *
 * @author Philip Helger
 * @since 8.8.0
 */
public class ExtAsyncContext
{
  private final AsyncContext m_aAsyncContext;
  private final EHttpVersion m_eHttpVersion;
  private final EHttpMethod m_eHttpMethod;
  private final String m_sContextPath;
  private final String m_sPathInfo;
  private final String m_sQueryString;
  private final String m_sRequestURI;
  private final String m_sServletPath;
  private boolean m_bSetAttrs = false;

  public ExtAsyncContext (@NonNull final AsyncContext aAsyncContext,
                          @NonNull final EHttpVersion eHttpVersion,
                          @NonNull final EHttpMethod eHttpMethod,
                          @Nullable final String sContextPath,
                          @Nullable final String sPathInfo,
                          @Nullable final String sQueryString,
                          @Nullable final String sRequestURI,
                          @Nullable final String sServletPath)
  {
    m_aAsyncContext = aAsyncContext;
    m_eHttpVersion = eHttpVersion;
    m_eHttpMethod = eHttpMethod;
    m_sContextPath = sContextPath;
    m_sPathInfo = sPathInfo;
    m_sQueryString = sQueryString;
    m_sRequestURI = sRequestURI;
    m_sServletPath = sServletPath;
  }

  @NonNull
  protected AsyncContext getAsyncContext ()
  {
    return m_aAsyncContext;
  }

  @NonNull
  public EHttpVersion getHTTPVersion ()
  {
    return m_eHttpVersion;
  }

  @NonNull
  public EHttpMethod getHTTPMethod ()
  {
    return m_eHttpMethod;
  }

  @NonNull
  public HttpServletRequest getRequest ()
  {
    // Wrap to be safe against "recycled facade" errors (see
    // SafeHttpServletRequest)
    final SafeHttpServletRequest ret = SafeHttpServletRequest.wrap ((HttpServletRequest) m_aAsyncContext.getRequest ());
    if (!m_bSetAttrs)
      if (false)
      {
        ret.setAttribute (AsyncContext.ASYNC_CONTEXT_PATH, m_sContextPath);
        ret.setAttribute (AsyncContext.ASYNC_PATH_INFO, m_sPathInfo);
        ret.setAttribute (AsyncContext.ASYNC_QUERY_STRING, m_sQueryString);
        ret.setAttribute (AsyncContext.ASYNC_REQUEST_URI, m_sRequestURI);
        ret.setAttribute (AsyncContext.ASYNC_SERVLET_PATH, m_sServletPath);
        m_bSetAttrs = true;
      }
    return ret;
  }

  @NonNull
  public HttpServletResponse getResponse ()
  {
    return (HttpServletResponse) m_aAsyncContext.getResponse ();
  }

  public void start (@NonNull final Runnable r)
  {
    m_aAsyncContext.start (r);
  }

  public void complete ()
  {
    m_aAsyncContext.complete ();
  }

  @NonNull
  public static ExtAsyncContext create (@NonNull final HttpServletRequest aHttpRequest,
                                        @NonNull final HttpServletResponse aHttpResponse,
                                        @NonNull final EHttpVersion eHttpVersion,
                                        @NonNull final EHttpMethod eHttpMethod,
                                        @NonNull final ServletAsyncSpec aAsyncSpec)
  {
    final SafeHttpServletRequest aSafeHttpRequest = SafeHttpServletRequest.wrap (aHttpRequest);

    final AsyncContext aAsyncContext = aSafeHttpRequest.startAsync (aSafeHttpRequest, aHttpResponse);
    aAsyncSpec.applyToAsyncContext (aAsyncContext);
    return new ExtAsyncContext (aAsyncContext,
                                eHttpVersion,
                                eHttpMethod,
                                aSafeHttpRequest.getContextPath (),
                                aSafeHttpRequest.getPathInfo (),
                                aSafeHttpRequest.getQueryString (),
                                aSafeHttpRequest.getRequestURI (),
                                aSafeHttpRequest.getServletPath ());
  }
}
