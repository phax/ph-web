/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ArrayHelper;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.collections.attrs.AbstractReadonlyAttributeContainer;
import com.helger.commons.collections.attrs.IReadonlyAttributeContainer;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.lang.CGStringHelper;
import com.helger.commons.lang.GenericReflection;
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
public final class RequestParamMap implements IRequestParamMap
{
  public static final String DEFAULT_OPEN = "[";
  public static final String DEFAULT_CLOSE = "]";
  private static final Logger s_aLogger = LoggerFactory.getLogger (RequestParamMap.class);

  /** The index open separator */
  private static String s_sOpen = DEFAULT_OPEN;
  /** The index close separator */
  private static String s_sClose = DEFAULT_CLOSE;

  private final Map <String, Object> m_aMap;

  public RequestParamMap ()
  {
    m_aMap = new HashMap <String, Object> ();
  }

  /**
   * This constructor is private, because it does not call the
   * {@link #put(String, Object)} method which does the main string parsing!
   *
   * @param aMap
   *        The map to use. May not be <code>null</code>.
   */
  private RequestParamMap (@Nonnull final Map <String, Object> aMap)
  {
    m_aMap = ValueEnforcer.notNull (aMap, "Map");
  }

  private void _recursiveAddItem (@Nonnull final Map <String, Object> aMap,
                                  @Nonnull final String sName,
                                  @Nullable final Object aValue)
  {
    final int nIndex = sName.indexOf (s_sOpen);
    if (nIndex == -1)
    {
      // Value level
      aMap.put (sName, aValue);
    }
    else
    {
      // Get the name until the first "["
      final String sPrefix = sName.substring (0, nIndex);

      // Ensure that the respective map is present
      final Object aPrefixValue = aMap.get (sPrefix);
      Map <String, Object> aChildMap = GenericReflection.<Object, Map <String, Object>> uncheckedCast (aPrefixValue);
      if (aChildMap == null)
      {
        aChildMap = new HashMap <String, Object> ();
        aMap.put (sPrefix, aChildMap);
      }

      // Recursively scan child items (starting at the first character after the
      // '[')
      _recursiveAddItem (aChildMap, sName.substring (nIndex + s_sOpen.length ()), aValue);
    }
  }

  public void put (@Nonnull final String sName, @Nullable final Object aValue)
  {
    // replace everything just to have opening separators ("[") left and only
    // one closing separator ("]") at the end
    String sRealName = StringHelper.replaceAll (sName, s_sClose + s_sOpen, s_sOpen);
    // Remove the remaining trailing closing separator
    sRealName = StringHelper.trimEnd (sRealName, s_sClose);
    // Start parsing
    _recursiveAddItem (m_aMap, sRealName, aValue);
  }

  @Nullable
  private static Map <String, Object> _getAsMap (@Nonnull final Map <String, Object> aMap, @Nullable final String sPath)
  {
    final Object aPathObj = aMap.get (sPath);
    if (aPathObj != null && !(aPathObj instanceof Map <?, ?>))
    {
      s_aLogger.warn ("You're trying to access the path element '" +
                      sPath +
                      "' as map, but it is a " +
                      CGStringHelper.getClassLocalName (aPathObj) +
                      "!");
      return null;
    }
    return GenericReflection.<Object, Map <String, Object>> uncheckedCast (aPathObj);
  }

  /**
   * Iterate the root map down to the map specified by the passed path.
   *
   * @param aPath
   *        The path to iterate. May neither be <code>null</code> nor empty.
   * @return The map. May be <code>null</code> if the path did not find such a
   *         child.
   */
  @Nullable
  private Map <String, Object> _getResolvedChildMap (@Nonnull @Nonempty final String... aPath)
  {
    ValueEnforcer.notEmpty (aPath, "Path");

    Map <String, Object> aMap = m_aMap;
    // Until the second last object
    for (int i = 0; i < aPath.length - 1; ++i)
    {
      aMap = _getAsMap (aMap, aPath[i]);
      if (aMap == null)
        return null;
    }
    return aMap;
  }

  public boolean contains (@Nonnull @Nonempty final String... aPath)
  {
    final Map <String, Object> aMap = _getResolvedChildMap (aPath);
    return aMap != null && aMap.containsKey (ArrayHelper.getLast (aPath));
  }

  @Nullable
  public Object getObject (@Nonnull @Nonempty final String... aPath)
  {
    final Map <String, Object> aMap = _getResolvedChildMap (aPath);
    return aMap == null ? null : aMap.get (ArrayHelper.getLast (aPath));
  }

  @Nullable
  public String getString (@Nonnull @Nonempty final String... aPath)
  {
    final Object aValue = getObject (aPath);
    return AbstractReadonlyAttributeContainer.getAsString (ArrayHelper.getLast (aPath), aValue, null);
  }

  @Nullable
  private Map <String, Object> _getResolvedMap (@Nonnull final String... aPath)
  {
    Map <String, Object> aMap = m_aMap;
    for (final String sPath : aPath)
    {
      aMap = _getAsMap (aMap, sPath);
      if (aMap == null)
        return null;
    }
    return aMap;
  }

  @Nullable
  public Map <String, String> getValueMap (@Nonnull @Nonempty final String... aPath)
  {
    final Map <String, Object> aMap = _getResolvedMap (aPath);
    if (aMap == null)
      return null;
    return getAsValueMap (aMap);
  }

  @Nullable
  @ReturnsMutableCopy
  public IRequestParamMap getMap (@Nonnull @Nonempty final String... aPath)
  {
    ValueEnforcer.notEmpty (aPath, "Path");

    final Map <String, Object> aMap = _getResolvedMap (aPath);
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
  public int size ()
  {
    return m_aMap.size ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public Set <String> keySet ()
  {
    return ContainerHelper.newSet (m_aMap.keySet ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public Collection <Object> values ()
  {
    return ContainerHelper.newList (m_aMap.values ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <String, Object> getAsObjectMap ()
  {
    return ContainerHelper.newMap (m_aMap);
  }

  @Nonnull
  @ReturnsMutableCopy
  public static Map <String, String> getAsValueMap (final Map <String, Object> aMap) throws ClassCastException
  {
    ValueEnforcer.notNull (aMap, "Map");
    final Map <String, String> ret = new HashMap <String, String> (aMap.size ());
    for (final Map.Entry <String, Object> aEntry : aMap.entrySet ())
      ret.put (aEntry.getKey (), (String) aEntry.getValue ());
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
    if (!(o instanceof RequestParamMap))
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
  public static IRequestParamMap create (@Nonnull final IReadonlyAttributeContainer aAttrCont)
  {
    final RequestParamMap ret = new RequestParamMap ();
    for (final String sName : aAttrCont.getAllAttributeNames ())
      ret.put (sName, aAttrCont.getAttributeObject (sName));
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
