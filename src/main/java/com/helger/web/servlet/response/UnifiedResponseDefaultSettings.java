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
package com.helger.web.servlet.response;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.Cookie;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.state.EChange;
import com.helger.web.http.CHTTPHeader;
import com.helger.web.http.CacheControlBuilder;
import com.helger.web.http.HTTPHeaderMap;

/**
 * This class encapsulates default settings to be applied to all
 * {@link UnifiedResponse}.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class UnifiedResponseDefaultSettings
{
  private static final HTTPHeaderMap s_aResponseHeaderMap = new HTTPHeaderMap ();
  private static Map <String, Cookie> s_aCookies = new LinkedHashMap <String, Cookie> ();

  private UnifiedResponseDefaultSettings ()
  {}

  /**
   * @return The non-<code>null</code> header map.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static HTTPHeaderMap getResponseHeaderMap ()
  {
    return s_aResponseHeaderMap.getClone ();
  }

  /**
   * @return <code>true</code> if at least one cookie is present.
   */
  public static boolean hasCookies ()
  {
    return ContainerHelper.isNotEmpty (s_aCookies);
  }

  /**
   * @return A copy of all contained cookies. Never <code>null</code> but maybe
   *         empty.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static Map <String, Cookie> getAllCookies ()
  {
    return ContainerHelper.newMap (s_aCookies);
  }

  public static void addCookie (@Nonnull final Cookie aCookie)
  {
    ValueEnforcer.notNull (aCookie, "Cookie");

    final String sKey = aCookie.getName ();
    s_aCookies.put (sKey, aCookie);
  }

  @Nonnull
  public static EChange removeCookie (@Nullable final String sName)
  {
    return EChange.valueOf (s_aCookies.remove (sName) != null);
  }

  /**
   * When specifying <code>false</code>, this method uses a special response
   * header to prevent certain browsers from MIME-sniffing a response away from
   * the declared content-type. When passing <code>true</code>, that header is
   * removed.
   *
   * @param bAllow
   *        Whether or not sniffing should be allowed (default is
   *        <code>true</code>).
   */
  public static void setAllowMimeSniffing (final boolean bAllow)
  {
    if (bAllow)
      s_aResponseHeaderMap.removeHeaders (CHTTPHeader.X_CONTENT_TYPE_OPTIONS);
    else
      s_aResponseHeaderMap.addHeader (CHTTPHeader.X_CONTENT_TYPE_OPTIONS, CHTTPHeader.VALUE_NOSNIFF);
  }

  /**
   * This header enables the Cross-site scripting (XSS) filter built into most
   * recent web browsers. It's usually enabled by default anyway, so the role of
   * this header is to re-enable the filter for this particular website if it
   * was disabled by the user. This header is supported in IE 8+, and in Chrome
   * (not sure which versions). The anti-XSS filter was added in Chrome 4. Its
   * unknown if that version honored this header.
   *
   * @param bEnable
   *        <code>true</code> to enable the header, <code>false</code> to
   *        disable it.
   */
  public static void setEnableXSSFilter (final boolean bEnable)
  {
    if (bEnable)
      s_aResponseHeaderMap.addHeader (CHTTPHeader.X_XSS_PROTECTION, "1; mode=block");
    else
      s_aResponseHeaderMap.removeHeaders (CHTTPHeader.X_XSS_PROTECTION);
  }

  /**
   * When specifying <code>false</code>, this method uses a special response
   * header to prevent certain browsers from MIME-sniffing a response away from
   * the declared content-type. When passing <code>true</code>, that header is
   * removed.
   *
   * @param nMaxAgeSeconds
   *        number of seconds, after the reception of the STS header field,
   *        during which the UA regards the host (from whom the message was
   *        received) as a Known HSTS Host.
   * @param bIncludeSubdomains
   *        if enabled, this signals the UA that the HSTS Policy applies to this
   *        HSTS Host as well as any sub-domains of the host's domain name.
   */
  public static void setStrictTransportSecurity (final int nMaxAgeSeconds, final boolean bIncludeSubdomains)
  {
    s_aResponseHeaderMap.addHeader (CHTTPHeader.STRICT_TRANSPORT_SECURITY,
                                    new CacheControlBuilder ().setMaxAgeSeconds (nMaxAgeSeconds)
                                                              .getAsHTTPHeaderValue () +
                                        (bIncludeSubdomains ? ";" + CHTTPHeader.VALUE_INCLUDE_SUBDOMAINS : ""));
  }

  /**
   * Adds a response header to the response according to the passed name and
   * value.<br/>
   * <b>ATTENTION:</b> You should only use the APIs that
   * {@link UnifiedResponseDefaultSettings} directly offers. Use this method
   * only in emergency and make sure you validate the header field and allowed
   * value!
   *
   * @param sName
   *        Name of the header. May neither be <code>null</code> nor empty.
   * @param sValue
   *        Value of the header. May neither be <code>null</code> nor empty.
   */
  public static void addCustomResponseHeader (@Nonnull @Nonempty final String sName,
                                              @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    s_aResponseHeaderMap.addHeader (sName, sValue);
  }

  /**
   * Removes the response headers matching the passed name from the response.<br/>
   * <b>ATTENTION:</b> You should only use the APIs that
   * {@link UnifiedResponseDefaultSettings} directly offers. Use this method
   * only in emergency and make sure you validate the header field and allowed
   * value!
   *
   * @param sName
   *        Name of the header to be removed. May neither be <code>null</code>
   *        nor empty.
   * @return {@link EChange#CHANGED} in header was removed.
   */
  @Nonnull
  public static EChange removeCustomResponseHeaders (@Nonnull @Nonempty final String sName)
  {
    ValueEnforcer.notEmpty (sName, "Name");

    return s_aResponseHeaderMap.removeHeaders (sName);
  }
}
