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
package com.helger.servlet.response;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.CGlobal;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.EChange;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.http.CHttpHeader;
import com.helger.http.EHttpReferrerPolicy;
import com.helger.http.cache.CacheControlBuilder;
import com.helger.http.header.HttpHeaderMap;
import com.helger.http.url.ISimpleURL;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;

/**
 * This class encapsulates default settings to be applied to all {@link UnifiedResponse} objects.
 *
 * @author Philip Helger
 * @since 6.0.5
 */
@ThreadSafe
public final class UnifiedResponseDefaultSettings
{
  /** By default HTTP header value unification is enabled */
  public static final boolean DEFAULT_HTTP_HEADER_VALUES_UNIFIED = true;
  /** By default HTTP header value unification is disabled */
  public static final boolean DEFAULT_HTTP_HEADER_VALUES_QUOTE_IF_NECESSARY = false;

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static final HttpHeaderMap RESPONSE_HEADER_MAP = new HttpHeaderMap ();
  @GuardedBy ("RW_LOCK")
  private static final ICommonsOrderedMap <String, Cookie> COOKIES = new CommonsLinkedHashMap <> ();
  @GuardedBy ("RW_LOCK")
  private static boolean s_bHttpHeaderValuesUnified = DEFAULT_HTTP_HEADER_VALUES_UNIFIED;
  @GuardedBy ("RW_LOCK")
  private static boolean s_bHttpHeaderValuesQuoteIfNecessary = DEFAULT_HTTP_HEADER_VALUES_QUOTE_IF_NECESSARY;

  static
  {
    // Sensible defaults
    setAllowMimeSniffing (false);
    setEnableXSSFilter (true);
    setStrictTransportSecurity (CGlobal.SECONDS_PER_HOUR, true);
    setXFrameOptions (EXFrameOptionType.DEFAULT, null);
    setReferrerPolicy (EHttpReferrerPolicy.NONE);
  }

  private UnifiedResponseDefaultSettings ()
  {}

  /**
   * @return The non-<code>null</code> header map.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static HttpHeaderMap getResponseHeaderMap ()
  {
    return RW_LOCK.readLockedGet (RESPONSE_HEADER_MAP::getClone);
  }

  /**
   * When specifying <code>false</code>, this method uses a special response header to prevent
   * certain browsers from MIME-sniffing a response away from the declared content-type. When
   * passing <code>true</code>, that header is removed.
   *
   * @param bAllow
   *        Whether or not sniffing should be allowed (default is <code>true</code>).
   */
  public static void setAllowMimeSniffing (final boolean bAllow)
  {
    if (bAllow)
      removeResponseHeaders (CHttpHeader.X_CONTENT_TYPE_OPTIONS);
    else
      setResponseHeader (CHttpHeader.X_CONTENT_TYPE_OPTIONS, CHttpHeader.VALUE_NOSNIFF);
  }

  /**
   * This header enables the Cross-site scripting (XSS) filter built into most recent web browsers.
   * It's usually enabled by default anyway, so the role of this header is to re-enable the filter
   * for this particular website if it was disabled by the user. This header is supported in IE 8+,
   * and in Chrome (not sure which versions). The anti-XSS filter was added in Chrome 4. Its unknown
   * if that version honored this header.
   *
   * @param bEnable
   *        <code>true</code> to enable the header, <code>false</code> to disable it.
   */
  @Deprecated (forRemoval = true, since = "10.4.4")
  public static void setEnableXSSFilter (final boolean bEnable)
  {
    if (bEnable)
      setResponseHeader (CHttpHeader.X_XSS_PROTECTION, "1; mode=block");
    else
      removeResponseHeaders (CHttpHeader.X_XSS_PROTECTION);
  }

  /**
   * HTTP Strict Transport Security (HSTS) is an opt-in security enhancement that is specified by a
   * web application through the use of a special response header. Once a supported browser receives
   * this header that browser will prevent any communications from being sent over HTTP to the
   * specified domain and will instead send all communications over HTTPS. It also prevents HTTPS
   * click through prompts on browsers. The specification has been released and published end of
   * 2012 as RFC 6797 (HTTP Strict Transport Security (HSTS)) by the IETF.
   *
   * @param nMaxAgeSeconds
   *        number of seconds, after the reception of the STS header field, during which the UA
   *        regards the host (from whom the message was received) as a Known HSTS Host.
   * @param bIncludeSubdomains
   *        if enabled, this signals the UA that the HSTS Policy applies to this HSTS Host as well
   *        as any sub-domains of the host's domain name.
   */
  public static void setStrictTransportSecurity (@Nonnegative final int nMaxAgeSeconds,
                                                 final boolean bIncludeSubdomains)
  {
    setResponseHeader (CHttpHeader.STRICT_TRANSPORT_SECURITY,
                       new CacheControlBuilder ().setMaxAgeSeconds (nMaxAgeSeconds).getAsHTTPHeaderValue () +
                                                              (bIncludeSubdomains ? ";" +
                                                                                    CHttpHeader.VALUE_INCLUDE_SUBDOMAINS
                                                                                  : ""));
  }

  /**
   * Remove the `Strict-Transport-Security` headers from the default settings.
   *
   * @since 9.1.1
   */
  public static void removeStrictTransportSecurity ()
  {
    removeResponseHeaders (CHttpHeader.STRICT_TRANSPORT_SECURITY);
  }

  /**
   * The X-Frame-Options HTTP response header can be used to indicate whether or not a browser
   * should be allowed to render a page in a &lt;frame&gt;, &lt;iframe&gt; or &lt;object&gt; . Sites
   * can use this to avoid clickjacking attacks, by ensuring that their content is not embedded into
   * other sites. Example:
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
   *        The domain URL to be used in "ALLOW-FROM". May be <code>null</code> for the other cases.
   */
  public static void setXFrameOptions (@Nullable final EXFrameOptionType eType, @Nullable final ISimpleURL aDomain)
  {
    if (eType != null && eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    if (eType == null)
    {
      removeResponseHeaders (CHttpHeader.X_FRAME_OPTIONS);
    }
    else
    {
      final String sHeaderValue = eType.isURLRequired () ? eType.getID () +
                                                           " " +
                                                           aDomain.getAsStringWithEncodedParameters () : eType.getID ();
      setResponseHeader (CHttpHeader.X_FRAME_OPTIONS, sHeaderValue);
    }
  }

  /**
   * Set the default referrer policy to use. See
   * https://scotthelme.co.uk/a-new-security-header-referrer-policy/
   *
   * @param eReferrerPolicy
   *        Policy to use. May be <code>null</code>.
   */
  public static void setReferrerPolicy (@Nullable final EHttpReferrerPolicy eReferrerPolicy)
  {
    if (eReferrerPolicy == null || eReferrerPolicy == EHttpReferrerPolicy.NONE)
      removeResponseHeaders (CHttpHeader.REFERRER_POLICY);
    else
      setResponseHeader (CHttpHeader.REFERRER_POLICY, eReferrerPolicy.getValue ());
  }

  /**
   * Sets a response header to the response according to the passed name and value. An existing
   * header entry with the same name is overridden.
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

    RW_LOCK.writeLocked ( () -> RESPONSE_HEADER_MAP.setHeader (sName, sValue));
  }

  /**
   * Adds a response header to the response according to the passed name and value. If an existing
   * header with the same is present, the value is added to the list so that the header is emitted
   * more than once.
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

    RW_LOCK.writeLocked ( () -> RESPONSE_HEADER_MAP.addHeader (sName, sValue));
  }

  /**
   * Removes the response headers matching the passed name from the response. <br>
   * <b>ATTENTION:</b> You should only use the APIs that {@link UnifiedResponseDefaultSettings}
   * directly offers. Use this method only in emergency and make sure you validate the header field
   * and allowed value!
   *
   * @param sName
   *        Name of the header to be removed. May neither be <code>null</code> nor empty.
   * @return {@link EChange#CHANGED} in header was removed.
   */
  @Nonnull
  public static EChange removeResponseHeaders (@Nonnull @Nonempty final String sName)
  {
    ValueEnforcer.notEmpty (sName, "Name");

    return RW_LOCK.writeLockedGet ( () -> RESPONSE_HEADER_MAP.removeHeaders (sName));
  }

  /**
   * Remove all response headers currently present.
   *
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange removeAllResponseHeaders ()
  {
    return RW_LOCK.writeLockedGet (RESPONSE_HEADER_MAP::removeAll);
  }

  /**
   * @return <code>true</code> if at least one cookie is present.
   */
  public static boolean hasCookies ()
  {
    return RW_LOCK.readLockedBoolean (COOKIES::isNotEmpty);
  }

  /**
   * @return A copy of all contained cookies. Never <code>null</code> but maybe empty.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsOrderedMap <String, Cookie> getAllCookies ()
  {
    return RW_LOCK.readLockedGet (COOKIES::getClone);
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

    RW_LOCK.writeLockedGet ( () -> COOKIES.put (sKey, aCookie));
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
    if (StringHelper.isEmpty (sName))
      return EChange.UNCHANGED;

    return RW_LOCK.writeLockedGet ( () -> COOKIES.removeObject (sName));
  }

  /**
   * Remove all cookies.
   *
   * @return {@link EChange#CHANGED} if at least one cookie was removed.
   */
  @Nonnull
  public static EChange removeAllCookies ()
  {
    return RW_LOCK.writeLockedGet (COOKIES::removeAll);
  }

  /**
   * @return <code>true</code> if HTTP header values will be unified, <code>false</code> if not.
   * @see #DEFAULT_HTTP_HEADER_VALUES_UNIFIED
   * @since 9.1.4
   */
  public static boolean isHttpHeaderValuesUnified ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bHttpHeaderValuesUnified);
  }

  /**
   * Enable or disable the unification of HTTP header values.
   *
   * @param bHttpHeaderValuesUnified
   *        <code>true</code> to enable it, <code>false</code> to disable it.
   * @since 9.1.4
   */
  public static void setHttpHeaderValuesUnified (final boolean bHttpHeaderValuesUnified)
  {
    RW_LOCK.writeLocked ( () -> s_bHttpHeaderValuesUnified = bHttpHeaderValuesUnified);
  }

  /**
   * @return <code>true</code> if HTTP header values will be unified and quoted if necessary,
   *         <code>false</code> if not.
   * @see #DEFAULT_HTTP_HEADER_VALUES_QUOTE_IF_NECESSARY
   * @since 9.1.4
   */
  public static boolean isHttpHeaderValuesQuoteIfNecessary ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bHttpHeaderValuesQuoteIfNecessary);
  }

  /**
   * Enable or disable the automatic quoting of HTTP header values. This only takes effect, if the
   * unification is enabled.
   *
   * @param bHttpHeaderValuesQuoteIfNecessary
   *        <code>true</code> to enable it, <code>false</code> to disable it.
   * @see #setHttpHeaderValuesUnified(boolean)
   * @since 9.1.4
   */
  public static void setHttpHeaderValuesQuoteIfNecessary (final boolean bHttpHeaderValuesQuoteIfNecessary)
  {
    RW_LOCK.writeLocked ( () -> s_bHttpHeaderValuesQuoteIfNecessary = bHttpHeaderValuesQuoteIfNecessary);
  }
}
