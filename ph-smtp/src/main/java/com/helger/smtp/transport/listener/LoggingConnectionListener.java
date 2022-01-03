/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
package com.helger.smtp.transport.listener;

import javax.annotation.Nonnull;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.callback.ICallback;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.level.IErrorLevel;
import com.helger.commons.log.LogHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * An implementation of {@link ConnectionListener} that logs stuff to a logger.
 *
 * @author Philip Helger
 */
public class LoggingConnectionListener implements ConnectionListener, ICallback
{
  private static final Logger LOGGER = LoggerFactory.getLogger (LoggingConnectionListener.class);

  private final IErrorLevel m_aErrorLevel;

  public LoggingConnectionListener ()
  {
    this (EErrorLevel.INFO);
  }

  public LoggingConnectionListener (@Nonnull final IErrorLevel aErrorLevel)
  {
    m_aErrorLevel = ValueEnforcer.notNull (aErrorLevel, "ErrorLevel");
  }

  @Nonnull
  public IErrorLevel getErrorLevel ()
  {
    return m_aErrorLevel;
  }

  public void opened (@Nonnull final ConnectionEvent aEvent)
  {
    LogHelper.log (LOGGER, m_aErrorLevel, "Connected to SMTP server");
  }

  public void disconnected (@Nonnull final ConnectionEvent aEvent)
  {
    LogHelper.log (LOGGER, m_aErrorLevel, "Disconnected from SMTP server");
  }

  public void closed (@Nonnull final ConnectionEvent aEvent)
  {
    LogHelper.log (LOGGER, m_aErrorLevel, "Closed connection to SMTP server");
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ErrorLevel", m_aErrorLevel).getToString ();
  }
}
