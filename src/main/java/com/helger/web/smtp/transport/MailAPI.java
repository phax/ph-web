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
package com.helger.web.smtp.transport;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.GlobalDebug;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.concurrent.ExtendedDefaultThreadFactory;
import com.helger.commons.email.IEmailAddress;
import com.helger.commons.state.EChange;
import com.helger.commons.state.ESuccess;
import com.helger.commons.stats.IStatisticsHandlerCounter;
import com.helger.commons.stats.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.vendor.VendorInfo;
import com.helger.datetime.PDTFactory;
import com.helger.web.smtp.EmailGlobalSettings;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.ISMTPSettings;
import com.helger.web.smtp.failed.FailedMailData;
import com.helger.web.smtp.failed.FailedMailQueue;
import com.helger.web.smtp.impl.ReadonlySMTPSettings;

/**
 * This class simplifies the task of sending an email.<br>
 * Note: do NOT use directly in pDAF3 - use the ScopedMailAPI instead.
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

  private static final Logger s_aLogger = LoggerFactory.getLogger (MailAPI.class);
  private static final IStatisticsHandlerCounter s_aQueuedMailHdl = StatisticsManager.getCounterHandler (MailAPI.class.getName () +
                                                                                                         "$mails.queued");

  private static final ReadWriteLock s_aRWLock = new ReentrantReadWriteLock ();
  private static final Map <ISMTPSettings, MailQueuePerSMTP> s_aQueueCache = new HashMap <ISMTPSettings, MailQueuePerSMTP> ();
  // Just to have custom named threads....
  private static final ThreadFactory s_aThreadFactory = new ExtendedDefaultThreadFactory ("MailAPI");
  private static final ExecutorService s_aSenderThreadPool = new ThreadPoolExecutor (0,
                                                                                     Integer.MAX_VALUE,
                                                                                     60L,
                                                                                     TimeUnit.SECONDS,
                                                                                     new SynchronousQueue <Runnable> (),
                                                                                     s_aThreadFactory);
  private static FailedMailQueue s_aFailedMailQueue = new FailedMailQueue ();

  private MailAPI ()
  {}

  /**
   * @return The current failed mail queue. Never <code>null</code>.
   */
  @Nonnull
  public static FailedMailQueue getFailedMailQueue ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_aFailedMailQueue;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
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

    s_aRWLock.writeLock ().lock ();
    try
    {
      s_aFailedMailQueue = aFailedMailQueue;

      // Update all existing queues
      for (final MailQueuePerSMTP aMailQueue : s_aQueueCache.values ())
        aMailQueue.setFailedMailQueue (aFailedMailQueue);
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }

    s_aLogger.info ("Set FailedMailQueue to " + aFailedMailQueue);
  }

  @Nonnull
  private static MailQueuePerSMTP _getOrCreateMailQueuePerSMTP (@Nonnull final ISMTPSettings aSMTPSettings)
  {
    ValueEnforcer.notNull (aSMTPSettings, "SmtpSettings");
    if (s_aSenderThreadPool.isShutdown ())
      throw new IllegalStateException ("Cannot submit to mailqueues that are already stopped!");

    // Ensure that always the same type is used!
    final ReadonlySMTPSettings aRealSMTPSettings = new ReadonlySMTPSettings (aSMTPSettings);

    // get queue per SMTP
    MailQueuePerSMTP aSMTPQueue = s_aQueueCache.get (aRealSMTPSettings);
    if (aSMTPQueue == null)
    {
      // create a new queue
      aSMTPQueue = new MailQueuePerSMTP (EmailGlobalSettings.getMaxMailQueueLength (),
                                         EmailGlobalSettings.getMaxMailSendCount (),
                                         aRealSMTPSettings,
                                         s_aFailedMailQueue);

      // put queue in cache
      s_aQueueCache.put (aRealSMTPSettings, aSMTPQueue);

      // and start running the queue
      s_aSenderThreadPool.submit (aSMTPQueue);
    }
    return aSMTPQueue;
  }

  public static boolean hasNonVendorEmailAddress (@Nullable final List <? extends IEmailAddress> aAddresses)
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
  public static ESuccess queueMail (@Nonnull final ISMTPSettings aSMTPSettings, @Nonnull final IEmailData aMailData)
  {
    final int nQueuedMails = queueMails (aSMTPSettings, ContainerHelper.newList (aMailData));
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
   *         number of {@link IEmailData} objects in the argument.
   */
  @Nonnegative
  public static int queueMails (@Nonnull final ISMTPSettings aSMTPSettings,
                                @Nonnull final Collection <? extends IEmailData> aMailDataList)
  {
    ValueEnforcer.notNull (aSMTPSettings, "SmtpSettings");
    ValueEnforcer.notNull (aMailDataList, "MailDataList");
    if (aMailDataList.isEmpty ())
      throw new IllegalArgumentException ("At least one message has to be supplied!");

    MailQueuePerSMTP aSMTPQueue;
    s_aRWLock.writeLock ().lock ();
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
        for (final IEmailData aEmailData : aMailDataList)
          getFailedMailQueue ().add (new FailedMailData (aSMTPSettings, aEmailData, ex));
        return 0;
      }
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }

    int nQueuedMails = 0;
    final boolean bSendVendorOnlyMails = GlobalDebug.isDebugMode ();

    // submit all messages
    for (final IEmailData aEmailData : aMailDataList)
    {
      if (aEmailData == null)
      {
        s_aLogger.error ("Mail data is null! Ignoring this item completely.");
        continue;
      }

      // queue the mail
      s_aQueuedMailHdl.increment ();

      // Do some consistency checks to ensure this particular email can be
      // send
      boolean bCanQueue = true;

      if (aEmailData.getFrom () == null)
      {
        s_aLogger.error ("Mail data has no sender address: " + aEmailData + " - not queuing!");
        bCanQueue = false;
      }

      if (aEmailData.getToCount () == 0)
      {
        s_aLogger.error ("Mail data has no receiver address: " + aEmailData + " - not queuing!");
        bCanQueue = false;
      }

      if (bSendVendorOnlyMails)
      {
        // In the debug version we can *only* send to vendor addresses!
        if (hasNonVendorEmailAddress (aEmailData.getTo ()) ||
            hasNonVendorEmailAddress (aEmailData.getCc ()) ||
            hasNonVendorEmailAddress (aEmailData.getBcc ()))
        {
          s_aLogger.error ("Debug mode: ignoring mail TO '" +
                           aEmailData.getTo () +
                           "'" +
                           (aEmailData.getCcCount () > 0 ? " and CC '" + aEmailData.getCc () + "'" : "") +
                           (aEmailData.getBccCount () > 0 ? " and BCC '" + aEmailData.getBcc () + "'" : "") +
                           " because at least one address is not targeted to the vendor domain '" +
                           VendorInfo.getVendorEmailSuffix () +
                           "'");
          bCanQueue = false;
        }
      }

      // Check if queue is already stopped
      if (aSMTPQueue.isStopped ())
      {
        s_aLogger.error ("Queue is already stopped - not queuing!");
        bCanQueue = false;
      }

      boolean bWasQueued = false;
      Exception aException = null;
      if (bCanQueue)
      {
        // Check if a subject is present
        if (StringHelper.hasNoText (aEmailData.getSubject ()))
        {
          s_aLogger.warn ("Mail data has no subject: " + aEmailData + " - defaulting to " + DEFAULT_SUBJECT);
          aEmailData.setSubject (DEFAULT_SUBJECT);
        }

        // Check if a body is present
        if (StringHelper.hasNoText (aEmailData.getBody ()))
          s_aLogger.warn ("Mail data has no body: " + aEmailData);

        if (bSendVendorOnlyMails)
        {
          // Add special debug prefix
          if (!StringHelper.startsWith (aEmailData.getSubject (), DEBUG_SUBJECT_PREFIX))
            aEmailData.setSubject (DEBUG_SUBJECT_PREFIX + aEmailData.getSubject ());
          s_aLogger.info ("Sending only-to-vendor mail in debug version:\n" + aSMTPSettings + "\n" + aEmailData);
        }

        // Uses UTC timezone!
        aEmailData.setSentDate (PDTFactory.getCurrentDateTime ());
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

      if (!bWasQueued)
      {
        // Mail was not queued - put in failed mail queue
        aSMTPQueue.getFailedMailQueue ().add (new FailedMailData (aSMTPSettings, aEmailData, aException));
      }
    }
    return nQueuedMails;
  }

  @Nonnegative
  private static int _getTotalQueueLength ()
  {
    int ret = 0;
    // count over all queues
    for (final MailQueuePerSMTP aQueue : s_aQueueCache.values ())
      ret += aQueue.getQueueLength ();
    return ret;
  }

  @Nonnegative
  public static int getTotalQueueLength ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return _getTotalQueueLength ();
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
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
    s_aRWLock.writeLock ().lock ();
    try
    {
      // Check if the thread pool is already shut down
      if (s_aSenderThreadPool.isShutdown ())
        return EChange.UNCHANGED;

      // don't take any more actions
      s_aSenderThreadPool.shutdown ();

      // stop all specific queues afterwards
      for (final MailQueuePerSMTP aQueue : s_aQueueCache.values ())
        aQueue.stopQueuingNewObjects (bStopImmediately);

      final int nQueues = s_aQueueCache.size ();
      // Subtract 1 for the STOP_MESSAGE
      final int nQueueLength = _getTotalQueueLength () - 1;
      if (nQueues > 0 || nQueueLength > 0)
        s_aLogger.info ("Stopping central mail queues: " +
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
      s_aRWLock.writeLock ().unlock ();
    }

    // Don't wait in a writeLock!
    try
    {
      while (!s_aSenderThreadPool.awaitTermination (1, TimeUnit.SECONDS))
      {
        // wait until we're done
      }
    }
    catch (final InterruptedException ex)
    {
      s_aLogger.error ("Error stopping mail queue", ex);
    }
    return EChange.CHANGED;
  }
}
