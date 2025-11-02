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
package com.helger.jsch.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.WillNotClose;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.clone.ICloneable;
import com.helger.base.concurrent.ThreadHelper;
import com.helger.base.io.stream.StreamHelper;
import com.helger.jsch.session.ISessionFactory;
import com.helger.jsch.session.SessionManager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
   * @param aSessionFactory
   *        The factory used to create a session manager
   */
  public CommandRunner (@NonNull final ISessionFactory aSessionFactory)
  {
    m_aSessionManager = SessionManager.create (aSessionFactory);
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
  @NonNull
  @ReturnsMutableCopy
  public CommandRunner getClone ()
  {
    // Ensured via the constructor of this class that it is a ISessionFactory
    return new CommandRunner ((ISessionFactory) m_aSessionManager.getSessionFactory ());
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
  @NonNull
  public ExecuteResult execute (final String command) throws JSchException, IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("executing " + command + " on " + m_aSessionManager.getAsString ());

    final Session aSession = m_aSessionManager.getSession ();

    // Using the synchronized BAOS is okay here
    try (final ByteArrayOutputStream stdErr = new ByteArrayOutputStream ();
        final ByteArrayOutputStream stdOut = new ByteArrayOutputStream ())
    {
      int nExitCode;
      ChannelExecWrapper aChannel = null;
      try
      {
        aChannel = new ChannelExecWrapper (aSession, command, null, stdOut, stdErr);
      }
      finally
      {
        // Wait until the execution finished
        nExitCode = aChannel.close ();
      }

      return new ExecuteResult (nExitCode,
                                new String (stdOut.toByteArray (), StandardCharsets.UTF_8),
                                new String (stdErr.toByteArray (), StandardCharsets.UTF_8));
    }
  }

  /**
   * Executes <code>command</code> and returns an execution wrapper that
   * provides safe access to and management of the underlying streams of data.
   *
   * @param sCommand
   *        The command to execute
   * @return An execution wrapper that allows you to process the streams
   * @throws JSchException
   *         If ssh execution fails
   * @throws IOException
   *         If unable to read the result data
   */
  @NonNull
  public ChannelExecWrapper open (final String sCommand) throws JSchException, IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("executing '" + sCommand + "' on " + m_aSessionManager.getAsString ());
    return new ChannelExecWrapper (m_aSessionManager.getSession (), sCommand, null, null, null);
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
  public static class ExecuteResult
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
  public static class ChannelExecWrapper
  {
    private final String m_sCommand;
    private final ChannelExec m_aChannel;
    private OutputStream m_aPassedInStdErr;
    private InputStream m_aPassedInStdIn;
    private OutputStream m_aPassedInStdOut;
    private InputStream m_aStdErr;
    private OutputStream m_aStdIn;
    private InputStream m_aStdOut;

    public ChannelExecWrapper (@NonNull final Session aSession,
                               @NonNull final String sCommand,
                               @Nullable final InputStream aStdIn,
                               @Nullable final OutputStream aStdOut,
                               @Nullable final OutputStream aStdErr) throws JSchException
    {
      m_sCommand = sCommand;
      m_aChannel = (ChannelExec) aSession.openChannel ("exec");
      if (aStdIn != null)
      {
        m_aPassedInStdIn = aStdIn;
        m_aChannel.setInputStream (aStdIn);
      }
      if (aStdOut != null)
      {
        m_aPassedInStdOut = aStdOut;
        m_aChannel.setOutputStream (aStdOut);
      }
      if (aStdErr != null)
      {
        m_aPassedInStdErr = aStdErr;
        m_aChannel.setErrStream (aStdErr);
      }
      m_aChannel.setCommand (sCommand);
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
      int nExitCode = -2;
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
              LOGGER.trace ("waiting for exit " + (i++));
            ThreadHelper.sleep (50);
          }
          nExitCode = m_aChannel.getExitStatus ();
        }
        finally
        {
          if (m_aChannel.isConnected ())
            m_aChannel.disconnect ();
        }
      }
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("'" + m_sCommand + "' exit " + nExitCode);
      return nExitCode;
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
    @NonNull
    @WillNotClose
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
    @NonNull
    @WillNotClose
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
    @NonNull
    @WillNotClose
    public OutputStream getOutputStream () throws IOException
    {
      if (m_aStdIn == null)
        m_aStdIn = m_aChannel.getOutputStream ();
      return m_aStdIn;
    }
  }
}
