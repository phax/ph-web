/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.smtp.listener;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.smtp.data.IEmailData;
import com.helger.smtp.settings.ISMTPSettings;
import com.helger.smtp.transport.MailSendDetails;

import jakarta.mail.internet.MimeMessage;

/**
 * Email data transport event for later evaluation.
 *
 * @author Philip Helger
 */
@Immutable
public class EmailDataTransportEvent
{
  private final ISMTPSettings m_aSMTPSettings;
  private final IEmailData m_aEmailData;
  private final MimeMessage m_aMimeMessage;
  private final ICommonsList <MailSendDetails> m_aValidSent;
  private final ICommonsList <MailSendDetails> m_aValidUnsent;
  private final ICommonsList <MailSendDetails> m_aInvalid;

  public EmailDataTransportEvent (@Nonnull final ISMTPSettings aSMTPSettings,
                                  @Nonnull final IEmailData aEmailData,
                                  @Nonnull final MimeMessage aMimeMessage,
                                  @Nonnull final Collection <MailSendDetails> aValidSent,
                                  @Nonnull final Collection <MailSendDetails> aValidUnsent,
                                  @Nonnull final Collection <MailSendDetails> aInvalid)
  {
    ValueEnforcer.notNull (aSMTPSettings, "SMTPSettings");
    ValueEnforcer.notNull (aEmailData, "EmailData");
    ValueEnforcer.notNull (aMimeMessage, "MimeMessage");
    ValueEnforcer.notNull (aValidSent, "ValidSent");
    ValueEnforcer.notNull (aValidUnsent, "ValidUnsent");
    ValueEnforcer.notNull (aInvalid, "Invalid");

    m_aSMTPSettings = aSMTPSettings;
    m_aEmailData = aEmailData;
    m_aMimeMessage = aMimeMessage;
    m_aValidSent = new CommonsArrayList <> (aValidSent);
    m_aValidUnsent = new CommonsArrayList <> (aValidUnsent);
    m_aInvalid = new CommonsArrayList <> (aInvalid);
  }

  /**
   * @return The SMTP settings used for this message. Never <code>null</code>.
   */
  @Nonnull
  public ISMTPSettings getSMTPSettings ()
  {
    return m_aSMTPSettings;
  }

  /**
   * @return The original email data that was (not) sent. Never
   *         <code>null</code>.
   */
  @Nonnull
  public IEmailData getEmailData ()
  {
    return m_aEmailData;
  }

  /**
   * @return The created mime message that was (not) sent. Never
   *         <code>null</code>.
   */
  @Nonnull
  public MimeMessage getMimeMessage ()
  {
    return m_aMimeMessage;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <MailSendDetails> getValidSentAddresses ()
  {
    return m_aValidSent.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <MailSendDetails> getValidUnsentAddresses ()
  {
    return m_aValidUnsent.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <MailSendDetails> getInvalidAddresses ()
  {
    return m_aInvalid.getClone ();
  }
}
