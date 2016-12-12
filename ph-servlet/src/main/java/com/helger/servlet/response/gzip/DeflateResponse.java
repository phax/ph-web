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
package com.helger.servlet.response.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;

public final class DeflateResponse extends AbstractCompressedResponseWrapper
{
  private static final class DeflateServletOutputStream extends AbstractCompressedServletOutputStream
  {
    public DeflateServletOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                       @Nonnull final HttpServletResponse aHttpResponse,
                                       @Nonnull final String sContentEncoding,
                                       final long nContentLength,
                                       @Nonnegative final int nMinCompressSize) throws IOException
    {
      super (aHttpRequest, aHttpResponse, sContentEncoding, nContentLength, nMinCompressSize);
    }

    @Override
    @Nonnull
    protected DeflaterOutputStream createDeflaterOutputStream (@Nonnull final OutputStream aOS)
    {
      return new DeflaterOutputStream (aOS);
    }
  }

  private static final IMutableStatisticsHandlerCounter s_aStatsDeflate = StatisticsManager.getCounterHandler (DeflateResponse.class);

  public DeflateResponse (@Nonnull final HttpServletRequest aHttpRequest,
                          @Nonnull final HttpServletResponse aHttpResponse,
                          @Nonnull final String sContentEncoding)
  {
    super (aHttpRequest, aHttpResponse, sContentEncoding);
    s_aStatsDeflate.increment ();
  }

  @Override
  @Nonnull
  protected AbstractCompressedServletOutputStream createCompressedOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                                                                @Nonnull final HttpServletResponse aHttpResponse,
                                                                                @Nonnull @Nonempty final String sContentEncoding,
                                                                                final long nContentLength,
                                                                                @Nonnegative final int nMinCompressSize) throws IOException
  {
    return new DeflateServletOutputStream (aHttpRequest,
                                           aHttpResponse,
                                           sContentEncoding,
                                           nContentLength,
                                           nMinCompressSize);
  }
}
