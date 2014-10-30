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
package com.helger.web.smtp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;

import com.helger.commons.annotations.ContainsSoftMigration;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.convert.IMicroTypeConverter;
import com.helger.commons.microdom.convert.MicroTypeConverter;
import com.helger.commons.microdom.impl.MicroElement;
import com.helger.commons.microdom.utils.MicroUtils;
import com.helger.datetime.config.PDTConfig;
import com.helger.web.datetime.PDTWebDateUtils;
import com.helger.web.smtp.EEmailType;

public final class EmailDataMicroTypeConverter implements IMicroTypeConverter
{
  private static final String ATTR_TYPE = "type";
  private static final String ATTR_ADDRESS = "address";
  private static final String ATTR_PERSONAL = "personal";
  private static final String ELEMENT_FROM = "from";
  private static final String ELEMENT_REPLYTO = "replyto";
  private static final String ELEMENT_TO = "to";
  private static final String ELEMENT_CC = "cc";
  private static final String ELEMENT_BCC = "bcc";
  private static final String ELEMENT_SENTDATE = "sentdate";
  private static final String ATTR_SUBJECT = "subject";
  private static final String ELEMENT_BODY = "body";
  private static final String ELEMENT_ATTACHMENTS = "attachments";
  private static final String ELEMENT_CUSTOM = "custom";
  private static final String ATTR_ID = "id";

  private static void _writeEmailAddress (@Nonnull final IMicroElement eParent,
                                          @Nonnull final IEmailAddress aEmailAddress)
  {
    eParent.setAttribute (ATTR_ADDRESS, aEmailAddress.getAddress ());
    if (aEmailAddress.getPersonal () != null)
      eParent.setAttribute (ATTR_PERSONAL, aEmailAddress.getPersonal ());
  }

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final Object aSource,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final EmailData aEmailData = (EmailData) aSource;
    final IMicroElement eEmailData = new MicroElement (sNamespaceURI, sTagName);
    eEmailData.setAttribute (ATTR_TYPE, aEmailData.getEmailType ().getID ());

    if (aEmailData.getFrom () != null)
      _writeEmailAddress (eEmailData.appendElement (sNamespaceURI, ELEMENT_FROM), aEmailData.getFrom ());
    for (final IEmailAddress aReplyTo : aEmailData.getReplyTo ())
      _writeEmailAddress (eEmailData.appendElement (sNamespaceURI, ELEMENT_REPLYTO), aReplyTo);
    for (final IEmailAddress aTo : aEmailData.getTo ())
      _writeEmailAddress (eEmailData.appendElement (sNamespaceURI, ELEMENT_TO), aTo);
    for (final IEmailAddress aCc : aEmailData.getCc ())
      _writeEmailAddress (eEmailData.appendElement (sNamespaceURI, ELEMENT_CC), aCc);
    for (final IEmailAddress aBcc : aEmailData.getBcc ())
      _writeEmailAddress (eEmailData.appendElement (sNamespaceURI, ELEMENT_BCC), aBcc);

    if (aEmailData.getSentDate () != null)
      eEmailData.setAttribute (ELEMENT_SENTDATE,
                               PDTWebDateUtils.getAsStringXSD (aEmailData.getSentDate ()
                                                                         .toDateTime (PDTConfig.getDateTimeZoneUTC ())));

    if (aEmailData.getSubject () != null)
      eEmailData.setAttribute (ATTR_SUBJECT, aEmailData.getSubject ());

    if (aEmailData.getBody () != null)
      eEmailData.appendElement (sNamespaceURI, ELEMENT_BODY).appendText (aEmailData.getBody ());

    eEmailData.appendChild (MicroTypeConverter.convertToMicroElement (aEmailData.getAttachments (),
                                                                      sNamespaceURI,
                                                                      ELEMENT_ATTACHMENTS));

    for (final Map.Entry <String, Object> aEntry : ContainerHelper.getSortedByKey (aEmailData.getAllAttributes ())
                                                                  .entrySet ())
    {
      final IMicroElement eCustom = eEmailData.appendElement (sNamespaceURI, ELEMENT_CUSTOM);
      eCustom.setAttribute (ATTR_ID, aEntry.getKey ());
      eCustom.appendText (String.valueOf (aEntry.getValue ()));
    }

    return eEmailData;
  }

  @Nonnull
  private static IEmailAddress _readEmailAddress (@Nonnull final IMicroElement eElement)
  {
    final String sAddress = eElement.getAttributeValue (ATTR_ADDRESS);
    final String sPersonal = eElement.getAttributeValue (ATTR_PERSONAL);
    return new EmailAddress (sAddress, sPersonal);
  }

  @Nonnull
  @ContainsSoftMigration
  public EmailData convertToNative (@Nonnull final IMicroElement eEmailData)
  {
    final String sEmailType = eEmailData.getAttributeValue (ATTR_TYPE);
    final EEmailType eEmailType = EEmailType.getFromIDOrNull (sEmailType);
    final EmailData aEmailData = new EmailData (eEmailType);

    final IMicroElement eFrom = eEmailData.getFirstChildElement (ELEMENT_FROM);
    aEmailData.setFrom (_readEmailAddress (eFrom));

    final List <IEmailAddress> aReplyTos = new ArrayList <IEmailAddress> ();
    for (final IMicroElement eReplyTo : eEmailData.getAllChildElements (ELEMENT_REPLYTO))
      aReplyTos.add (_readEmailAddress (eReplyTo));
    aEmailData.setReplyTo (aReplyTos);

    final List <IEmailAddress> aTos = new ArrayList <IEmailAddress> ();
    for (final IMicroElement eTo : eEmailData.getAllChildElements (ELEMENT_TO))
      aTos.add (_readEmailAddress (eTo));
    aEmailData.setTo (aTos);

    final List <IEmailAddress> aCcs = new ArrayList <IEmailAddress> ();
    for (final IMicroElement eCc : eEmailData.getAllChildElements (ELEMENT_CC))
      aCcs.add (_readEmailAddress (eCc));
    aEmailData.setCc (aCcs);

    final List <IEmailAddress> aBccs = new ArrayList <IEmailAddress> ();
    for (final IMicroElement eBcc : eEmailData.getAllChildElements (ELEMENT_BCC))
      aBccs.add (_readEmailAddress (eBcc));
    aEmailData.setBcc (aBccs);

    final String sSentDate = eEmailData.getAttributeValue (ELEMENT_SENTDATE);
    if (sSentDate != null)
    {
      final DateTime aDT = PDTWebDateUtils.getDateTimeFromXSD (sSentDate);
      if (aDT != null)
        aEmailData.setSentDate (aDT.withChronology (PDTConfig.getDefaultChronology ()));
    }

    aEmailData.setSubject (eEmailData.getAttributeValue (ATTR_SUBJECT));
    aEmailData.setBody (MicroUtils.getChildTextContent (eEmailData, ELEMENT_BODY));

    final IMicroElement eAttachments = eEmailData.getFirstChildElement (ELEMENT_ATTACHMENTS);
    if (eAttachments != null)
    {
      // Default way: use converter
      aEmailData.setAttachments (MicroTypeConverter.convertToNative (eAttachments, EmailAttachmentList.class));
    }
    else
    {
      // For legacy stuff:
      final EmailAttachmentList aAttachments = new EmailAttachmentList ();
      for (final IMicroElement eAttachment : eEmailData.getAllChildElements ("attachment"))
        aAttachments.addAttachment (MicroTypeConverter.convertToNative (eAttachment, EmailAttachment.class));
      if (!aAttachments.isEmpty ())
        aEmailData.setAttachments (aAttachments);
    }

    for (final IMicroElement eCustom : eEmailData.getAllChildElements (ELEMENT_CUSTOM))
      aEmailData.setAttribute (eCustom.getAttributeValue (ATTR_ID), eCustom.getTextContent ());

    return aEmailData;
  }
}
