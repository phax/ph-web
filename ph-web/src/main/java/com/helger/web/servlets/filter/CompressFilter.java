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
package com.helger.web.servlets.filter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.http.AcceptEncodingHandler;
import com.helger.http.AcceptEncodingList;
import com.helger.http.CHTTPHeader;
import com.helger.web.servlet.response.ResponseHelperSettings;
import com.helger.web.servlet.response.gzip.AbstractCompressedResponseWrapper;
import com.helger.web.servlet.response.gzip.CompressFilterSettings;
import com.helger.web.servlet.response.gzip.DeflateResponse;
import com.helger.web.servlet.response.gzip.GZIPResponse;

/**
 * This is a generic filter that first tries to find whether "GZip" is
 * supported, and if this fails, whether "Deflate" is supported. If none is
 * supported, no compression will happen in this filter.
 *
 * @author Philip Helger
 */
public class CompressFilter implements Filter
{
  private static final String REQUEST_ATTR = CompressFilter.class.getName ();
  private static final IMutableStatisticsHandlerCounter s_aStatsNone = StatisticsManager.getCounterHandler (CompressFilter.class.getName () +
                                                                                                            "$none");

  public void init (@Nonnull final FilterConfig aFilterConfig)
  {
    // Mark the filter as loaded
    CompressFilterSettings.markFilterLoaded ();

    // As compression is done in the filter, no compression in
    // ResponseHelper is required there
    ResponseHelperSettings.setResponseCompressionEnabled (false);
  }

  private static void _performCompressed (@Nonnull final ServletRequest aRequest,
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

  public void doFilter (@Nonnull final ServletRequest aRequest,
                        @Nonnull final ServletResponse aResponse,
                        @Nonnull final FilterChain aChain) throws IOException, ServletException
  {
    if (CompressFilterSettings.isResponseCompressionEnabled () &&
        aRequest instanceof HttpServletRequest &&
        aResponse instanceof HttpServletResponse &&
        aRequest.getAttribute (REQUEST_ATTR) == null)
    {
      aRequest.setAttribute (REQUEST_ATTR, Boolean.TRUE);
      final HttpServletRequest aHttpRequest = (HttpServletRequest) aRequest;
      final HttpServletResponse aHttpResponse = (HttpServletResponse) aResponse;

      // Inform caches that responses may vary according to
      // Accept-Encoding
      aHttpResponse.setHeader (CHTTPHeader.VARY, CHTTPHeader.ACCEPT_ENCODING);

      final AcceptEncodingList aAEL = AcceptEncodingHandler.getAcceptEncodings (aHttpRequest);

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
        _performCompressed (aRequest, aChain, aHttpResponse, aCompressedResponse);
        return;
      }

      // No GZip or deflate
      s_aStatsNone.increment ();
    }

    // Perform as is
    aChain.doFilter (aRequest, aResponse);
  }

  public void destroy ()
  {}
}
