/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.parse;

import java.util.Iterator;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.IteratorHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.fileupload.IFileItemHeaders;

/**
 * Default implementation of the {@link IFileItemHeaders} interface.
 *
 * @author Michael C. Macaluso
 * @since 1.3
 */
@ThreadSafe
public class FileItemHeaders implements IFileItemHeaders
{
  private final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();

  /**
   * Map of <code>String</code> keys to a <code>List</code> of
   * <code>String</code> instances.
   */
  private final ICommonsMap <String, ICommonsList <String>> m_aHeaderNameToValueListMap = new CommonsHashMap <> ();

  /**
   * List to preserve order of headers as added.
   */
  private final ICommonsOrderedSet <String> m_aHeaderNameList = new CommonsLinkedHashSet <> ();

  @Nullable
  public String getHeader (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "HeaderName");

    final String sNameLower = sName.toLowerCase (Locale.US);

    final ICommonsList <String> aHeaderValueList = m_aRWLock.readLocked ( () -> m_aHeaderNameToValueListMap.get (sNameLower));
    return aHeaderValueList == null ? null : aHeaderValueList.getFirst ();
  }

  @Nullable
  public String getHeaderContentDisposition ()
  {
    return getHeader (CHttpHeader.CONTENT_DISPOSITION);
  }

  @Nullable
  public String getHeaderContentType ()
  {
    return getHeader (CHttpHeader.CONTENT_TYPE);
  }

  @Nullable
  public String getHeaderContentLength ()
  {
    return getHeader (CHttpHeader.CONTENT_LENGTH);
  }

  @Nonnull
  public Iterator <String> getHeaders (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "HeaderName");

    final String sNameLower = sName.toLowerCase (Locale.US);

    final ICommonsList <String> aHeaderValueList = m_aRWLock.readLocked ( () -> m_aHeaderNameToValueListMap.get (sNameLower));
    return IteratorHelper.getIterator (aHeaderValueList);
  }

  @Nonnull
  public Iterator <String> getHeaderNames ()
  {
    return m_aRWLock.readLocked (m_aHeaderNameList::iterator);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <String> getAllHeaderNames ()
  {
    return m_aRWLock.readLocked (m_aHeaderNameList::getCopyAsList);
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

    m_aRWLock.writeLocked ( () -> {
      ICommonsList <String> aHeaderValueList = m_aHeaderNameToValueListMap.get (sNameLower);
      if (aHeaderValueList == null)
      {
        aHeaderValueList = new CommonsArrayList <> ();
        m_aHeaderNameToValueListMap.put (sNameLower, aHeaderValueList);
        m_aHeaderNameList.add (sNameLower);
      }
      aHeaderValueList.add (sValue);
    });
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("headerNameToValueListMap", m_aHeaderNameToValueListMap).getToString ();
  }
}
