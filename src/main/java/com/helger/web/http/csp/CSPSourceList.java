/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.http.csp;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.IHasStringRepresentation;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;

public class CSPSourceList implements IHasStringRepresentation
{
  public static final String KEYWORD_SELF = "'self'";
  public static final String KEYWORD_UNSAFE_INLINE = "'unsafe-inline'";
  public static final String KEYWORD_UNSAFE_EVAL = "'unsafe-eval'";

  private final Set <String> m_aList = new LinkedHashSet <String> ();

  public CSPSourceList ()
  {}

  @Nonnegative
  public int getExpressionCount ()
  {
    return m_aList.size ();
  }

  /**
   * Add a scheme
   *
   * @param sScheme
   *        Scheme in the format <code>scheme ":"</code>
   * @return this
   */
  @Nonnull
  public CSPSourceList addScheme (@Nonnull @Nonempty final String sScheme)
  {
    ValueEnforcer.notEmpty (sScheme, "Scheme");
    if (sScheme.length () <= 1 || !sScheme.endsWith (":"))
      throw new IllegalArgumentException ("Passed scheme '" + sScheme + "' is invalid!");
    m_aList.add (sScheme);
    return this;
  }

  /**
   * Add a host
   *
   * @param aHost
   *        Host to add. Must be a valid URL.
   * @return this
   */
  @Nonnull
  public CSPSourceList addHost (@Nonnull final ISimpleURL aHost)
  {
    ValueEnforcer.notNull (aHost, "Host");
    return addHost (aHost.getAsString ());
  }

  /**
   * Add a host
   *
   * @param sHost
   *        Host to add. Must be a valid URL or a star prefixed version.
   * @return this
   */
  @Nonnull
  public CSPSourceList addHost (@Nonnull @Nonempty final String sHost)
  {
    ValueEnforcer.notEmpty (sHost, "Host");
    m_aList.add (sHost);
    return this;
  }

  /**
   * Add all hosts as "*"
   *
   * @return this
   */
  @Nonnull
  public CSPSourceList addHostAll ()
  {
    m_aList.add ("*");
    return this;
  }

  /**
   * source expression 'self' represents the set of URIs which are in the same
   * origin as the protected resource
   *
   * @return this
   */
  @Nonnull
  public CSPSourceList addKeywordSelf ()
  {
    m_aList.add (KEYWORD_SELF);
    return this;
  }

  /**
   * source expression 'unsafe-inline' represents content supplied inline in the
   * resource itself
   *
   * @return this
   */
  @Nonnull
  public CSPSourceList addKeywordUnsafeInline ()
  {
    m_aList.add (KEYWORD_UNSAFE_INLINE);
    return this;
  }

  @Nonnull
  public CSPSourceList addKeywordUnsafeEval ()
  {
    m_aList.add (KEYWORD_UNSAFE_EVAL);
    return this;
  }

  @Nonnull
  public String getAsString ()
  {
    return StringHelper.getImploded (' ', m_aList);
  }
}
