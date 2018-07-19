/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.servlet.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.collection.multimap.MultiHashMapLinkedHashSetBased;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.UnsupportedOperation;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.IteratorHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.CommonsHashSet;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsCollection;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.locale.IHasLocale;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.string.StringHelper;
import com.helger.commons.system.SystemHelper;
import com.helger.commons.url.URLHelper;
import com.helger.commons.url.URLParameter;
import com.helger.commons.url.URLParameterList;
import com.helger.http.AcceptCharsetHandler;
import com.helger.http.EHttpVersion;
import com.helger.network.port.SchemeDefaultPortMapper;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.spec.IServletRequest300To310Migration;

/**
 * Mock implementation of {@link HttpServletRequest}.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class MockHttpServletRequest implements HttpServletRequest, IHasLocale, IServletRequest300To310Migration
{
  public static final boolean DEFAULT_INVOKE_HTTP_LISTENER = true;
  public static final String DEFAULT_PROTOCOL = EHttpVersion.HTTP_11.getName ();
  public static final String DEFAULT_SCHEME = SchemeDefaultPortMapper.SCHEME_HTTP;
  public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";
  public static final String DEFAULT_SERVER_NAME = "localhost";
  public static final int DEFAULT_SERVER_PORT = SchemeDefaultPortMapper.getDefaultPortOrThrow (DEFAULT_SCHEME);
  public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";
  public static final String DEFAULT_REMOTE_HOST = "localhost";
  /** The default HTTP method: GET */
  public static final EHttpMethod DEFAULT_METHOD = EHttpMethod.GET;
  private static final Logger LOGGER = LoggerFactory.getLogger (MockHttpServletRequest.class);

  private boolean m_bInvalidated = false;
  private boolean m_bActive = true;
  private final ICommonsMap <String, Object> m_aAttributes = new CommonsHashMap <> ();
  private Charset m_aCharacterEncoding;
  private byte [] m_aContent;
  private String m_sContentType;
  private final URLParameterList m_aParameters = new URLParameterList ();
  private String m_sProtocol = DEFAULT_PROTOCOL;
  private String m_sScheme = DEFAULT_SCHEME;
  private String m_sServerName = DEFAULT_SERVER_NAME;
  private int m_nServerPort = DEFAULT_SERVER_PORT;
  private final ICommonsList <Locale> m_aLocales = new CommonsArrayList <> ();
  private boolean m_bSecure = false;
  private final ServletContext m_aServletContext;
  private String m_sRemoteAddr = DEFAULT_REMOTE_ADDR;
  private String m_sRemoteHost = DEFAULT_REMOTE_HOST;
  private int m_nRemotePort = DEFAULT_SERVER_PORT;
  private String m_sLocalName = DEFAULT_SERVER_NAME;
  private String m_sLocalAddr = DEFAULT_SERVER_ADDR;
  private int m_nLocalPort = DEFAULT_SERVER_PORT;
  private String m_sAuthType;
  private Cookie [] m_aCookies;
  private final MultiHashMapLinkedHashSetBased <String, String> m_aHeaders = new MultiHashMapLinkedHashSetBased <> ();
  private EHttpMethod m_eMethod;
  private String m_sPathInfo;
  private String m_sContextPath = "";
  private String m_sQueryString;
  private String m_sRemoteUser;
  private final ICommonsSet <String> m_aUserRoles = new CommonsHashSet <> ();
  private Principal m_aUserPrincipal;
  private String m_sRequestURI;
  private String m_sServletPath = "";
  private HttpSession m_aSession;
  private String m_sSessionID;
  private boolean m_bRequestedSessionIDValid = true;
  private boolean m_bRequestedSessionIDFromCookie = true;
  private boolean m_bRequestedSessionIDFromURL = false;

  /**
   * Create a new MockHttpServletRequest with a default
   * {@link MockServletContext}.
   *
   * @see MockServletContext
   */
  public MockHttpServletRequest ()
  {
    // No servlet context present -> no listeners
    this (null, DEFAULT_METHOD, false);
  }

  /**
   * Create a new MockHttpServletRequest.
   *
   * @param aServletContext
   *        the ServletContext that the request runs in (may be
   *        <code>null</code> to use a default MockServletContext)
   * @see MockServletContext
   */
  public MockHttpServletRequest (@Nullable final ServletContext aServletContext)
  {
    this (aServletContext, DEFAULT_METHOD, DEFAULT_INVOKE_HTTP_LISTENER);
  }

  /**
   * Create a new MockHttpServletRequest.
   *
   * @param aServletContext
   *        the ServletContext that the request runs in (may be
   *        <code>null</code> to use a default MockServletContext)
   * @param eMethod
   *        the request method (may be <code>null</code>)
   * @see MockServletContext
   */
  public MockHttpServletRequest (@Nullable final ServletContext aServletContext, @Nullable final EHttpMethod eMethod)
  {
    this (aServletContext, eMethod, DEFAULT_INVOKE_HTTP_LISTENER);
  }

  /**
   * Create a new MockHttpServletRequest.
   *
   * @param aServletContext
   *        the ServletContext that the request runs in (may be
   *        <code>null</code> to use a default MockServletContext)
   * @param eMethod
   *        the request method (may be <code>null</code>)
   * @param bInvokeHttpListeners
   *        if <code>true</code> than the HTTP request event listeners from
   *        {@link MockHttpListener} are triggered
   * @see #setMethod
   * @see MockServletContext
   */
  public MockHttpServletRequest (@Nullable final ServletContext aServletContext,
                                 @Nullable final EHttpMethod eMethod,
                                 final boolean bInvokeHttpListeners)
  {
    m_aServletContext = aServletContext;
    setMethod (eMethod);
    m_aLocales.add (Locale.ENGLISH);

    // Add default HTTP header
    addHeader (CHttpHeader.USER_AGENT, getClass ().getName ());
    // Disable GZip and Deflate!
    addHeader (CHttpHeader.ACCEPT_ENCODING, "*, gzip;q=0, x-gzip;q=0, deflate;q=0, compress;q=0, x-compress;q=0");
    addHeader (CHttpHeader.ACCEPT_CHARSET, AcceptCharsetHandler.ANY_CHARSET);

    if (aServletContext != null && bInvokeHttpListeners)
    {
      // Invoke all HTTP event listener
      final ServletRequestEvent aSRE = new ServletRequestEvent (aServletContext, this);
      for (final ServletRequestListener aListener : MockHttpListener.getAllServletRequestListeners ())
        aListener.requestInitialized (aSRE);
    }
  }

  /**
   * @return the ServletContext that this request is associated with. (Not
   *         available in the standard HttpServletRequest interface for some
   *         reason.). Never <code>null</code>.
   */
  @Nonnull
  public final ServletContext getServletContext ()
  {
    if (m_aServletContext == null)
      throw new IllegalStateException ("No servlet context present!");
    return m_aServletContext;
  }

  /**
   * @return whether this request is still active (that is, not completed yet).
   */
  public boolean isActive ()
  {
    return m_bActive;
  }

  /**
   * Mark this request as completed, keeping its state.
   */
  public void close ()
  {
    m_bActive = false;
  }

  /**
   * Invalidate this request, clearing its state and invoking all HTTP event
   * listener.
   *
   * @see #close()
   * @see #clearAttributes()
   */
  public void invalidate ()
  {
    if (m_bInvalidated)
      throw new IllegalStateException ("Request scope already invalidated!");
    m_bInvalidated = true;

    if (m_aServletContext != null)
    {
      final ServletRequestEvent aSRE = new ServletRequestEvent (m_aServletContext, this);
      for (final ServletRequestListener aListener : MockHttpListener.getAllServletRequestListeners ())
        aListener.requestDestroyed (aSRE);
    }

    close ();
    clearAttributes ();
  }

  /**
   * Check whether this request is still active (that is, not completed yet),
   * throwing an IllegalStateException if not active anymore.
   */
  protected void checkActive ()
  {
    if (!m_bActive)
      throw new IllegalStateException ("Request is not active anymore");
  }

  @Nullable
  public Object getAttribute (@Nullable final String sName)
  {
    checkActive ();
    return m_aAttributes.get (sName);
  }

  @Nonnull
  public Enumeration <String> getAttributeNames ()
  {
    checkActive ();
    return IteratorHelper.getEnumeration (m_aAttributes.keySet ());
  }

  public void setCharacterEncoding (@Nullable final String sCharacterEncoding)
  {
    setCharacterEncoding (sCharacterEncoding == null ? null : CharsetHelper.getCharsetFromName (sCharacterEncoding));
  }

  public void setCharacterEncoding (@Nullable final Charset aCharacterEncoding)
  {
    m_aCharacterEncoding = aCharacterEncoding;
  }

  @Nullable
  public String getCharacterEncoding ()
  {
    return m_aCharacterEncoding == null ? null : m_aCharacterEncoding.name ();
  }

  @Nullable
  public Charset getCharacterEncodingObj ()
  {
    return m_aCharacterEncoding;
  }

  @Nonnull
  public Charset getCharacterEncodingObjOrDefault ()
  {
    Charset ret = getCharacterEncodingObj ();
    if (ret == null)
      ret = SystemHelper.getSystemCharset ();
    return ret;
  }

  @Nonnull
  public MockHttpServletRequest setContent (@Nullable final byte [] aContent)
  {
    m_aContent = ArrayHelper.getCopy (aContent);
    removeHeader (CHttpHeader.CONTENT_LENGTH);
    addHeader (CHttpHeader.CONTENT_LENGTH, Integer.toString (m_aContent.length));
    return this;
  }

  @CheckForSigned
  public int getContentLength ()
  {
    return m_aContent != null ? m_aContent.length : -1;
  }

  @Nonnull
  public MockHttpServletRequest setContentType (@Nullable final IMimeType aContentType)
  {
    return setContentType (aContentType == null ? null : aContentType.getAsString ());
  }

  @Nonnull
  public MockHttpServletRequest setContentType (@Nullable final String sContentType)
  {
    m_sContentType = sContentType;
    removeHeader (CHttpHeader.CONTENT_TYPE);
    if (sContentType != null)
      addHeader (CHttpHeader.CONTENT_TYPE, sContentType);
    return this;
  }

  @Nullable
  public String getContentType ()
  {
    return m_sContentType;
  }

  /**
   * Note: do not change the content via {@link #setContent(byte[])}, while an
   * input stream is open, because this may lead to indeterministic results!
   *
   * @return <code>null</code> if no content is present. If non-
   *         <code>null</code> the caller is responsible for closing the
   *         {@link InputStream}.
   */
  @Nullable
  public ServletInputStream getInputStream ()
  {
    if (m_aContent == null)
      return null;

    return new MockServletInputStream (m_aContent);
  }

  /**
   * Set a single value for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, they will be replaced.
   *
   * @param sName
   *        Parameter name
   * @param sValue
   *        Parameter value
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest setParameter (@Nonnull final String sName, @Nullable final String sValue)
  {
    m_aParameters.remove (sName);
    m_aParameters.add (sName, sValue);
    return this;
  }

  /**
   * Set an array of values for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, they will be replaced.
   *
   * @param sName
   *        Parameter name
   * @param aValues
   *        Parameter values
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest setParameter (@Nonnull final String sName, @Nullable final String [] aValues)
  {
    m_aParameters.remove (sName);
    m_aParameters.addAll (sName, aValues);
    return this;
  }

  /**
   * Sets all provided parameters <em>replacing</em> any existing values for the
   * provided parameter names. To add without replacing existing values, use
   * {@link #addParameters(List)}.
   *
   * @param aParams
   *        Parameter name value map. May be <code>null</code>.
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest setParameters (@Nullable final List <? extends URLParameter> aParams)
  {
    if (aParams != null)
    {
      for (final URLParameter aParam : aParams)
        m_aParameters.remove (aParam.getName ());
      m_aParameters.addAll (aParams);
    }
    return this;
  }

  /**
   * Add a single value for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, the given value will be added to the end of the list.
   *
   * @param sName
   *        Parameter name
   * @param sValue
   *        Parameter value
   * @return this
   */
  @Nonnull
  public final MockHttpServletRequest addParameter (@Nonnull final String sName, @Nullable final String sValue)
  {
    if (sValue != null)
      m_aParameters.add (sName, sValue);
    else
      m_aParameters.add (sName);
    return this;
  }

  /**
   * Add an array of values for the specified HTTP parameter.
   * <p>
   * If there are already one or more values registered for the given parameter
   * name, the given values will be added to the end of the list.
   *
   * @param sName
   *        Parameter name
   * @param aValues
   *        Parameter values
   * @return this
   */
  @Nonnull
  public final MockHttpServletRequest addParameter (@Nonnull final String sName, @Nullable final String [] aValues)
  {
    m_aParameters.addAll (sName, aValues);
    return this;
  }

  /**
   * Adds all provided parameters <em>without</em> replacing any existing
   * values. To replace existing values, use {@link #setParameters(List)}.
   *
   * @param aParams
   *        Parameter name value map
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest addParameters (@Nullable final List <? extends URLParameter> aParams)
  {
    m_aParameters.addAll (aParams);
    return this;
  }

  /**
   * Remove already registered values for the specified HTTP parameter, if any.
   *
   * @param sName
   *        Parameter name
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest removeParameter (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");
    m_aParameters.remove (sName);
    return this;
  }

  /**
   * Removes all existing parameters.
   *
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest removeAllParameters ()
  {
    m_aParameters.clear ();
    return this;
  }

  @Nullable
  public String getParameter (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");

    return m_aParameters.getFirstParamValue (sName);
  }

  @Nonnull
  public Enumeration <String> getParameterNames ()
  {
    return IteratorHelper.getEnumeration (m_aParameters.getAllParamNames ());
  }

  @Nullable
  @ReturnsMutableCopy
  public String [] getParameterValues (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");
    return m_aParameters.getAllParamValues (sName).toArray (new String [0]);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsOrderedMap <String, String []> getParameterMap ()
  {
    final ICommonsOrderedMap <String, String []> ret = new CommonsLinkedHashMap <> ();
    for (final String sParamName : m_aParameters.getAllParamNames ())
      ret.put (sParamName, getParameterValues (sParamName));
    return ret;
  }

  @Nonnull
  public MockHttpServletRequest setProtocol (@Nullable final String sProtocol)
  {
    m_sProtocol = sProtocol;
    return this;
  }

  @Nullable
  public String getProtocol ()
  {
    return m_sProtocol;
  }

  @Nonnull
  public MockHttpServletRequest setScheme (@Nullable final String sScheme)
  {
    m_sScheme = sScheme;
    return this;
  }

  @Nullable
  public String getScheme ()
  {
    return m_sScheme;
  }

  @Nonnull
  public MockHttpServletRequest setServerName (@Nullable final String sServerName)
  {
    m_sServerName = sServerName;
    return this;
  }

  @Nullable
  public String getServerName ()
  {
    return m_sServerName;
  }

  @Nonnull
  public MockHttpServletRequest setServerPort (final int nServerPort)
  {
    m_nServerPort = nServerPort;
    return this;
  }

  public int getServerPort ()
  {
    return m_nServerPort;
  }

  @Nullable
  public BufferedReader getReader ()
  {
    if (m_aContent == null)
      return null;

    final InputStream aIS = new NonBlockingByteArrayInputStream (m_aContent);
    final Reader aReader = StreamHelper.createReader (aIS, getCharacterEncodingObjOrDefault ());
    return new BufferedReader (aReader);
  }

  @Nonnull
  public MockHttpServletRequest setRemoteAddr (@Nullable final String sRemoteAddr)
  {
    m_sRemoteAddr = sRemoteAddr;
    return this;
  }

  @Nullable
  public String getRemoteAddr ()
  {
    return m_sRemoteAddr;
  }

  @Nonnull
  public MockHttpServletRequest setRemoteHost (@Nullable final String sRemoteHost)
  {
    m_sRemoteHost = sRemoteHost;
    return this;
  }

  @Nullable
  public String getRemoteHost ()
  {
    return m_sRemoteHost;
  }

  public void setAttribute (@Nonnull final String sName, @Nullable final Object aValue)
  {
    checkActive ();
    ValueEnforcer.notNull (sName, "Name");

    if (aValue != null)
      m_aAttributes.put (sName, aValue);
    else
      m_aAttributes.remove (sName);
  }

  public void removeAttribute (@Nonnull final String sName)
  {
    ValueEnforcer.notNull (sName, "Name");

    checkActive ();
    m_aAttributes.remove (sName);
  }

  /**
   * Clear all of this request's attributes.
   *
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest clearAttributes ()
  {
    m_aAttributes.clear ();
    return this;
  }

  /**
   * Add a new preferred locale, before any existing locales.
   *
   * @param aLocale
   *        preferred locale
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest addPreferredLocale (@Nonnull final Locale aLocale)
  {
    ValueEnforcer.notNull (aLocale, "Locale");
    m_aLocales.add (0, aLocale);
    return this;
  }

  @Nonnull
  public Locale getLocale ()
  {
    // One element is added in ctor!
    return m_aLocales.getFirst ();
  }

  @Nonnull
  @Nonempty
  public Enumeration <Locale> getLocales ()
  {
    return IteratorHelper.getEnumeration (m_aLocales);
  }

  @Nonnull
  public MockHttpServletRequest setSecure (final boolean bSecure)
  {
    m_bSecure = bSecure;
    return this;
  }

  public boolean isSecure ()
  {
    return m_bSecure;
  }

  @Nonnull
  public MockRequestDispatcher getRequestDispatcher (@Nonnull final String sPath)
  {
    return new MockRequestDispatcher (sPath);
  }

  @Deprecated
  public String getRealPath (@Nonnull final String sPath)
  {
    return getServletContext ().getRealPath (sPath);
  }

  @Nonnull
  public MockHttpServletRequest setRemotePort (final int nRemotePort)
  {
    m_nRemotePort = nRemotePort;
    return this;
  }

  public int getRemotePort ()
  {
    return m_nRemotePort;
  }

  @Nonnull
  public MockHttpServletRequest setLocalName (@Nullable final String sLocalName)
  {
    m_sLocalName = sLocalName;
    return this;
  }

  @Nullable
  public String getLocalName ()
  {
    return m_sLocalName;
  }

  @Nonnull
  public MockHttpServletRequest setLocalAddr (@Nullable final String sLocalAddr)
  {
    m_sLocalAddr = sLocalAddr;
    return this;
  }

  @Nullable
  public String getLocalAddr ()
  {
    return m_sLocalAddr;
  }

  @Nonnull
  public MockHttpServletRequest setLocalPort (final int nLocalPort)
  {
    m_nLocalPort = nLocalPort;
    return this;
  }

  public int getLocalPort ()
  {
    return m_nLocalPort;
  }

  @Nonnull
  public MockHttpServletRequest setAuthType (@Nullable final String sAuthType)
  {
    m_sAuthType = sAuthType;
    return this;
  }

  @Nullable
  public String getAuthType ()
  {
    return m_sAuthType;
  }

  @Nonnull
  public MockHttpServletRequest setCookies (@Nullable final Cookie [] aCookies)
  {
    m_aCookies = ArrayHelper.getCopy (aCookies);
    return this;
  }

  @Nullable
  public Cookie [] getCookies ()
  {
    return ArrayHelper.getCopy (m_aCookies);
  }

  @Nullable
  private static String _getUnifiedHeaderName (@Nullable final String s)
  {
    return s == null ? null : s.toLowerCase (Locale.US);
  }

  /**
   * Add a header entry for the given name.
   * <p>
   * If there was no entry for that header name before, the value will be used
   * as-is. In case of an existing entry, a String array will be created, adding
   * the given value (more specifically, its toString representation) as further
   * element.
   * <p>
   * Multiple values can only be stored as list of Strings, following the
   * Servlet spec (see <code>getHeaders</code> accessor). As alternative to
   * repeated <code>addHeader</code> calls for individual elements, you can use
   * a single call with an entire array or Collection of values as parameter.
   *
   * @param sName
   *        header name
   * @param aValue
   *        header value
   * @return this
   * @see #getHeaderNames
   * @see #getHeader
   * @see #getHeaders
   * @see #getDateHeader
   * @see #getIntHeader
   */
  @Nonnull
  public final MockHttpServletRequest addHeader (@Nullable final String sName, @Nullable final String aValue)
  {
    m_aHeaders.putSingle (_getUnifiedHeaderName (sName), aValue);
    return this;
  }

  @Nonnull
  public MockHttpServletRequest removeHeader (@Nullable final String sName)
  {
    m_aHeaders.remove (_getUnifiedHeaderName (sName));
    return this;
  }

  @UnsupportedOperation
  @Deprecated
  public long getDateHeader (@Nullable final String sName)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  @Deprecated
  public int getIntHeader (@Nullable final String sName)
  {
    throw new UnsupportedOperationException ();
  }

  @Nullable
  public String getHeader (@Nullable final String sName)
  {
    final ICommonsSet <String> aValue = m_aHeaders.get (_getUnifiedHeaderName (sName));
    return aValue == null || aValue.isEmpty () ? null : String.valueOf (aValue.iterator ().next ());
  }

  @Nonnull
  public Enumeration <String> getHeaders (@Nullable final String sName)
  {
    final ICommonsSet <String> vals = m_aHeaders.get (_getUnifiedHeaderName (sName));
    return IteratorHelper.getEnumeration (vals);
  }

  @Nonnull
  public Enumeration <String> getHeaderNames ()
  {
    return IteratorHelper.getEnumeration (m_aHeaders.keySet ());
  }

  @Nonnull
  public final MockHttpServletRequest setMethod (@Nullable final EHttpMethod eMethod)
  {
    m_eMethod = eMethod;
    return this;
  }

  @Nullable
  public EHttpMethod getMethodEnum ()
  {
    return m_eMethod;
  }

  @Nullable
  public String getMethod ()
  {
    return m_eMethod == null ? null : m_eMethod.getName ();
  }

  @Nonnull
  public MockHttpServletRequest setPathInfo (@Nullable final String sPathInfo)
  {
    m_sPathInfo = sPathInfo;
    return this;
  }

  @Nullable
  public String getPathInfo ()
  {
    return m_sPathInfo;
  }

  @Nullable
  public String getPathTranslated ()
  {
    return m_sPathInfo != null ? getRealPath (m_sPathInfo) : null;
  }

  @Nonnull
  public MockHttpServletRequest setContextPath (@Nullable final String sContextPath)
  {
    if (StringHelper.hasText (sContextPath) && !StringHelper.startsWith (sContextPath, '/'))
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("Illegal context path specified: '" + sContextPath + "'");
    m_sContextPath = sContextPath;
    return this;
  }

  @Nullable
  public String getContextPath ()
  {
    return m_sContextPath;
  }

  @Nonnull
  public MockHttpServletRequest setQueryString (@Nullable final String sQueryString)
  {
    m_sQueryString = sQueryString;
    return this;
  }

  @Nullable
  public String getQueryString ()
  {
    return m_sQueryString;
  }

  @Nonnull
  public MockHttpServletRequest setRemoteUser (@Nullable final String sRemoteUser)
  {
    m_sRemoteUser = sRemoteUser;
    return this;
  }

  @Nullable
  public String getRemoteUser ()
  {
    return m_sRemoteUser;
  }

  @Nonnull
  public MockHttpServletRequest addUserRole (@Nullable final String sRole)
  {
    m_aUserRoles.add (sRole);
    return this;
  }

  public boolean isUserInRole (@Nullable final String sRole)
  {
    return m_aUserRoles.contains (sRole);
  }

  @Nonnull
  public MockHttpServletRequest setUserPrincipal (@Nullable final Principal aUserPrincipal)
  {
    m_aUserPrincipal = aUserPrincipal;
    return this;
  }

  @Nullable
  public Principal getUserPrincipal ()
  {
    return m_aUserPrincipal;
  }

  @Nullable
  public String getRequestedSessionId ()
  {
    return getSession (true).getId ();
  }

  @Nonnull
  public MockHttpServletRequest setRequestURI (@Nullable final String sRequestURI)
  {
    m_sRequestURI = sRequestURI;
    return this;
  }

  @Nullable
  public String getRequestURI ()
  {
    return m_sRequestURI;
  }

  @Nonnull
  public StringBuffer getRequestURL ()
  {
    return new StringBuffer ().append (RequestHelper.getFullServerName (m_sScheme, m_sServerName, m_nServerPort))
                              .append (ServletHelper.getRequestRequestURI (this));
  }

  @Nonnull
  public MockHttpServletRequest setServletPath (@Nullable final String sServletPath)
  {
    if (StringHelper.hasText (sServletPath) && !StringHelper.startsWith (sServletPath, '/'))
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("ServletPath must be empty or start with a slash: '" + sServletPath + "'");
    m_sServletPath = sServletPath;
    return this;
  }

  @Nullable
  public String getServletPath ()
  {
    return m_sServletPath;
  }

  /**
   * Define the session ID to be used when creating a new session
   *
   * @param sSessionID
   *        The session ID to be used. If it is <code>null</code> a unique
   *        session ID is generated.
   * @return this
   */
  @Nonnull
  public MockHttpServletRequest setSessionID (@Nullable final String sSessionID)
  {
    m_sSessionID = sSessionID;
    return this;
  }

  /**
   * @return The session ID to use or <code>null</code> if a new session ID
   *         should be generated!
   */
  @Nullable
  public String getSessionID ()
  {
    return m_sSessionID;
  }

  @Nonnull
  public MockHttpServletRequest setSession (@Nullable final HttpSession aHttpSession)
  {
    m_aSession = aHttpSession;
    if (aHttpSession instanceof MockHttpSession)
      ((MockHttpSession) aHttpSession).doAccess ();
    return this;
  }

  @Nullable
  public HttpSession getSession (final boolean bCreate)
  {
    checkActive ();

    // Reset session if invalidated.
    if (m_aSession instanceof MockHttpSession && ((MockHttpSession) m_aSession).isInvalid ())
      m_aSession = null;

    // Create new session if necessary.
    if (m_aSession == null && bCreate)
      m_aSession = new MockHttpSession (getServletContext (), m_sSessionID);

    // Update last access time
    if (m_aSession instanceof MockHttpSession)
      ((MockHttpSession) m_aSession).doAccess ();

    return m_aSession;
  }

  @Nonnull
  public HttpSession getSession ()
  {
    return getSession (true);
  }

  @Nonnull
  public MockHttpServletRequest setRequestedSessionIdValid (final boolean bRequestedSessionIdValid)
  {
    m_bRequestedSessionIDValid = bRequestedSessionIdValid;
    return this;
  }

  public boolean isRequestedSessionIdValid ()
  {
    return m_bRequestedSessionIDValid;
  }

  @Nonnull
  public MockHttpServletRequest setRequestedSessionIdFromCookie (final boolean bRequestedSessionIdFromCookie)
  {
    m_bRequestedSessionIDFromCookie = bRequestedSessionIdFromCookie;
    return this;
  }

  public boolean isRequestedSessionIdFromCookie ()
  {
    return m_bRequestedSessionIDFromCookie;
  }

  @Nonnull
  public MockHttpServletRequest setRequestedSessionIdFromURL (final boolean bRequestedSessionIdFromURL)
  {
    m_bRequestedSessionIDFromURL = bRequestedSessionIdFromURL;
    return this;
  }

  public boolean isRequestedSessionIdFromURL ()
  {
    return m_bRequestedSessionIDFromURL;
  }

  @Deprecated
  public boolean isRequestedSessionIdFromUrl ()
  {
    return isRequestedSessionIdFromURL ();
  }

  // Servlet 3.0 API

  @UnsupportedOperation
  public AsyncContext startAsync ()
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public AsyncContext startAsync (final ServletRequest servletRequest, final ServletResponse servletResponse)
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public boolean isAsyncStarted ()
  {
    throw new UnsupportedOperationException ();
  }

  public boolean isAsyncSupported ()
  {
    return false;
  }

  @UnsupportedOperation
  public AsyncContext getAsyncContext ()
  {
    throw new UnsupportedOperationException ();
  }

  public DispatcherType getDispatcherType ()
  {
    return DispatcherType.REQUEST;
  }

  @UnsupportedOperation
  public boolean authenticate (final HttpServletResponse response) throws IOException, ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public void login (final String username, final String password) throws ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public void logout () throws ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public ICommonsCollection <Part> getParts () throws IOException, ServletException
  {
    throw new UnsupportedOperationException ();
  }

  @UnsupportedOperation
  public Part getPart (final String name) throws IOException, ServletException
  {
    throw new UnsupportedOperationException ();
  }

  /**
   * Set all path related members to the value to be deduced from the request
   * URI.
   *
   * @param sRequestURL
   *        The request URL to parse and set correctly. If it is
   *        <code>null</code> or empty, all methods are set to <code>null</code>
   *        .
   * @return this
   * @see #setScheme(String)
   * @see #setServerName(String)
   * @see #setServerPort(int)
   * @see #setContextPath(String)
   * @see #setServletPath(String)
   * @see #setPathInfo(String)
   * @see #setQueryString(String)
   * @see #setParameters(List)
   */
  @Nonnull
  public MockHttpServletRequest setAllPaths (@Nullable final String sRequestURL)
  {
    if (StringHelper.hasText (sRequestURL))
    {
      final URI aURI = URLHelper.getAsURI (RequestHelper.getWithoutSessionID (sRequestURL));
      if (aURI != null)
      {
        // Server stuff - straight forward
        setScheme (aURI.getScheme ());
        setSecure (SchemeDefaultPortMapper.SCHEME_HTTPS.equals (aURI.getScheme ()));
        setServerName (aURI.getHost ());
        setServerPort (RequestHelper.getServerPortToUse (aURI.getScheme (), aURI.getPort ()));

        // Path stuff
        String sPath = aURI.getPath ();

        // Context path
        final String sServletContextPath = m_aServletContext == null ? "" : m_aServletContext.getContextPath ();
        if (sServletContextPath.isEmpty () || StringHelper.startsWith (sPath, sServletContextPath))
        {
          setContextPath (sServletContextPath);
          sPath = sPath.substring (sServletContextPath.length ());
        }
        else
        {
          setContextPath ("");
        }

        // Servlet path
        final int nIndex = sPath.indexOf ('/', 1);
        if (nIndex >= 0)
        {
          setServletPath (sPath.substring (0, nIndex));
          sPath = sPath.substring (nIndex);
        }
        else
        {
          setServletPath (sPath);
          sPath = "";
        }

        // Remaining is the path info:
        setPathInfo (sPath);

        // Update request URI
        setRequestURI (ServletHelper.getRequestContextPath (this) +
                       ServletHelper.getRequestServletPath (this) +
                       ServletHelper.getRequestPathInfo (this));

        // Request parameters
        setQueryString (aURI.getQuery ());
        removeAllParameters ();
        setParameters (URLHelper.getParsedQueryParameters (aURI.getQuery ()));
        return this;
      }
    }

    setScheme (null);
    setSecure (false);
    setServerName (null);
    setServerPort (DEFAULT_SERVER_PORT);
    setContextPath (null);
    setServletPath (null);
    setPathInfo (null);
    setRequestURI (null);
    setQueryString (null);
    removeAllParameters ();
    return this;
  }

  // Servlet spec 3.1 methods:

  public long getContentLengthLong ()
  {
    return getContentLength ();
  }

  public String changeSessionId ()
  {
    m_sSessionID = GlobalIDFactory.getNewStringID ();
    return m_sSessionID;
  }

  public <T extends HttpUpgradeHandler> T upgrade (final Class <T> handlerClass) throws IOException, ServletException
  {
    throw new UnsupportedOperationException ("upgrade is not supported!");
  }
}
