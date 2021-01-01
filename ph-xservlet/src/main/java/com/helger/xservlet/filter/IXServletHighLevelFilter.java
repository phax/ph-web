/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import com.helger.web.scope.IRequestWebScope;

/**
 * High-level filter for a single XServlet. It has methods for before and after.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public interface IXServletHighLevelFilter
{
  /**
   * Invoked before an XServlet request is handled. Exceptions occurring in this
   * method will be caught by the exception handler registered in the XServlet.
   *
   * @param aRequestScope
   *        Request scope. Never <code>null</code>.
   * @exception ServletException
   *            in case of business logic error.
   * @throws IOException
   *         in case of IO error.
   */
  void beforeRequest (@Nonnull IRequestWebScope aRequestScope) throws ServletException, IOException;

  /**
   * Invoked after an XServlet request was handled. After is always called, even
   * if before request was canceled (in a finally)! Exceptions occurring in this
   * method will be logged but ignored otherwise!
   *
   * @param aRequestScope
   *        Request scope. Never <code>null</code>.
   * @exception ServletException
   *            in case of business logic error
   * @throws IOException
   *         in case of IO error
   */
  void afterRequest (@Nonnull IRequestWebScope aRequestScope) throws ServletException, IOException;
}
