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
package com.helger.servlet.request;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * Represents a single item in a {@link RequestParamMap}. It consists of a
 * String value and optional children.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class RequestParamMapItem
{
  private String m_sValue;
  private final ICommonsOrderedMap <String, RequestParamMapItem> m_aChildren = new CommonsLinkedHashMap <> ();

  public RequestParamMapItem ()
  {}

  public RequestParamMapItem (@Nonnull final RequestParamMapItem aOther)
  {
    m_sValue = aOther.m_sValue;
    m_aChildren.putAll (aOther.m_aChildren);
  }

  public boolean hasValue ()
  {
    return m_sValue != null;
  }

  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  @Nullable
  public String getValueTrimmed ()
  {
    return StringHelper.trim (m_sValue);
  }

  public boolean hasChildren ()
  {
    return m_aChildren.isNotEmpty ();
  }

  @Nonnull
  @ReturnsMutableObject ("design")
  public ICommonsOrderedMap <String, RequestParamMapItem> directGetChildren ()
  {
    return m_aChildren;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsOrderedMap <String, RequestParamMapItem> getAllChildren ()
  {
    return m_aChildren.getClone ();
  }

  public boolean containsChild (@Nullable final String sName)
  {
    return m_aChildren.containsKey (sName);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final RequestParamMapItem rhs = (RequestParamMapItem) o;
    return EqualsHelper.equals (m_sValue, rhs.m_sValue) && m_aChildren.equals (rhs.m_aChildren);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sValue).append (m_aChildren).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).appendIfNotNull ("Value", m_sValue)
                                       .appendIf ("Children", m_aChildren, CollectionHelper::isNotEmpty)
                                       .getToString ();
  }

  @Nullable
  public static RequestParamMapItem create (@Nullable final Object o)
  {
    if (o instanceof RequestParamMapItem)
    {
      // Create a copy
      return new RequestParamMapItem ((RequestParamMapItem) o);
    }
    if (o instanceof String)
    {
      final RequestParamMapItem ret = new RequestParamMapItem ();
      ret.m_sValue = (String) o;
      return ret;
    }
    if (o instanceof Map <?, ?>)
    {
      final RequestParamMapItem ret = new RequestParamMapItem ();
      for (final Map.Entry <?, ?> aEntry : ((Map <?, ?>) o).entrySet ())
      {
        // Recursive create function
        final RequestParamMapItem aChildItem = RequestParamMapItem.create (aEntry.getValue ());
        if (aChildItem != null)
          ret.m_aChildren.put ((String) aEntry.getKey (), aChildItem);
      }
      return ret;
    }

    // Unsupported type - this can e.g. happen when building the overall
    // RequestParamMap from a request and the request contains arbitrary objects
    // that are of any type. These should simply be ignored.
    return null;
  }
}
