/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.xservlet;

import java.io.IOException;

import com.helger.annotation.style.OverrideOnDemand;
import com.helger.base.state.EContinue;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Abstract XFilter implementation using {@link UnifiedResponse} objects.
 *
 * @author Philip Helger
 * @since 9.0.3
 */
public abstract class AbstractXFilterUnifiedResponse extends AbstractXFilter
{
  protected AbstractXFilterUnifiedResponse ()
  {}

  /**
   * Create a unified response object. This can be used to provide a custom implementation instead.
   *
   * @param eHttpVersion
   *        HTTP version of the current request.
   * @param eHttpMethod
   *        HTTP method of the current request.
   * @param aHttpRequest
   *        Current HTTP request
   * @param aRequestScope
   *        Current HTTP request scope
   * @return The new unified response. Never <code>null</code>.
   * @since 11.1.1
   */
  @Nonnull
  @OverrideOnDemand
  protected UnifiedResponse createUnifiedResponse (@Nonnull final EHttpVersion eHttpVersion,
                                                   @Nonnull final EHttpMethod eHttpMethod,
                                                   @Nonnull final HttpServletRequest aHttpRequest,
                                                   @Nonnull final IRequestWebScope aRequestScope)
  {
    return new UnifiedResponse (eHttpVersion, eHttpMethod, aHttpRequest);
  }

  /**
   * Overwrite this method to fill your response.
   *
   * @param aRequestScope
   *        The request scope to use. There is no direct access to the {@link HttpServletResponse}.
   *        Everything must be handled with the unified response! Never <code>null</code>.
   * @param aUnifiedResponse
   *        The response object to be filled. Never <code>null</code>.
   * @return If {@link EContinue#BREAK} is returned, the content of the unified response is rendered
   *         to the HTTP servlet response and the filter chain stops. On {@link EContinue#CONTINUE}
   *         the content of the unified response is discarded and the filter chain continues as
   *         normal.
   * @throws IOException
   *         In case of an error
   * @throws ServletException
   *         In case of an error
   */
  @Nonnull
  protected abstract EContinue onFilterBefore (@Nonnull IRequestWebScopeWithoutResponse aRequestScope,
                                               @Nonnull UnifiedResponse aUnifiedResponse) throws IOException,
                                                                                          ServletException;

  @Override
  @Nonnull
  @OverrideOnDemand
  public final EContinue onFilterBefore (@Nonnull final HttpServletRequest aHttpRequest,
                                         @Nonnull final HttpServletResponse aHttpResponse,
                                         @Nonnull final IRequestWebScope aRequestScope) throws IOException,
                                                                                        ServletException
  {
    // Check HTTP version
    final EHttpVersion eHTTPVersion = RequestHelper.getHttpVersion (aHttpRequest);
    if (eHTTPVersion == null)
    {
      aHttpResponse.sendError (HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
      return EContinue.BREAK;
    }

    // Check HTTP Method
    final EHttpMethod eHTTPMethod = RequestHelper.getHttpMethod (aHttpRequest);
    if (eHTTPMethod == null)
    {
      if (eHTTPVersion.is10 ())
        aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
      else
        aHttpResponse.sendError (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return EContinue.BREAK;
    }

    // Start unified response handling
    final UnifiedResponse aUnifiedResponse = createUnifiedResponse (eHTTPVersion,
                                                                    eHTTPMethod,
                                                                    aHttpRequest,
                                                                    aRequestScope);
    if (onFilterBefore (aRequestScope, aUnifiedResponse).isBreak ())
    {
      // Filter ended chain -> send response
      aUnifiedResponse.applyToResponse (aHttpResponse);
      return EContinue.BREAK;
    }

    // Filter passed, without any output -> continue
    // Discard the content of the unified response
    return EContinue.CONTINUE;
  }
}
