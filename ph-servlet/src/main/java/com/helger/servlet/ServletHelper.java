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
package com.helger.servlet;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.string.StringHelper;

/**
 * Very basic servlet API helper
 *
 * @author Philip Helger
 * @since 8.6.3
 */
@ThreadSafe
public final class ServletHelper
{
  public static final boolean DEFAULT_LOG_EXCEPTIONS = false;

  private static final Logger LOGGER = LoggerFactory.getLogger (ServletHelper.class);
  private static final AtomicBoolean s_aLogExceptions = new AtomicBoolean (DEFAULT_LOG_EXCEPTIONS);

  @PresentForCodeCoverage
  private static final ServletHelper s_aInstance = new ServletHelper ();

  private ServletHelper ()
  {}

  /**
   * Enable or disable the logging of caught exceptions. By default they are not
   * logged.
   *
   * @param bLog
   *        <code>true</code> to enable logging, <code>false</code> to disable
   *        logging.
   * @since 8.8.2
   */
  public static void setLogExceptions (final boolean bLog)
  {
    s_aLogExceptions.set (bLog);
  }

  /**
   * @return <code>true</code> to log exceptions, <code>false</code> to omit
   *         caught exceptions.
   * @since 8.8.2
   */
  public static boolean isLogExceptions ()
  {
    return s_aLogExceptions.get ();
  }

  /**
   * Safe version of <code>ServletRequest.setAttribute (String, Object)</code>
   * to work around an error in certain Tomcat versions.
   *
   * <pre>
  java.lang.NullPointerException
  1.: org.apache.catalina.connector.Request.notifyAttributeAssigned(Request.java:1493)
  2.: org.apache.catalina.connector.Request.setAttribute(Request.java:1483)
  3.: org.apache.catalina.connector.RequestFacade.setAttribute(RequestFacade.java:539)
   * </pre>
   *
   * @param aServletRequest
   *        Servlet request. May not be <code>null</code>.
   * @param sAttrName
   *        Attribute name. May not be <code>null</code>.
   * @param aAttrValue
   *        Attribute value. May be <code>null</code>.
   */
  public static void setRequestAttribute (@Nonnull final ServletRequest aServletRequest,
                                          @Nonnull final String sAttrName,
                                          @Nullable final Object aAttrValue)
  {
    try
    {
      aServletRequest.setAttribute (sAttrName, aAttrValue);
    }
    catch (final Exception ex)
    {
      // Happens in certain Tomcat versions (e.g. 7.0.42 with JDK 8):
      if (isLogExceptions ())
        if (LOGGER.isWarnEnabled ())
          LOGGER.warn ("[ServletHelper] Failed to set attribute '" + sAttrName + "' in HTTP request", ex);
    }
  }

  /**
   * Work around an exception that can occur on Tomcat 8.0.20:
   *
   * <pre>
  java.lang.NullPointerException: null
  at org.apache.catalina.connector.Request.getServletContext(Request.java:1593) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.Request.getContextPath(Request.java:1910) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.RequestFacade.getContextPath(RequestFacade.java:783) ~[catalina.jar:8.0.20]
  at com.helger.web.servlet.request.RequestLogger.getRequestFieldMap(RequestLogger.java:81) ~[ph-web-8.6.3.jar:8.6.3]
   * </pre>
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return Empty string if request is <code>null</code> or a String specifying
   *         the portion of the request URI that indicates the context of the
   *         request
   */
  @Nonnull
  public static String getRequestContextPath (@Nullable final HttpServletRequest aHttpRequest)
  {
    return getRequestContextPath (aHttpRequest, ServletContextPathHolder.getContextPath ());
  }

  /**
   * Work around an exception that can occur on Tomcat 8.0.20:
   *
   * <pre>
  java.lang.NullPointerException: null
  at org.apache.catalina.connector.Request.getServletContext(Request.java:1593) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.Request.getContextPath(Request.java:1910) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.RequestFacade.getContextPath(RequestFacade.java:783) ~[catalina.jar:8.0.20]
  at com.helger.web.servlet.request.RequestLogger.getRequestFieldMap(RequestLogger.java:81) ~[ph-web-8.6.3.jar:8.6.3]
   * </pre>
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @param sFallback
   *        Fallback context path to use, if none could be determined from the
   *        request. May be <code>null</code>.
   * @return Empty string if request is <code>null</code> or a String specifying
   *         the portion of the request URI that indicates the context of the
   *         request
   * @since 9.1.10
   */
  @Nonnull
  public static String getRequestContextPath (@Nullable final HttpServletRequest aHttpRequest, @Nullable final String sFallback)
  {
    String ret = null;
    if (aHttpRequest != null)
      try
      {
        if (aHttpRequest.isAsyncSupported () && aHttpRequest.isAsyncStarted ())
          ret = (String) aHttpRequest.getAttribute (AsyncContext.ASYNC_CONTEXT_PATH);
        else
          ret = aHttpRequest.getContextPath ();
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine context path of HTTP request", ex);
      }

    if (ret == null)
    {
      // Fallback
      ret = sFallback;
    }

    return StringHelper.getNotNull (ret, "");
  }

  /**
   * Get the path info of an request, supporting sync and async requests.
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return Empty string if request is <code>null</code> or a the path info.
   */
  @Nonnull
  public static String getRequestPathInfo (@Nullable final HttpServletRequest aHttpRequest)
  {
    String ret = null;
    if (aHttpRequest != null)
      try
      {
        // They may return null!
        if (aHttpRequest.isAsyncSupported () && aHttpRequest.isAsyncStarted ())
          ret = (String) aHttpRequest.getAttribute (AsyncContext.ASYNC_PATH_INFO);
        else
          ret = aHttpRequest.getPathInfo ();
      }
      catch (final UnsupportedOperationException ex)
      {
        // Offline request - fall through
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine path info of HTTP request", ex);
      }
    return ret == null ? "" : ret;
  }

  /**
   * Work around an exception that can occur in Jetty 9.3.13:
   *
   * <pre>
  java.lang.NullPointerException: null
  at org.eclipse.jetty.server.Request.getQueryString(Request.java:1119) ~[jetty-server-9.3.13.v20161014.jar:9.3.13.v20161014]
  at com.helger.web.servlet.request.RequestHelper.getURL(RequestHelper.java:340) ~[ph-web-8.6.2.jar:8.6.2]
   * </pre>
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return <code>null</code> if request is <code>null</code> or if no query
   *         string could be determined, or if none is present
   */
  @Nullable
  public static String getRequestQueryString (@Nullable final HttpServletRequest aHttpRequest)
  {
    String ret = null;
    if (aHttpRequest != null)
      try
      {
        if (aHttpRequest.isAsyncSupported () && aHttpRequest.isAsyncStarted ())
          ret = (String) aHttpRequest.getAttribute (AsyncContext.ASYNC_QUERY_STRING);
        else
          ret = aHttpRequest.getQueryString ();
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine query string of HTTP request", ex);
      }
    return ret;
  }

  /**
   * Get the request URI of an request, supporting sync and async requests.
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return Empty string if request is <code>null</code> or the request URI.
   */
  @Nonnull
  public static String getRequestRequestURI (@Nullable final HttpServletRequest aHttpRequest)
  {
    String ret = "";
    if (aHttpRequest != null)
      try
      {
        if (aHttpRequest.isAsyncSupported () && aHttpRequest.isAsyncStarted ())
          ret = (String) aHttpRequest.getAttribute (AsyncContext.ASYNC_REQUEST_URI);
        else
          ret = aHttpRequest.getRequestURI ();
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine request URI of HTTP request", ex);
      }
    return ret;
  }

  /**
   * Get the request URL of an request, supporting sync and async requests.
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return Empty {@link StringBuffer} if request is <code>null</code> or the
   *         request URL.
   */
  @Nonnull
  public static StringBuffer getRequestRequestURL (@Nullable final HttpServletRequest aHttpRequest)
  {
    StringBuffer ret = null;
    if (aHttpRequest != null)
      try
      {
        ret = aHttpRequest.getRequestURL ();
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine request URL of HTTP request", ex);
      }
    return ret != null ? ret : new StringBuffer ();
  }

  /**
   * Get the servlet path of an request, supporting sync and async requests.
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return Empty string if request is <code>null</code> or the servlet path.
   * @since 8.8.0
   */
  @Nonnull
  public static String getRequestServletPath (@Nullable final HttpServletRequest aHttpRequest)
  {
    String ret = "";
    if (aHttpRequest != null)
      try
      {
        if (aHttpRequest.isAsyncSupported () && aHttpRequest.isAsyncStarted ())
          ret = (String) aHttpRequest.getAttribute (AsyncContext.ASYNC_SERVLET_PATH);
        else
          ret = aHttpRequest.getServletPath ();
      }
      catch (final UnsupportedOperationException ex)
      {
        // Offline request - fall through
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine servlet path of HTTP request", ex);
      }
    return ret;
  }

  /**
   * Work around an exception that can occur on Tomcat 8.0.20:
   *
   * <pre>
  java.lang.NullPointerException: null
  at org.apache.catalina.connector.Request.parseCookies(Request.java:2943) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.Request.convertCookies(Request.java:2958) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.Request.getCookies(Request.java:1987) ~[catalina.jar:8.0.20]
  at org.apache.catalina.connector.RequestFacade.getCookies(RequestFacade.java:662) ~[catalina.jar:8.0.20]
   * </pre>
   *
   * @param aHttpRequest
   *        Source request. May be <code>null</code>.
   * @return getRequestCookies
   */
  @Nullable
  public static Cookie [] getRequestCookies (@Nullable final HttpServletRequest aHttpRequest)
  {
    Cookie [] ret = null;
    if (aHttpRequest != null)
      try
      {
        ret = aHttpRequest.getCookies ();
      }
      catch (final Exception ex)
      {
        // fall through
        if (isLogExceptions ())
          if (LOGGER.isWarnEnabled ())
            LOGGER.warn ("[ServletHelper] Failed to determine cookies of HTTP request", ex);
      }
    return ret;
  }

  /**
   * Safely set the request character encoding.
   *
   * @param aHttpRequest
   *        Http request to change. May not be <code>null</code>.
   * @param aCharset
   *        Character set to use. May be <code>null</code>.
   * @since 9.1.9
   */
  public static void setRequestCharacterEncoding (@Nonnull final HttpServletRequest aHttpRequest, @Nullable final Charset aCharset)
  {
    setRequestCharacterEncoding (aHttpRequest, aCharset != null ? aCharset.name () : null);
  }

  /**
   * Safely set the request character encoding.
   *
   * @param aHttpRequest
   *        Http request to change. May not be <code>null</code>.
   * @param sCharset
   *        Character set to use. May be <code>null</code>.
   * @since 9.1.9
   */
  public static void setRequestCharacterEncoding (@Nonnull final HttpServletRequest aHttpRequest, @Nullable final String sCharset)
  {
    if (StringHelper.hasText (sCharset))
      try
      {
        aHttpRequest.setCharacterEncoding (sCharset);
      }
      catch (final UnsupportedEncodingException ex)
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Failed to set request character encoding to '" + sCharset + "'", ex);
      }
  }

  /**
   * Get the servlet context base path (for ".")
   *
   * @param aSC
   *        Servlet context. May not be <code>null</code>.
   * @return The non-<code>null</code> base path.
   */
  @Nonnull
  public static String getServletContextBasePath (@Nonnull final ServletContext aSC)
  {
    String sPath = aSC.getRealPath (".");
    if (sPath == null)
    {
      // Fallback for Undertow
      sPath = aSC.getRealPath ("");
    }
    if (StringHelper.hasNoText (sPath))
    {
      // This is e.g. the case if "Unpack WAR files" in Tomcat is disabled
      throw new IllegalStateException ("Failed to determine real path of ServletContext " + aSC);
    }
    return sPath;
  }
}
