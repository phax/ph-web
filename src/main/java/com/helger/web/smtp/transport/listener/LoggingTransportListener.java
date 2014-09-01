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
package com.helger.web.smtp.transport.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.EErrorLevel;
import com.helger.commons.lang.CGStringHelper;
import com.helger.commons.log.LogUtils;
import com.helger.commons.string.ToStringGenerator;

/**
 * An implementation of {@link TransportListener} that logs stuff to a logger.
 * 
 * @author Philip Helger
 */
public class LoggingTransportListener implements TransportListener
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (LoggingConnectionListener.class);

  private final EErrorLevel m_eErrorLevel;

  public LoggingTransportListener ()
  {
    this (EErrorLevel.INFO);
  }

  public LoggingTransportListener (@Nonnull final EErrorLevel eErrorLevel)
  {
    m_eErrorLevel = ValueEnforcer.notNull (eErrorLevel, "ErrorLevel");
  }

  @Nonnull
  public EErrorLevel getErrorLevel ()
  {
    return m_eErrorLevel;
  }

  @Nonnull
  public static String getAddressesString (@Nullable final Address [] aAddresses)
  {
    if (aAddresses == null || aAddresses.length == 0)
      return "[]";
    final StringBuilder aSB = new StringBuilder ().append ('[');
    for (final Address aAddress : aAddresses)
    {
      if (aSB.length () > 1)
        aSB.append (", ");
      aSB.append (aAddress == null ? "null" : aAddress.toString ());
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
    return CGStringHelper.getClassLocalName (aMsg.getClass ());
  }

  @Nonnull
  public static String getLogString (@Nonnull final TransportEvent aEvent)
  {
    return "validSent=" +
           getAddressesString (aEvent.getValidSentAddresses ()) +
           "; validUnsent=" +
           getAddressesString (aEvent.getValidUnsentAddresses ()) +
           "; invalid=" +
           getAddressesString (aEvent.getInvalidAddresses ()) +
           "; msg=" +
           getMessageString (aEvent.getMessage ());
  }

  public void messageDelivered (@Nonnull final TransportEvent aEvent)
  {
    LogUtils.log (s_aLogger, m_eErrorLevel, "Message delivered: " + getLogString (aEvent));
  }

  public void messageNotDelivered (@Nonnull final TransportEvent aEvent)
  {
    LogUtils.log (s_aLogger, m_eErrorLevel, "Message not delivered: " + getLogString (aEvent));
  }

  public void messagePartiallyDelivered (@Nonnull final TransportEvent aEvent)
  {
    LogUtils.log (s_aLogger, m_eErrorLevel, "Message partially delivered: " + getLogString (aEvent));
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).toString ();
  }
}
