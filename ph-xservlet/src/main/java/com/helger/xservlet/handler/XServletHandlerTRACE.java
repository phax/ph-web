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
package com.helger.xservlet.handler;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.http.CHttp;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.mime.EMimeContentType;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpVersion;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.scope.IRequestWebScope;

/**
 * Called by the server (via the <code>service</code> method) to allow a servlet
 * to handle a TRACE request. A TRACE returns the headers sent with the TRACE
 * request to the client, so that they can be used in debugging. There's no need
 * to override this method.
 *
 * @author Servlet Spec 3.1
 * @since 9.0.0
 */
public class XServletHandlerTRACE implements IXServletHandler
{
  private static final String CONTENT_TYPE = EMimeContentType.MESSAGE.buildMimeType ("http").getAsString ();

  public XServletHandlerTRACE ()
  {}

  public void onRequest (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final EHttpVersion eHTTPVersion,
                         @Nonnull final EHttpMethod eHTTPMethod,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    final StringBuilder aSB = new StringBuilder ().append (EHttpMethod.TRACE.getName ())
                                                  .append (' ')
                                                  .append (ServletHelper.getRequestRequestURI (aHttpRequest))
                                                  .append (' ')
                                                  .append (aHttpRequest.getProtocol ())
                                                  .append (CHttp.EOL);
    RequestHelper.forEachRequestHeader (aHttpRequest,
                                        (sHeaderName,
                                         sHeaderValue) -> aSB.append (sHeaderName).append (": ").append (sHeaderValue).append (CHttp.EOL));

    aHttpResponse.setContentType (CONTENT_TYPE);
    aHttpResponse.setContentLength (aSB.length ());
    aHttpResponse.getOutputStream ().print (aSB.toString ());
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).getToString ();
  }
}
