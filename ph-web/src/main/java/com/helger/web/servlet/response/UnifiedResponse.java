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
package com.helger.web.servlet.response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.charset.CCharset;
import com.helger.commons.charset.CharsetManager;
import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsOrderedMap;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.debug.GlobalDebug;
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
import com.helger.web.encoding.RFC5987Encoder;
import com.helger.web.http.AcceptCharsetHandler;
import com.helger.web.http.AcceptCharsetList;
import com.helger.web.http.AcceptMimeTypeHandler;
import com.helger.web.http.AcceptMimeTypeList;
import com.helger.web.http.CHTTPHeader;
import com.helger.web.http.CacheControlBuilder;
import com.helger.web.http.EHTTPMethod;
import com.helger.web.http.EHTTPVersion;
import com.helger.web.http.HTTPHeaderMap;
import com.helger.web.http.QValue;
import com.helger.web.scope.util.RequestHelper;
import com.helger.web.scope.util.RequestLogger;
import com.helger.web.useragent.browser.BrowserInfo;
import com.helger.web.useragent.browser.EBrowserType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
  public static final boolean DEFAULT_ALLOW_CONTENT_ON_STATUS_CODE = false;
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

  private static final Logger s_aLogger = LoggerFactory.getLogger (UnifiedResponse.class);
  private static final AtomicInteger s_aResponseNum = new AtomicInteger (0);

  // Input fields set from request
  private final EHTTPVersion m_eHTTPVersion;
  private final EHTTPMethod m_eHTTPMethod;
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
  private byte [] m_aContent;
  private IHasInputStream m_aContentISP;
  private EContentDispositionType m_eContentDispositionType = DEFAULT_CONTENT_DISPOSITION_TYPE;
  private String m_sContentDispositionFilename;
  private CacheControlBuilder m_aCacheControl;
  private final HTTPHeaderMap m_aResponseHeaderMap = new HTTPHeaderMap ();
  private int m_nStatusCode = CGlobal.ILLEGAL_UINT;
  private String m_sRedirectTargetUrl;
  private ERedirectMode m_eRedirectMode;
  private boolean m_bWarnOnDuplicateCookies = DEFAULT_WARN_ON_DUPLICATE_COOKIES;
  private ICommonsOrderedMap <String, Cookie> m_aCookies;

  // Internal status members
  /**
   * Unique internal ID for each response, so that error messages can be more
   * easily aggregated.
   */
  private final int m_nResponseID = s_aResponseNum.incrementAndGet ();

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
  private final HTTPHeaderMap m_aRequestHeaderMap;

  /**
   * An optional encode to be used to determine if a content-disposition
   * filename can be ISO-8859-1 encoded.
   */
  private CharsetEncoder m_aContentDispositionEncoder;

  @Nonnull
  @ReturnsMutableCopy
  private static ICommonsOrderedMap <String, Cookie> _createCookieMap ()
  {
    return new CommonsLinkedHashMap <> ();
  }

  /**
   * Constructor
   *
   * @param aHttpRequest
   *        The main HTTP request
   */
  public UnifiedResponse (@Nonnull final HttpServletRequest aHttpRequest)
  {
    this (RequestHelper.getHttpVersion (aHttpRequest), RequestHelper.getHttpMethod (aHttpRequest), aHttpRequest);
  }

  /**
   * Constructor
   *
   * @param eHTTPVersion
   *        HTTP version of this request (1.0 or 1.1)
   * @param eHTTPMethod
   *        HTTP method of this request (GET, POST, ...)
   * @param aHttpRequest
   *        The main HTTP request
   */
  public UnifiedResponse (@Nonnull final EHTTPVersion eHTTPVersion,
                          @Nonnull final EHTTPMethod eHTTPMethod,
                          @Nonnull final HttpServletRequest aHttpRequest)
  {
    m_eHTTPVersion = ValueEnforcer.notNull (eHTTPVersion, "HTTPVersion");
    m_eHTTPMethod = ValueEnforcer.notNull (eHTTPMethod, "HTTPMethod");
    m_aHttpRequest = ValueEnforcer.notNull (aHttpRequest, "HTTPRequest");
    m_aAcceptCharsetList = AcceptCharsetHandler.getAcceptCharsets (aHttpRequest);
    m_aAcceptMimeTypeList = AcceptMimeTypeHandler.getAcceptMimeTypes (aHttpRequest);
    m_aRequestHeaderMap = RequestHelper.getRequestHeaderMap (aHttpRequest);

    // Copy all default settings
    m_aResponseHeaderMap.addAllHeaders (UnifiedResponseDefaultSettings.getResponseHeaderMap ());
    if (UnifiedResponseDefaultSettings.hasCookies ())
    {
      m_aCookies = _createCookieMap ();
      m_aCookies.putAll (UnifiedResponseDefaultSettings.getAllCookies ());
    }
  }

  @Nonnull
  @Nonempty
  private String _getRequestURL ()
  {
    if (m_sRequestURL == null)
      m_sRequestURL = RequestHelper.getURL (m_aHttpRequest);
    return m_sRequestURL;
  }

  @Nonnull
  @Nonempty
  private String _getLogPrefix ()
  {
    return "UnifiedResponse[" + m_nResponseID + "] to [" + _getRequestURL () + "]: ";
  }

  private void _info (@Nonnull final String sMsg)
  {
    s_aLogger.info (_getLogPrefix () + sMsg);
  }

  private void _showRequestInfo ()
  {
    if (!m_bAlreadyEmittedRequestHeaders)
    {
      s_aLogger.warn ("  Request Headers: " +
                      RequestLogger.getHTTPHeaderMap (m_aRequestHeaderMap).getSortedByKey (Comparator.naturalOrder ()));
      if (!m_aResponseHeaderMap.isEmpty ())
        s_aLogger.warn ("  Response Headers: " +
                        RequestLogger.getHTTPHeaderMap (m_aResponseHeaderMap)
                                     .getSortedByKey (Comparator.naturalOrder ()));
      m_bAlreadyEmittedRequestHeaders = true;
    }
  }

  private void _warn (@Nonnull final String sMsg)
  {
    s_aLogger.warn (_getLogPrefix () + sMsg);
    _showRequestInfo ();
  }

  private void _error (@Nonnull final String sMsg)
  {
    s_aLogger.error (_getLogPrefix () + sMsg);
    _showRequestInfo ();
  }

  /**
   * @return The HTTP version of the request. Never <code>null</code>.
   */
  @Nonnull
  public final EHTTPVersion getHTTPVersion ()
  {
    return m_eHTTPVersion;
  }

  /**
   * @return The HTTP method of the request. Never <code>null</code>.
   */
  @Nonnull
  public final EHTTPMethod getHTTPMethod ()
  {
    return m_eHTTPMethod;
  }

  /**
   * @return The browser info of the request. Never <code>null</code>.
   */
  @Nullable
  public BrowserInfo getRequestBrowserInfo ()
  {
    return m_aRequestBrowserInfo;
  }

  @Nonnull
  public UnifiedResponse setRequestBrowserInfo (@Nullable final BrowserInfo aRequestBrowserInfo)
  {
    m_aRequestBrowserInfo = aRequestBrowserInfo;
    return this;
  }

  /**
   * @return <code>true</code> if content is allowed even if a redirect is
   *         present.
   */
  public boolean isAllowContentOnRedirect ()
  {
    return m_bAllowContentOnRedirect;
  }

  @Nonnull
  public UnifiedResponse setAllowContentOnRedirect (final boolean bAllowContentOnRedirect)
  {
    m_bAllowContentOnRedirect = bAllowContentOnRedirect;
    return this;
  }

  /**
   * @return <code>true</code> if content is allowed even if a status code is
   *         present.
   */
  public boolean isAllowContentOnStatusCode ()
  {
    return m_bAllowContentOnStatusCode;
  }

  @Nonnull
  public UnifiedResponse setAllowContentOnStatusCode (final boolean bAllowContentOnStatusCode)
  {
    m_bAllowContentOnStatusCode = bAllowContentOnStatusCode;
    return this;
  }

  @Nullable
  public Charset getCharset ()
  {
    return m_aCharset;
  }

  @Nonnull
  public UnifiedResponse setCharset (@Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (aCharset, "Charset");
    if (m_aCharset != null)
      _info ("Overwriting charset from " + m_aCharset + " to " + aCharset);
    m_aCharset = aCharset;
    return this;
  }

  @Nonnull
  public UnifiedResponse removeCharset ()
  {
    m_aCharset = null;
    return this;
  }

  @Nullable
  public IMimeType getMimeType ()
  {
    return m_aMimeType;
  }

  @Nonnull
  public UnifiedResponse setMimeType (@Nonnull final IMimeType aMimeType)
  {
    ValueEnforcer.notNull (aMimeType, "MimeType");
    if (m_aMimeType != null)
      _info ("Overwriting MimeType from " + m_aMimeType + " to " + aMimeType);
    m_aMimeType = aMimeType;
    return this;
  }

  @Nonnull
  public UnifiedResponse setMimeTypeString (@Nonnull @Nonempty final String sMimeType)
  {
    ValueEnforcer.notEmpty (sMimeType, "MimeType");

    final IMimeType aMimeType = MimeTypeParser.parseMimeType (sMimeType);
    if (aMimeType != null)
      setMimeType (aMimeType);
    else
      _error ("Failed to resolve mime type from '" + sMimeType + "'");
    return this;
  }

  @Nonnull
  public UnifiedResponse removeMimeType ()
  {
    m_aMimeType = null;
    return this;
  }

  /**
   * @return <code>true</code> if a content was already set, <code>false</code>
   *         if not.
   */
  public boolean hasContent ()
  {
    return m_aContent != null || m_aContentISP != null;
  }

  /**
   * Utility method to set an empty response content.
   *
   * @return this
   */
  @Nonnull
  public UnifiedResponse setEmptyContent ()
  {
    return setContent (new byte [0]);
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
  public UnifiedResponse setContentAndCharset (@Nonnull final String sContent, @Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (sContent, "Content");
    setCharset (aCharset);
    setContent (CharsetManager.getAsBytes (sContent, aCharset));
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
  @SuppressFBWarnings ("EI_EXPOSE_REP2")
  public UnifiedResponse setContent (@Nonnull final byte [] aContent)
  {
    ValueEnforcer.notNull (aContent, "Content");
    if (hasContent ())
      _info ("Overwriting content with byte array!");
    m_aContent = aContent;
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
  public UnifiedResponse setContent (@Nonnull final IHasInputStream aISP)
  {
    ValueEnforcer.notNull (aISP, "InputStreamProvider");
    if (hasContent ())
      _info ("Overwriting content with content provider!");
    m_aContent = null;
    m_aContentISP = aISP;
    return this;
  }

  @Nonnull
  public UnifiedResponse removeContent ()
  {
    m_aContent = null;
    m_aContentISP = null;
    return this;
  }

  @Nonnull
  public UnifiedResponse setExpires (@Nonnull final LocalDateTime aDT)
  {
    m_aResponseHeaderMap.setDateHeader (CHTTPHeader.EXPIRES, aDT);
    return this;
  }

  @Nonnull
  public UnifiedResponse removeExpires ()
  {
    m_aResponseHeaderMap.removeHeaders (CHTTPHeader.EXPIRES);
    return this;
  }

  @Nonnull
  public UnifiedResponse setLastModified (@Nonnull final LocalDateTime aDT)
  {
    if (m_eHTTPMethod != EHTTPMethod.GET && m_eHTTPMethod != EHTTPMethod.HEAD)
      _warn ("Setting Last-Modified on a non GET or HEAD request may have no impact!");

    m_aResponseHeaderMap.setDateHeader (CHTTPHeader.LAST_MODIFIED, aDT);
    return this;
  }

  @Nonnull
  public UnifiedResponse removeLastModified ()
  {
    m_aResponseHeaderMap.removeHeaders (CHTTPHeader.LAST_MODIFIED);
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
  public UnifiedResponse setETag (@Nonnull @Nonempty final String sETag)
  {
    ValueEnforcer.notEmpty (sETag, "ETag");
    if (!sETag.startsWith ("\"") && !sETag.startsWith ("W/\""))
      throw new IllegalArgumentException ("Etag must start with a '\"' character or with 'W/\"': " + sETag);
    if (!sETag.endsWith ("\""))
      throw new IllegalArgumentException ("Etag must end with a '\"' character: " + sETag);
    if (m_eHTTPMethod != EHTTPMethod.GET)
      _warn ("Setting an ETag on a non-GET request may have no impact!");

    m_aResponseHeaderMap.setHeader (CHTTPHeader.ETAG, sETag);
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
  public UnifiedResponse setETagIfApplicable (@Nonnull @Nonempty final String sETag)
  {
    if (m_eHTTPVersion == EHTTPVersion.HTTP_11)
      setETag (sETag);
    return this;
  }

  @Nonnull
  public UnifiedResponse removeETag ()
  {
    m_aResponseHeaderMap.removeHeaders (CHTTPHeader.ETAG);
    return this;
  }

  @Nonnull
  public UnifiedResponse setContentDispositionType (@Nonnull final EContentDispositionType eContentDispositionType)
  {
    ValueEnforcer.notNull (eContentDispositionType, "ContentDispositionType");

    m_eContentDispositionType = eContentDispositionType;
    return this;
  }

  @Nonnull
  public EContentDispositionType getContentDispositionType ()
  {
    return m_eContentDispositionType;
  }

  @Nonnull
  public UnifiedResponse setContentDispositionFilename (@Nonnull @Nonempty final String sFilename)
  {
    ValueEnforcer.notEmpty (sFilename, "Filename");

    // Ensure that a valid filename is used
    // -> Strip all paths and replace all invalid characters
    final String sFilenameToUse = FilenameHelper.getWithoutPath (FilenameHelper.getAsSecureValidFilename (sFilename));
    if (!sFilename.equals (sFilenameToUse))
      _warn ("Content-Dispostion filename was internally modified from '" +
             sFilename +
             "' to '" +
             sFilenameToUse +
             "'");

    // Disabled because of the extended UTF-8 handling (RFC 5987)
    if (false)
    {
      // Check if encoding as ISO-8859-1 is possible
      if (m_aContentDispositionEncoder == null)
        m_aContentDispositionEncoder = CCharset.CHARSET_ISO_8859_1_OBJ.newEncoder ();
      if (!m_aContentDispositionEncoder.canEncode (sFilenameToUse))
        _error ("Content-Dispostion filename '" + sFilenameToUse + "' cannot be encoded to ISO-8859-1!");
    }

    // Are we overwriting?
    if (m_sContentDispositionFilename != null)
      _info ("Overwriting Content-Dispostion filename from '" +
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

  @Nullable
  public String getContentDispositionFilename ()
  {
    return m_sContentDispositionFilename;
  }

  @Nonnull
  public UnifiedResponse removeContentDispositionFilename ()
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
  public UnifiedResponse setDownloadFilename (@Nonnull @Nonempty final String sFilename)
  {
    setMimeType (CMimeType.APPLICATION_FORCE_DOWNLOAD);
    setContentDispositionFilename (sFilename);
    return this;
  }

  @Nonnull
  public UnifiedResponse setCacheControl (@Nonnull final CacheControlBuilder aCacheControl)
  {
    ValueEnforcer.notNull (aCacheControl, "CacheControl");

    if (m_aCacheControl != null)
      _info ("Overwriting Cache-Control data from '" +
             m_aCacheControl.getAsHTTPHeaderValue () +
             "' to '" +
             aCacheControl.getAsHTTPHeaderValue () +
             "'");
    m_aCacheControl = aCacheControl;
    return this;
  }

  @Nullable
  @ReturnsMutableObject ("Design")
  public CacheControlBuilder getCacheControl ()
  {
    return m_aCacheControl;
  }

  @Nonnull
  public UnifiedResponse removeCacheControl ()
  {
    m_aCacheControl = null;
    return this;
  }

  /**
   * @return The non-<code>null</code> header map.
   */
  @Nonnull
  @ReturnsMutableObject ("design")
  protected HTTPHeaderMap getResponseHeaderMap ()
  {
    return m_aResponseHeaderMap;
  }

  /**
   * Remove all settings and headers relevant to caching.
   *
   * @return this for chaining
   */
  @Nonnull
  public UnifiedResponse removeCaching ()
  {
    // Remove any eventually set headers
    removeExpires ();
    removeCacheControl ();
    removeETag ();
    removeLastModified ();
    m_aResponseHeaderMap.removeHeaders (CHTTPHeader.PRAGMA);
    return this;
  }

  /**
   * A utility method that disables caching for this response.
   *
   * @return this
   */
  @Nonnull
  public UnifiedResponse disableCaching ()
  {
    // Remove any eventually set headers
    removeCaching ();

    switch (m_eHTTPVersion)
    {
      case HTTP_10:
      {
        // Set to expire far in the past for HTTP/1.0.
        m_aResponseHeaderMap.setHeader (CHTTPHeader.EXPIRES, ResponseHelperSettings.EXPIRES_NEVER_STRING);

        // Set standard HTTP/1.0 no-cache header.
        m_aResponseHeaderMap.setHeader (CHTTPHeader.PRAGMA, "no-cache");
        break;
      }
      case HTTP_11:
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
        break;
      }
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
  public UnifiedResponse enableCaching (@Nonnegative final int nSeconds)
  {
    ValueEnforcer.isGT0 (nSeconds, "Seconds");

    // Remove any eventually set headers
    // Note: don't remove Last-Modified and ETag!
    removeExpires ();
    removeCacheControl ();
    m_aResponseHeaderMap.removeHeaders (CHTTPHeader.PRAGMA);

    switch (m_eHTTPVersion)
    {
      case HTTP_10:
      {
        m_aResponseHeaderMap.setDateHeader (CHTTPHeader.EXPIRES,
                                            PDTFactory.getCurrentLocalDate ().plus (nSeconds, ChronoUnit.SECONDS));
        break;
      }
      case HTTP_11:
      {
        final CacheControlBuilder aCacheControlBuilder = new CacheControlBuilder ().setPublic (true)
                                                                                   .setMaxAgeSeconds (nSeconds);
        setCacheControl (aCacheControlBuilder);
        break;
      }
    }
    return this;
  }

  /**
   * @return <code>true</code> if a status code is defined, <code>false</code>
   *         if not.
   */
  public boolean isStatusCodeDefined ()
  {
    return m_nStatusCode != CGlobal.ILLEGAL_UINT;
  }

  /**
   * @return The HTTP status code defined or {@link CGlobal#ILLEGAL_UINT} if
   *         undefined.
   * @see #isStatusCodeDefined()
   */
  public int getStatusCode ()
  {
    return m_nStatusCode;
  }

  private void _setStatus (@Nonnegative final int nStatusCode)
  {
    if (isStatusCodeDefined ())
      _info ("Overwriting status code " + m_nStatusCode + " with " + nStatusCode);
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
  public UnifiedResponse setStatus (@Nonnegative final int nStatusCode)
  {
    _setStatus (nStatusCode);
    return this;
  }

  /**
   * Special handling for returning status code 401 UNAUTHORIZED.
   *
   * @param sAuthenticate
   *        The string to be used for the {@link CHTTPHeader#WWW_AUTHENTICATE}
   *        response header. May be <code>null</code> or empty.
   * @return this
   */
  @Nonnull
  public UnifiedResponse setStatusUnauthorized (@Nullable final String sAuthenticate)
  {
    _setStatus (HttpServletResponse.SC_UNAUTHORIZED);
    if (StringHelper.hasText (sAuthenticate))
      m_aResponseHeaderMap.setHeader (CHTTPHeader.WWW_AUTHENTICATE, sAuthenticate);
    return this;
  }

  public boolean isRedirectDefined ()
  {
    return m_sRedirectTargetUrl != null;
  }

  @Nonnull
  public UnifiedResponse setRedirect (@Nonnull final ISimpleURL aRedirectTargetUrl)
  {
    ValueEnforcer.notNull (aRedirectTargetUrl, "RedirectTargetUrl");

    return setRedirect (aRedirectTargetUrl, ERedirectMode.DEFAULT);
  }

  @Nonnull
  public UnifiedResponse setRedirect (@Nonnull final ISimpleURL aRedirectTargetUrl,
                                      @Nonnull final ERedirectMode eRedirectMode)
  {
    ValueEnforcer.notNull (aRedirectTargetUrl, "RedirectTargetUrl");

    return setRedirect (aRedirectTargetUrl.getAsStringWithEncodedParameters (), eRedirectMode);
  }

  @Nonnull
  public UnifiedResponse setRedirect (@Nonnull @Nonempty final String sRedirectTargetUrl)
  {
    return setRedirect (sRedirectTargetUrl, ERedirectMode.DEFAULT);
  }

  @Nonnull
  public UnifiedResponse setRedirect (@Nonnull @Nonempty final String sRedirectTargetUrl,
                                      @Nonnull final ERedirectMode eRedirectMode)
  {
    ValueEnforcer.notEmpty (sRedirectTargetUrl, "RedirectTargetUrl");
    ValueEnforcer.notNull (eRedirectMode, "RedirectMode");

    if (isRedirectDefined ())
      _info ("Overwriting redirect target URL '" + m_sRedirectTargetUrl + "' with '" + sRedirectTargetUrl + "'");
    m_sRedirectTargetUrl = sRedirectTargetUrl;
    m_eRedirectMode = eRedirectMode;
    return this;
  }

  /**
   * @return <code>true</code> if warning on duplicated cookies is enabled,
   *         <code>false</code> if it is disabled.
   * @since 6.0.5
   */
  public boolean isWarnOnDuplicateCookies ()
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
  public UnifiedResponse setWarnOnDuplicateCookies (final boolean bWarnOnDuplicateCookies)
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
  public UnifiedResponse addCookie (@Nonnull final Cookie aCookie)
  {
    ValueEnforcer.notNull (aCookie, "Cookie");

    final String sKey = aCookie.getName ();
    if (m_aCookies == null)
      m_aCookies = _createCookieMap ();
    else
    {
      if (m_bWarnOnDuplicateCookies && m_aCookies.containsKey (sKey))
        _warn ("Overwriting cookie '" + sKey + "' with the new value '" + aCookie.getValue () + "'");
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
  public UnifiedResponse removeCookie (@Nullable final String sName)
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
  public EChange removeAllCookies ()
  {
    return m_aCookies == null ? EChange.UNCHANGED : m_aCookies.removeAll ();
  }

  /**
   * @return <code>true</code> if at least one cookie is present.
   * @since 6.0.5
   */
  public boolean hasCookies ()
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
  protected ICommonsOrderedMap <String, Cookie> getAllCookies ()
  {
    return new CommonsLinkedHashMap <> (m_aCookies);
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
  public UnifiedResponse setAllowMimeSniffing (final boolean bAllow)
  {
    if (bAllow)
      removeCustomResponseHeaders (CHTTPHeader.X_CONTENT_TYPE_OPTIONS);
    else
      setCustomResponseHeader (CHTTPHeader.X_CONTENT_TYPE_OPTIONS, CHTTPHeader.VALUE_NOSNIFF);
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
  public UnifiedResponse setEnableXSSFilter (final boolean bEnable)
  {
    if (bEnable)
      setCustomResponseHeader (CHTTPHeader.X_XSS_PROTECTION, "1; mode=block");
    else
      removeCustomResponseHeaders (CHTTPHeader.X_XSS_PROTECTION);
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
  public UnifiedResponse setStrictTransportSecurity (final int nMaxAgeSeconds, final boolean bIncludeSubdomains)
  {
    setCustomResponseHeader (CHTTPHeader.STRICT_TRANSPORT_SECURITY,
                             new CacheControlBuilder ().setMaxAgeSeconds (nMaxAgeSeconds).getAsHTTPHeaderValue () +
                                                                    (bIncludeSubdomains ? ";" +
                                                                                          CHTTPHeader.VALUE_INCLUDE_SUBDOMAINS
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
  public UnifiedResponse removeStrictTransportSecurity ()
  {
    removeCustomResponseHeaders (CHTTPHeader.STRICT_TRANSPORT_SECURITY);
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
  public UnifiedResponse setXFrameOptions (@Nonnull final EXFrameOptionType eType, @Nullable final ISimpleURL aDomain)
  {
    ValueEnforcer.notNull (eType, "Type");
    if (eType.isURLRequired ())
      ValueEnforcer.notNull (aDomain, "Domain");

    if (eType.isURLRequired ())
      setCustomResponseHeader (CHTTPHeader.X_FRAME_OPTIONS,
                               eType.getID () + " " + aDomain.getAsStringWithEncodedParameters ());
    else
      setCustomResponseHeader (CHTTPHeader.X_FRAME_OPTIONS, eType.getID ());
    return this;
  }

  /**
   * Remove the X-Frame-Options HTTP header if it is present.
   *
   * @return this
   * @since 6.0.5
   */
  @Nonnull
  public UnifiedResponse removeXFrameOptions ()
  {
    removeCustomResponseHeaders (CHTTPHeader.X_FRAME_OPTIONS);
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
  public void addCustomResponseHeader (@Nonnull @Nonempty final String sName, @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    m_aResponseHeaderMap.addHeader (sName, sValue);
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
  public void setCustomResponseHeader (@Nonnull @Nonempty final String sName, @Nonnull @Nonempty final String sValue)
  {
    ValueEnforcer.notEmpty (sName, "Name");
    ValueEnforcer.notEmpty (sValue, "Value");

    m_aResponseHeaderMap.setHeader (sName, sValue);
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
  public EChange removeCustomResponseHeaders (@Nonnull @Nonempty final String sName)
  {
    ValueEnforcer.notEmpty (sName, "Name");

    return m_aResponseHeaderMap.removeHeaders (sName);
  }

  private void _verifyCachingIntegrity ()
  {
    final boolean bIsHttp11 = m_eHTTPVersion == EHTTPVersion.HTTP_11;
    final boolean bExpires = m_aResponseHeaderMap.containsHeaders (CHTTPHeader.EXPIRES);
    final boolean bCacheControl = m_aCacheControl != null;
    final boolean bLastModified = m_aResponseHeaderMap.containsHeaders (CHTTPHeader.LAST_MODIFIED);
    final boolean bETag = m_aResponseHeaderMap.containsHeaders (CHTTPHeader.ETAG);

    if (bExpires && bIsHttp11)
      _info ("Expires found in HTTP 1.1 response: " + m_aResponseHeaderMap.getAllHeaderValues (CHTTPHeader.EXPIRES));

    if (bExpires && bCacheControl)
      _warn ("Expires and Cache-Control are both present. Cache-Control takes precedence!");

    if (bETag && !bIsHttp11)
      _warn ("Sending an ETag for HTTP version " + m_eHTTPVersion + " has no effect!");

    if (!bExpires && !bCacheControl)
    {
      if (bLastModified || bETag)
        _warn ("Validators (Last-Modified and ETag) have no effect if no Expires or Cache-Control is present");
      else
        _warn ("Response has no caching information at all");
    }

    if (m_aCacheControl != null)
    {
      if (!bIsHttp11)
        _warn ("Sending a Cache-Control header for HTTP version " + m_eHTTPVersion + " may have no or limited effect!");

      if (m_aCacheControl.isPrivate ())
      {
        if (m_aCacheControl.isPublic ())
          _warn ("Cache-Control cannot be private and public at the same time");

        if (m_aCacheControl.hasMaxAgeSeconds ())
          _warn ("Cache-Control cannot be private and have a max-age definition");

        if (m_aCacheControl.hasSharedMaxAgeSeconds ())
          _warn ("Cache-Control cannot be private and have a s-maxage definition");
      }
    }
  }

  @Nonnull
  @Nonempty
  private static String _getAsStringMimeTypes (@Nonnull final ICommonsOrderedMap <IMimeType, QValue> aMap)
  {
    final StringBuilder aSB = new StringBuilder ("{");
    for (final Map.Entry <IMimeType, QValue> aEntry : aMap.getSortedByValue (Comparator.naturalOrder ()).entrySet ())
    {
      if (aSB.length () > 1)
        aSB.append (", ");
      aSB.append (aEntry.getKey ().getAsString ()).append ('=').append (aEntry.getValue ().getQuality ());
    }
    return aSB.append ("}").toString ();
  }

  @Nonnull
  @Nonempty
  private static String _getAsStringText (@Nonnull final ICommonsOrderedMap <String, QValue> aMap)
  {
    final StringBuilder aSB = new StringBuilder ("{");
    for (final Map.Entry <String, QValue> aEntry : aMap.getSortedByValue (Comparator.naturalOrder ()).entrySet ())
    {
      if (aSB.length () > 1)
        aSB.append (", ");
      aSB.append (aEntry.getKey ()).append ('=').append (aEntry.getValue ().getQuality ());
    }
    return aSB.append ("}").toString ();
  }

  private void _applyLengthChecks (final long nContentLength)
  {
    // Source:
    // http://joshua.perina.com/africa/gambia/fajara/post/internet-explorer-css-file-size-limit
    if (m_aMimeType != null &&
        m_aMimeType.equals (CMimeType.TEXT_CSS) &&
        nContentLength > (MAX_CSS_KB_FOR_IE * CGlobal.BYTES_PER_KILOBYTE_LONG))
    {
      _warn ("Internet Explorer has problems handling CSS files > " +
             MAX_CSS_KB_FOR_IE +
             "KB and this one has " +
             nContentLength +
             " bytes!");
    }
  }

  private void _applyContent (@Nonnull final HttpServletResponse aHttpResponse) throws IOException
  {
    if (m_aContent != null)
    {
      // We're having a fixed byte array of content
      final int nContentLength = m_aContent.length;

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
      if (nContentLength > 0 && m_eHTTPMethod.isContentAllowed ())
      {
        // Create the correct stream
        try (final OutputStream aOS = ResponseHelper.getBestSuitableOutputStream (m_aHttpRequest, aHttpResponse))
        {
          // Emit main content to stream
          aOS.write (m_aContent, 0, nContentLength);
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
          s_aLogger.error ("Failed to open input stream from " + m_aContentISP);

          // Handle it gracefully with a 404 and not with a 500
          aHttpResponse.setStatus (HttpServletResponse.SC_NOT_FOUND);
        }
        else
        {
          // Don't emit content for HEAD method
          if (m_eHTTPMethod.isContentAllowed ())
          {
            // We do have an input stream
            // -> copy it to the response
            final OutputStream aOS = aHttpResponse.getOutputStream ();
            final MutableLong aByteCount = new MutableLong ();

            if (StreamHelper.copyInputStreamToOutputStream (aContentIS, aOS, aByteCount).isSuccess ())
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
              _error ("Copying from " +
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
      {
        // Set status 204 - no content; this is most likely a programming
        // error
        aHttpResponse.setStatus (HttpServletResponse.SC_NO_CONTENT);
        _warn ("No content present for the response");
      }
  }

  public void applyToResponse (@Nonnull final HttpServletResponse aHttpResponse) throws IOException
  {
    ValueEnforcer.notNull (aHttpResponse, "HttpResponse");

    // Apply all collected headers
    for (final Map.Entry <String, ICommonsList <String>> aEntry : m_aResponseHeaderMap)
    {
      final String sHeaderName = aEntry.getKey ();
      int nIndex = 0;
      for (final String sHeaderValue : aEntry.getValue ())
      {
        if (nIndex == 0)
          aHttpResponse.setHeader (sHeaderName, sHeaderValue);
        else
          aHttpResponse.addHeader (sHeaderName, sHeaderValue);
        ++nIndex;
      }
    }

    final boolean bIsRedirect = isRedirectDefined ();
    final boolean bHasStatusCode = isStatusCodeDefined ();

    if (bIsRedirect)
    {
      if (bHasStatusCode)
        _warn ("Ignoring provided status code because a redirect is specified!");
      if (!m_bAllowContentOnRedirect)
      {
        if (m_aCacheControl != null)
          _info ("Ignoring provided Cache-Control because a redirect is specified!");
        if (m_sContentDispositionFilename != null)
          _warn ("Ignoring provided Content-Dispostion filename because a redirect is specified!");
        if (m_aMimeType != null)
          _warn ("Ignoring provided MimeType because a redirect is specified!");
        if (m_aCharset != null)
          _warn ("Ignoring provided charset because a redirect is specified!");
        if (hasContent ())
          _warn ("Ignoring provided content because a redirect is specified!");
      }

      // Note: After using this method, the response should be
      // considered to be committed and should not be written to.
      final String sRealTargetURL = aHttpResponse.encodeRedirectURL (m_sRedirectTargetUrl);
      switch (m_eRedirectMode)
      {
        case DEFAULT:
          aHttpResponse.sendRedirect (sRealTargetURL);
          break;
        case POST_REDIRECT_GET:
          switch (m_eHTTPVersion)
          {
            case HTTP_10:
              // For HTTP 1.0 send 302
              aHttpResponse.setStatus (HttpServletResponse.SC_FOUND);
              break;
            case HTTP_11:
              // For HTTP 1.1 send 303
              aHttpResponse.setStatus (HttpServletResponse.SC_SEE_OTHER);
              break;
          }
          // Set the location header
          aHttpResponse.addHeader (CHTTPHeader.LOCATION, sRealTargetURL);
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
        _warn ("Overriding provided redirect because a status code is specified!");
      if (!m_bAllowContentOnStatusCode)
      {
        if (m_aCacheControl != null)
          _info ("Ignoring provided Cache-Control because a status code is specified!");
        if (m_sContentDispositionFilename != null)
          _warn ("Ignoring provided Content-Dispostion filename because a status code is specified!");
        if (m_aMimeType != null)
          _warn ("Ignoring provided MimeType because a status code is specified!");
        if (m_aCharset != null)
          _warn ("Ignoring provided charset because a status code is specified!");
        if (hasContent ())
          _warn ("Ignoring provided content because a status code is specified!");
      }
      if (m_nStatusCode == HttpServletResponse.SC_UNAUTHORIZED &&
          !m_aResponseHeaderMap.containsHeaders (CHTTPHeader.WWW_AUTHENTICATE))
        _warn ("Status code UNAUTHORIZED (401) is returned, but no " +
               CHTTPHeader.WWW_AUTHENTICATE +
               " HTTP response header is set!");

      // Content may be present so, sendError is not an option here!
      if (m_nStatusCode >= HttpServletResponse.SC_BAD_REQUEST && m_aContent == null)
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
    }

    // Verify only if is a response with content
    _verifyCachingIntegrity ();

    if (m_aCacheControl != null)
    {
      final String sCacheControlValue = m_aCacheControl.getAsHTTPHeaderValue ();
      if (StringHelper.hasText (sCacheControlValue))
        aHttpResponse.setHeader (CHTTPHeader.CACHE_CONTROL, sCacheControlValue);
      else
        _warn ("An empty Cache-Control was provided!");
    }

    if (m_sContentDispositionFilename != null)
    {
      final StringBuilder aSB = new StringBuilder ();
      if (m_aRequestBrowserInfo != null &&
          m_aRequestBrowserInfo.getBrowserType () == EBrowserType.IE &&
          m_aRequestBrowserInfo.getVersion ().getMajor () <= 8)
      {
        // Special case for IE <= 8
        final Charset aCharsetToUse = m_aCharset != null ? m_aCharset : CCharset.CHARSET_UTF_8_OBJ;
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
           .append ("\"");

        // Check if we need an UTF-8 filename
        // http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http/6745788#6745788
        final String sRFC5987Filename = RFC5987Encoder.getRFC5987EncodedUTF8 (m_sContentDispositionFilename);
        if (!sRFC5987Filename.equals (m_sContentDispositionFilename))
          aSB.append ("; filename*=UTF-8''").append (sRFC5987Filename);
      }

      aHttpResponse.setHeader (CHTTPHeader.CONTENT_DISPOSITION, aSB.toString ());
      if (m_aMimeType == null)
      {
        _warn ("Content-Disposition is specified but no MimeType is set. Using the default download MimeType.");
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
        _error ("MimeType '" +
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
              _warn ("MimeType '" +
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
      _warn ("No MimeType present");

    // Charset
    if (m_aCharset != null)
    {
      final String sCharset = m_aCharset.name ();
      if (m_aMimeType == null)
        _warn ("If no MimeType present, the client cannot get notified about the character encoding '" +
               sCharset +
               "'");

      // Check with request charset
      final QValue aQuality = m_aAcceptCharsetList.getQValueOfCharset (sCharset);
      if (aQuality.isMinimumQuality ())
      {
        final ICommonsOrderedMap <String, QValue> aBetterValues = m_aAcceptCharsetList.getAllQValuesGreaterThan (aQuality.getQuality ());
        _error ("Character encoding '" +
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
            _warn ("Character encoding '" +
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
        _warn ("Also no character encoding present");
      else
        switch (m_aMimeType.getContentType ())
        {
          case TEXT:
          case MULTIPART:
            _warn ("A character encoding for MimeType '" + m_aMimeType.getAsString () + "' is appreciated.");
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
    _applyContent (aHttpResponse);
  }
}
