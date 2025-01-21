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
package com.helger.web.servlets.filter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.helger.commons.http.CHttpHeader;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.http.AcceptEncodingList;
import com.helger.scope.mgr.ScopeManager;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.filter.AbstractHttpServletFilter;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.ResponseHelperSettings;
import com.helger.servlet.response.gzip.AbstractCompressedResponseWrapper;
import com.helger.servlet.response.gzip.CompressFilterSettings;
import com.helger.servlet.response.gzip.DeflateResponse;
import com.helger.servlet.response.gzip.GZIPResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is a generic filter that first tries to find whether "GZip" is
 * supported, and if this fails, whether "Deflate" is supported. If none is
 * supported, no compression will happen in this filter.
 *
 * @author Philip Helger
 */
public class CompressFilter extends AbstractHttpServletFilter
{
  private static final String REQUEST_ATTR = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                             CompressFilter.class.getName ();
  private static final IMutableStatisticsHandlerCounter STATS_COUNTER = StatisticsManager.getCounterHandler (CompressFilter.class.getName () +
                                                                                                             "$none");

  public CompressFilter ()
  {}

  @Override
  @OverridingMethodsMustInvokeSuper
  public void init () throws ServletException
  {
    super.init ();

    // Mark the filter as loaded
    CompressFilterSettings.markFilterLoaded ();

    // As compression is done in the filter, no compression in
    // ResponseHelper is required there
    ResponseHelperSettings.setResponseCompressionEnabled (false);
  }

  private static void _performCompressed (@Nonnull final HttpServletRequest aRequest,
                                          @Nonnull final FilterChain aChain,
                                          @Nonnull final HttpServletResponse aHttpResponse,
                                          @Nonnull final AbstractCompressedResponseWrapper aCompressedResponse) throws IOException,
                                                                                                                ServletException
  {
    boolean bException = true;
    try
    {
      aChain.doFilter (aRequest, aCompressedResponse);
      bException = false;
    }
    finally
    {
      if (bException && !aHttpResponse.isCommitted ())
      {
        // An exception occurred
        aCompressedResponse.resetBuffer ();
        aCompressedResponse.setNoCompression ();
      }
      else
        aCompressedResponse.finish ();
    }
  }

  @Override
  public void doHttpFilter (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final FilterChain aChain) throws IOException, ServletException
  {
    if (CompressFilterSettings.isResponseCompressionEnabled () &&
        ServletHelper.getRequestAttribute (aHttpRequest, REQUEST_ATTR) == null)
    {
      ServletHelper.setRequestAttribute (aHttpRequest, REQUEST_ATTR, Boolean.TRUE);

      // Inform caches that responses may vary according to
      // Accept-Encoding
      aHttpResponse.setHeader (CHttpHeader.VARY, CHttpHeader.ACCEPT_ENCODING);

      final AcceptEncodingList aAEL = RequestHelper.getAcceptEncodings (aHttpRequest);

      AbstractCompressedResponseWrapper aCompressedResponse = null;

      final String sGZIPEncoding = aAEL.getUsedGZIPEncoding ();
      if (sGZIPEncoding != null && CompressFilterSettings.isResponseGzipEnabled ())
      {
        // Use gzip
        aCompressedResponse = new GZIPResponse (aHttpRequest, aHttpResponse, sGZIPEncoding);
      }
      else
      {
        final String sDeflateEncoding = aAEL.getUsedDeflateEncoding ();
        if (sDeflateEncoding != null && CompressFilterSettings.isResponseDeflateEnabled ())
        {
          // Use deflate
          aCompressedResponse = new DeflateResponse (aHttpRequest, aHttpResponse, sDeflateEncoding);
        }
      }

      if (aCompressedResponse != null)
      {
        _performCompressed (aHttpRequest, aChain, aHttpResponse, aCompressedResponse);
        return;
      }

      // No GZip or deflate
      STATS_COUNTER.increment ();
    }

    // Perform as is
    aChain.doFilter (aHttpRequest, aHttpResponse);
  }
}
