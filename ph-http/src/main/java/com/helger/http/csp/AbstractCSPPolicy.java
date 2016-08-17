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
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * Abstract CSP policy declaration.
 *
 * @author Philip Helger
 * @param <T>
 *        The CSP directive type
 */
@NotThreadSafe
public class AbstractCSPPolicy <T extends ICSPDirective> implements Serializable
{
  private final ICommonsList <T> m_aList = new CommonsArrayList<> ();

  public AbstractCSPPolicy ()
  {}

  public boolean isEmpty ()
  {
    return m_aList.isEmpty ();
  }

  public boolean isNotEmpty ()
  {
    return m_aList.isNotEmpty ();
  }

  @Nonnegative
  public int getDirectiveCount ()
  {
    return m_aList.size ();
  }

  @Nonnull
  public AbstractCSPPolicy <T> addDirective (@Nonnull final T aDirective)
  {
    ValueEnforcer.notNull (aDirective, "Directive");
    m_aList.add (aDirective);
    return this;
  }

  @Nonnull
  public EChange removeDirective (@Nullable final T aDirective)
  {
    return m_aList.removeObject (aDirective);
  }

  @Nonnull
  public EChange removeDirectiveAtIndex (final int nIndex)
  {
    return m_aList.removeAtIndex (nIndex);
  }

  @Nonnull
  public EChange removeAllDirectives ()
  {
    return m_aList.removeAll ();
  }

  @Nonnull
  public String getAsString ()
  {
    return StringHelper.getImplodedNonEmpty ("; ", m_aList, ICSPDirective::getAsString);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final AbstractCSPPolicy <?> rhs = (AbstractCSPPolicy <?>) o;
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
