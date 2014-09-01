/**
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

import com.helger.commons.IHasSize;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.datetime.PDTFactory;
import com.helger.web.datetime.PDTWebDateUtils;

/**
 * Abstracts HTTP header interface for external usage.
 * 
 * @author Philip Helger
 */
@NotThreadSafe
public class HTTPHeaderMap implements IHasSize, Iterable <Map.Entry <String, List <String>>>, Serializable
{
  private final Map <String, List <String>> m_aHeaders = new LinkedHashMap <String, List <String>> ();

  public HTTPHeaderMap ()
  {}

  public void reset ()
  {
    m_aHeaders.clear ();
  }

  @Nonnull
  @ReturnsMutableCopy
  private List <String> _getOrCreateHeaderList (final String sName)
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
  private static String _getAsString (@Nonnull final DateTime aDT)
  {
    // This method internally converts the date to UTC
    return PDTWebDateUtils.getAsStringRFC822 (aDT);
  }

  public void setDateHeader (@Nonnull @Nonempty final String sName, final long nMillis)
  {
    _setHeader (sName, _getAsString (PDTFactory.createDateTimeFromMillis (nMillis)));
  }

  public void setDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final DateTime aDT)
  {
    _setHeader (sName, _getAsString (aDT));
  }

  public void addDateHeader (@Nonnull @Nonempty final String sName, final long nMillis)
  {
    _addHeader (sName, _getAsString (PDTFactory.createDateTimeFromMillis (nMillis)));
  }

  public void addDateHeader (@Nonnull @Nonempty final String sName, @Nonnull final DateTime aDT)
  {
    _addHeader (sName, _getAsString (aDT));
  }

  public void setHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    _setHeader (sName, sValue);
  }

  public void addHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    _addHeader (sName, sValue);
  }

  public void setIntHeader (@Nonnull @Nonempty final String sName, final int nValue)
  {
    _setHeader (sName, Integer.toString (nValue));
  }

  public void addIntHeader (@Nonnull @Nonempty final String sName, final int nValue)
  {
    _addHeader (sName, Integer.toString (nValue));
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <String, List <String>> getAllHeaders ()
  {
    return ContainerHelper.newOrderedMap (m_aHeaders);
  }

  /**
   * @deprecated Use {@link #getHeaderValues(String)} instead
   */
  @Deprecated
  @Nonnull
  @ReturnsMutableCopy
  public List <String> getHeaders (@Nullable final String sName)
  {
    return getHeaderValues (sName);
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <String> getHeaderValues (@Nullable final String sName)
  {
    return ContainerHelper.newList (m_aHeaders.get (sName));
  }

  /**
   * @deprecated Use {@link #getHeaderValuesCaseInsensitive(String)} instead
   */
  @Deprecated
  @Nonnull
  @ReturnsMutableCopy
  public List <String> getHeaderValuesCaseInsenstive (@Nullable final String sName)
  {
    return getHeaderValuesCaseInsensitive (sName);
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <String> getHeaderValuesCaseInsensitive (@Nullable final String sName)
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
  public int size ()
  {
    return m_aHeaders.size ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("headers", m_aHeaders).toString ();
  }
}
