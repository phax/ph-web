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
package com.helger.servlet.logging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.collection.impl.CommonsHashSet;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.filter.AbstractHttpServletFilter;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.ResponseHelper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoggingFilter extends AbstractHttpServletFilter
{
  private static final AtomicBoolean ENABLED = new AtomicBoolean (true);

  private Logger m_aLogger = LoggerFactory.getLogger (getClass ());
  private int m_nMaxContentSize = 1024;
  private final ICommonsSet <String> m_aExcludedPaths = new CommonsHashSet <> ();
  private String m_sRequestPrefix = "REQUEST: ";
  private String m_sResponsePrefix = "RESPONSE: ";

  public static void setGloballyEnabled (final boolean bEnabled)
  {
    ENABLED.set (bEnabled);
  }

  public static boolean isGloballyEnabled ()
  {
    return ENABLED.get ();
  }

  public LoggingFilter ()
  {}

  @Override
  @OverridingMethodsMustInvokeSuper
  public void init () throws ServletException
  {
    final FilterConfig aFilterConfig = getFilterConfig ();

    final String sLoggerName = aFilterConfig.getInitParameter ("loggerName");
    if (StringHelper.hasText (sLoggerName))
      m_aLogger = LoggerFactory.getLogger (sLoggerName);

    final String sMaxContentSize = aFilterConfig.getInitParameter ("maxContentSize");
    if (sMaxContentSize != null)
      m_nMaxContentSize = Integer.parseInt (sMaxContentSize);

    final String sExcludedPaths = aFilterConfig.getInitParameter ("excludedPaths");
    if (StringHelper.hasText (sExcludedPaths))
      m_aExcludedPaths.setAll (RegExHelper.getSplitToArray (sExcludedPaths, "\\s*,\\s*"));

    final String sRequestPrefix = aFilterConfig.getInitParameter ("requestPrefix");
    if (StringHelper.hasText (sRequestPrefix))
      m_sRequestPrefix = sRequestPrefix;

    final String sResponsePrefix = aFilterConfig.getInitParameter ("responsePrefix");
    if (StringHelper.hasText (sResponsePrefix))
      m_sResponsePrefix = sResponsePrefix;
  }

  @Nonnull
  @OverrideOnDemand
  protected String getRequestDescription (@Nonnull final LoggingHttpServletRequestWrapper aRequestWrapper)
  {
    final LoggingRequest aLoggingRequest = new LoggingRequest ();
    aLoggingRequest.setSender (aRequestWrapper.getLocalAddr ());
    aLoggingRequest.setMethod (ServletHelper.getRequestMethod (aRequestWrapper));
    aLoggingRequest.setPath (ServletHelper.getRequestRequestURI (aRequestWrapper));
    aLoggingRequest.setParams (aRequestWrapper.isFormPost () ? null : aRequestWrapper.getParameters ());
    aLoggingRequest.setHeaders (RequestHelper.getRequestHeaderMap (aRequestWrapper));
    final String sContent = aRequestWrapper.getContent ();
    if (m_aLogger.isTraceEnabled () || m_nMaxContentSize <= 0)
    {
      aLoggingRequest.setBody (sContent);
    }
    else
    {
      aLoggingRequest.setBody (sContent.substring (0, Math.min (sContent.length (), m_nMaxContentSize)));
    }
    return aLoggingRequest.getAsJson ().getAsJsonString ();
  }

  @Nonnull
  @OverrideOnDemand
  protected String getResponseDescription (@Nonnull final LoggingHttpServletResponseWrapper responseWrapper)
  {
    final LoggingResponse aLoggingResponse = new LoggingResponse ();
    aLoggingResponse.setStatus (responseWrapper.getStatus ());
    aLoggingResponse.setHeaders (ResponseHelper.getResponseHeaderMap (responseWrapper));
    final String content = responseWrapper.getContentAsString ();
    if (m_aLogger.isTraceEnabled () || m_nMaxContentSize <= 0)
    {
      aLoggingResponse.setBody (content);
    }
    else
    {
      aLoggingResponse.setBody (content.substring (0, Math.min (content.length (), m_nMaxContentSize)));
    }
    return aLoggingResponse.getAsJson ().getAsJsonString ();
  }

  /**
   * Check if this request should be logged or not.
   *
   * @param aHttpRequest
   *        Current HTTP servlet request. Never <code>null</code>.
   * @param aHttpResponse
   *        Current HTTP servlet response. Never <code>null</code>.
   * @return <code>true</code> to log, <code>false</code> to not log the request
   */
  @OverrideOnDemand
  protected boolean isLogRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse)
  {
    boolean bLog = isGloballyEnabled ();
    if (bLog)
    {
      // Check for excluded path
      final String sRequestURI = ServletHelper.getRequestRequestURI (aHttpRequest);
      for (final String sExcludedPath : m_aExcludedPaths)
        if (sRequestURI.startsWith (sExcludedPath))
        {
          bLog = false;
          break;
        }
    }
    return bLog;
  }

  @Override
  public void doHttpFilter (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final FilterChain aFilterChain) throws IOException, ServletException
  {
    if (isLogRequest (aHttpRequest, aHttpResponse))
    {
      final LoggingHttpServletRequestWrapper aRequestWrapper = new LoggingHttpServletRequestWrapper (aHttpRequest);
      final LoggingHttpServletResponseWrapper aResponseWrapper = new LoggingHttpServletResponseWrapper (aHttpResponse);

      m_aLogger.info (m_sRequestPrefix + getRequestDescription (aRequestWrapper));

      aFilterChain.doFilter (aRequestWrapper, aResponseWrapper);

      m_aLogger.info (m_sResponsePrefix + getResponseDescription (aResponseWrapper));

      aResponseWrapper.writeContentTo (aHttpResponse.getOutputStream ());
    }
    else
    {
      aFilterChain.doFilter (aHttpRequest, aHttpResponse);
    }
  }
}
