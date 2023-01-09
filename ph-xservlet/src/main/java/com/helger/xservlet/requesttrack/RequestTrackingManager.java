/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.callback.CallbackList;
import com.helger.commons.callback.ICallbackList;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.web.scope.IRequestWebScope;

/**
 * The request time manager manages all currently running requests.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@ThreadSafe
public final class RequestTrackingManager
{
  @Deprecated
  public static final boolean DEFAULT_LONG_RUNNING_CHECK_ENABLED = RequestTrackerSettings.DEFAULT_LONG_RUNNING_CHECK_ENABLED;
  @Deprecated
  public static final long DEFAULT_NOTIFICATION_MILLISECONDS = RequestTrackerSettings.DEFAULT_LONG_RUNNING_NOTIFICATION_MILLISECONDS;
  @Deprecated
  public static final boolean DEFAULT_PARALLEL_RUNNING_REQUESTS_CHECK_ENABLED = RequestTrackerSettings.DEFAULT_PARALLEL_RUNNING_REQUESTS_CHECK_ENABLED;
  @Deprecated
  public static final int DEFAULT_PARALLEL_RUNNING_REQUESTS_BARRIER = RequestTrackerSettings.DEFAULT_PARALLEL_RUNNING_REQUESTS_BARRIER;

  private static final Logger LOGGER = LoggerFactory.getLogger (RequestTrackingManager.class);

  private final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("m_aRWLock")
  private boolean m_bParallelRunningRequestsAboveLimit = false;
  // Must be ordered!
  @GuardedBy ("m_aRWLock")
  private final ICommonsOrderedMap <String, TrackedRequest> m_aOpenRequests = new CommonsLinkedHashMap <> ();

  public RequestTrackingManager ()
  {}

  @Deprecated
  public boolean isLongRunningCheckEnabled ()
  {
    return RequestTrackerSettings.isLongRunningRequestsCheckEnabled ();
  }

  @Nonnull
  @Deprecated
  public RequestTrackingManager setLongRunningCheckEnabled (final boolean bLongRunningCheckEnabled)
  {
    RequestTrackerSettings.setLongRunningRequestsCheckEnabled (bLongRunningCheckEnabled);
    return this;
  }

  @Nonnegative
  @Deprecated
  public long getNotificationMilliseconds ()
  {
    return RequestTrackerSettings.getLongRunningRequestWarnDurationMillis ();
  }

  @Nonnull
  @Deprecated
  public RequestTrackingManager setNotificationMilliseconds (@Nonnegative final long nLongRunningMilliSeconds)
  {
    RequestTrackerSettings.setLongRunningRequestWarnDurationMillis (nLongRunningMilliSeconds);
    return this;
  }

  @Deprecated
  public boolean isParallelRunningRequestCheckEnabled ()
  {
    return RequestTrackerSettings.isParallelRunningRequestsCheckEnabled ();
  }

  @Nonnull
  @Deprecated
  public RequestTrackingManager setParallelRunningRequestCheckEnabled (final boolean bParallelRunningRequestCheckEnabled)
  {
    RequestTrackerSettings.setParallelRunningRequestsCheckEnabled (bParallelRunningRequestCheckEnabled);
    return this;
  }

  @Nonnegative
  @Deprecated
  public int getParallelRunningRequestBarrier ()
  {
    return RequestTrackerSettings.getParallelRunningRequestBarrier ();
  }

  @Nonnull
  @Deprecated
  public RequestTrackingManager setParallelRunningRequestBarrier (@Nonnegative final int nParallelRunningRequestBarrier)
  {
    RequestTrackerSettings.setParallelRunningRequestBarrier (nParallelRunningRequestBarrier);
    return this;
  }

  public void addRequest (@Nonnull @Nonempty final String sRequestID,
                          @Nonnull final IRequestWebScope aRequestScope,
                          @Nonnull final CallbackList <IParallelRunningRequestCallback> aCallbacks)
  {
    boolean bNotifyOnParallelRequests = false;
    ICommonsList <TrackedRequest> aOpenRequests = null;
    m_aRWLock.writeLock ().lock ();
    try
    {
      final TrackedRequest aTR = new TrackedRequest (sRequestID, aRequestScope);
      final TrackedRequest aOldTR = m_aOpenRequests.put (sRequestID, aTR);

      // An old TR may be present, if the request is dispatched internally, but
      // in that case the request scope must have the same identity!
      if (aOldTR != null && aOldTR.getRequestScope () != aRequestScope)
      {
        // Should never happen
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Request ID '" + sRequestID + "' is already registered! Old TR: " + aOldTR + "; New TR: " + aTR);
      }

      if (RequestTrackerSettings.isParallelRunningRequestsCheckEnabled () &&
          m_aOpenRequests.size () >= RequestTrackerSettings.getParallelRunningRequestBarrier ())
      {
        // Grab directly here to avoid another locked section
        bNotifyOnParallelRequests = true;
        aOpenRequests = m_aOpenRequests.copyOfValues ();
        // Remember that we're above limit
        m_bParallelRunningRequestsAboveLimit = true;
      }
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    if (bNotifyOnParallelRequests)
    {
      // Invoke callbacks "above limit"
      final ICommonsList <TrackedRequest> aFinalOpenRequests = aOpenRequests;
      aCallbacks.forEach (aCB -> aCB.onParallelRunningRequests (aFinalOpenRequests.size (), aFinalOpenRequests));
    }
  }

  public void removeRequest (@Nonnull @Nonempty final String sRequestID,
                             @Nonnull final CallbackList <IParallelRunningRequestCallback> aCallbacks)
  {
    boolean bNowBelowLimit = false;
    m_aRWLock.writeLock ().lock ();
    try
    {
      if (m_aOpenRequests.remove (sRequestID) == null)
      {
        // Should never happen
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Failed to remove internal request with ID '" + sRequestID + "'");
      }
      else
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Removed request with ID '" + sRequestID + "'");
      }
      if (RequestTrackerSettings.isParallelRunningRequestsCheckEnabled () &&
          m_bParallelRunningRequestsAboveLimit &&
          m_aOpenRequests.size () < RequestTrackerSettings.getParallelRunningRequestBarrier ())
      {
        // Back to normal!
        m_bParallelRunningRequestsAboveLimit = false;
        bNowBelowLimit = true;
      }
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    if (bNowBelowLimit)
    {
      // Invoke callbacks "below limit again"
      aCallbacks.forEach (IParallelRunningRequestCallback::onParallelRunningRequestsBelowLimit);
    }
  }

  public void checkForLongRunningRequests (@Nonnull final ICallbackList <ILongRunningRequestCallback> aCallbacks)
  {
    if (aCallbacks.isNotEmpty ())
    {
      m_aRWLock.readLock ().lock ();
      try
      {
        // Check only if they are enabled!
        if (RequestTrackerSettings.isLongRunningRequestsCheckEnabled ())
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Checking for long running requests");

          // Grab in read lock!
          final long nNotificationMS = RequestTrackerSettings.getLongRunningRequestWarnDurationMillis ();

          // Iterate all running requests
          final Iterator <Map.Entry <String, TrackedRequest>> it = m_aOpenRequests.entrySet ().iterator ();
          while (it.hasNext ())
          {
            final Map.Entry <String, TrackedRequest> aItem = it.next ();
            final long nRunningMilliseconds = aItem.getValue ().getRunningMilliseconds ();
            if (nRunningMilliseconds > nNotificationMS)
            {
              // Invoke callbacks
              aCallbacks.forEach (aCB -> aCB.onLongRunningRequest (aItem.getKey (),
                                                                   aItem.getValue ().getRequestScope (),
                                                                   nRunningMilliseconds));
            }
            else
            {
              // Don't check any further, since all other requests should be
              // younger than the current one because we're using a
              // CommonsLinkedHashMap!
              break;
            }
          }
        }
      }
      finally
      {
        m_aRWLock.readLock ().unlock ();
      }
    }
  }
}
