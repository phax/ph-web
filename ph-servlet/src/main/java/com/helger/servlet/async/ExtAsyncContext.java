/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extended {@link AsyncContext} type.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public class ExtAsyncContext
{
  private final AsyncContext m_aAsyncContext;
  private final String m_sContextPath;
  private final String m_sPathInfo;
  private final String m_sQueryString;
  private final String m_sRequestURI;
  private final String m_sServletPath;
  private boolean m_bSetAttrs = false;

  public ExtAsyncContext (@Nonnull final AsyncContext aAsyncContext,
                          @Nullable final String sContextPath,
                          @Nullable final String sPathInfo,
                          @Nullable final String sQueryString,
                          @Nullable final String sRequestURI,
                          @Nullable final String sServletPath)
  {
    m_aAsyncContext = aAsyncContext;
    m_sContextPath = sContextPath;
    m_sPathInfo = sPathInfo;
    m_sQueryString = sQueryString;
    m_sRequestURI = sRequestURI;
    m_sServletPath = sServletPath;
  }

  @Nonnull
  protected AsyncContext getAsyncContext ()
  {
    return m_aAsyncContext;
  }

  @Nonnull
  public HttpServletRequest getRequest ()
  {
    final HttpServletRequest ret = (HttpServletRequest) m_aAsyncContext.getRequest ();
    if (!m_bSetAttrs && false)
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

  @Nonnull
  public HttpServletResponse getResponse ()
  {
    return (HttpServletResponse) m_aAsyncContext.getResponse ();
  }

  public void complete ()
  {
    m_aAsyncContext.complete ();
  }

  @Nonnull
  public static ExtAsyncContext create (@Nonnull final HttpServletRequest aHttpRequest,
                                        @Nonnull final HttpServletResponse aHttpResponse,
                                        @Nonnull final ServletAsyncSpec aAsyncSpec)
  {
    final AsyncContext aAsyncContext = aHttpRequest.startAsync (aHttpRequest, aHttpResponse);
    aAsyncSpec.applyToAsyncContext (aAsyncContext);
    return new ExtAsyncContext (aAsyncContext,
                                aHttpRequest.getContextPath (),
                                aHttpRequest.getPathInfo (),
                                aHttpRequest.getQueryString (),
                                aHttpRequest.getRequestURI (),
                                aHttpRequest.getServletPath ());
  }
}
