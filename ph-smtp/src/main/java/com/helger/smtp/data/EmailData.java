/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.attr.StringMap;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Default implementation of the {@link IMutableEmailData} interface. Note: the
 * attribute container may only contain String values, otherwise the
 * serialization and deserialization will result in different results!
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class EmailData implements IMutableEmailData
{
  private EEmailType m_eEmailType;
  private IEmailAddress m_aFrom;
  private final ICommonsList <IEmailAddress> m_aReplyTo = new CommonsArrayList <> ();
  private final ICommonsList <IEmailAddress> m_aTo = new CommonsArrayList <> ();
  private final ICommonsList <IEmailAddress> m_aCc = new CommonsArrayList <> ();
  private final ICommonsList <IEmailAddress> m_aBcc = new CommonsArrayList <> ();
  private LocalDateTime m_aSentDateTime;
  private String m_sSubject;
  private String m_sBody;
  private IMutableEmailAttachmentList m_aAttachments;
  private final StringMap m_aCustomAttrs = new StringMap ();

  public EmailData (@Nonnull final EEmailType eEmailType)
  {
    setEmailType (eEmailType);
  }

  @Nonnull
  public EEmailType getEmailType ()
  {
    return m_eEmailType;
  }

  @Nonnull
  public final EmailData setEmailType (@Nonnull final EEmailType eEmailType)
  {
    ValueEnforcer.notNull (eEmailType, "EmailType");
    m_eEmailType = eEmailType;
    return this;
  }

  @Nullable
  public IEmailAddress getFrom ()
  {
    return m_aFrom;
  }

  @Nonnull
  public final EmailData setFrom (@Nullable final IEmailAddress sFrom)
  {
    m_aFrom = sFrom;
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsList <IEmailAddress> replyTo ()
  {
    return m_aReplyTo;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsList <IEmailAddress> to ()
  {
    return m_aTo;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsList <IEmailAddress> cc ()
  {
    return m_aCc;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsList <IEmailAddress> bcc ()
  {
    return m_aBcc;
  }

  @Nonnull
  public final EmailData setSentDateTime (@Nullable final LocalDateTime aSentDateTime)
  {
    m_aSentDateTime = PDTFactory.getWithMillisOnly (aSentDateTime);
    return this;
  }

  @Nullable
  public LocalDateTime getSentDateTime ()
  {
    return m_aSentDateTime;
  }

  @Nonnull
  public final EmailData setSubject (@Nullable final String sSubject)
  {
    m_sSubject = sSubject;
    return this;
  }

  @Nullable
  public String getSubject ()
  {
    return m_sSubject;
  }

  @Nonnull
  public final EmailData setBody (@Nullable final String sBody)
  {
    m_sBody = sBody;
    return this;
  }

  @Nullable
  public String getBody ()
  {
    return m_sBody;
  }

  @Nullable
  public IMutableEmailAttachmentList getAttachments ()
  {
    return m_aAttachments;
  }

  @Nonnull
  public final EmailData setAttachments (@Nullable final IEmailAttachmentList aAttachments)
  {
    if (aAttachments != null && !aAttachments.isEmpty ())
      m_aAttachments = new EmailAttachmentList (aAttachments);
    else
      m_aAttachments = null;
    return this;
  }

  @Nonnull
  @ReturnsMutableObject
  public StringMap attrs ()
  {
    return m_aCustomAttrs;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final EmailData rhs = (EmailData) o;
    return EqualsHelper.equals (m_aFrom, rhs.m_aFrom) &&
           EqualsHelper.equals (m_aReplyTo, rhs.m_aReplyTo) &&
           m_aTo.equals (rhs.m_aTo) &&
           m_aCc.equals (rhs.m_aCc) &&
           m_aBcc.equals (rhs.m_aBcc) &&
           EqualsHelper.equals (m_aSentDateTime, rhs.m_aSentDateTime) &&
           EqualsHelper.equals (m_sSubject, rhs.m_sSubject) &&
           EqualsHelper.equals (m_sBody, rhs.m_sBody) &&
           EqualsHelper.equals (m_aAttachments, rhs.m_aAttachments) &&
           m_aCustomAttrs.equals (rhs.m_aCustomAttrs);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aFrom)
                                       .append (m_aReplyTo)
                                       .append (m_aTo)
                                       .append (m_aCc)
                                       .append (m_aBcc)
                                       .append (m_aSentDateTime)
                                       .append (m_sSubject)
                                       .append (m_sBody)
                                       .append (m_aAttachments)
                                       .append (m_aCustomAttrs)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("from", m_aFrom)
                                       .append ("replyTo", m_aReplyTo)
                                       .append ("to", m_aTo)
                                       .append ("cc", m_aCc)
                                       .append ("bcc", m_aBcc)
                                       .append ("sendDate", m_aSentDateTime)
                                       .append ("subject", m_sSubject)
                                       .append ("body", m_sBody)
                                       .appendIfNotNull ("attachments", m_aAttachments)
                                       .append ("CustomAttrs", m_aCustomAttrs)
                                       .getToString ();
  }

  /**
   * Utility method for converting different fields to a single
   * {@link IMutableEmailData}.
   *
   * @param eEmailType
   *        The type of the email.
   * @param aSender
   *        The sender address.
   * @param aReceiver
   *        The receiver address.
   * @param sSubject
   *        The subject line.
   * @param sBody
   *        The mail body text.
   * @param aAttachments
   *        Any attachments to use. May be <code>null</code>.
   * @return The created {@link EmailData} and never <code>null</code>.
   */
  @Nonnull
  public static EmailData createEmailData (@Nonnull final EEmailType eEmailType,
                                           @Nullable final IEmailAddress aSender,
                                           @Nullable final IEmailAddress aReceiver,
                                           @Nullable final String sSubject,
                                           @Nullable final String sBody,
                                           @Nullable final IMutableEmailAttachmentList aAttachments)
  {
    final EmailData aEmailData = new EmailData (eEmailType);
    aEmailData.setFrom (aSender);
    if (aReceiver != null)
      aEmailData.to ().add (aReceiver);
    aEmailData.setSubject (sSubject);
    aEmailData.setBody (sBody);
    aEmailData.setAttachments (aAttachments);
    return aEmailData;
  }
}
