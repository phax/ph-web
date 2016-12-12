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
package com.helger.http.servlet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * Very basic servlet API helper
 *
 * @author Philip Helger
 * @since 8.6.3
 */
@Immutable
public final class ServletHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ServletHelper.class);

  @PresentForCodeCoverage
  private static final ServletHelper s_aInstance = new ServletHelper ();

  private ServletHelper ()
  {}

  /**
   * Safe version of <code>ServletRequest.setAttribute (String, Object)</code>
   * to work around an error in certain Tomcat versions.
   *
   * @param aRequest
   *        Servlet request. May not be <code>null</code>.
   * @param sAttrName
   *        Attribute name. May not be <code>null</code>.
   * @param aAttrValue
   *        Attribute value. May be <code>null</code>.
   */
  public static void setRequestAttribute (@Nonnull final ServletRequest aRequest,
                                          @Nonnull final String sAttrName,
                                          @Nullable final Object aAttrValue)
  {
    try
    {
      aRequest.setAttribute (sAttrName, aAttrValue);
    }
    catch (final Throwable t)
    {
      // Happens in certain Tomcat versions (e.g. 7.0.42 with JDK 8):
      /**
       * <pre>
      java.lang.NullPointerException
      1.: org.apache.catalina.connector.Request.notifyAttributeAssigned(Request.java:1493)
      2.: org.apache.catalina.connector.Request.setAttribute(Request.java:1483)
      3.: org.apache.catalina.connector.RequestFacade.setAttribute(RequestFacade.java:539)
       * </pre>
       */
      s_aLogger.warn ("Failed to set attribute '" + sAttrName + "' in HTTP request", t);
    }
  }

  /**
   * Work around an exception that can occur in Jetty 9.3.13:
   *
   * @param aRequest
   *        Source request
   * @return <code>null</code> if request is <code>null</code> or if no query
   *         string could be determined, or if none is present
   */
  @Nullable
  public static String getRequestQueryString (@Nullable final HttpServletRequest aRequest)
  {
    String ret = null;
    if (aRequest != null)
      try
      {
        ret = aRequest.getQueryString ();
      }
      catch (final Throwable t)
      {
        // fall through
        /**
         * <pre>
         * java.lang.NullPointerException: null
         * at org.eclipse.jetty.server.Request.getQueryString(Request.java:1119) ~[jetty-server-9.3.13.v20161014.jar:9.3.13.v20161014]
         * at com.helger.web.servlet.request.RequestHelper.getURL(RequestHelper.java:340) ~[ph-web-8.6.2.jar:8.6.2]
         * </pre>
         */
        s_aLogger.warn ("Failed to determine query string of HTTP request", t);
      }
    return ret;
  }

  /**
   * Work around an exception that can occur on Tomcat 8.0.20:
   *
   * @param aRequest
   *        Source request
   * @return Empty string if request is <code>null</code> or if no query string
   *         could be determined, or if none is present
   */
  @Nonnull
  public static String getRequestContextPath (@Nullable final HttpServletRequest aRequest)
  {
    String ret = "";
    if (aRequest != null)
      try
      {
        ret = aRequest.getContextPath ();
      }
      catch (final Throwable t)
      {
        // fall through
        /**
         * <pre>
         * java.lang.NullPointerException: null
        at org.apache.catalina.connector.Request.getServletContext(Request.java:1593) ~[catalina.jar:8.0.20]
        at org.apache.catalina.connector.Request.getContextPath(Request.java:1910) ~[catalina.jar:8.0.20]
        at org.apache.catalina.connector.RequestFacade.getContextPath(RequestFacade.java:783) ~[catalina.jar:8.0.20]
        at com.helger.web.servlet.request.RequestLogger.getRequestFieldMap(RequestLogger.java:81) ~[ph-web-8.6.3.jar:8.6.3]
         * </pre>
         */
        s_aLogger.warn ("Failed to determine query string of HTTP request", t);
      }
    return ret;
  }
}
