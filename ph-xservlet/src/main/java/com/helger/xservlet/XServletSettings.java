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
package com.helger.xservlet;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpReferrerPolicy;

/**
 * This class keeps all the settings that can be applied to all XServlet based
 * settings. The settings need to be applied per Servlet instance!<br>
 * The following things can be set here:
 * <ul>
 * <li>HTTP Referrer Policy - see {@link EHttpReferrerPolicy}</li>
 * </ul>
 *
 * @author Philip Helger
 */
public class XServletSettings implements Serializable, ICloneable <XServletSettings>
{
  /** Maximum compatibility */
  private EHttpReferrerPolicy m_eHttpReferrerPolicy = EHttpReferrerPolicy.NO_REFERRER;

  /** By default Multipart handling is enabled */
  private boolean m_bIsMultipartEnabled = true;

  public XServletSettings ()
  {}

  public XServletSettings (@Nonnull final XServletSettings aOther)
  {
    ValueEnforcer.notNull (aOther, "Other");
    m_eHttpReferrerPolicy = aOther.m_eHttpReferrerPolicy;
    m_bIsMultipartEnabled = aOther.m_bIsMultipartEnabled;
  }

  @Nullable
  public EHttpReferrerPolicy getHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy;
  }

  @Nonnull
  public XServletSettings setHttpReferrerPolicy (@Nullable final EHttpReferrerPolicy eHttpReferrerPolicy)
  {
    m_eHttpReferrerPolicy = eHttpReferrerPolicy;
    return this;
  }

  public boolean hasHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy != null;
  }

  @Nonnull
  public XServletSettings setMultipartEnabled (final boolean bIsMultipartEnabled)
  {
    m_bIsMultipartEnabled = bIsMultipartEnabled;
    return this;
  }

  public boolean isMultipartEnabled ()
  {
    return m_bIsMultipartEnabled;
  }

  @Nonnull
  @ReturnsMutableCopy
  public XServletSettings getClone ()
  {
    return new XServletSettings (this);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("HttpReferrerPolicy", m_eHttpReferrerPolicy)
                                       .append ("IsMultipartEnabled", m_bIsMultipartEnabled)
                                       .getToString ();
  }
}
