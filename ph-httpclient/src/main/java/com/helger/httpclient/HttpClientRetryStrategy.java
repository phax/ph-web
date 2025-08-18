/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.util.TimeValue;

import com.helger.annotation.Nonnegative;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.tostring.ToStringGenerator;

import jakarta.annotation.Nonnull;

/**
 * HTTP client retry handler based on DefaultHttpRequestRetryStrategy
 *
 * @author Apache HC
 */
public class HttpClientRetryStrategy extends DefaultHttpRequestRetryStrategy
{
  private final int m_nMaxRetries;
  private final TimeValue m_aRetryInterval;
  private final boolean m_bRetryAlways;

  public HttpClientRetryStrategy (@Nonnegative final int nMaxRetries,
                                  @Nonnull final TimeValue aRetryInterval,
                                  final boolean bRetryAlways)
  {
    super (nMaxRetries, aRetryInterval);
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");
    ValueEnforcer.notNull (aRetryInterval, "RetryInterval");
    m_nMaxRetries = nMaxRetries;
    m_aRetryInterval = aRetryInterval;
    m_bRetryAlways = bRetryAlways;
  }

  @Nonnegative
  public final int getMaxRetries ()
  {
    return m_nMaxRetries;
  }

  @Nonnull
  public final TimeValue getRetryInterval ()
  {
    return m_aRetryInterval;
  }

  @Override
  protected boolean handleAsIdempotent (final HttpRequest request)
  {
    if (m_bRetryAlways)
      return true;

    return super.handleAsIdempotent (request);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("MaxRetries", m_nMaxRetries)
                                       .append ("RetryInterval", m_aRetryInterval)
                                       .append ("RetryAlways", m_bRetryAlways)
                                       .getToString ();
  }
}
