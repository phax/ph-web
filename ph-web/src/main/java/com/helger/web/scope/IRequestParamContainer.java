/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.web.scope;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.attr.IAttributeContainerAny;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.string.StringHelper;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.fileupload.IFileItem;

/**
 * A special request parameter container with support for file items etc. By
 * default only <code>String</code> and <code>String[]</code> entries are
 * present. If the request was parsed as a multipart request, it may also
 * container <code>IFileItem</code> or <code>IFileItem[]</code> entries.
 *
 * @author Philip Helger
 */
public interface IRequestParamContainer extends IAttributeContainerAny <String>
{
  /**
   * @return A non-<code>null</code> but maybe empty map with all contained
   *         {@link IFileItem} objects from file uploads. The key of the map is
   *         the field name. Important: if the value is an array of
   *         {@link IFileItem} it is not considered in the returned map!
   */
  @Nonnull
  default ICommonsMap <String, IFileItem> getAllUploadedFileItems ()
  {
    final ICommonsMap <String, IFileItem> ret = new CommonsHashMap <> ();
    for (final Map.Entry <String, Object> aEntry : entrySet ())
    {
      final Object aAttrValue = aEntry.getValue ();
      if (aAttrValue instanceof IFileItem)
        ret.put (aEntry.getKey (), (IFileItem) aAttrValue);
    }
    return ret;
  }

  /**
   * @return A non-<code>null</code> but maybe empty map with all contained
   *         {@link IFileItem} objects from file uploads. The key of the map is
   *         the field name.
   */
  @Nonnull
  default ICommonsMap <String, IFileItem []> getAllUploadedFileItemsComplete ()
  {
    final ICommonsMap <String, IFileItem []> ret = new CommonsHashMap <> ();
    for (final Map.Entry <String, Object> aEntry : entrySet ())
    {
      final String sAttrName = aEntry.getKey ();
      final Object aAttrValue = aEntry.getValue ();
      if (aAttrValue instanceof IFileItem)
        ret.put (sAttrName, new IFileItem [] { (IFileItem) aAttrValue });
      else
        if (aAttrValue instanceof IFileItem [])
          ret.put (sAttrName, ArrayHelper.getCopy ((IFileItem []) aAttrValue));
    }
    return ret;
  }

  /**
   * @return A non-<code>null</code> but maybe empty list of all {@link IFileItem}
   *         objects in the request. In comparison to
   *         {@link #getAllUploadedFileItems()} this method also returns the
   *         content of {@link IFileItem} arrays.
   */
  @Nonnull
  default ICommonsList <IFileItem> getAllUploadedFileItemValues ()
  {
    final ICommonsList <IFileItem> ret = new CommonsArrayList <> ();
    for (final Object aAttrValue : values ())
    {
      if (aAttrValue instanceof IFileItem)
        ret.add ((IFileItem) aAttrValue);
      else
        if (aAttrValue instanceof IFileItem [])
          for (final IFileItem aChild : (IFileItem []) aAttrValue)
            ret.add (aChild);
    }
    return ret;
  }

  /**
   * Get the request attribute denoted by the specified attribute name as an
   * uploaded file item. In case the specified parameter is present but not a file
   * item, the method returns <code>null</code>.
   *
   * @param sAttrName
   *        The attribute name to resolved. May be <code>null</code>.
   * @return <code>null</code> if no such attribute is present, or if the
   *         attribute is not a file item.
   */
  @Nullable
  default IFileItem getAsFileItem (@Nullable final String sAttrName)
  {
    return getSafeCastedValue (sAttrName, IFileItem.class);
  }

  /**
   * Get the value of the checkbox of the request parameter with the given name.
   *
   * @param sFieldName
   *        Request parameter name. May be <code>null</code>.
   * @param bDefaultValue
   *        the default value to be returned, if no request attribute is present
   * @return <code>true</code> if the checkbox is checked, <code>false</code> if
   *         it is not checked and the default value otherwise.
   */
  default boolean isCheckBoxChecked (@Nullable final String sFieldName, final boolean bDefaultValue)
  {
    if (StringHelper.hasText (sFieldName))
    {
      // Is the checked value present?
      final String sRequestValue = getAsString (sFieldName);
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

  default boolean hasCheckBoxValue (@Nonnull @Nonempty final String sFieldName,
                                    @Nonnull final String sFieldValue,
                                    final boolean bDefaultValue)
  {
    ValueEnforcer.notEmpty (sFieldName, "FieldName");
    ValueEnforcer.notNull (sFieldValue, "FieldValue");

    // Get all values for the field name
    ICommonsOrderedSet <String> aValues = getAsStringSet (sFieldName);
    if (aValues != null)
      return aValues.contains (sFieldValue);

    // Check if the hidden parameter for "checkbox is contained in the request"
    // is present?
    aValues = getAsStringSet (RequestHelper.getCheckBoxHiddenFieldName (sFieldName));
    if (aValues != null && aValues.contains (sFieldValue))
      return false;

    // Neither nor - default!
    return bDefaultValue;
  }

  /**
   * Same as <code>getAsString</code> but with a trimmed return value.
   *
   * @param sFieldName
   *        The field name to query. May be <code>null</code>.
   * @return <code>null</code> if no such parameter name is present.
   * @since 9.0.2
   */
  @Nullable
  default String getAsStringTrimmed (@Nullable final String sFieldName)
  {
    return StringHelper.trim (getAsString (sFieldName));
  }

  /**
   * Same as <code>getAsString</code> but with a trimmed return value.
   *
   * @param sFieldName
   *        The field name to query. May be <code>null</code>.
   * @param sDefault
   *        The value to be returned if the retrieved value is <code>null</code> .
   * @return <code>null</code> if no such parameter name is present.
   * @since 9.0.2
   */
  @Nullable
  default String getAsStringTrimmed (@Nullable final String sFieldName, @Nullable final String sDefault)
  {
    return StringHelper.trim (getAsString (sFieldName, sDefault));
  }
}
