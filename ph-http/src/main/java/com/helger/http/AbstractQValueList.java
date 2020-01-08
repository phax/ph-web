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
package com.helger.http;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.string.ToStringGenerator;

/**
 * Represents a base class for all QValue'd stuff
 *
 * @author Philip Helger
 * @param <KEYTYPE>
 *        The key type for the map. Must implement Serializable.
 */
public abstract class AbstractQValueList <KEYTYPE extends Serializable> implements Serializable
{
  // Maps something to quality
  protected final ICommonsOrderedMap <KEYTYPE, QValue> m_aMap = new CommonsLinkedHashMap<> ();

  public AbstractQValueList ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsOrderedMap <KEYTYPE, QValue> getAllQValues ()
  {
    return m_aMap.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsOrderedMap <KEYTYPE, QValue> getAllQValuesLowerThan (final double dQuality)
  {
    final ICommonsOrderedMap <KEYTYPE, QValue> ret = new CommonsLinkedHashMap<> ();
    for (final Map.Entry <KEYTYPE, QValue> aEntry : m_aMap.entrySet ())
    {
      final QValue aQValue = aEntry.getValue ();
      if (aQValue.getQuality () < dQuality)
        ret.put (aEntry.getKey (), aQValue);
    }
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsOrderedMap <KEYTYPE, QValue> getAllQValuesLowerOrEqual (final double dQuality)
  {
    final ICommonsOrderedMap <KEYTYPE, QValue> ret = new CommonsLinkedHashMap<> ();
    for (final Map.Entry <KEYTYPE, QValue> aEntry : m_aMap.entrySet ())
    {
      final QValue aQValue = aEntry.getValue ();
      if (aQValue.getQuality () <= dQuality)
        ret.put (aEntry.getKey (), aQValue);
    }
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsOrderedMap <KEYTYPE, QValue> getAllQValuesGreaterThan (final double dQuality)
  {
    final ICommonsOrderedMap <KEYTYPE, QValue> ret = new CommonsLinkedHashMap<> ();
    for (final Map.Entry <KEYTYPE, QValue> aEntry : m_aMap.entrySet ())
    {
      final QValue aQValue = aEntry.getValue ();
      if (aQValue.getQuality () > dQuality)
        ret.put (aEntry.getKey (), aQValue);
    }
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsOrderedMap <KEYTYPE, QValue> getAllQValuesGreaterOrEqual (final double dQuality)
  {
    final ICommonsOrderedMap <KEYTYPE, QValue> ret = new CommonsLinkedHashMap<> ();
    for (final Map.Entry <KEYTYPE, QValue> aEntry : m_aMap.entrySet ())
    {
      final QValue aQValue = aEntry.getValue ();
      if (aQValue.getQuality () >= dQuality)
        ret.put (aEntry.getKey (), aQValue);
    }
    return ret;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("map", m_aMap).getToString ();
  }
}
