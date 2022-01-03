/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.string.StringHelper;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.annotation.IsOffline;

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

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, String> getRequestFieldMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    if (IS_OFFLINE_CACHE.hasAnnotation (aHttpRequest))
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
      ret.put ("ContextPath", ServletHelper.getRequestContextPath (aHttpRequest));
      ret.put ("ContextPath2", ServletContextPathHolder.getContextPathOrNull ());
      ret.put ("LocalAddr", aHttpRequest.getLocalAddr ());
      ret.put ("LocalName", aHttpRequest.getLocalName ());
      ret.put ("LocalPort", Integer.toString (aHttpRequest.getLocalPort ()));
      ret.put ("Method", aHttpRequest.getMethod ());
      ret.put ("PathInfo", ServletHelper.getRequestPathInfo (aHttpRequest));
      ret.put ("PathInfo2", RequestHelper.getPathInfo (aHttpRequest));
      ret.put ("PathTranslated", aHttpRequest.getPathTranslated ());
      ret.put ("Protocol", aHttpRequest.getProtocol ());
      ret.put ("QueryString", ServletHelper.getRequestQueryString (aHttpRequest));
      ret.put ("RemoteAddr", aHttpRequest.getRemoteAddr ());
      ret.put ("RemoteHost", aHttpRequest.getRemoteHost ());
      ret.put ("RemotePort", Integer.toString (aHttpRequest.getRemotePort ()));
      ret.put ("RemoteUser", aHttpRequest.getRemoteUser ());
      ret.put ("RequestedSessionId", aHttpRequest.getRequestedSessionId ());
      ret.put ("RequestURI", ServletHelper.getRequestRequestURI (aHttpRequest));
      ret.put ("RequestURI2", RequestHelper.getRequestURIDecoded (aHttpRequest));
      ret.put ("RequestURI3", RequestHelper.getRequestURIEncoded (aHttpRequest));
      ret.put ("RequestURL", ServletHelper.getRequestRequestURL (aHttpRequest).toString ());
      ret.put ("RequestURL2", RequestHelper.getRequestURLDecoded (aHttpRequest).toString ());
      ret.put ("RequestURL3", RequestHelper.getRequestURLEncoded (aHttpRequest).toString ());
      ret.put ("Scheme", aHttpRequest.getScheme ());
      ret.put ("ServerName", aHttpRequest.getServerName ());
      ret.put ("ServerPort", Integer.toString (aHttpRequest.getServerPort ()));
      ret.put ("ServletPath", ServletHelper.getRequestServletPath (aHttpRequest));
    }
    final HttpSession aSession = aHttpRequest.getSession (false);
    if (aSession != null)
      ret.put ("SessionID", aSession.getId ());
    return ret;
  }

  public static void debugAppendRequestFields (@Nonnull final Map <String, String> aRequestFieldMap, @Nonnull final StringBuilder aSB)
  {
    aSB.append ("Request:\n");
    for (final Map.Entry <String, String> aEntry : aRequestFieldMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = ").append (aEntry.getValue ()).append ('\n');
  }

  public static void debugAppendRequestHeader (@Nonnull final HttpHeaderMap aRequestHeaderMap, @Nonnull final StringBuilder aSB)
  {
    aSB.append ("Headers:\n");
    aRequestHeaderMap.forEachHeaderLine (x -> aSB.append ("  ").append (x).append ('\n'), true);
  }

  @Nonnull
  public static ICommonsOrderedMap <String, String> getRequestParameterMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    for (final Map.Entry <String, String []> aEntry : CollectionHelper.getSortedByKey (aHttpRequest.getParameterMap ()).entrySet ())
      ret.put (aEntry.getKey (), StringHelper.getImploded (", ", aEntry.getValue ()));
    return ret;
  }

  public static void debugAppendRequestParameters (@Nonnull final Map <String, String> aRequestParameterMap,
                                                   @Nonnull final StringBuilder aSB)
  {
    aSB.append ("Request parameters:\n");
    for (final Map.Entry <String, String> aEntry : aRequestParameterMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = '").append (aEntry.getValue ()).append ("'\n");
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
    if (StringHelper.hasText (aCookie.getComment ()))
      aSB.append (" [comment=").append (aCookie.getComment ()).append (']');
    if (aCookie.isHttpOnly ())
      aSB.append (" [http-only]");
    return aSB.toString ();
  }

  public static void debugAppendRequestCookies (@Nonnull final HttpServletRequest aHttpRequest, @Nonnull final StringBuilder aSB)
  {
    aSB.append ("Cookies:\n");
    final Cookie [] aCookies = ServletHelper.getRequestCookies (aHttpRequest);
    if (aCookies != null)
      for (final Cookie aCookie : aCookies)
        aSB.append ("  ").append (aCookie.getName ()).append (" = ").append (getCookieValue (aCookie)).append ('\n');
  }

  @Nonnull
  public static StringBuilder getRequestDebugString (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final StringBuilder aSB = new StringBuilder ();
    debugAppendRequestFields (getRequestFieldMap (aHttpRequest), aSB);
    debugAppendRequestHeader (RequestHelper.getRequestHeaderMap (aHttpRequest), aSB);
    debugAppendRequestParameters (getRequestParameterMap (aHttpRequest), aSB);
    debugAppendRequestCookies (aHttpRequest, aSB);
    return aSB;
  }

  public static void logRequestComplete (@Nonnull final HttpServletRequest aHttpRequest)
  {
    if (LOGGER.isInfoEnabled ())
      LOGGER.info (getRequestDebugString (aHttpRequest).toString ());
  }
}
