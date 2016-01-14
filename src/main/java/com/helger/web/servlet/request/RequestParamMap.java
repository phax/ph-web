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
package com.helger.web.servlet.request;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.attr.AttributeValueConverter;
import com.helger.commons.collection.attr.IAttributeContainer;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class represents a nested map that is build from request parameters.
 * E.g. the parameter <code>struct[key]=value</code> results in a
 * <code>map{struct=map{key=value}}</code>.<br>
 * If another parameter <code>struct[key2]=value2</code> is added the resulting
 * map looks like this: <code>map{struct=map{key=value, key2=value2}}</code>.
 * Theses maps can indefinitely be nested.<br>
 * Having only <code>struct[key1][key2][key3]=value</code> results in
 * <code>map{struct=map{key1=map{key2=map{key3=value}}}}</code><br>
 * <br>
 * By default the separator chars are "[" and "]" but since this may be a
 * problem with JS expressions, {@link #setSeparators(char, char)} and
 * {@link #setSeparators(String, String)} offer the possibility to set different
 * separator separators that are not special.
 *
 * @author Philip Helger
 */
@Immutable
public class RequestParamMap implements IRequestParamMap
{
  public static final String DEFAULT_OPEN = "[";
  public static final String DEFAULT_CLOSE = "]";

  /** The index open separator */
  private static String s_sOpen = DEFAULT_OPEN;
  /** The index close separator */
  private static String s_sClose = DEFAULT_CLOSE;

  /** Linked hash map for consistent results */
  private final Map <String, RequestParamMapItem> m_aMap = new LinkedHashMap <String, RequestParamMapItem> ();

  public RequestParamMap ()
  {}

  /**
   * This constructor is private, because it does not call the
   * {@link #put(String, Object)} method which does the main string parsing!
   *
   * @param aMap
   *        The map to use. May not be <code>null</code>.
   */
  public RequestParamMap (@Nonnull final Map <String, ?> aMap)
  {
    for (final Map.Entry <String, ?> aEntry : aMap.entrySet ())
    {
      final RequestParamMapItem aItem = RequestParamMapItem.create (aEntry.getValue ());
      if (aItem != null)
        m_aMap.put (aEntry.getKey (), aItem);
    }
  }

  private void _recursiveAddItem (@Nonnull final Map <String, RequestParamMapItem> aMap,
                                  @Nonnull final String sName,
                                  @Nullable final Object aValue)
  {
    final int nIndex = sName.indexOf (s_sOpen);
    if (nIndex == -1)
    {
      // Value level
      final RequestParamMapItem aItem = RequestParamMapItem.create (aValue);
      if (aItem != null)
        aMap.put (sName, aItem);
    }
    else
    {
      if (nIndex == 0)
      {
        // Empty level - makes no sense

        // Recursively scan child items (starting at the first character after
        // the '[')
        _recursiveAddItem (aMap, sName.substring (s_sOpen.length ()), aValue);
      }
      else
      {
        // Get the name until the first "["
        final String sPrefix = sName.substring (0, nIndex);

        // Ensure that the respective map is present
        RequestParamMapItem aChildItem = aMap.get (sPrefix);
        if (aChildItem == null)
        {
          aChildItem = new RequestParamMapItem ();
          aMap.put (sPrefix, aChildItem);
        }

        // Recursively scan child items (starting at the first character after
        // the '[')
        _recursiveAddItem (aChildItem.directGetChildren (), sName.substring (nIndex + s_sOpen.length ()), aValue);
      }
    }
  }

  public void put (@Nonnull final String sPath, @Nullable final Object aValue)
  {
    // replace everything just to have opening separators ("[") left and only
    // one closing separator ("]") at the end
    String sRealPath = StringHelper.replaceAll (sPath, s_sClose + s_sOpen, s_sOpen);
    // Remove the remaining trailing closing separator
    sRealPath = StringHelper.trimEnd (sRealPath, s_sClose);
    // Remove any remaining opening closing separator because this indicates and
    // invalid level (as e.g. in 'columns[0][]')
    sRealPath = StringHelper.trimEndRepeatedly (sRealPath, s_sOpen);
    // Start parsing
    _recursiveAddItem (m_aMap, sRealPath, aValue);
  }

  @Nullable
  private static Map <String, RequestParamMapItem> _getChildMap (@Nonnull final Map <String, RequestParamMapItem> aMap,
                                                                 @Nullable final String sPath)
  {
    final RequestParamMapItem aPathObj = aMap.get (sPath);
    return aPathObj == null ? null : aPathObj.directGetChildren ();
  }

  /**
   * Iterate the root map down to the map specified by the passed path except
   * for the last element.
   *
   * @param aPath
   *        The path to iterate. May neither be <code>null</code> nor empty.
   * @return The map. May be <code>null</code> if the path did not find such a
   *         child.
   */
  @Nullable
  private Map <String, RequestParamMapItem> _getChildMapExceptLast (@Nonnull @Nonempty final String... aPath)
  {
    ValueEnforcer.notEmpty (aPath, "Path");

    Map <String, RequestParamMapItem> aMap = m_aMap;
    // Until the second last object
    for (int i = 0; i < aPath.length - 1; ++i)
    {
      aMap = _getChildMap (aMap, aPath[i]);
      if (aMap == null)
        return null;
    }
    return aMap;
  }

  public boolean contains (@Nonnull @Nonempty final String... aPath)
  {
    final Map <String, RequestParamMapItem> aMap = _getChildMapExceptLast (aPath);
    if (aMap == null)
      return false;
    final String sLastPathPart = ArrayHelper.getLast (aPath);
    if (aMap.containsKey (sLastPathPart))
      return true;

    for (final RequestParamMapItem aItem : aMap.values ())
      if (aItem.containsChild (sLastPathPart))
        return true;

    return false;
  }

  @Nullable
  public RequestParamMapItem getObject (@Nonnull @Nonempty final String... aPath)
  {
    final Map <String, RequestParamMapItem> aMap = _getChildMapExceptLast (aPath);
    return aMap == null ? null : aMap.get (ArrayHelper.getLast (aPath));
  }

  @Nullable
  public String getString (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : aItem.getValue ();
  }

  public boolean getBoolean (@Nonnull @Nonempty final String sPath, final boolean bDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? bDefault : AttributeValueConverter.getAsBoolean (sPath, aItem.getValue (), bDefault);
  }

  public double getDouble (@Nonnull @Nonempty final String sPath, final double dDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? dDefault : AttributeValueConverter.getAsDouble (sPath, aItem.getValue (), dDefault);
  }

  public int getInt (@Nonnull @Nonempty final String sPath, final int nDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? nDefault : AttributeValueConverter.getAsInt (sPath, aItem.getValue (), nDefault);
  }

  public long getLong (@Nonnull @Nonempty final String sPath, final long nDefault)
  {
    final RequestParamMapItem aItem = getObject (sPath);
    return aItem == null ? nDefault : AttributeValueConverter.getAsLong (sPath, aItem.getValue (), nDefault);
  }

  @Nullable
  public BigInteger getBigInteger (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : AttributeValueConverter.getAsBigInteger (ArrayHelper.getLast (aPath),
                                                                           aItem.getValue (),
                                                                           null);
  }

  @Nullable
  public BigDecimal getBigDecimal (@Nonnull @Nonempty final String... aPath)
  {
    final RequestParamMapItem aItem = getObject (aPath);
    return aItem == null ? null : AttributeValueConverter.getAsBigDecimal (ArrayHelper.getLast (aPath),
                                                                           aItem.getValue (),
                                                                           null);
  }

  @Nullable
  private Map <String, RequestParamMapItem> _getChildMapFully (@Nonnull final String... aPath)
  {
    Map <String, RequestParamMapItem> aMap = m_aMap;
    for (final String sPath : aPath)
    {
      aMap = _getChildMap (aMap, sPath);
      if (aMap == null)
        return null;
    }
    return aMap;
  }

  @Nullable
  public Map <String, String> getValueMap (@Nonnull @Nonempty final String... aPath)
  {
    final Map <String, RequestParamMapItem> aMap = _getChildMapFully (aPath);
    if (aMap == null)
      return null;
    return getAsValueMap (aMap);
  }

  @Nullable
  @ReturnsMutableCopy
  public IRequestParamMap getMap (@Nonnull @Nonempty final String... aPath)
  {
    ValueEnforcer.notEmpty (aPath, "Path");

    final Map <String, RequestParamMapItem> aMap = _getChildMapFully (aPath);
    if (aMap == null)
      return null;
    return new RequestParamMap (aMap);
  }

  public boolean containsKey (@Nullable final String sKey)
  {
    return m_aMap.containsKey (sKey);
  }

  public boolean isEmpty ()
  {
    return m_aMap.isEmpty ();
  }

  @Nonnegative
  public int getSize ()
  {
    return m_aMap.size ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public Set <String> keySet ()
  {
    return CollectionHelper.newSet (m_aMap.keySet ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public Collection <RequestParamMapItem> values ()
  {
    return CollectionHelper.newList (m_aMap.values ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <String, RequestParamMapItem> getAsObjectMap ()
  {
    return CollectionHelper.newMap (m_aMap);
  }

  @Nonnull
  @ReturnsMutableCopy
  public static Map <String, String> getAsValueMap (final Map <String, RequestParamMapItem> aMap) throws ClassCastException
  {
    ValueEnforcer.notNull (aMap, "Map");
    final Map <String, String> ret = new LinkedHashMap <String, String> (aMap.size ());
    for (final Map.Entry <String, RequestParamMapItem> aEntry : aMap.entrySet ())
      if (aEntry.getValue ().hasValue ())
        ret.put (aEntry.getKey (), aEntry.getValue ().getValue ());
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <String, String> getAsValueMap () throws ClassCastException
  {
    return getAsValueMap (m_aMap);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final RequestParamMap rhs = (RequestParamMap) o;
    return m_aMap.equals (rhs.m_aMap);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aMap).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("map", m_aMap).toString ();
  }

  @Nonnull
  public static IRequestParamMap create (@Nonnull final Map <String, Object> aAttrCont)
  {
    final RequestParamMap ret = new RequestParamMap ();
    for (final Map.Entry <String, Object> aEntry : aAttrCont.entrySet ())
      ret.put (aEntry.getKey (), aEntry.getValue ());
    return ret;
  }

  @Nonnull
  public static IRequestParamMap create (@Nonnull final IAttributeContainer <String, ?> aAttrCont)
  {
    final RequestParamMap ret = new RequestParamMap ();
    for (final String sName : aAttrCont.getAllAttributeNames ())
      ret.put (sName, aAttrCont.getAttributeObject (sName));
    return ret;
  }

  @Nonnull
  public static IRequestParamMap createFromRequest (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final RequestParamMap ret = new RequestParamMap ();
    for (final Map.Entry <String, String []> aEntry : aHttpRequest.getParameterMap ().entrySet ())
      if (aEntry.getValue ().length > 0)
        ret.put (aEntry.getKey (), aEntry.getValue ()[0]);
    return ret;
  }

  /**
   * This method doesn't make sense but it should stay, so that it's easy to
   * spot usage of this invalid method.
   *
   * @param sBaseName
   *        Base name
   * @return Base name as is.
   */
  @Nonnull
  @Nonempty
  @Deprecated
  public static String getFieldName (@Nonnull @Nonempty final String sBaseName)
  {
    ValueEnforcer.notEmpty (sBaseName, "BaseName");

    return sBaseName;
  }

  @Nonnull
  @Nonempty
  public static String getFieldName (@Nonnull @Nonempty final String sBaseName, @Nullable final String... aSuffixes)
  {
    ValueEnforcer.notEmpty (sBaseName, "BaseName");

    final StringBuilder aSB = new StringBuilder (sBaseName);
    if (aSuffixes != null)
      for (final String sSuffix : aSuffixes)
        aSB.append (s_sOpen).append (StringHelper.getNotNull (sSuffix)).append (s_sClose);
    return aSB.toString ();
  }

  @Nonnull
  @Nonempty
  public static String getFieldName (@Nonnull @Nonempty final String sBaseName, @Nullable final int... aSuffixes)
  {
    ValueEnforcer.notEmpty (sBaseName, "BaseName");

    final StringBuilder aSB = new StringBuilder (sBaseName);
    if (aSuffixes != null)
      for (final int nSuffix : aSuffixes)
        aSB.append (s_sOpen).append (nSuffix).append (s_sClose);
    return aSB.toString ();
  }

  public static void setSeparatorsToDefault ()
  {
    s_sOpen = DEFAULT_OPEN;
    s_sClose = DEFAULT_CLOSE;
  }

  /**
   * Set the separator chars to use.
   *
   * @param cOpen
   *        Open char
   * @param cClose
   *        Close char - must be different from open char!
   */
  public static void setSeparators (final char cOpen, final char cClose)
  {
    if (cOpen == cClose)
      throw new IllegalArgumentException ("Open and closing element may not be identical!");
    s_sOpen = Character.toString (cOpen);
    s_sClose = Character.toString (cClose);
  }

  /**
   * Set the separators to use.
   *
   * @param sOpen
   *        Open string. May neither be <code>null</code> nor empty.
   * @param sClose
   *        Close string. May neither be <code>null</code> nor empty.
   */
  public static void setSeparators (@Nonnull @Nonempty final String sOpen, @Nonnull @Nonempty final String sClose)
  {
    ValueEnforcer.notEmpty (sOpen, "Open");
    ValueEnforcer.notEmpty (sClose, "Close");
    if (sOpen.contains (sClose))
      throw new IllegalArgumentException ("open may not contain close");
    if (sClose.contains (sOpen))
      throw new IllegalArgumentException ("close may not contain open");
    s_sOpen = sOpen;
    s_sClose = sClose;
  }

  /**
   * @return The open char. By default this is "[". Never <code>null</code> nor
   *         empty.
   * @see #DEFAULT_OPEN
   */
  @Nonnull
  @Nonempty
  public static String getOpenSeparator ()
  {
    return s_sOpen;
  }

  /**
   * @return The close char. By default this is "]". Never <code>null</code> nor
   *         empty.
   * @see #DEFAULT_CLOSE
   */
  @Nonnull
  @Nonempty
  public static String getCloseSeparator ()
  {
    return s_sClose;
  }
}
