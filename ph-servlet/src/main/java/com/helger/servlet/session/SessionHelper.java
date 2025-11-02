/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.servlet.session;

import java.util.Enumeration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.reflection.GenericReflection;
import com.helger.base.state.EChange;
import com.helger.collection.base.EmptyEnumeration;
import com.helger.servlet.ServletHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * HTTP session utilities.
 *
 * @author Philip Helger
 */
@Immutable
public final class SessionHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SessionHelper.class);

  @PresentForCodeCoverage
  private static final SessionHelper INSTANCE = new SessionHelper ();

  private SessionHelper ()
  {}

  /**
   * Invalidate the session if the session is still active.
   *
   * @param aSession
   *        The session to be invalidated. May be <code>null</code>.
   * @return {@link EChange#CHANGED} if the session was invalidated,
   *         {@link EChange#UNCHANGED} otherwise.
   */
  @NonNull
  public static EChange safeInvalidateSession (@Nullable final HttpSession aSession)
  {
    if (aSession != null)
    {
      try
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Invalidating session " + aSession.getId ());
        aSession.invalidate ();
        return EChange.CHANGED;
      }
      catch (final IllegalStateException ex)
      {
        // session already invalidated
      }
    }
    return EChange.UNCHANGED;
  }

  /**
   * Get all attribute names present in the specified session.
   *
   * @param aSession
   *        The session to use. May not be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @NonNull
  public static Enumeration <String> getAllAttributes (@NonNull final HttpSession aSession)
  {
    ValueEnforcer.notNull (aSession, "Session");

    try
    {
      return GenericReflection.uncheckedCast (aSession.getAttributeNames ());
    }
    catch (final IllegalStateException ex)
    {
      // Session no longer valid
      return new EmptyEnumeration <> ();
    }
  }

  /**
   * Invalidate the session of the specified request (if any) and create a new
   * session.
   *
   * @param aHttpRequest
   *        The HTTP request to use. May not be <code>null</code>.
   * @return The new {@link HttpSession} to use. Never <code>null</code>.
   */
  @NonNull
  public static HttpSession safeRenewSession (@NonNull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    // Is there any existing session?
    final HttpSession aSession = ServletHelper.getRequestSession (aHttpRequest, false);
    if (aSession != null)
    {
      try
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Invalidating session " + aSession.getId () + " for renewal");
        aSession.invalidate ();
      }
      catch (final IllegalStateException ex)
      {
        // session already invalidated
      }
    }

    // Create the new session
    return ServletHelper.getRequestSession (aHttpRequest, true);
  }
}
