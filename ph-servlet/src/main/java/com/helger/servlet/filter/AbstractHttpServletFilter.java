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
package com.helger.servlet.filter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An abstract {@link Filter} implementation that only works with
 * {@link HttpServletRequest} and {@link HttpServletResponse}. All other request
 * and response types are not handled.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public abstract class AbstractHttpServletFilter extends AbstractServletFilter
{
  /**
   * Implement this main filtering method in subclasses.
   *
   * @param aHttpRequest
   *        The HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The HTTP response. Never <code>null</code>.
   * @param aChain
   *        The further filter chain.
   * @throws IOException
   *         In case of an IO error
   * @throws ServletException
   *         For non IO errors
   */
  public abstract void doHttpFilter (@Nonnull HttpServletRequest aHttpRequest,
                                     @Nonnull HttpServletResponse aHttpResponse,
                                     @Nonnull FilterChain aChain) throws IOException, ServletException;

  public final void doFilter (@Nonnull final ServletRequest aRequest,
                              @Nonnull final ServletResponse aResponse,
                              @Nonnull final FilterChain aChain) throws IOException, ServletException
  {
    if (aRequest instanceof HttpServletRequest && aResponse instanceof HttpServletResponse)
    {
      final HttpServletRequest aHttpRequest = (HttpServletRequest) aRequest;
      final HttpServletResponse aHttpResponse = (HttpServletResponse) aResponse;
      doHttpFilter (aHttpRequest, aHttpResponse, aChain);
    }
    else
    {
      // Ignore and continue
      aChain.doFilter (aRequest, aResponse);
    }
  }
}
