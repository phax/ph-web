/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.web.scope;

import java.nio.charset.Charset;
import java.security.Principal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.SimpleURL;
import com.helger.http.EHttpVersion;
import com.helger.scope.IRequestScope;
import com.helger.scope.mgr.ScopeManager;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.request.IRequestParamMap;
import com.helger.servlet.request.RequestHelper;
import com.helger.useragent.IUserAgent;
import com.helger.useragent.browser.BrowserInfo;

/**
 * Interface for a single web request scope object that does not offer access to
 * the HTTP response.
 *
 * @author Philip Helger
 */
public interface IRequestWebScopeWithoutResponse extends IRequestScope, IWebScope
{
  /**
   * @return A cached header map for this request. Never <code>null</code>.
   *         Alterations to this map are visible everywhere. Clone the object if
   *         you need to modify it.
   */
  @Nonnull
  @ReturnsMutableObject
  HttpHeaderMap headers ();

  /**
   * Get the user agent object of this HTTP request.
   *
   * @return A non-<code>null</code> user agent object.
   */
  @Nonnull
  default IUserAgent getUserAgent ()
  {
    return RequestHelper.getUserAgent (getRequest ());
  }

  /**
   * @return The information about the matching browser or <code>null</code> if
   *         no known browser was detected.
   */
  @Nullable
  default BrowserInfo getBrowserInfo ()
  {
    return getUserAgent ().getBrowserInfo ();
  }

  /**
   * @return The external URL parameters. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableObject
  IRequestParamContainer params ();

  /**
   * @return A cached request param map for this request. Never
   *         <code>null</code>.
   */
  @Nonnull
  IRequestParamMap getRequestParamMap ();

  @Nullable
  default String getSessionID (final boolean bCreateIfNotExisting)
  {
    final HttpSession aSession = getSession (bCreateIfNotExisting);
    return aSession == null ? null : aSession.getId ();
  }

  /**
   * Returns the name of the character encoding used in the body of this
   * request. This method returns <code>null</code> if the request does not
   * specify a character encoding
   *
   * @return a <code>String</code> containing the name of the character
   *         encoding, or <code>null</code> if the request does not specify a
   *         character encoding
   */
  @Nullable
  default String getCharacterEncoding ()
  {
    return getRequest ().getCharacterEncoding ();
  }

  /**
   * Returns the MIME type of the body of the request, or <code>null</code> if
   * the type is not known. For HTTP servlets, same as the value of the CGI
   * variable CONTENT_TYPE.
   *
   * @return a <code>String</code> containing the name of the MIME type of the
   *         request, or null if the type is not known
   */
  default String getContentType ()
  {
    return getRequest ().getContentType ();
  }

  /**
   * Returns the length, in bytes, of the request body and made available by the
   * input stream, or -1 if the length is not known. For HTTP servlets, same as
   * the value of the CGI variable CONTENT_LENGTH.
   *
   * @return an integer containing the length of the request body or -1 if the
   *         length is not known
   */
  default long getContentLength ()
  {
    return RequestHelper.getContentLength (getRequest ());
  }

  /**
   * @return The charset defined for this request. May be <code>null</code> if
   *         none is present;
   */
  @Nullable
  default Charset getCharset ()
  {
    final String sEncoding = getRequest ().getCharacterEncoding ();
    return StringHelper.hasNoText (sEncoding) ? null : CharsetHelper.getCharsetFromName (sEncoding);
  }

  /**
   * Returns the name and version of the protocol the request uses in the form
   * <i>protocol/majorVersion.minorVersion</i>, for example, HTTP/1.1. For HTTP
   * servlets, the value returned is the same as the value of the CGI variable
   * <code>SERVER_PROTOCOL</code>.
   *
   * @return a <code>String</code> containing the protocol name and version
   *         number
   */
  default String getProtocol ()
  {
    return getRequest ().getProtocol ();
  }

  /**
   * @return The {@link EHttpVersion} of the request.
   */
  @Nullable
  default EHttpVersion getHttpVersion ()
  {
    return RequestHelper.getHttpVersion (getRequest ());
  }

  /**
   * Returns the name of the scheme used to make this request, for example,
   * <code>http</code>, <code>https</code>, or <code>ftp</code>. Different
   * schemes have different rules for constructing URLs, as noted in RFC 1738.
   *
   * @return a <code>String</code> containing the name of the scheme used to
   *         make this request
   */
  default String getScheme ()
  {
    return getRequest ().getScheme ();
  }

  /**
   * Returns the host name of the server to which the request was sent. It is
   * the value of the part before ":" in the <code>Host</code> header value, if
   * any, or the resolved server name, or the server IP address.
   *
   * @return a <code>String</code> containing the name of the server
   */
  default String getServerName ()
  {
    return getRequest ().getServerName ();
  }

  /**
   * Returns the port number to which the request was sent. It is the value of
   * the part after ":" in the <code>Host</code> header value, if any, or the
   * server port where the client connection was accepted on.
   *
   * @return an integer specifying the port number
   */
  default int getServerPort ()
  {
    return getRequest ().getServerPort ();
  }

  /**
   * Returns the Internet Protocol (IP) address of the client or last proxy that
   * sent the request. For HTTP servlets, same as the value of the CGI variable
   * <code>REMOTE_ADDR</code>.
   *
   * @return a <code>String</code> containing the IP address of the client that
   *         sent the request
   */
  default String getRemoteAddr ()
  {
    return getRequest ().getRemoteAddr ();
  }

  /**
   * Returns the fully qualified name of the client or the last proxy that sent
   * the request. If the engine cannot or chooses not to resolve the hostname
   * (to improve performance), this method returns the dotted-string form of the
   * IP address. For HTTP servlets, same as the value of the CGI variable
   * <code>REMOTE_HOST</code>.
   *
   * @return a <code>String</code> containing the fully qualified name of the
   *         client
   */
  default String getRemoteHost ()
  {
    return getRequest ().getRemoteHost ();
  }

  /**
   * Returns the Internet Protocol (IP) source port of the client or last proxy
   * that sent the request.
   *
   * @return an integer specifying the port number
   */
  default int getRemotePort ()
  {
    return getRequest ().getRemotePort ();
  }

  /**
   * Returns a boolean indicating whether this request was made using a secure
   * channel, such as HTTPS.
   *
   * @return a boolean indicating if the request was made using a secure channel
   */
  default boolean isSecure ()
  {
    return getRequest ().isSecure ();
  }

  /**
   * Returns the host name of the Internet Protocol (IP) interface on which the
   * request was received.
   *
   * @return a <code>String</code> containing the host name of the IP on which
   *         the request was received.
   */
  default String getLocalName ()
  {
    return getRequest ().getLocalName ();
  }

  /**
   * Returns the Internet Protocol (IP) address of the interface on which the
   * request was received.
   *
   * @return a <code>String</code> containing the IP address on which the
   *         request was received.
   */
  default String getLocalAddr ()
  {
    return getRequest ().getLocalAddr ();
  }

  /**
   * Returns the Internet Protocol (IP) port number of the interface on which
   * the request was received.
   *
   * @return an integer specifying the port number
   */
  default int getLocalPort ()
  {
    return getRequest ().getLocalPort ();
  }

  // HttpServletRequest:

  /**
   * Returns the name of the authentication scheme used to protect the servlet.
   * All servlet containers support basic, form and client certificate
   * authentication, and may additionally support digest authentication. If the
   * servlet is not authenticated <code>null</code> is returned.
   * <p>
   * Same as the value of the CGI variable AUTH_TYPE.
   *
   * @return one of the static members BASIC_AUTH, FORM_AUTH, CLIENT_CERT_AUTH,
   *         DIGEST_AUTH (suitable for == comparison) or the container-specific
   *         string indicating the authentication scheme, or <code>null</code>
   *         if the request was not authenticated.
   */
  default String getAuthType ()
  {
    return getRequest ().getAuthType ();
  }

  /**
   * Returns an array containing all of the <code>Cookie</code> objects the
   * client sent with this request. This method returns <code>null</code> if no
   * cookies were sent.
   *
   * @return an array of all the <code>Cookies</code> included with this
   *         request, or <code>null</code> if the request has no cookies
   */
  @Nullable
  default Cookie [] getCookies ()
  {
    return ServletHelper.getRequestCookies (getRequest ());
  }

  /**
   * Returns the name of the HTTP method with which this request was made, for
   * example, GET, POST, or PUT. Same as the value of the CGI variable
   * REQUEST_METHOD.
   *
   * @return a <code>String</code> specifying the name of the method with which
   *         this request was made
   */
  default String getMethod ()
  {
    return getRequest ().getMethod ();
  }

  /**
   * @return The {@link EHttpMethod} matching the {@link #getMethod()}
   */
  @Nullable
  default EHttpMethod getHttpMethod ()
  {
    return RequestHelper.getHttpMethod (getRequest ());
  }

  /**
   * Returns any extra path information associated with the URL the client sent
   * when it made this request. The extra path information follows the servlet
   * path but precedes the query string and will start with a "/" character.
   * <p>
   * This method returns <code>null</code> if there was no extra path
   * information.
   * <p>
   * Same as the value of the CGI variable PATH_INFO.
   *
   * @return a <code>String</code>, decoded by the web container, specifying
   *         extra path information that comes after the servlet path but before
   *         the query string in the request URL; or <code>null</code> if the
   *         URL does not have any extra path information
   */
  default String getPathInfo ()
  {
    return RequestHelper.getPathInfo (getRequest ());
  }

  /**
   * Returns any extra path information after the servlet name but before the
   * query string, and translates it to a real path. Same as the value of the
   * CGI variable PATH_TRANSLATED.
   * <p>
   * If the URL does not have any extra path information, this method returns
   * <code>null</code> or the servlet container cannot translate the virtual
   * path to a real path for any reason (such as when the web application is
   * executed from an archive). The web container does not decode this string.
   *
   * @return a <code>String</code> specifying the real path, or
   *         <code>null</code> if the URL does not have any extra path
   *         information
   */
  default String getPathTranslated ()
  {
    return getRequest ().getPathTranslated ();
  }

  /**
   * @return Returns the portion of the request URI that indicates the context
   *         of the request. The context path always comes first in a request
   *         URI. The path starts with a "/" character but does not end with a
   *         "/" character. For servlets in the default (root) context, this
   *         method returns "". The container does not decode this string. E.g.
   *         <code>/context</code> or an empty string for the root context.
   *         Never with a trailing slash.
   * @see #getFullContextPath()
   */
  @Nonnull
  String getContextPath ();

  /**
   * Returns the query string that is contained in the request URL after the
   * path. This method returns <code>null</code> if the URL does not have a
   * query string. Same as the value of the CGI variable QUERY_STRING.
   *
   * @return a <code>String</code> containing the query string or
   *         <code>null</code> if the URL contains no query string. The value is
   *         not decoded by the container.
   */
  default String getQueryString ()
  {
    return ServletHelper.getRequestQueryString (getRequest ());
  }

  /**
   * Returns the login of the user making this request, if the user has been
   * authenticated, or <code>null</code> if the user has not been authenticated.
   * Whether the user name is sent with each subsequent request depends on the
   * browser and type of authentication. Same as the value of the CGI variable
   * REMOTE_USER.
   *
   * @return a <code>String</code> specifying the login of the user making this
   *         request, or <code>null</code> if the user login is not known
   */
  default String getRemoteUser ()
  {
    return getRequest ().getRemoteUser ();
  }

  /**
   * Returns a boolean indicating whether the authenticated user is included in
   * the specified logical "role". Roles and role membership can be defined
   * using deployment descriptors. If the user has not been authenticated, the
   * method returns <code>false</code>.
   *
   * @param sRole
   *        a <code>String</code> specifying the name of the role
   * @return a <code>boolean</code> indicating whether the user making this
   *         request belongs to a given role; <code>false</code> if the user has
   *         not been authenticated
   */
  default boolean isUserInRole (final String sRole)
  {
    return getRequest ().isUserInRole (sRole);
  }

  /**
   * Returns a <code>java.security.Principal</code> object containing the name
   * of the current authenticated user. If the user has not been authenticated,
   * the method returns <code>null</code>.
   *
   * @return a <code>java.security.Principal</code> containing the name of the
   *         user making this request; <code>null</code> if the user has not
   *         been authenticated
   */
  @Nullable
  default Principal getUserPrincipal ()
  {
    return getRequest ().getUserPrincipal ();
  }

  /**
   * Returns the session ID specified by the client. This may not be the same as
   * the ID of the current valid session for this request. If the client did not
   * specify a session ID, this method returns <code>null</code>.
   *
   * @return a <code>String</code> specifying the session ID, or
   *         <code>null</code> if the request did not specify a session ID
   * @see #isRequestedSessionIdValid
   */
  @Nullable
  default String getRequestedSessionId ()
  {
    return getRequest ().getRequestedSessionId ();
  }

  /**
   * Returns the part of this request's URL from the protocol name up to the
   * query string in the first line of the HTTP request. The web container does
   * not decode this String. For example:
   * <table summary="Examples of Returned Values">
   * <tr align=left>
   * <th>First line of HTTP request</th>
   * <th>Returned Value</th>
   * </tr>
   * <tr>
   * <td>POST /some/path.html HTTP/1.1</td>
   * <td>/some/path.html</td>
   * </tr>
   * <tr>
   * <td>GET http://foo.bar/a.html HTTP/1.0</td>
   * <td>/a.html</td>
   * </tr>
   * <tr>
   * <td>HEAD /xyz?a=b HTTP/1.1</td>
   * <td>/xyz</td>
   * </tr>
   * </table>
   *
   * @return a <code>String</code> containing the part of the URL from the
   *         protocol name up to the query string
   */
  default String getRequestURI ()
  {
    return RequestHelper.getRequestURI (getRequest ());
  }

  /**
   * Reconstructs the URL the client used to make the request. The returned URL
   * contains a protocol, server name, port number, and server path, but it does
   * not include query string parameters.
   * <p>
   * If this request has been forwarded using
   * {@link javax.servlet.RequestDispatcher#forward}, the server path in the
   * reconstructed URL must reflect the path used to obtain the
   * RequestDispatcher, and not the server path specified by the client.
   * <p>
   * Because this method returns a <code>StringBuffer</code>, not a string, you
   * can modify the URL easily, for example, to append query parameters.
   * <p>
   * This method is useful for creating redirect messages and for reporting
   * errors.
   *
   * @return a <code>StringBuilder</code> object containing the reconstructed
   *         URL
   */
  default StringBuilder getRequestURL ()
  {
    return RequestHelper.getRequestURL (getRequest ());
  }

  /**
   * Returns the part of this request's URL that calls the servlet. This path
   * starts with a "/" character and includes either the servlet name or a path
   * to the servlet, but does not include any extra path information or a query
   * string. Same as the value of the CGI variable SCRIPT_NAME.
   * <p>
   * This method will return an empty string ("") if the servlet used to process
   * this request was matched using the "/*" pattern.
   *
   * @return a <code>String</code> containing the name or path of the servlet
   *         being called, as specified in the request URL, decoded, or an empty
   *         string if the servlet used to process the request is matched using
   *         the "/*" pattern.
   */
  @Nonnull
  default String getServletPath ()
  {
    return ServletHelper.getRequestServletPath (getRequest ());
  }

  /**
   * Returns the current <code>HttpSession</code> associated with this request
   * or, if there is no current session and <code>create</code> is true, returns
   * a new session.
   * <p>
   * If <code>bCreateIfNotExisting</code> is <code>false</code> and the request
   * has no valid <code>HttpSession</code>, this method returns
   * <code>null</code>.
   * <p>
   * To make sure the session is properly maintained, you must call this method
   * before the response is committed. If the container is using cookies to
   * maintain session integrity and is asked to create a new session when the
   * response is committed, an IllegalStateException is thrown.
   *
   * @param bCreateIfNotExisting
   *        <code>true</code> to create a new session for this request if
   *        necessary; <code>false</code> to return <code>null</code> if there's
   *        no current session
   * @return the <code>HttpSession</code> associated with this request or
   *         <code>null</code> if <code>bCreateIfNotExisting</code> is
   *         <code>false</code> and the request has no valid session
   */
  @Nullable
  default HttpSession getSession (final boolean bCreateIfNotExisting)
  {
    return getRequest ().getSession (bCreateIfNotExisting);
  }

  /**
   * Checks whether the requested session ID is still valid.
   * <p>
   * If the client did not specify any session ID, this method returns
   * <code>false</code>.
   *
   * @return <code>true</code> if this request has an id for a valid session in
   *         the current session context; <code>false</code> otherwise
   * @see #getRequestedSessionId
   * @see #getSession
   */
  default boolean isRequestedSessionIdValid ()
  {
    return getRequest ().isRequestedSessionIdValid ();
  }

  /**
   * Checks whether the requested session ID came in as a cookie.
   *
   * @return <code>true</code> if the session ID came in as a cookie; otherwise,
   *         <code>false</code>
   * @see #getSession
   */
  default boolean isRequestedSessionIdFromCookie ()
  {
    return getRequest ().isRequestedSessionIdFromCookie ();
  }

  /**
   * Checks whether the requested session ID came in as part of the request URL.
   *
   * @return <code>true</code> if the session ID came in as part of a URL;
   *         otherwise, <code>false</code>
   * @see #getSession
   */
  default boolean isRequestedSessionIdFromURL ()
  {
    return getRequest ().isRequestedSessionIdFromURL ();
  }

  // Extended API

  /**
   * @return Return the absolute server path. E.g. "http://localhost:8080"
   */
  @Nonnull
  default String getFullServerPath ()
  {
    return RequestHelper.getFullServerName (getRequest ()).toString ();
  }

  /**
   * @return Return the absolute context path. E.g.
   *         <code>http://localhost:8080/context</code>. Never with a trailing
   *         slash.
   * @see #getContextPath()
   */
  @Nonnull
  default String getFullContextPath ()
  {
    return RequestHelper.getFullServerName (getRequest ()).append (getContextPath ()).toString ();
  }

  /**
   * @return Return the absolute servlet path. E.g.
   *         <code>/context/config.jsp</code> or <code>/context/action/</code>
   */
  @Nonnull
  String getContextAndServletPath ();

  /**
   * @return Return the absolute servlet path. E.g.
   *         <code>http://localhost:8080/context/config.jsp</code> or
   *         <code>http://localhost:8080/context/action/</code>
   */
  @Nonnull
  String getFullContextAndServletPath ();

  /**
   * Get the full URI (excl. protocol and host) and parameters of the current
   * request. <br>
   * <code>/context/servlet/path/a/b?c=123&amp;d=789</code>
   *
   * @return The full URI of the current request.
   * @since 9.1.3
   */
  @Nonnull
  @Nonempty
  default String getURI ()
  {
    return (String) attrs ().computeIfAbsent (ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "req-uri",
                                              k -> RequestHelper.getURI (getRequest ()));
  }

  /**
   * Get the full URL (incl. protocol) and parameters of the current request.
   * <br>
   * <code>http://hostname.com:81/context/servlet/path/a/b?c=123&amp;d=789</code>
   *
   * @return The full URL of the current request.
   */
  @Nonnull
  @Nonempty
  default String getURL ()
  {
    return (String) attrs ().computeIfAbsent (ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "req-url",
                                              k -> RequestHelper.getURL (getRequest ()));
  }

  /**
   * Encodes the specified URL by including the session ID in it, or, if
   * encoding is not needed, returns the URL unchanged. The implementation of
   * this method includes the logic to determine whether the session ID needs to
   * be encoded in the URL. For example, if the browser supports cookies, or
   * session tracking is turned off, URL encoding is unnecessary.
   * <p>
   * For robust session tracking, all URLs emitted by a servlet should be run
   * through this method. Otherwise, URL rewriting cannot be used with browsers
   * which do not support cookies.
   *
   * @param sURL
   *        the url to be encoded. May not be <code>null</code>.
   * @return the encoded URL if encoding is needed; the unchanged URL otherwise.
   */
  @Nonnull
  String encodeURL (@Nonnull String sURL);

  /**
   * Encodes the specified URL by including the session ID in it, or, if
   * encoding is not needed, returns the URL unchanged. The implementation of
   * this method includes the logic to determine whether the session ID needs to
   * be encoded in the URL. For example, if the browser supports cookies, or
   * session tracking is turned off, URL encoding is unnecessary.
   * <p>
   * For robust session tracking, all URLs emitted by a servlet should be run
   * through this method. Otherwise, URL rewriting cannot be used with browsers
   * which do not support cookies.
   *
   * @param aURL
   *        the url to be encoded. May not be <code>null</code>.
   * @return the encoded URL if encoding is needed. Never <code>null</code>.
   */
  @Nonnull
  default ISimpleURL encodeURL (@Nonnull final ISimpleURL aURL)
  {
    ValueEnforcer.notNull (aURL, "URL");

    // Encode only the path and copy params and anchor
    return new SimpleURL (encodeURL (aURL.getPath ()), aURL.params (), aURL.getAnchor ());
  }

  /**
   * Encodes the specified URL for use in the <code>sendRedirect</code> method
   * or, if encoding is not needed, returns the URL unchanged. The
   * implementation of this method includes the logic to determine whether the
   * session ID needs to be encoded in the URL. Because the rules for making
   * this determination can differ from those used to decide whether to encode a
   * normal link, this method is separated from the <code>encodeURL</code>
   * method.
   * <p>
   * All URLs sent to the <code>HttpServletResponse.sendRedirect</code> method
   * should be run through this method. Otherwise, URL rewriting cannot be used
   * with browsers which do not support cookies.
   *
   * @param sURL
   *        the url to be encoded.
   * @return the encoded URL if encoding is needed; the unchanged URL otherwise.
   * @see #encodeURL(String)
   */
  @Nonnull
  String encodeRedirectURL (@Nonnull String sURL);

  /**
   * Encodes the specified URL for use in the <code>sendRedirect</code> method
   * or, if encoding is not needed, returns the URL unchanged. The
   * implementation of this method includes the logic to determine whether the
   * session ID needs to be encoded in the URL. Because the rules for making
   * this determination can differ from those used to decide whether to encode a
   * normal link, this method is separated from the <code>encodeURL</code>
   * method.
   * <p>
   * All URLs sent to the <code>HttpServletResponse.sendRedirect</code> method
   * should be run through this method. Otherwise, URL rewriting cannot be used
   * with browsers which do not support cookies.
   *
   * @param aURL
   *        the url to be encoded. May not be <code>null</code>.
   * @return the encoded URL if encoding is needed. Never <code>null</code>.
   * @see #encodeURL(String)
   */
  @Nonnull
  default ISimpleURL encodeRedirectURL (@Nonnull final ISimpleURL aURL)
  {
    ValueEnforcer.notNull (aURL, "URL");

    // Encode only the path and copy params and anchor
    return new SimpleURL (encodeRedirectURL (aURL.getPath ()), aURL.params (), aURL.getAnchor ());
  }

  /**
   * Check if this request uses a Cookie based session handling (meaning cookies
   * are enabled) or whether the session ID needs to be appended to a URL.
   *
   * @return <code>true</code> if the session ID is passed via cookies.
   */
  default boolean areCookiesEnabled ()
  {
    // Just check whether the session ID is appended to the URL or not
    return "a".equals (encodeURL ("a"));
  }

  /**
   * Return the URI of the request within the servlet context.
   *
   * @return the path within the web application and never <code>null</code>. By
   *         default "/" is returned is an empty request URI is determined.
   */
  @Nonnull
  default String getPathWithinServletContext ()
  {
    return RequestHelper.getPathWithinServletContext (getRequest ());
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
   * @return the path within the servlet mapping, or ""
   */
  @Nonnull
  default String getPathWithinServlet ()
  {
    return RequestHelper.getPathWithinServlet (getRequest ());
  }

  /**
   * @return The underlying HTTP servlet request object. Never <code>null</code>
   *         .
   */
  @Nonnull
  HttpServletRequest getRequest ();
}
