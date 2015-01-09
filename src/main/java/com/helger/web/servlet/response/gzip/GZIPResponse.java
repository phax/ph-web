/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.servlet.response.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.stats.IStatisticsHandlerCounter;
import com.helger.commons.stats.StatisticsManager;

public final class GZIPResponse extends AbstractCompressedResponseWrapper
{
  private static final class GZIPServletOutputStream extends AbstractCompressedServletOutputStream
  {
    public GZIPServletOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                    @Nonnull final HttpServletResponse aHttpResponse,
                                    @Nonnull final String sContentEncoding,
                                    final long nContentLength,
                                    @Nonnegative final int nMinCompressSize) throws IOException
    {
      super (aHttpRequest, aHttpResponse, sContentEncoding, nContentLength, nMinCompressSize);
    }

    @Override
    @Nonnull
    protected DeflaterOutputStream createDeflaterOutputStream (@Nonnull final OutputStream aOS) throws IOException
    {
      return new GZIPOutputStream (aOS);
    }
  }

  private static final IStatisticsHandlerCounter s_aStatsGZip = StatisticsManager.getCounterHandler (GZIPResponse.class);

  public GZIPResponse (final HttpServletRequest aHttpRequest,
                       final HttpServletResponse aHttpResponse,
                       final String sContentEncoding)
  {
    super (aHttpRequest, aHttpResponse, sContentEncoding);
    s_aStatsGZip.increment ();
  }

  @Override
  @Nonnull
  protected AbstractCompressedServletOutputStream createCompressedOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                                                                @Nonnull final HttpServletResponse aHttpResponse,
                                                                                @Nonnull @Nonempty final String sContentEncoding,
                                                                                final long nContentLength,
                                                                                @Nonnegative final int nMinCompressSize) throws IOException
  {
    return new GZIPServletOutputStream (aHttpRequest, aHttpResponse, sContentEncoding, nContentLength, nMinCompressSize);
  }
}
