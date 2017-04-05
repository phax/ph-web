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

import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;

/**
 * Extended {@link AsyncContext} type.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public class ExtAsyncContext2 extends ExtAsyncContext
{
  private final EHTTPVersion m_eHttpVersion;
  private final EHTTPMethod m_eHttpMethod;

  public ExtAsyncContext2 (@Nonnull final AsyncContext aAsyncContext,
                           @Nonnull final EHTTPVersion eHttpVersion,
                           @Nonnull final EHTTPMethod eHttpMethod,
                           @Nullable final String sContextPath,
                           @Nullable final String sPathInfo,
                           @Nullable final String sQueryString,
                           @Nullable final String sRequestURI,
                           @Nullable final String sServletPath)
  {
    super (aAsyncContext, sContextPath, sPathInfo, sQueryString, sRequestURI, sServletPath);
    m_eHttpVersion = eHttpVersion;
    m_eHttpMethod = eHttpMethod;
  }

  @Nonnull
  public EHTTPVersion getHTTPVersion ()
  {
    return m_eHttpVersion;
  }

  @Nonnull
  public EHTTPMethod getHTTPMethod ()
  {
    return m_eHttpMethod;
  }

  @Nonnull
  public static ExtAsyncContext2 create (@Nonnull final HttpServletRequest aHttpRequest,
                                         @Nonnull final HttpServletResponse aHttpResponse,
                                         @Nonnull final EHTTPVersion eHttpVersion,
                                         @Nonnull final EHTTPMethod eHttpMethod,
                                         @Nonnull final ServletAsyncSpec aAsyncSpec)
  {
    final AsyncContext aAsyncContext = aHttpRequest.startAsync (aHttpRequest, aHttpResponse);
    aAsyncSpec.applyToAsyncContext (aAsyncContext);
    return new ExtAsyncContext2 (aAsyncContext,
                                 eHttpVersion,
                                 eHttpMethod,
                                 aHttpRequest.getContextPath (),
                                 aHttpRequest.getPathInfo (),
                                 aHttpRequest.getQueryString (),
                                 aHttpRequest.getRequestURI (),
                                 aHttpRequest.getServletPath ());
  }
}
