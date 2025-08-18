/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.annotation.IsOffline;

import jakarta.annotation.Nonnull;
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
      try
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
        ret.put ("Method", ServletHelper.getRequestMethod (aHttpRequest));
        ret.put ("PathInfo", ServletHelper.getRequestPathInfo (aHttpRequest));
        ret.put ("PathInfo2", RequestHelper.getPathInfo (aHttpRequest));
        ret.put ("PathTranslated", aHttpRequest.getPathTranslated ());
        ret.put ("Protocol", ServletHelper.getRequestProtocol (aHttpRequest));
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
        ret.put ("Scheme", ServletHelper.getRequestScheme (aHttpRequest));
        ret.put ("ServerName", ServletHelper.getRequestServerName (aHttpRequest));
        ret.put ("ServerPort", Integer.toString (ServletHelper.getRequestServerPort (aHttpRequest)));
        ret.put ("ServletPath", ServletHelper.getRequestServletPath (aHttpRequest));
      }
      catch (final RuntimeException ex)
      {
        ret.put ("InternalErrorClass", ex.getClass ().getName ());
        ret.put ("InternalErrorMessage", ex.getMessage ());
      }
    }

    final HttpSession aSession = ServletHelper.getRequestSession (aHttpRequest, false);
    if (aSession != null)
      ret.put ("SessionID", aSession.getId ());
    return ret;
  }

  public static void debugAppendRequestFields (@Nonnull final Map <String, String> aRequestFieldMap,
                                               @Nonnull final StringBuilder aSB)
  {
    aSB.append ("Request:\n");
    for (final Map.Entry <String, String> aEntry : aRequestFieldMap.entrySet ())
      aSB.append ("  ").append (aEntry.getKey ()).append (" = ").append (aEntry.getValue ()).append ('\n');
  }

  public static void debugAppendRequestHeader (@Nonnull final HttpHeaderMap aRequestHeaderMap,
                                               @Nonnull final StringBuilder aSB)
  {
    aSB.append ("Headers:\n");
    aRequestHeaderMap.forEachHeaderLine (x -> aSB.append ("  ").append (x).append ('\n'), true);
  }

  @Nonnull
  public static ICommonsOrderedMap <String, String> getRequestParameterMap (@Nonnull final HttpServletRequest aHttpRequest)
  {
    final ICommonsOrderedMap <String, String> ret = new CommonsLinkedHashMap <> ();
    try
    {
      for (final Map.Entry <String, String []> aEntry : CollectionSort.getSortedByKey (aHttpRequest.getParameterMap ())
                                                                      .entrySet ())
        ret.put (aEntry.getKey (), StringImplode.getImploded (", ", aEntry.getValue ()));
    }
    catch (final RuntimeException ex)
    {
      // Ignore
    }
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
    if (StringHelper.isNotEmpty (aCookie.getDomain ()))
      aSB.append (" [domain=").append (aCookie.getDomain ()).append (']');
    aSB.append (" [maxage=").append (aCookie.getMaxAge ()).append (']');
    if (StringHelper.isNotEmpty (aCookie.getPath ()))
      aSB.append (" [path=").append (aCookie.getPath ()).append (']');
    if (aCookie.getSecure ())
      aSB.append (" [secure]");
    aSB.append (" [version=").append (aCookie.getVersion ()).append (']');
    if (StringHelper.isNotEmpty (aCookie.getComment ()))
      aSB.append (" [comment=").append (aCookie.getComment ()).append (']');
    if (aCookie.isHttpOnly ())
      aSB.append (" [http-only]");
    return aSB.toString ();
  }

  public static void debugAppendRequestCookies (@Nonnull final HttpServletRequest aHttpRequest,
                                                @Nonnull final StringBuilder aSB)
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
    LOGGER.info (getRequestDebugString (aHttpRequest).toString ());
  }
}
