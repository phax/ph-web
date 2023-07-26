/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.servlet.request;

import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.function.BiConsumer;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.SimpleURL;
import com.helger.commons.url.URLData;
import com.helger.commons.url.URLHelper;
import com.helger.http.AcceptCharsetHandler;
import com.helger.http.AcceptCharsetList;
import com.helger.http.AcceptEncodingHandler;
import com.helger.http.AcceptEncodingList;
import com.helger.http.AcceptLanguageHandler;
import com.helger.http.AcceptLanguageList;
import com.helger.http.AcceptMimeTypeHandler;
import com.helger.http.AcceptMimeTypeList;
import com.helger.http.EHttpVersion;
import com.helger.http.basicauth.BasicAuthClientCredentials;
import com.helger.http.basicauth.HttpBasicAuth;
import com.helger.http.digestauth.DigestAuthClientCredentials;
import com.helger.http.digestauth.HttpDigestAuth;
import com.helger.network.port.CNetworkPort;
import com.helger.network.port.NetworkPortHelper;
import com.helger.network.port.SchemeDefaultPortMapper;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletHelper;
import com.helger.useragent.IUserAgent;
import com.helger.useragent.UserAgent;
import com.helger.useragent.UserAgentDatabase;
import com.helger.useragent.UserAgentElementList;
import com.helger.useragent.uaprofile.IUAProfileHeaderProvider;
import com.helger.useragent.uaprofile.UAProfile;
import com.helger.useragent.uaprofile.UAProfileDatabase;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Misc. helper method on {@link HttpServletRequest} objects.
 *
 * @author Philip Helger
 */
@Immutable
public final class RequestHelper
{
  private static final class UAProfileHeaderProviderHttpServletRequest implements IUAProfileHeaderProvider
  {
    private final HttpServletRequest m_aHttpRequest;

    public UAProfileHeaderProviderHttpServletRequest (@Nonnull final HttpServletRequest aHttpRequest)
    {
      m_aHttpRequest = aHttpRequest;
    }

    @Nonnull
    @ReturnsMutableCopy
    public ICommonsList <String> getAllHeaderNames ()
    {
      return new CommonsArrayList <> (m_aHttpRequest.getHeaderNames ());
    }

    @Nonnull
    @ReturnsMutableCopy
    public ICommonsList <String> getHeaders (@Nullable final String sName)
    {
      return new CommonsArrayList <> (m_aHttpRequest.getHeaders (sName));
    }

    @Nullable
    public String getHeaderValue (@Nullable final String sHeader)
    {
      return ServletHelper.getRequestHeader (m_aHttpRequest, sHeader);
    }
  }

  public static final String SERVLET_ATTR_SSL_CIPHER_SUITE = "jakarta.servlet.request.cipher_suite";
  public static final String SERVLET_ATTR_SSL_KEY_SIZE = "jakarta.servlet.request.key_size";
  public static final String SERVLET_ATTR_CLIENT_CERTIFICATE = "jakarta.servlet.request.X509Certificate";

  /**
   * Content-disposition value for form data.
   */
  public static final String FORM_DATA = "form-data";

  /**
   * Content-disposition value for file attachment.
   */
  public static final String ATTACHMENT = "attachment";

  /**
   * Part of HTTP content type header. Must be all lower case!
   */
  public static final String MULTIPART = "multipart/";

  /**
   * HTTP content type header for multipart forms. Must be all lower case!
   */
  public static final String MULTIPART_FORM_DATA = MULTIPART + "form-data";

  /**
   * HTTP content type header for multiple uploads. Must be all lower case!
   */
  public static final String MULTIPART_MIXED = MULTIPART + "mixed";

  /**
   * The prefix to appended to the field name of the checkbox to create the
   * hidden field.
   */
  public static final String DEFAULT_CHECKBOX_HIDDEN_FIELD_PREFIX = "__";

  private static final String SCOPE_ATTR_REQUESTHELP_REQUESTPARAMMAP = "$requesthelp.requestparammap";
  private static final Logger LOGGER = LoggerFactory.getLogger (RequestHelper.class);

  @PresentForCodeCoverage
  private static final RequestHelper INSTANCE = new RequestHelper ();

  private RequestHelper ()
  {}

  /**
   * Get the passed string without an eventually contained session ID like in
   * "test.html;JSESSIONID=1234".<br>
   * Attention: this methods does not consider eventually present request
   * parameters. If parameters are present, they are most likely be stripped
   * away!
   *
   * @param sValue
   *        The value to strip the session ID from. May not be
   *        <code>null</code>.
   * @return The value without a session ID or the original string.
   */
  @Nonnull
  public static String getWithoutSessionID (@Nonnull final String sValue)
  {
    ValueEnforcer.notNull (sValue, "Value");

    // Strip session ID parameter
    final int nIndex = sValue.indexOf (';');
    return nIndex == -1 ? sValue : sValue.substring (0, nIndex);
  }

  /**
   * Get the passed string without an eventually contained session ID like in
   * "test.html;JSESSIONID=1234?param=value".
   *
   * @param aURL
   *        The value to strip the session ID from the path
   * @return The value without a session ID or the original string.
   */
  @Nonnull
  public static SimpleURL getWithoutSessionID (@Nonnull final ISimpleURL aURL)
  {
    ValueEnforcer.notNull (aURL, "URL");
    // Strip the parameter from the path, but keep parameters and anchor intact!
    // Note: using URLData avoid parsing, since the data was already parsed!
    return new SimpleURL (new URLData (getWithoutSessionID (aURL.getPath ()), aURL.params (), aURL.getAnchor ()));
  }

  /**
   * Get the session ID of the passed string (like in
   * "test.html;JSESSIONID=1234").<br>
   * Attention: this methods does not consider eventually present request
   * parameters. If parameters are present, they must be stripped away
   * explicitly!
   *
   * @param sValue
   *        The value to get the session ID from. May not be <code>null</code>.
   * @return The session ID of the value or <code>null</code> if no session ID
   *         is present.
   */
  @Nullable
  public static String getSessionID (@Nonnull final String sValue)
  {
    ValueEnforcer.notNull (sValue, "Value");

    // Get the session ID parameter
    final int nIndex = sValue.indexOf (';');
    return nIndex == -1 ? null : sValue.substring (nIndex + 1);
  }

  /**
   * Get the session ID of the passed string (like in
   * "test.html;JSESSIONID=1234").<br>
   * Attention: this methods does not consider eventually present request
   * parameters. If parameters are present, they must be stripped away
   * explicitly!
   *
   * @param aURL
   *        The URL to get the session ID from. May not be <code>null</code>.
   * @return The session ID of the value or <code>null</code> if no session ID
   *         is present.
   */
  @Nullable
  public static String getSessionID (@Nonnull final ISimpleURL aURL)
  {
    ValueEnforcer.notNull (aURL, "URL");

    // Ignore everything except the path
    return getSessionID (aURL.getPath ());
  }

  /**
   * Get the request URI without an eventually appended session
   * (";jsessionid=...").<br>
   * This method considers the GlobalWebScope custom context path.<br>
   * This method returns the percent decoded parameters
   * <table>
   * <caption>Examples of Returned Values</caption>
   * <tr>
   * <th>First line of HTTP request</th>
   * <th>Returned Value</th>
   * <tr>
   * <td>POST /some/pa%3Ath.html;JSESSIONID=4711</td>
   * <td>/some/pa:th.html</td>
   * </tr>
   * <tr>
   * <td>GET http://foo.bar/a.html;JSESSIONID=4711</td>
   * <td>/a.html</td>
   * </tr>
   * <tr>
   * <td>HEAD /xyz;JSESSIONID=4711?a=b</td>
   * <td>/xyz</td>
   * </tr>
   * </table>
   *
   * @param aHttpRequest
   *        The HTTP request. May not be <code>null</code>.
   * @return The request URI without the optional session ID. Never
   *         <code>null</code>.
   * @since 9.1.0
   */
  @Nonnull
  public static String getRequestURIDecoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    // Use the GlobalWebScope context path to build the result string instead of
    // "aHttpRequest.getRequestURI"!
    // Avoid exception on shutdown
    final String sContextPath = StringHelper.getNotNull (ServletContextPathHolder.getContextPath (), "");

    final String sServletPath = ServletHelper.getRequestServletPath (aHttpRequest);
    // Lacks the information about percent encoding
    final String sPathInfo = ServletHelper.getRequestPathInfo (aHttpRequest);

    final String sRequestURI = sContextPath + sServletPath + sPathInfo;
    if (sRequestURI.length () == 0)
      return sRequestURI;

    return getWithoutSessionID (sRequestURI);
  }

  /**
   * Get the request URI without an eventually appended session
   * (";jsessionid=...").<br>
   * This method considers the GlobalWebScope custom context path.<br>
   * This method returns the percent encoded parameters "as is"
   * <table>
   * <caption>Examples of Returned Values</caption>
   * <tr>
   * <th>First line of HTTP request</th>
   * <th>Returned Value</th>
   * <tr>
   * <td>POST /some/pa%3Ath.html;JSESSIONID=4711</td>
   * <td>/some/pa%3Ath.html</td>
   * </tr>
   * <tr>
   * <td>GET http://foo.bar/a.html;JSESSIONID=4711</td>
   * <td>/a.html</td>
   * </tr>
   * <tr>
   * <td>HEAD /xyz;JSESSIONID=4711?a=b</td>
   * <td>/xyz</td>
   * </tr>
   * </table>
   *
   * @param aHttpRequest
   *        The HTTP request. May not be <code>null</code>.
   * @return The request URI without the optional session ID. Never
   *         <code>null</code>.
   * @since 9.1.0
   */
  @Nonnull
  public static String getRequestURIEncoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    // Use the GlobalWebScope context path to build the result string instead of
    // "aHttpRequest.getRequestURI"!
    // Avoid exception on shutdown
    final String sContextPath = StringHelper.getNotNull (ServletContextPathHolder.getContextPathOrNull (), "");

    final String sRequestContextPath = ServletHelper.getRequestContextPath (aHttpRequest, "");
    // Maintain the encoding
    String sRealRequestURI = ServletHelper.getRequestRequestURI (aHttpRequest);
    // Strip "real" leading context path, so that the one from
    // ServletContextPathHolder can be used instead
    if (sRequestContextPath.length () > 0 && sRealRequestURI.startsWith (sRequestContextPath))
      sRealRequestURI = sRealRequestURI.substring (sRequestContextPath.length ());

    final String sRequestURI = sContextPath + sRealRequestURI;
    if (sRequestURI.length () == 0)
      return sRequestURI;

    return getWithoutSessionID (sRequestURI);
  }

  /**
   * Get the request path info without an eventually appended session
   * (";jsessionid=...")
   *
   * @param aHttpRequest
   *        The HTTP request
   * @return Returns any extra path information associated with the URL the
   *         client sent when it made this request. The extra path information
   *         follows the servlet path but precedes the query string and will
   *         start with a "/" character. The optional session ID is stripped.
   */
  @Nullable
  public static String getPathInfo (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sPathInfo = ServletHelper.getRequestPathInfo (aHttpRequest);
    if (StringHelper.hasNoText (sPathInfo))
      return sPathInfo;

    return getWithoutSessionID (sPathInfo);
  }

  /**
   * Return the URI of the request within the servlet context.
   *
   * @param aHttpRequest
   *        The HTTP request. May not be <code>null</code>.
   * @return the path within the web application and never <code>null</code>. By
   *         default "/" is returned is an empty request URI is determined.
   */
  @Nonnull
  public static String getPathWithinServletContext (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // false for backwards compatibility
    return getPathWithinServletContext (aHttpRequest, false);
  }

  /**
   * Return the URI of the request within the servlet context.
   *
   * @param aHttpRequest
   *        The HTTP request. May not be <code>null</code>.
   * @param bUseEncodedPath
   *        <code>true</code> to use the URL encoded path, <code>false</code> to
   *        use the decoded path
   * @return the path within the web application and never <code>null</code>. By
   *         default "/" is returned is an empty request URI is determined.
   * @since 9.1.10
   */
  @Nonnull
  public static String getPathWithinServletContext (@Nonnull final HttpServletRequest aHttpRequest,
                                                    final boolean bUseEncodedPath)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sRequestURI = bUseEncodedPath ? getRequestURIEncoded (aHttpRequest) : getRequestURIDecoded (
                                                                                                             aHttpRequest);
    if (StringHelper.hasNoText (sRequestURI))
    {
      // Can e.g. happen for "Request(GET //localhost:90/)"
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Having empty request URI '" + sRequestURI + "' from request " + aHttpRequest);
      return "/";
    }
    // Always use the context path
    final String sContextPath = ServletContextPathHolder.getContextPath ();
    if (StringHelper.hasNoText (sContextPath) || !sRequestURI.startsWith (sContextPath))
      return sRequestURI;

    // Normal case: URI contains context path.
    final String sPath = sRequestURI.substring (sContextPath.length ());
    return sPath.length () > 0 ? sPath : "/";
  }

  /**
   * Return the path within the servlet mapping for the given request, i.e. the
   * part of the request's URL beyond the part that called the servlet, or "" if
   * the whole URL has been used to identify the servlet. <br>
   * Detects include request URL if called within a RequestDispatcher include.
   * <br>
   * E.g.: servlet mapping = "/test/*"; request URI = "/test/a" -&gt; "/a". <br>
   * E.g.: servlet mapping = "/test"; request URI = "/test" -&gt; "". <br>
   * E.g.: servlet mapping = "/*.test"; request URI = "/a.test" -&gt; "".
   *
   * @param aHttpRequest
   *        current HTTP request
   * @return the path within the servlet mapping, or ""
   */
  @Nonnull
  public static String getPathWithinServlet (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // false for backwards compatibility
    return getPathWithinServlet (aHttpRequest, false);
  }

  /**
   * Return the path within the servlet mapping for the given request, i.e. the
   * part of the request's URL beyond the part that called the servlet, or "" if
   * the whole URL has been used to identify the servlet. <br>
   * Detects include request URL if called within a RequestDispatcher include.
   * <br>
   * E.g.: servlet mapping = "/test/*"; request URI = "/test/a" -&gt; "/a". <br>
   * E.g.: servlet mapping = "/test"; request URI = "/test" -&gt; "". <br>
   * E.g.: servlet mapping = "/*.test"; request URI = "/a.test" -&gt; "".
   *
   * @param aHttpRequest
   *        current HTTP request
   * @param bUseEncodedPath
   *        <code>true</code> to use the URL encoded path, <code>false</code> to
   *        use the decoded path
   * @return the path within the servlet mapping, or ""
   * @since 9.1.10
   */
  @Nonnull
  public static String getPathWithinServlet (@Nonnull final HttpServletRequest aHttpRequest,
                                             final boolean bUseEncodedPath)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sPathWithinApp = getPathWithinServletContext (aHttpRequest, bUseEncodedPath);
    final String sServletPath = ServletHelper.getRequestServletPath (aHttpRequest);
    if (sPathWithinApp.startsWith (sServletPath))
      return sPathWithinApp.substring (sServletPath.length ());

    // Special case: URI is different from servlet path.
    // Can happen e.g. with index page: URI="/", servletPath="/index.html"
    // Use servlet path in this case, as it indicates the actual target path.
    return sServletPath;
  }

  /**
   * Reconstructs the URL the client used to make the request. The returned URL
   * contains a protocol, server name, port number, and server path, but it does
   * not include query string parameters.<br>
   * This method returns the percent decoded parameters
   * <p>
   * If this request has been forwarded using
   * {@link jakarta.servlet.RequestDispatcher#forward}, the server path in the
   * reconstructed URL must reflect the path used to obtain the
   * RequestDispatcher, and not the server path specified by the client.
   * <p>
   * Because this method returns a <code>StringBuilder</code>, not a string, you
   * can modify the URL easily, for example, to append query parameters.
   * <p>
   * This method is useful for creating redirect messages and for reporting
   * errors.
   *
   * @param aHttpRequest
   *        The HTTP request to get the request URL from. May not be
   *        <code>null</code>.
   * @return a <code>StringBuilder</code> object containing the reconstructed
   *         URL
   * @since 9.1.10
   */
  @Nonnull
  @Nonempty
  public static StringBuilder getRequestURLDecoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return getFullServerName (aHttpRequest.getScheme (), aHttpRequest.getServerName (), aHttpRequest.getServerPort ())
                                                                                                                      .append (getRequestURIDecoded (aHttpRequest));
  }

  /**
   * Reconstructs the URL the client used to make the request. The returned URL
   * contains a protocol, server name, port number, and server path, but it does
   * not include query string parameters.<br>
   * This method returns the percent encoded parameters "as is"
   * <p>
   * If this request has been forwarded using
   * {@link jakarta.servlet.RequestDispatcher#forward}, the server path in the
   * reconstructed URL must reflect the path used to obtain the
   * RequestDispatcher, and not the server path specified by the client.
   * <p>
   * Because this method returns a <code>StringBuilder</code>, not a string, you
   * can modify the URL easily, for example, to append query parameters.
   * <p>
   * This method is useful for creating redirect messages and for reporting
   * errors.
   *
   * @param aHttpRequest
   *        The HTTP request to get the request URL from. May not be
   *        <code>null</code>.
   * @return a <code>StringBuilder</code> object containing the reconstructed
   *         URL
   * @since 9.1.10
   */
  @Nonnull
  @Nonempty
  public static StringBuilder getRequestURLEncoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return getFullServerName (aHttpRequest.getScheme (), aHttpRequest.getServerName (), aHttpRequest.getServerPort ())
                                                                                                                      .append (getRequestURIEncoded (aHttpRequest));
  }

  /**
   * Get the full URL (incl. protocol) and parameters of the passed request.<br>
   *
   * <pre>
   * http://hostname.com/mywebapp/servlet/dir/a/b.xml=123?d=789
   * </pre>
   *
   * @param aHttpRequest
   *        The request to use. May not be <code>null</code>.
   * @return The full URL.
   * @see #getURIDecoded(HttpServletRequest) getURI to retrieve the URL without
   *      the server scheme and name.
   * @since 9.1.10
   */
  @Nonnull
  @Nonempty
  public static String getURLDecoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final StringBuilder ret = getRequestURLDecoded (aHttpRequest);
    // query string
    final String sQueryString = ServletHelper.getRequestQueryString (aHttpRequest);
    if (StringHelper.hasText (sQueryString))
      ret.append (URLHelper.QUESTIONMARK).append (sQueryString);
    return ret.toString ();
  }

  /**
   * Get the full URL (incl. protocol) and parameters of the passed request.<br>
   *
   * <pre>
   * http://hostname.com/mywebapp/servlet/dir/a/b.xml=123?d=789
   * </pre>
   *
   * @param aHttpRequest
   *        The request to use. May not be <code>null</code>.
   * @return The full URL.
   * @see #getURIEncoded(HttpServletRequest) getURI to retrieve the URL without
   *      the server scheme and name.
   * @since 9.1.10
   */

  @Nonnull
  @Nonempty
  public static String getURLEncoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final StringBuilder ret = getRequestURLEncoded (aHttpRequest);
    // query string
    final String sQueryString = ServletHelper.getRequestQueryString (aHttpRequest);
    if (StringHelper.hasText (sQueryString))
      ret.append (URLHelper.QUESTIONMARK).append (sQueryString);
    return ret.toString ();
  }

  /**
   * Get the full URI (excl. protocol) and parameters of the passed request.<br>
   * Example:
   *
   * <pre>
   * /mywebapp/servlet/dir/a/b.xml=123?d=789
   * </pre>
   *
   * @param aHttpRequest
   *        The request to use. May not be <code>null</code>.
   * @return The full URI.
   * @see #getURLDecoded(HttpServletRequest) getURL to retrieve the absolute URL
   * @since 9.1.10
   */
  @Nonnull
  @Nonempty
  public static String getURIDecoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sReqUrl = getRequestURIDecoded (aHttpRequest);
    // d=789&x=y
    final String sQueryString = ServletHelper.getRequestQueryString (aHttpRequest);
    if (StringHelper.hasText (sQueryString))
      return sReqUrl + URLHelper.QUESTIONMARK + sQueryString;
    return sReqUrl;
  }

  /**
   * Get the full URI (excl. protocol) and parameters of the passed request.<br>
   * Example:
   *
   * <pre>
   * /mywebapp/servlet/dir/a/b.xml=123?d=789
   * </pre>
   *
   * @param aHttpRequest
   *        The request to use. May not be <code>null</code>.
   * @return The full URI.
   * @see #getURLDecoded(HttpServletRequest) getURL to retrieve the absolute URL
   * @since 9.1.10
   */
  @Nonnull
  @Nonempty
  public static String getURIEncoded (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sReqUrl = getRequestURIEncoded (aHttpRequest);
    // d=789&x=y
    final String sQueryString = ServletHelper.getRequestQueryString (aHttpRequest);
    if (StringHelper.hasText (sQueryString))
      return sReqUrl + URLHelper.QUESTIONMARK + sQueryString;
    return sReqUrl;
  }

  @CheckForSigned
  public static int getDefaultServerPort (@Nullable final String sScheme)
  {
    return SchemeDefaultPortMapper.getDefaultPort (sScheme, CNetworkPort.INVALID_PORT_NUMBER);
  }

  @CheckForSigned
  public static int getServerPortToUse (@Nonnull final String sScheme, @CheckForSigned final int nServerPort)
  {
    // URL.getPort() delivers -1 for unspecified ports
    if (!NetworkPortHelper.isValidPort (nServerPort))
      return getDefaultServerPort (sScheme);
    return nServerPort;
  }

  @Nonnull
  @Nonempty
  public static StringBuilder getFullServerName (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");
    return getFullServerName (aHttpRequest.getScheme (), aHttpRequest.getServerName (), aHttpRequest.getServerPort ());
  }

  @Nonnull
  @Nonempty
  public static StringBuilder getFullServerName (@Nullable final String sScheme,
                                                 @Nullable final String sServerName,
                                                 final int nServerPort)
  {
    // Reconstruct URL
    final StringBuilder aSB = new StringBuilder ();
    if (StringHelper.hasText (sScheme))
      aSB.append (sScheme).append ("://");
    if (StringHelper.hasText (sServerName))
      aSB.append (sServerName);
    if (NetworkPortHelper.isValidPort (nServerPort) && nServerPort != getDefaultServerPort (sScheme))
      aSB.append (':').append (nServerPort);
    return aSB;
  }

  @Nonnull
  @Nonempty
  public static String getFullServerNameAndPath (@Nullable final String sScheme,
                                                 @Nullable final String sServerName,
                                                 final int nServerPort,
                                                 @Nullable final String sPath,
                                                 @Nullable final String sQueryString)
  {
    final StringBuilder aURL = getFullServerName (sScheme, sServerName, nServerPort);
    if (StringHelper.hasText (sPath))
    {
      if (!StringHelper.startsWith (sPath, '/'))
        aURL.append ('/');
      aURL.append (sPath);
    }
    if (StringHelper.hasText (sQueryString))
      aURL.append (URLHelper.QUESTIONMARK).append (sQueryString);
    return aURL.toString ();
  }

  @Nullable
  public static String getHttpReferer (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.REFERER);
  }

  /**
   * Get the HTTP version associated with the given HTTP request
   *
   * @param aHttpRequest
   *        The http request to query. May not be <code>null</code>.
   * @return <code>null</code> if no supported HTTP version is contained
   */
  @Nullable
  public static EHttpVersion getHttpVersion (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sProtocol = aHttpRequest.getProtocol ();
    return EHttpVersion.getFromNameOrNull (sProtocol);
  }

  /**
   * Get the HTTP method associated with the given HTTP request
   *
   * @param aHttpRequest
   *        The http request to query. May not be <code>null</code>.
   * @return <code>null</code> if no supported HTTP method is contained
   */
  @Nullable
  public static EHttpMethod getHttpMethod (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return getHttpMethodOrDefault (aHttpRequest, null);
  }

  /**
   * Get the HTTP method associated with the given HTTP request
   *
   * @param aHttpRequest
   *        The http request to query. May not be <code>null</code>.
   * @param eDefault
   *        The default to be returned, if no HTTP method could be found. May be
   *        <code>null</code>.
   * @return <code>null</code> if no supported HTTP method is contained
   * @since 9.1.6
   */
  @Nullable
  public static EHttpMethod getHttpMethodOrDefault (@Nonnull final HttpServletRequest aHttpRequest,
                                                    @Nullable final EHttpMethod eDefault)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sMethod = aHttpRequest.getMethod ();
    final EHttpMethod ret = EHttpMethod.getFromNameOrNull (sMethod);
    if (ret != null)
      return ret;
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Unknown HTTP request method '" + sMethod + "' used in request " + aHttpRequest);
    return eDefault;
  }

  /**
   * Get a complete request header map as a copy.
   *
   * @param aHttpRequest
   *        The source HTTP request. May not be <code>null</code>.
   * @param aConsumer
   *        The BiConsumer that takes name and value. May not be
   *        <code>null</code>.
   * @since 9.1.9
   */
  public static void forEachRequestHeader (@Nonnull final HttpServletRequest aHttpRequest,
                                           @Nonnull final BiConsumer <String, String> aConsumer)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");
    ValueEnforcer.notNull (aConsumer, "Consumer");

    final Enumeration <String> aHeaders = aHttpRequest.getHeaderNames ();
    while (aHeaders.hasMoreElements ())
    {
      final String sName = aHeaders.nextElement ();
      final Enumeration <String> eHeaderValues = aHttpRequest.getHeaders (sName);
      while (eHeaderValues.hasMoreElements ())
      {
        final String sValue = eHeaderValues.nextElement ();
        aConsumer.accept (sName, sValue);
      }
    }
  }

  /**
   * Get a complete request header map as a copy.
   *
   * @param aHttpRequest
   *        The source HTTP request. May not be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static HttpHeaderMap getRequestHeaderMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final HttpHeaderMap ret = new HttpHeaderMap ();
    forEachRequestHeader (aHttpRequest, ret::addHeader);
    return ret;
  }

  @Nonnull
  public static IRequestParamMap getRequestParamMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    // Check if a value is cached in the HTTP request
    IRequestParamMap aValue = ServletHelper.getRequestAttributeAs (aHttpRequest,
                                                                   SCOPE_ATTR_REQUESTHELP_REQUESTPARAMMAP);
    if (aValue == null)
    {
      aValue = RequestParamMap.createFromRequest (aHttpRequest);
      ServletHelper.setRequestAttribute (aHttpRequest, SCOPE_ATTR_REQUESTHELP_REQUESTPARAMMAP, aValue);
    }
    return aValue;
  }

  /**
   * Get the content length of the passed request. This is not done using
   * <code>request.getContentLength()</code> but instead parsing the HTTP header
   * field {@link CHttpHeader#CONTENT_LENGTH} manually!
   *
   * @param aHttpRequest
   *        Source HTTP request. May not be <code>null</code>.
   * @return -1 if no or an invalid content length is set in the header
   */
  @CheckForSigned
  public static long getContentLength (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    if (false)
    {
      // Missing support > 2GB!!!
      return aHttpRequest.getContentLength ();
    }
    final String sContentLength = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.CONTENT_LENGTH);
    return StringParser.parseLong (sContentLength, -1L);
  }

  @Nullable
  private static <T> T _getRequestAttr (@Nonnull final HttpServletRequest aHttpRequest,
                                        @Nonnull @Nonempty final String sAttrName,
                                        @Nonnull final Class <T> aDstClass)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final Object aValue = ServletHelper.getRequestAttributeAs (aHttpRequest, sAttrName);
    if (aValue == null)
    {
      // No client certificates present
      return null;
    }
    // type check
    if (!aDstClass.isAssignableFrom (aValue.getClass ()))
    {
      LOGGER.error ("Request attribute " +
                    sAttrName +
                    " is not of type " +
                    aDstClass.getName () +
                    " but of type " +
                    aValue.getClass ().getName ());
      return null;
    }
    // Return the certificates
    return aDstClass.cast (aValue);
  }

  /**
   * @param aHttpRequest
   *        he HTTP servlet request to extract the information from. May not be
   *        <code>null</code>.
   * @return SSL cipher suite or <code>null</code> if no such attribute is
   *         present
   */
  @Nullable
  public static String getRequestSSLCipherSuite (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return _getRequestAttr (aHttpRequest, SERVLET_ATTR_SSL_CIPHER_SUITE, String.class);
  }

  /**
   * @param aHttpRequest
   *        he HTTP servlet request to extract the information from. May not be
   *        <code>null</code>.
   * @return Bit size of the algorithm or <code>null</code> if no such attribute
   *         is present
   */
  @Nullable
  public static Integer getRequestSSLKeySize (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return _getRequestAttr (aHttpRequest, SERVLET_ATTR_SSL_KEY_SIZE, Integer.class);
  }

  /**
   * Get the client certificates provided by a HTTP servlet request.
   *
   * @param aHttpRequest
   *        The HTTP servlet request to extract the information from. May not be
   *        <code>null</code>.
   * @return <code>null</code> if the passed request does not contain any client
   *         certificate
   */
  @Nullable
  public static X509Certificate [] getRequestClientCertificates (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return _getRequestAttr (aHttpRequest, SERVLET_ATTR_CLIENT_CERTIFICATE, X509Certificate [].class);
  }

  /**
   * Utility method that determines whether the request contains multipart
   * content.
   *
   * @param sContentType
   *        The content type to be checked. May be <code>null</code>.
   * @return <code>true</code> if the request is multipart; <code>false</code>
   *         otherwise.
   */
  public static boolean isMultipartContent (@Nullable final String sContentType)
  {
    return sContentType != null && sContentType.toLowerCase (Locale.US).startsWith (MULTIPART);
  }

  /**
   * Utility method that determines whether the request contains multipart
   * content.
   *
   * @param aHttpRequest
   *        The servlet request to be evaluated. Must be non-null.
   * @return <code>true</code> if the request is multipart; <code>false</code>
   *         otherwise.
   */
  public static boolean isMultipartContent (@Nonnull final HttpServletRequest aHttpRequest)
  {
    if (getHttpMethod (aHttpRequest) != EHttpMethod.POST)
      return false;

    return isMultipartContent (aHttpRequest.getContentType ());
  }

  /**
   * Utility method that determines whether the request contains
   * <code>multipart/form-data</code> content.
   *
   * @param sContentType
   *        The content type to be checked. May be <code>null</code>.
   * @return <code>true</code> if the passed, lowercased content type starts
   *         with <code>multipart/form-data</code>; <code>false</code>
   *         otherwise.
   */
  public static boolean isMultipartFormDataContent (@Nullable final String sContentType)
  {
    return sContentType != null && sContentType.toLowerCase (Locale.US).startsWith (MULTIPART_FORM_DATA);
  }

  /**
   * Utility method that determines whether the request contains multipart
   * content.
   *
   * @param aHttpRequest
   *        The servlet request to be evaluated. Must be non-null.
   * @return <code>true</code> if the request is multipart; <code>false</code>
   *         otherwise.
   */
  public static boolean isMultipartFormDataContent (@Nonnull final HttpServletRequest aHttpRequest)
  {
    if (getHttpMethod (aHttpRequest) != EHttpMethod.POST)
      return false;

    return isMultipartFormDataContent (aHttpRequest.getContentType ());
  }

  @Nonnull
  public static AcceptCharsetList getAcceptCharsets (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Check if a value is cached in the HTTP request
    AcceptCharsetList aValue = ServletHelper.getRequestAttributeAs (aHttpRequest, AcceptCharsetList.class.getName ());
    if (aValue == null)
    {
      final String sAcceptCharset = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.ACCEPT_CHARSET);
      aValue = AcceptCharsetHandler.getAcceptCharsets (sAcceptCharset);
      ServletHelper.setRequestAttribute (aHttpRequest, AcceptCharsetList.class.getName (), aValue);
    }
    return aValue;
  }

  @Nonnull
  public static AcceptEncodingList getAcceptEncodings (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Check if a value is cached in the HTTP request
    AcceptEncodingList aValue = ServletHelper.getRequestAttributeAs (aHttpRequest, AcceptEncodingList.class.getName ());
    if (aValue == null)
    {
      final String sAcceptEncoding = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.ACCEPT_ENCODING);
      aValue = AcceptEncodingHandler.getAcceptEncodings (sAcceptEncoding);
      ServletHelper.setRequestAttribute (aHttpRequest, AcceptEncodingList.class.getName (), aValue);
    }
    return aValue;
  }

  @Nonnull
  public static AcceptLanguageList getAcceptLanguages (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Check if a value is cached in the HTTP request
    AcceptLanguageList aValue = ServletHelper.getRequestAttributeAs (aHttpRequest, AcceptLanguageList.class.getName ());
    if (aValue == null)
    {
      final String sAcceptLanguage = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.ACCEPT_LANGUAGE);
      aValue = AcceptLanguageHandler.getAcceptLanguages (sAcceptLanguage);
      ServletHelper.setRequestAttribute (aHttpRequest, AcceptLanguageList.class.getName (), aValue);
    }
    return aValue;
  }

  @Nonnull
  public static AcceptMimeTypeList getAcceptMimeTypes (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Check if a value is cached in the HTTP request
    AcceptMimeTypeList aValue = ServletHelper.getRequestAttributeAs (aHttpRequest, AcceptMimeTypeList.class.getName ());
    if (aValue == null)
    {
      final String sAcceptMimeTypes = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.ACCEPT);
      aValue = AcceptMimeTypeHandler.getAcceptMimeTypes (sAcceptMimeTypes);
      ServletHelper.setRequestAttribute (aHttpRequest, AcceptMimeTypeList.class.getName (), aValue);
    }
    return aValue;
  }

  /**
   * Get the Basic authentication credentials from the passed HTTP servlet
   * request from the HTTP header {@link CHttpHeader#AUTHORIZATION}.
   *
   * @param aHttpRequest
   *        The HTTP request to be interpreted. May be <code>null</code>.
   * @return <code>null</code> if the passed request does not contain a valid
   *         HTTP Basic Authentication header value.
   */
  @Nullable
  public static BasicAuthClientCredentials getBasicAuthClientCredentials (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sHeaderValue = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.AUTHORIZATION);
    return HttpBasicAuth.getBasicAuthClientCredentials (sHeaderValue);
  }

  /**
   * Get the Digest authentication credentials from the passed HTTP servlet
   * request from the HTTP header {@link CHttpHeader#AUTHORIZATION}.
   *
   * @param aHttpRequest
   *        The HTTP request to be interpreted. May be <code>null</code>.
   * @return <code>null</code> if the passed request does not contain a valid
   *         HTTP Digest Authentication header value.
   */
  @Nullable
  public static DigestAuthClientCredentials getDigestAuthClientCredentials (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sHeaderValue = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.AUTHORIZATION);
    return HttpDigestAuth.getDigestAuthClientCredentials (sHeaderValue);
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
    String sUserAgent = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.UA);
    if (sUserAgent == null)
    {
      sUserAgent = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.X_DEVICE_USER_AGENT);
      if (sUserAgent == null)
        sUserAgent = ServletHelper.getRequestHeader (aHttpRequest, CHttpHeader.USER_AGENT);
    }
    return sUserAgent;
  }

  /**
   * Get the user agent object from the given HTTP request.
   *
   * @param aHttpRequest
   *        The HTTP request to extract the information from.
   * @return A non-<code>null</code> user agent object or <code>null</code> in
   *         case of an internal inconsistency.
   */
  @Nullable
  public static IUserAgent getUserAgent (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final Object aAttr = ServletHelper.getRequestAttribute (aHttpRequest, IUserAgent.class.getName ());
    try
    {
      IUserAgent aUserAgent = (IUserAgent) aAttr;
      if (aUserAgent == null)
      {
        // Extract HTTP header from request
        final String sUserAgent = getHttpUserAgentStringFromRequest (aHttpRequest);
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
  @Nonnull
  public static UAProfile getUAProfile (@Nonnull final HttpServletRequest aHttpRequest)
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

  /**
   * Get the name of the automatic hidden field associated with a check-box.
   *
   * @param sFieldName
   *        The name of the check-box.
   * @return The name of the hidden field associated with the given check-box
   *         name.
   */
  @Nonnull
  @Nonempty
  public static String getCheckBoxHiddenFieldName (@Nonnull @Nonempty final String sFieldName)
  {
    ValueEnforcer.notEmpty (sFieldName, "FieldName");
    return DEFAULT_CHECKBOX_HIDDEN_FIELD_PREFIX + sFieldName;
  }
}
