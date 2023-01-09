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
package com.helger.jsch.sftp;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jsch.session.ISessionFactory;
import com.helger.jsch.session.SessionManager;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

/**
 * Provides a convenience wrapper around an <code>sftp</code> channel. This
 * implementation offers a simplified interface that manages the resources
 * needed to issue <code>sftp</code> commands.
 *
 * @see com.jcraft.jsch.ChannelSftp
 */
public class SftpRunner implements AutoCloseable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SftpRunner.class);
  private static final String CHANNEL_SFTP = "sftp";

  private final SessionManager m_aSessionManager;

  /**
   * Creates a new SftpRunner that will use a {@link SessionManager} that wraps
   * the supplied <code>sessionFactory</code>.
   *
   * @param aSessionFactory
   *        The factory used to create a session manager
   */
  public SftpRunner (@Nonnull final ISessionFactory aSessionFactory)
  {
    m_aSessionManager = SessionManager.create (aSessionFactory);
  }

  /**
   * Executes the <code>sftp</code> callback providing it an open
   * {@link ChannelSftp}. SFTP callback implementations should <i>NOT</i> close
   * the channel.
   *
   * @param aSftp
   *        A callback to invoke with the {@link ChannelSftp}
   * @throws JSchException
   *         If ssh execution fails
   * @throws IOException
   *         If unable to read the result data
   */
  public void execute (@Nonnull final ISftp aSftp) throws JSchException, IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("executing sftp command on " + m_aSessionManager.getAsString ());

    ChannelSftp aChannelSftp = null;
    try
    {
      aChannelSftp = (ChannelSftp) m_aSessionManager.getSession ().openChannel (CHANNEL_SFTP);
      aChannelSftp.connect ();
      aSftp.run (aChannelSftp);
    }
    finally
    {
      if (aChannelSftp != null)
        aChannelSftp.disconnect ();
    }
  }

  /**
   * Closes the underlying {@link SessionManager}.
   *
   * @see SessionManager#close()
   */
  @Override
  public void close () throws IOException
  {
    m_aSessionManager.close ();
  }
}
