/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ELockType;
import com.helger.commons.annotation.MustBeLocked;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.concurrent.BasicThreadFactory;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.state.EChange;
import com.helger.commons.state.ESuccess;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.vendor.VendorInfo;
import com.helger.smtp.EmailGlobalSettings;
import com.helger.smtp.data.IMutableEmailData;
import com.helger.smtp.failed.FailedMailData;
import com.helger.smtp.failed.FailedMailQueue;
import com.helger.smtp.settings.ISMTPSettings;

/**
 * This class simplifies the task of sending an email. For a scope aware version
 * please see {@link com.helger.smtp.scope.ScopedMailAPI}.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class MailAPI
{
  /** The default prefix used in debug mode */
  public static final String DEBUG_SUBJECT_PREFIX = "[DEBUG] ";
  /** The default subject to be used if none is specified */
  public static final String DEFAULT_SUBJECT = "<no subject specified>";
  /** By default wait until all queued mails are send */
  public static final boolean DEFAULT_STOP_IMMEDIATLY = false;

  private static final Logger LOGGER = LoggerFactory.getLogger (MailAPI.class);
  private static final IMutableStatisticsHandlerCounter STATS_MAILS_QUEUED = StatisticsManager.getCounterHandler (MailAPI.class.getName () +
                                                                                                                  "$mails.queued");

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static final ICommonsMap <ISMTPSettings, MailQueuePerSMTP> QUEUE_CACHE = new CommonsHashMap <> ();
  // Just to have custom named threads....
  private static final ThreadFactory THREAD_FACTORY = new BasicThreadFactory.Builder ().namingPattern ("MailAPI-%d")
                                                                                       .daemon (true)
                                                                                       .priority (Thread.NORM_PRIORITY)
                                                                                       .build ();
  @GuardedBy ("RW_LOCK")
  private static final ExecutorService SENDER_THREAD_POOL = new ThreadPoolExecutor (0,
                                                                                    Integer.MAX_VALUE,
                                                                                    60L,
                                                                                    TimeUnit.SECONDS,
                                                                                    new SynchronousQueue <> (),
                                                                                    THREAD_FACTORY);
  @GuardedBy ("RW_LOCK")
  private static FailedMailQueue s_aFailedMailQueue = new FailedMailQueue ();

  private MailAPI ()
  {}

  /**
   * @return The current failed mail queue. Never <code>null</code>.
   */
  @Nonnull
  public static FailedMailQueue getFailedMailQueue ()
  {
    return RW_LOCK.readLockedGet ( () -> s_aFailedMailQueue);
  }

  /**
   * Set a new global failed mail queue. Updates all existing queues.
   *
   * @param aFailedMailQueue
   *        The new failed mail queue to set. May not be <code>null</code>.
   */
  public static void setFailedMailQueue (@Nonnull final FailedMailQueue aFailedMailQueue)
  {
    ValueEnforcer.notNull (aFailedMailQueue, "FailedMailQueue");

    RW_LOCK.writeLocked ( () -> {
      s_aFailedMailQueue = aFailedMailQueue;

      // Update all existing queues
      for (final MailQueuePerSMTP aMailQueue : QUEUE_CACHE.values ())
        aMailQueue.setFailedMailQueue (aFailedMailQueue);
    });

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Set FailedMailQueue to " + aFailedMailQueue);
  }

  @Nonnull
  @MustBeLocked (ELockType.WRITE)
  private static MailQueuePerSMTP _getOrCreateMailQueuePerSMTP (@Nonnull final ISMTPSettings aSMTPSettings)
  {
    ValueEnforcer.notNull (aSMTPSettings, "SmtpSettings");
    if (SENDER_THREAD_POOL.isShutdown ())
      throw new IllegalStateException ("Cannot submit to mailqueues that are already stopped!");

    // get queue per SMTP
    MailQueuePerSMTP aSMTPQueue = QUEUE_CACHE.get (aSMTPSettings);
    if (aSMTPQueue == null)
    {
      // create a new queue
      aSMTPQueue = new MailQueuePerSMTP (EmailGlobalSettings.getMaxMailQueueLength (),
                                         EmailGlobalSettings.getMaxMailSendCount (),
                                         aSMTPSettings,
                                         getFailedMailQueue ());

      // put queue in cache
      QUEUE_CACHE.put (aSMTPSettings, aSMTPQueue);

      // and start running the queue
      SENDER_THREAD_POOL.submit (aSMTPQueue::collect);
    }
    return aSMTPQueue;
  }

  public static boolean hasNonVendorEmailAddress (@Nullable final Iterable <? extends IEmailAddress> aAddresses)
  {
    if (aAddresses != null)
    {
      final String sVendorEmailSuffix = VendorInfo.getVendorEmailSuffix ();
      for (final IEmailAddress aAddress : aAddresses)
        if (!aAddress.getAddress ().endsWith (sVendorEmailSuffix))
          return true;
    }
    return false;
  }

  /**
   * Queue a single mail.
   *
   * @param aSMTPSettings
   *        The SMTP settings to be used.
   * @param aMailData
   *        The mail message to queue. May not be <code>null</code>.
   * @return {@link ESuccess}.
   */
  @Nonnull
  public static ESuccess queueMail (@Nonnull final ISMTPSettings aSMTPSettings, @Nonnull final IMutableEmailData aMailData)
  {
    final int nQueuedMails = queueMails (aSMTPSettings, new CommonsArrayList <> (aMailData));
    return ESuccess.valueOf (nQueuedMails == 1);
  }

  /**
   * Queue more than one mail.
   *
   * @param aSMTPSettings
   *        The SMTP settings to be used.
   * @param aMailDataList
   *        The mail messages to queue. May not be <code>null</code>.
   * @return The number of queued emails. Always &ge; 0. Maximum value is the
   *         number of {@link IMutableEmailData} objects in the argument.
   */
  @Nonnegative
  public static int queueMails (@Nonnull final ISMTPSettings aSMTPSettings,
                                @Nonnull final Collection <? extends IMutableEmailData> aMailDataList)
  {
    ValueEnforcer.notNull (aSMTPSettings, "SmtpSettings");
    ValueEnforcer.notNull (aMailDataList, "MailDataList");
    if (aMailDataList.isEmpty ())
      throw new IllegalArgumentException ("At least one message has to be supplied!");

    MailQueuePerSMTP aSMTPQueue;
    RW_LOCK.writeLock ().lock ();
    try
    {
      // get queue per SMTP settings
      try
      {
        aSMTPQueue = _getOrCreateMailQueuePerSMTP (aSMTPSettings);
      }
      catch (final IllegalStateException ex)
      {
        // Happens if queue is already stopped
        // Put all errors in failed mail queue
        final MailTransportError aError = new MailTransportError (ex);
        for (final IMutableEmailData aEmailData : aMailDataList)
          getFailedMailQueue ().add (new FailedMailData (aSMTPSettings, aEmailData, aError));
        return 0;
      }
    }
    finally
    {
      RW_LOCK.writeLock ().unlock ();
    }

    int nQueuedMails = 0;
    final boolean bSendVendorOnlyMails = GlobalDebug.isDebugMode ();

    // submit all messages
    for (final IMutableEmailData aEmailData : aMailDataList)
    {
      if (aEmailData == null)
      {
        LOGGER.error ("Mail data is null! Ignoring this item completely.");
        continue;
      }

      // queue the mail
      STATS_MAILS_QUEUED.increment ();

      // Do some consistency checks to ensure this particular email can be
      // send
      boolean bCanQueue = true;

      if (aEmailData.getFrom () == null)
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Mail data has no sender address: " + aEmailData + " - not queuing!");
        bCanQueue = false;
      }

      if (aEmailData.to ().isEmpty ())
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Mail data has no receiver address: " + aEmailData + " - not queuing!");
        bCanQueue = false;
      }

      if (bSendVendorOnlyMails)
      {
        // In the debug version we can *only* send to vendor addresses!
        if (hasNonVendorEmailAddress (aEmailData.to ()) ||
            hasNonVendorEmailAddress (aEmailData.cc ()) ||
            hasNonVendorEmailAddress (aEmailData.bcc ()))
        {
          if (LOGGER.isErrorEnabled ())
            LOGGER.error ("Debug mode: ignoring mail TO '" +
                          aEmailData.to () +
                          "'" +
                          (aEmailData.cc ().isNotEmpty () ? " and CC '" + aEmailData.cc () + "'" : "") +
                          (aEmailData.bcc ().isNotEmpty () ? " and BCC '" + aEmailData.bcc () + "'" : "") +
                          " because at least one address is not targeted to the vendor domain '" +
                          VendorInfo.getVendorEmailSuffix () +
                          "'");
          bCanQueue = false;
        }
      }

      // Check if queue is already stopped
      if (aSMTPQueue.isStopped ())
      {
        LOGGER.error ("Queue is already stopped - not queuing!");
        bCanQueue = false;
      }

      boolean bWasQueued = false;
      Exception aException = null;
      if (bCanQueue)
      {
        // Check if a subject is present
        if (StringHelper.hasNoText (aEmailData.getSubject ()))
        {
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("Mail data has no subject: " + aEmailData + " - defaulting to " + DEFAULT_SUBJECT);
          aEmailData.setSubject (DEFAULT_SUBJECT);
        }

        // Check if a body is present
        if (StringHelper.hasNoText (aEmailData.getBody ()))
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("Mail data has no body: " + aEmailData);

        if (bSendVendorOnlyMails)
        {
          // Add special debug prefix
          if (!StringHelper.startsWith (aEmailData.getSubject (), DEBUG_SUBJECT_PREFIX))
            aEmailData.setSubject (DEBUG_SUBJECT_PREFIX + aEmailData.getSubject ());

          if (LOGGER.isInfoEnabled ())
            LOGGER.info ("Sending only-to-vendor mail in debug version:\n" + aSMTPSettings + "\n" + aEmailData);
        }

        // Uses UTC timezone!
        aEmailData.setSentDateTime (PDTFactory.getCurrentLocalDateTime ());
        try
        {
          if (aSMTPQueue.queueObject (aEmailData).isSuccess ())
          {
            bWasQueued = true;
            ++nQueuedMails;
          }
          // else an error message was already logged
        }
        catch (final Exception ex)
        {
          // Occurs if queue is already stopped
          aException = ex;
        }
      }
      else
      {
        aException = new MailSendException ("Email cannot be queued because internal constraints are not fulfilled!");
      }

      if (!bWasQueued)
      {
        // Mail was not queued - put in failed mail queue
        aSMTPQueue.getFailedMailQueue ().add (new FailedMailData (aSMTPSettings, aEmailData, new MailTransportError (aException)));
      }
    }
    return nQueuedMails;
  }

  @Nonnegative
  @MustBeLocked (ELockType.READ)
  private static int _getTotalQueueLength ()
  {
    int ret = 0;
    // count over all queues
    for (final MailQueuePerSMTP aQueue : QUEUE_CACHE.values ())
      ret += aQueue.getQueueLength ();
    return ret;
  }

  @Nonnegative
  public static int getTotalQueueLength ()
  {
    return RW_LOCK.readLockedInt (MailAPI::_getTotalQueueLength);
  }

  /**
   * Stop taking new mails, and wait until all mails already in the queue are
   * delivered.
   *
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange stop ()
  {
    return stop (DEFAULT_STOP_IMMEDIATLY);
  }

  /**
   * Stop taking new mails, and wait until all mails already in the queue are
   * delivered.
   *
   * @param bStopImmediately
   *        <code>true</code> if all mails currently in the queue should be
   *        removed and put in the failed mail queue. Only the emails currently
   *        in sending are continued to be sent out.
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange stop (final boolean bStopImmediately)
  {
    RW_LOCK.writeLock ().lock ();
    try
    {
      // Check if the thread pool is already shut down
      if (SENDER_THREAD_POOL.isShutdown ())
        return EChange.UNCHANGED;

      // don't take any more actions
      SENDER_THREAD_POOL.shutdown ();

      // stop all specific queues afterwards
      for (final MailQueuePerSMTP aQueue : QUEUE_CACHE.values ())
        aQueue.stopQueuingNewObjects (bStopImmediately);

      final int nQueues = QUEUE_CACHE.size ();
      // Subtract 1 for the STOP_MESSAGE
      final int nQueueLength = _getTotalQueueLength () - 1;
      if (nQueues > 0 || nQueueLength > 0)
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Stopping central mail queues: " +
                       nQueues +
                       " queue" +
                       (nQueues == 1 ? "" : "s") +
                       " with " +
                       nQueueLength +
                       " mail" +
                       (nQueueLength == 1 ? "" : "s"));
    }
    finally
    {
      RW_LOCK.writeLock ().unlock ();
    }

    // Don't wait in a writeLock!
    try
    {
      while (!SENDER_THREAD_POOL.awaitTermination (1, TimeUnit.SECONDS))
      {
        // wait until we're done
      }
    }
    catch (final InterruptedException ex)
    {
      LOGGER.error ("Error stopping mail queue", ex);
      Thread.currentThread ().interrupt ();
    }
    return EChange.CHANGED;
  }
}
