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
package com.helger.jsch.session;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.tostring.ToStringGenerator;
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

  private final ISessionProvider m_aSessionProvider;
  private final String m_sSessionDisplayName;

  // Status vars
  private Session m_aSession;

  /**
   * Creates a SessionManager for the supplied <code>sessionFactory</code>.
   *
   * @param aSessionFactory
   *        The session factory
   * @param sSessionDisplayName
   *        The session display name for logging etc.
   */
  public SessionManager (@NonNull final ISessionProvider aSessionFactory, @NonNull final String sSessionDisplayName)
  {
    ValueEnforcer.notNull (aSessionFactory, "SessionFactory");
    ValueEnforcer.notNull (sSessionDisplayName, "SessionDisplayName");
    m_aSessionProvider = aSessionFactory;
    m_sSessionDisplayName = sSessionDisplayName;
  }

  /**
   * @return the session factory used by this manager. Never <code>null</code>.
   */
  @NonNull
  public final ISessionProvider getSessionFactory ()
  {
    return m_aSessionProvider;
  }

  @Override
  public void close () throws IOException
  {
    if (m_aSession != null)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Disconnecting JSCH session now");
      if (m_aSession.isConnected ())
        m_aSession.disconnect ();
      m_aSession = null;
    }
  }

  /**
   * Returns a connected session. Gets or creates from the underlying session
   * factory
   *
   * @return A connected session
   * @throws JSchException
   *         If unable to connect the session
   */
  @NonNull
  public Session getSession () throws JSchException
  {
    if (m_aSession == null || !m_aSession.isConnected ())
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Getting new JSCH session from session factory");
      m_aSession = m_aSessionProvider.createSession ();

      // Avoid double connect
      if (!m_aSession.isConnected ())
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Explicitly connecting JSCH session");
        m_aSession.connect ();
      }
    }
    return m_aSession;
  }

  @NonNull
  @Nonempty
  public String getAsString ()
  {
    return m_sSessionDisplayName;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("SessionFactory", m_aSessionProvider)
                                       .append ("SessionDisplayName", m_sSessionDisplayName)
                                       .getToString ();
  }

  @NonNull
  public static SessionManager create (@NonNull final ISessionFactory aFactory)
  {
    return new SessionManager (aFactory, aFactory.getAsString ());
  }
}
