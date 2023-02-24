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

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.state.EContinue;
import com.helger.http.EHttpReferrerPolicy;
import com.helger.http.EHttpVersion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Always set the correct "Referrer-Policy" header.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletFilterSecurityHttpReferrerPolicy implements IXServletLowLevelFilter
{
  private final EHttpReferrerPolicy m_eHttpReferrerPolicy;

  public XServletFilterSecurityHttpReferrerPolicy (@Nonnull final EHttpReferrerPolicy eHttpReferrerPolicy)
  {
    m_eHttpReferrerPolicy = ValueEnforcer.notNull (eHttpReferrerPolicy, "HttpReferrerPolicy");
  }

  /**
   * @return The "Referrer-Policy" as provided in the constructor. Never
   *         <code>null</code>.
   */
  @Nonnull
  public final EHttpReferrerPolicy getHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy;
  }

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod)
  {
    // Ensure the response header is present
    aHttpResponse.addHeader (CHttpHeader.REFERRER_POLICY, m_eHttpReferrerPolicy.getValue ());
    return EContinue.CONTINUE;
  }
}
