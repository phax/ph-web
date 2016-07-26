/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.useragent;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsHashSet;
import com.helger.commons.collection.ext.ICommonsSet;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.string.StringHelper;
import com.helger.http.CHTTPHeader;

/**
 * Central cache for known user agents (see HTTP header field
 * {@link CHTTPHeader#USER_AGENT}).
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class UserAgentDatabase
{
  private static final String REQUEST_ATTR = UserAgentDatabase.class.getName ();
  private static final Logger s_aLogger = LoggerFactory.getLogger (UserAgentDatabase.class);
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsSet <String> s_aUniqueUserAgents = new CommonsHashSet <> ();
  @GuardedBy ("s_aRWLock")
  private static Consumer <IUserAgent> s_aNewUserAgentCallback;

  @PresentForCodeCoverage
  private static final UserAgentDatabase s_aInstance = new UserAgentDatabase ();

  private UserAgentDatabase ()
  {}

  public static void setNewUserAgentCallback (@Nullable final Consumer <IUserAgent> aCallback)
  {
    s_aRWLock.writeLocked ( () -> s_aNewUserAgentCallback = aCallback);
  }

  /**
   * Get the user agent from the given request.
   *
   * @param aHttpRequest
   *        The HTTP request to get the UA from.
   * @return <code>null</code> if no user agent string is present
   */
  @Nullable
  public static String getHttpUserAgentStringFromRequest (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Use non-standard headers first
    String sUserAgent = aHttpRequest.getHeader (CHTTPHeader.UA);
    if (sUserAgent == null)
    {
      sUserAgent = aHttpRequest.getHeader (CHTTPHeader.X_DEVICE_USER_AGENT);
      if (sUserAgent == null)
        sUserAgent = aHttpRequest.getHeader (CHTTPHeader.USER_AGENT);
    }
    return sUserAgent;
  }

  @Nullable
  public static IUserAgent getParsedUserAgent (@Nullable final String sUserAgent)
  {
    if (StringHelper.hasNoText (sUserAgent))
      return null;

    // Decrypt outside the lock
    final IUserAgent aUserAgent = UserAgentDecryptor.decryptUserAgentString (sUserAgent);

    return s_aRWLock.writeLocked ( () -> {
      if (s_aUniqueUserAgents.add (sUserAgent))
        if (s_aNewUserAgentCallback != null)
          s_aNewUserAgentCallback.accept (aUserAgent);
      return aUserAgent;
    });
  }

  /**
   * Get the user agent object from the given HTTP request.
   *
   * @param aHttpRequest
   *        The HTTP request to extract the information from.
   * @return A non-<code>null</code> user agent object.
   */
  @Nonnull
  public static IUserAgent getUserAgent (@Nonnull final HttpServletRequest aHttpRequest)
  {
    IUserAgent aUserAgent = (IUserAgent) aHttpRequest.getAttribute (REQUEST_ATTR);
    if (aUserAgent == null)
    {
      // Extract HTTP header from request
      final String sUserAgent = getHttpUserAgentStringFromRequest (aHttpRequest);
      aUserAgent = getParsedUserAgent (sUserAgent);
      if (aUserAgent == null)
      {
        s_aLogger.warn ("No user agent was passed in the request!");
        aUserAgent = new UserAgent ("", new UserAgentElementList ());
      }
      aHttpRequest.setAttribute (REQUEST_ATTR, aUserAgent);
    }
    return aUserAgent;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsSet <String> getAllUniqueUserAgents ()
  {
    return s_aRWLock.readLocked ( () -> s_aUniqueUserAgents.getClone ());
  }
}
