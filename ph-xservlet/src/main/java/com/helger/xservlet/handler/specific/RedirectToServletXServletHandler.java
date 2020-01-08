/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
  private final String m_sServletPath;

  /**
   * Constructor.
   *
   * @param sServletPath
   *        The servlet path (relative to the current context) to redirect to.
   *        Must started with a slash ("/").
   */
  public RedirectToServletXServletHandler (@Nonnull @Nonempty final String sServletPath)
  {
    ValueEnforcer.notEmpty (sServletPath, "ServletPath");
    ValueEnforcer.isTrue (sServletPath.startsWith ("/"), "Path must start with '/'!");

    m_sServletPath = sServletPath;
  }

  public void handleRequest (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                             @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    String sRedirectURL = aRequestScope.getContextPath () + m_sServletPath;

    final String sQueryString = aRequestScope.getQueryString ();
    if (StringHelper.hasText (sQueryString))
      sRedirectURL += "?" + sQueryString;

    aUnifiedResponse.setRedirect (sRedirectURL);
  }
}
