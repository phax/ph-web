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
package com.helger.smtp.failed;

import java.time.LocalDateTime;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.email.IEmailAddress;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.id.factory.GlobalIDFactory;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.base.type.ITypedObject;
import com.helger.base.type.ObjectType;
import com.helger.datetime.helper.PDTFactory;
import com.helger.smtp.data.IMutableEmailData;
import com.helger.smtp.settings.ISMTPSettings;
import com.helger.smtp.transport.MailTransportError;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * This class represents a single email that was tried to be send out but failed.
 *
 * @author Philip Helger
 */
@Immutable
public class FailedMailData implements ITypedObject <String>
{
  public static final ObjectType TYPE_FAILEDMAIL = new ObjectType ("failedmail");

  private final String m_sID;
  private final LocalDateTime m_aErrorDT;
  private final ISMTPSettings m_aSettings;
  private final LocalDateTime m_aOriginalSentDateTime;
  private final IMutableEmailData m_aEmailData;
  private final MailTransportError m_aError;

  /**
   * Constructor for message unspecific error.
   *
   * @param aSettings
   *        The mail settings for which the error occurs. Never <code>null</code>.
   * @param aError
   *        The exception that occurred. Never <code>null</code>.
   */
  public FailedMailData (@Nonnull final ISMTPSettings aSettings, @Nonnull final MailTransportError aError)
  {
    this (aSettings, null, aError);
  }

  /**
   * Constructor for message specific error.
   *
   * @param aSettings
   *        The mail settings for which the error occurs. Never <code>null</code>.
   * @param aEmailData
   *        The message that failed to send. May not be <code>null</code> in practice.
   */
  public FailedMailData (@Nonnull final ISMTPSettings aSettings, @Nullable final IMutableEmailData aEmailData)
  {
    this (aSettings, aEmailData, (MailTransportError) null);
  }

  /**
   * Constructor for message specific error.
   *
   * @param aSettings
   *        The mail settings for which the error occurs. Never <code>null</code>.
   * @param aEmailData
   *        The message that failed to send. May be <code>null</code> if it is a mail-independent
   *        error.
   * @param aError
   *        The exception that occurred. May be <code>null</code>.
   */
  public FailedMailData (@Nonnull final ISMTPSettings aSettings,
                         @Nullable final IMutableEmailData aEmailData,
                         @Nullable final MailTransportError aError)
  {
    this (GlobalIDFactory.getNewPersistentStringID (),
          PDTFactory.getCurrentLocalDateTimeMillisOnly (),
          aSettings,
          aEmailData == null ? null : aEmailData.getSentDateTime (),
          aEmailData,
          aError);
  }

  /**
   * Constructor for serialization only!
   *
   * @param sID
   *        The ID of this object. Never <code>null</code>.
   * @param aErrorDT
   *        The date and time when the error occurred. Never <code>null</code>.
   * @param aSettings
   *        The mail settings for which the error occurs. Never <code>null</code>.
   * @param aOriginalSentDT
   *        The date and time when the message was originally sent. Never <code>null</code>.
   * @param aEmailData
   *        The message that failed to send. May be <code>null</code> if it is a mail-independent
   *        error.
   * @param aError
   *        The exception that occurred. May be <code>null</code>.
   */
  public FailedMailData (@Nonnull final String sID,
                         @Nonnull final LocalDateTime aErrorDT,
                         @Nonnull final ISMTPSettings aSettings,
                         @Nullable final LocalDateTime aOriginalSentDT,
                         @Nullable final IMutableEmailData aEmailData,
                         @Nullable final MailTransportError aError)
  {
    m_sID = ValueEnforcer.notNull (sID, "ID");
    m_aErrorDT = ValueEnforcer.notNull (aErrorDT, "ErrorDT");
    m_aSettings = ValueEnforcer.notNull (aSettings, "Settings");
    m_aOriginalSentDateTime = aOriginalSentDT != null ? aOriginalSentDT : aEmailData != null ? aEmailData
                                                                                                         .getSentDateTime ()
                                                                                             : null;
    m_aEmailData = aEmailData;
    m_aError = aError;
  }

  @Nonnull
  public ObjectType getObjectType ()
  {
    return TYPE_FAILEDMAIL;
  }

  @Nonnull
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return The date and time when the error occurred.
   */
  @Nonnull
  public LocalDateTime getErrorDateTime ()
  {
    return m_aErrorDT;
  }

  /**
   * @return The original SMTP settings used for sending the mail.
   */
  @Nonnull
  public ISMTPSettings getSMTPSettings ()
  {
    return m_aSettings;
  }

  /**
   * @return The date and time when the message was originally sent.
   */
  @Nullable
  public LocalDateTime getOriginalSentDateTime ()
  {
    return m_aOriginalSentDateTime;
  }

  /**
   * @return The message object on which the error occurred. May be <code>null</code> if e.g. a
   *         general authentication problem occurred.
   */
  @Nullable
  public IMutableEmailData getEmailData ()
  {
    return m_aEmailData;
  }

  @Nullable
  public MailTransportError getTransportError ()
  {
    return m_aError;
  }

  public boolean hasTransportError ()
  {
    return m_aError != null;
  }

  @Nullable
  public Throwable getTransportThrowable ()
  {
    return m_aError == null ? null : m_aError.getThrowable ();
  }

  @Nonnull
  public String getSMTPServerDisplayText ()
  {
    StringBuilder ret = new StringBuilder ().append (m_aSettings.getHostName ());
    if (m_aSettings.hasPort ())
      ret.append (":").append (m_aSettings.getPort ());
    if (m_aSettings.hasUserName ())
    {
      ret.append ("[").append (m_aSettings.getUserName ());
      if (m_aSettings.hasPassword ())
        ret.append ("/****");
      ret.append (']');
    }
    return ret.toString ();
  }

  @Nonnull
  public String getSenderDisplayText ()
  {
    return m_aEmailData == null ? "" : m_aEmailData.getFrom ().getDisplayName ();
  }

  @Nonnull
  public String getRecipientDisplayText ()
  {
    final StringBuilder ret = new StringBuilder ();
    if (m_aEmailData != null)
      for (final IEmailAddress aEmailAddress : m_aEmailData.to ())
      {
        if (ret.length () > 0)
          ret.append ("; ");
        ret.append (aEmailAddress.getDisplayName ());
      }
    return ret.toString ();
  }

  @Nullable
  public String getTransportThrowableMessage ()
  {
    return m_aError == null ? null : m_aError.getThrowable ().getMessage ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final FailedMailData rhs = (FailedMailData) o;
    return m_sID.equals (rhs.m_sID) &&
           m_aErrorDT.equals (rhs.m_aErrorDT) &&
           m_aSettings.equals (rhs.m_aSettings) &&
           EqualsHelper.equals (m_aOriginalSentDateTime, rhs.m_aOriginalSentDateTime) &&
           EqualsHelper.equals (m_aEmailData, rhs.m_aEmailData) &&
           EqualsHelper.equals (getTransportThrowableMessage (), rhs.getTransportThrowableMessage ());
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sID)
                                       .append (m_aErrorDT)
                                       .append (m_aSettings)
                                       .append (m_aOriginalSentDateTime)
                                       .append (m_aEmailData)
                                       .append (getTransportThrowableMessage ())
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("id", m_sID)
                                       .append ("errorDateTime", m_aErrorDT)
                                       .append ("settings", m_aSettings)
                                       .appendIfNotNull ("originalSentDateTime", m_aOriginalSentDateTime)
                                       .appendIfNotNull ("emailData", m_aEmailData)
                                       .appendIfNotNull ("error", m_aError)
                                       .getToString ();
  }
}
