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
package com.helger.servlet.request;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.helger.annotation.Nonempty;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.iface.IHasSize;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.collection.commons.ICommonsOrderedSet;
import com.helger.typeconvert.impl.TypeConverter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A request parameter map. It handles complex request parameters and lets you iterate the
 * structure.
 *
 * @author Philip Helger
 */
public interface IRequestParamMap extends IHasSize
{
  boolean contains (@Nonnull @Nonempty String... aPath);

  @Nullable
  RequestParamMapItem getObject (@Nonnull @Nonempty String... aPath);

  @Nullable
  default String getString (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : aItem.getValue ();
  }

  @Nullable
  default String getStringTrimmed (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : aItem.getValueTrimmed ();
  }

  default boolean getBoolean (@Nonnull @Nonempty final String sPath, final boolean bDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? bDefault : TypeConverter.convertToBoolean (aItem.getValue (), bDefault);
  }

  default double getDouble (@Nonnull @Nonempty final String sPath, final double dDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? dDefault : TypeConverter.convertToDouble (aItem.getValue (), dDefault);
  }

  default int getInt (@Nonnull @Nonempty final String sPath, final int nDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? nDefault : TypeConverter.convertToInt (aItem.getValue (), nDefault);
  }

  default long getLong (@Nonnull @Nonempty final String sPath, final long nDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? nDefault : TypeConverter.convertToLong (aItem.getValue (), nDefault);
  }

  @Nullable
  default BigInteger getBigInteger (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : TypeConverter.convert (aItem.getValue (), BigInteger.class);
  }

  @Nullable
  default BigDecimal getBigDecimal (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : TypeConverter.convert (aItem.getValue (), BigDecimal.class);
  }

  /**
   * Get the value of the checkbox of the request parameter with the given name. Ripped from
   * IRequestParamContainer....
   *
   * @param sFieldName
   *        Request parameter name. May be <code>null</code>.
   * @param bDefaultValue
   *        the default value to be returned, if no request attribute is present
   * @return <code>true</code> if the checkbox is checked, <code>false</code> if it is not checked
   *         and the default value otherwise.
   */
  default boolean isCheckBoxChecked (@Nullable final String sFieldName, final boolean bDefaultValue)
  {
    if (StringHelper.isNotEmpty (sFieldName))
    {
      // Is the checked value present?
      final String sRequestValue = getString (sFieldName);
      if (sRequestValue != null)
        return true;

      // Check if the hidden parameter for "checkbox is contained in the
      // request" is present?
      // If so it means the checkbox parameter is part of the request, but the
      // checkbox is not checked
      if (containsKey (RequestHelper.getCheckBoxHiddenFieldName (sFieldName)))
        return false;
    }

    // Neither nor - default!
    return bDefaultValue;
  }

  @Nullable
  ICommonsOrderedMap <String, String> getValueMap (@Nonnull @Nonempty String... aPath);

  @Nullable
  ICommonsOrderedMap <String, String> getValueTrimmedMap (@Nonnull @Nonempty String... aPath);

  /**
   * Get a nested map for the specified path.
   *
   * @param aPath
   *        The path to be resolved
   * @return <code>null</code> if the path could not be resolved.
   */
  @Nullable
  IRequestParamMap getMap (@Nonnull @Nonempty String... aPath);

  /**
   * Check if this map, contains the passed key. This will be true both for nested maps as well as
   * for values.
   *
   * @param sKey
   *        The key to check.
   * @return <code>true</code> if such a key is contained, <code>false</code> if not
   */
  boolean containsKey (@Nullable String sKey);

  /**
   * @return A set of all contained key. Never <code>null</code> but maybe empty.
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsOrderedSet <String> keySet ();

  /**
   * @return A collection of all values of this map. The type of the value is usually either
   *         {@link String}, file item (from upload) or {@link Map} from String to Object.
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsList <RequestParamMapItem> values ();

  /**
   * @return A copy of the contained map. For the value types see {@link #values()}
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsOrderedMap <String, RequestParamMapItem> getAsObjectMap ();

  /**
   * @return A key/value map, with enforced values. If this map contains a nested map, the nested
   *         maps are ignored!
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsOrderedMap <String, String> getAsValueMap ();

  /**
   * @return A key/value map, with enforced and trimmed values. If this map contains a nested map,
   *         the nested maps are ignored!
   */
  @Nonnull
  @ReturnsMutableCopy
  ICommonsOrderedMap <String, String> getAsValueTrimmedMap ();
}
