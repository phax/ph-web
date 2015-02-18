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
package com.helger.web.scopes.util;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.OverrideOnDemand;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.id.IHasID;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scopes.domain.IRequestWebScope;
import com.helger.web.scopes.mgr.WebScopeManager;

/**
 * Represents a wrapper around a single request value. It allows gathering the
 * current value, optionally using a default value.
 *
 * @author Philip Helger
 */
@Immutable
public class RequestFieldData implements Serializable
{
  private final String m_sFieldName;
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
    this (aRF.m_sFieldName, aRF.m_sDefaultValue);
  }

  /**
   * Create a new request field that has no default value
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   */
  public RequestFieldData (@Nonnull @Nonempty final String sFieldName)
  {
    this (sFieldName, "");
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
    m_sFieldName = ValueEnforcer.notEmpty (sFieldName, "FieldName");
    m_sDefaultValue = sDefaultValue == null ? "" : sDefaultValue;
  }

  /**
   * Utility constructor that uses an optional default value provider that has
   * an ID
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   * @param aDefaultValueProvider
   *        The object who's ID is to be used. May be <code>null</code> in which
   *        case no default value is used
   */
  public RequestFieldData (@Nonnull @Nonempty final String sFieldName,
                           @Nullable final IHasID <String> aDefaultValueProvider)
  {
    this (sFieldName, aDefaultValueProvider == null ? "" : aDefaultValueProvider.getID ());
  }

  /**
   * Helper constructor using an int instead of a String.
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   * @param nDefaultValue
   *        The default value to be used. Is converted to a String
   */
  public RequestFieldData (@Nonnull @Nonempty final String sFieldName, final int nDefaultValue)
  {
    this (sFieldName, Integer.toString (nDefaultValue));
  }

  /**
   * Helper constructor using a long instead of a String.
   *
   * @param sFieldName
   *        The field name to use. May neither be <code>null</code> nor empty.
   * @param nDefaultValue
   *        The default value to be used. Is converted to a String
   */
  public RequestFieldData (@Nonnull @Nonempty final String sFieldName, final long nDefaultValue)
  {
    this (sFieldName, Long.toString (nDefaultValue));
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
   * This is a utility method to always retrieve the correct scope.
   *
   * @return The current request scope to use.
   */
  @Nonnull
  protected static final IRequestWebScope getScope ()
  {
    return WebScopeManager.getRequestScope ();
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
    return getScope ().getAttributeAsString (m_sFieldName, null);
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
    return getScope ().getAttributeAsString (m_sFieldName, getDefaultValue ());
  }

  /**
   * In case multiple request parameters with the same value are present (e.g.
   * multi-selects or checkboxes) this method retrieves all request values. If
   * no such request value is present a list with one entry (the default value)
   * is returned, in case the default value is non-empty
   *
   * @return A list of simple request values with the same key or
   *         <code>null</code> if no such request parameter is present and no
   *         default value was provided
   */
  @Nullable
  public final List <String> getRequestValues ()
  {
    List <String> aDefault = null;
    final String sDefaultValue = getDefaultValue ();
    if (StringHelper.hasText (sDefaultValue))
      aDefault = ContainerHelper.newList (sDefaultValue);
    return getScope ().getAttributeValues (m_sFieldName, aDefault);
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
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final RequestFieldData rhs = (RequestFieldData) o;
    return m_sFieldName.equals (rhs.m_sFieldName) && m_sDefaultValue.equals (rhs.m_sDefaultValue);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sFieldName).append (m_sDefaultValue).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("fieldName", m_sFieldName)
                                       .append ("defaultValue", m_sDefaultValue)
                                       .toString ();
  }
}
