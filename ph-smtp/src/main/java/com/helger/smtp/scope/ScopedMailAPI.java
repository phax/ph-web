/**
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
package com.helger.smtp.scope;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.state.ESuccess;
import com.helger.scope.IScope;
import com.helger.scope.singleton.AbstractGlobalSingleton;
import com.helger.smtp.data.IMutableEmailData;
import com.helger.smtp.failed.FailedMailQueue;
import com.helger.smtp.settings.ISMTPSettings;
import com.helger.smtp.transport.MailAPI;

/**
 * Scope aware wrapper around {@link MailAPI} class so that it is gracefully
 * stopped when the global scope is stopped.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class ScopedMailAPI extends AbstractGlobalSingleton
{
  public static final boolean DEFAULT_STOP_IMMEDIATLY = false;

  private final AtomicBoolean m_aStopImmediately = new AtomicBoolean (DEFAULT_STOP_IMMEDIATLY);

  @UsedViaReflection
  @Deprecated
  public ScopedMailAPI ()
  {}

  @Nonnull
  public static ScopedMailAPI getInstance ()
  {
    return getGlobalSingleton (ScopedMailAPI.class);
  }

  /**
   * @return The current failed mail queue. Never <code>null</code>.
   */
  @Nonnull
  public FailedMailQueue getFailedMailQueue ()
  {
    return MailAPI.getFailedMailQueue ();
  }

  /**
   * Set a new global failed mail queue. Updates all existing queues.
   *
   * @param aFailedMailQueue
   *        The new failed mail queue to set. May not be <code>null</code>.
   */
  public void setFailedMailQueue (@Nonnull final FailedMailQueue aFailedMailQueue)
  {
    MailAPI.setFailedMailQueue (aFailedMailQueue);
  }

  /**
   * Unconditionally queue a mail
   *
   * @param aSMTPSettings
   *        The SMTP settings to use. May not be <code>null</code>.
   * @param aMailData
   *        The data of the email to be send. May not be <code>null</code>.
   * @return {@link ESuccess}
   */
  @Nonnull
  public ESuccess queueMail (@Nonnull final ISMTPSettings aSMTPSettings, @Nonnull final IMutableEmailData aMailData)
  {
    return MailAPI.queueMail (aSMTPSettings, aMailData);
  }

  /**
   * Queue multiple mails at once.
   *
   * @param aSMTPSettings
   *        The SMTP settings to be used.
   * @param aMailDataList
   *        The mail messages to queue. May not be <code>null</code>.
   * @return The number of queued emails. Always &ge; 0. Maximum value is the
   *         number of {@link IMutableEmailData} objects in the argument.
   */
  @Nonnegative
  public int queueMails (@Nonnull final ISMTPSettings aSMTPSettings, @Nonnull final Collection <? extends IMutableEmailData> aMailDataList)
  {
    return MailAPI.queueMails (aSMTPSettings, aMailDataList);
  }

  @Nonnegative
  public int getTotalQueueLength ()
  {
    return MailAPI.getTotalQueueLength ();
  }

  /**
   * @return <code>true</code> if all mails currently in the queue should be
   *         removed and put in the failed mail queue. Only the emails currently
   *         in sending are continued to be sent out.
   */
  public boolean isStopImmediately ()
  {
    return m_aStopImmediately.get ();
  }

  /**
   * Determine whether to stop immediately or not
   *
   * @param bStopImmediately
   *        <code>true</code> if all mails currently in the queue should be
   *        removed and put in the failed mail queue. Only the emails currently
   *        in sending are continued to be sent out.
   */
  public void setStopImmediatly (final boolean bStopImmediately)
  {
    m_aStopImmediately.set (bStopImmediately);
  }

  @Override
  protected void onDestroy (@Nonnull final IScope aScopeInDestruction)
  {
    // Stop mail queues
    MailAPI.stop (isStopImmediately ());
  }
}
