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
package com.helger.xservlet.filter;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.EContinue;
import com.helger.http.CHttpHeader;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.response.EXFrameOptionType;
import com.helger.url.ISimpleURL;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Avoid Clickjacking attack using the 'X-Frame-Options' HTTP header.
 *
 * @author Philip Helger
 * @since 9.1.1
 */
public class XServletFilterSecurityXFrameOptions implements IXServletLowLevelFilter
{
  private final EXFrameOptionType m_eType;
  private final ISimpleURL m_aDomain;
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
  public XServletFilterSecurityXFrameOptions (@Nonnull final EXFrameOptionType eType, @Nullable final ISimpleURL aDomain)
  {
    ValueEnforcer.notNull (eType, "Type");
    if (eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    m_eType = eType;
    m_aDomain = aDomain;
    if (eType.isURLRequired ())
      m_sHeaderValue = eType.getID () + " " + aDomain.getAsString ();
    else
      m_sHeaderValue = eType.getID ();
  }

  /**
   * @return The X-Frame-Options type provided in the constructor. Never
   *         <code>null</code>.
   * @since 9.3.2
   */
  @Nonnull
  public final EXFrameOptionType getXFrameOptionsType ()
  {
    return m_eType;
  }

  /**
   * @return The domain passed in the constructor. May be <code>null</code>. Is
   *         not <code>null</code> if the {@link #getXFrameOptionsType()}
   *         requires a URL.
   * @since 9.3.2
   */
  @Nullable
  public final ISimpleURL getDomain ()
  {
    return m_aDomain;
  }

  /**
   * @return The header values to be used. Neither <code>null</code> nor empty.
   * @since 9.3.2
   */
  @Nonnull
  @Nonempty
  public final String getHeaderValue ()
  {
    return m_sHeaderValue;
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
