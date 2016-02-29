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
package com.helger.web.http.csp;

import java.io.Serializable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * CSP 1.0 policy. See http://www.w3.org/TR/CSP/
 *
 * @author Philip Helger
 * @since 6.0.3
 */
@NotThreadSafe
public class CSPPolicy implements Serializable
{
  private final ICommonsList <CSPDirective> m_aList = new CommonsArrayList <> ();

  public CSPPolicy ()
  {}

  @Nonnegative
  public int getDirectiveCount ()
  {
    return m_aList.size ();
  }

  @Nonnull
  public CSPPolicy addDirective (@Nonnull final CSPDirective aDirective)
  {
    ValueEnforcer.notNull (aDirective, "Directive");
    m_aList.add (aDirective);
    return this;
  }

  @Nonnull
  public String getAsString ()
  {
    return StringHelper.getImplodedNonEmpty ("; ", m_aList, CSPDirective::getAsString);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final CSPPolicy rhs = (CSPPolicy) o;
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
