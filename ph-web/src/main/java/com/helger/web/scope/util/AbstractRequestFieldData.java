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
package com.helger.web.scope.util;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scope.IRequestParamContainer;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * Abstract base class for request field data classes.
 *
 * @author Philip Helger
 * @since 9.0.2
 */
@Immutable
public abstract class AbstractRequestFieldData
{
  private final String m_sFieldName;

  /**
   * Default constructor.
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   */
  public AbstractRequestFieldData (@Nonnull @Nonempty final String sFieldName)
  {
    m_sFieldName = ValueEnforcer.notEmpty (sFieldName, "FieldName");
  }

  /**
   * @return The field name of this request field
   */
  @Nonnull
  @Nonempty
  public final String getFieldName ()
  {
    return m_sFieldName;
  }

  /**
   * This is a utility method to always retrieve the correct scope.
   *
   * @return The current request scope to use.
   */
  @Nonnull
  protected static final IRequestParamContainer getParams ()
  {
    return WebScopeManager.getRequestScope ().params ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final AbstractRequestFieldData rhs = (AbstractRequestFieldData) o;
    return m_sFieldName.equals (rhs.m_sFieldName);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sFieldName).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("FieldName", m_sFieldName).getToString ();
  }
}
