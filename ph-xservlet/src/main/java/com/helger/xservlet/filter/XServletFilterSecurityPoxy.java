/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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
package com.helger.xservlet.filter;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.state.EContinue;
import com.helger.http.EHttpVersion;
import com.helger.servlet.request.RequestLogger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Avoid Httpoxy attack using the 'Proxy' HTTP header
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletFilterSecurityPoxy implements IXServletLowLevelFilter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XServletFilterSecurityPoxy.class);
  public static final XServletFilterSecurityPoxy INSTANCE = new XServletFilterSecurityPoxy ();

  protected XServletFilterSecurityPoxy ()
  {}

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod) throws IOException
  {
    final String sPoxy = aHttpRequest.getHeader (CHttpHeader.PROXY);
    if (sPoxy != null)
    {
      // potentially malicious request - log and block
      LOGGER.warn ("httpoxy request successfully blocked: " + aHttpRequest);
      RequestLogger.logRequestComplete (aHttpRequest);
      aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
      return EContinue.BREAK;
    }
    // Continue with request
    return EContinue.CONTINUE;
  }
}
