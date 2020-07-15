/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.web.scope.util;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.state.ESuccess;
import com.helger.quartz.IJob;
import com.helger.quartz.IJobExecutionContext;
import com.helger.quartz.JobDataMap;
import com.helger.schedule.job.AbstractJob;
import com.helger.scope.mgr.ScopeManager;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.mock.MockHttpServletResponse;
import com.helger.servlet.mock.OfflineHttpServletRequest;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * Abstract {@link IJob} implementation that handles request scopes correctly.
 * This is required, because each scheduled job runs in its own thread so that
 * no default {@link ScopeManager} information would be available.
 *
 * @author Philip Helger
 */
@ThreadSafe
public abstract class AbstractScopeAwareJob extends AbstractJob
{
  public AbstractScopeAwareJob ()
  {}

  /**
   * @return The dummy HTTP request to be used for executing this job. By
   *         default an {@link OfflineHttpServletRequest} is created.
   */
  @Nonnull
  @OverrideOnDemand
  protected MockHttpServletRequest createMockHttpServletRequest ()
  {
    // Don't use "MockServletContext" to avoid that the global Servlet Context
    // Path is overriden!
    final OfflineHttpServletRequest ret = new OfflineHttpServletRequest (WebScopeManager.getGlobalScope ().getServletContext (), false);
    // Use a fixed session ID, because Quartz jobs regularly use the session and
    // this avoids spanning too many sessions
    ret.setSessionID ("quartz.job." + ClassHelper.getClassLocalName (getClass ()));
    return ret;
  }

  /**
   * @return The dummy HTTP response to be used for executing this job. By
   *         default a {@link MockHttpServletResponse} is created.
   */
  @Nonnull
  @OverrideOnDemand
  protected MockHttpServletResponse createMockHttpServletResponse ()
  {
    return new MockHttpServletResponse ();
  }

  /**
   * Called before the job gets executed. This method is called after the scopes
   * are initialized!
   *
   * @param aJobDataMap
   *        The current job data map. Never <code>null</code>.
   * @param aContext
   *        The current job execution context. Never <code>null</code>.
   */
  @OverrideOnDemand
  protected void beforeExecuteInScope (@Nonnull final JobDataMap aJobDataMap, @Nonnull final IJobExecutionContext aContext)
  {}

  @Override
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  protected void beforeExecute (@Nonnull final JobDataMap aJobDataMap, @Nonnull final IJobExecutionContext aContext)
  {
    // Scopes (ensure to create a new scope each time!)
    final MockHttpServletRequest aHttpRequest = createMockHttpServletRequest ();
    final MockHttpServletResponse aHttpResponse = createMockHttpServletResponse ();
    WebScopeManager.onRequestBegin (aHttpRequest, aHttpResponse);

    // Invoke callback
    beforeExecuteInScope (aJobDataMap, aContext);
  }

  /**
   * Called after the job gets executed. This method is called before the scopes
   * are destroyed.
   *
   * @param aJobDataMap
   *        The current job data map. Never <code>null</code>.
   * @param aContext
   *        The current job execution context. Never <code>null</code>.
   * @param eExecSuccess
   *        The execution success state. Never <code>null</code>.
   */
  @OverrideOnDemand
  protected void afterExecuteInScope (@Nonnull final JobDataMap aJobDataMap,
                                      @Nonnull final IJobExecutionContext aContext,
                                      @Nonnull final ESuccess eExecSuccess)
  {}

  @Override
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  protected void afterExecute (@Nonnull final JobDataMap aJobDataMap,
                               @Nonnull final IJobExecutionContext aContext,
                               @Nonnull final ESuccess eExecSuccess)
  {
    try
    {
      // Invoke callback
      afterExecuteInScope (aJobDataMap, aContext, eExecSuccess);
    }
    finally
    {
      // Close request scope
      WebScopeManager.onRequestEnd ();
    }
  }
}
