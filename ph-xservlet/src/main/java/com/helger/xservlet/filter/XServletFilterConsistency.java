/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.http.CHttp;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.state.EContinue;
import com.helger.commons.string.StringHelper;
import com.helger.http.EHttpVersion;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.ResponseHelper;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
  public static final XServletFilterConsistency INSTANCE = new XServletFilterConsistency ();

  private static final Logger LOGGER = LoggerFactory.getLogger (XServletFilterConsistency.class);
  private static final AtomicBoolean SILENT_MODE = new AtomicBoolean (GlobalDebug.DEFAULT_SILENT_MODE);

  protected XServletFilterConsistency ()
  {}

  /**
   * @return <code>true</code> if logging is disabled, <code>false</code> if it
   *         is enabled.
   * @since 9.1.7
   */
  public static boolean isSilentMode ()
  {
    return SILENT_MODE.get ();
  }

  /**
   * Enable or disable certain regular log messages.
   *
   * @param bSilentMode
   *        <code>true</code> to disable logging, <code>false</code> to enable
   *        logging
   * @return The previous value of the silent mode.
   * @since 9.1.7
   */
  public static boolean setSilentMode (final boolean bSilentMode)
  {
    return SILENT_MODE.getAndSet (bSilentMode);
  }

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod)
  {
    return EContinue.CONTINUE;
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param nStatusCode
   *        The response status code.
   * @param eHttpMethod
   *        Used HTTP Method
   */
  @OverrideOnDemand
  protected void checkStatusCode (@Nonnull final String sRequestURL,
                                  final int nStatusCode,
                                  @Nonnull final EHttpMethod eHttpMethod)
  {
    // < 200 || >= 400?
    if (nStatusCode < CHttp.HTTP_OK || nStatusCode >= CHttp.HTTP_BAD_REQUEST)
      if (!isSilentMode ())
        LOGGER.warn ("HTTP status code " +
                     nStatusCode +
                     " in response to " +
                     eHttpMethod.getName () +
                     " '" +
                     sRequestURL +
                     "'");
  }

  private static boolean _isContentExpected (final int nStatusCode)
  {
    // >= 200 && < 300
    return nStatusCode >= CHttp.HTTP_OK &&
           nStatusCode < CHttp.HTTP_MULTIPLE_CHOICES &&
           !ResponseHelper.isEmptyStatusCode (nStatusCode);
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param sCharacterEncoding
   *        The response character encoding.
   * @param nStatusCode
   *        The response status code.
   * @param eHttpMethod
   *        Used HTTP Method
   */
  @OverrideOnDemand
  protected void checkCharacterEncoding (@Nonnull final String sRequestURL,
                                         @Nullable final String sCharacterEncoding,
                                         final int nStatusCode,
                                         @Nonnull final EHttpMethod eHttpMethod)
  {
    if (StringHelper.hasNoText (sCharacterEncoding) && _isContentExpected (nStatusCode))
      if (!isSilentMode ())
        LOGGER.warn ("No character encoding on HTTP " +
                     nStatusCode +
                     " response to " +
                     eHttpMethod.getName () +
                     " '" +
                     sRequestURL +
                     "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param sContentType
   *        The response content type.
   * @param nStatusCode
   *        The response status code.
   * @param eHttpMethod
   *        Used HTTP Method
   */
  @OverrideOnDemand
  protected void checkContentType (@Nonnull final String sRequestURL,
                                   @Nullable final String sContentType,
                                   final int nStatusCode,
                                   @Nonnull final EHttpMethod eHttpMethod)
  {
    if (StringHelper.hasNoText (sContentType) && _isContentExpected (nStatusCode))
      if (!isSilentMode ())
        LOGGER.warn ("No content type on HTTP " +
                     nStatusCode +
                     " response to " +
                     eHttpMethod.getName () +
                     " '" +
                     sRequestURL +
                     "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param aHeaders
   *        All response HTTP headers.
   * @param nStatusCode
   *        The response status code.
   * @param eHttpMethod
   *        Used HTTP Method
   */
  @OverrideOnDemand
  protected void checkHeaders (@Nonnull final String sRequestURL,
                               @Nonnull final HttpHeaderMap aHeaders,
                               final int nStatusCode,
                               @Nonnull final EHttpMethod eHttpMethod)
  {
    // Happens because of the default headers in the
    // UnifiedResponseDefaultSettings
    if (false)
      if (nStatusCode != CHttp.HTTP_OK && aHeaders.isNotEmpty ())
        if (!isSilentMode ())
          LOGGER.warn ("Headers on HTTP " +
                       nStatusCode +
                       " response to " +
                       eHttpMethod.getName () +
                       " '" +
                       sRequestURL +
                       "': " +
                       aHeaders);
  }

  @Override
  public void afterRequest (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final EHttpVersion eHttpVersion,
                            @Nonnull final EHttpMethod eHttpMethod,
                            final boolean bInvokeHandler,
                            @Nullable final Throwable aCaughtException,
                            final boolean bIsHandledAsync)
  {
    ValueEnforcer.isTrue (aHttpResponse instanceof StatusAwareHttpResponseWrapper,
                          "Must be a StatusAwareHttpResponseWrapper");
    final String sRequestURL = RequestHelper.getURLDecoded (aHttpRequest);
    final int nStatusCode = ((StatusAwareHttpResponseWrapper) aHttpResponse).getStatusCode ();
    final HttpHeaderMap aHeaders = ((StatusAwareHttpResponseWrapper) aHttpResponse).headerMap ();
    final String sCharacterEncoding = aHttpResponse.getCharacterEncoding ();
    final String sContentType = aHttpResponse.getContentType ();

    checkStatusCode (sRequestURL, nStatusCode, eHttpMethod);
    checkCharacterEncoding (sRequestURL, sCharacterEncoding, nStatusCode, eHttpMethod);
    if (!bIsHandledAsync)
      checkContentType (sRequestURL, sContentType, nStatusCode, eHttpMethod);
    checkHeaders (sRequestURL, aHeaders, nStatusCode, eHttpMethod);
  }
}
