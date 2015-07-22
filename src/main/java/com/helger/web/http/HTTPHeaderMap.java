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
package com.helger.web.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.lang.IHasSize;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.datetime.PDTFactory;
import com.helger.web.datetime.PDTWebDateHelper;

/**
 * Abstracts HTTP header interface for external usage.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class HTTPHeaderMap implements IHasSize, Iterable <Map.Entry <String, List <String>>>, ICloneable <HTTPHeaderMap>, Serializable
{
  private final Map <String, List <String>> m_aHeaders = new LinkedHashMap <String, List <String>> ();

  /**
   * Default constructor.
   */
  public HTTPHeaderMap ()
  {}

  /**
   * Copy constructor.
   *
   * @param aOther
   *        Map to copy from. May not be <code>null</code>.
   * @since 6.0.5
   */
  public HTTPHeaderMap (@Nonnull final HTTPHeaderMap aOther)
  {
    ValueEnforcer.notNull (aOther, "Other");
    m_aHeaders.putAll (aOther.m_aHeaders);
  }

  /**
   * Remove all contained headers.
   *
   * @return {@link EChange}.
   * @since 6.0.5
   */
  @Nonnull
  public EChange clear ()
  {
    if (m_aHeaders.isEmpty ())
      return EChange.UNCHANGED;
    m_aHeaders.clear ();
    return EChange.CHANGED;
  }

  @Nonnull
  @ReturnsMutableObject ("design")
  private List <String> _getOrCreateHeaderList (@Nonnull @Nonempty final String sName)
  {
    List <String> aValues = m_aHeaders.get (sName);
    if (aValues == null)
    {
      aValues = new ArrayList <String> (2);
      m_aHeaders.put (sName, aValues);
    }
    return aValues;
  }

  private void _setHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notNull (sValue, "Value");

    final List <String> aValues = _getOrCreateHeaderList (sName);
    aValues.clear ();
    aValues.add (sValue);
  }

  private void _addHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notNull (sValue, "Value");

    _getOrCreateHeaderList (sName).add (sValue);
  }

  public void setContentLength (final int nLength)
  {
    _setHeader (CHTTPHeader.CONTENT_LENGTH, Integer.toString (nLength));
  }

  public void setContentType (@Nonnull final String sContentType)
  {
    _setHeader (CHTTPHeader.CONTENT_TYPE, sContentType);
  }

  @Nonnull
  public static String getDateTimeAsString (@Nonnull final DateTime aDT)
  {
    ValueEnforcer.notNull (aDT, "DateTime");

    // This method internally converts the date to UTC
    return PDTWebDateHelper.getAsStringRFC822 (aDT);
  }

  @Nonnull
  public static String getDateTimeAsString (@Nonnull final LocalDateTime aLDT)
  {
    ValueEnforcer.notNull (aLDT, "DateTime");

    // This method internally converts the date to UTC
    return PDTWebDateHelper.getAsStringRFC822 (aLDT);
  }

  /**
   * Set the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param nMillis
   *        The milliseconds to set as a date.
   */
  public void setDateHeader (@Nonnull @Nonempty final String sName, final long nMillis)
  {
    setDateHeader (sName, PDTFactory.createDateTimeFromMillis (nMillis));
  }

  /**
   * Set the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param aLD
   *        The LocalDate to set as a date. The time is set to start of day. May
   *        not be <code>null</code>.
   * @since 6.0.5
   */
  public void setDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final LocalDate aLD)
  {
    setDateHeader (sName, PDTFactory.createDateTime (aLD));
  }

  /**
   * Set the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param aLDT
   *        The LocalDateTime to set as a date. May not be <code>null</code>.
   * @since 6.0.5
   */
  public void setDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final LocalDateTime aLDT)
  {
    setDateHeader (sName, PDTFactory.createDateTime (aLDT));
  }

  /**
   * Set the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param aDT
   *        The DateTime to set as a date. May not be <code>null</code>.
   */
  public void setDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final DateTime aDT)
  {
    _setHeader (sName, getDateTimeAsString (aDT));
  }

  /**
   * Add the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param nMillis
   *        The milliseconds to set as a date.
   */
  public void addDateHeader (@Nonnull @Nonempty final String sName, final long nMillis)
  {
    addDateHeader (sName, PDTFactory.createDateTimeFromMillis (nMillis));
  }

  /**
   * Add the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param aLD
   *        The LocalDate to set as a date. The time is set to start of day. May
   *        not be <code>null</code>.
   * @since 6.0.5
   */
  public void addDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final LocalDate aLD)
  {
    addDateHeader (sName, PDTFactory.createDateTime (aLD));
  }

  /**
   * Add the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param aLDT
   *        The LocalDateTime to set as a date. May not be <code>null</code>.
   * @since 6.0.5
   */
  public void addDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final LocalDateTime aLDT)
  {
    addDateHeader (sName, PDTFactory.createDateTime (aLDT));
  }

  /**
   * Add the passed header as a date header.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param aDT
   *        The DateTime to set as a date. May not be <code>null</code>.
   */
  public void addDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final DateTime aDT)
  {
    _addHeader (sName, getDateTimeAsString (aDT));
  }

  /**
   * Set the passed header as is.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param sValue
   *        The value to be set. May not be <code>null</code>.
   */
  public void setHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    _setHeader (sName, sValue);
  }

  /**
   * Add the passed header as is.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param sValue
   *        The value to be set. May not be <code>null</code>.
   */
  public void addHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    _addHeader (sName, sValue);
  }

  /**
   * Set the passed header as a number.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param nValue
   *        The value to be set. May not be <code>null</code>.
   */
  public void setIntHeader (@Nonnull @Nonempty final String sName, final int nValue)
  {
    _setHeader (sName, Integer.toString (nValue));
  }

  /**
   * Add the passed header as a number.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param nValue
   *        The value to be set. May not be <code>null</code>.
   */
  public void addIntHeader (@Nonnull @Nonempty final String sName, final int nValue)
  {
    _addHeader (sName, Integer.toString (nValue));
  }

  /**
   * Set the passed header as a number.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param nValue
   *        The value to be set. May not be <code>null</code>.
   * @since 6.0.5
   */
  public void setLongHeader (@Nonnull @Nonempty final String sName, final long nValue)
  {
    _setHeader (sName, Long.toString (nValue));
  }

  /**
   * Add the passed header as a number.
   *
   * @param sName
   *        Header name. May neither be <code>null</code> nor empty.
   * @param nValue
   *        The value to be set. May not be <code>null</code>.
   * @since 6.0.5
   */
  public void addLongHeader (@Nonnull @Nonempty final String sName, final long nValue)
  {
    _addHeader (sName, Long.toString (nValue));
  }

  /**
   * Add all headers from the passed map.
   *
   * @param aOther
   *        The header map to add. May not be <code>null</code>.
   * @since 6.0.5
   */
  public void addAllHeaders (@Nonnull final HTTPHeaderMap aOther)
  {
    ValueEnforcer.notNull (aOther, "Other");

    for (final Map.Entry <String, List <String>> aEntry : aOther.m_aHeaders.entrySet ())
      for (final String sValue : aEntry.getValue ())
        _addHeader (aEntry.getKey (), sValue);
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <String, List <String>> getAllHeaders ()
  {
    return CollectionHelper.newOrderedMap (m_aHeaders);
  }

  /**
   * Get all header values doing a case sensitive match
   *
   * @param sName
   *        The name to be searched.
   * @return The list with all matching values. Never <code>null</code> but
   *         maybe empty.
   * @since 6.0.5
   */
  @Nonnull
  @ReturnsMutableCopy
  public List <String> getAllHeaderValues (@Nullable final String sName)
  {
    return CollectionHelper.newList (m_aHeaders.get (sName));
  }

  /**
   * Get all header values doing a case insensitive match
   *
   * @param sName
   *        The name to be searched.
   * @return The list with all matching values. Never <code>null</code> but
   *         maybe empty.
   * @since 6.0.5
   */
  @Nonnull
  @ReturnsMutableCopy
  public List <String> getAllHeaderValuesCaseInsensitive (@Nullable final String sName)
  {
    final List <String> ret = new ArrayList <String> ();
    if (StringHelper.hasText (sName))
      for (final Map.Entry <String, List <String>> aEntry : m_aHeaders.entrySet ())
        if (sName.equalsIgnoreCase (aEntry.getKey ()))
        {
          ret.addAll (aEntry.getValue ());
          break;
        }
    return ret;
  }

  public boolean containsHeaders (@Nullable final String sName)
  {
    return m_aHeaders.containsKey (sName);
  }

  public boolean containsHeadersCaseInsensitive (@Nullable final String sName)
  {
    if (StringHelper.hasText (sName))
      for (final String sHeaderName : m_aHeaders.keySet ())
        if (sName.equalsIgnoreCase (sHeaderName))
          return true;
    return false;
  }

  @Nonnull
  public EChange removeHeaders (@Nullable final String sName)
  {
    return EChange.valueOf (m_aHeaders.remove (sName) != null);
  }

  @Nonnull
  public Iterator <Map.Entry <String, List <String>>> iterator ()
  {
    return m_aHeaders.entrySet ().iterator ();
  }

  public boolean isEmpty ()
  {
    return m_aHeaders.isEmpty ();
  }

  @Nonnegative
  public int getSize ()
  {
    return m_aHeaders.size ();
  }

  /**
   * @since 6.0.5
   */
  @Nonnull
  @ReturnsMutableCopy
  public HTTPHeaderMap getClone ()
  {
    return new HTTPHeaderMap (this);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("headers", m_aHeaders).toString ();
  }
}
