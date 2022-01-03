/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
package com.helger.smtp.transport;

import java.nio.charset.Charset;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.MimeType;
import com.helger.commons.typeconvert.TypeConverter;
import com.helger.mail.address.InternetAddressHelper;
import com.helger.smtp.data.IEmailAttachmentDataSource;
import com.helger.smtp.data.IMutableEmailAttachmentList;
import com.helger.smtp.data.IMutableEmailData;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Fill a {@link MimeMessage} object with the data of an
 * {@link IMutableEmailData} object.
 *
 * @author Philip Helger
 */
@Immutable
public final class MailConverter
{
  private MailConverter ()
  {}

  public static void setSubject (@Nonnull final MimeMessage aMIMEMessage, @Nonnull final String sSubject, @Nonnull final Charset aCharset)
  {
    try
    {
      aMIMEMessage.setSubject (sSubject, aCharset.name ());
    }
    catch (final MessagingException ex)
    {
      throw new IllegalStateException ("Charset " + aCharset + " is unknown!", ex);
    }
  }

  public static void setText (@Nonnull final MimeBodyPart aMIMEBody, @Nonnull final String sText, @Nonnull final Charset aCharset)
  {
    try
    {
      aMIMEBody.setText (sText, aCharset.name ());
    }
    catch (final MessagingException ex)
    {
      throw new IllegalStateException ("Charset " + aCharset + " is unknown!", ex);
    }
  }

  public static void fillMimeMessageUnsafe (@Nonnull final MimeMessage aMIMEMessage,
                                            @Nonnull final IMutableEmailData aMailData,
                                            @Nullable final Charset aCharset) throws MessagingException
  {
    if (aMailData.getFrom () != null)
      aMIMEMessage.setFrom (InternetAddressHelper.getAsInternetAddress (aMailData.getFrom (), aCharset));
    aMIMEMessage.setReplyTo (InternetAddressHelper.getAsInternetAddressArray (aMailData.replyTo (), aCharset));
    aMIMEMessage.setRecipients (Message.RecipientType.TO, InternetAddressHelper.getAsInternetAddressArray (aMailData.to (), aCharset));
    aMIMEMessage.setRecipients (Message.RecipientType.CC, InternetAddressHelper.getAsInternetAddressArray (aMailData.cc (), aCharset));
    aMIMEMessage.setRecipients (Message.RecipientType.BCC, InternetAddressHelper.getAsInternetAddressArray (aMailData.bcc (), aCharset));
    if (aMailData.getSentDateTime () != null)
      aMIMEMessage.setSentDate (TypeConverter.convert (aMailData.getSentDateTime (), Date.class));
    if (aMailData.getSubject () != null)
      if (aCharset != null)
        setSubject (aMIMEMessage, aMailData.getSubject (), aCharset);
      else
        aMIMEMessage.setSubject (aMailData.getSubject ());

    final MimeMultipart aMixedMultipart = new MimeMultipart ();

    // build text part
    {
      final MimeBodyPart aBodyPart = new MimeBodyPart ();
      if (aMailData.getEmailType ().isHTML ())
      {
        // HTML text
        if (aCharset != null)
        {
          aBodyPart.setContent (aMailData.getBody (),
                                new MimeType (CMimeType.TEXT_HTML).addParameter (CMimeType.PARAMETER_NAME_CHARSET, aCharset.name ())
                                                                  .getAsString ());
        }
        else
          aBodyPart.setContent (aMailData.getBody (), CMimeType.TEXT_HTML.getAsString ());
      }
      else
      {
        // Plain text
        if (aCharset != null)
          setText (aBodyPart, aMailData.getBody (), aCharset);
        else
          aBodyPart.setText (aMailData.getBody ());
      }
      aMixedMultipart.addBodyPart (aBodyPart);
    }

    // Does the mail data contain attachments?
    final IMutableEmailAttachmentList aAttachments = aMailData.getAttachments ();
    if (aAttachments != null)
      for (final IEmailAttachmentDataSource aDS : aAttachments.getAsDataSourceList ())
      {
        final MimeBodyPart aAttachmentPart = new MimeBodyPart ();
        aAttachmentPart.setDisposition (aDS.getDisposition ().getID ());
        aAttachmentPart.setFileName (aDS.getName ());
        aAttachmentPart.setDataHandler (aDS.getAsDataHandler ());
        aMixedMultipart.addBodyPart (aAttachmentPart);
      }

    // set mixed multipart content
    aMIMEMessage.setContent (aMixedMultipart);
  }

  /**
   * Fill the {@link MimeMessage} object with the {@link IMutableEmailData}
   * elements.
   *
   * @param aMimeMessage
   *        The javax.mail object to be filled.
   * @param aMailData
   *        The mail data object that contains all the source information to be
   *        send.
   * @param aCharset
   *        The character set to be used for sending.
   * @throws RuntimeException
   *         in case of an error
   */
  public static void fillMimeMessage (@Nonnull final MimeMessage aMimeMessage,
                                      @Nonnull final IMutableEmailData aMailData,
                                      @Nullable final Charset aCharset)
  {
    try
    {
      fillMimeMessageUnsafe (aMimeMessage, aMailData, aCharset);
    }
    catch (final RuntimeException ex)
    {
      throw ex;
    }
    catch (final Exception ex)
    {
      throw new IllegalArgumentException (ex);
    }
  }
}
