/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.xservlet.requesttrack;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.annotation.style.UsedViaReflection;
import com.helger.base.callback.CallbackList;
import com.helger.base.concurrent.BasicThreadFactory;
import com.helger.base.concurrent.ExecutorServiceHelper;
import com.helger.diagnostics.error.level.EErrorLevel;
import com.helger.scope.IScope;
import com.helger.servlet.mock.OfflineHttpServletRequest;
import com.helger.web.scope.IGlobalWebScope;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.mgr.WebScopeManager;
import com.helger.web.scope.mgr.WebScoped;
import com.helger.web.scope.singleton.AbstractGlobalWebSingleton;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is the entry point for request time monitoring. It keeps a central
 * {@link RequestTrackingManager} and runs a daemon {@link Thread} for monitoring all open requests.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@Immutable
public final class RequestTracker extends AbstractGlobalWebSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RequestTracker.class);

  private static final CallbackList <ILongRunningRequestCallback> CB_LONG_RUNNING = new CallbackList <> ();
  private static final CallbackList <IParallelRunningRequestCallback> CB_PARALLEL_RUNNING = new CallbackList <> ();

  static
  {
    // Register default callbacks
    CB_LONG_RUNNING.add (new LoggingLongRunningRequestCallback (EErrorLevel.ERROR));
    // CB_LONG_RUNNING.add (new AuditingLongRunningRequestCallback ());
    CB_PARALLEL_RUNNING.add (new LoggingParallelRunningRequestCallback (EErrorLevel.WARN));
    // CB_PARALLEL_RUNNING.add (new
    // AuditingParallelRunningRequestCallback ());
  }

  @NonNull
  @ReturnsMutableObject
  public static CallbackList <ILongRunningRequestCallback> longRunningRequestCallbacks ()
  {
    return CB_LONG_RUNNING;
  }

  @NonNull
  @ReturnsMutableObject
  public static CallbackList <IParallelRunningRequestCallback> parallelRunningRequestCallbacks ()
  {
    return CB_PARALLEL_RUNNING;
  }

  private final RequestTrackingManager m_aRequestTrackingMgr = new RequestTrackingManager ();
  private final ScheduledExecutorService m_aExecSvc;

  private final class LongRunningRequestMonitor implements Runnable
  {
    private final IGlobalWebScope m_aGlobalScope;

    public LongRunningRequestMonitor ()
    {
      // Remember once here
      m_aGlobalScope = WebScopeManager.getGlobalScope ();
    }

    public void run ()
    {
      // Global scope may not be present here (on shutdown)
      // And we cannot retrieve it here, because the shutdown is called in a
      // ScopeManager write lock!
      if (m_aGlobalScope.isValid ())
      {
        final HttpServletRequest aRequest = new OfflineHttpServletRequest (m_aGlobalScope.getServletContext (), false);
        try (final WebScoped aWebScoped = new WebScoped (aRequest))
        {
          // Check for long running requests
          m_aRequestTrackingMgr.checkForLongRunningRequests (CB_LONG_RUNNING);
        }
        catch (final Exception ex)
        {
          LOGGER.error ("Error checking for long running requests", ex);
        }
      }
    }
  }

  @Deprecated (forRemoval = false)
  @UsedViaReflection
  public RequestTracker ()
  {
    // Create the executor service
    m_aExecSvc = Executors.newSingleThreadScheduledExecutor (BasicThreadFactory.builder ()
                                                                               .namingPattern ("RequestTrackerMonitor-%d")
                                                                               .daemon (true)
                                                                               .build ());

    if (RequestTrackerSettings.isLongRunningRequestsCheckEnabled ())
    {
      final long nIntervalMilliseconds = RequestTrackerSettings.getLongRunningRequestCheckIntervalMilliseconds ();

      // Start the monitoring thread to check every n milliseconds
      m_aExecSvc.scheduleAtFixedRate (new LongRunningRequestMonitor (),
                                      0,
                                      nIntervalMilliseconds,
                                      TimeUnit.MILLISECONDS);
      LOGGER.info ("LongRunningRequestMonitor was installed successfully at an interval of " +
                   nIntervalMilliseconds +
                   " milliseconds.");
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("LongRunningRequestMonitor is disabled.");
    }
  }

  @Override
  protected void onDestroy (@NonNull final IScope aScopeInDestruction)
  {
    LOGGER.info ("RequestTrackerMonitor is now shutting down");
    // Destroy RequestTrackerMonitor thread(s)
    ExecutorServiceHelper.shutdownAndWaitUntilAllTasksAreFinished (m_aExecSvc);
    LOGGER.info ("RequestTrackerMonitor was uninstalled successfully.");
  }

  @NonNull
  public static RequestTracker getInstance ()
  {
    return getGlobalSingleton (RequestTracker.class);
  }

  /**
   * @return The underlying request tracking manager. Never <code>null</code>. Don't use except you
   *         know what you are doing!
   */
  @NonNull
  public RequestTrackingManager getRequestTrackingMgr ()
  {
    return m_aRequestTrackingMgr;
  }

  /**
   * @return The executor service used to schedule the background tasks
   */
  @NonNull
  public ScheduledExecutorService getExecutorService ()
  {
    return m_aExecSvc;
  }

  /**
   * Add new request to the tracking
   *
   * @param sRequestID
   *        The unique request ID.
   * @param aRequestScope
   *        The request scope itself.
   */
  public static void addRequest (@NonNull @Nonempty final String sRequestID,
                                 @NonNull final IRequestWebScope aRequestScope)
  {
    getInstance ().m_aRequestTrackingMgr.addRequest (sRequestID, aRequestScope, CB_PARALLEL_RUNNING);
  }

  /**
   * Remove a request from the tracking.
   *
   * @param sRequestID
   *        The request ID.
   */
  public static void removeRequest (@NonNull @Nonempty final String sRequestID)
  {
    final RequestTracker aTracker = getGlobalSingletonIfInstantiated (RequestTracker.class);
    if (aTracker != null)
      aTracker.m_aRequestTrackingMgr.removeRequest (sRequestID, CB_PARALLEL_RUNNING);
  }
}
