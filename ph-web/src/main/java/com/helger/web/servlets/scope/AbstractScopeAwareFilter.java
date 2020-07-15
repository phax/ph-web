/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.web.servlets.scope;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.state.EContinue;
import com.helger.servlet.filter.AbstractHttpServletFilter;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.request.RequestScopeInitializer;

/**
 * Abstract HTTP servlet filter implementation using the correct scope handling.
 * The scope initialization happens before the main action is executed, and the
 * scope destruction happens after <b>all</b> the whole filter chain finished!
 * If more than one scope aware filter are present in the filter chain, only the
 * filter invoked first creates the request scope. Succeeding scope aware
 * filters wont create a request scope.
 *
 * @author Philip Helger
 */
public abstract class AbstractScopeAwareFilter extends AbstractHttpServletFilter
{
  protected AbstractScopeAwareFilter ()
  {}

  /**
   * Implement this main filtering method in subclasses.
   *
   * @param aHttpRequest
   *        The HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used.
   * @return {@link EContinue#CONTINUE} to indicate that the next filter is to
   *         be called or {@link EContinue#BREAK} to indicate that the next
   *         filter does not need to be called! Never return <code>null</code>!
   * @throws IOException
   *         In case of an IO error
   * @throws ServletException
   *         For non IO errors
   */
  @Nonnull
  protected abstract EContinue doHttpFilter (@Nonnull HttpServletRequest aHttpRequest,
                                             @Nonnull HttpServletResponse aHttpResponse,
                                             @Nonnull IRequestWebScope aRequestScope) throws IOException, ServletException;

  @Override
  public final void doHttpFilter (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final FilterChain aChain) throws IOException, ServletException
  {
    // Check if a scope needs to be created
    try (final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.createMultipart (aHttpRequest, aHttpResponse))
    {
      // Apply any optional filter
      if (doHttpFilter (aHttpRequest, aHttpResponse, aRequestScopeInitializer.getRequestScope ()).isContinue ())
      {
        // Continue as usual
        aChain.doFilter (aHttpRequest, aHttpResponse);
      }
    }
  }
}
