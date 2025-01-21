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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.io.IHasInputStream;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.mutable.MutableLong;
import com.helger.commons.state.EChange;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.URLHelper;
import com.helger.commons.url.URLProtocolRegistry;
import com.helger.http.AcceptCharsetList;
import com.helger.http.AcceptMimeTypeList;
import com.helger.http.CacheControlBuilder;
import com.helger.http.EHttpVersion;
import com.helger.http.QValue;
import com.helger.http.RFC5987Encoder;
import com.helger.servlet.ServletSettings;
import com.helger.servlet.request.RequestHelper;
import com.helger.useragent.browser.BrowserInfo;
import com.helger.useragent.browser.EBrowserType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class encapsulates all things required to build a HTTP response. It
 * offer warnings and consistency checks if something is missing.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class UnifiedResponse
{
  /** By default content is not allowed on redirect */
  public static final boolean DEFAULT_ALLOW_CONTENT_ON_REDIRECT = false;
  /** By default content is not allowed for status codes */
  public static final boolean DEFAULT_ALLOW_CONTENT_ON_STATUS_CODE = true;
  /** Default content disposition type is Attachment */
  public static final EContentDispositionType DEFAULT_CONTENT_DISPOSITION_TYPE = EContentDispositionType.ATTACHMENT;
  /**
   * By default a warning is emitted on duplicate cookies
   *
   * @since 6.0.5
   */
  public static final boolean DEFAULT_WARN_ON_DUPLICATE_COOKIES = true;
  /** Maximum KB a CSS file might have in IE */
  public static final int MAX_CSS_KB_FOR_IE = 288;

  private static final Logger LOGGER = LoggerFactory.getLogger (UnifiedResponse.class);
  private static final AtomicInteger RESPONSE_NUM = new AtomicInteger (0);
  private static final AtomicBoolean SILENT_MODE = new AtomicBoolean (GlobalDebug.DEFAULT_SILENT_MODE);

  // Input fields set from request
  private final EHttpVersion m_eHttpVersion;
  private final EHttpMethod m_eHttpMethod;
  private final HttpServletRequest m_aHttpRequest;
  private final AcceptCharsetList m_aAcceptCharsetList;
  private final AcceptMimeTypeList m_aAcceptMimeTypeList;
  private BrowserInfo m_aRequestBrowserInfo;

  // Settings
  /**
   * Flag which determines whether content is allow, if a redirect is set. This
   * is rarely used.
   */
  private boolean m_bAllowContentOnRedirect = DEFAULT_ALLOW_CONTENT_ON_REDIRECT;

  /**
   * Flag which determines whether content is allow, if a status code is set.
   * This is rarely used.
   */
  private boolean m_bAllowContentOnStatusCode = DEFAULT_ALLOW_CONTENT_ON_STATUS_CODE;

  // Main response fields
  private Charset m_aCharset;
  private IMimeType m_aMimeType;
  private byte [] m_aContentArray;
  private int m_nContentArrayOfs;
  private int m_nContentArrayLength;
  private IHasInputStream m_aContentISP;
  private EContentDispositionType m_eContentDispositionType = DEFAULT_CONTENT_DISPOSITION_TYPE;
  private String m_sContentDispositionFilename;
  private CacheControlBuilder m_aCacheControl;
  private final HttpHeaderMap m_aResponseHeaderMap = new HttpHeaderMap ();
  private int m_nStatusCode = CGlobal.ILLEGAL_UINT;
  private String m_sRedirectTargetUrl;
  private ERedirectMode m_eRedirectMode;
  private boolean m_bWarnOnDuplicateCookies = DEFAULT_WARN_ON_DUPLICATE_COOKIES;
  private ICommonsOrderedMap <String, Cookie> m_aCookies;
  private boolean m_bHttpHeaderValuesUnified;
  private boolean m_bHttpHeaderValuesQuoteIfNecessary;

  // Internal status members
  /**
   * Unique internal ID for each response, so that error messages can be more
   * easily aggregated.
   */
  private final int m_nResponseID = RESPONSE_NUM.incrementAndGet ();

  /**
   * The request URL, lazily initialized.
   */
  private String m_sRequestURL;

  /**
   * Just avoid emitting the request headers more than once, as they wont change
   * from error to error.
   */
  private boolean m_bAlreadyEmittedRequestHeaders = false;

  /** This maps keeps all the response headers for later emitting. */
  private final HttpHeaderMap m_aRequestHeaderMap;

  /**
   * An optional encode to be used to determine if a content-disposition
   * filename can be ISO-8859-1 encoded.
   */
  private CharsetEncoder m_aContentDispositionEncoder;

  public static boolean isSilentMode ()
  {
    return SILENT_MODE.get ();
  }

  public static boolean setSilentMode (final boolean bSilentMode)
  {
    return SILENT_MODE.getAndSet (bSilentMode);
  }

  @Nonnull
  @ReturnsMutableCopy
  private static ICommonsOrderedMap <String, Cookie> _createCookieMap ()
  {
    return new CommonsLinkedHashMap <> ();
  }

  /**
   * Constructor
   *
   * @param eHttpVersion
   *        HTTP version of this request (1.0 or 1.1)
   * @param eHttpMethod
   *        HTTP method of this request (GET, POST, ...)
   * @param aHttpRequest
   *        The main HTTP request
   */
  public UnifiedResponse (@Nonnull final EHttpVersion eHttpVersion,
                          @Nonnull final EHttpMethod eHttpMethod,
                          @Nonnull final HttpServletRequest aHttpRequest)
  {
    m_eHttpVersion = ValueEnforcer.notNull (eHttpVersion, "HTTPVersion");
    m_eHttpMethod = ValueEnforcer.notNull (eHttpMethod, "HTTPMethod");
    m_aHttpRequest = ValueEnforcer.notNull (aHttpRequest, "HTTPRequest");
    m_aAcceptCharsetList = RequestHelper.getAcceptCharsets (aHttpRequest);
    m_aAcceptMimeTypeList = RequestHelper.getAcceptMimeTypes (aHttpRequest);
    m_aRequestHeaderMap = RequestHelper.getRequestHeaderMap (aHttpRequest);

    // Copy all default settings (without unification)
    m_aResponseHeaderMap.setAllHeaders (UnifiedResponseDefaultSettings.getResponseHeaderMap ());
    if (UnifiedResponseDefaultSettings.hasCookies ())
    {
      m_aCookies = _createCookieMap ();
      m_aCookies.putAll (UnifiedResponseDefaultSettings.getAllCookies ());
    }
    m_bHttpHeaderValuesUnified = UnifiedResponseDefaultSettings.isHttpHeaderValuesUnified ();
    m_bHttpHeaderValuesQuoteIfNecessary = UnifiedResponseDefaultSettings.isHttpHeaderValuesQuoteIfNecessary ();
  }

  @Nonnull
  @Nonempty
  protected final String getRequestURL ()
  {
    if (m_sRequestURL == null)
      m_sRequestURL = RequestHelper.getURLDecoded (m_aHttpRequest);
    return m_sRequestURL;
  }

  @Nonnull
  @Nonempty
  protected String getLogPrefix ()
  {
    return "UnifiedResponse[" + m_nResponseID + "] to [" + m_eHttpMethod.getName () + " " + getRequestURL () + "]: ";
  }

  protected void logInfo (@Nonnull final String sMsg)
  {
    LOGGER.info (getLogPrefix () + sMsg);
  }

  protected final void showRequestInfo ()
  {
    if (!isSilentMode ())
    {
      // Emit only once per response
      if (!m_bAlreadyEmittedRequestHeaders)
      {
        final StringBuilder aSB = new StringBuilder ();
        aSB.append ("  Request Headers: " +
                    m_aRequestHeaderMap.getAllHeaders ().getSortedByKey (Comparator.naturalOrder ()));
        if (m_aCookies != null && m_aCookies.isNotEmpty ())
          aSB.append ("  Request Cookies: " + m_aCookies.getSortedByKey (Comparator.naturalOrder ()));
        if (m_aResponseHeaderMap.isNotEmpty ())
          aSB.append ("\n  Response Headers: " +
                      m_aResponseHeaderMap.getAllHeaders ().getSortedByKey (Comparator.naturalOrder ()));

        LOGGER.warn (aSB.toString ());
        m_bAlreadyEmittedRequestHeaders = true;
      }
    }
  }

  protected void logWarn (@Nonnull final String sMsg)
  {
    LOGGER.warn (getLogPrefix () + sMsg);
    showRequestInfo ();
  }

  protected void logError (@Nonnull final String sMsg)
  {
    LOGGER.error (getLogPrefix () + sMsg);
    showRequestInfo ();
  }

  /**
   * @return The HTTP version of the request. Never <code>null</code>.
   */
  @Nonnull
  public final EHttpVersion getHttpVersion ()
  {
    return m_eHttpVersion;
  }

  /**
   * @return The HTTP method of the request. Never <code>null</code>.
   */
  @Nonnull
  public final EHttpMethod getHttpMethod ()
  {
    return m_eHttpMethod;
  }

  /**
   * @return The browser info of the request. Never <code>null</code>.
   */
  @Nullable
  @Deprecated (forRemoval = true, since = "10.1.6")
  public final BrowserInfo getRequestBrowserInfo ()
  {
    return m_aRequestBrowserInfo;
  }

  @Nonnull
  @Deprecated (forRemoval = true, since = "10.1.6")
  public final UnifiedResponse setRequestBrowserInfo (@Nullable final BrowserInfo aRequestBrowserInfo)
  {
    m_aRequestBrowserInfo = aRequestBrowserInfo;
    return this;
  }

  /**
   * @return <code>true</code> if content is allowed even if a redirect is
   *         present.
   */
  public final boolean isAllowContentOnRedirect ()
  {
    return m_bAllowContentOnRedirect;
  }

  @Nonnull
  public final UnifiedResponse setAllowContentOnRedirect (final boolean bAllowContentOnRedirect)
  {
    m_bAllowContentOnRedirect = bAllowContentOnRedirect;
    return this;
  }

  /**
   * @return <code>true</code> if content is allowed even if a status code is
   *         present.
   */
  public final boolean isAllowContentOnStatusCode ()
  {
    return m_bAllowContentOnStatusCode;
  }

  @Nonnull
  public final UnifiedResponse setAllowContentOnStatusCode (final boolean bAllowContentOnStatusCode)
  {
    m_bAllowContentOnStatusCode = bAllowContentOnStatusCode;
    return this;
  }

  @Nullable
  public final Charset getCharset ()
  {
    return m_aCharset;
  }

  @Nonnull
  public final UnifiedResponse setCharset (@Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (aCharset, "Charset");
    if (m_aCharset != null)
      logInfo ("Overwriting charset from " + m_aCharset + " to " + aCharset);
    m_aCharset = aCharset;
    return this;
  }

  @Nonnull
  public final UnifiedResponse removeCharset ()
  {
    m_aCharset = null;
    return this;
  }

  @Nullable
  public final IMimeType getMimeType ()
  {
    return m_aMimeType;
  }

  @Nonnull
  public final UnifiedResponse setMimeType (@Nonnull final IMimeType aMimeType)
  {
    ValueEnforcer.notNull (aMimeType, "MimeType");
    if (m_aMimeType != null)
      logInfo ("Overwriting MimeType from " + m_aMimeType + " to " + aMimeType);
    m_aMimeType = aMimeType;
    return this;
  }

  @Nonnull
  public final UnifiedResponse setMimeTypeString (@Nonnull @Nonempty final String sMimeType)
  {
    ValueEnforcer.notEmpty (sMimeType, "MimeType");

    final IMimeType aMimeType = MimeTypeParser.safeParseMimeType (sMimeType);
    if (aMimeType != null)
      setMimeType (aMimeType);
    else
      logError ("Failed to resolve mime type from '" + sMimeType + "'");
    return this;
  }

  @Nonnull
  public final UnifiedResponse removeMimeType ()
  {
    m_aMimeType = null;
    return this;
  }

  /**
   * @return <code>true</code> if a content was already set, <code>false</code>
   *         if not.
   */
  public final boolean hasContent ()
  {
    return m_aContentArray != null || m_aContentISP != null;
  }

  /**
   * Utility method to set an empty response content.
   *
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setEmptyContent ()
  {
    return setContent (ArrayHelper.EMPTY_BYTE_ARRAY, 0, 0);
  }

  /**
   * Utility method to set content and charset at once.
   *
   * @param sContent
   *        The response content string. May not be <code>null</code>.
   * @param aCharset
   *        The charset to use. May not be <code>null</code>.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setContentAndCharset (@Nonnull final String sContent, @Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (sContent, "Content");
    setCharset (aCharset);
    setContent (sContent.getBytes (aCharset));
    return this;
  }

  /**
   * Set the response content. To return an empty response pass in a new empty
   * array, but not <code>null</code>.
   *
   * @param aContent
   *        The content to be returned. Is <b>not</b> copied inside! May not be
   *        <code>null</code> but maybe empty.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setContent (@Nonnull final byte [] aContent)
  {
    ValueEnforcer.notNull (aContent, "Content");
    return setContent (aContent, 0, aContent.length);
  }

  /**
   * Set the response content. To return an empty response pass in a new empty
   * array, but not <code>null</code>.
   *
   * @param aContent
   *        The content to be returned. Is <b>not</b> copied inside! May not be
   *        <code>null</code> but maybe empty.
   * @param nOfs
   *        The content offset to start at. Must be &ge; 0.
   * @param nLen
   *        The content length to use. Must be &ge; 0 and &le; than the content
   *        length!
   * @return this
   */
  @Nonnull
  @SuppressFBWarnings ("EI_EXPOSE_REP2")
  public final UnifiedResponse setContent (@Nonnull final byte [] aContent,
                                           @Nonnegative final int nOfs,
                                           @Nonnegative final int nLen)
  {
    ValueEnforcer.isArrayOfsLen (aContent, nOfs, nLen);
    if (hasContent ())
      logInfo ("Overwriting content with byte array!");
    m_aContentArray = aContent;
    m_nContentArrayOfs = nOfs;
    m_nContentArrayLength = nLen;
    m_aContentISP = null;
    return this;
  }

  /**
   * Set the response content provider.
   *
   * @param aISP
   *        The content provider to be used. May not be <code>null</code>.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setContent (@Nonnull final IHasInputStream aISP)
  {
    ValueEnforcer.notNull (aISP, "InputStreamProvider");
    if (hasContent ())
      logInfo ("Overwriting content with content provider!");
    m_aContentArray = null;
    m_nContentArrayOfs = -1;
    m_nContentArrayLength = -1;
    m_aContentISP = aISP;
    return this;
  }

  @Nonnull
  public final UnifiedResponse removeContent ()
  {
    m_aContentArray = null;
    m_nContentArrayOfs = -1;
    m_nContentArrayLength = -1;
    m_aContentISP = null;
    return this;
  }

  @Nonnull
  public final UnifiedResponse setExpires (@Nonnull final LocalDateTime aDT)
  {
    m_aResponseHeaderMap.setDateHeader (CHttpHeader.EXPIRES, aDT);
    return this;
  }

  @Nonnull
  public final UnifiedResponse removeExpires ()
  {
    m_aResponseHeaderMap.removeHeaders (CHttpHeader.EXPIRES);
    return this;
  }

  @Nonnull
  public final UnifiedResponse setLastModified (@Nonnull final LocalDateTime aDT)
  {
    if (m_eHttpMethod != EHttpMethod.GET && m_eHttpMethod != EHttpMethod.HEAD)
      logWarn ("Setting Last-Modified on a non GET or HEAD request may have no impact!");

    m_aResponseHeaderMap.setDateHeader (CHttpHeader.LAST_MODIFIED, aDT);
    return this;
  }

  @Nonnull
  public final UnifiedResponse removeLastModified ()
  {
    m_aResponseHeaderMap.removeHeaders (CHttpHeader.LAST_MODIFIED);
    return this;
  }

  /**
   * Set an ETag for the response. The ETag must be a quoted value (being
   * surrounded by double quotes).
   *
   * @param sETag
   *        The quoted ETag to be set. May neither be <code>null</code> nor
   *        empty.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setETag (@Nonnull @Nonempty final String sETag)
  {
    ValueEnforcer.notEmpty (sETag, "ETag");
    ValueEnforcer.isTrue (sETag.startsWith ("\"") || sETag.startsWith ("W/\""),
                          () -> "Etag must start with a '\"' character or with 'W/\"': " + sETag);
    ValueEnforcer.isTrue (sETag.endsWith ("\""), () -> "Etag must end with a '\"' character: " + sETag);
    if (m_eHttpMethod != EHttpMethod.GET && m_eHttpMethod != EHttpMethod.HEAD)
      logWarn ("Setting an ETag on a non-GET/HEAD request may have no impact!");

    m_aResponseHeaderMap.setHeader (CHttpHeader.ETAG, sETag);
    return this;
  }

  /**
   * Set an ETag for the response if this is an HTTP/1.1 response. HTTP/1.0 does
   * not support ETags. The ETag must be a quoted value (being surrounded by
   * double quotes).
   *
   * @param sETag
   *        The quoted ETag to be set. May neither be <code>null</code> nor
   *        empty.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setETagIfApplicable (@Nonnull @Nonempty final String sETag)
  {
    if (m_eHttpVersion.isAtLeast11 ())
      setETag (sETag);
    return this;
  }

  /**
   * Remove the ETag if present.
   *
   * @return this
   */
  @Nonnull
  public final UnifiedResponse removeETag ()
  {
    m_aResponseHeaderMap.removeHeaders (CHttpHeader.ETAG);
    return this;
  }

  /**
   * Set the content disposition type (e.g. for PDF/Excel downloads). The
   * default is {@link #DEFAULT_CONTENT_DISPOSITION_TYPE}. This value is only
   * used if a content disposition filename is defined.
   *
   * @param eContentDispositionType
   *        The content disposition type to be used. May not be
   *        <code>null</code>.
   * @return this
   * @see #setContentDispositionFilename(String)
   */
  @Nonnull
  public final UnifiedResponse setContentDispositionType (@Nonnull final EContentDispositionType eContentDispositionType)
  {
    ValueEnforcer.notNull (eContentDispositionType, "ContentDispositionType");

    m_eContentDispositionType = eContentDispositionType;
    return this;
  }

  /**
   * @return The current content disposition type. Never <code>null</code>.
   *         Default is {@link #DEFAULT_CONTENT_DISPOSITION_TYPE}.
   */
  @Nonnull
  public final EContentDispositionType getContentDispositionType ()
  {
    return m_eContentDispositionType;
  }

  /**
   * Set the content disposition filename for attachment download.
   *
   * @param sFilename
   *        The filename for attachment download to use. May neither be
   *        <code>null</code> nor empty.
   * @return this
   * @see #removeContentDispositionFilename()
   */
  @Nonnull
  public final UnifiedResponse setContentDispositionFilename (@Nonnull @Nonempty final String sFilename)
  {
    ValueEnforcer.notEmpty (sFilename, "Filename");

    // Ensure that a valid filename is used
    // -> Strip all paths and replace all invalid characters
    final String sFilenameToUse = FilenameHelper.getWithoutPath (FilenameHelper.getAsSecureValidFilename (sFilename));
    if (!sFilename.equals (sFilenameToUse))
      logWarn ("Content-Dispostion filename was internally modified from '" +
               sFilename +
               "' to '" +
               sFilenameToUse +
               "'");

    // Disabled because of the extended UTF-8 handling (RFC 5987)
    if (false)
    {
      // Check if encoding as ISO-8859-1 is possible
      if (m_aContentDispositionEncoder == null)
        m_aContentDispositionEncoder = StandardCharsets.ISO_8859_1.newEncoder ();
      if (!m_aContentDispositionEncoder.canEncode (sFilenameToUse))
        logError ("Content-Dispostion filename '" + sFilenameToUse + "' cannot be encoded to ISO-8859-1!");
    }
    // Are we overwriting?
    if (m_sContentDispositionFilename != null)
      logInfo ("Overwriting Content-Dispostion filename from '" +
               m_sContentDispositionFilename +
               "' to '" +
               sFilenameToUse +
               "'");

    // No URL encoding necessary.
    // Filename must be in ISO-8859-1
    // See http://greenbytes.de/tech/tc2231/
    m_sContentDispositionFilename = sFilenameToUse;
    return this;
  }

  /**
   * @return The current content disposition filename. May be <code>null</code>
   *         if not set.
   */
  @Nullable
  public final String getContentDispositionFilename ()
  {
    return m_sContentDispositionFilename;
  }

  /**
   * Remove the current content disposition filename. This method can be called
   * if a filename is set or not.
   *
   * @return this
   */
  @Nonnull
  public final UnifiedResponse removeContentDispositionFilename ()
  {
    m_sContentDispositionFilename = null;
    return this;
  }

  /**
   * Utility method for setting the MimeType application/force-download and set
   * the respective content disposition filename.
   *
   * @param sFilename
   *        The filename to be used.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setDownloadFilename (@Nonnull @Nonempty final String sFilename)
  {
    setMimeType (CMimeType.APPLICATION_FORCE_DOWNLOAD);
    setContentDispositionFilename (sFilename);
    return this;
  }

  @Nonnull
  public final UnifiedResponse setCacheControl (@Nonnull final CacheControlBuilder aCacheControl)
  {
    ValueEnforcer.notNull (aCacheControl, "CacheControl");

    if (m_aCacheControl != null)
      logInfo ("Overwriting Cache-Control data from '" +
               m_aCacheControl.getAsHTTPHeaderValue () +
               "' to '" +
               aCacheControl.getAsHTTPHeaderValue () +
               "'");
    m_aCacheControl = aCacheControl;
    return this;
  }

  @Nullable
  @ReturnsMutableObject
  public final CacheControlBuilder cacheControl ()
  {
    return m_aCacheControl;
  }

  @Nonnull
  public final UnifiedResponse removeCacheControl ()
  {
    m_aCacheControl = null;
    return this;
  }

  /**
   * @return The non-<code>null</code> header map.
   */
  @Nonnull
  @ReturnsMutableObject
  protected final HttpHeaderMap responseHeaderMap ()
  {
    return m_aResponseHeaderMap;
  }

  /**
   * Remove all settings and headers relevant to caching.
   *
   * @return this for chaining
   */
  @Nonnull
  public final UnifiedResponse removeCaching ()
  {
    // Remove any eventually set headers
    removeExpires ();
    removeCacheControl ();
    removeETag ();
    removeLastModified ();
    m_aResponseHeaderMap.removeHeaders (CHttpHeader.PRAGMA);
    return this;
  }

  /**
   * A utility method that disables caching for this response.
   *
   * @return this
   */
  @Nonnull
  public final UnifiedResponse disableCaching ()
  {
    // Remove any eventually set headers
    removeCaching ();
    if (m_eHttpVersion.is10 ())
    {
      // Set to expire far in the past for HTTP/1.0.
      m_aResponseHeaderMap.setHeader (CHttpHeader.EXPIRES, ResponseHelperSettings.EXPIRES_NEVER_STRING);

      // Set standard HTTP/1.0 no-cache header.
      m_aResponseHeaderMap.setHeader (CHttpHeader.PRAGMA, "no-cache");
    }
    else
    {
      final CacheControlBuilder aCacheControlBuilder = new CacheControlBuilder ().setNoStore (true)
                                                                                 .setNoCache (true)
                                                                                 .setMustRevalidate (true)
                                                                                 .setProxyRevalidate (true);

      // Set IE extended HTTP/1.1 no-cache headers.
      // http://aspnetresources.com/blog/cache_control_extensions
      // Disabled because:
      // http://blogs.msdn.com/b/ieinternals/archive/2009/07/20/using-post_2d00_check-and-pre_2d00_check-cache-directives.aspx
      if (false)
        aCacheControlBuilder.addExtension ("post-check=0").addExtension ("pre-check=0");

      setCacheControl (aCacheControlBuilder);
    }
    return this;
  }

  /**
   * Enable caching of this resource for the specified number of seconds.
   *
   * @param nSeconds
   *        The number of seconds caching is allowed. Must be &gt; 0.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse enableCaching (@Nonnegative final int nSeconds)
  {
    ValueEnforcer.isGT0 (nSeconds, "Seconds");

    // Remove any eventually set headers
    // Note: don't remove Last-Modified and ETag!
    removeExpires ();
    removeCacheControl ();
    m_aResponseHeaderMap.removeHeaders (CHttpHeader.PRAGMA);
    if (m_eHttpVersion.is10 ())
    {
      m_aResponseHeaderMap.setDateHeader (CHttpHeader.EXPIRES,
                                          PDTFactory.getCurrentLocalDateTime ().plusSeconds (nSeconds));
    }
    else
    {
      final CacheControlBuilder aCacheControlBuilder = new CacheControlBuilder ().setPublic (true)
                                                                                 .setMaxAgeSeconds (nSeconds);
      setCacheControl (aCacheControlBuilder);
    }
    return this;
  }

  /**
   * @return <code>true</code> if a status code is defined, <code>false</code>
   *         if not.
   */
  public final boolean isStatusCodeDefined ()
  {
    return m_nStatusCode != CGlobal.ILLEGAL_UINT;
  }

  /**
   * @return The HTTP status code defined or {@link CGlobal#ILLEGAL_UINT} if
   *         undefined.
   * @see #isStatusCodeDefined()
   */
  public final int getStatusCode ()
  {
    return m_nStatusCode;
  }

  private void _setStatus (@Nonnegative final int nStatusCode)
  {
    if (isStatusCodeDefined ())
      logInfo ("Overwriting status code " + m_nStatusCode + " with " + nStatusCode);
    m_nStatusCode = nStatusCode;
  }

  /**
   * Set the status code to be returned from the response.
   *
   * @param nStatusCode
   *        The status code to be set. Must be a valid HTTP response code.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setStatus (@Nonnegative final int nStatusCode)
  {
    _setStatus (nStatusCode);
    return this;
  }

  /**
   * Special handling for returning status code 401 UNAUTHORIZED.
   *
   * @param sAuthenticate
   *        The string to be used for the {@link CHttpHeader#WWW_AUTHENTICATE}
   *        response header. May be <code>null</code> or empty.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setStatusUnauthorized (@Nullable final String sAuthenticate)
  {
    _setStatus (HttpServletResponse.SC_UNAUTHORIZED);
    if (StringHelper.hasText (sAuthenticate))
      m_aResponseHeaderMap.setHeader (CHttpHeader.WWW_AUTHENTICATE, sAuthenticate);
    return this;
  }

  public final boolean isRedirectDefined ()
  {
    return m_sRedirectTargetUrl != null;
  }

  @Nonnull
  public final UnifiedResponse setRedirect (@Nonnull final ISimpleURL aRedirectTargetUrl)
  {
    ValueEnforcer.notNull (aRedirectTargetUrl, "RedirectTargetUrl");

    return setRedirect (aRedirectTargetUrl, ERedirectMode.DEFAULT);
  }

  @Nonnull
  public final UnifiedResponse setRedirect (@Nonnull final ISimpleURL aRedirectTargetUrl,
                                            @Nonnull final ERedirectMode eRedirectMode)
  {
    ValueEnforcer.notNull (aRedirectTargetUrl, "RedirectTargetUrl");

    return setRedirect (aRedirectTargetUrl.getAsStringWithEncodedParameters (), eRedirectMode);
  }

  @Nonnull
  public final UnifiedResponse setRedirect (@Nonnull @Nonempty final String sRedirectTargetUrl)
  {
    return setRedirect (sRedirectTargetUrl, ERedirectMode.DEFAULT);
  }

  private static boolean _isRelative (@Nonnull final String sURL)
  {
    if (URLProtocolRegistry.getInstance ().hasKnownProtocol (sURL))
    {
      // Protocol is present
      return false;
    }
    if (sURL.startsWith ("//"))
    {
      // Special shortcut to stay in the current protocol - usually followed by
      // server name etc.
      return false;
    }
    return true;
  }

  @Nonnull
  public final UnifiedResponse setRedirect (@Nonnull @Nonempty final String sRedirectTargetUrl,
                                            @Nonnull final ERedirectMode eRedirectMode)
  {
    ValueEnforcer.notEmpty (sRedirectTargetUrl, "RedirectTargetUrl");
    ValueEnforcer.notNull (eRedirectMode, "RedirectMode");

    if (_isRelative (sRedirectTargetUrl))
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("The redirect target URL '" + sRedirectTargetUrl + "' seems to be relative.");

    if (isRedirectDefined ())
      logInfo ("Overwriting redirect target URL '" +
               m_sRedirectTargetUrl +
               "' with '" +
               sRedirectTargetUrl +
               "'" +
               (m_eRedirectMode == eRedirectMode ? "" : " and mode " + m_eRedirectMode + " with " + eRedirectMode));
    m_sRedirectTargetUrl = sRedirectTargetUrl;
    m_eRedirectMode = eRedirectMode;
    return this;
  }

  /**
   * @return <code>true</code> if warning on duplicated cookies is enabled,
   *         <code>false</code> if it is disabled.
   * @since 6.0.5
   */
  public final boolean isWarnOnDuplicateCookies ()
  {
    return m_bWarnOnDuplicateCookies;
  }

  /**
   * Enable or disable warning message on duplicated cookie names.
   *
   * @param bWarnOnDuplicateCookies
   *        <code>true</code> to enable warnings, <code>false</code> to disable
   *        them.
   * @return this
   * @since 6.0.5
   */
  @Nonnull
  public final UnifiedResponse setWarnOnDuplicateCookies (final boolean bWarnOnDuplicateCookies)
  {
    m_bWarnOnDuplicateCookies = bWarnOnDuplicateCookies;
    return this;
  }

  /**
   * Add the passed cookie.
   *
   * @param aCookie
   *        The cookie to be added. May not be <code>null</code>.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse addCookie (@Nonnull final Cookie aCookie)
  {
    ValueEnforcer.notNull (aCookie, "Cookie");

    final String sKey = aCookie.getName ();
    if (m_aCookies == null)
      m_aCookies = _createCookieMap ();
    else
    {
      if (m_bWarnOnDuplicateCookies && m_aCookies.containsKey (sKey))
        logWarn ("Overwriting cookie '" + sKey + "' with the new value '" + aCookie.getValue () + "'");
    }
    m_aCookies.put (sKey, aCookie);
    return this;
  }

  /**
   * Remove the cookie with the specified name.
   *
   * @param sName
   *        The name of the cookie to be removed. May be <code>null</code>.
   * @return this
   */
  @Nonnull
  public final UnifiedResponse removeCookie (@Nullable final String sName)
  {
    if (m_aCookies != null)
      m_aCookies.remove (sName);
    return this;
  }

  /**
   * Remove all cookies.
   *
   * @return {@link EChange#CHANGED} if at least one cookie was removed.
   * @since 6.0.5
   */
  @Nonnull
  public final EChange removeAllCookies ()
  {
    return m_aCookies == null ? EChange.UNCHANGED : m_aCookies.removeAll ();
  }

  /**
   * @return <code>true</code> if at least one cookie is present.
   * @since 6.0.5
   */
  public final boolean hasCookies ()
  {
    return m_aCookies != null && m_aCookies.isNotEmpty ();
  }

  /**
   * @return A copy of all contained cookies. Never <code>null</code> but maybe
   *         empty.
   * @since 6.0.5
   */
  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsOrderedMap <String, Cookie> getAllCookies ()
  {
    return new CommonsLinkedHashMap <> (m_aCookies);
  }

  /**
   * @return <code>true</code> if HTTP header values will be unified,
   *         <code>false</code> if not.
   * @see UnifiedResponseDefaultSettings#isHttpHeaderValuesUnified()
   * @since 9.1.4
   */
  public final boolean isHttpHeaderValuesUnified ()
  {
    return m_bHttpHeaderValuesUnified;
  }

  /**
   * Enable or disable the unification of HTTP header values.
   *
   * @param bHttpHeaderValuesUnified
   *        <code>true</code> to enable it, <code>false</code> to disable it.
   * @return this for chaining
   * @since 9.1.4
   */
  @Nonnull
  public final UnifiedResponse setHttpHeaderValuesUnified (final boolean bHttpHeaderValuesUnified)
  {
    m_bHttpHeaderValuesUnified = bHttpHeaderValuesUnified;
    return this;
  }

  /**
   * @return <code>true</code> if HTTP header values will be unified and quoted
   *         if necessary, <code>false</code> if not.
   * @see UnifiedResponseDefaultSettings#isHttpHeaderValuesQuoteIfNecessary()
   * @since 9.1.4
   */
  public final boolean isHttpHeaderValuesQuoteIfNecessary ()
  {
    return m_bHttpHeaderValuesQuoteIfNecessary;
  }

  /**
   * Enable or disable the automatic quoting of HTTP header values. This only
   * takes effect, if the unification is enabled.
   *
   * @param bHttpHeaderValuesQuoteIfNecessary
   *        <code>true</code> to enable it, <code>false</code> to disable it.
   * @return this for chaining
   * @see #setHttpHeaderValuesUnified(boolean)
   * @since 9.1.4
   */
  @Nonnull
  public final UnifiedResponse setHttpHeaderValuesQuoteIfNecessary (final boolean bHttpHeaderValuesQuoteIfNecessary)
  {
    m_bHttpHeaderValuesQuoteIfNecessary = bHttpHeaderValuesQuoteIfNecessary;
    return this;
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
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setAllowMimeSniffing (final boolean bAllow)
  {
    if (bAllow)
      removeCustomResponseHeaders (CHttpHeader.X_CONTENT_TYPE_OPTIONS);
    else
      setCustomResponseHeader (CHttpHeader.X_CONTENT_TYPE_OPTIONS, CHttpHeader.VALUE_NOSNIFF);
    return this;
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
   * @return this
   * @since 6.0.5
   */
  @Nonnull
  public final UnifiedResponse setEnableXSSFilter (final boolean bEnable)
  {
    if (bEnable)
      setCustomResponseHeader (CHttpHeader.X_XSS_PROTECTION, "1; mode=block");
    else
      removeCustomResponseHeaders (CHttpHeader.X_XSS_PROTECTION);
    return this;
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
   * @return this
   */
  @Nonnull
  public final UnifiedResponse setStrictTransportSecurity (final int nMaxAgeSeconds, final boolean bIncludeSubdomains)
  {
    setCustomResponseHeader (CHttpHeader.STRICT_TRANSPORT_SECURITY,
                             new CacheControlBuilder ().setMaxAgeSeconds (nMaxAgeSeconds).getAsHTTPHeaderValue () +
                                                                    (bIncludeSubdomains ? ";" +
                                                                                          CHttpHeader.VALUE_INCLUDE_SUBDOMAINS
                                                                                        : ""));
    return this;
  }

  /**
   * Remove the X-Frame-Options HTTP header if it is present.
   *
   * @return this
   * @since 6.0.5
   */
  @Nonnull
  public final UnifiedResponse removeStrictTransportSecurity ()
  {
    removeCustomResponseHeaders (CHttpHeader.STRICT_TRANSPORT_SECURITY);
    return this;
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
   * @return this
   * @since 6.0.5
   */
  @Nonnull
  public final UnifiedResponse setXFrameOptions (@Nonnull final EXFrameOptionType eType,
                                                 @Nullable final ISimpleURL aDomain)
  {
    ValueEnforcer.notNull (eType, "Type");
    if (eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    if (eType.isURLRequired ())
      setCustomResponseHeader (CHttpHeader.X_FRAME_OPTIONS,
                               eType.getID () + " " + aDomain.getAsStringWithEncodedParameters ());
    else
      setCustomResponseHeader (CHttpHeader.X_FRAME_OPTIONS, eType.getID ());
    return this;
  }

  /**
   * Remove the X-Frame-Options HTTP header if it is present.
   *
   * @return this
   * @since 6.0.5
   */
  @Nonnull
  public final UnifiedResponse removeXFrameOptions ()
  {
    removeCustomResponseHeaders (CHttpHeader.X_FRAME_OPTIONS);
    return this;
  }

  /**
   * Adds a response header to the response according to the passed name and
   * value. If an existing header with the same is present, the value is added
   * to the list so that the header is emitted more than once.<br>
   * <b>ATTENTION:</b> You should only use the APIs that {@link UnifiedResponse}
   * directly offers. Use this method only in emergency and make sure you
   * validate the header field and allowed value!
   *
   * @param sName
   *        Name of the header. May neither be <code>null</code> nor empty.
   * @param sValue
   *        Value of the header. May neither be <code>null</code> nor empty.
   */
  public final void addCustomResponseHeader (@Nonnull @Nonempty final String sName,
                                             @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    m_aResponseHeaderMap.addHeader (sName, sValue);
  }

  /**
   * Add many custom headers at once.
   *
   * @param aOther
   *        The headers to be added. May be <code>null</code>.
   */
  public final void addCustomResponseHeaders (@Nullable final HttpHeaderMap aOther)
  {
    if (aOther != null)
      m_aResponseHeaderMap.setAllHeaders (aOther);
  }

  /**
   * Sets a response header to the response according to the passed name and
   * value. An existing header entry with the same name is overridden.<br>
   * <b>ATTENTION:</b> You should only use the APIs that {@link UnifiedResponse}
   * directly offers. Use this method only in emergency and make sure you
   * validate the header field and allowed value!
   *
   * @param sName
   *        Name of the header. May neither be <code>null</code> nor empty.
   * @param sValue
   *        Value of the header. May neither be <code>null</code> nor empty.
   */
  public final void setCustomResponseHeader (@Nonnull @Nonempty final String sName,
                                             @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    m_aResponseHeaderMap.setHeader (sName, sValue);
  }

  /**
   * Set many custom headers at once. All existing headers are unconditionally
   * removed.
   *
   * @param aOther
   *        The headers to be set. May be <code>null</code>.
   */
  public final void setCustomResponseHeaders (@Nullable final HttpHeaderMap aOther)
  {
    m_aResponseHeaderMap.removeAll ();
    if (aOther != null)
      m_aResponseHeaderMap.setAllHeaders (aOther);
  }

  /**
   * Removes the response headers matching the passed name from the response.
   * <br>
   * <b>ATTENTION:</b> You should only use the APIs that {@link UnifiedResponse}
   * directly offers. Use this method only in emergency and make sure you
   * validate the header field and allowed value!
   *
   * @param sName
   *        Name of the header to be removed. May neither be <code>null</code>
   *        nor empty.
   * @return {@link EChange#CHANGED} in header was removed.
   */
  @Nonnull
  public final EChange removeCustomResponseHeaders (@Nonnull @Nonempty final String sName)
  {
    ValueEnforcer.notEmpty (sName, "Name");

    return m_aResponseHeaderMap.removeHeaders (sName);
  }

  private void _verifyCachingIntegrity ()
  {
    final boolean bIsHttp11 = m_eHttpVersion.isAtLeast11 ();
    final boolean bExpires = m_aResponseHeaderMap.containsHeaders (CHttpHeader.EXPIRES);
    final boolean bCacheControl = m_aCacheControl != null;
    final boolean bLastModified = m_aResponseHeaderMap.containsHeaders (CHttpHeader.LAST_MODIFIED);
    final boolean bETag = m_aResponseHeaderMap.containsHeaders (CHttpHeader.ETAG);

    if (bExpires && bIsHttp11)
      logInfo ("Expires found in HTTP 1.1 response: " + m_aResponseHeaderMap.getAllHeaderValues (CHttpHeader.EXPIRES));

    if (bExpires && bCacheControl)
      logWarn ("Expires and Cache-Control are both present. Cache-Control takes precedence!");

    if (bETag && !bIsHttp11)
      logWarn ("Sending an ETag for HTTP version " + m_eHttpVersion + " has no effect!");
    if (!bExpires && !bCacheControl)
    {
      if (bLastModified || bETag)
        logWarn ("Validators (Last-Modified and ETag) have no effect if no Expires or Cache-Control is present");
      else
        logWarn ("Response has no caching information at all");
    }
    if (m_aCacheControl != null)
    {
      if (!bIsHttp11)
        logWarn ("Sending a Cache-Control header for HTTP version " +
                 m_eHttpVersion +
                 " may have no or limited effect!");
      if (m_aCacheControl.isPrivate ())
      {
        if (m_aCacheControl.isPublic ())
          logWarn ("Cache-Control cannot be private and public at the same time");

        if (m_aCacheControl.hasMaxAgeSeconds ())
          logWarn ("Cache-Control cannot be private and have a max-age definition");

        if (m_aCacheControl.hasSharedMaxAgeSeconds ())
          logWarn ("Cache-Control cannot be private and have a s-maxage definition");
      }
    }
  }

  @Nonnull
  @Nonempty
  private static String _getAsStringMimeTypes (@Nonnull final ICommonsMap <IMimeType, QValue> aMap)
  {
    final StringBuilder aSB = new StringBuilder ().append ('{');
    for (final Map.Entry <IMimeType, QValue> aEntry : aMap.getSortedByValue (Comparator.naturalOrder ()).entrySet ())
    {
      if (aSB.length () > 1)
        aSB.append (", ");
      aSB.append (aEntry.getKey ().getAsString ()).append ('=').append (aEntry.getValue ().getQuality ());
    }
    return aSB.append ('}').toString ();
  }

  @Nonnull
  @Nonempty
  private static String _getAsStringText (@Nonnull final ICommonsMap <String, QValue> aMap)
  {
    final StringBuilder aSB = new StringBuilder ().append ('{');
    for (final Map.Entry <String, QValue> aEntry : aMap.getSortedByValue (Comparator.naturalOrder ()).entrySet ())
    {
      if (aSB.length () > 1)
        aSB.append (", ");
      aSB.append (aEntry.getKey ()).append ('=').append (aEntry.getValue ().getQuality ());
    }
    return aSB.append ('}').toString ();
  }

  private void _applyLengthChecks (final long nContentLength)
  {
    // Source:
    // http://joshua.perina.com/africa/gambia/fajara/post/internet-explorer-css-file-size-limit
    if (m_aMimeType != null &&
        m_aMimeType.equals (CMimeType.TEXT_CSS) &&
        nContentLength > (MAX_CSS_KB_FOR_IE * CGlobal.BYTES_PER_KILOBYTE_LONG))
    {
      logWarn ("Internet Explorer has problems handling CSS files > " +
               MAX_CSS_KB_FOR_IE +
               "KB and this one has " +
               nContentLength +
               " bytes!");
    }
  }

  private void _applyContent (@Nonnull final HttpServletResponse aHttpResponse, final boolean bStatusCodeWasAlreadySet)
                                                                                                                        throws IOException
  {
    if (m_aContentArray != null)
    {
      // We're having a fixed byte array of content
      final int nContentLength = m_nContentArrayLength;

      // Determine the response stream type to use
      final EResponseStreamType eResponseStreamType = ResponseHelper.getBestSuitableOutputStreamType (m_aHttpRequest);
      if (eResponseStreamType.isUncompressed ())
      {
        // Must be set before the content itself arrives
        // Note: Set it only if the content is uncompressed, because we cannot
        // determine the length of the compressed text in advance without
        // computational overhead
        ResponseHelper.setContentLength (aHttpResponse, nContentLength);
      }
      // Don't emit empty content or content for HEAD method
      if (nContentLength > 0 && m_eHttpMethod.isContentAllowed ())
      {
        // Create the correct stream
        try (final OutputStream aOS = ResponseHelper.getBestSuitableOutputStream (m_aHttpRequest, aHttpResponse))
        {
          // Emit main content to stream
          aOS.write (m_aContentArray, m_nContentArrayOfs, nContentLength);
          aOS.flush ();
        }
        _applyLengthChecks (nContentLength);
      }
      // Don't send 204, as this is most likely not handled correctly on the
      // client side
    }
    else
      if (m_aContentISP != null)
      {
        // We have a dynamic content input stream
        // -> no content length can be determined!
        final InputStream aContentIS = m_aContentISP.getInputStream ();
        if (aContentIS == null)
        {
          logError ("Failed to open input stream from " + m_aContentISP);

          // Handle it gracefully with a 404 and not with a 500
          aHttpResponse.setStatus (HttpServletResponse.SC_NOT_FOUND);
        }
        else
        {
          // Don't emit content for HEAD method
          if (m_eHttpMethod.isContentAllowed ())
          {
            // We do have an input stream
            // -> copy it to the response
            final OutputStream aOS = aHttpResponse.getOutputStream ();
            final MutableLong aByteCount = new MutableLong (0);
            if (StreamHelper.copyByteStream ()
                            .from (aContentIS)
                            .closeFrom (true)
                            .to (aOS)
                            .closeTo (true)
                            .copyByteCount (aByteCount)
                            .build ()
                            .isSuccess ())
            {
              // Copying succeeded
              final long nBytesCopied = aByteCount.longValue ();

              // Don't apply additional Content-Length header after the resource
              // was streamed!
              _applyLengthChecks (nBytesCopied);
            }
            else
            {
              // Copying failed -> this is a 500
              final boolean bResponseCommitted = aHttpResponse.isCommitted ();
              logError ("Copying from " +
                        m_aContentISP +
                        " failed after " +
                        aByteCount.longValue () +
                        " bytes! Response is committed: " +
                        bResponseCommitted);

              if (!bResponseCommitted)
                aHttpResponse.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
          }
        }
      }
      else
        if (!bStatusCodeWasAlreadySet)
        {
          // Set status 204 - no content; this is most likely a programming
          // error
          aHttpResponse.setStatus (HttpServletResponse.SC_NO_CONTENT);
          logWarn ("No content present for the response");
        }
  }

  public final void applyToResponse (@Nonnull final HttpServletResponse aHttpResponse) throws IOException
  {
    ValueEnforcer.notNull (aHttpResponse, "HttpResponse");

    // Apply all collected headers
    for (final Map.Entry <String, ICommonsList <String>> aEntry : m_aResponseHeaderMap)
    {
      final String sHeaderName = aEntry.getKey ();
      int nIndex = 0;
      for (final String sHeaderValue : aEntry.getValue ())
      {
        // Ensure single line values
        final String sUnifiedHeaderValue = m_bHttpHeaderValuesUnified ? HttpHeaderMap.getUnifiedValue (sHeaderValue,
                                                                                                       m_bHttpHeaderValuesQuoteIfNecessary)
                                                                      : sHeaderValue;
        if (nIndex == 0)
          aHttpResponse.setHeader (sHeaderName, sUnifiedHeaderValue);
        else
          aHttpResponse.addHeader (sHeaderName, sUnifiedHeaderValue);
        ++nIndex;
      }
    }
    final boolean bIsRedirect = isRedirectDefined ();
    final boolean bHasStatusCode = isStatusCodeDefined ();
    if (bIsRedirect)
    {
      if (bHasStatusCode)
        logWarn ("Ignoring provided status code because a redirect is specified!");
      if (!m_bAllowContentOnRedirect)
      {
        if (m_aCacheControl != null)
          logInfo ("Ignoring provided Cache-Control because a redirect is specified!");
        if (m_sContentDispositionFilename != null)
          logWarn ("Ignoring provided Content-Dispostion filename because a redirect is specified!");
        if (m_aMimeType != null)
          logWarn ("Ignoring provided MimeType because a redirect is specified!");
        if (m_aCharset != null)
          logWarn ("Ignoring provided charset because a redirect is specified!");
        if (hasContent ())
          logWarn ("Ignoring provided content because a redirect is specified!");
      }
      // Note: After using this method, the response should be
      // considered to be committed and should not be written to.
      String sRealTargetURL;
      if (ServletSettings.isEncodeURLs ())
      {
        try
        {
          sRealTargetURL = aHttpResponse.encodeRedirectURL (m_sRedirectTargetUrl);
        }
        catch (final IllegalArgumentException ex)
        {
          // Happens e.g. if "http://server/../" is requested
          logWarn ("Failed to encode redirect target URL '" + m_sRedirectTargetUrl + "': " + ex.getMessage ());
          sRealTargetURL = m_sRedirectTargetUrl;
        }
      }
      else
      {
        sRealTargetURL = m_sRedirectTargetUrl;
      }

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Response is a redirect to '" + sRealTargetURL + "' using mode " + m_eRedirectMode);

      switch (m_eRedirectMode)
      {
        case DEFAULT:
          aHttpResponse.sendRedirect (sRealTargetURL);
          break;
        case POST_REDIRECT_GET:
          if (m_eHttpVersion.is10 ())
          {
            // For HTTP 1.0 send 302
            aHttpResponse.setStatus (HttpServletResponse.SC_FOUND);
          }
          else
          {
            // For HTTP 1.1 send 303
            aHttpResponse.setStatus (HttpServletResponse.SC_SEE_OTHER);
          }
          // Set the location header
          aHttpResponse.addHeader (CHttpHeader.LOCATION, sRealTargetURL);
          break;
        default:
          throw new IllegalStateException ("Unimplemented redirect mode " + m_eRedirectMode + "!");
      }
      if (!m_bAllowContentOnRedirect)
        return;
    }

    if (bHasStatusCode)
    {
      if (bIsRedirect)
        logWarn ("Overriding provided redirect because a status code is specified!");
      if (!m_bAllowContentOnStatusCode)
      {
        if (m_aCacheControl != null)
          logInfo ("Ignoring provided Cache-Control because a status code is specified!");
        if (m_sContentDispositionFilename != null)
          logWarn ("Ignoring provided Content-Dispostion filename because a status code is specified!");
        if (m_aMimeType != null)
          logWarn ("Ignoring provided MimeType because a status code is specified!");
        if (m_aCharset != null)
          logWarn ("Ignoring provided charset because a status code is specified!");
        if (hasContent ())
          logWarn ("Ignoring provided content because a status code is specified!");
      }

      if (m_nStatusCode == HttpServletResponse.SC_UNAUTHORIZED &&
          !m_aResponseHeaderMap.containsHeaders (CHttpHeader.WWW_AUTHENTICATE))
      {
        logWarn ("Status code UNAUTHORIZED (401) is returned, but no " +
                 CHttpHeader.WWW_AUTHENTICATE +
                 " HTTP response header is set!");
      }

      // Content may be present so, sendError is not an option here!
      if (m_nStatusCode >= HttpServletResponse.SC_BAD_REQUEST && m_aContentArray == null)
      {
        // It's an error
        // Note: After using this method, the response should be considered
        // to be committed and should not be written to.
        aHttpResponse.sendError (m_nStatusCode);
      }
      else
      {
        // It's a status message "only"
        // Note: The container clears the buffer and sets the Location
        // header, preserving cookies and other headers.
        aHttpResponse.setStatus (m_nStatusCode);
      }
      if (!m_bAllowContentOnStatusCode)
        return;
      if (!hasContent ())
      {
        // Content is allowed on status code, but no content is contained -> no
        // need to
        // continue - would produce some warnings
        return;
      }
    }
    // Verify only if is a response with content
    _verifyCachingIntegrity ();
    if (m_aCacheControl != null)
    {
      final String sCacheControlValue = m_aCacheControl.getAsHTTPHeaderValue ();
      if (StringHelper.hasText (sCacheControlValue))
        aHttpResponse.setHeader (CHttpHeader.CACHE_CONTROL, sCacheControlValue);
      else
        logWarn ("An empty Cache-Control was provided!");
    }
    if (m_sContentDispositionFilename != null)
    {
      final StringBuilder aSB = new StringBuilder ();
      if (m_aRequestBrowserInfo != null &&
          m_aRequestBrowserInfo.getBrowserType () == EBrowserType.IE &&
          m_aRequestBrowserInfo.getVersion ().getMajor () <= 8)
      {
        // Special case for IE <= 8
        final Charset aCharsetToUse = m_aCharset != null ? m_aCharset : StandardCharsets.UTF_8;
        aSB.append (m_eContentDispositionType.getID ())
           .append ("; filename=")
           .append (URLHelper.urlEncode (m_sContentDispositionFilename, aCharsetToUse));
      }
      else
      {
        // Filename needs to be surrounded with double quotes (single quotes
        // don't work).
        aSB.append (m_eContentDispositionType.getID ())
           .append ("; filename=\"")
           .append (m_sContentDispositionFilename)
           .append ('"');

        // Check if we need an UTF-8 filename
        // http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http/6745788#6745788
        final String sRFC5987Filename = RFC5987Encoder.getRFC5987EncodedUTF8 (m_sContentDispositionFilename);
        if (!sRFC5987Filename.equals (m_sContentDispositionFilename))
          aSB.append ("; filename*=UTF-8''").append (sRFC5987Filename);
      }
      aHttpResponse.setHeader (CHttpHeader.CONTENT_DISPOSITION, aSB.toString ());
      if (m_aMimeType == null)
      {
        logWarn ("Content-Disposition is specified but no MimeType is set. Using the default download MimeType.");
        aHttpResponse.setContentType (CMimeType.APPLICATION_FORCE_DOWNLOAD.getAsString ());
      }
    }
    // Mime type
    if (m_aMimeType != null)
    {
      final String sMimeType = m_aMimeType.getAsString ();

      // Check with request accept mime types
      final QValue aQuality = m_aAcceptMimeTypeList.getQValueOfMimeType (m_aMimeType);
      if (aQuality.isMinimumQuality ())
      {
        final ICommonsOrderedMap <IMimeType, QValue> aBetterValues = m_aAcceptMimeTypeList.getAllQValuesGreaterThan (aQuality.getQuality ());
        logError ("MimeType '" +
                  sMimeType +
                  "' is not at all supported by the request. Allowed values are: " +
                  _getAsStringMimeTypes (aBetterValues));
      }
      else
        if (aQuality.isLowValue ())
        {
          // This might bloat the logfile for text/css MIME types and therefore
          // only in the debug version
          if (GlobalDebug.isDebugMode ())
          {
            // Inform if the quality of the request is <= 50%!
            final ICommonsOrderedMap <IMimeType, QValue> aBetterValues = m_aAcceptMimeTypeList.getAllQValuesGreaterThan (aQuality.getQuality ());
            if (!aBetterValues.isEmpty ())
              logWarn ("MimeType '" +
                       sMimeType +
                       "' is not best supported by the request (" +
                       aQuality +
                       "). Better MimeTypes are: " +
                       _getAsStringMimeTypes (aBetterValues));
          }
        }
      aHttpResponse.setContentType (sMimeType);
    }
    else
    {
      logWarn ("No MimeType present");
    }
    // Charset
    if (m_aCharset != null)
    {
      final String sCharset = m_aCharset.name ();
      if (m_aMimeType == null)
        logWarn ("If no MimeType present, the client cannot get notified about the character encoding '" +
                 sCharset +
                 "'");

      // Check with request charset
      final QValue aQuality = m_aAcceptCharsetList.getQValueOfCharset (sCharset);
      if (aQuality.isMinimumQuality ())
      {
        final ICommonsOrderedMap <String, QValue> aBetterValues = m_aAcceptCharsetList.getAllQValuesGreaterThan (aQuality.getQuality ());
        logError ("Character encoding '" +
                  sCharset +
                  "' is not at all supported by the request. Allowed values are: " +
                  _getAsStringText (aBetterValues));
      }
      else
        if (aQuality.isLowValue ())
        {
          // Inform if the quality of the request is <= 50%!
          final ICommonsOrderedMap <String, QValue> aBetterValues = m_aAcceptCharsetList.getAllQValuesGreaterThan (aQuality.getQuality ());
          if (!aBetterValues.isEmpty ())
            logWarn ("Character encoding '" +
                     sCharset +
                     "' is not best supported by the request (" +
                     aQuality +
                     "). Better charsets are: " +
                     _getAsStringText (aBetterValues));
        }
      aHttpResponse.setCharacterEncoding (sCharset);
    }
    else
      if (m_aMimeType == null)
      {
        logWarn ("Also no character encoding present");
      }
      else
        switch (m_aMimeType.getContentType ())
        {
          case TEXT:
          case MULTIPART:
            logWarn ("A character encoding for MimeType '" + m_aMimeType.getAsString () + "' is appreciated.");
            break;
          default:
            // Do we need character encoding here as well???
            break;
        }
    // Add all cookies
    if (m_aCookies != null)
      for (final Cookie aCookie : m_aCookies.values ())
        aHttpResponse.addCookie (aCookie);

    // Write the body to the response
    _applyContent (aHttpResponse, bHasStatusCode);
  }

  /**
   * Factory method
   *
   * @param aHttpRequest
   *        The main HTTP request. May not be <code>null</code>.
   * @return New {@link UnifiedResponse}. Never <code>null</code>.
   * @since 8.8.0
   */
  @Nonnull
  public static UnifiedResponse createSimple (@Nonnull final HttpServletRequest aHttpRequest)
  {
    return new UnifiedResponse (RequestHelper.getHttpVersion (aHttpRequest),
                                RequestHelper.getHttpMethod (aHttpRequest),
                                aHttpRequest);
  }
}
