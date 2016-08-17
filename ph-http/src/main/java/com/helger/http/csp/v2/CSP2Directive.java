/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.http.csp.v2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.csp.ICSPDirective;

/**
 * A single CSP 2.0 directive. It's a name-value-pair.
 *
 * @author Philip Helger
 */
public class CSP2Directive implements ICSPDirective
{
  private final String m_sName;
  private final String m_sValue;

  public CSP2Directive (@Nonnull @Nonempty final String sName, @Nullable final CSP2SourceList aValue)
  {
    this (sName, aValue == null ? null : aValue.getAsString ());
  }

  public CSP2Directive (@Nonnull @Nonempty final String sName, @Nullable final String sValue)
  {
    m_sName = ValueEnforcer.notEmpty (sName, "Name");
    m_sValue = sValue;
    if (StringHelper.hasText (sValue))
    {
      if (sValue.indexOf (',') >= 0)
        throw new IllegalArgumentException ("Value may not contain a comma (,): " + sValue);
      if (sValue.indexOf (';') >= 0)
        throw new IllegalArgumentException ("Value may not contain a semicolon (;): " + sValue);
    }
  }

  @Nonnull
  @Nonempty
  public String getName ()
  {
    return m_sName;
  }

  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final CSP2Directive rhs = (CSP2Directive) o;
    return m_sName.equals (rhs.m_sName) && EqualsHelper.equals (m_sValue, rhs.m_sValue);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sName).append (m_sValue).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("name", m_sName).appendIfNotNull ("value", m_sValue).toString ();
  }

  @Nonnull
  public static CSP2Directive createDefaultSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("default-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createScriptSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("script-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createObjectSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("object-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createStyleSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("style-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createImgSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("img-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createMediaSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("media-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createFrameSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("frame-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createFontSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("font-src", aValue);
  }

  @Nonnull
  public static CSP2Directive createConnectSrc (@Nullable final CSP2SourceList aValue)
  {
    return new CSP2Directive ("connect-src", aValue);
  }

  /**
   * The sandbox directive specifies an HTML sandbox policy that the user agent
   * applies to the protected resource.
   *
   * @param sValue
   *        value
   * @return new directive
   */
  @Nonnull
  public static CSP2Directive createSandbox (@Nullable final String sValue)
  {
    return new CSP2Directive ("sandbox", sValue);
  }

  /**
   * The report-uri directive specifies a URI to which the user agent sends
   * reports about policy violation.
   *
   * @param sValue
   *        Report URI
   * @return new directive
   */
  @Nonnull
  public static CSP2Directive createReportURI (@Nullable final String sValue)
  {
    return new CSP2Directive ("report-uri", sValue);
  }
}
