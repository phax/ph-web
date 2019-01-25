/**
 * Copyright (C) 2016-2019 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.state.EContinue;
import com.helger.commons.url.ISimpleURL;
import com.helger.http.EHttpVersion;
import com.helger.servlet.response.EXFrameOptionType;

/**
 * Avoid Clickjacking attack using the 'X-Frame-Options' HTTP header.
 *
 * @author Philip Helger
 * @since 9.1.1
 */
public class XServletFilterSecurityXFrameOptions implements IXServletLowLevelFilter
{
  private final String m_sHeaderValue;

  /**
   * The X-Frame-Options HTTP response header can be used to indicate whether or
   * not a browser should be allowed to render a page in a &lt;frame&gt;,
   * &lt;iframe&gt; or &lt;object&gt; . Sites can use this to avoid clickjacking
   * attacks, by ensuring that their content is not embedded into other sites.
   * Example:
   *
   * <pre>
   * X-Frame-Options: DENY
   * X-Frame-Options: SAMEORIGIN
   * X-Frame-Options: ALLOW-FROM https://example.com/
   * </pre>
   *
   * @param eType
   *        The X-Frame-Options type to be set. May not be <code>null</code>.
   * @param aDomain
   *        The domain URL to be used in "ALLOW-FROM". May be <code>null</code>
   *        for the other cases.
   */
  public XServletFilterSecurityXFrameOptions (@Nonnull final EXFrameOptionType eType,
                                              @Nullable final ISimpleURL aDomain)
  {
    ValueEnforcer.notNull (eType, "Type");
    if (eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    if (eType.isURLRequired ())
      m_sHeaderValue = eType.getID () + " " + aDomain.getAsStringWithEncodedParameters ();
    else
      m_sHeaderValue = eType.getID ();
  }

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod)
  {
    // Ensure the response header is present
    aHttpResponse.addHeader (CHttpHeader.X_FRAME_OPTIONS, m_sHeaderValue);
    return EContinue.CONTINUE;
  }
}
