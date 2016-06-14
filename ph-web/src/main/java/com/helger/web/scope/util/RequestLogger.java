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
package com.helger.web.scope.util;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.cache.AnnotationUsageCache;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsOrderedMap;
import com.helger.commons.string.StringHelper;
import com.helger.web.annotation.IsOffline;
import com.helger.web.http.HTTPHeaderMap;
import com.helger.web.servlet.ServletContextPathHolder;

/**
 * Helper class to debug information passed to a JSP page or a servlet.
 *
 * @author Philip Helger
 */
@Immutable
public final class RequestLogger
{
  /** The logger to use. */
  private static final Logger s_aLogger = LoggerFactory.getLogger (RequestLogger.class);

  private static final AnnotationUsageCache s_aOfflineCache = new AnnotationUsageCache (IsOffline.class);

  @PresentForCodeCoverage
  private static final RequestLogger s_aInstance = new RequestLogger ();

  private RequestLogger ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, String> getRequestFieldMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    if (s_aOfflineCache.hasAnnotation (aHttpRequest))
    {
      // Special handling, because otherwise exceptions would be thrown
      ret.put ("Offline", "true");
    }
    else
    {
      ret.put ("AuthType", aHttpRequest.getAuthType ());
      ret.put ("CharacterEncoding", aHttpRequest.getCharacterEncoding ());
      ret.put ("ContentLength", Long.toString (RequestHelper.getContentLength (aHttpRequest)));
      ret.put ("ContentType", aHttpRequest.getContentType ());
      ret.put ("ContextPath", aHttpRequest.getContextPath ());
      try
      {
        ret.put ("ContextPath2", ServletContextPathHolder.getContextPath ());
      }
      catch (final IllegalStateException ex)
      {
        // No global web scope available
      }
      ret.put ("LocalAddr", aHttpRequest.getLocalAddr ());
      ret.put ("LocalName", aHttpRequest.getLocalName ());
      ret.put ("LocalPort", Integer.toString (aHttpRequest.getLocalPort ()));
      ret.put ("Method", aHttpRequest.getMethod ());
      ret.put ("PathInfo", aHttpRequest.getPathInfo ());
      ret.put ("PathInfo2", RequestHelper.getPathInfo (aHttpRequest));
      ret.put ("PathTranslated", aHttpRequest.getPathTranslated ());
      ret.put ("Protocol", aHttpRequest.getProtocol ());
      ret.put ("QueryString", aHttpRequest.getQueryString ());
      ret.put ("RemoteAddr", aHttpRequest.getRemoteAddr ());
      ret.put ("RemoteHost", aHttpRequest.getRemoteHost ());
      ret.put ("RemotePort", Integer.toString (aHttpRequest.getRemotePort ()));
      ret.put ("RemoteUser", aHttpRequest.getRemoteUser ());
      ret.put ("RequestedSessionId", aHttpRequest.getRequestedSessionId ());
      ret.put ("RequestURI", aHttpRequest.getRequestURI ());
      try
      {
        ret.put ("RequestURI2", RequestHelper.getRequestURI (aHttpRequest));
      }
      catch (final IllegalStateException ex)
      {
        // No global web scope available
      }
      ret.put ("RequestURL", aHttpRequest.getRequestURL ().toString ());
      try
      {
        ret.put ("RequestURL2", RequestHelper.getRequestURL (aHttpRequest).toString ());
      }
      catch (final IllegalStateException ex)
      {
        // No global web scope available
      }
      ret.put ("Scheme", aHttpRequest.getScheme ());
      ret.put ("ServerName", aHttpRequest.getServerName ());
      ret.put ("ServerPort", Integer.toString (aHttpRequest.getServerPort ()));
      ret.put ("ServletPath", aHttpRequest.getServletPath ());
    }
    final HttpSession aSession = aHttpRequest.getSession (false);
    if (aSession != null)
      ret.put ("SessionID", aSession.getId ());
    return ret;
  }

  @Nonnull
  public static StringBuilder getRequestFields (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return getRequestFields (getRequestFieldMap (aHttpRequest));
  }

  @Nonnull
  public static StringBuilder getRequestFields (@Nonnull final Map <String, String> aRequestFieldMap)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append ("Request:\n");
    for (final Map.Entry <String, String> aEntry : aRequestFieldMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = ").append (aEntry.getValue ()).append ('\n');
    return aSB;
  }

  /**
   * Debug log the most interesting parts of the HTTP request. It logs
   * everything but the HTTP headers.
   *
   * @param aHttpRequest
   *        the {@link HttpServletRequest} to debug
   */
  public static void logRequestFields (@Nonnull final HttpServletRequest aHttpRequest)
  {
    s_aLogger.info (getRequestFields (aHttpRequest).toString ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, String> getHTTPHeaderMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return getHTTPHeaderMap (RequestHelper.getRequestHeaderMap (aHttpRequest));
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, String> getHTTPHeaderMap (@Nonnull final HTTPHeaderMap aMap)
  {
    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    for (final Map.Entry <String, ICommonsList <String>> aEntry : aMap)
    {
      final String sName = aEntry.getKey ();
      final ICommonsList <String> aValue = aEntry.getValue ();
      if (aValue.size () == 1)
        ret.put (sName, aValue.getFirst ());
      else
        ret.put (sName, aValue.toString ());
    }
    return ret;
  }

  @Nonnull
  public static StringBuilder getRequestHeader (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return getRequestHeader (getHTTPHeaderMap (aHttpRequest));
  }

  @Nonnull
  public static StringBuilder getRequestHeader (@Nonnull final Map <String, String> aRequestHeaderMap)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append ("Headers:\n");
    for (final Map.Entry <String, String> aEntry : aRequestHeaderMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = ").append (aEntry.getValue ()).append ('\n');
    return aSB;
  }

  /**
   * Debug log information about the HTTP header send with a
   * {@link HttpServletRequest}.
   *
   * @param aHttpRequest
   *        the servlet request to debug the HTTP headers from
   */
  public static void logRequestHeader (@Nonnull final HttpServletRequest aHttpRequest)
  {
    s_aLogger.info (getRequestHeader (aHttpRequest).toString ());
  }

  @Nonnull
  public static ICommonsOrderedMap <String, String> getRequestParameterMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    for (final Map.Entry <String, String []> aEntry : CollectionHelper.getSortedByKey (aHttpRequest.getParameterMap ())
                                                                      .entrySet ())
      ret.put (aEntry.getKey (), StringHelper.getImploded (", ", aEntry.getValue ()));
    return ret;
  }

  @Nonnull
  public static StringBuilder getRequestParameters (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return getRequestParameters (getRequestParameterMap (aHttpRequest));
  }

  @Nonnull
  public static StringBuilder getRequestParameters (@Nonnull final Map <String, String> aRequestParameterMap)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append ("Request parameters:\n");
    for (final Map.Entry <String, String> aEntry : aRequestParameterMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = '").append (aEntry.getValue ()).append ("'\n");
    return aSB;
  }

  public static void logRequestParameters (@Nonnull final HttpServletRequest aHttpRequest)
  {
    s_aLogger.info (getRequestParameters (aHttpRequest).toString ());
  }

  @Nonnull
  public static String getCookieValue (@Nonnull final Cookie aCookie)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append (aCookie.getValue ());
    if (StringHelper.hasText (aCookie.getDomain ()))
      aSB.append (" [domain=").append (aCookie.getDomain ()).append (']');
    aSB.append (" [maxage=").append (aCookie.getMaxAge ()).append (']');
    if (StringHelper.hasText (aCookie.getPath ()))
      aSB.append (" [path=").append (aCookie.getPath ()).append (']');
    if (aCookie.getSecure ())
      aSB.append (" [secure]");
    aSB.append (" [version=").append (aCookie.getVersion ()).append (']');
    return aSB.toString ();
  }

  @Nonnull
  public static StringBuilder getRequestCookies (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append ("Cookies:\n");
    final Cookie [] aCookies = aHttpRequest.getCookies ();
    if (aCookies != null)
      for (final Cookie aCookie : aCookies)
        aSB.append ("  ").append (aCookie.getName ()).append (" = ").append (getCookieValue (aCookie)).append ('\n');
    return aSB;
  }

  public static void logRequestCookies (@Nonnull final HttpServletRequest aHttpRequest)
  {
    s_aLogger.info (getRequestCookies (aHttpRequest).toString ());
  }

  @Nonnull
  public static StringBuilder getRequestComplete (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append (getRequestFields (aHttpRequest));
    aSB.append (getRequestHeader (aHttpRequest));
    aSB.append (getRequestParameters (aHttpRequest));
    aSB.append (getRequestCookies (aHttpRequest));
    return aSB;
  }

  public static void logRequestComplete (@Nonnull final HttpServletRequest aHttpRequest)
  {
    s_aLogger.info (getRequestComplete (aHttpRequest).toString ());
  }
}
