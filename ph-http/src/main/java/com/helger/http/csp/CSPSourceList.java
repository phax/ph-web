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
package com.helger.http.csp;

import java.io.Serializable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ext.CommonsLinkedHashSet;
import com.helger.commons.collection.ext.ICommonsOrderedSet;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.url.ISimpleURL;

/**
 * A source list to be used in a CSP 1.0 directive ({@link CSPDirective}). It's
 * just a convenient way to build a CSP directive value.
 *
 * @author Philip Helger
 * @since 6.0.3
 */
@NotThreadSafe
public class CSPSourceList implements Serializable
{
  public static final String KEYWORD_HOST_ALL = "*";
  public static final String KEYWORD_SELF = "'self'";
  public static final String KEYWORD_UNSAFE_INLINE = "'unsafe-inline'";
  public static final String KEYWORD_UNSAFE_EVAL = "'unsafe-eval'";

  private final ICommonsOrderedSet <String> m_aList = new CommonsLinkedHashSet<> ();

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
    return addHost (aHost.getAsStringWithEncodedParameters ());
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
    m_aList.add (KEYWORD_HOST_ALL);
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

  /**
   * @return The whole source list as a single string, separated by a blank
   *         char.
   */
  @Nonnull
  public String getAsString ()
  {
    return StringHelper.getImploded (' ', m_aList);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final CSPSourceList rhs = (CSPSourceList) o;
    return m_aList.equals (rhs.m_aList);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aList).getHashCode ();
  }

  @Override
  @Nonnull
  public String toString ()
  {
    return new ToStringGenerator (this).append ("list", m_aList).toString ();
  }
}
