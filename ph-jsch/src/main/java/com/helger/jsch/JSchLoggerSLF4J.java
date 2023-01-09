/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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
package com.helger.jsch;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;

/**
 * JSch logger implementation on top of SLF4J.
 *
 * @author Philip Helger
 */
@Immutable
public class JSchLoggerSLF4J implements com.jcraft.jsch.Logger
{
  private final Logger m_aLogger;

  public JSchLoggerSLF4J (@Nonnull final Class <?> aClass)
  {
    this (aClass.getName ());
  }

  public JSchLoggerSLF4J (@Nonnull @Nonempty final String sLoggerName)
  {
    ValueEnforcer.notEmpty (sLoggerName, "LoggerName");
    m_aLogger = LoggerFactory.getLogger (sLoggerName);
  }

  @Nonnull
  public Logger getLogger ()
  {
    return m_aLogger;
  }

  public boolean isEnabled (final int nLevel)
  {
    if (nLevel >= ERROR)
      return m_aLogger.isErrorEnabled ();
    if (nLevel >= WARN)
      return m_aLogger.isWarnEnabled ();
    if (nLevel >= INFO)
      return m_aLogger.isInfoEnabled ();
    if (nLevel >= DEBUG)
      return m_aLogger.isDebugEnabled ();
    return m_aLogger.isTraceEnabled ();
  }

  public void log (final int nLevel, final String sMessage)
  {
    if (nLevel >= ERROR)
      m_aLogger.error (sMessage);
    else
      if (nLevel >= WARN)
        m_aLogger.warn (sMessage);
      else
        if (nLevel >= INFO)
          m_aLogger.info (sMessage);
        else
          if (nLevel >= DEBUG)
            m_aLogger.debug (sMessage);
          else
            m_aLogger.trace (sMessage);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Logger", m_aLogger).getToString ();
  }
}
