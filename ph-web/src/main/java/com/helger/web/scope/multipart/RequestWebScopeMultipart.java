/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.web.scope.multipart;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.style.OverrideOnDemand;
import com.helger.base.state.EChange;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.web.scope.impl.RequestWebScope;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The default request web scope that also tries to parse multi part requests if
 * the Content-Type matches .
 *
 * @author Philip Helger
 */
public class RequestWebScopeMultipart extends RequestWebScope
{
  private boolean m_bParsedMultipart = false;

  public RequestWebScopeMultipart (@NonNull final HttpServletRequest aHttpRequest,
                                   @NonNull final HttpServletResponse aHttpResponse)
  {
    super (aHttpRequest, aHttpResponse);
  }

  @Override
  @OverrideOnDemand
  protected EChange addSpecialRequestParams ()
  {
    // Parse as multipart if the Content-Type matches, and add each item into
    // params()
    // This may create temporary files via DiskFileItem
    final EChange ret = RequestMultipartHelper.handleMultipartFormData (m_aHttpRequest, params ()::putIn);
    m_bParsedMultipart = ret.isChanged ();
    return ret;
  }

  /**
   * @return <code>true</code> if this request scope was parsed as multipart,
   *         <code>false</code> if not.
   * @since 9.1.1
   */
  public final boolean isMultipartRequest ()
  {
    return m_bParsedMultipart;
  }

  @Override
  public boolean equals (final Object o)
  {
    // New fields but no change in rules
    return super.equals (o);
  }

  @Override
  public int hashCode ()
  {
    // New fields but no change in rules
    return super.hashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("ParsedMultipart", m_bParsedMultipart)
                            .getToString ();
  }
}
