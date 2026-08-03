/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.CheckForSigned;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.reflection.GenericReflection;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

/**
 * A {@link HttpServletRequestWrapper} that catches all {@link RuntimeException}s (mainly
 * {@link NullPointerException} and {@link IllegalStateException} of the type
 * <code>"The request object has been recycled and is no longer associated with this facade"</code>)
 * that can be thrown by certain Servlet containers (Tomcat, Jetty, ...) when a request object is
 * accessed after it has already been recycled by the container. This happens e.g. when a background
 * thread (like the long running request monitor) accesses a request after the original request
 * handling already finished.
 * <p>
 * Only {@link RuntimeException}s are caught. Methods that declare a checked exception (e.g.
 * {@link #getInputStream()}, {@link #getReader()}, {@link #setCharacterEncoding(String)}, the
 * <code>authenticate</code>/<code>login</code>/<code>logout</code> and multipart methods) are
 * overridden as well, but there only the (facade) {@link RuntimeException}s are caught - the
 * declared checked exceptions are propagated unchanged, so genuine I/O and Servlet errors are not
 * hidden.
 * <p>
 * This class supersedes the static <code>getRequestXXX</code> workaround methods in
 * {@link ServletHelper}. It shares the {@link ServletHelper#isLogExceptions()} switch to control
 * whether caught exceptions are logged.
 * <p>
 * Use the {@link #wrap(HttpServletRequest)} factory method to create instances - it avoids double
 * wrapping and gracefully handles <code>null</code>.
 *
 * @author Philip Helger
 * @since 11.4.3
 */
public class SafeHttpServletRequest extends HttpServletRequestWrapper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SafeHttpServletRequest.class);

  /** The wrapped request, typed as HttpServletRequest to avoid repeated casts. */
  private final HttpServletRequest m_aSrc;

  /**
   * Constructor. Consider using {@link #wrap(HttpServletRequest)} instead, to avoid double
   * wrapping.
   *
   * @param aHttpRequest
   *        The request to wrap. May not be <code>null</code>.
   */
  protected SafeHttpServletRequest (@NonNull final HttpServletRequest aHttpRequest)
  {
    super (aHttpRequest);
    ValueEnforcer.isFalse (() -> aHttpRequest instanceof SafeHttpServletRequest,
                           "The wrapped HttpServletRequest must nor be a Safe one");
    m_aSrc = aHttpRequest;
  }

  /**
   * Access the internally wrapped {@link HttpServletRequest}.
   */
  @Override
  @NonNull
  public HttpServletRequest getRequest ()
  {
    return m_aSrc;
  }

  private static void _warn (@NonNull final String sMsg, @NonNull final RuntimeException ex)
  {
    // Happens e.g. in certain Tomcat versions (10.1 with JDK 17):
    // "The request object has been recycled and is no longer associated with
    // this facade"
    if (ServletHelper.isLogExceptions ())
      LOGGER.warn ("[SafeHttpServletRequest] " + sMsg, ex);
  }

  @Nullable
  private String _getAttrAsString (@NonNull final String sAttrName)
  {
    // Uses the safe getAttribute below
    final Object aValue = getAttribute (sAttrName);
    return aValue instanceof final String s ? s : null;
  }

  // --- ServletRequest ---

  @Override
  @Nullable
  public Object getAttribute (final String sName)
  {
    try
    {
      return m_aSrc.getAttribute (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get attribute '" + sName + "' from HTTP request", ex);
      return null;
    }
  }

  @Nullable
  public <T> T getAttributeAs (final String sName)
  {
    return GenericReflection.uncheckedCast (getAttribute (sName));
  }

  @Override
  @NonNull
  public Enumeration <String> getAttributeNames ()
  {
    try
    {
      return m_aSrc.getAttributeNames ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get attribute names from HTTP request", ex);
      return Collections.emptyEnumeration ();
    }
  }

  @Override
  @Nullable
  public String getCharacterEncoding ()
  {
    try
    {
      return m_aSrc.getCharacterEncoding ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get character encoding from HTTP request", ex);
      return null;
    }
  }

  @Override
  @CheckForSigned
  public int getContentLength ()
  {
    try
    {
      return m_aSrc.getContentLength ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine Content-Length of HTTP request", ex);
      return -1;
    }
  }

  @Override
  @CheckForSigned
  public long getContentLengthLong ()
  {
    try
    {
      // Support > 2GB!!!
      return m_aSrc.getContentLengthLong ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine Content-Length of HTTP request", ex);
      return -1;
    }
  }

  @Override
  @Nullable
  public String getContentType ()
  {
    try
    {
      return m_aSrc.getContentType ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine Content-Type of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getParameter (final String sName)
  {
    try
    {
      return m_aSrc.getParameter (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get parameter '" + sName + "' from HTTP request", ex);
      return null;
    }
  }

  @Override
  @NonNull
  public Enumeration <String> getParameterNames ()
  {
    try
    {
      return m_aSrc.getParameterNames ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get parameter names from HTTP request", ex);
      return Collections.emptyEnumeration ();
    }
  }

  @Override
  @Nullable
  public String [] getParameterValues (final String sName)
  {
    try
    {
      return m_aSrc.getParameterValues (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get parameter values for '" + sName + "' from HTTP request", ex);
      return null;
    }
  }

  @Override
  @NonNull
  public Map <String, String []> getParameterMap ()
  {
    try
    {
      return m_aSrc.getParameterMap ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get parameter map from HTTP request", ex);
      return Collections.emptyMap ();
    }
  }

  @Override
  @Nullable
  public String getProtocol ()
  {
    try
    {
      return m_aSrc.getProtocol ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine Protocol of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getScheme ()
  {
    try
    {
      return m_aSrc.getScheme ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine Scheme of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getServerName ()
  {
    try
    {
      return m_aSrc.getServerName ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine ServerName of HTTP request", ex);
      return null;
    }
  }

  @Override
  @CheckForSigned
  public int getServerPort ()
  {
    try
    {
      return m_aSrc.getServerPort ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine ServerPort of HTTP request", ex);
      return -1;
    }
  }

  @Override
  @Nullable
  public String getRemoteAddr ()
  {
    try
    {
      return m_aSrc.getRemoteAddr ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine remote address of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getRemoteHost ()
  {
    try
    {
      return m_aSrc.getRemoteHost ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine remote host of HTTP request", ex);
      return null;
    }
  }

  @Override
  public void setAttribute (final String sName, final Object aValue)
  {
    try
    {
      m_aSrc.setAttribute (sName, aValue);
    }
    catch (final RuntimeException ex)
    {
      // Happens in certain Tomcat versions (e.g. 7.0.42 with JDK 8)
      _warn ("Failed to set attribute '" + sName + "' in HTTP request", ex);
    }
  }

  @Override
  public void removeAttribute (final String sName)
  {
    try
    {
      m_aSrc.removeAttribute (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to remove attribute '" + sName + "' from HTTP request", ex);
    }
  }

  @Override
  @NonNull
  public Locale getLocale ()
  {
    try
    {
      return m_aSrc.getLocale ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine locale of HTTP request", ex);
      return Locale.getDefault ();
    }
  }

  @Override
  @NonNull
  public Enumeration <Locale> getLocales ()
  {
    try
    {
      return m_aSrc.getLocales ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine locales of HTTP request", ex);
      return Collections.enumeration (Collections.singletonList (Locale.getDefault ()));
    }
  }

  @Override
  public boolean isSecure ()
  {
    try
    {
      return m_aSrc.isSecure ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine secure state of HTTP request", ex);
      return false;
    }
  }

  @Override
  @CheckForSigned
  public int getRemotePort ()
  {
    try
    {
      return m_aSrc.getRemotePort ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine remote port of HTTP request", ex);
      return -1;
    }
  }

  @Override
  @Nullable
  public String getLocalName ()
  {
    try
    {
      return m_aSrc.getLocalName ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine local name of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getLocalAddr ()
  {
    try
    {
      return m_aSrc.getLocalAddr ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine local address of HTTP request", ex);
      return null;
    }
  }

  @Override
  @CheckForSigned
  public int getLocalPort ()
  {
    try
    {
      return m_aSrc.getLocalPort ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine local port of HTTP request", ex);
      return -1;
    }
  }

  @Override
  @Nullable
  public ServletContext getServletContext ()
  {
    try
    {
      return m_aSrc.getServletContext ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine servlet context of HTTP request", ex);
      return null;
    }
  }

  @Override
  public boolean isAsyncStarted ()
  {
    try
    {
      return m_aSrc.isAsyncStarted ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine async started state of HTTP request", ex);
      return false;
    }
  }

  @Override
  public boolean isAsyncSupported ()
  {
    try
    {
      return m_aSrc.isAsyncSupported ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine async supported state of HTTP request", ex);
      return false;
    }
  }

  @Override
  @Nullable
  public AsyncContext getAsyncContext ()
  {
    try
    {
      return m_aSrc.getAsyncContext ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine async context of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public DispatcherType getDispatcherType ()
  {
    try
    {
      return m_aSrc.getDispatcherType ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine dispatcher type of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getRequestId ()
  {
    try
    {
      return m_aSrc.getRequestId ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine request ID of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getProtocolRequestId ()
  {
    try
    {
      return m_aSrc.getProtocolRequestId ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine protocol request ID of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public ServletConnection getServletConnection ()
  {
    try
    {
      return m_aSrc.getServletConnection ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine servlet connection of HTTP request", ex);
      return null;
    }
  }

  // --- HttpServletRequest ---

  @Override
  @Nullable
  public String getAuthType ()
  {
    try
    {
      return m_aSrc.getAuthType ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine auth type of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public Cookie [] getCookies ()
  {
    try
    {
      return m_aSrc.getCookies ();
    }
    catch (final RuntimeException ex)
    {
      // Happens e.g. on Tomcat 8.0.20 in Request.parseCookies
      _warn ("Failed to determine cookies of HTTP request", ex);
      return null;
    }
  }

  @Override
  @CheckForSigned
  public long getDateHeader (final String sName)
  {
    try
    {
      return m_aSrc.getDateHeader (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get Date header '" + sName + "' from HTTP request", ex);
      return -1;
    }
  }

  @Override
  @Nullable
  public String getHeader (final String sName)
  {
    try
    {
      return m_aSrc.getHeader (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get header '" + sName + "' from HTTP request", ex);
      return null;
    }
  }

  @Override
  @NonNull
  public Enumeration <String> getHeaders (final String sName)
  {
    try
    {
      return m_aSrc.getHeaders (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get headers for '" + sName + "' from HTTP request", ex);
      return Collections.emptyEnumeration ();
    }
  }

  @NonNull
  public ICommonsList <String> getHeadersAsList (final String sName)
  {
    return new CommonsArrayList <> (getHeaders (sName));
  }

  @Override
  @NonNull
  public Enumeration <String> getHeaderNames ()
  {
    try
    {
      return m_aSrc.getHeaderNames ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get header names from HTTP request", ex);
      return Collections.emptyEnumeration ();
    }
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <String> getHeaderNamesAsList ()
  {
    return new CommonsArrayList <> (getHeaderNames ());
  }

  @Override
  @CheckForSigned
  public int getIntHeader (final String sName)
  {
    try
    {
      return m_aSrc.getIntHeader (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get int header '" + sName + "' from HTTP request", ex);
      return -1;
    }
  }

  @Override
  @Nullable
  public HttpServletMapping getHttpServletMapping ()
  {
    try
    {
      return m_aSrc.getHttpServletMapping ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get servlet mapping of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getMethod ()
  {
    try
    {
      return m_aSrc.getMethod ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine Method of HTTP request", ex);
      return null;
    }
  }

  @Override
  @NonNull
  public String getPathInfo ()
  {
    String ret = null;
    try
    {
      // They may return null!
      if (m_aSrc.isAsyncSupported () && m_aSrc.isAsyncStarted ())
        ret = _getAttrAsString (AsyncContext.ASYNC_PATH_INFO);
      else
        ret = m_aSrc.getPathInfo ();
    }
    catch (final UnsupportedOperationException ex)
    {
      // Offline request - fall through
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine path info of HTTP request", ex);
    }
    return ret == null ? "" : ret;
  }

  @Override
  @Nullable
  public String getPathTranslated ()
  {
    try
    {
      return m_aSrc.getPathTranslated ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine path translated of HTTP request", ex);
      return null;
    }
  }

  @Override
  @NonNull
  public String getContextPath ()
  {
    return getContextPath (ServletContextPathHolder.getContextPath ());
  }

  @Nullable
  public String getContextPath (@Nullable final String sFallback)
  {
    String ret = null;
    try
    {
      if (m_aSrc.isAsyncSupported () && m_aSrc.isAsyncStarted ())
        ret = _getAttrAsString (AsyncContext.ASYNC_CONTEXT_PATH);
      else
        ret = m_aSrc.getContextPath ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine context path of HTTP request", ex);
    }
    if (ret == null)
    {
      // Fallback
      ret = sFallback;
    }
    return StringHelper.getNotNull (ret, "");
  }

  @Override
  @Nullable
  public String getQueryString ()
  {
    String ret = null;
    try
    {
      if (m_aSrc.isAsyncSupported () && m_aSrc.isAsyncStarted ())
        ret = _getAttrAsString (AsyncContext.ASYNC_QUERY_STRING);
      else
        ret = m_aSrc.getQueryString ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine query string of HTTP request", ex);
    }
    return ret;
  }

  @Override
  @Nullable
  public String getRemoteUser ()
  {
    try
    {
      return m_aSrc.getRemoteUser ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine remote user of HTTP request", ex);
      return null;
    }
  }

  @Override
  public boolean isUserInRole (final String sRole)
  {
    try
    {
      return m_aSrc.isUserInRole (sRole);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine role '" + sRole + "' of HTTP request", ex);
      return false;
    }
  }

  @Override
  @Nullable
  public Principal getUserPrincipal ()
  {
    try
    {
      return m_aSrc.getUserPrincipal ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine user principal of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public String getRequestedSessionId ()
  {
    try
    {
      return m_aSrc.getRequestedSessionId ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine requested session ID of HTTP request", ex);
      return null;
    }
  }

  @Override
  @NonNull
  public String getRequestURI ()
  {
    String ret = "";
    try
    {
      if (m_aSrc.isAsyncSupported () && m_aSrc.isAsyncStarted ())
        ret = _getAttrAsString (AsyncContext.ASYNC_REQUEST_URI);
      else
        ret = m_aSrc.getRequestURI ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine request URI of HTTP request", ex);
    }
    return ret == null ? "" : ret;
  }

  @Override
  @NonNull
  public StringBuffer getRequestURL ()
  {
    StringBuffer ret = null;
    try
    {
      ret = m_aSrc.getRequestURL ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine request URL of HTTP request", ex);
    }
    return ret != null ? ret : new StringBuffer ();
  }

  @Override
  @NonNull
  public String getServletPath ()
  {
    String ret = "";
    try
    {
      if (m_aSrc.isAsyncSupported () && m_aSrc.isAsyncStarted ())
        ret = _getAttrAsString (AsyncContext.ASYNC_SERVLET_PATH);
      else
        ret = m_aSrc.getServletPath ();
    }
    catch (final UnsupportedOperationException ex)
    {
      // Offline request - fall through
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine servlet path of HTTP request", ex);
    }
    return ret == null ? "" : ret;
  }

  @Override
  @Nullable
  public HttpSession getSession (final boolean bCreate)
  {
    try
    {
      return m_aSrc.getSession (bCreate);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get session (" + bCreate + ") of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public HttpSession getSession ()
  {
    return getSession (true);
  }

  @Override
  @Nullable
  public String changeSessionId ()
  {
    try
    {
      return m_aSrc.changeSessionId ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to change session ID of HTTP request", ex);
      return null;
    }
  }

  @Override
  public boolean isRequestedSessionIdValid ()
  {
    try
    {
      return m_aSrc.isRequestedSessionIdValid ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine requested session ID validity of HTTP request", ex);
      return false;
    }
  }

  @Override
  public boolean isRequestedSessionIdFromCookie ()
  {
    try
    {
      return m_aSrc.isRequestedSessionIdFromCookie ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine requested session ID from cookie of HTTP request", ex);
      return false;
    }
  }

  @Override
  public boolean isRequestedSessionIdFromURL ()
  {
    try
    {
      return m_aSrc.isRequestedSessionIdFromURL ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine requested session ID from URL of HTTP request", ex);
      return false;
    }
  }

  @Override
  @NonNull
  public Map <String, String> getTrailerFields ()
  {
    try
    {
      return m_aSrc.getTrailerFields ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine trailer fields of HTTP request", ex);
      return Collections.emptyMap ();
    }
  }

  @Override
  public boolean isTrailerFieldsReady ()
  {
    try
    {
      return m_aSrc.isTrailerFieldsReady ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to determine trailer fields ready state of HTTP request", ex);
      // Default per Servlet spec: no trailers -> ready
      return true;
    }
  }

  // --- Methods with checked exceptions ---
  // Only the (facade) RuntimeExceptions are caught here; the declared checked
  // exceptions are propagated unchanged.

  @Override
  @Nullable
  public ServletInputStream getInputStream () throws IOException
  {
    try
    {
      return m_aSrc.getInputStream ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get input stream of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public BufferedReader getReader () throws IOException
  {
    try
    {
      return m_aSrc.getReader ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get reader of HTTP request", ex);
      return null;
    }
  }

  @Override
  public void setCharacterEncoding (final String sEncoding)
  {
    try
    {
      m_aSrc.setCharacterEncoding (sEncoding);
    }
    catch (final UnsupportedEncodingException ex)
    {
      LOGGER.error ("Failed to set character encoding '" + sEncoding + "' of HTTP request", ex);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to set character encoding '" + sEncoding + "' of HTTP request", ex);
    }
  }

  public void setCharacterEncoding (@Nullable final Charset aCharset)
  {
    if (aCharset != null)
      setCharacterEncoding (aCharset.name ());
  }

  @Override
  public boolean authenticate (final HttpServletResponse aResponse) throws IOException, ServletException
  {
    try
    {
      return m_aSrc.authenticate (aResponse);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to authenticate HTTP request", ex);
      return false;
    }
  }

  @Override
  public void login (final String sUsername, final String sPassword) throws ServletException
  {
    try
    {
      m_aSrc.login (sUsername, sPassword);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to login user '" + sUsername + "' of HTTP request", ex);
    }
  }

  @Override
  public void logout () throws ServletException
  {
    try
    {
      m_aSrc.logout ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to logout of HTTP request", ex);
    }
  }

  @Override
  @NonNull
  public Collection <Part> getParts () throws IOException, ServletException
  {
    try
    {
      return m_aSrc.getParts ();
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get parts of HTTP request", ex);
      return Collections.emptyList ();
    }
  }

  @Override
  @Nullable
  public Part getPart (final String sName) throws IOException, ServletException
  {
    try
    {
      return m_aSrc.getPart (sName);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to get part '" + sName + "' of HTTP request", ex);
      return null;
    }
  }

  @Override
  @Nullable
  public <T extends HttpUpgradeHandler> T upgrade (final Class <T> aHandlerClass) throws IOException, ServletException
  {
    try
    {
      return m_aSrc.upgrade (aHandlerClass);
    }
    catch (final RuntimeException ex)
    {
      _warn ("Failed to upgrade HTTP request", ex);
      return null;
    }
  }

  /**
   * Wrap the provided request into a {@link SafeHttpServletRequest} - unless it already is one, in
   * which case the passed request is returned as-is. This is the preferred way to create instances,
   * as it avoids double wrapping.
   *
   * @param aHttpRequest
   *        The request to wrap. May be <code>null</code>.
   * @return <code>null</code> if the provided request is <code>null</code>, the provided request if
   *         it already is a {@link SafeHttpServletRequest}, or a new {@link SafeHttpServletRequest}
   *         wrapping the provided request otherwise.
   */
  @Nullable
  public static SafeHttpServletRequest wrap (@Nullable final HttpServletRequest aHttpRequest)
  {
    if (aHttpRequest == null)
      return null;
    if (aHttpRequest instanceof final SafeHttpServletRequest aSafeReq)
      return aSafeReq;
    return new SafeHttpServletRequest (aHttpRequest);
  }
}
