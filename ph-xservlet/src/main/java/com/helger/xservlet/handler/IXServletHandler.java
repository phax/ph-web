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
package com.helger.xservlet.handler;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.web.scope.IRequestWebScope;

/**
 * Handler for a single HTTP method in an XServlet.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@FunctionalInterface
public interface IXServletHandler extends Serializable
{
  /**
   * Handle the servlet action for a certain request and response.
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
   * @throws ServletException
   *         On business error
   * @throws IOException
   *         On IO error
   */
  void onRequest (@Nonnull HttpServletRequest aHttpRequest,
                  @Nonnull HttpServletResponse aHttpResponse,
                  @Nonnull EHttpVersion eHttpVersion,
                  @Nonnull EHttpMethod eHttpMethod,
                  @Nonnull IRequestWebScope aRequestScope) throws ServletException, IOException;
}
