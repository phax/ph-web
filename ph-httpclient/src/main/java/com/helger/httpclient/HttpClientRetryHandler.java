/**
 * Copyright (C) 2015-2017 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;

/**
 * HTTP client retry handler based on
 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html#d4e280
 *
 * @author Apache HC
 */
public class HttpClientRetryHandler implements HttpRequestRetryHandler
{
  public static enum ERetryMode
  {
    /**
     * Retry always as long as the max-retries is not exceeded. This mode is
     * independent of the thrown exception and can therefore be considered a
     * quite dumb version. This might nevertheless be an option for testing.
     */
    RETRY_ALWAYS,
    /**
     * Retry always as long as the max-retries is not exceeded. Do not retry if
     * certain exceptions are thrown that indicate that a retry would make no
     * sense (like SSL handshake errors or the like).
     */
    RETRY_SMART,
    /**
     * The same as {@link #RETRY_SMART} but only if the request is idempotent
     * (if it has no payload).
     */
    RETRY_IDEMPOTENT_ONLY;

    public boolean isCheckException ()
    {
      return this == RETRY_SMART || this == RETRY_IDEMPOTENT_ONLY;
    }
  }

  private final int m_nMaxRetries;
  private final ERetryMode m_eRetryMode;

  public HttpClientRetryHandler (@Nonnegative final int nMaxRetries, @Nonnull final ERetryMode eRetryMode)
  {
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");
    ValueEnforcer.notNull (eRetryMode, "RetryMode");
    m_nMaxRetries = nMaxRetries;
    m_eRetryMode = eRetryMode;
  }

  @Nonnegative
  public int getMaxRetries ()
  {
    return m_nMaxRetries;
  }

  @Nonnull
  public ERetryMode getRetryMode ()
  {
    return m_eRetryMode;
  }

  public boolean retryRequest (final IOException aEx, final int nExecutionCount, final HttpContext aContext)
  {
    if (nExecutionCount >= m_nMaxRetries)
    {
      // Do not retry if over max retry count
      return false;
    }

    // Check if exceptions make the retry unnecessary
    if (m_eRetryMode.isCheckException ())
    {
      if (aEx instanceof InterruptedIOException)
      {
        // Timeout
        return false;
      }
      if (aEx instanceof UnknownHostException)
      {
        // Unknown host
        return false;
      }
      if (aEx instanceof ConnectTimeoutException)
      {
        // Connection refused
        return false;
      }
      if (aEx instanceof SSLException)
      {
        // SSL handshake exception
        return false;
      }
    }

    switch (m_eRetryMode)
    {
      case RETRY_ALWAYS:
      case RETRY_SMART:
        return true;
      case RETRY_IDEMPOTENT_ONLY:
        final HttpClientContext aClientContext = HttpClientContext.adapt (aContext);
        final HttpRequest aRequest = aClientContext.getRequest ();
        final boolean bIdempotent = !(aRequest instanceof HttpEntityEnclosingRequest);
        if (bIdempotent)
        {
          // Retry if the request is considered idempotent
          // (if it has no payload)
          return true;
        }
        return false;
      default:
        throw new IllegalStateException ("Unsupported retry mode: " + m_eRetryMode);
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("MaxRetries", m_nMaxRetries)
                                       .append ("RetryMode", m_eRetryMode)
                                       .getToString ();
  }
}
