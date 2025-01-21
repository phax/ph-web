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
package com.helger.jsch.scp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.NonBlockingStack;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Based on protocol information found
 * <a href="https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works"
 * >here</a>
 *
 * @author LTHEISEN
 */
public class ScpConnection implements Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpConnection.class);

  private final ChannelExec m_aChannel;
  private final NonBlockingStack <ICurrentEntry> m_aEntryStack = new NonBlockingStack <> ();
  private final InputStream m_aIS;
  private final OutputStream m_aOS;
  private final Session m_aSession;

  public ScpConnection (@Nonnull final ISessionFactory aSessionFactory,
                        final String sPath,
                        @Nonnull final EScpMode eScpMode,
                        @Nullable final ECopyMode eCopyMode) throws JSchException, IOException
  {
    ValueEnforcer.notNull (aSessionFactory, "SessionFactory");
    ValueEnforcer.notNull (eScpMode, "ScpMode");

    m_aSession = aSessionFactory.createSession ();
    if (!m_aSession.isConnected ())
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("connecting session");
      m_aSession.connect ();
    }

    final String sCommand = _getCommand (eScpMode, eCopyMode, sPath);
    m_aChannel = (ChannelExec) m_aSession.openChannel ("exec");

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("setting exec command to '" + sCommand + "'");
    m_aChannel.setCommand (sCommand);

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("connecting channel");
    m_aChannel.connect ();

    m_aOS = m_aChannel.getOutputStream ();
    m_aIS = m_aChannel.getInputStream ();

    if (eScpMode == EScpMode.FROM)
      _writeAck ();
    else
      if (eScpMode == EScpMode.TO)
        _checkAck ();
  }

  @Nonnull
  @Nonempty
  private static String _getCommand (@Nonnull final EScpMode eScpMode,
                                     @Nullable final ECopyMode eCopyMode,
                                     @Nonnull final String sPath)
  {
    final StringBuilder aSB;
    switch (eScpMode)
    {
      case TO:
        aSB = new StringBuilder ("scp -tq");
        break;
      case FROM:
        aSB = new StringBuilder ("scp -fq");
        break;
      default:
        throw new IllegalStateException ();
    }

    if (eCopyMode == ECopyMode.RECURSIVE)
      aSB.append ('r');
    return aSB.append (' ').append (sPath).toString ();
  }

  /**
   * Throws an JSchIOException if ack was in error. Ack codes are:
   *
   * <pre>
   *   0 for success,
   *   1 for error,
   *   2 for fatal error
   * </pre>
   *
   * Also throws, IOException if unable to read from the InputStream. If nothing
   * was thrown, ack was a success.
   */
  private int _checkAck () throws IOException
  {
    if (LOGGER.isTraceEnabled ())
      LOGGER.trace ("wait for ack");
    final int b = m_aIS.read ();
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("ack response: '" + b + "'");

    if (b == 1 || b == 2)
    {
      final StringBuilder aSB = new StringBuilder ();
      int c;
      while ((c = m_aIS.read ()) != '\n')
      {
        aSB.append ((char) c);
      }
      if (b == 1 || b == 2)
        throw new IOException (aSB.toString ());
    }

    return b;
  }

  public void close () throws IOException
  {
    IOException aToThrow = null;
    try
    {
      while (!m_aEntryStack.isEmpty ())
      {
        m_aEntryStack.pop ().complete ();
      }
    }
    catch (final IOException e)
    {
      aToThrow = e;
    }

    StreamHelper.close (m_aOS);
    StreamHelper.close (m_aIS);

    if (m_aChannel != null && m_aChannel.isConnected ())
    {
      m_aChannel.disconnect ();
    }
    if (m_aSession != null && m_aSession.isConnected ())
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("disconnecting session");
      m_aSession.disconnect ();
    }

    if (aToThrow != null)
      throw aToThrow;
  }

  public void closeEntry () throws IOException
  {
    m_aEntryStack.pop ().complete ();
  }

  @Nullable
  public InputStream getCurrentInputStream ()
  {
    if (m_aEntryStack.isEmpty ())
      return null;
    final ICurrentEntry aEntry = m_aEntryStack.peek ();
    return aEntry instanceof InputStream ? (InputStream) aEntry : null;
  }

  @Nullable
  public OutputStream getCurrentOuputStream ()
  {
    if (m_aEntryStack.isEmpty ())
      return null;
    final ICurrentEntry aEntry = m_aEntryStack.peek ();
    return aEntry instanceof OutputStream ? (OutputStream) aEntry : null;
  }

  @Nullable
  public ScpEntry getNextEntry () throws IOException
  {
    if (!m_aEntryStack.isEmpty () && !m_aEntryStack.peek ().isDirectoryEntry ())
    {
      closeEntry ();
    }

    final ScpEntry entry = _parseMessage ();
    if (entry == null)
      return null;

    if (entry.isEndOfDirectory ())
    {
      while (!m_aEntryStack.isEmpty ())
      {
        final boolean isDirectory = m_aEntryStack.peek ().isDirectoryEntry ();
        closeEntry ();
        if (isDirectory)
          break;
      }
    }
    else
      if (entry.isDirectory ())
      {
        m_aEntryStack.push (new InputDirectoryEntry ());
      }
      else
      {
        m_aEntryStack.push (new EntryInputStream (entry));
      }
    return entry;
  }

  /**
   * Parses SCP protocol messages, for example:
   *
   * <pre>
   *     File:          C0640 13 test.txt
   *     Directory:     D0750 0 testdir
   *     End Directory: E
   * </pre>
   *
   * @return An ScpEntry for a file (C), directory (D), end of directory (E), or
   *         null when no more messages are available.
   * @throws IOException
   */
  @Nullable
  private ScpEntry _parseMessage () throws IOException
  {
    final int ack = _checkAck ();
    if (ack == -1)
    {
      // end of stream
      return null;
    }

    final char type = (char) ack;

    final ScpEntry scpEntry;
    if (type == 'E')
    {
      scpEntry = ScpEntry.newEndOfDirectory ();
      // read and discard the \n
      _readMessageSegment ();
    }
    else
      if (type == 'C' || type == 'D')
      {
        final String mode = _readMessageSegment ();
        final String sizeString = _readMessageSegment ();
        if (sizeString == null)
          return null;

        final long size = Long.parseLong (sizeString);
        final String name = _readMessageSegment ();
        if (name == null)
          return null;

        scpEntry = type == 'C' ? ScpEntry.newFile (name, size, mode) : ScpEntry.newDirectory (name, mode);
      }
      else
      {
        throw new UnsupportedOperationException ("unknown protocol message type " + type);
      }

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("read '" + scpEntry.getAsString () + "'");
    return scpEntry;
  }

  public void putNextEntry (final String name) throws IOException
  {
    putNextEntry (ScpEntry.newDirectory (name));
  }

  public void putNextEntry (final String name, final long size) throws IOException
  {
    putNextEntry (ScpEntry.newFile (name, size));
  }

  public void putNextEntry (@Nonnull final ScpEntry aEntry) throws IOException
  {
    if (aEntry.isEndOfDirectory ())
    {
      while (!m_aEntryStack.isEmpty ())
      {
        final boolean bIsDirectory = m_aEntryStack.peek ().isDirectoryEntry ();
        closeEntry ();
        if (bIsDirectory)
          break;
      }
      return;
    }
    else
      if (!m_aEntryStack.isEmpty ())
      {
        final ICurrentEntry currentEntry = m_aEntryStack.peek ();
        if (!currentEntry.isDirectoryEntry ())
        {
          // auto close previous file entry
          closeEntry ();
        }
      }

    if (aEntry.isDirectory ())
    {
      m_aEntryStack.push (new OutputDirectoryEntry (aEntry));
    }
    else
    {
      m_aEntryStack.push (new EntryOutputStream (aEntry));
    }
  }

  @Nonnull
  private String _readMessageSegment () throws IOException
  {
    final byte [] buffer = new byte [1024];
    int bytesRead = 0;
    for (;; bytesRead++)
    {
      final byte b = (byte) m_aIS.read ();
      if (b == -1)
        return null; // end of stream
      if (b == ' ' || b == '\n')
        break;
      buffer[bytesRead] = b;
    }
    return new String (buffer, 0, bytesRead, StandardCharsets.US_ASCII);
  }

  private void _writeAck () throws IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("writing ack");
    m_aOS.write ((byte) 0);
    m_aOS.flush ();
  }

  private void _writeMessage (final String message) throws IOException
  {
    _writeMessage (message.getBytes (StandardCharsets.US_ASCII));
  }

  private void _writeMessage (final byte... message) throws IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("writing message: '" + new String (message, StandardCharsets.US_ASCII) + "'");
    m_aOS.write (message);
    m_aOS.flush ();
    _checkAck ();
  }

  private interface ICurrentEntry
  {
    void complete () throws IOException;

    boolean isDirectoryEntry ();
  }

  private class InputDirectoryEntry implements ICurrentEntry
  {
    private InputDirectoryEntry () throws IOException
    {
      _writeAck ();
    }

    public void complete () throws IOException
    {
      _writeAck ();
    }

    public boolean isDirectoryEntry ()
    {
      return true;
    }
  }

  private class OutputDirectoryEntry implements ICurrentEntry
  {
    private OutputDirectoryEntry (final ScpEntry entry) throws IOException
    {
      _writeMessage ("D" + entry.getMode () + " 0 " + entry.getName () + "\n");
    }

    public void complete () throws IOException
    {
      _writeMessage ("E\n");
    }

    public boolean isDirectoryEntry ()
    {
      return true;
    }
  }

  private class EntryInputStream extends InputStream implements ICurrentEntry
  {
    private final ScpEntry m_aEntry;
    private long m_nIOCount;
    private boolean m_bClosed;

    public EntryInputStream (final ScpEntry entry) throws IOException
    {
      m_aEntry = entry;
      m_nIOCount = 0L;

      _writeAck ();
      m_bClosed = false;
    }

    @Override
    public void close () throws IOException
    {
      if (!m_bClosed)
      {
        if (!_isComplete ())
        {
          throw new IOException ("stream not finished (" + m_nIOCount + "!=" + m_aEntry.getSize () + ")");
        }
        _writeAck ();
        _checkAck ();
        m_bClosed = true;
      }
    }

    public void complete () throws IOException
    {
      close ();
    }

    private void _increment (@Nonnegative final int nCount)
    {
      m_nIOCount += nCount;
    }

    private boolean _isComplete ()
    {
      return m_nIOCount == m_aEntry.getSize ();
    }

    public boolean isDirectoryEntry ()
    {
      return false;
    }

    @Override
    public int read () throws IOException
    {
      if (_isComplete ())
        return -1;

      _increment (1);
      return m_aIS.read ();
    }

    @Override
    public int read (final byte [] aBuf, final int nOfs, final int nLen) throws IOException
    {
      if (_isComplete ())
        return -1;

      final int nBytesRead = m_aIS.read (aBuf, nOfs, nLen);
      _increment (nBytesRead);
      return nBytesRead;
    }
  }

  private class EntryOutputStream extends OutputStream implements ICurrentEntry
  {
    private final ScpEntry m_aEntry;
    private long m_nIOCount;
    private boolean m_bClosed;

    public EntryOutputStream (final ScpEntry entry) throws IOException
    {
      m_aEntry = entry;
      m_nIOCount = 0L;

      _writeMessage ("C" + entry.getMode () + " " + entry.getSize () + " " + entry.getName () + "\n");
      m_bClosed = false;
    }

    @Override
    public void close () throws IOException
    {
      if (!m_bClosed)
      {
        if (!_isComplete ())
          throw new IOException ("stream not finished (" + m_nIOCount + "!=" + m_aEntry.getSize () + ")");
        _writeMessage ((byte) 0);
        m_bClosed = true;
      }
    }

    public void complete () throws IOException
    {
      close ();
    }

    private void _increment (@Nonnegative final int n) throws IOException
    {
      if (_isComplete ())
        throw new IOException ("too many bytes written for file " + m_aEntry.getName ());
      m_nIOCount += n;
    }

    private boolean _isComplete ()
    {
      return m_nIOCount == m_aEntry.getSize ();
    }

    public boolean isDirectoryEntry ()
    {
      return false;
    }

    @Override
    public void write (final int b) throws IOException
    {
      _increment (1);
      m_aOS.write (b);
    }

    @Override
    public void write (final byte [] aBuf, final int nOfs, final int nLen) throws IOException
    {
      _increment (nLen);
      m_aOS.write (aBuf, nOfs, nLen);
    }
  }
}
