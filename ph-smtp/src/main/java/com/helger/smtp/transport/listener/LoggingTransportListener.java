/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.smtp.transport.listener;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.level.IErrorLevel;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.log.LogHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.smtp.listener.EmailDataTransportEvent;
import com.helger.smtp.listener.IEmailDataTransportListener;
import com.helger.smtp.transport.MailSendDetails;

/**
 * An implementation of {@link IEmailDataTransportListener} that logs stuff to a
 * logger.
 *
 * @author Philip Helger
 */
public class LoggingTransportListener implements IEmailDataTransportListener
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (LoggingConnectionListener.class);

  private final IErrorLevel m_aErrorLevel;

  public LoggingTransportListener ()
  {
    this (EErrorLevel.INFO);
  }

  public LoggingTransportListener (@Nonnull final IErrorLevel eErrorLevel)
  {
    m_aErrorLevel = ValueEnforcer.notNull (eErrorLevel, "ErrorLevel");
  }

  @Nonnull
  public IErrorLevel getErrorLevel ()
  {
    return m_aErrorLevel;
  }

  @Nonnull
  public static String getAddressesString (@Nullable final Collection <? extends MailSendDetails> aAddresses)
  {
    if (aAddresses == null || aAddresses.isEmpty ())
      return "[]";
    final StringBuilder aSB = new StringBuilder ().append ('[');
    for (final MailSendDetails aFailure : aAddresses)
    {
      if (aSB.length () > 1)
        aSB.append (", ");
      aSB.append (aFailure.getAddress ());
      if (!aFailure.isAddressValid ())
        aSB.append (" (").append (aFailure.getErrorMessage ()).append (')');
    }
    return aSB.append (']').toString ();
  }

  @Nonnull
  public static String getMessageString (@Nullable final Message aMsg)
  {
    if (aMsg == null)
      return "null";
    if (aMsg instanceof MimeMessage)
      try
      {
        return "MIME-Msg " + ((MimeMessage) aMsg).getMessageID ();
      }
      catch (final MessagingException ex)
      {
        return "MIME-Msg " + ex.getClass ().getName () + " - " + ex.getMessage ();
      }
    return ClassHelper.getClassLocalName (aMsg.getClass ());
  }

  @Nonnull
  public static String getLogString (@Nonnull final EmailDataTransportEvent aEvent)
  {
    return "validSent=" +
           getAddressesString (aEvent.getValidSentAddresses ()) +
           "; validUnsent=" +
           getAddressesString (aEvent.getValidUnsentAddresses ()) +
           "; invalid=" +
           getAddressesString (aEvent.getInvalidAddresses ()) +
           "; msg=" +
           getMessageString (aEvent.getMimeMessage ());
  }

  public void messageDelivered (@Nonnull final EmailDataTransportEvent aEvent)
  {
    LogHelper.log (s_aLogger, m_aErrorLevel, "Message delivered: " + getLogString (aEvent));
  }

  public void messageNotDelivered (@Nonnull final EmailDataTransportEvent aEvent)
  {
    LogHelper.log (s_aLogger, m_aErrorLevel, "Message not delivered: " + getLogString (aEvent));
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).toString ();
  }
}
