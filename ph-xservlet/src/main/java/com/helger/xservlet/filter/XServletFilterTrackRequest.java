/*
 * Copyright (C) 2016-2024 Philip Helger (www.helger.com)
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
package com.helger.xservlet.filter;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.scope.mgr.ScopeManager;
import com.helger.web.scope.IRequestWebScope;
import com.helger.xservlet.requesttrack.RequestTracker;

/**
 * A special filter that tracks the request. Each servlet request requires it's
 * own instance of this class!
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public final class XServletFilterTrackRequest implements IXServletHighLevelFilter
{
  /** The name of the request attribute uniquely identifying the request ID */
  public static final String REQUEST_ATTR_ID = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "request.id";

  private static final Logger LOGGER = LoggerFactory.getLogger (XServletFilterTrackRequest.class);

  /** Thread-safe request counter */
  private static final AtomicLong REQUEST_ID = new AtomicLong (0);

  private boolean m_bTrackedRequest = false;

  public XServletFilterTrackRequest ()
  {}

  private static boolean _trackBeforeHandleRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    // Check if an attribute is already present
    // An ID may already be present, if the request is internally dispatched
    // (e.g. via the error handler)
    String sID = aRequestScope.attrs ().getAsString (REQUEST_ATTR_ID);
    if (sID != null)
    {
      // Mainly debug logging to see, if this can be checked better
      // Therefore I need to understand better when this happens
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Request already contains an ID (" + sID + ") - so this is a recursive request...");
      return false;
    }

    // Create a unique ID for the request
    sID = Long.toString (REQUEST_ID.incrementAndGet ());

    // Remember in request scope
    aRequestScope.attrs ().putIn (REQUEST_ATTR_ID, sID);

    // Remember request
    RequestTracker.addRequest (sID, aRequestScope);

    return true;
  }

  private static void _trackAfterHandleRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    final String sID = aRequestScope.attrs ().getAsString (REQUEST_ATTR_ID);
    RequestTracker.removeRequest (sID);
  }

  public void beforeRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    m_bTrackedRequest = _trackBeforeHandleRequest (aRequestScope);
  }

  public void afterRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    if (m_bTrackedRequest)
    {
      // Track after only if tracked on the beginning
      _trackAfterHandleRequest (aRequestScope);
    }
  }
}
