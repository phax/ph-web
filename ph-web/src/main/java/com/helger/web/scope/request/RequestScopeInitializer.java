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
package com.helger.web.scope.request;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.mgr.WebScopeManager;
import com.helger.web.scope.multipart.RequestWebScopeMultipart;

/**
 * Internal class from scope aware filter and servlets.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class RequestScopeInitializer implements AutoCloseable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RequestScopeInitializer.class);

  private final IRequestWebScope m_aRequestScope;
  private boolean m_bCreatedIt;

  /**
   * Ctor.
   *
   * @param aRequestScope
   *        The request scope to be used. May not be <code>null</code>.
   * @param bCreatedIt
   *        <code>true</code> if the request scope was newly created,
   *        <code>false</code> if an existing request web scope is reused.
   */
  private RequestScopeInitializer (@Nonnull final IRequestWebScope aRequestScope, final boolean bCreatedIt)
  {
    m_aRequestScope = ValueEnforcer.notNull (aRequestScope, "RequestScope");
    m_bCreatedIt = bCreatedIt;
  }

  /**
   * @return The request web scope to be used.
   */
  @Nonnull
  public IRequestWebScope getRequestScope ()
  {
    return m_aRequestScope;
  }

  public boolean isNew ()
  {
    return m_bCreatedIt;
  }

  public void internalSetDontDestroyRequestScope ()
  {
    m_bCreatedIt = false;
  }

  /**
   * Destroy the current request scope if it was initialized here.
   */
  public void close ()
  {
    if (m_bCreatedIt)
    {
      // End the scope after the complete filtering process (if it was
      // created)
      WebScopeManager.onRequestEnd ();
    }
  }

  @Nonnull
  public static RequestScopeInitializer createMultipart (@Nonnull final HttpServletRequest aHttpRequest,
                                                         @Nonnull final HttpServletResponse aHttpResponse)
  {
    return create (aHttpRequest, aHttpResponse, RequestWebScopeMultipart::new);
  }

  @Nonnull
  public static RequestScopeInitializer create (@Nonnull final HttpServletRequest aHttpRequest,
                                                @Nonnull final HttpServletResponse aHttpResponse,
                                                @Nonnull final BiFunction <? super HttpServletRequest, ? super HttpServletResponse, IRequestWebScope> aFactory)
  {
    // Check if a request scope is already present
    final IRequestWebScope aExistingRequestScope = WebScopeManager.getRequestScopeOrNull ();
    if (aExistingRequestScope != null)
    {
      // A scope is already present - e.g. from a scope aware filter

      // Check if scope is in destruction or destroyed!
      if (aExistingRequestScope.isValid ())
      {
        return new RequestScopeInitializer (aExistingRequestScope, false);
      }

      // Wow...
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("The existing request scope is no longer valid - creating a new one: " + aExistingRequestScope.toString ());
    }

    // No valid scope present
    // -> create a new scope
    final IRequestWebScope aRequestScope = WebScopeManager.onRequestBegin (aHttpRequest, aHttpResponse, aFactory);
    return new RequestScopeInitializer (aRequestScope, true);
  }
}
