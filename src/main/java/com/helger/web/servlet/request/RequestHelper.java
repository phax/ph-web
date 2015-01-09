/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.servlet.request;

import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.SimpleURL;
import com.helger.commons.url.URLData;
import com.helger.commons.url.URLUtils;
import com.helger.web.CWeb;
import com.helger.web.http.CHTTPHeader;
import com.helger.web.http.EHTTPMethod;
import com.helger.web.http.EHTTPVersion;
import com.helger.web.http.HTTPHeaderMap;
import com.helger.web.port.CNetworkPort;
import com.helger.web.port.DefaultNetworkPorts;

/**
 * Misc. helper method on {@link HttpServletRequest} objects.
 *
 * @author Philip Helger
 */
@Immutable
public final class RequestHelper
{
  public static final String SERVLET_ATTR_SSL_CIPHER_SUITE = "javax.servlet.request.cipher_suite";
  public static final String SERVLET_ATTR_SSL_KEY_SIZE = "javax.servlet.request.key_size";
  public static final String SERVLET_ATTR_CLIENT_CERTIFICATE = "javax.servlet.request.X509Certificate";

  private static final String SCOPE_ATTR_REQUESTHELP_REQUESTPARAMMAP = "$requesthelp.requestparammap";
  private static final Logger s_aLogger = LoggerFactory.getLogger (RequestHelper.class);

  @PresentForCodeCoverage
  @SuppressWarnings ("unused")
  private static final RequestHelper s_aInstance = new RequestHelper ();

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
   *        The value to strip the session ID from
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
    return new SimpleURL (new URLData (getWithoutSessionID (aURL.getPath ()), aURL.getAllParams (), aURL.getAnchor ()));
  }

  /**
   * Get the request URI without an eventually appended session
   * (";jsessionid=...")
   * <table summary="Examples of Returned Values">
   * <tr align=left>
   * <th>First line of HTTP request</th>
   * <th>Returned Value</th>
   * <tr>
   * <td>POST /some/path.html;JSESSIONID=4711</td>
   * <td>/some/path.html</td>
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
   *        The HTTP request
   * @return The request URI without the optional session ID
   */
  @Nullable
  public static String getRequestURI (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sRequestURI = aHttpRequest.getRequestURI ();
    if (StringHelper.hasNoText (sRequestURI))
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

    final String sPathInfo = aHttpRequest.getPathInfo ();
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
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sRequestURI = getRequestURI (aHttpRequest);
    if (StringHelper.hasNoText (sRequestURI))
    {
      // I just want to to know whether we get null or ""
      s_aLogger.info ("Having empty request URI '" + sRequestURI + "' from request " + aHttpRequest);
      return "/";
    }

    // In some rare scenarios, Tomcat 7 may return null here!
    final String sContextPath = StringHelper.getNotNull (aHttpRequest.getContextPath (), "");
    if (!sRequestURI.startsWith (sContextPath))
      return sRequestURI;

    // Normal case: URI contains context path.
    final String sPath = sRequestURI.substring (sContextPath.length ());
    return sPath.length () > 0 ? sPath : "/";
  }

  /**
   * Return the path within the servlet mapping for the given request, i.e. the
   * part of the request's URL beyond the part that called the servlet, or "" if
   * the whole URL has been used to identify the servlet.
   * <p>
   * Detects include request URL if called within a RequestDispatcher include.
   * <p>
   * E.g.: servlet mapping = "/test/*"; request URI = "/test/a" -> "/a".
   * <p>
   * E.g.: servlet mapping = "/test"; request URI = "/test" -> "".
   * <p>
   * E.g.: servlet mapping = "/*.test"; request URI = "/a.test" -> "".
   *
   * @param aHttpRequest
   *        current HTTP request
   * @return the path within the servlet mapping, or ""
   */
  @Nonnull
  public static String getPathWithinServlet (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sPathWithinApp = getPathWithinServletContext (aHttpRequest);
    final String sServletPath = aHttpRequest.getServletPath ();
    if (sPathWithinApp.startsWith (sServletPath))
      return sPathWithinApp.substring (sServletPath.length ());

    // Special case: URI is different from servlet path.
    // Can happen e.g. with index page: URI="/", servletPath="/index.html"
    // Use servlet path in this case, as it indicates the actual target path.
    return sServletPath;
  }

  /**
   * Get the full URL (incl. protocol) and parameters of the passed request.<br>
   *
   * <pre>
   * http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123?d=789
   * </pre>
   *
   * @param aHttpRequest
   *        The request to use. May not be <code>null</code>.
   * @return The full URL.
   */
  @Nonnull
  @Nonempty
  public static String getURL (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final StringBuffer aReqUrl = aHttpRequest.getRequestURL ();
    final String sQueryString = aHttpRequest.getQueryString (); // d=789
    if (sQueryString != null)
      aReqUrl.append (URLUtils.QUESTIONMARK).append (sQueryString);
    return aReqUrl.toString ();
  }

  /**
   * Get the full URI (excl. protocol) and parameters of the passed request.<br>
   * Example:
   *
   * <pre>
   * /mywebapp/servlet/MyServlet/a/b;c=123?d=789
   * </pre>
   *
   * @param aHttpRequest
   *        The request to use. May not be <code>null</code>.
   * @return The full URI.
   */
  @Nonnull
  @Nonempty
  public static String getURI (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sReqUrl = getRequestURI (aHttpRequest);
    final String sQueryString = aHttpRequest.getQueryString (); // d=789&x=y
    if (StringHelper.hasText (sQueryString))
      return sReqUrl + URLUtils.QUESTIONMARK + sQueryString;
    return sReqUrl;
  }

  @CheckForSigned
  public static int getDefaultServerPort (@Nullable final String sScheme)
  {
    if (CWeb.SCHEME_HTTP.equalsIgnoreCase (sScheme))
      return CWeb.DEFAULT_PORT_HTTP;
    if (CWeb.SCHEME_HTTPS.equalsIgnoreCase (sScheme))
      return CWeb.DEFAULT_PORT_HTTPS;
    return CNetworkPort.INVALID_PORT_NUMBER;
  }

  @CheckForSigned
  public static int getServerPortToUse (@Nonnull final String sScheme, @CheckForSigned final int nServerPort)
  {
    // URL.getPort() delivers -1 for unspecified ports
    if (!DefaultNetworkPorts.isValidPort (nServerPort))
      return getDefaultServerPort (sScheme);
    return nServerPort;
  }

  @Nonnull
  @Nonempty
  public static StringBuilder getFullServerName (@Nonnull final String sScheme,
                                                 @Nonnull final String sServerName,
                                                 final int nServerPort)
  {
    ValueEnforcer.notNull (sScheme, "Scheme");
    ValueEnforcer.notNull (sServerName, "ServerName");

    // Reconstruct URL
    final StringBuilder aSB = new StringBuilder (sScheme).append ("://").append (sServerName);
    if (DefaultNetworkPorts.isValidPort (nServerPort) && nServerPort != getDefaultServerPort (sScheme))
      aSB.append (':').append (nServerPort);
    return aSB;
  }

  @Nonnull
  @Nonempty
  public static String getFullServerNameAndPath (@Nonnull final String sScheme,
                                                 @Nonnull final String sServerName,
                                                 final int nServerPort,
                                                 @Nullable final String sPath,
                                                 @Nullable final String sQueryString)
  {
    final StringBuilder aURL = getFullServerName (sScheme, sServerName, nServerPort);
    if (StringHelper.hasText (sPath))
    {
      if (!sPath.startsWith ("/", 0))
        aURL.append ('/');
      aURL.append (sPath);
    }
    if (StringHelper.hasText (sQueryString))
      aURL.append (URLUtils.QUESTIONMARK).append (sQueryString);
    return aURL.toString ();
  }

  @Nullable
  public static String getHttpReferer (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return aHttpRequest.getHeader (CHTTPHeader.REFERER);
  }

  /**
   * Get the HTTP version associated with the given HTTP request
   *
   * @param aHttpRequest
   *        The http request to query. May not be <code>null</code>.
   * @return <code>null</code> if no supported HTTP version is contained
   */
  @Nullable
  public static EHTTPVersion getHttpVersion (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sProtocol = aHttpRequest.getProtocol ();
    return EHTTPVersion.getFromNameOrNull (sProtocol);
  }

  /**
   * Get the HTTP method associated with the given HTTP request
   *
   * @param aHttpRequest
   *        The http request to query. May not be <code>null</code>.
   * @return <code>null</code> if no supported HTTP method is contained
   */
  @Nullable
  public static EHTTPMethod getHttpMethod (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sMethod = aHttpRequest.getMethod ();
    return EHTTPMethod.getFromNameOrNull (sMethod);
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
  public static HTTPHeaderMap getRequestHeaderMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final HTTPHeaderMap ret = new HTTPHeaderMap ();
    final Enumeration <?> eHeaders = aHttpRequest.getHeaderNames ();
    while (eHeaders.hasMoreElements ())
    {
      final String sName = (String) eHeaders.nextElement ();
      final Enumeration <?> eHeaderValues = aHttpRequest.getHeaders (sName);
      while (eHeaderValues.hasMoreElements ())
      {
        final String sValue = (String) eHeaderValues.nextElement ();
        ret.addHeader (sName, sValue);
      }
    }
    return ret;
  }

  /**
   * This is a utility method which avoids that all map values are enclosed in
   * an array. Jetty seems to create String arrays out of simple string values
   *
   * @param aHttpRequest
   *        The source HTTP request. May not be <code>null</code>.
   * @return A Map containing pure strings instead of string arrays with one
   *         item
   */
  @Nonnull
  @ReturnsMutableCopy
  public static Map <String, Object> getParameterMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final Map <String, Object> aResult = new HashMap <String, Object> ();
    @SuppressWarnings ("unchecked")
    final Map <String, Object> aOriginalMap = aHttpRequest.getParameterMap ();

    // For all parameters
    for (final Map.Entry <String, Object> aEntry : aOriginalMap.entrySet ())
    {
      final String sKey = aEntry.getKey ();
      final Object aValue = aEntry.getValue ();
      if (aValue instanceof String [])
      {
        // It's an array value
        final String [] aArrayValue = (String []) aValue;
        if (aArrayValue.length > 1)
          aResult.put (sKey, aArrayValue);
        else
          if (aArrayValue.length == 1)
          {
            // Flatten array to String
            aResult.put (sKey, aArrayValue[0]);
          }
          else
            aResult.put (sKey, "");
      }
      else
      {
        // It's a single value
        aResult.put (sKey, aValue);
      }
    }
    return aResult;
  }

  @Nonnull
  public static IRequestParamMap getRequestParamMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    // Check if a value is cached in the HTTP request
    IRequestParamMap aValue = (IRequestParamMap) aHttpRequest.getAttribute (SCOPE_ATTR_REQUESTHELP_REQUESTPARAMMAP);
    if (aValue == null)
    {
      aValue = RequestParamMap.create (getParameterMap (aHttpRequest));
      aHttpRequest.setAttribute (SCOPE_ATTR_REQUESTHELP_REQUESTPARAMMAP, aValue);
    }
    return aValue;
  }

  /**
   * Get the content length of the passed request. This is not done using
   * <code>request.getContentLength()</code> but instead parsing the HTTP header
   * field {@link CHTTPHeader#CONTENT_LENGTH} manually!
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

    final String sContentLength = aHttpRequest.getHeader (CHTTPHeader.CONTENT_LENGTH);
    return StringParser.parseLong (sContentLength, -1L);
  }

  /**
   * Get all request headers of the passed request in a correctly typed
   * {@link Enumeration}.
   *
   * @param aHttpRequest
   *        Source HTTP request. May not be <code>null</code>.
   * @param sName
   *        Name of the request header to retrieve.
   * @return Never <code>null</code>.
   */
  @Nullable
  public static Enumeration <String> getRequestHeaders (@Nonnull final HttpServletRequest aHttpRequest,
                                                        @Nullable final String sName)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return GenericReflection.<Enumeration <?>, Enumeration <String>> uncheckedCast (aHttpRequest.getHeaders (sName));
  }

  /**
   * Get all all request header names of the passed request in a correctly typed
   * {@link Enumeration}.
   *
   * @param aHttpRequest
   *        Source HTTP request. May not be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @Nullable
  public static Enumeration <String> getRequestHeaderNames (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    return GenericReflection.<Enumeration <?>, Enumeration <String>> uncheckedCast (aHttpRequest.getHeaderNames ());
  }

  @Nullable
  private static <T> T _getRequestAttr (@Nonnull final HttpServletRequest aHttpRequest,
                                        @Nonnull @Nonempty final String sAttrName,
                                        @Nonnull final Class <T> aDstClass)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final Object aValue = aHttpRequest.getAttribute (sAttrName);
    if (aValue == null)
    {
      // No client certificates present
      return null;
    }

    // type check
    if (!aDstClass.isAssignableFrom (aValue.getClass ()))
    {
      s_aLogger.error ("Request attribute " +
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
}
