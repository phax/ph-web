/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.pastdev.jsch.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ICloneable;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.pastdev.jsch.ISessionFactory;
import com.pastdev.jsch.SessionManager;

/**
 * Provides a convenience wrapper around an <code>exec</code> channel. This
 * implementation offers a simplified interface to executing remote commands and
 * retrieving the results of execution.
 *
 * @see com.jcraft.jsch.ChannelExec
 */
public class CommandRunner implements AutoCloseable, ICloneable <CommandRunner>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CommandRunner.class);

  protected final SessionManager m_aSessionManager;

  /**
   * Creates a new CommandRunner that will use a {@link SessionManager} that
   * wraps the supplied <code>sessionFactory</code>.
   *
   * @param sessionFactory
   *        The factory used to create a session manager
   */
  public CommandRunner (@Nonnull final ISessionFactory sessionFactory)
  {
    m_aSessionManager = new SessionManager (sessionFactory);
  }

  /**
   * Closes the underlying {@link SessionManager}.
   *
   * @see SessionManager#close()
   */
  public void close () throws IOException
  {
    m_aSessionManager.close ();
  }

  /**
   * Returns a new CommandRunner with the same SessionFactory, but will create a
   * separate session.
   *
   * @return A duplicate CommandRunner with a different session.
   */
  @Nonnull
  @ReturnsMutableCopy
  public CommandRunner getClone ()
  {
    return new CommandRunner (m_aSessionManager.getSessionFactory ());
  }

  /**
   * Executes <code>command</code> and returns the result. Use this method when
   * the command you are executing requires no input, writes only UTF-8
   * compatible text to STDOUT and/or STDERR, and you are comfortable with
   * buffering up all of that data in memory. Otherwise, use
   * {@link #open(String)}, which allows you to work with the underlying
   * streams.
   *
   * @param command
   *        The command to execute
   * @return The resulting data
   * @throws JSchException
   *         If ssh execution fails
   * @throws IOException
   *         If unable to read the result data
   */
  public ExecuteResult execute (final String command) throws JSchException, IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("executing " + command + " on " + m_aSessionManager);

    final Session session = m_aSessionManager.getSession ();

    // Using the synchronized BAOS is okay here
    final ByteArrayOutputStream stdErr = new ByteArrayOutputStream ();
    final ByteArrayOutputStream stdOut = new ByteArrayOutputStream ();
    int exitCode;
    ChannelExecWrapper channel = null;
    try
    {
      channel = new ChannelExecWrapper (session, command, null, stdOut, stdErr);
    }
    finally
    {
      // Wait until the execution finished
      exitCode = channel.close ();
    }

    return new ExecuteResult (exitCode,
                              new String (stdOut.toByteArray (), StandardCharsets.UTF_8),
                              new String (stdErr.toByteArray (), StandardCharsets.UTF_8));
  }

  /**
   * Executes <code>command</code> and returns an execution wrapper that
   * provides safe access to and management of the underlying streams of data.
   *
   * @param command
   *        The command to execute
   * @return An execution wrapper that allows you to process the streams
   * @throws JSchException
   *         If ssh execution fails
   * @throws IOException
   *         If unable to read the result data
   */
  @Nonnull
  public ChannelExecWrapper open (final String command) throws JSchException, IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("executing '" + command + "' on " + m_aSessionManager);
    return new ChannelExecWrapper (m_aSessionManager.getSession (), command, null, null, null);
  }

  /**
   * A simple container for the results of a command execution. Contains
   * <ul>
   * <li>The exit code</li>
   * <li>The text written to STDOUT</li>
   * <li>The text written to STDERR</li>
   * </ul>
   * The text will be UTF-8 decoded byte data written by the command.
   */
  public class ExecuteResult
  {
    private final int m_nExitCode;
    private final String m_sStderr;
    private final String m_sStdout;

    public ExecuteResult (final int exitCode, final String stdout, final String stderr)
    {
      m_nExitCode = exitCode;
      m_sStderr = stderr;
      m_sStdout = stdout;
    }

    /**
     * Returns the exit code of the command execution.
     *
     * @return The exit code
     */
    public int getExitCode ()
    {
      return m_nExitCode;
    }

    /**
     * Returns the text written to STDERR. This will be a UTF-8 decoding of the
     * actual bytes written to STDERR.
     *
     * @return The text written to STDERR
     */
    public String getStderr ()
    {
      return m_sStderr;
    }

    /**
     * Returns the text written to STDOUT. This will be a UTF-8 decoding of the
     * actual bytes written to STDOUT.
     *
     * @return The text written to STDOUT
     */
    public String getStdout ()
    {
      return m_sStdout;
    }
  }

  /**
   * Wraps the execution of a command to handle the opening and closing of all
   * the data streams for you. To use this wrapper, you call
   * <code>getXxxStream()</code> for the streams you want to work with, which
   * will return an opened stream. Use the stream as needed then call
   * {@link ChannelExecWrapper#close() close()} on the ChannelExecWrapper
   * itself, which will return the the exit code from the execution of the
   * command.
   */
  public class ChannelExecWrapper
  {
    private final String m_sCommand;
    private final ChannelExec m_aChannel;
    private OutputStream m_aPassedInStdErr;
    private InputStream m_aPassedInStdIn;
    private OutputStream m_aPassedInStdOut;
    private InputStream m_aStdErr;
    private OutputStream m_aStdIn;
    private InputStream m_aStdOut;

    public ChannelExecWrapper (@Nonnull final Session session,
                               @Nonnull final String command,
                               @Nullable final InputStream stdIn,
                               @Nullable final OutputStream stdOut,
                               @Nullable final OutputStream stdErr) throws JSchException
    {
      m_sCommand = command;
      m_aChannel = (ChannelExec) session.openChannel ("exec");
      if (stdIn != null)
      {
        m_aPassedInStdIn = stdIn;
        m_aChannel.setInputStream (stdIn);
      }
      if (stdOut != null)
      {
        m_aPassedInStdOut = stdOut;
        m_aChannel.setOutputStream (stdOut);
      }
      if (stdErr != null)
      {
        m_aPassedInStdErr = stdErr;
        m_aChannel.setErrStream (stdErr);
      }
      m_aChannel.setCommand (command);
      m_aChannel.connect ();
    }

    /**
     * Safely closes all stream, waits for the underlying connection to close,
     * then returns the exit code from the command execution.
     *
     * @return The exit code from the command execution
     */
    public int close ()
    {
      int exitCode = -2;
      if (m_aChannel != null)
      {
        try
        {
          // In jsch closing the output stream causes an ssh
          // message to get sent in another thread. It returns
          // before the message was actually sent. So now i
          // wait until the exit status is no longer -1 (active).
          StreamHelper.close (m_aPassedInStdIn);
          StreamHelper.close (m_aPassedInStdOut);
          StreamHelper.close (m_aPassedInStdErr);
          StreamHelper.close (m_aStdIn);
          StreamHelper.close (m_aStdOut);
          StreamHelper.close (m_aStdErr);
          int i = 0;
          while (!m_aChannel.isClosed ())
          {
            if (LOGGER.isTraceEnabled ())
              LOGGER.trace ("waiting for exit " + i++);
            ThreadHelper.sleep (50);
          }
          exitCode = m_aChannel.getExitStatus ();
        }
        finally
        {
          if (m_aChannel.isConnected ())
            m_aChannel.disconnect ();
        }
      }
      if (LOGGER.isTraceEnabled ())
        LOGGER.trace ("'" + m_sCommand + "' exit " + exitCode);
      return exitCode;
    }

    /**
     * Returns the STDERR stream for you to read from. No need to close this
     * stream independently, instead, when done with all processing, call
     * {@link #close()};
     *
     * @return The STDERR stream
     * @throws IOException
     *         If unable to read from the stream
     */
    public InputStream getErrStream () throws IOException
    {
      if (m_aStdErr == null)
        m_aStdErr = m_aChannel.getErrStream ();
      return m_aStdErr;
    }

    /**
     * Returns the STDOUT stream for you to read from. No need to close this
     * stream independently, instead, when done with all processing, call
     * {@link #close()};
     *
     * @return The STDOUT stream
     * @throws IOException
     *         If unable to read from the stream
     */
    public InputStream getInputStream () throws IOException
    {
      if (m_aStdOut == null)
        m_aStdOut = m_aChannel.getInputStream ();
      return m_aStdOut;
    }

    /**
     * Returns the STDIN stream for you to write to. No need to close this
     * stream independently, instead, when done with all processing, call
     * {@link #close()};
     *
     * @return The STDIN stream
     * @throws IOException
     *         If unable to write to the stream
     */
    public OutputStream getOutputStream () throws IOException
    {
      if (m_aStdIn == null)
        m_aStdIn = m_aChannel.getOutputStream ();
      return m_aStdIn;
    }
  }
}
