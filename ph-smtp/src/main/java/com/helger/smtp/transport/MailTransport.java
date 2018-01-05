/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionListener;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.ToStringGenerator;
import com.helger.network.WebExceptionHelper;
import com.helger.smtp.EmailGlobalSettings;
import com.helger.smtp.data.IMutableEmailData;
import com.helger.smtp.listener.EmailDataTransportEvent;
import com.helger.smtp.listener.IEmailDataTransportListener;
import com.helger.smtp.settings.ISMTPSettings;
import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPAddressSucceededException;

/**
 * The wrapper around the main javax.mail transport
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class MailTransport
{
  public static final String SMTP_PROTOCOL = "smtp";
  public static final String SMTPS_PROTOCOL = "smtps";
  public static final String X_MAILER = "ph-smtp";

  private static final IMutableStatisticsHandlerCounter s_aStatsCountSuccess = StatisticsManager.getCounterHandler (MailTransport.class.getName () +
                                                                                                                    "$success");
  private static final IMutableStatisticsHandlerCounter s_aStatsCountFailed = StatisticsManager.getCounterHandler (MailTransport.class.getName () +
                                                                                                                   "$failed");
  private static final Logger s_aLogger = LoggerFactory.getLogger (MailTransport.class);
  private static final String HEADER_MESSAGE_ID = "Message-ID";

  private final ISMTPSettings m_aSMTPSettings;
  private final boolean m_bSMTPS;
  private final ICommonsMap <String, String> m_aMailProperties;
  private final Session m_aSession;

  /**
   * Check if the "smtps" properties should be used or not.
   *
   * @param aSettings
   *        The base settings. May not be <code>null</code>.
   * @return <code>true</code> to use "smtps".
   * @since 1.0.1
   */
  public static boolean isUseSMTPS (@Nonnull final ISMTPSettings aSettings)
  {
    return aSettings.isSSLEnabled () || aSettings.isSTARTTLSEnabled ();
  }

  /**
   * @param aSettings
   *        The SMTP settings used as the basis.
   * @return The properties to be used in the javax.mail Session. Never
   *         <code>null</code>.
   * @since 1.0.1
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsMap <String, String> createSessionProperties (@Nonnull final ISMTPSettings aSettings)
  {
    final ICommonsMap <String, String> ret = new CommonsHashMap <> ();
    final boolean bSMTPS = isUseSMTPS (aSettings);

    // Enable SSL?
    if (aSettings.isSSLEnabled ())
      ret.put (ESMTPTransportProperty.SSL_ENABLE.getPropertyName (bSMTPS), Boolean.TRUE.toString ());

    // Check if authentication is required
    if (aSettings.hasUserName ())
      ret.put (ESMTPTransportProperty.AUTH.getPropertyName (bSMTPS), Boolean.TRUE.toString ());

    // Enable STARTTLS?
    if (aSettings.isSTARTTLSEnabled ())
      ret.put (ESMTPTransportProperty.STARTTLS_ENABLE.getPropertyName (bSMTPS), Boolean.TRUE.toString ());

    if (bSMTPS && false)
    {
      ret.put (ESMTPTransportProperty.SSL_SOCKETFACTORY_CLASS.getPropertyName (bSMTPS),
               com.sun.mail.util.MailSSLSocketFactory.class.getName ());
      ret.put (ESMTPTransportProperty.SSL_SOCKETFACTORY_PORT.getPropertyName (bSMTPS),
               Integer.toString (aSettings.getPort ()));
    }

    // Set connection timeout
    final long nConnectionTimeoutMilliSecs = aSettings.getConnectionTimeoutMilliSecs ();
    if (nConnectionTimeoutMilliSecs > 0)
      ret.put (ESMTPTransportProperty.CONNECTIONTIMEOUT.getPropertyName (bSMTPS),
               Long.toString (nConnectionTimeoutMilliSecs));

    // Set socket timeout
    final long nTimeoutMilliSecs = aSettings.getTimeoutMilliSecs ();
    if (nTimeoutMilliSecs > 0)
      ret.put (ESMTPTransportProperty.TIMEOUT.getPropertyName (bSMTPS), Long.toString (nTimeoutMilliSecs));

    // Throw exception in case of success
    ret.put (ESMTPTransportProperty.REPORTSUCCESS.getPropertyName (bSMTPS), Boolean.toString (true));

    // Disallow partial delivery - either send to all or to none (is the
    // default, but to be sure)
    ret.put (ESMTPTransportProperty.SENDPARTIAL.getPropertyName (bSMTPS), Boolean.toString (false));

    // Debug flags
    ret.put ("mail.debug", Boolean.toString (aSettings.isDebugSMTP ()));
    ret.put ("mail.debug.auth", Boolean.toString (aSettings.isDebugSMTP ()));

    return ret;
  }

  public MailTransport (@Nonnull final ISMTPSettings aSettings)
  {
    ValueEnforcer.notNull (aSettings, "Settings");

    m_aSMTPSettings = aSettings;
    m_bSMTPS = isUseSMTPS (aSettings);
    m_aMailProperties = createSessionProperties (aSettings);

    if (s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("Mail properties: " + m_aMailProperties);

    // Create session based on properties
    final Properties aProps = new Properties ();
    aProps.putAll (m_aMailProperties);
    m_aSession = Session.getInstance (aProps);
  }

  @Nonnull
  public ISMTPSettings getSMTPSettings ()
  {
    return m_aSMTPSettings;
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <String, String> getMailProperties ()
  {
    return m_aMailProperties.getClone ();
  }

  /**
   * Actually send the given array of MimeMessages via JavaMail.
   *
   * @param aAllMessages
   *        Email data objects to send. May be <code>null</code>.
   * @return A non-<code>null</code> map of the failed messages
   */
  @Nonnull
  public ICommonsOrderedMap <IMutableEmailData, MailTransportError> send (@Nullable final Collection <IMutableEmailData> aAllMessages)
  {
    final ICommonsOrderedMap <IMutableEmailData, MailTransportError> aFailedMessages = new CommonsLinkedHashMap <> ();
    if (aAllMessages != null)
    {
      final ICommonsList <IMutableEmailData> aRemainingMessages = new CommonsArrayList <> (aAllMessages);
      MailSendException aExceptionToBeRemembered = null;

      try (final Transport aTransport = m_aSession.getTransport (m_bSMTPS ? SMTPS_PROTOCOL : SMTP_PROTOCOL))
      {
        // Add global listeners (if present)
        for (final ConnectionListener aConnectionListener : EmailGlobalSettings.getAllConnectionListeners ())
          aTransport.addConnectionListener (aConnectionListener);

        // Check if a detailed listener is present
        final ICommonsList <IEmailDataTransportListener> aEmailDataTransportListeners = EmailGlobalSettings.getAllEmailDataTransportListeners ();

        // Connect
        aTransport.connect (m_aSMTPSettings.getHostName (),
                            m_aSMTPSettings.getPort (),
                            m_aSMTPSettings.getUserName (),
                            m_aSMTPSettings.getPassword ());

        // For all messages
        for (final IMutableEmailData aEmailData : aAllMessages)
        {
          final MimeMessage aMimeMessage = new MimeMessage (m_aSession);
          try
          {
            // convert from IEmailData to MimeMessage
            MailConverter.fillMimeMessage (aMimeMessage, aEmailData, m_aSMTPSettings.getCharsetObj ());

            // Ensure a sent date is present
            if (aMimeMessage.getSentDate () == null)
              aMimeMessage.setSentDate (new Date ());

            // Get an explicitly specified message ID
            final String sMessageID = aMimeMessage.getMessageID ();

            // This creates a new message ID (besides other things)
            aMimeMessage.saveChanges ();

            if (sMessageID != null)
            {
              // Preserve explicitly specified message id...
              aMimeMessage.setHeader (HEADER_MESSAGE_ID, sMessageID);
            }
            aMimeMessage.setHeader ("X-Mailer", X_MAILER);

            s_aLogger.info ("Delivering mail from " +
                            Arrays.toString (aMimeMessage.getFrom ()) +
                            " to " +
                            Arrays.toString (aMimeMessage.getAllRecipients ()) +
                            " with subject '" +
                            aMimeMessage.getSubject () +
                            "' and message ID '" +
                            aMimeMessage.getMessageID () +
                            "'");

            // Main transmit - always throws an exception
            aTransport.sendMessage (aMimeMessage, aMimeMessage.getAllRecipients ());
            throw new IllegalStateException ("Never expected to come beyong sendMessage!");
          }
          catch (final SendFailedException ex)
          {
            if (EmailGlobalSettings.isDebugSMTP ())
              s_aLogger.error ("Error send mail - SendFailedException", ex);

            /*
             * Extract all addresses: the valid addresses to which the message
             * was sent, the valid address to which the message was not sent and
             * the invalid addresses
             */
            final ICommonsSet <String> aValidSent = CollectionHelper.newSetMapped (ex.getValidSentAddresses (),
                                                                                   Address::toString);
            final ICommonsSet <String> aValidUnsent = CollectionHelper.newSetMapped (ex.getValidUnsentAddresses (),
                                                                                     Address::toString);
            final ICommonsSet <String> aInvalid = CollectionHelper.newSetMapped (ex.getInvalidAddresses (),
                                                                                 Address::toString);

            final ICommonsList <MailSendDetails> aDetails = new CommonsArrayList <> ();
            Exception ex2;
            MessagingException bex = ex;
            while ((ex2 = bex.getNextException ()) != null && ex2 instanceof MessagingException)
            {
              if (ex2 instanceof SMTPAddressFailedException)
              {
                final SMTPAddressFailedException ssfe = (SMTPAddressFailedException) ex2;
                aDetails.add (new MailSendDetails (false,
                                                   ssfe.getAddress ().toString (),
                                                   ssfe.getCommand (),
                                                   ssfe.getMessage ().trim (),
                                                   ESMTPErrorCode.getFromIDOrDefault (ssfe.getReturnCode (),
                                                                                      ESMTPErrorCode.FALLBACK)));
              }
              else
                if (ex2 instanceof SMTPAddressSucceededException)
                {
                  final SMTPAddressSucceededException ssfe = (SMTPAddressSucceededException) ex2;
                  aDetails.add (new MailSendDetails (true,
                                                     ssfe.getAddress ().toString (),
                                                     ssfe.getCommand (),
                                                     ssfe.getMessage ().trim (),
                                                     ESMTPErrorCode.getFromIDOrDefault (ssfe.getReturnCode (),
                                                                                        ESMTPErrorCode.FALLBACK)));
                }

              bex = (MessagingException) ex2;
            }

            // Map addresses to details
            final ICommonsOrderedSet <MailSendDetails> aValidSentExt = new CommonsLinkedHashSet <> ();
            final ICommonsOrderedSet <MailSendDetails> aValidUnsentExt = new CommonsLinkedHashSet <> ();
            final ICommonsOrderedSet <MailSendDetails> aInvalidExt = new CommonsLinkedHashSet <> ();
            for (final MailSendDetails aFailure : aDetails)
            {
              final String sAddress = aFailure.getAddress ();
              if (aValidSent.contains (sAddress))
                aValidSentExt.add (aFailure);
              else
                if (aValidUnsent.contains (sAddress))
                  aValidUnsentExt.add (aFailure);
                else
                  aInvalidExt.add (aFailure);
            }

            final EmailDataTransportEvent aEvent = new EmailDataTransportEvent (m_aSMTPSettings,
                                                                                aEmailData,
                                                                                aMimeMessage,
                                                                                aValidSentExt,
                                                                                aValidUnsentExt,
                                                                                aInvalidExt);
            if (aValidUnsent.isEmpty () && aInvalid.isEmpty () && aValidSent.isNotEmpty ())
            {
              // Message delivered
              for (final IEmailDataTransportListener aEmailDataTransportListener : aEmailDataTransportListeners)
                aEmailDataTransportListener.messageDelivered (aEvent);

              // Remove message from list of remaining
              s_aStatsCountSuccess.increment ();
            }
            else
            {
              // Message not delivered
              for (final IEmailDataTransportListener aEmailDataTransportListener : aEmailDataTransportListeners)
                aEmailDataTransportListener.messageNotDelivered (aEvent);

              // Sending exactly this message failed
              aFailedMessages.put (aEmailData, new MailTransportError (ex, aDetails));
              s_aStatsCountFailed.increment ();
            }
            // Remove message from list of remaining as we put it in the
            // failed message list manually in case of error
            aRemainingMessages.remove (aEmailData);
          }
          catch (final MessagingException ex)
          {
            if (EmailGlobalSettings.isDebugSMTP ())
              s_aLogger.error ("Error send mail - MessagingException", ex);

            final ICommonsOrderedSet <MailSendDetails> aInvalid = new CommonsLinkedHashSet <> ();
            final Consumer <IEmailAddress> aConsumer = a -> aInvalid.add (new MailSendDetails (false,
                                                                                               a.getAddress (),
                                                                                               "<generic error>",
                                                                                               ex.getMessage (),
                                                                                               ESMTPErrorCode.FALLBACK));
            aEmailData.forEachTo (aConsumer);
            aEmailData.forEachCc (aConsumer);
            aEmailData.forEachBcc (aConsumer);

            final EmailDataTransportEvent aEvent = new EmailDataTransportEvent (m_aSMTPSettings,
                                                                                aEmailData,
                                                                                aMimeMessage,
                                                                                new CommonsArrayList <> (),
                                                                                new CommonsArrayList <> (),
                                                                                aInvalid);
            // Message not delivered
            for (final IEmailDataTransportListener aEmailDataTransportListener : aEmailDataTransportListeners)
              aEmailDataTransportListener.messageNotDelivered (aEvent);

            // Sending exactly this message failed
            aFailedMessages.put (aEmailData, new MailTransportError (ex));
            // Remove message from list of remaining as we put it in the
            // failed message list manually
            aRemainingMessages.remove (aEmailData);
            s_aStatsCountFailed.increment ();
          }
        } // for all messages
      }
      catch (final AuthenticationFailedException ex)
      {
        // problem with the credentials
        aExceptionToBeRemembered = new MailSendException ("Mail server authentication failed", ex);
      }
      catch (final MessagingException ex)
      {
        if (WebExceptionHelper.isServerNotReachableConnection (ex.getCause ()))
          aExceptionToBeRemembered = new MailSendException ("Failed to connect to mail server: " +
                                                            ex.getCause ().getMessage ());
        else
          aExceptionToBeRemembered = new MailSendException ("Mail server connection failed", ex);
      }
      catch (final Throwable t)
      {
        // E.g. IllegalState from SMTPTransport ("Not connected")
        aExceptionToBeRemembered = new MailSendException ("Internal error sending mail", t);
      }
      finally
      {
        // Was any message not sent
        if (aRemainingMessages.isNotEmpty ())
        {
          if (aExceptionToBeRemembered == null)
            aExceptionToBeRemembered = new MailSendException ("Internal error - messages are remaining but no Exception occurred!");
          for (final IMutableEmailData aRemainingMessage : aRemainingMessages)
            aFailedMessages.put (aRemainingMessage, new MailTransportError (aExceptionToBeRemembered));
        }
      }
    }
    return aFailedMessages;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
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
                                       .getToString ();
  }
}
