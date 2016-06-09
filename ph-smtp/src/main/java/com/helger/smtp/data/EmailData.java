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
package com.helger.smtp.data;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.attr.MapBasedAttributeContainerAny;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.CommonsTreeMap;
import com.helger.commons.collection.ext.ICommonsList;
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
public class EmailData extends MapBasedAttributeContainerAny <String> implements IMutableEmailData
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

  public EmailData (@Nonnull final EEmailType eEmailType)
  {
    // CommonsTreeMap for consistent serialization compared to a regular HashMap
    super (true, new CommonsTreeMap <> ());
    setEmailType (eEmailType);
  }

  @Nonnull
  public final EmailData setEmailType (@Nonnull final EEmailType eEmailType)
  {
    ValueEnforcer.notNull (eEmailType, "EmailType");
    m_eEmailType = eEmailType;
    return this;
  }

  @Nonnull
  public EEmailType getEmailType ()
  {
    return m_eEmailType;
  }

  @Nonnull
  public final EmailData setFrom (@Nullable final IEmailAddress sFrom)
  {
    m_aFrom = sFrom;
    return this;
  }

  @Nullable
  public IEmailAddress getFrom ()
  {
    return m_aFrom;
  }

  @Nonnull
  public EmailData removeAllReplyTo ()
  {
    m_aReplyTo.clear ();
    return this;
  }

  @Nonnull
  public EmailData addReplyTo (@Nullable final IEmailAddress aReplyTo)
  {
    if (aReplyTo != null)
      m_aReplyTo.add (aReplyTo);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <? extends IEmailAddress> getAllReplyTo ()
  {
    return m_aReplyTo.getClone ();
  }

  @Nonnegative
  public int getReplyToCount ()
  {
    return m_aReplyTo.size ();
  }

  @Nonnull
  public EmailData removeAllTo ()
  {
    m_aTo.clear ();
    return this;
  }

  @Nonnull
  public EmailData addTo (@Nullable final IEmailAddress aTo)
  {
    if (aTo != null)
      m_aTo.add (aTo);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <? extends IEmailAddress> getAllTo ()
  {
    return m_aTo.getClone ();
  }

  public void forEachTo (@Nonnull final Consumer <? super IEmailAddress> aConsumer)
  {
    m_aTo.forEach (aConsumer);
  }

  @Nonnegative
  public int getToCount ()
  {
    return m_aTo.size ();
  }

  @Nonnull
  public EmailData removeAllCc ()
  {
    m_aCc.clear ();
    return this;
  }

  @Nonnull
  public EmailData addCc (@Nullable final IEmailAddress aCc)
  {
    if (aCc != null)
      m_aCc.add (aCc);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <? extends IEmailAddress> getAllCc ()
  {
    return m_aCc.getClone ();
  }

  public void forEachCc (@Nonnull final Consumer <? super IEmailAddress> aConsumer)
  {
    m_aCc.forEach (aConsumer);
  }

  @Nonnegative
  public int getCcCount ()
  {
    return m_aCc.size ();
  }

  @Nonnull
  public EmailData removeAllBcc ()
  {
    m_aBcc.clear ();
    return this;
  }

  @Nonnull
  public EmailData addBcc (@Nullable final IEmailAddress aBcc)
  {
    if (aBcc != null)
      m_aBcc.add (aBcc);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <? extends IEmailAddress> getAllBcc ()
  {
    return m_aBcc.getClone ();
  }

  public void forEachBcc (@Nonnull final Consumer <? super IEmailAddress> aConsumer)
  {
    m_aBcc.forEach (aConsumer);
  }

  @Nonnegative
  public int getBccCount ()
  {
    return m_aBcc.size ();
  }

  @Nonnull
  public final EmailData setSentDateTime (@Nullable final LocalDateTime aSentDateTime)
  {
    m_aSentDateTime = aSentDateTime;
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

  @Nonnegative
  public int getAttachmentCount ()
  {
    return m_aAttachments == null ? 0 : m_aAttachments.getSize ();
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

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!super.equals (o))
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
           EqualsHelper.equals (m_aAttachments, rhs.m_aAttachments);
  }

  @Override
  public int hashCode ()
  {
    return HashCodeGenerator.getDerived (super.hashCode ())
                            .append (m_aFrom)
                            .append (m_aReplyTo)
                            .append (m_aTo)
                            .append (m_aCc)
                            .append (m_aBcc)
                            .append (m_aSentDateTime)
                            .append (m_sSubject)
                            .append (m_sBody)
                            .append (m_aAttachments)
                            .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("from", m_aFrom)
                            .appendIfNotNull ("replyTo", m_aReplyTo)
                            .append ("to", m_aTo)
                            .appendIfNotNull ("cc", m_aCc)
                            .appendIfNotNull ("bcc", m_aBcc)
                            .append ("sendDate", m_aSentDateTime)
                            .append ("subject", m_sSubject)
                            .append ("body", m_sBody)
                            .appendIfNotNull ("attachments", m_aAttachments)
                            .toString ();
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
    aEmailData.setTo (aReceiver);
    aEmailData.setSubject (sSubject);
    aEmailData.setBody (sBody);
    aEmailData.setAttachments (aAttachments);
    return aEmailData;
  }
}
