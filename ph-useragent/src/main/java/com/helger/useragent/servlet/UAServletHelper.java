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
package com.helger.useragent.servlet;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.request.RequestHelper;
import com.helger.useragent.IUserAgent;
import com.helger.useragent.UserAgent;
import com.helger.useragent.UserAgentDatabase;
import com.helger.useragent.UserAgentElementList;
import com.helger.useragent.uaprofile.UAProfile;
import com.helger.useragent.uaprofile.UAProfileDatabase;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Helper class to retrieve user agent information from
 * {@link HttpServletRequest} with caching.
 *
 * @author Philip Helger
 * @since 10.3.0
 */
@Immutable
public final class UAServletHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UAServletHelper.class);

  private UAServletHelper ()
  {}

  /**
   * Get the user agent object from the given HTTP request.
   *
   * @param aHttpRequest
   *        The HTTP request to extract the information from.
   * @return A non-<code>null</code> user agent object or <code>null</code> in
   *         case of an internal inconsistency.
   */
  @Nullable
  public static IUserAgent getUserAgent (@NonNull final HttpServletRequest aHttpRequest)
  {
    final Object aAttr = ServletHelper.getRequestAttribute (aHttpRequest, IUserAgent.class.getName ());
    try
    {
      IUserAgent aUserAgent = (IUserAgent) aAttr;
      if (aUserAgent == null)
      {
        // Extract HTTP header from request
        final String sUserAgent = RequestHelper.getHttpUserAgentStringFromRequest (aHttpRequest);
        aUserAgent = UserAgentDatabase.getParsedUserAgent (sUserAgent);
        if (aUserAgent == null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("No user agent was passed in the request!");
          aUserAgent = new UserAgent ("", new UserAgentElementList ());
        }
        ServletHelper.setRequestAttribute (aHttpRequest, IUserAgent.class.getName (), aUserAgent);
      }
      return aUserAgent;
    }
    catch (final ClassCastException ex)
    {
      /**
       * Don't know why this happens:
       *
       * <pre>
       * Thread[144][ajp-nio-127.0.0.1-8009-exec-8][RUNNABLE][5][main]
      java.lang.ClassCastException: com.helger.useragent.UserAgent cannot be cast to com.helger.useragent.IUserAgent
      1.: com.helger.servlet.request.RequestHelper.getUserAgent(RequestHelper.java:1215)
      2.: com.helger.photon.core.interror.InternalErrorHandler.fillInternalErrorMetaData(InternalErrorHandler.java:354)
      3.: com.helger.photon.core.interror.InternalErrorHandler._notifyVendor(InternalErrorHandler.java:496)
       * </pre>
       */
      LOGGER.error ("ClassCastException whysoever.");
      if (aAttr != null)
        LOGGER.error ("  IUserAgent classloader=" + aAttr.getClass ().getClassLoader ().toString ());
      if (aAttr != null)
        LOGGER.error ("  UserAgent classloader=" + UserAgent.class.getClassLoader ().toString ());
      return null;
    }
  }

  /**
   * Get the user agent object from the given HTTP request.
   *
   * @param aHttpRequest
   *        The HTTP request to extract the information from.
   * @return A non-<code>null</code> user agent object.
   */
  @NonNull
  public static UAProfile getUAProfile (@NonNull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    UAProfile aUAProfile = ServletHelper.getRequestAttributeAs (aHttpRequest, UAProfile.class.getName ());
    if (aUAProfile == null)
    {
      // Extract HTTP header from request
      aUAProfile = UAProfileDatabase.getParsedUAProfile (new UAProfileHeaderProviderHttpServletRequest (aHttpRequest));
      ServletHelper.setRequestAttribute (aHttpRequest, UAProfile.class.getName (), aUAProfile);
    }
    return aUAProfile;
  }
}
