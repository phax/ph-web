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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.CodingStyleguideUnaware;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.state.EChange;
import com.helger.commons.string.ToStringGenerator;

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

  public final void addAttachment (@Nonnull final IEmailAttachment aAttachment)
  {
    ValueEnforcer.notNull (aAttachment, "Attachment");

    final String sKey = aAttachment.getFilename ();
    if (m_aMap.containsKey (sKey))
      LOGGER.warn ("Overwriting email attachment with filename '" + sKey + "'");
    m_aMap.put (sKey, aAttachment);
  }

  @Nonnull
  public EChange removeAttachment (@Nullable final String sFilename)
  {
    return m_aMap.removeObject (sFilename);
  }

  @Nonnull
  public EChange removeAll ()
  {
    return m_aMap.removeAll ();
  }

  public boolean containsAttachment (@Nullable final String sFilename)
  {
    return m_aMap.containsKey (sFilename);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsOrderedSet <String> getAllAttachmentFilenames ()
  {
    return m_aMap.copyOfKeySet ();
  }

  @Nonnull
  @ReturnsMutableObject ("speed")
  @CodingStyleguideUnaware
  Collection <IEmailAttachment> directGetAllAttachments ()
  {
    return m_aMap.values ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <IEmailAttachment> getAllAttachments ()
  {
    return m_aMap.copyOfValues ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <IEmailAttachmentDataSource> getAsDataSourceList ()
  {
    return new CommonsArrayList <> (m_aMap.values (), IEmailAttachment::getAsDataSource);
  }

  @Nonnull
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
