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
package com.helger.xservlet.handler.specific;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.StringHelper;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.handler.simple.IXServletSimpleHandler;

/**
 * An {@link IXServletSimpleHandler} that does a redirect to another servlet.
 *
 * @author Philip Helger
 */
public class RedirectToServletXServletHandler implements IXServletSimpleHandler
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RedirectToServletXServletHandler.class);

  private final String m_sServletPath;

  /**
   * Constructor.
   *
   * @param sServletPath
   *        The servlet path (relative to the current context) to redirect to.
   *        Must start with a slash ("/").
   */
  public RedirectToServletXServletHandler (@Nonnull @Nonempty final String sServletPath)
  {
    ValueEnforcer.notEmpty (sServletPath, "ServletPath");
    ValueEnforcer.isTrue (sServletPath.startsWith ("/"), "Path must start with '/'!");

    m_sServletPath = sServletPath;
  }

  /**
   * @return The servlet path as provided in the constructor. Always starts with
   *         a slash. Neither <code>null</code> nor empty.
   * @since 9.3.1
   */
  @Nonnull
  @Nonempty
  public final String getServletPath ()
  {
    return m_sServletPath;
  }

  /**
   * Get the redirect URL to be used.
   *
   * @param aRequestScope
   *        The current request scope to be used. Never <code>null</code>.
   * @return The target URL to redirect to. If it is relative, the application
   *         server is responsible for making it absolute.
   * @since 9.6.3
   */
  @Nonnull
  @OverridingMethodsMustInvokeSuper
  protected String getRedirectURL (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    String sRedirectURL = aRequestScope.getContextPath () + m_sServletPath;

    final String sQueryString = aRequestScope.getQueryString ();
    if (StringHelper.hasText (sQueryString))
      sRedirectURL += "?" + sQueryString;
    return sRedirectURL;
  }

  public void handleRequest (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                             @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    final String sRedirectURL = getRedirectURL (aRequestScope);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Sending redirect to '" + sRedirectURL + "'");

    aUnifiedResponse.setRedirect (sRedirectURL);
  }
}
