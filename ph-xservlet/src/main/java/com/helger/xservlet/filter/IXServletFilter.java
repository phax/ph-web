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
package com.helger.xservlet.filter;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.state.EContinue;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.web.scope.IRequestWebScope;

/**
 * Filter for a single XServlet. It has methods for before and after.
 *
 * @author Philip Helger
 * @since 8.0.0
 */
public interface IXServletFilter extends Serializable
{
  /**
   * Invoked before an XServlet request is handled.
   *
   * @param aHttpRequest
   *        HTTP servlet request. Never <code>null</code>.
   * @param aHttpResponse
   *        HTTP servlet response. Never <code>null</code>.
   * @param eHttpVersion
   *        HTTP version. Never <code>null</code>.
   * @param eHttpMethod
   *        HTTP method. Never <code>null</code>.
   * @param aRequestScope
   *        Request scope. Never <code>null</code>.
   * @return {@link EContinue#CONTINUE} to continue processing, or
   *         {@link EContinue#BREAK} if this request should not be processed, in
   *         which case the HttpServletResponse must contain a valid response!
   * @exception ServletException
   *            in case of business logic error
   * @throws IOException
   *         in case of IO error
   */
  @Nonnull
  default EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                   @Nonnull final HttpServletResponse aHttpResponse,
                                   @Nonnull final EHttpVersion eHttpVersion,
                                   @Nonnull final EHttpMethod eHttpMethod,
                                   @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    return EContinue.CONTINUE;
  }

  /**
   * Invoked after an XServlet request was handled. After is always called, even
   * if before request was canceled!
   *
   * @param aHttpRequest
   *        HTTP servlet request. Never <code>null</code>.
   * @param aHttpResponse
   *        HTTP servlet response. Never <code>null</code>.
   * @param eHttpVersion
   *        HTTP version. Never <code>null</code>.
   * @param eHttpMethod
   *        HTTP method. Never <code>null</code>.
   * @param aRequestScope
   *        Request scope. Never <code>null</code>.
   * @exception ServletException
   *            in case of business logic error
   * @throws IOException
   *         in case of IO error
   */
  default void afterRequest (@Nonnull final HttpServletRequest aHttpRequest,
                             @Nonnull final HttpServletResponse aHttpResponse,
                             @Nonnull final EHttpVersion eHttpVersion,
                             @Nonnull final EHttpMethod eHttpMethod,
                             @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {}
}
