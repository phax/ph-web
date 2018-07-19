/**
 * Copyright (C) 2016-2018 Philip Helger (www.helger.com)
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
package com.helger.xservlet.servletstatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsEnumMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.string.ToStringGenerator;

/**
 * Keep the status of a single servlet.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@NotThreadSafe
public final class ServletStatus implements Serializable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ServletStatus.class);

  private final String m_sClassName;
  private EServletStatus m_eCurrentStatus;
  private final ICommonsMap <EServletStatus, LocalDateTime> m_aStatusChangeDates = new CommonsEnumMap <> (EServletStatus.class);
  private final AtomicInteger m_aInvocationCount = new AtomicInteger (0);

  public ServletStatus (@Nonnull @Nonempty final String sClassName)
  {
    m_sClassName = ValueEnforcer.notEmpty (sClassName, "ClassName");
  }

  @Nonnull
  @Nonempty
  public String getClassName ()
  {
    return m_sClassName;
  }

  @Nullable
  public EServletStatus getCurrentStatus ()
  {
    return m_eCurrentStatus;
  }

  protected void internalSetCurrentStatus (@Nonnull final EServletStatus eNewStatus)
  {
    ValueEnforcer.notNull (eNewStatus, "NewStatus");
    if (!EServletStatus.isValidSuccessorOf (m_eCurrentStatus, eNewStatus))
    {
      // This should never happen
      // If it happens, it is an indicator that the Servlet startup failed.
      // In that case please check the logfiles (e.g.
      // "localhost.yyyy-mm-dd.log")
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("The new status " +
                         eNewStatus +
                         " is not a valid successor of the old status " +
                         m_eCurrentStatus +
                         " for " +
                         m_sClassName);
    }
    m_eCurrentStatus = ValueEnforcer.notNull (eNewStatus, "NewStatus");
    m_aStatusChangeDates.put (eNewStatus, PDTFactory.getCurrentLocalDateTime ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <EServletStatus, LocalDateTime> getStatusChangeMap ()
  {
    return m_aStatusChangeDates.getClone ();
  }

  @Nullable
  public LocalDateTime getDateTimeOfStatus (@Nonnull final EServletStatus eStatus)
  {
    return eStatus == null ? null : m_aStatusChangeDates.get (eStatus);
  }

  protected void internalIncrementInvocationCount ()
  {
    m_aInvocationCount.incrementAndGet ();
  }

  @Nonnegative
  public int getInvocationCount ()
  {
    return m_aInvocationCount.get ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ClassName", m_sClassName)
                                       .append ("CurrentStatus", m_eCurrentStatus)
                                       .append ("StatusChangeDates", m_aStatusChangeDates)
                                       .append ("InvocationCount", m_aInvocationCount.get ())
                                       .getToString ();
  }
}
