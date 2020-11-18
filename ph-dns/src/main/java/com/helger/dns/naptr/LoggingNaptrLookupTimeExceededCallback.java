/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.dns.naptr;

import java.time.Duration;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.ToStringGenerator;

/**
 * A logging implementation of {@link INaptrLookupTimeExceededCallback}.
 *
 * @author Philip Helger
 * @since 9.5.0
 */
public class LoggingNaptrLookupTimeExceededCallback implements INaptrLookupTimeExceededCallback
{
  private static final Logger LOGGER = LoggerFactory.getLogger (LoggingNaptrLookupTimeExceededCallback.class);

  private boolean m_bEmitStackTrace;

  public LoggingNaptrLookupTimeExceededCallback (final boolean bEmitStackTrace)
  {
    setEmitStackTrace (bEmitStackTrace);
  }

  public final boolean isEmitStackTrace ()
  {
    return m_bEmitStackTrace;
  }

  @Nonnull
  public final LoggingNaptrLookupTimeExceededCallback setEmitStackTrace (final boolean bEmitStackTrace)
  {
    m_bEmitStackTrace = bEmitStackTrace;
    return this;
  }

  public void onLookupTimeExceeded (@Nonnull final String sMsg,
                                    @Nonnull final Duration aExecutionMillis,
                                    @Nonnull final Duration aLimitMillis)
  {
    LOGGER.warn (sMsg + " took " + aExecutionMillis.toMillis () + "ms (limit is " + aLimitMillis.toMillis () + " ms)",
                 m_bEmitStackTrace ? new Exception () : null);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("EmitStackTrace", m_bEmitStackTrace).getToString ();
  }
}
