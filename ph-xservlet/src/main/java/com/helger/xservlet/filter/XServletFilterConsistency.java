/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.xservlet.filter;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.state.EContinue;
import com.helger.commons.string.StringHelper;
import com.helger.http.EHttpVersion;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.ResponseHelper;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;
import com.helger.web.scope.IRequestWebScope;

/**
 * Handle special content related stuff that needs to be processed for every
 * servlet. Currently handled are:
 * <ul>
 * <li>Request fallback charset</li>
 * <li>Response fallback charset</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletFilterConsistency implements IXServletLowLevelFilter
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (XServletFilterConsistency.class);

  /** The request fallback charset to be used, if none is present! */
  private final Charset m_aRequestFallbackCharset;

  /** The response fallback charset to be used, if none is present! */
  private final Charset m_aResponseFallbackCharset;

  public XServletFilterConsistency (@Nonnull final Charset aRequestFallbackCharset,
                                    @Nonnull final Charset aResponseFallbackCharset)
  {
    m_aRequestFallbackCharset = aRequestFallbackCharset;
    m_aResponseFallbackCharset = aResponseFallbackCharset;
  }

  /**
   * This method is required to ensure that the HTTP request is correctly
   * encoded. Normally this is done via the charset filter, but if a
   * non-existing URL is accessed then the error redirect happens without the
   * charset filter ever called.
   *
   * @param aHttpRequest
   *        The current HTTP request. Never <code>null</code>.
   * @throws IOException
   *         In invalid charset
   */
  @OverrideOnDemand
  protected void ensureRequestCharset (@Nonnull final HttpServletRequest aHttpRequest) throws IOException
  {
    if (aHttpRequest.getCharacterEncoding () == null)
    {
      final String sCharsetName = m_aRequestFallbackCharset.name ();

      // Happens too often - seems to be the case for all requests where no
      // parameters are present
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("Forcing request charset to " + sCharsetName);
      aHttpRequest.setCharacterEncoding (sCharsetName);
    }
  }

  /**
   * This method is required to ensure that the HTTP response is correctly
   * encoded. Normally this is done via the charset filter, but if a
   * non-existing URL is accessed then the error redirect happens without the
   * charset filter ever called.
   *
   * @param aHttpResponse
   *        The current HTTP response. Never <code>null</code>.
   */
  @OverrideOnDemand
  protected void ensureResponseCharset (@Nonnull final HttpServletResponse aHttpResponse)
  {
    if (aHttpResponse.getCharacterEncoding () == null)
    {
      final String sCharsetName = m_aResponseFallbackCharset.name ();
      s_aLogger.warn ("Forcing response charset to " + sCharsetName);
      aHttpResponse.setCharacterEncoding (sCharsetName);
    }
  }

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod,
                                  @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    ensureRequestCharset (aHttpRequest);
    ensureResponseCharset (aHttpResponse);
    return EContinue.CONTINUE;
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkStatusCode (@Nonnull final String sRequestURL, final int nStatusCode)
  {
    // < 200 || >= 400?
    if (nStatusCode < HttpServletResponse.SC_OK || nStatusCode >= HttpServletResponse.SC_BAD_REQUEST)
      s_aLogger.warn ("HTTP status code " + nStatusCode + " in response to '" + sRequestURL + "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param sCharacterEncoding
   *        The response character encoding.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkCharacterEncoding (@Nonnull final String sRequestURL,
                                         @Nullable final String sCharacterEncoding,
                                         final int nStatusCode)
  {
    if (StringHelper.hasNoText (sCharacterEncoding) && !ResponseHelper.isEmptyStatusCode (nStatusCode))
      s_aLogger.warn ("No character encoding on HTTP " + nStatusCode + " response to '" + sRequestURL + "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param sContentType
   *        The response content type.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkContentType (@Nonnull final String sRequestURL,
                                   @Nullable final String sContentType,
                                   final int nStatusCode)
  {
    if (StringHelper.hasNoText (sContentType) && !ResponseHelper.isEmptyStatusCode (nStatusCode))
      s_aLogger.warn ("No content type on HTTP " + nStatusCode + " response to '" + sRequestURL + "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param aHeaders
   *        All response HTTP headers.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkHeaders (@Nonnull final String sRequestURL,
                               @Nonnull final HttpHeaderMap aHeaders,
                               final int nStatusCode)
  {
    // Happens because of the default headers in the
    // UnifiedResponseDefaultSettings
    if (false)
      if (nStatusCode != HttpServletResponse.SC_OK && aHeaders.isNotEmpty ())
        s_aLogger.warn ("Headers on HTTP " + nStatusCode + " response to '" + sRequestURL + "': " + aHeaders);
  }

  public void afterRequest (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final EHttpVersion eHttpVersion,
                            @Nonnull final EHttpMethod eHttpMethod,
                            @Nonnull final IRequestWebScope aRequestScope,
                            final boolean bInvokeHandler,
                            @Nullable final Throwable aCaughtException)
  {
    ValueEnforcer.isTrue (aHttpResponse instanceof StatusAwareHttpResponseWrapper,
                          "Must be a StatusAwareHttpResponseWrapper");
    final String sRequestURL = RequestHelper.getURL (aHttpRequest);
    final int nStatusCode = ((StatusAwareHttpResponseWrapper) aHttpResponse).getStatusCode ();
    final HttpHeaderMap aHeaders = ((StatusAwareHttpResponseWrapper) aHttpResponse).headerMap ();
    final String sCharacterEncoding = aHttpResponse.getCharacterEncoding ();
    final String sContentType = aHttpResponse.getContentType ();

    checkStatusCode (sRequestURL, nStatusCode);
    checkCharacterEncoding (sRequestURL, sCharacterEncoding, nStatusCode);
    checkContentType (sRequestURL, sContentType, nStatusCode);
    checkHeaders (sRequestURL, aHeaders, nStatusCode);
  }
}
