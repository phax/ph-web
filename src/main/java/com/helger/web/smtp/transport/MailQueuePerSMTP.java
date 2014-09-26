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
package com.helger.web.smtp.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.callback.IThrowingRunnableWithParameter;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.concurrent.collector.ConcurrentCollectorMultiple;
import com.helger.commons.state.ESuccess;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.ISMTPSettings;
import com.helger.web.smtp.failed.FailedMailData;
import com.helger.web.smtp.failed.FailedMailQueue;

/**
 * This class collects instances of {@link IEmailData} and tries to transmit
 * them using the specified SMTP settings.
 *
 * @author Philip Helger
 */
final class MailQueuePerSMTP extends ConcurrentCollectorMultiple <IEmailData> implements IThrowingRunnableWithParameter <List <IEmailData>>
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (MailQueuePerSMTP.class);

  private final MailTransport m_aTransport;
  private FailedMailQueue m_aFailedMailQueue;

  /**
   * Constructor
   *
   * @param nMaxQueueSize
   *        Maximum objects to queue
   * @param nMaxPerformCount
   *        Maximum number of emails to send at once
   * @param aSMTPSettings
   *        SMTP settings to use. May not be <code>null</code>.
   * @param aFailedMailQueue
   *        The queue for unsent mails. May not be <code>null</code>.
   */
  public MailQueuePerSMTP (@Nonnegative final int nMaxQueueSize,
                           @Nonnegative final int nMaxPerformCount,
                           @Nonnull final ISMTPSettings aSMTPSettings,
                           @Nonnull final FailedMailQueue aFailedMailQueue)
  {
    super (nMaxQueueSize, nMaxPerformCount, null);
    ValueEnforcer.notNull (aSMTPSettings, "SMTPSettings");

    // Mail mail transport object
    m_aTransport = new MailTransport (aSMTPSettings);
    setFailedMailQueue (aFailedMailQueue);

    // Set the callback of the concurrent collector
    setPerformer (this);
  }

  /**
   * @return The SMTP settings used for this queue. Never <code>null</code>.
   */
  @Nonnull
  public ISMTPSettings getSMTPSettings ()
  {
    return m_aTransport.getSMTPSettings ();
  }

  /**
   * @return The Failed mail queue to be used for this queue.
   */
  @Nonnull
  public FailedMailQueue getFailedMailQueue ()
  {
    return m_aFailedMailQueue;
  }

  public void setFailedMailQueue (@Nonnull final FailedMailQueue aFailedMailQueue)
  {
    m_aFailedMailQueue = ValueEnforcer.notNull (aFailedMailQueue, "FailedMailQueue");
  }

  /**
   * This is the callback to be invoked everytime something is in the queue.
   *
   * @param aMessages
   *        The non-null and non-empty list of messages to be send
   */
  public void run (@Nullable final List <IEmailData> aMessages)
  {
    // Expect the worst
    if (ContainerHelper.isNotEmpty (aMessages))
    {
      final ISMTPSettings aSettings = m_aTransport.getSMTPSettings ();
      try
      {
        final int nMessages = aMessages.size ();
        s_aLogger.info ("Sending " + nMessages + " mail message" + (nMessages == 1 ? "" : "s") + "!");

        // send messages
        final Map <IEmailData, MessagingException> aFailedMessages = m_aTransport.send (aMessages);

        // handle failed messages
        for (final Map.Entry <IEmailData, MessagingException> aEntry : aFailedMessages.entrySet ())
          m_aFailedMailQueue.add (new FailedMailData (aSettings, aEntry.getKey (), aEntry.getValue ()));
      }
      catch (final MailSendException ex)
      {
        s_aLogger.error ("Error sending mail: " + ex.getMessage (), ex.getCause ());

        // mark all mails as failed
        for (final IEmailData aMessage : aMessages)
          m_aFailedMailQueue.add (new FailedMailData (aSettings, aMessage, ex));
      }
      catch (final Throwable ex)
      {
        // No message specific error, but a settings specific error
        s_aLogger.error ("Error sending mail: " + ex.getMessage (), ex.getCause ());
        m_aFailedMailQueue.add (new FailedMailData (aSettings, ex));
      }
    }
  }

  /**
   * Stop this queue
   *
   * @param bStopImmediately
   *        <code>true</code> if all mails currently in the queue should be
   *        removed and put in the failed mail queue. Only the emails currently
   *        in sending are continued to be sent out.
   * @return {@link ESuccess}
   */
  @Nonnull
  public ESuccess stopQueuingNewObjects (final boolean bStopImmediately)
  {
    if (bStopImmediately)
    {
      // Remove all mails from the queue and put it in the failed mail queue
      final List <Object> aLeftOvers = new ArrayList <Object> ();
      m_aQueue.drainTo (aLeftOvers);
      if (!aLeftOvers.isEmpty ())
      {
        final ISMTPSettings aSMTPSettings = getSMTPSettings ();
        for (final Object aLeftOver : aLeftOvers)
          m_aFailedMailQueue.add (new FailedMailData (aSMTPSettings, (IEmailData) aLeftOver));
        s_aLogger.info ("Put " + aLeftOvers + " unsent mails into the failed mail queue because of immediate stop.");
      }
    }

    // Regular stop
    return super.stopQueuingNewObjects ();
  }
}
