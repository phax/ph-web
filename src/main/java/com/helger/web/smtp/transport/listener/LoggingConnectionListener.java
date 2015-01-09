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
package com.helger.web.smtp.transport.listener;

import javax.annotation.Nonnull;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.EErrorLevel;
import com.helger.commons.log.LogUtils;
import com.helger.commons.string.ToStringGenerator;

/**
 * An implementation of {@link ConnectionListener} that logs stuff to a logger.
 * 
 * @author Philip Helger
 */
public class LoggingConnectionListener implements ConnectionListener
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (LoggingConnectionListener.class);

  private final EErrorLevel m_eErrorLevel;

  public LoggingConnectionListener ()
  {
    this (EErrorLevel.INFO);
  }

  public LoggingConnectionListener (@Nonnull final EErrorLevel eErrorLevel)
  {
    m_eErrorLevel = ValueEnforcer.notNull (eErrorLevel, "ErrorLevel");
  }

  @Nonnull
  public EErrorLevel getErrorLevel ()
  {
    return m_eErrorLevel;
  }

  public void opened (@Nonnull final ConnectionEvent aEvent)
  {
    LogUtils.log (s_aLogger, m_eErrorLevel, "Connected to SMTP server");
  }

  public void disconnected (@Nonnull final ConnectionEvent aEvent)
  {
    LogUtils.log (s_aLogger, m_eErrorLevel, "Disconnected from SMTP server");
  }

  public void closed (@Nonnull final ConnectionEvent aEvent)
  {
    LogUtils.log (s_aLogger, m_eErrorLevel, "Closed connection to SMTP server");
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).toString ();
  }
}
