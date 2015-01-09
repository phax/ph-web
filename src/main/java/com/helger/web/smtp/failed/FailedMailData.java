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
package com.helger.web.smtp.failed;

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.equals.EqualsUtils;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.idfactory.GlobalIDFactory;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.type.ITypedObject;
import com.helger.commons.type.ObjectType;
import com.helger.datetime.PDTFactory;
import com.helger.datetime.format.PDTToString;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.ISMTPSettings;

@Immutable
public final class FailedMailData implements ITypedObject <String>, Serializable
{
  public static final ObjectType TYPE_FAILEDMAIL = new ObjectType ("failedmail");

  private final String m_sID;
  private final LocalDateTime m_aErrorDT;
  private final ISMTPSettings m_aSettings;
  private final DateTime m_aOriginalSentDateTime;
  private final IEmailData m_aEmailData;
  private final Throwable m_aError;

  /**
   * Constructor for message unspecific error.
   * 
   * @param aSettings
   *        The mail settings for which the error occurs. Never
   *        <code>null</code>.
   * @param aError
   *        The exception that occurred. Never <code>null</code>.
   */
  public FailedMailData (@Nonnull final ISMTPSettings aSettings, @Nonnull final Throwable aError)
  {
    this (aSettings, null, aError);
  }

  /**
   * Constructor for message specific error.
   * 
   * @param aSettings
   *        The mail settings for which the error occurs. Never
   *        <code>null</code>.
   * @param aEmailData
   *        The message that failed to send. May not be <code>null</code> in
   *        practice.
   */
  public FailedMailData (@Nonnull final ISMTPSettings aSettings, @Nullable final IEmailData aEmailData)
  {
    this (aSettings, aEmailData, (Throwable) null);
  }

  /**
   * Constructor for message specific error.
   * 
   * @param aSettings
   *        The mail settings for which the error occurs. Never
   *        <code>null</code>.
   * @param aEmailData
   *        The message that failed to send. May be <code>null</code> if it is a
   *        mail-independent error.
   * @param aError
   *        The exception that occurred. May be <code>null</code>.
   */
  public FailedMailData (@Nonnull final ISMTPSettings aSettings,
                         @Nullable final IEmailData aEmailData,
                         @Nullable final Throwable aError)
  {
    this (GlobalIDFactory.getNewPersistentStringID (),
          PDTFactory.getCurrentLocalDateTime (),
          aSettings,
          aEmailData == null ? null : aEmailData.getSentDate (),
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
   *        The mail settings for which the error occurs. Never
   *        <code>null</code>.
   * @param aOriginalSentDT
   *        The date and time when the message was originally sent. Never
   *        <code>null</code>.
   * @param aEmailData
   *        The message that failed to send. May be <code>null</code> if it is a
   *        mail-independent error.
   * @param aError
   *        The exception that occurred. May be <code>null</code>.
   */
  public FailedMailData (@Nonnull final String sID,
                         @Nonnull final LocalDateTime aErrorDT,
                         @Nonnull final ISMTPSettings aSettings,
                         @Nullable final DateTime aOriginalSentDT,
                         @Nullable final IEmailData aEmailData,
                         @Nullable final Throwable aError)
  {
    m_sID = ValueEnforcer.notNull (sID, "ID");
    m_aErrorDT = ValueEnforcer.notNull (aErrorDT, "ErrorDT");
    m_aSettings = ValueEnforcer.notNull (aSettings, "Settings");
    m_aOriginalSentDateTime = aOriginalSentDT != null ? aOriginalSentDT
                                                     : (aEmailData != null ? aEmailData.getSentDate () : null);
    m_aEmailData = aEmailData;
    m_aError = aError;
  }

  @Nonnull
  public ObjectType getTypeID ()
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
  @Nonnull
  public DateTime getOriginalSentDateTime ()
  {
    return m_aOriginalSentDateTime;
  }

  /**
   * @return The message object on which the error occurred. May be
   *         <code>null</code> if e.g. a general authentication problem
   *         occurred.
   */
  @Nullable
  public IEmailData getEmailData ()
  {
    return m_aEmailData;
  }

  @Nullable
  public Throwable getError ()
  {
    return m_aError;
  }

  @Nonnull
  public String getErrorTimeDisplayText (@Nonnull final Locale aDisplayLocale)
  {
    return PDTToString.getAsString (m_aErrorDT, aDisplayLocale);
  }

  @Nonnull
  public String getSMTPServerDisplayText ()
  {
    String ret = m_aSettings.getHostName () + ":" + m_aSettings.getPort ();
    if (StringHelper.hasText (m_aSettings.getUserName ()))
    {
      ret += "[" + m_aSettings.getUserName ();
      if (StringHelper.hasText (m_aSettings.getPassword ()))
        ret += "/****";
      ret += ']';
    }
    return ret;
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
      for (final IEmailAddress aEmailAddress : m_aEmailData.getTo ())
      {
        if (ret.length () > 0)
          ret.append ("; ");
        ret.append (aEmailAddress.getDisplayName ());
      }
    return ret.toString ();
  }

  @Nullable
  public String getMessageDisplayText ()
  {
    return m_aError == null ? null : m_aError.getMessage ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!(o instanceof FailedMailData))
      return false;
    final FailedMailData rhs = (FailedMailData) o;
    return m_sID.equals (rhs.m_sID) &&
           m_aErrorDT.equals (rhs.m_aErrorDT) &&
           m_aSettings.equals (rhs.m_aSettings) &&
           EqualsUtils.equals (m_aOriginalSentDateTime, rhs.m_aOriginalSentDateTime) &&
           EqualsUtils.equals (m_aEmailData, rhs.m_aEmailData) &&
           EqualsUtils.equals (getMessageDisplayText (), rhs.getMessageDisplayText ());
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sID)
                                       .append (m_aErrorDT)
                                       .append (m_aSettings)
                                       .append (m_aOriginalSentDateTime)
                                       .append (m_aEmailData)
                                       .append (getMessageDisplayText ())
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
                                       .toString ();
  }
}
