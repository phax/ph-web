/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.functional.ISupplier;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpVersion;
import com.helger.web.scope.IRequestWebScope;

/**
 * Called by the server (via the <code>service</code> method) to allow a servlet
 * to handle a OPTIONS request. An OPTIONS request returns the allowed HTTP
 * methods supported by the servlet in the ALLOW HTTP response header.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletHandlerOPTIONS implements IXServletHandler
{
  private final ISupplier <String> m_aAllowProvider;

  /**
   * Constructor
   *
   * @param aAllowProvider
   *        The supplier to use. Must be a supplier, because the underlying
   *        "Allow" string can change at runtime!
   */
  public XServletHandlerOPTIONS (@Nonnull final ISupplier <String> aAllowProvider)
  {
    m_aAllowProvider = ValueEnforcer.notNull (aAllowProvider, "AllowProvider");
  }

  public void onRequest (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final EHttpVersion eHTTPVersion,
                         @Nonnull final EHttpMethod eHTTPMethod,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    // Build Allow response header - that's it
    aHttpResponse.setHeader (CHttpHeader.ALLOW, m_aAllowProvider.get ());
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).getToString ();
  }
}
