/*
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.jsch.session;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Provides a convenience wrapper to sessions that maintains the session
 * connection for you. Every time you obtain your session through a call to
 * {@link #getSession()} the current session will have its connection verified,
 * and will reconnect if necessary.
 */
public class SessionManager implements AutoCloseable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SessionManager.class);

  private final ISessionFactory m_aSessionFactory;
  // Status vars
  private Session m_aSession;

  /**
   * Creates a SessionManager for the supplied <code>sessionFactory</code>.
   *
   * @param aSessionFactory
   *        The session factory
   */
  public SessionManager (@Nonnull final ISessionFactory aSessionFactory)
  {
    ValueEnforcer.notNull (aSessionFactory, "SessionFactory");
    m_aSessionFactory = aSessionFactory;
  }

  /**
   * @return the session factory used by this manager. Never <code>null</code>.
   */
  @Nonnull
  public ISessionFactory getSessionFactory ()
  {
    return m_aSessionFactory;
  }

  @Override
  public void close () throws IOException
  {
    if (m_aSession != null && m_aSession.isConnected ())
      m_aSession.disconnect ();
    m_aSession = null;
  }

  /**
   * Returns a connected session.
   *
   * @return A connected session
   * @throws JSchException
   *         If unable to connect the session
   */
  @Nonnull
  public Session getSession () throws JSchException
  {
    if (m_aSession == null || !m_aSession.isConnected ())
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("getting new session from factory session");
      m_aSession = m_aSessionFactory.newSession ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("connecting session");
      m_aSession.connect ();
    }
    return m_aSession;
  }

  @Nonnull
  @Nonempty
  public String getAsString ()
  {
    return m_aSessionFactory.getAsString ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("SessionFactory", m_aSessionFactory).getToString ();
  }
}
