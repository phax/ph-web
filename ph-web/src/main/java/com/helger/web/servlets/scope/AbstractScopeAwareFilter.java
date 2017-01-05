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
package com.helger.web.servlets.scope;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.state.EContinue;
import com.helger.commons.string.StringHelper;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.request.RequestScopeInitializer;

/**
 * Abstract HTTP servlet filter implementation using the correct scope handling.
 * The scope initialization happens before the main action is executed, and the
 * scope destruction happens after <b>all</b> the whole filter chain finished!
 * If more than one scope aware filter are present in the filter chain, only the
 * filter invoked first creates the request scope. Succeeding scope aeware
 * filters wont create a request scope.
 *
 * @author Philip Helger
 */
public abstract class AbstractScopeAwareFilter implements Filter
{
  private String m_sApplicationID;

  /**
   * Determine the application ID to be used, based on the passed filter
   * configuration. This method is only invoked once on startup.
   *
   * @param aFilterConfig
   *        The filter configuration
   * @return The application ID for this filter.
   */
  @OverrideOnDemand
  protected String getApplicationID (@Nonnull final FilterConfig aFilterConfig)
  {
    return ClassHelper.getClassLocalName (getClass ());
  }

  /**
   * Initialize the filter
   *
   * @param aFilterConfig
   *        Filter configuration from servlet container
   * @throws ServletException
   *         If something goes wrong
   */
  @OverrideOnDemand
  protected void onInit (@Nonnull final FilterConfig aFilterConfig) throws ServletException
  {}

  public final void init (@Nonnull final FilterConfig aFilterConfig) throws ServletException
  {
    m_sApplicationID = getApplicationID (aFilterConfig);
    if (StringHelper.hasNoText (m_sApplicationID))
      throw new InitializationException ("Failed retrieve a valid application ID!");
    onInit (aFilterConfig);
  }

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
  protected abstract EContinue doFilter (@Nonnull HttpServletRequest aHttpRequest,
                                         @Nonnull HttpServletResponse aHttpResponse,
                                         @Nonnull IRequestWebScope aRequestScope) throws IOException, ServletException;

  public final void doFilter (final ServletRequest aRequest,
                              final ServletResponse aResponse,
                              final FilterChain aChain) throws IOException, ServletException
  {
    if (aRequest instanceof HttpServletRequest && aResponse instanceof HttpServletResponse)
    {
      final HttpServletRequest aHttpRequest = (HttpServletRequest) aRequest;
      final HttpServletResponse aHttpResponse = (HttpServletResponse) aResponse;

      // Check if a scope needs to be created
      final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (m_sApplicationID,
                                                                                               aHttpRequest,
                                                                                               aHttpResponse);
      try
      {
        // Apply any optional filter
        if (doFilter (aHttpRequest, aHttpResponse, aRequestScopeInitializer.getRequestScope ()).isContinue ())
        {
          // Continue as usual
          aChain.doFilter (aRequest, aResponse);
        }
      }
      finally
      {
        // And destroy the scope depending on its creation state
        aRequestScopeInitializer.destroyScope ();
      }
    }
    else
    {
      // No scope handling
      aChain.doFilter (aRequest, aResponse);
    }
  }

  @OverrideOnDemand
  public void destroy ()
  {}
}
