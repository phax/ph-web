/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.xservlet.requesttrack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.scope.IRequestWebScope;

/**
 * This class represents a single tracked request.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@Immutable
public final class TrackedRequest
{
  private final String m_sRequestID;
  private final IRequestWebScope m_aRequestScope;
  private final long m_nStartMillis;

  public TrackedRequest (@Nonnull @Nonempty final String sRequestID, @Nonnull final IRequestWebScope aRequestScope)
  {
    m_sRequestID = ValueEnforcer.notEmpty (sRequestID, "RequestID");
    m_aRequestScope = ValueEnforcer.notNull (aRequestScope, "RequestScope");
    m_nStartMillis = System.currentTimeMillis ();
  }

  @Nonnull
  @Nonempty
  public String getRequestID ()
  {
    return m_sRequestID;
  }

  @Nonnull
  public IRequestWebScope getRequestScope ()
  {
    return m_aRequestScope;
  }

  @Nonnegative
  public long getStartTimeMilliseconds ()
  {
    return m_nStartMillis;
  }

  @Nonnegative
  public long getRunningMilliseconds ()
  {
    return System.currentTimeMillis () - m_nStartMillis;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("RequestID", m_sRequestID)
                                       .append ("RequestScope", m_aRequestScope)
                                       .append ("StartMillis", m_nStartMillis)
                                       .getToString ();
  }
}
