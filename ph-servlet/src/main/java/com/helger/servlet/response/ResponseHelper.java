/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.http.AcceptEncodingList;
import com.helger.servlet.request.RequestHelper;

/**
 * Misc. helper methods on {@link HttpServletResponse} objects.<br>
 * Consider using the {@link UnifiedResponse} for easy and consistent HTTP
 * response handling.
 *
 * @author Philip Helger
 */
@Immutable
public final class ResponseHelper
{
  @PresentForCodeCoverage
  private static final ResponseHelper s_aInstance = new ResponseHelper ();

  private ResponseHelper ()
  {}

  /**
   * Determine the best suitable output stream type for the given request
   * without actually modifying response data. If the request supports gzip, the
   * result is a {@link EResponseStreamType#GZIP}, if the request supports
   * deflate or compress, the result will be a
   * {@link EResponseStreamType#DEFLATE}. If none of that matches, the regular
   * value {@link EResponseStreamType#PLAIN} will be returned.
   *
   * @param aHttpRequest
   *        request
   * @return The best matching output stream type. Never <code>null</code>.
   * @see ResponseHelperSettings
   */
  @Nonnull
  public static EResponseStreamType getBestSuitableOutputStreamType (@Nonnull final HttpServletRequest aHttpRequest)
  {
    if (ResponseHelperSettings.isResponseCompressionEnabled ())
    {
      // Can we get resource transfer working with GZIP or deflate?
      final AcceptEncodingList aAcceptEncodings = RequestHelper.getAcceptEncodings (aHttpRequest);

      if (ResponseHelperSettings.isResponseGzipEnabled ())
      {
        // Check if GZip is supported by the requester
        if (aAcceptEncodings.getUsedGZIPEncoding () != null)
        {
          // Use GZip
          return EResponseStreamType.GZIP;
        }
      }

      if (ResponseHelperSettings.isResponseDeflateEnabled ())
      {
        // Check if Deflate is supported by the requester
        if (aAcceptEncodings.getUsedDeflateEncoding () != null)
        {
          // Use Deflate
          return EResponseStreamType.DEFLATE;
        }
      }
    }

    // Use plain response
    return EResponseStreamType.PLAIN;
  }

  /**
   * Get the best suitable output stream for the given combination of request
   * and response. If the request supports gzip, the result is a
   * {@link GZIPOutputStream}, if the request supports deflate or compress, the
   * result will be a {@link ZipOutputStream}. If none of that matches, the
   * regular response output stream is used
   *
   * @param aHttpRequest
   *        request
   * @param aHttpResponse
   *        Response
   * @return The best matching output stream
   * @throws IOException
   *         In case of IO error
   * @see ResponseHelperSettings
   */
  @Nonnull
  public static OutputStream getBestSuitableOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                                          @Nonnull final HttpServletResponse aHttpResponse) throws IOException
  {
    OutputStream aOS = aHttpResponse.getOutputStream ();

    if (ResponseHelperSettings.isResponseCompressionEnabled ())
    {
      // Can we get resource transfer working with GZIP or deflate?
      final AcceptEncodingList aAcceptEncodings = RequestHelper.getAcceptEncodings (aHttpRequest);

      // Inform caches that responses may vary according to Accept-Encoding
      aHttpResponse.setHeader (CHttpHeader.VARY, CHttpHeader.ACCEPT_ENCODING);

      final String sGZipEncoding = aAcceptEncodings.getUsedGZIPEncoding ();
      if (sGZipEncoding != null && ResponseHelperSettings.isResponseGzipEnabled ())
      {
        // Use GZip
        aHttpResponse.setHeader (CHttpHeader.CONTENT_ENCODING, sGZipEncoding);
        aOS = new GZIPOutputStream (aHttpResponse.getOutputStream ());
      }
      else
      {
        final String sDeflateEncoding = aAcceptEncodings.getUsedDeflateEncoding ();
        if (sDeflateEncoding != null && ResponseHelperSettings.isResponseDeflateEnabled ())
        {
          // Use Deflate
          aHttpResponse.setHeader (CHttpHeader.CONTENT_ENCODING, sDeflateEncoding);
          aOS = new ZipOutputStream (aHttpResponse.getOutputStream ());
          // A dummy ZIP entry is required!
          ((ZipOutputStream) aOS).putNextEntry (new ZipEntry ("dummy name"));
        }
      }
    }

    return aOS;
  }

  public static void finishReponseOutputStream (@Nonnull final OutputStream aOS) throws IOException
  {
    ValueEnforcer.notNull (aOS, "OutputStream");

    aOS.flush ();
    aOS.close ();
  }

  public static boolean isEmptyStatusCode (final int nSC)
  {
    // 204 || 301 || 302 || 303 || 304
    return nSC == HttpServletResponse.SC_NO_CONTENT ||
           nSC == HttpServletResponse.SC_MOVED_PERMANENTLY ||
           nSC == HttpServletResponse.SC_MOVED_TEMPORARILY ||
           nSC == HttpServletResponse.SC_SEE_OTHER ||
           nSC == HttpServletResponse.SC_NOT_MODIFIED;
  }

  /**
   * Set the content length of an HTTP response. If the passed content length is
   * a valid integer, <code>aHttpResponse.setContentLength</code> is invoked,
   * else the HTTP header {@link CHttpHeader#CONTENT_LENGTH} is set manually.
   *
   * @param aHttpResponse
   *        The response to set the content length to
   * @param nContentLength
   *        The content length to set
   */
  public static void setContentLength (@Nonnull final HttpServletResponse aHttpResponse,
                                       @Nonnegative final long nContentLength)
  {
    if (nContentLength < Integer.MAX_VALUE)
      aHttpResponse.setContentLength ((int) nContentLength);
    else
      aHttpResponse.setHeader (CHttpHeader.CONTENT_LENGTH, Long.toString (nContentLength));
  }

  /**
   * Get a complete response header map as a copy.
   *
   * @param aHttpResponse
   *        The source HTTP response. May not be <code>null</code>.
   * @return Never <code>null</code>.
   * @since 8.7.3
   */
  @Nonnull
  @ReturnsMutableCopy
  public static HttpHeaderMap getResponseHeaderMap (@Nonnull final HttpServletResponse aHttpResponse)
  {
    ValueEnforcer.notNull (aHttpResponse, "HttpResponse");

    final HttpHeaderMap ret = new HttpHeaderMap ();
    for (final String sName : aHttpResponse.getHeaderNames ())
      for (final String sValue : aHttpResponse.getHeaders (sName))
        ret.addHeader (sName, sValue);
    return ret;
  }
}
