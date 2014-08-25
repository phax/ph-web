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
package com.helger.web.smtp.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.annotations.ReturnsMutableObject;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.io.IInputStreamProvider;
import com.helger.commons.state.EChange;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.smtp.IEmailAttachment;
import com.helger.web.smtp.IEmailAttachmentDataSource;
import com.helger.web.smtp.IEmailAttachmentList;

/**
 * This is the default implementation of the {@link IEmailAttachmentList}
 * interface.
 * 
 * @author Philip Helger
 */
@NotThreadSafe
public class EmailAttachmentList implements IEmailAttachmentList
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (EmailAttachmentList.class);

  private final Map <String, IEmailAttachment> m_aMap = new LinkedHashMap <String, IEmailAttachment> ();

  public EmailAttachmentList ()
  {}

  @Nonnegative
  public int size ()
  {
    return m_aMap.size ();
  }

  public boolean isEmpty ()
  {
    return m_aMap.isEmpty ();
  }

  public <ISP extends IInputStreamProvider & Serializable> void addAttachment (@Nonnull final String sFilename,
                                                                               @Nonnull final ISP aISS)
  {
    addAttachment (new EmailAttachment (sFilename, aISS));
  }

  public void addAttachment (@Nonnull final IEmailAttachment aAttachment)
  {
    ValueEnforcer.notNull (aAttachment, "Attachment");

    final String sKey = aAttachment.getFilename ();
    if (m_aMap.containsKey (sKey))
      s_aLogger.warn ("Overwriting email attachment with filename '" + sKey + "'");
    m_aMap.put (sKey, aAttachment);
  }

  @Nonnull
  public EChange removeAttachment (@Nullable final String sFilename)
  {
    return EChange.valueOf (m_aMap.remove (sFilename) != null);
  }

  @Nonnull
  public EChange clear ()
  {
    if (m_aMap.isEmpty ())
      return EChange.UNCHANGED;
    m_aMap.clear ();
    return EChange.CHANGED;
  }

  public boolean containsAttachment (@Nullable final String sFilename)
  {
    return m_aMap.containsKey (sFilename);
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <String> getAllAttachmentFilenames ()
  {
    return ContainerHelper.newList (m_aMap.keySet ());
  }

  @Nonnull
  @ReturnsMutableObject (reason = "speed")
  Collection <IEmailAttachment> directGetAllAttachments ()
  {
    return m_aMap.values ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <IEmailAttachment> getAllAttachments ()
  {
    return ContainerHelper.newList (m_aMap.values ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <IEmailAttachmentDataSource> getAsDataSourceList ()
  {
    final List <IEmailAttachmentDataSource> ret = new ArrayList <IEmailAttachmentDataSource> ();
    for (final IEmailAttachment aAttachment : m_aMap.values ())
      ret.add (aAttachment.getAsDataSource ());
    return ret;
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
    return new ToStringGenerator (this).append ("map", m_aMap).toString ();
  }
}
