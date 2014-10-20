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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.GlobalDebug;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.hash.HashCodeGenerator;
import com.helger.commons.stats.IStatisticsHandlerCounter;
import com.helger.commons.stats.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.datetime.PDTFactory;
import com.helger.web.WebExceptionHelper;
import com.helger.web.smtp.EmailGlobalSettings;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.IEmailDataTransportListener;
import com.helger.web.smtp.ISMTPSettings;

/**
 * The wrapper around the main javax.mail transport
 *
 * @author Philip Helger
 */
final class MailTransport
{
  public static final String SMTP_PROTOCOL = "smtp";
  public static final String SMTPS_PROTOCOL = "smtps";

  private static final IStatisticsHandlerCounter s_aStatsCountSuccess = StatisticsManager.getCounterHandler (MailTransport.class);
  private static final IStatisticsHandlerCounter s_aStatsCountFailed = StatisticsManager.getCounterHandler (MailTransport.class.getName () +
                                                                                                            "$failed");
  private static final Logger s_aLogger = LoggerFactory.getLogger (MailTransport.class);
  private static final String HEADER_MESSAGE_ID = "Message-ID";

  private final ISMTPSettings m_aSMTPSettings;
  private final boolean m_bSMTPS;
  private final Properties m_aMailProperties = new Properties ();
  private final Session m_aSession;

  public MailTransport (@Nonnull final ISMTPSettings aSettings)
  {
    ValueEnforcer.notNull (aSettings, "Settings");

    m_aSMTPSettings = aSettings;
    m_bSMTPS = aSettings.isSSLEnabled () || aSettings.isSTARTTLSEnabled ();

    // Enable SSL?
    if (aSettings.isSSLEnabled ())
      m_aMailProperties.setProperty (ESMTPTransportProperty.SSL_ENABLE.getPropertyName (m_bSMTPS),
                                     Boolean.TRUE.toString ());

    // Check if authentication is required
    if (StringHelper.hasText (aSettings.getUserName ()))
      m_aMailProperties.setProperty (ESMTPTransportProperty.AUTH.getPropertyName (m_bSMTPS), Boolean.TRUE.toString ());

    // Enable STARTTLS?
    if (aSettings.isSTARTTLSEnabled ())
      m_aMailProperties.setProperty (ESMTPTransportProperty.STARTTLS_ENABLE.getPropertyName (m_bSMTPS),
                                     Boolean.TRUE.toString ());

    if (m_bSMTPS)
    {
      m_aMailProperties.setProperty (ESMTPTransportProperty.SSL_SOCKETFACTORY_CLASS.getPropertyName (m_bSMTPS),
                                     com.sun.mail.util.MailSSLSocketFactory.class.getName ());
      m_aMailProperties.setProperty (ESMTPTransportProperty.SSL_SOCKETFACTORY_PORT.getPropertyName (m_bSMTPS),
                                     Integer.toString (aSettings.getPort ()));
    }

    // Set connection timeout
    final long nConnectionTimeoutMilliSecs = aSettings.getConnectionTimeoutMilliSecs ();
    if (nConnectionTimeoutMilliSecs > 0)
      m_aMailProperties.setProperty (ESMTPTransportProperty.CONNECTIONTIMEOUT.getPropertyName (m_bSMTPS),
                                     Long.toString (nConnectionTimeoutMilliSecs));

    // Set socket timeout
    final long nTimeoutMilliSecs = aSettings.getTimeoutMilliSecs ();
    if (nTimeoutMilliSecs > 0)
      m_aMailProperties.setProperty (ESMTPTransportProperty.TIMEOUT.getPropertyName (m_bSMTPS),
                                     Long.toString (nTimeoutMilliSecs));

    if (false)
      m_aMailProperties.setProperty (ESMTPTransportProperty.REPORTSUCCESS.getPropertyName (m_bSMTPS),
                                     Boolean.TRUE.toString ());

    // Debug flag
    m_aMailProperties.setProperty ("mail.debug.auth", Boolean.toString (GlobalDebug.isDebugMode ()));

    if (s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("Mail properties: " + m_aMailProperties);

    // Create session based on properties
    m_aSession = Session.getInstance (m_aMailProperties);

    // Set after eventual properties are set, because in setJavaMailProperties,
    // the session is reset!
    m_aSession.setDebug (GlobalDebug.isDebugMode ());
  }

  @Nonnull
  public ISMTPSettings getSMTPSettings ()
  {
    return m_aSMTPSettings;
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <Object, Object> getMailProperties ()
  {
    return ContainerHelper.newMap (m_aMailProperties);
  }

  /**
   * Actually send the given array of MimeMessages via JavaMail.
   *
   * @param aMessages
   *        Email data objects to send. May be <code>null</code>.
   * @return A non-<code>null</code> map of the failed messages
   * @throws MailSendException
   *         If anything goes wrong
   */
  @Nonnull
  public Map <IEmailData, MessagingException> send (@Nullable final Collection <IEmailData> aMessages) throws MailSendException
  {
    final Map <IEmailData, MessagingException> aFailedMessages = new LinkedHashMap <IEmailData, MessagingException> ();
    if (aMessages != null)
    {
      try
      {
        final Transport aTransport = m_aSession.getTransport (m_bSMTPS ? SMTPS_PROTOCOL : SMTP_PROTOCOL);

        // Add global listeners (if present)
        final ConnectionListener aConnectionListener = EmailGlobalSettings.getConnectionListener ();
        if (aConnectionListener != null)
          aTransport.addConnectionListener (aConnectionListener);

        final TransportListener aGlobalTransportListener = EmailGlobalSettings.getTransportListener ();
        final IEmailDataTransportListener aEmailDataTransportListener = EmailGlobalSettings.getEmailDataTransportListener ();
        if (aGlobalTransportListener != null && aEmailDataTransportListener == null)
        {
          // Set only the global transport listener
          aTransport.addTransportListener (aGlobalTransportListener);
        }

        // Connect
        aTransport.connect (m_aSMTPSettings.getHostName (),
                            m_aSMTPSettings.getPort (),
                            m_aSMTPSettings.getUserName (),
                            m_aSMTPSettings.getPassword ());

        try
        {
          // For all messages
          for (final IEmailData aMessage : aMessages)
          {
            try
            {
              // Set email data specific listeners
              TransportListener aPerMailListener = null;
              if (aEmailDataTransportListener != null)
              {
                aPerMailListener = new TransportListener ()
                {
                  public void messageDelivered (@Nonnull final TransportEvent aEvent)
                  {
                    aEmailDataTransportListener.messageDelivered (m_aSMTPSettings, aMessage, aEvent);
                    if (aGlobalTransportListener != null)
                      aGlobalTransportListener.messageDelivered (aEvent);
                  }

                  public void messageNotDelivered (@Nonnull final TransportEvent aEvent)
                  {
                    aEmailDataTransportListener.messageNotDelivered (m_aSMTPSettings, aMessage, aEvent);
                    if (aGlobalTransportListener != null)
                      aGlobalTransportListener.messageNotDelivered (aEvent);
                  }

                  public void messagePartiallyDelivered (@Nonnull final TransportEvent aEvent)
                  {
                    aEmailDataTransportListener.messagePartiallyDelivered (m_aSMTPSettings, aMessage, aEvent);
                    if (aGlobalTransportListener != null)
                      aGlobalTransportListener.messagePartiallyDelivered (aEvent);
                  }
                };
                aTransport.addTransportListener (aPerMailListener);
              }

              // convert from IEmailData to MimeMessage
              final MimeMessage aMimeMessage = new MimeMessage (m_aSession);
              MailConverter.fillMimeMesage (aMimeMessage, aMessage, m_aSMTPSettings.getCharsetObj ());

              // Ensure a sent date is present
              if (aMimeMessage.getSentDate () == null)
                aMimeMessage.setSentDate (PDTFactory.getCurrentDateTime ().toDate ());

              // Get an explicitly specified message ID
              final String sMessageID = aMimeMessage.getMessageID ();

              // This creates a new message ID (besides other things)
              aMimeMessage.saveChanges ();

              if (sMessageID != null)
              {
                // Preserve explicitly specified message id...
                aMimeMessage.setHeader (HEADER_MESSAGE_ID, sMessageID);
              }

              s_aLogger.info ("Delivering mail to " +
                              Arrays.toString (aMimeMessage.getRecipients (RecipientType.TO)) +
                              " with subject '" +
                              aMimeMessage.getSubject () +
                              "' and message ID '" +
                              aMimeMessage.getMessageID () +
                              "'");

              // Start transmitting
              aTransport.sendMessage (aMimeMessage, aMimeMessage.getAllRecipients ());

              // Remove per-mail listener again
              if (aPerMailListener != null)
                aTransport.removeTransportListener (aPerMailListener);

              s_aStatsCountSuccess.increment ();
            }
            catch (final MessagingException ex)
            {
              s_aStatsCountFailed.increment ();
              // Sending exactly THIS messages failed
              aFailedMessages.put (aMessage, ex);
            }
          }
        }
        finally
        {
          try
          {
            aTransport.close ();
          }
          catch (final MessagingException ex)
          {
            throw new MailSendException ("Failed to close mail transport", ex);
          }
        }
      }
      catch (final AuthenticationFailedException ex)
      {
        // problem with the credentials
        throw new MailSendException ("Mail server authentication failed", ex);
      }
      catch (final MessagingException ex)
      {
        if (WebExceptionHelper.isServerNotReachableConnection (ex.getCause ()))
          throw new MailSendException ("Failed to connect to mail server: " + ex.getCause ().getMessage ());
        throw new MailSendException ("Mail server connection failed", ex);
      }
    }
    return aFailedMessages;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!(o instanceof MailTransport))
      return false;
    final MailTransport rhs = (MailTransport) o;
    return m_aSMTPSettings.equals (rhs.m_aSMTPSettings);
  }

  @Override
  public int hashCode ()
  {
    // Compare only settings - session and properties are derived
    return new HashCodeGenerator (this).append (m_aSMTPSettings).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("settings", m_aSMTPSettings)
                                       .append ("properties", m_aMailProperties)
                                       .append ("session", m_aSession)
                                       .toString ();
  }
}
