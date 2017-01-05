/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.servlet.response;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.Cookie;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsOrderedMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.http.CHTTPHeader;
import com.helger.http.CacheControlBuilder;
import com.helger.http.HTTPHeaderMap;

/**
 * This class encapsulates default settings to be applied to all
 * {@link UnifiedResponse} objects.
 *
 * @author Philip Helger
 * @since 6.0.5
 */
@ThreadSafe
public final class UnifiedResponseDefaultSettings
{
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  @GuardedBy ("s_aRWLock")
  private static final HTTPHeaderMap s_aResponseHeaderMap = new HTTPHeaderMap ();
  @GuardedBy ("s_aRWLock")
  private static final ICommonsOrderedMap <String, Cookie> s_aCookies = new CommonsLinkedHashMap<> ();

  private UnifiedResponseDefaultSettings ()
  {}

  /**
   * @return The non-<code>null</code> header map.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static HTTPHeaderMap getResponseHeaderMap ()
  {
    return s_aRWLock.readLocked (s_aResponseHeaderMap::getClone);
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
      removeResponseHeaders (CHTTPHeader.X_CONTENT_TYPE_OPTIONS);
    else
      setResponseHeader (CHTTPHeader.X_CONTENT_TYPE_OPTIONS, CHTTPHeader.VALUE_NOSNIFF);
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
      setResponseHeader (CHTTPHeader.X_XSS_PROTECTION, "1; mode=block");
    else
      removeResponseHeaders (CHTTPHeader.X_XSS_PROTECTION);
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
  public static void setStrictTransportSecurity (@Nonnegative final int nMaxAgeSeconds,
                                                 final boolean bIncludeSubdomains)
  {
    setResponseHeader (CHTTPHeader.STRICT_TRANSPORT_SECURITY,
                       new CacheControlBuilder ().setMaxAgeSeconds (nMaxAgeSeconds).getAsHTTPHeaderValue () +
                                                              (bIncludeSubdomains ? ";" +
                                                                                    CHTTPHeader.VALUE_INCLUDE_SUBDOMAINS
                                                                                  : ""));
  }

  /**
   * The X-Frame-Options HTTP response header can be used to indicate whether or
   * not a browser should be allowed to render a page in a &lt;frame&gt;,
   * &lt;iframe&gt; or &lt;object&gt; . Sites can use this to avoid clickjacking
   * attacks, by ensuring that their content is not embedded into other sites.
   * Example:
   *
   * <pre>
   * X-Frame-Options: DENY
   * X-Frame-Options: SAMEORIGIN
   * X-Frame-Options: ALLOW-FROM https://example.com/
   * </pre>
   *
   * @param eType
   *        The X-Frame-Options type to be set. May not be <code>null</code>.
   * @param aDomain
   *        The domain URL to be used in "ALLOW-FROM". May be <code>null</code>
   *        for the other cases.
   */
  public static void setXFrameOptions (@Nonnull final EXFrameOptionType eType, @Nullable final ISimpleURL aDomain)
  {
    ValueEnforcer.notNull (eType, "Type");
    if (eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    if (eType.isURLRequired ())
      setResponseHeader (CHTTPHeader.X_FRAME_OPTIONS,
                         eType.getID () + " " + aDomain.getAsStringWithEncodedParameters ());
    else
      setResponseHeader (CHTTPHeader.X_FRAME_OPTIONS, eType.getID ());
  }

  /**
   * Sets a response header to the response according to the passed name and
   * value. An existing header entry with the same name is overridden.
   *
   * @param sName
   *        Name of the header. May neither be <code>null</code> nor empty.
   * @param sValue
   *        Value of the header. May neither be <code>null</code> nor empty.
   */
  public static void setResponseHeader (@Nonnull @Nonempty final String sName, @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    s_aRWLock.writeLocked ( () -> {
      s_aResponseHeaderMap.setHeader (sName, sValue);
    });
  }

  /**
   * Adds a response header to the response according to the passed name and
   * value. If an existing header with the same is present, the value is added
   * to the list so that the header is emitted more than once.
   *
   * @param sName
   *        Name of the header. May neither be <code>null</code> nor empty.
   * @param sValue
   *        Value of the header. May neither be <code>null</code> nor empty.
   */
  public static void addResponseHeader (@Nonnull @Nonempty final String sName, @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    s_aRWLock.writeLocked ( () -> {
      s_aResponseHeaderMap.addHeader (sName, sValue);
    });
  }

  /**
   * Removes the response headers matching the passed name from the response.
   * <br>
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
  public static EChange removeResponseHeaders (@Nonnull @Nonempty final String sName)
  {
    ValueEnforcer.notEmpty (sName, "Name");

    return s_aRWLock.writeLocked ( () -> s_aResponseHeaderMap.removeHeaders (sName));
  }

  /**
   * Remove all response headers currently present.
   *
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange removeAllResponseHeaders ()
  {
    return s_aRWLock.writeLocked ( () -> s_aResponseHeaderMap.clear ());
  }

  /**
   * @return <code>true</code> if at least one cookie is present.
   */
  public static boolean hasCookies ()
  {
    return s_aRWLock.readLocked ( () -> s_aCookies.isNotEmpty ());
  }

  /**
   * @return A copy of all contained cookies. Never <code>null</code> but maybe
   *         empty.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, Cookie> getAllCookies ()
  {
    return s_aRWLock.readLocked ( () -> s_aCookies.getClone ());
  }

  /**
   * Add the passed cookie.
   *
   * @param aCookie
   *        The cookie to be added. May not be <code>null</code>.
   */
  public static void addCookie (@Nonnull final Cookie aCookie)
  {
    ValueEnforcer.notNull (aCookie, "Cookie");

    final String sKey = aCookie.getName ();

    s_aRWLock.writeLocked ( () -> s_aCookies.put (sKey, aCookie));
  }

  /**
   * Remove the cookie with the specified name.
   *
   * @param sName
   *        The name of the cookie to be removed. May be <code>null</code>.
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange removeCookie (@Nullable final String sName)
  {
    if (StringHelper.hasNoText (sName))
      return EChange.UNCHANGED;

    return s_aRWLock.writeLocked ( () -> EChange.valueOf (s_aCookies.remove (sName) != null));
  }

  /**
   * Remove all cookies.
   *
   * @return {@link EChange#CHANGED} if at least one cookie was removed.
   */
  @Nonnull
  public static EChange removeAllCookies ()
  {
    return s_aRWLock.writeLocked ( () -> s_aCookies.removeAll ());
  }
}
