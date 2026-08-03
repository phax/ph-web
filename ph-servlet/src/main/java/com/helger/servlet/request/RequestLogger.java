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
package com.helger.servlet.request;

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.cache.clazz.AnnotationUsageCache;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.collection.helper.CollectionSort;
import com.helger.http.header.HttpHeaderMap;
import com.helger.servlet.SafeHttpServletRequest;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.annotation.IsOffline;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Helper class to debug information passed to a JSP page or a servlet.
 *
 * @author Philip Helger
 */
@Immutable
public final class RequestLogger
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RequestLogger.class);

  private static final AnnotationUsageCache IS_OFFLINE_CACHE = new AnnotationUsageCache (IsOffline.class);

  @PresentForCodeCoverage
  private static final RequestLogger INSTANCE = new RequestLogger ();

  private RequestLogger ()
  {}

  @NonNull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, String> getRequestFieldMap (@NonNull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final SafeHttpServletRequest aSafeHttpRequest = SafeHttpServletRequest.wrap (aHttpRequest);

    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    // Note: use the original request here, because the offline marker annotation
    // is bound to its concrete class (a wrapper would hide it)
    if (IS_OFFLINE_CACHE.hasAnnotation (aSafeHttpRequest.getRequest ()))
    {
      // Special handling, because otherwise exceptions would be thrown
      ret.put ("Offline", "true");
    }
    else
    {
      try
      {
        ret.put ("AuthType", aSafeHttpRequest.getAuthType ());
        ret.put ("CharacterEncoding", aSafeHttpRequest.getCharacterEncoding ());
        ret.put ("ContentLength", Long.toString (aSafeHttpRequest.getContentLength ()));
        ret.put ("ContentType", aSafeHttpRequest.getContentType ());
        ret.put ("ContextPath", aSafeHttpRequest.getContextPath ());
        ret.put ("ContextPath2", ServletContextPathHolder.getContextPathOrNull ());
        ret.put ("LocalAddr", aSafeHttpRequest.getLocalAddr ());
        ret.put ("LocalName", aSafeHttpRequest.getLocalName ());
        ret.put ("LocalPort", Integer.toString (aSafeHttpRequest.getLocalPort ()));
        ret.put ("Method", aSafeHttpRequest.getMethod ());
        ret.put ("PathInfo", aSafeHttpRequest.getPathInfo ());
        ret.put ("PathInfo2", RequestHelper.getPathInfo (aHttpRequest));
        ret.put ("PathTranslated", aSafeHttpRequest.getPathTranslated ());
        ret.put ("Protocol", aSafeHttpRequest.getProtocol ());
        ret.put ("QueryString", aSafeHttpRequest.getQueryString ());
        ret.put ("RemoteAddr", aSafeHttpRequest.getRemoteAddr ());
        ret.put ("RemoteHost", aSafeHttpRequest.getRemoteHost ());
        ret.put ("RemotePort", Integer.toString (aSafeHttpRequest.getRemotePort ()));
        ret.put ("RemoteUser", aSafeHttpRequest.getRemoteUser ());
        ret.put ("RequestedSessionId", aSafeHttpRequest.getRequestedSessionId ());
        ret.put ("RequestURI", aSafeHttpRequest.getRequestURI ());
        ret.put ("RequestURI2", RequestHelper.getRequestURIDecoded (aHttpRequest));
        ret.put ("RequestURI3", RequestHelper.getRequestURIEncoded (aHttpRequest));
        ret.put ("RequestURL", aSafeHttpRequest.getRequestURL ().toString ());
        ret.put ("RequestURL2", RequestHelper.getRequestURLDecoded (aHttpRequest).toString ());
        ret.put ("RequestURL3", RequestHelper.getRequestURLEncoded (aHttpRequest).toString ());
        ret.put ("Scheme", aSafeHttpRequest.getScheme ());
        ret.put ("ServerName", aSafeHttpRequest.getServerName ());
        ret.put ("ServerPort", Integer.toString (aSafeHttpRequest.getServerPort ()));
        ret.put ("ServletPath", aSafeHttpRequest.getServletPath ());
      }
      catch (final RuntimeException ex)
      {
        ret.put ("InternalErrorClass", ex.getClass ().getName ());
        ret.put ("InternalErrorMessage", ex.getMessage ());
      }
    }

    final HttpSession aSession = aSafeHttpRequest.getSession (false);
    if (aSession != null)
      ret.put ("SessionID", aSession.getId ());
    return ret;
  }

  public static void debugAppendRequestFields (@NonNull final Map <String, String> aRequestFieldMap,
                                               @NonNull final StringBuilder aSB)
  {
    aSB.append ("Request:\n");
    for (final Map.Entry <String, String> aEntry : aRequestFieldMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = ").append (aEntry.getValue ()).append ('\n');
  }

  public static void debugAppendRequestHeader (@NonNull final HttpHeaderMap aRequestHeaderMap,
                                               @NonNull final StringBuilder aSB)
  {
    aSB.append ("Headers:\n");
    aRequestHeaderMap.forEachHeaderLine (x -> aSB.append ("  ").append (x).append ('\n'), true);
  }

  @NonNull
  public static ICommonsOrderedMap <String, String> getRequestParameterMap (@NonNull final HttpServletRequest aHttpRequest)
  {
    final SafeHttpServletRequest aSafeHttpRequest = SafeHttpServletRequest.wrap (aHttpRequest);

    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    for (final var aEntry : CollectionSort.getSortedByKey (aSafeHttpRequest.getParameterMap ()).entrySet ())
      ret.put (aEntry.getKey (), StringImplode.imploder ().separator (", ").source (aEntry.getValue ()).build ());
    return ret;
  }

  public static void debugAppendRequestParameters (@NonNull final Map <String, String> aRequestParameterMap,
                                                   @NonNull final StringBuilder aSB)
  {
    aSB.append ("Request parameters:\n");
    for (final Map.Entry <String, String> aEntry : aRequestParameterMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = '").append (aEntry.getValue ()).append ("'\n");
  }

  @NonNull
  public static String getCookieValue (@NonNull final Cookie aCookie)
  {
    final StringBuilder aSB = new StringBuilder ();
    aSB.append (aCookie.getValue ());
    if (StringHelper.isNotEmpty (aCookie.getDomain ()))
      aSB.append (" [domain=").append (aCookie.getDomain ()).append (']');
    aSB.append (" [maxage=").append (aCookie.getMaxAge ()).append (']');
    if (StringHelper.isNotEmpty (aCookie.getPath ()))
      aSB.append (" [path=").append (aCookie.getPath ()).append (']');
    if (aCookie.getSecure ())
      aSB.append (" [secure]");
    if (aCookie.isHttpOnly ())
      aSB.append (" [http-only]");
    return aSB.toString ();
  }

  public static void debugAppendRequestCookies (@NonNull final HttpServletRequest aHttpRequest,
                                                @NonNull final StringBuilder aSB)
  {
    aSB.append ("Cookies:\n");
    final SafeHttpServletRequest aSafeHttpRequest = SafeHttpServletRequest.wrap (aHttpRequest);
    final Cookie [] aCookies = aSafeHttpRequest.getCookies ();
    if (aCookies != null)
      for (final Cookie aCookie : aCookies)
        aSB.append ("  ").append (aCookie.getName ()).append (" = ").append (getCookieValue (aCookie)).append ('\n');
  }

  @NonNull
  public static StringBuilder getRequestDebugString (@NonNull final HttpServletRequest aHttpRequest)
  {
    final StringBuilder aSB = new StringBuilder ();
    debugAppendRequestFields (getRequestFieldMap (aHttpRequest), aSB);
    debugAppendRequestHeader (RequestHelper.getRequestHeaderMap (aHttpRequest), aSB);
    debugAppendRequestParameters (getRequestParameterMap (aHttpRequest), aSB);
    debugAppendRequestCookies (aHttpRequest, aSB);
    return aSB;
  }

  public static void logRequestComplete (@NonNull final HttpServletRequest aHttpRequest)
  {
    LOGGER.info (getRequestDebugString (aHttpRequest).toString ());
  }
}
