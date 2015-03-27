/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.scopes.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import com.helger.commons.scopes.domain.IGlobalScope;
import com.helger.web.scopes.IWebScope;

/**
 * Interface for a global web scope.
 *
 * @author Philip Helger
 */
public interface IGlobalWebScope extends IGlobalScope, IWebScope
{
  @Nullable
  IApplicationWebScope getApplicationScope (String sAppID, boolean bCreateIfNotExisting);

  /**
   * @return The underlying servlet context. Never <code>null</code>.
   */
  @Nonnull
  ServletContext getServletContext ();

  /**
   * Manually change the context path to be used. Normally there is no need to
   * call the method, because the context path is automatically determined from
   * the {@link ServletContext} or from the underlying request. This method is
   * only needed, if a web application is proxied by e.g. an Apache httpd and
   * the context path between httpd and Java web application server is
   * different!
   *
   * @param sContextPath
   *        The context path of the web application, or "" for the default
   *        (root) context. May not be <code>null</code>.
   */
  void setCustomContextPath (@Nonnull String sContextPath);

  /**
   * @return <code>true</code> if a custom context path was set.
   * @see #setCustomContextPath(String)
   */
  boolean hasCustomContextPath ();

  /**
   * Returns the context path of the web application.
   * <p>
   * The context path is the portion of the request URI that is used to select
   * the context of the request. The context path always comes first in a
   * request URI. The path starts with a "/" character but does not end with a
   * "/" character. For servlets in the default (root) context, this method
   * returns "".
   * <p>
   * It is possible that a servlet container may match a context by more than
   * one context path. In such cases the context path will return the actual
   * context path used by the request and it may differ from the path returned
   * by this method. The context path returned by this method should be
   * considered as the prime or preferred context path of the application.
   *
   * @return The context path of the web application, or "" for the default
   *         (root) context
   */
  @Nonnull
  String getContextPath ();
}
