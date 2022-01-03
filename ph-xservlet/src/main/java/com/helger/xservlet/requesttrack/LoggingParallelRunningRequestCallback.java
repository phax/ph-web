/*
 * Copyright (C) 2016-2022 Philip Helger (www.helger.com)
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

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.level.IErrorLevel;
import com.helger.commons.log.LogHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * A simple implementation of {@link IParallelRunningRequestCallback} simply
 * logging such events.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class LoggingParallelRunningRequestCallback implements IParallelRunningRequestCallback
{
  private static final Logger LOGGER = LoggerFactory.getLogger (LoggingParallelRunningRequestCallback.class);

  private IErrorLevel m_aErrorLevel;

  public LoggingParallelRunningRequestCallback ()
  {
    this (EErrorLevel.WARN);
  }

  public LoggingParallelRunningRequestCallback (@Nonnull final IErrorLevel aErrorLevel)
  {
    setErrorLevel (aErrorLevel);
  }

  @Nonnull
  public final IErrorLevel getErrorLevel ()
  {
    return m_aErrorLevel;
  }

  @Nonnull
  public final LoggingParallelRunningRequestCallback setErrorLevel (@Nonnull final IErrorLevel aErrorLevel)
  {
    m_aErrorLevel = ValueEnforcer.notNull (aErrorLevel, "ErrorLevel");
    return this;
  }

  public void onParallelRunningRequests (@Nonnegative final int nParallelRequests, @Nonnull @Nonempty final List <TrackedRequest> aRequests)
  {
    LogHelper.log (LOGGER, m_aErrorLevel, "Currently " + nParallelRequests + " parallel requests are active!");
  }

  public void onParallelRunningRequestsBelowLimit ()
  {
    LogHelper.log (LOGGER, m_aErrorLevel, "Parallel requests are back to normal!");
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("errorLevel", m_aErrorLevel).getToString ();
  }
}
