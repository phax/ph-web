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
package com.helger.servlet.response.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.statistics.api.IMutableStatisticsHandlerCounter;
import com.helger.statistics.impl.StatisticsManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class DeflateResponse extends AbstractCompressedResponseWrapper
{
  public static class DeflateServletOutputStream extends AbstractCompressedServletOutputStream
  {
    public DeflateServletOutputStream (@NonNull final HttpServletRequest aHttpRequest,
                                       @NonNull final HttpServletResponse aHttpResponse,
                                       @NonNull final String sContentEncoding,
                                       final long nContentLength,
                                       @Nonnegative final long nMinCompressSize) throws IOException
    {
      super (aHttpRequest, aHttpResponse, sContentEncoding, nContentLength, nMinCompressSize);
    }

    @Override
    @NonNull
    protected DeflaterOutputStream createDeflaterOutputStream (@NonNull final OutputStream aOS)
    {
      return new DeflaterOutputStream (aOS);
    }
  }

  private static final IMutableStatisticsHandlerCounter STATS_COUNTER_DEFLATE = StatisticsManager.getCounterHandler (DeflateResponse.class);

  public DeflateResponse (@NonNull final HttpServletRequest aHttpRequest,
                          @NonNull final HttpServletResponse aHttpResponse,
                          @NonNull final String sContentEncoding)
  {
    super (aHttpRequest, aHttpResponse, sContentEncoding);
    STATS_COUNTER_DEFLATE.increment ();
  }

  @Override
  @NonNull
  protected DeflateServletOutputStream createCompressedOutputStream (@NonNull final HttpServletRequest aHttpRequest,
                                                                     @NonNull final HttpServletResponse aHttpResponse,
                                                                     @NonNull @Nonempty final String sContentEncoding,
                                                                     final long nContentLength,
                                                                     @Nonnegative final long nMinCompressSize) throws IOException
  {
    return new DeflateServletOutputStream (aHttpRequest, aHttpResponse, sContentEncoding, nContentLength, nMinCompressSize);
  }
}
