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
package com.helger.web.smtp.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.joda.time.DateTime;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.collections.attrs.MapBasedAttributeContainer;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.equals.EqualsUtils;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.smtp.EEmailType;
import com.helger.web.smtp.IEmailAttachmentList;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.InternetAddressUtils;

/**
 * Default implementation of the {@link IEmailData} interface. Note: the
 * attribute container may only contain String values, otherwise the
 * serialization and deserialization will result in different results!
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class EmailData extends MapBasedAttributeContainer implements IEmailData
{
  private EEmailType m_eEmailType;
  private IEmailAddress m_aFrom;
  private final List <IEmailAddress> m_aReplyTo = new ArrayList <IEmailAddress> ();
  private final List <IEmailAddress> m_aTo = new ArrayList <IEmailAddress> ();
  private final List <IEmailAddress> m_aCc = new ArrayList <IEmailAddress> ();
  private final List <IEmailAddress> m_aBcc = new ArrayList <IEmailAddress> ();
  private DateTime m_aSentDate;
  private String m_sSubject;
  private String m_sBody;
  private IEmailAttachmentList m_aAttachments;

  public EmailData (@Nonnull final EEmailType eEmailType)
  {
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
  public final EmailData setFrom (@Nullable final String sFromAddress)
  {
    return setFrom (sFromAddress == null ? null : new EmailAddress (sFromAddress));
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
  public EmailData setReplyTo (@Nullable final String sReplyToAddress)
  {
    return setReplyTo (sReplyToAddress == null ? null : new EmailAddress (sReplyToAddress));
  }

  @Nonnull
  public final EmailData setReplyTo (@Nullable final IEmailAddress aReplyTo)
  {
    m_aReplyTo.clear ();
    if (aReplyTo != null)
      m_aReplyTo.add (aReplyTo);
    return this;
  }

  @Nonnull
  public final EmailData setReplyTo (@Nullable final IEmailAddress... aReplyTos)
  {
    m_aReplyTo.clear ();
    if (aReplyTos != null)
      for (final IEmailAddress aReplyTo : aReplyTos)
        if (aReplyTo != null)
          m_aReplyTo.add (aReplyTo);
    return this;
  }

  @Nonnull
  public final EmailData setReplyTo (@Nullable final List <? extends IEmailAddress> aReplyTos)
  {
    m_aReplyTo.clear ();
    if (aReplyTos != null)
      for (final IEmailAddress aReplyTo : aReplyTos)
        if (aReplyTo != null)
          m_aReplyTo.add (aReplyTo);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <? extends IEmailAddress> getReplyTo ()
  {
    return ContainerHelper.newList (m_aReplyTo);
  }

  @Nonnull
  @ReturnsMutableCopy
  private static InternetAddress [] _asArray (@Nonnull final List <IEmailAddress> aList, @Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                                                                         AddressException
  {
    final InternetAddress [] ret = new InternetAddress [aList.size ()];
    int i = 0;
    for (final IEmailAddress aMA : aList)
      ret[i++] = InternetAddressUtils.getAsInternetAddress (aMA, sCharset);
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  private static InternetAddress [] _asArray (@Nonnull final List <IEmailAddress> aList,
                                              @Nullable final Charset aCharset) throws AddressException
  {
    try
    {
      return _asArray (aList, aCharset == null ? null : aCharset.name ());
    }
    catch (final UnsupportedEncodingException ex)
    {
      throw new IllegalStateException ("Charset " + aCharset + " is unknown!", ex);
    }
  }

  @Nonnull
  @ReturnsMutableCopy
  @Deprecated
  public InternetAddress [] getReplyToArray (@Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                             AddressException
  {
    return _asArray (m_aReplyTo, sCharset);
  }

  @Nonnull
  @ReturnsMutableCopy
  public InternetAddress [] getReplyToArray (@Nullable final Charset aCharset) throws AddressException
  {
    return _asArray (m_aReplyTo, aCharset);
  }

  @Nonnegative
  public int getReplyToCount ()
  {
    return m_aReplyTo.size ();
  }

  @Nonnull
  public final EmailData setTo (@Nullable final String sToAddress)
  {
    return setTo (sToAddress == null ? null : new EmailAddress (sToAddress));
  }

  @Nonnull
  public final EmailData setTo (@Nullable final IEmailAddress aTo)
  {
    m_aTo.clear ();
    if (aTo != null)
      m_aTo.add (aTo);
    return this;
  }

  @Nonnull
  public final EmailData setTo (@Nullable final IEmailAddress... aTos)
  {
    m_aTo.clear ();
    if (aTos != null)
      for (final IEmailAddress aTo : aTos)
        if (aTo != null)
          m_aTo.add (aTo);
    return this;
  }

  @Nonnull
  public final EmailData setTo (@Nullable final List <? extends IEmailAddress> aTos)
  {
    m_aTo.clear ();
    if (aTos != null)
      for (final IEmailAddress aTo : aTos)
        if (aTo != null)
          m_aTo.add (aTo);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <? extends IEmailAddress> getTo ()
  {
    return ContainerHelper.newList (m_aTo);
  }

  @Nonnull
  @ReturnsMutableCopy
  @Deprecated
  public InternetAddress [] getToArray (@Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                        AddressException
  {
    return _asArray (m_aTo, sCharset);
  }

  @Nonnull
  @ReturnsMutableCopy
  public InternetAddress [] getToArray (@Nullable final Charset aCharset) throws AddressException
  {
    return _asArray (m_aTo, aCharset);
  }

  @Nonnegative
  public int getToCount ()
  {
    return m_aTo.size ();
  }

  @Nonnull
  public final EmailData setCc (@Nullable final String sCcAddress)
  {
    return setCc (sCcAddress == null ? null : new EmailAddress (sCcAddress));
  }

  @Nonnull
  public final EmailData setCc (@Nullable final IEmailAddress aCc)
  {
    m_aCc.clear ();
    if (aCc != null)
      m_aCc.add (aCc);
    return this;
  }

  @Nonnull
  public final EmailData setCc (@Nullable final IEmailAddress... aCcs)
  {
    m_aCc.clear ();
    if (aCcs != null)
      for (final IEmailAddress aCc : aCcs)
        if (aCc != null)
          m_aCc.add (aCc);
    return this;
  }

  @Nonnull
  public final EmailData setCc (@Nullable final List <? extends IEmailAddress> aCcs)
  {
    m_aCc.clear ();
    if (aCcs != null)
      for (final IEmailAddress aCc : aCcs)
        if (aCc != null)
          m_aCc.add (aCc);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <? extends IEmailAddress> getCc ()
  {
    return ContainerHelper.newList (m_aCc);
  }

  @Nonnull
  @ReturnsMutableCopy
  @Deprecated
  public InternetAddress [] getCcArray (@Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                        AddressException
  {
    return _asArray (m_aCc, sCharset);
  }

  @Nonnull
  @ReturnsMutableCopy
  public InternetAddress [] getCcArray (@Nullable final Charset aCharset) throws AddressException
  {
    return _asArray (m_aCc, aCharset);
  }

  @Nonnegative
  public int getCcCount ()
  {
    return m_aCc.size ();
  }

  @Nonnull
  public final EmailData setBcc (@Nullable final String sBccAddress)
  {
    return setBcc (sBccAddress == null ? null : new EmailAddress (sBccAddress));
  }

  @Nonnull
  public final EmailData setBcc (@Nullable final IEmailAddress aBcc)
  {
    m_aBcc.clear ();
    if (aBcc != null)
      m_aBcc.add (aBcc);
    return this;
  }

  @Nonnull
  public final EmailData setBcc (@Nullable final IEmailAddress... aBccs)
  {
    m_aBcc.clear ();
    if (aBccs != null)
      for (final IEmailAddress aBcc : aBccs)
        if (aBcc != null)
          m_aBcc.add (aBcc);
    return this;
  }

  @Nonnull
  public final EmailData setBcc (@Nullable final List <? extends IEmailAddress> aBccs)
  {
    m_aBcc.clear ();
    if (aBccs != null)
      for (final IEmailAddress aBcc : aBccs)
        if (aBcc != null)
          m_aBcc.add (aBcc);
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <? extends IEmailAddress> getBcc ()
  {
    return ContainerHelper.newList (m_aBcc);
  }

  @Nonnull
  @ReturnsMutableCopy
  @Deprecated
  public InternetAddress [] getBccArray (@Nullable final String sCharset) throws UnsupportedEncodingException,
                                                                         AddressException
  {
    return _asArray (m_aBcc, sCharset);
  }

  @Nonnull
  @ReturnsMutableCopy
  public InternetAddress [] getBccArray (@Nullable final Charset aCharset) throws AddressException
  {
    return _asArray (m_aBcc, aCharset);
  }

  @Nonnegative
  public int getBccCount ()
  {
    return m_aBcc.size ();
  }

  @Nonnull
  public final EmailData setSentDate (@Nullable final DateTime aSentDate)
  {
    m_aSentDate = aSentDate;
    return this;
  }

  @Nullable
  public DateTime getSentDate ()
  {
    return m_aSentDate;
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
  public IEmailAttachmentList getAttachments ()
  {
    return m_aAttachments;
  }

  @Nonnegative
  public int getAttachmentCount ()
  {
    return m_aAttachments == null ? 0 : m_aAttachments.size ();
  }

  @Nonnull
  public final EmailData setAttachments (@Nullable final IEmailAttachmentList aAttachments)
  {
    if (aAttachments != null && !aAttachments.isEmpty ())
      m_aAttachments = aAttachments;
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
    return EqualsUtils.equals (m_aFrom, rhs.m_aFrom) &&
           EqualsUtils.equals (m_aReplyTo, rhs.m_aReplyTo) &&
           m_aTo.equals (rhs.m_aTo) &&
           m_aCc.equals (rhs.m_aCc) &&
           m_aBcc.equals (rhs.m_aBcc) &&
           EqualsUtils.equals (m_aSentDate, rhs.m_aSentDate) &&
           EqualsUtils.equals (m_sSubject, rhs.m_sSubject) &&
           EqualsUtils.equals (m_sBody, rhs.m_sBody) &&
           EqualsUtils.equals (m_aAttachments, rhs.m_aAttachments);
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
                            .append (m_aSentDate)
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
                            .append ("sendDate", m_aSentDate)
                            .append ("subject", m_sSubject)
                            .append ("body", m_sBody)
                            .appendIfNotNull ("attachments", m_aAttachments)
                            .toString ();
  }

  /**
   * Utility method for converting different fields to a single
   * {@link IEmailData}.
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
                                           @Nullable final IEmailAttachmentList aAttachments)
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
