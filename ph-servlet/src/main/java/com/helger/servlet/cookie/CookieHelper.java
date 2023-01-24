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
package com.helger.servlet.cookie;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.string.StringHelper;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletHelper;

/**
 * Misc. helper methods on HTTP cookies.
 *
 * @author Philip Helger
 */
@Immutable
public final class CookieHelper
{
  public static final int DEFAULT_MAX_AGE_SECONDS = 30 * CGlobal.SECONDS_PER_DAY;

  @PresentForCodeCoverage
  private static final CookieHelper INSTANCE = new CookieHelper ();

  private CookieHelper ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, Cookie> getAllCookies (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final ICommonsOrderedMap <String, Cookie> ret = new CommonsLinkedHashMap <> ();
    ret.putAllMapped (ServletHelper.getRequestCookies (aHttpRequest), Cookie::getName, Function.identity ());
    return ret;
  }

  @Nullable
  public static Cookie getCookie (@Nonnull final HttpServletRequest aHttpRequest, @Nonnull final String sCookieName)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");
    ValueEnforcer.notNull (sCookieName, "CookieName");

    final Cookie [] aCookies = ServletHelper.getRequestCookies (aHttpRequest);
    if (aCookies != null)
      for (final Cookie aCookie : aCookies)
        if (aCookie.getName ().equals (sCookieName))
          return aCookie;
    return null;
  }

  public static boolean containsCookie (@Nonnull final HttpServletRequest aHttpRequest,
                                        @Nonnull final String sCookieName)
  {
    return getCookie (aHttpRequest, sCookieName) != null;
  }

  /**
   * Create a cookie that is bound on a certain path within the local web
   * server.
   *
   * @param sName
   *        The cookie name.
   * @param sValue
   *        The cookie value.
   * @param sPath
   *        The path the cookie is valid for.
   * @param bExpireWhenBrowserIsClosed
   *        <code>true</code> if this is a browser session cookie
   * @param bSecure
   *        <code>true</code> to send the cookie from the browser to the server
   *        only when using a secure protocol (e.g. https)
   * @return The created cookie object.
   * @since 9.3.2
   */
  @Nonnull
  public static Cookie createCookie (@Nonnull final String sName,
                                     @Nullable final String sValue,
                                     final String sPath,
                                     final boolean bExpireWhenBrowserIsClosed,
                                     final boolean bSecure)
  {
    final Cookie aCookie = new Cookie (sName, sValue);
    aCookie.setPath (sPath);
    if (bExpireWhenBrowserIsClosed)
      aCookie.setMaxAge (-1);
    else
      aCookie.setMaxAge (DEFAULT_MAX_AGE_SECONDS);
    aCookie.setSecure (bSecure);
    return aCookie;
  }

  /**
   * Create a cookie that is bound to the servlet context path within the local
   * web server.
   *
   * @param sName
   *        The cookie name.
   * @param sValue
   *        The cookie value.
   * @param bExpireWhenBrowserIsClosed
   *        <code>true</code> if this is a browser session cookie
   * @param bSecure
   *        <code>true</code> to send the cookie from the browser to the server
   *        only when using a secure protocol (e.g. https)
   * @return The created cookie object.
   * @since 9.3.2
   */
  @Nonnull
  public static Cookie createContextCookie (@Nonnull final String sName,
                                            @Nullable final String sValue,
                                            final boolean bExpireWhenBrowserIsClosed,
                                            final boolean bSecure)
  {
    // Always use the context path from the global scope!
    final String sContextPath = ServletContextPathHolder.getContextPath ();
    return createCookie (sName,
                         sValue,
                         StringHelper.hasText (sContextPath) ? sContextPath : "/",
                         bExpireWhenBrowserIsClosed,
                         bSecure);
  }

  /**
   * Remove a cookie by setting the max age to 0.
   *
   * @param aHttpResponse
   *        The HTTP response. May not be <code>null</code>.
   * @param aCookie
   *        The cookie to be removed. May not be <code>null</code>.
   */
  public static void removeCookie (@Nonnull final HttpServletResponse aHttpResponse, @Nonnull final Cookie aCookie)
  {
    ValueEnforcer.notNull (aHttpResponse, "HttpResponse");
    ValueEnforcer.notNull (aCookie, "aCookie");

    // expire the cookie!
    aCookie.setMaxAge (0);
    aHttpResponse.addCookie (aCookie);
  }
}
