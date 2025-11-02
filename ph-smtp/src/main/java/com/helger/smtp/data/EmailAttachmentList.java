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
package com.helger.smtp.data;

import java.util.Collection;
import java.util.Iterator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.annotation.style.CodingStyleguideUnaware;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.state.EChange;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.collection.commons.ICommonsOrderedSet;

/**
 * This is the default implementation of the {@link IMutableEmailAttachmentList}
 * interface.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class EmailAttachmentList implements IMutableEmailAttachmentList
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EmailAttachmentList.class);

  private final ICommonsOrderedMap <String, IEmailAttachment> m_aMap = new CommonsLinkedHashMap <> ();

  public EmailAttachmentList ()
  {}

  public EmailAttachmentList (@Nullable final IEmailAttachmentList aAttachments)
  {
    if (aAttachments != null)
      for (final IEmailAttachment aAttachment : aAttachments)
        addAttachment (aAttachment);
  }

  public EmailAttachmentList (@Nullable final IEmailAttachment... aAttachments)
  {
    if (aAttachments != null)
      for (final IEmailAttachment aAttachment : aAttachments)
        addAttachment (aAttachment);
  }

  @Nonnegative
  public int size ()
  {
    return m_aMap.size ();
  }

  public boolean isEmpty ()
  {
    return m_aMap.isEmpty ();
  }

  public final void addAttachment (@NonNull final IEmailAttachment aAttachment)
  {
    ValueEnforcer.notNull (aAttachment, "Attachment");

    final String sKey = aAttachment.getFilename ();
    if (m_aMap.containsKey (sKey))
      LOGGER.warn ("Overwriting email attachment with filename '" + sKey + "'");
    m_aMap.put (sKey, aAttachment);
  }

  @NonNull
  public EChange removeAttachment (@Nullable final String sFilename)
  {
    return m_aMap.removeObject (sFilename);
  }

  @NonNull
  public EChange removeAll ()
  {
    return m_aMap.removeAll ();
  }

  public boolean containsAttachment (@Nullable final String sFilename)
  {
    return m_aMap.containsKey (sFilename);
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsOrderedSet <String> getAllAttachmentFilenames ()
  {
    return m_aMap.copyOfKeySet ();
  }

  @NonNull
  @ReturnsMutableObject ("speed")
  @CodingStyleguideUnaware
  Collection <IEmailAttachment> directGetAllAttachments ()
  {
    return m_aMap.values ();
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <IEmailAttachment> getAllAttachments ()
  {
    return m_aMap.copyOfValues ();
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <IEmailAttachmentDataSource> getAsDataSourceList ()
  {
    return new CommonsArrayList <> (m_aMap.values (), IEmailAttachment::getAsDataSource);
  }

  @NonNull
  public Iterator <IEmailAttachment> iterator ()
  {
    return m_aMap.values ().iterator ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final EmailAttachmentList rhs = (EmailAttachmentList) o;
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
    return new ToStringGenerator (this).append ("map", m_aMap).getToString ();
  }
}
