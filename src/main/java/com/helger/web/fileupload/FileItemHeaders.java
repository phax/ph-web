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
package com.helger.web.fileupload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.http.CHTTPHeader;

/**
 * Default implementation of the {@link IFileItemHeaders} interface.
 *
 * @author Michael C. Macaluso
 * @since 1.3
 */
@ThreadSafe
public class FileItemHeaders implements IFileItemHeaders, Serializable
{
  private final ReadWriteLock m_aRWLock = new ReentrantReadWriteLock ();

  /**
   * Map of <code>String</code> keys to a <code>List</code> of
   * <code>String</code> instances.
   */
  private final Map <String, List <String>> m_aHeaderNameToValueListMap = new HashMap <String, List <String>> ();

  /**
   * List to preserve order of headers as added. This would not be needed if a
   * <code>LinkedHashMap</code> could be used, but don't want to depend on 1.4.
   */
  private final List <String> m_aHeaderNameList = new ArrayList <String> ();

  @Nullable
  public String getHeader (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "HeaderName");

    final String sNameLower = sName.toLowerCase (Locale.US);

    m_aRWLock.readLock ().lock ();
    try
    {
      final List <String> aHeaderValueList = m_aHeaderNameToValueListMap.get (sNameLower);
      return ContainerHelper.getFirstElement (aHeaderValueList);
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  @Nullable
  public String getHeaderContentDisposition ()
  {
    return getHeader (CHTTPHeader.CONTENT_DISPOSITION);
  }

  @Nullable
  public String getHeaderContentType ()
  {
    return getHeader (CHTTPHeader.CONTENT_TYPE);
  }

  @Nullable
  public String getHeaderContentLength ()
  {
    return getHeader (CHTTPHeader.CONTENT_LENGTH);
  }

  @Nonnull
  public Iterator <String> getHeaders (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "HeaderName");

    final String sNameLower = sName.toLowerCase (Locale.US);

    m_aRWLock.readLock ().lock ();
    try
    {
      final List <String> aHeaderValueList = m_aHeaderNameToValueListMap.get (sNameLower);
      return ContainerHelper.getIterator (aHeaderValueList);
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  @Nonnull
  public Iterator <String> getHeaderNames ()
  {
    m_aRWLock.readLock ().lock ();
    try
    {
      return m_aHeaderNameList.iterator ();
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <String> getAllHeaderNames ()
  {
    m_aRWLock.readLock ().lock ();
    try
    {
      return ContainerHelper.newList (m_aHeaderNameList);
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Method to add header values to this instance.
   *
   * @param sName
   *        name of this header
   * @param sValue
   *        value of this header
   */
  public void addHeader (@Nonnull final String sName, @Nullable final String sValue)
  {
    ValueEnforcer.notNull (sName, "HeaderName");

    final String sNameLower = sName.toLowerCase (Locale.US);

    m_aRWLock.writeLock ().lock ();
    try
    {
      List <String> aHeaderValueList = m_aHeaderNameToValueListMap.get (sNameLower);
      if (aHeaderValueList == null)
      {
        aHeaderValueList = new ArrayList <String> ();
        m_aHeaderNameToValueListMap.put (sNameLower, aHeaderValueList);
        m_aHeaderNameList.add (sNameLower);
      }
      aHeaderValueList.add (sValue);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("headerNameToValueListMap", m_aHeaderNameToValueListMap).toString ();
  }
}
