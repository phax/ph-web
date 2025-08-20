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

import java.util.Collection;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.OverrideOnDemand;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.helper.CollectionEqualsHelper;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Represents a wrapper around a single request value. It allows gathering the current values,
 * optionally using default values.
 *
 * @author Philip Helger
 * @see RequestFieldData For single value class
 * @since 9.0.2
 */
@Immutable
public class RequestFieldDataMultiValue extends AbstractRequestFieldData
{
  private final ICommonsList <String> m_aDefaultValues;

  /**
   * Copy constructor
   *
   * @param aRF
   *        The request field to copy the values from. May not be <code>null</code>.
   */
  public RequestFieldDataMultiValue (@Nonnull final RequestFieldDataMultiValue aRF)
  {
    this (aRF.getFieldName (), aRF.m_aDefaultValues);
  }

  /**
   * Create a new request field that has no default value
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   */
  public RequestFieldDataMultiValue (@Nonnull @Nonempty final String sFieldName)
  {
    this (sFieldName, (Collection <String>) null);
  }

  /**
   * Default constructor.
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   * @param aDefaultValues
   *        The default values to use, if no value is present in the request scope.
   */
  public RequestFieldDataMultiValue (@Nonnull @Nonempty final String sFieldName,
                                     @Nullable final Collection <String> aDefaultValues)
  {
    super (sFieldName);
    // Always create a copy
    m_aDefaultValues = new CommonsArrayList <> (aDefaultValues);
  }

  /**
   * @return The default values to be used if no request parameter is present. Is never
   *         <code>null</code> but an empty list if no default value is available.
   */
  @Nonnull
  @ReturnsMutableCopy
  @OverrideOnDemand
  public ICommonsList <String> getDefaultValues ()
  {
    return m_aDefaultValues.getClone ();
  }

  /**
   * Helper method to get the request value without falling back to the provided default value.
   *
   * @return <code>null</code> if no such request value is present
   */
  @Nullable
  protected final ICommonsList <String> getRequestValuesWithoutDefault ()
  {
    return getParams ().getAsStringList (getFieldName (), null);
  }

  /**
   * Get the value of the request - optionally falling back to the provided default value if no such
   * request parameter is present
   *
   * @return A single request value as string.
   */
  @Nonnull
  public final ICommonsList <String> getRequestValues ()
  {
    return getParams ().getAsStringList (getFieldName (), getDefaultValues ());
  }

  /**
   * Utility method that checks if the passed expected value matches the request parameter
   * (considering the fallback mechanism)
   *
   * @param aExpectedValues
   *        The list of expected values. May not be <code>null</code>.
   * @return <code>true</code> if the passed value equals the actual request value
   */
  public final boolean hasRequestValues (@Nonnull final Collection <String> aExpectedValues)
  {
    ValueEnforcer.notNull (aExpectedValues, "ExpectedValues");

    return CollectionEqualsHelper.equalsCollection (aExpectedValues, getRequestValues ());
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!super.equals (o))
      return false;
    final RequestFieldDataMultiValue rhs = (RequestFieldDataMultiValue) o;
    return m_aDefaultValues.equals (rhs.m_aDefaultValues);
  }

  @Override
  public int hashCode ()
  {
    return HashCodeGenerator.getDerived (super.hashCode ()).append (m_aDefaultValues).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("DefaultValues", m_aDefaultValues).getToString ();
  }
}
