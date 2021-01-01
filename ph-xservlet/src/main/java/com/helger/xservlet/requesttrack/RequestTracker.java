/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.callback.CallbackList;
import com.helger.commons.concurrent.BasicThreadFactory;
import com.helger.commons.concurrent.ExecutorServiceHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.scope.IScope;
import com.helger.servlet.mock.OfflineHttpServletRequest;
import com.helger.web.scope.IGlobalWebScope;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.mgr.WebScopeManager;
import com.helger.web.scope.mgr.WebScoped;
import com.helger.web.scope.singleton.AbstractGlobalWebSingleton;

/**
 * This is the entry point for request time monitoring. It keeps a central
 * {@link RequestTrackingManager} and runs a daemon {@link Thread} for
 * monitoring all open requests.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@Immutable
public final class RequestTracker extends AbstractGlobalWebSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RequestTracker.class);

  private static final CallbackList <ILongRunningRequestCallback> s_aLongRunningCallbacks = new CallbackList <> ();
  private static final CallbackList <IParallelRunningRequestCallback> s_aParallelRunningCallbacks = new CallbackList <> ();

  static
  {
    // Register default callbacks
    s_aLongRunningCallbacks.add (new LoggingLongRunningRequestCallback (EErrorLevel.ERROR));
    // s_aLongRunningCallbacks.add (new AuditingLongRunningRequestCallback ());
    s_aParallelRunningCallbacks.add (new LoggingParallelRunningRequestCallback (EErrorLevel.WARN));
    // s_aParallelRunningCallbacks.add (new
    // AuditingParallelRunningRequestCallback ());
  }

  @Nonnull
  @ReturnsMutableObject
  public static CallbackList <ILongRunningRequestCallback> longRunningRequestCallbacks ()
  {
    return s_aLongRunningCallbacks;
  }

  @Nonnull
  @ReturnsMutableObject
  public static CallbackList <IParallelRunningRequestCallback> parallelRunningRequestCallbacks ()
  {
    return s_aParallelRunningCallbacks;
  }

  private final RequestTrackingManager m_aRequestTrackingMgr = new RequestTrackingManager ();
  private final ScheduledExecutorService m_aExecSvc;

  private final class RequestTrackerMonitor implements Runnable
  {
    private final IGlobalWebScope m_aGlobalScope;

    public RequestTrackerMonitor ()
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
          m_aRequestTrackingMgr.checkForLongRunningRequests (s_aLongRunningCallbacks);
        }
        catch (final Exception ex)
        {
          LOGGER.error ("Error checking for long running requests", ex);
        }
      }
    }
  }

  @Deprecated
  @UsedViaReflection
  public RequestTracker ()
  {
    // Create the executor service
    m_aExecSvc = Executors.newSingleThreadScheduledExecutor (new BasicThreadFactory.Builder ().setNamingPattern ("RequestTrackerMonitor-%d")
                                                                                              .setDaemon (true)
                                                                                              .build ());

    // Start the monitoring thread to check every 2 seconds
    m_aExecSvc.scheduleAtFixedRate (new RequestTrackerMonitor (), 0, 2, TimeUnit.SECONDS);
    LOGGER.info ("RequestTrackerMonitor was installed successfully.");
  }

  @Override
  protected void onDestroy (@Nonnull final IScope aScopeInDestruction)
  {
    LOGGER.info ("RequestTrackerMonitor is now shutting down");
    // Destroy RequestTrackerMonitor thread(s)
    ExecutorServiceHelper.shutdownAndWaitUntilAllTasksAreFinished (m_aExecSvc);
    LOGGER.info ("RequestTrackerMonitor was uninstalled successfully.");
  }

  @Nonnull
  public static RequestTracker getInstance ()
  {
    return getGlobalSingleton (RequestTracker.class);
  }

  /**
   * @return The underlying request tracking manager. Never <code>null</code>.
   *         Don't use except you know what you are doing!
   */
  @Nonnull
  public RequestTrackingManager getRequestTrackingMgr ()
  {
    return m_aRequestTrackingMgr;
  }

  /**
   * @return The executor service used to schedule the background tasks
   */
  @Nonnull
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
  public static void addRequest (@Nonnull @Nonempty final String sRequestID, @Nonnull final IRequestWebScope aRequestScope)
  {
    getInstance ().m_aRequestTrackingMgr.addRequest (sRequestID, aRequestScope, s_aParallelRunningCallbacks);
  }

  /**
   * Remove a request from the tracking.
   *
   * @param sRequestID
   *        The request ID.
   */
  public static void removeRequest (@Nonnull @Nonempty final String sRequestID)
  {
    final RequestTracker aTracker = getGlobalSingletonIfInstantiated (RequestTracker.class);
    if (aTracker != null)
      aTracker.m_aRequestTrackingMgr.removeRequest (sRequestID, s_aParallelRunningCallbacks);
  }
}
