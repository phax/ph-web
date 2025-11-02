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
package com.helger.xservlet;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.clone.ICloneable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.http.EHttpReferrerPolicy;
import com.helger.servlet.response.EXFrameOptionType;
import com.helger.url.ISimpleURL;
import com.helger.url.ReadOnlyURL;

/**
 * This class keeps all the settings that can be applied to all XServlet based settings. The
 * settings need to be applied per Servlet instance!<br>
 * The following things can be set here:
 * <ul>
 * <li>HTTP Referrer Policy - see {@link EHttpReferrerPolicy}</li>
 * </ul>
 *
 * @author Philip Helger
 */
public class XServletSettings implements ICloneable <XServletSettings>
{
  public static final EHttpReferrerPolicy DEFAULT_HTTP_REFERRER_POLICY = EHttpReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN;
  public static final boolean DEFAULT_MULTIPART_ENABLED = true;

  /** Best tradeoff between security and convenience */
  private EHttpReferrerPolicy m_eHttpReferrerPolicy = DEFAULT_HTTP_REFERRER_POLICY;

  /** Best tradeoff between security and convenience */
  private EXFrameOptionType m_eXFrameOptionsType = EXFrameOptionType.DEFAULT;

  /** not needed for default value */
  private ISimpleURL m_aXFrameOptionsDomain;

  /** By default Multipart handling is enabled */
  private boolean m_bIsMultipartEnabled = DEFAULT_MULTIPART_ENABLED;

  public XServletSettings ()
  {}

  public XServletSettings (@NonNull final XServletSettings aOther)
  {
    ValueEnforcer.notNull (aOther, "Other");
    m_eHttpReferrerPolicy = aOther.m_eHttpReferrerPolicy;
    m_eXFrameOptionsType = aOther.m_eXFrameOptionsType;
    m_aXFrameOptionsDomain = aOther.m_aXFrameOptionsDomain;
    m_bIsMultipartEnabled = aOther.m_bIsMultipartEnabled;
  }

  /**
   * @return The current http Referrer Policy or <code>null</code> if none is set.
   */
  @Nullable
  public final EHttpReferrerPolicy getHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy;
  }

  /**
   * @return <code>true</code> if a referrer policy is set, <code>false</code> if not.
   */
  public final boolean hasHttpReferrerPolicy ()
  {
    return m_eHttpReferrerPolicy != null;
  }

  /**
   * Set the Http Referrer Policy to be used.
   *
   * @param eHttpReferrerPolicy
   *        The enumeration value to be used. May be <code>null</code> to indicate: don't set
   * @return this for chaining
   */
  @NonNull
  public final XServletSettings setHttpReferrerPolicy (@Nullable final EHttpReferrerPolicy eHttpReferrerPolicy)
  {
    m_eHttpReferrerPolicy = eHttpReferrerPolicy;
    return this;
  }

  /**
   * @return The currently set X-Frame-Options type. May be <code>null</code>.
   * @since 9.1.1
   */
  @Nullable
  public final EXFrameOptionType getXFrameOptionsType ()
  {
    return m_eXFrameOptionsType;
  }

  /**
   * @return The currently set X-Frame-Options domain only used the type is
   *         {@link EXFrameOptionType#ALLOW_FROM}. May be <code>null</code>.
   * @since 9.1.1
   */
  @Nullable
  public final ISimpleURL getXFrameOptionsDomain ()
  {
    return m_aXFrameOptionsDomain;
  }

  /**
   * @return <code>true</code> if X-Frame-Options are defined, <code>false</code> if not.
   * @since 9.1.1
   */
  public final boolean hasXFrameOptions ()
  {
    return m_eXFrameOptionsType != null;
  }

  /**
   * The X-Frame-Options HTTP response header can be used to indicate whether or not a browser
   * should be allowed to render a page in a &lt;frame&gt;, &lt;iframe&gt; or &lt;object&gt; . Sites
   * can use this to avoid clickjacking attacks, by ensuring that their content is not embedded into
   * other sites. Example:
   *
   * <pre>
   * X-Frame-Options: DENY
   * X-Frame-Options: SAMEORIGIN
   * X-Frame-Options: ALLOW-FROM https://example.com/
   * </pre>
   *
   * @param eType
   *        The X-Frame-Options type to be set. May be <code>null</code>.
   * @param aDomain
   *        The domain URL to be used in "ALLOW-FROM". May be <code>null</code> for the other cases.
   * @return this for chaining
   * @since 9.1.1
   */
  @NonNull
  public final XServletSettings setXFrameOptions (@Nullable final EXFrameOptionType eType,
                                                  @Nullable final ISimpleURL aDomain)
  {
    if (eType != null && eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    m_eXFrameOptionsType = eType;
    m_aXFrameOptionsDomain = aDomain == null ? null : ReadOnlyURL.of (aDomain);
    return this;
  }

  public final boolean isMultipartEnabled ()
  {
    return m_bIsMultipartEnabled;
  }

  @NonNull
  public final XServletSettings setMultipartEnabled (final boolean bIsMultipartEnabled)
  {
    m_bIsMultipartEnabled = bIsMultipartEnabled;
    return this;
  }

  @NonNull
  @ReturnsMutableCopy
  public XServletSettings getClone ()
  {
    return new XServletSettings (this);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final XServletSettings rhs = (XServletSettings) o;
    return EqualsHelper.equals (m_eHttpReferrerPolicy, rhs.m_eHttpReferrerPolicy) &&
           EqualsHelper.equals (m_eXFrameOptionsType, rhs.m_eXFrameOptionsType) &&
           EqualsHelper.equals (m_aXFrameOptionsDomain, rhs.m_aXFrameOptionsDomain) &&
           m_bIsMultipartEnabled == rhs.m_bIsMultipartEnabled;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_eHttpReferrerPolicy)
                                       .append (m_eXFrameOptionsType)
                                       .append (m_aXFrameOptionsDomain)
                                       .append (m_bIsMultipartEnabled)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("HttpReferrerPolicy", m_eHttpReferrerPolicy)
                                       .append ("XFrameOptionsType", m_eXFrameOptionsType)
                                       .append ("XFrameOptionsDomain", m_aXFrameOptionsDomain)
                                       .append ("IsMultipartEnabled", m_bIsMultipartEnabled)
                                       .getToString ();
  }
}
