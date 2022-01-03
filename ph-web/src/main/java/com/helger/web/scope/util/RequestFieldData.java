/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Represents a wrapper around a single request value. It allows gathering the
 * current value, optionally using a default value.
 *
 * @author Philip Helger
 * @see RequestFieldDataMultiValue for multi value data
 */
@Immutable
public class RequestFieldData extends AbstractRequestFieldData
{
  private final String m_sDefaultValue;

  /**
   * Copy constructor
   *
   * @param aRF
   *        The request field to copy the values from. May not be
   *        <code>null</code>.
   */
  public RequestFieldData (@Nonnull final RequestFieldData aRF)
  {
    this (aRF.getFieldName (), aRF.m_sDefaultValue);
  }

  /**
   * Create a new request field that has no default value
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   */
  public RequestFieldData (@Nonnull @Nonempty final String sFieldName)
  {
    this (sFieldName, (String) null);
  }

  /**
   * Default constructor.
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   * @param sDefaultValue
   *        The default value to use, if no value is present in the request
   *        scope.
   */
  public RequestFieldData (@Nonnull @Nonempty final String sFieldName, @Nullable final String sDefaultValue)
  {
    super (sFieldName);
    m_sDefaultValue = sDefaultValue == null ? "" : sDefaultValue;
  }

  /**
   * @return The default value to be used if no request parameter is present. Is
   *         never <code>null</code> but an empty string if no default value is
   *         available.
   */
  @Nonnull
  @OverrideOnDemand
  public String getDefaultValue ()
  {
    return m_sDefaultValue;
  }

  /**
   * Helper method to get the request value without falling back to the provided
   * default value.
   *
   * @return <code>null</code> if no such request value is present
   */
  @Nullable
  protected final String getRequestValueWithoutDefault ()
  {
    return getParams ().getAsString (getFieldName (), null);
  }

  /**
   * Get the value of the request - optionally falling back to the provided
   * default value if no such request parameter is present
   *
   * @return A single request value as string.
   */
  @Nonnull
  public final String getRequestValue ()
  {
    return getParams ().getAsString (getFieldName (), getDefaultValue ());
  }

  /**
   * Utility method that checks if the passed expected value matches the request
   * parameter (considering the fallback mechanism)
   *
   * @param sExpectedValue
   *        The expected value. May not be <code>null</code>.
   * @return <code>true</code> if the passed value equals the actual request
   *         value
   */
  public final boolean hasRequestValue (@Nonnull final String sExpectedValue)
  {
    ValueEnforcer.notNull (sExpectedValue, "ExpectedValue");

    return sExpectedValue.equals (getRequestValue ());
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!super.equals (o))
      return false;
    final RequestFieldData rhs = (RequestFieldData) o;
    return m_sDefaultValue.equals (rhs.m_sDefaultValue);
  }

  @Override
  public int hashCode ()
  {
    return HashCodeGenerator.getDerived (super.hashCode ()).append (m_sDefaultValue).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("DefaultValue", m_sDefaultValue).getToString ();
  }
}
