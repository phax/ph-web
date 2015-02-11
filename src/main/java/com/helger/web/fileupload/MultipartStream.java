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
package com.helger.web.fileupload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.charset.CharsetManager;
import com.helger.commons.io.streams.NonBlockingByteArrayOutputStream;
import com.helger.commons.io.streams.StreamUtils;
import com.helger.commons.system.SystemHelper;
import com.helger.web.fileupload.io.ICloseable;
import com.helger.web.fileupload.io.Streams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * <p>
 * Low level API for processing file uploads.
 * <p>
 * This class can be used to process data streams conforming to MIME 'multipart'
 * format as defined in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC
 * 1867</a>. Arbitrarily large amounts of data in the stream can be processed
 * under constant memory usage.
 * <p>
 * The format of the stream is defined in the following way:<br>
 * <code>
 *   multipart-body := preamble 1*encapsulation close-delimiter epilogue<br>
 *   encapsulation := delimiter body CRLF<br>
 *   delimiter := "--" boundary CRLF<br>
 *   close-delimiter := "--" boudary "--"<br>
 *   preamble := &lt;ignore&gt;<br>
 *   epilogue := &lt;ignore&gt;<br>
 *   body := header-part CRLF body-part<br>
 *   header-part := 1*header CRLF<br>
 *   header := header-name ":" header-value<br>
 *   header-name := &lt;printable ascii characters except ":"&gt;<br>
 *   header-value := &lt;any ascii characters except CR & LF&gt;<br>
 *   body-data := &lt;arbitrary data&gt;<br>
 * </code>
 * <p>
 * Note that body-data can contain another mulipart entity. There is limited
 * support for single pass processing of such nested streams. The nested stream
 * is <strong>required</strong> to have a boundary token of the same length as
 * the parent stream (see {@link #setBoundary(byte[])}).
 * <p>
 * Here is an example of usage of this class.<br>
 *
 * <pre>
 * try
 * {
 *   MultipartStream multipartStream = new MultipartStream (input, boundary);
 *   boolean nextPart = multipartStream.skipPreamble ();
 *   OutputStream output;
 *   while (nextPart)
 *   {
 *     String header = multipartStream.readHeaders ();
 *     // process headers
 *     // create some output stream
 *     multipartStream.readBodyData (output);
 *     nextPart = multipartStream.readBoundary ();
 *   }
 * }
 * catch (MultipartStream.MalformedStreamException e)
 * {
 *   // the stream failed to follow required syntax
 * }
 * catch (IOException e)
 * {
 *   // a read or write error occurred
 * }
 * </pre>
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 * @version $Id: MultipartStream.java 735374 2009-01-18 02:18:45Z jochen $
 */
public final class MultipartStream
{
  /**
   * Internal class, which is used to invoke the {@link IProgressListener}.
   */
  public static final class ProgressNotifier
  {
    private static final Logger s_aLogger = LoggerFactory.getLogger (MultipartStream.ProgressNotifier.class);
    /**
     * The listener to invoke.
     */
    private final IProgressListener m_aListener;
    /**
     * Number of expected bytes, if known, or -1.
     */
    private final long m_nContentLength;
    /**
     * Number of bytes, which have been read so far.
     */
    private long m_nBytesRead;
    /**
     * Number of items, which have been read so far.
     */
    private int m_nItems;

    /**
     * Creates a new instance with the given listener and content length.
     *
     * @param aListener
     *        The listener to invoke.
     * @param nContentLength
     *        The expected content length.
     */
    ProgressNotifier (@Nullable final IProgressListener aListener, final long nContentLength)
    {
      if (aListener != null && s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("setting listener " + aListener.getClass ().getName ());
      m_aListener = aListener;
      m_nContentLength = nContentLength;
    }

    /**
     * Called for notifying the listener.
     */
    private void _notifyListener ()
    {
      if (m_aListener != null)
        m_aListener.update (m_nBytesRead, m_nContentLength, m_nItems);
    }

    /**
     * Called to indicate that bytes have been read.
     *
     * @param nBytes
     *        Number of bytes, which have been read.
     */
    void noteBytesRead (@Nonnegative final int nBytes)
    {
      ValueEnforcer.isGE0 (nBytes, "Bytes");
      /*
       * Indicates, that the given number of bytes have been read from the input
       * stream.
       */
      m_nBytesRead += nBytes;
      _notifyListener ();
    }

    /**
     * Called to indicate, that a new file item has been detected.
     */
    void noteItem ()
    {
      ++m_nItems;
      _notifyListener ();
    }
  }

  // ----------------------------------------------------- Manifest constants

  /**
   * The Carriage Return ASCII character value.
   */
  public static final byte CR = 0x0D;

  /**
   * The Line Feed ASCII character value.
   */
  public static final byte LF = 0x0A;

  /**
   * The dash (-) ASCII character value.
   */
  public static final byte DASH = 0x2D;

  /**
   * The maximum length of <code>header-part</code> that will be processed (10
   * kilobytes = 10240 bytes.).
   */
  public static final int HEADER_PART_SIZE_MAX = 10240;

  /**
   * The default length of the buffer used for processing a request.
   */
  private static final int DEFAULT_BUFSIZE = 4096;

  /**
   * A byte sequence that marks the end of <code>header-part</code> (
   * <code>CRLFCRLF</code>).
   */
  private static final byte [] HEADER_SEPARATOR = { CR, LF, CR, LF };

  /**
   * A byte sequence that that follows a delimiter that will be followed by an
   * encapsulation (<code>CRLF</code>).
   */
  private static final byte [] FIELD_SEPARATOR = { CR, LF };

  /**
   * A byte sequence that that follows a delimiter of the last encapsulation in
   * the stream (<code>--</code>).
   */
  private static final byte [] STREAM_TERMINATOR = { DASH, DASH };

  /**
   * A byte sequence that precedes a boundary (<code>CRLF--</code>).
   */
  private static final byte [] BOUNDARY_PREFIX = { CR, LF, DASH, DASH };

  // ----------------------------------------------------------- Data members

  /**
   * The input stream from which data is read.
   */
  private final InputStream m_aInput;

  /**
   * The length of the boundary token plus the leading <code>CRLF--</code>.
   */
  private int m_nBoundaryLength;

  /**
   * The amount of data, in bytes, that must be kept in the buffer in order to
   * detect delimiters reliably.
   */
  private final int m_nKeepRegion;

  /**
   * The byte sequence that partitions the stream.
   */
  private final byte [] m_aBoundary;

  /**
   * The length of the buffer used for processing the request.
   */
  private final int m_nBufSize;

  /**
   * The buffer used for processing the request.
   */
  private final byte [] m_aBuffer;

  /**
   * The index of first valid character in the buffer. <br>
   * 0 <= head < bufSize
   */
  private int m_nHead;

  /**
   * The index of last valid character in the buffer + 1. <br>
   * 0 <= tail <= bufSize
   */
  private int m_nTail;

  /**
   * The content encoding to use when reading headers.
   */
  private String m_sHeaderEncoding;

  /**
   * The progress notifier, if any, or null.
   */
  private final ProgressNotifier m_aNotifier;

  // ----------------------------------------------------------- Constructors

  /**
   * <p>
   * Constructs a <code>MultipartStream</code> with a custom size buffer.
   * <p>
   * Note that the buffer must be at least big enough to contain the boundary
   * string, plus 4 characters for CR/LF and double dash, plus at least one byte
   * of data. Too small a buffer size setting will degrade performance.
   *
   * @param aIS
   *        The <code>InputStream</code> to serve as a data source.
   * @param aBoundary
   *        The token used for dividing the stream into
   *        <code>encapsulations</code>.
   * @param nBufSize
   *        The size of the buffer to be used, in bytes.
   * @param aNotifier
   *        The notifier, which is used for calling the progress listener, if
   *        any.
   * @see #MultipartStream(InputStream, byte[],
   *      MultipartStream.ProgressNotifier)
   */
  MultipartStream (final InputStream aIS, final byte [] aBoundary, final int nBufSize, final ProgressNotifier aNotifier)
  {
    m_aInput = aIS;
    m_nBufSize = nBufSize;
    m_aBuffer = new byte [nBufSize];
    m_aNotifier = aNotifier;

    // We prepend CR/LF to the boundary to chop trailng CR/LF from
    // body-data tokens.
    m_aBoundary = new byte [aBoundary.length + BOUNDARY_PREFIX.length];
    m_nBoundaryLength = aBoundary.length + BOUNDARY_PREFIX.length;
    m_nKeepRegion = m_aBoundary.length;
    System.arraycopy (BOUNDARY_PREFIX, 0, m_aBoundary, 0, BOUNDARY_PREFIX.length);
    System.arraycopy (aBoundary, 0, m_aBoundary, BOUNDARY_PREFIX.length, aBoundary.length);

    m_nHead = 0;
    m_nTail = 0;
  }

  /**
   * <p>
   * Constructs a <code>MultipartStream</code> with a default size buffer.
   *
   * @param aIS
   *        The <code>InputStream</code> to serve as a data source.
   * @param aBoundary
   *        The token used for dividing the stream into
   *        <code>encapsulations</code>.
   * @param aNotifier
   *        An object for calling the progress listener, if any.
   * @see #MultipartStream(InputStream, byte[], int,
   *      MultipartStream.ProgressNotifier)
   */
  MultipartStream (final InputStream aIS, final byte [] aBoundary, final ProgressNotifier aNotifier)
  {
    this (aIS, aBoundary, DEFAULT_BUFSIZE, aNotifier);
  }

  // --------------------------------------------------------- Public methods

  /**
   * Retrieves the character encoding used when reading the headers of an
   * individual part. When not specified, or <code>null</code>, the platform
   * default encoding is used.
   *
   * @return The encoding used to read part headers.
   */
  @Nullable
  public String getHeaderEncoding ()
  {
    return m_sHeaderEncoding;
  }

  /**
   * Specifies the character encoding to be used when reading the headers of
   * individual parts. When not specified, or <code>null</code>, the platform
   * default encoding is used.
   *
   * @param sHeaderEncoding
   *        The encoding used to read part headers.
   */
  public void setHeaderEncoding (@Nullable final String sHeaderEncoding)
  {
    m_sHeaderEncoding = sHeaderEncoding;
  }

  /**
   * Reads a byte from the <code>buffer</code>, and refills it as necessary.
   *
   * @return The next byte from the input stream.
   * @throws IOException
   *         if there is no more data available.
   */
  public byte readByte () throws IOException
  {
    // Buffer depleted ?
    if (m_nHead == m_nTail)
    {
      m_nHead = 0;
      // Refill.
      m_nTail = m_aInput.read (m_aBuffer, m_nHead, m_nBufSize);
      if (m_nTail == -1)
      {
        // No more data available.
        throw new IOException ("No more data is available");
      }
      if (m_aNotifier != null)
      {
        m_aNotifier.noteBytesRead (m_nTail);
      }
    }
    return m_aBuffer[m_nHead++];
  }

  /**
   * Skips a <code>boundary</code> token, and checks whether more
   * <code>encapsulations</code> are contained in the stream.
   *
   * @return <code>true</code> if there are more encapsulations in this stream;
   *         <code>false</code> otherwise.
   * @throws MalformedStreamException
   *         if the stream ends unexpectedly or fails to follow required syntax.
   */
  public boolean readBoundary () throws MalformedStreamException
  {
    final byte [] marker = new byte [2];
    boolean bNextChunk = false;

    m_nHead += m_nBoundaryLength;
    try
    {
      marker[0] = readByte ();
      if (marker[0] == LF)
      {
        // Work around IE5 Mac bug with input type=image.
        // Because the boundary delimiter, not including the trailing
        // CRLF, must not appear within any file (RFC 2046, section
        // 5.1.1), we know the missing CR is due to a buggy browser
        // rather than a file containing something similar to a
        // boundary.
        return true;
      }

      marker[1] = readByte ();
      if (arrayequals (marker, STREAM_TERMINATOR, 2))
      {
        bNextChunk = false;
      }
      else
        if (arrayequals (marker, FIELD_SEPARATOR, 2))
        {
          bNextChunk = true;
        }
        else
        {
          throw new MalformedStreamException ("Unexpected characters follow a boundary");
        }
    }
    catch (final IOException ex)
    {
      throw new MalformedStreamException ("Stream ended unexpectedly", ex);
    }
    return bNextChunk;
  }

  /**
   * <p>
   * Changes the boundary token used for partitioning the stream.
   * <p>
   * This method allows single pass processing of nested multipart streams.
   * <p>
   * The boundary token of the nested stream is <code>required</code> to be of
   * the same length as the boundary token in parent stream.
   * <p>
   * Restoring the parent stream boundary token after processing of a nested
   * stream is left to the application.
   *
   * @param aBoundary
   *        The boundary to be used for parsing of the nested stream.
   * @throws IllegalBoundaryException
   *         if the <code>boundary</code> has a different length than the one
   *         being currently parsed.
   */
  public void setBoundary (@Nonnull final byte [] aBoundary) throws IllegalBoundaryException
  {
    ValueEnforcer.notNull (aBoundary, "Boundary");
    if (aBoundary.length != m_nBoundaryLength - BOUNDARY_PREFIX.length)
      throw new IllegalBoundaryException ("The length of a boundary token can not be changed");
    System.arraycopy (aBoundary, 0, m_aBoundary, BOUNDARY_PREFIX.length, aBoundary.length);
  }

  /**
   * <p>
   * Reads the <code>header-part</code> of the current
   * <code>encapsulation</code>.
   * <p>
   * Headers are returned verbatim to the input stream, including the trailing
   * <code>CRLF</code> marker. Parsing is left to the application.
   * <p>
   * <strong>TODO</strong> allow limiting maximum header size to protect against
   * abuse.
   *
   * @return The <code>header-part</code> of the current encapsulation.
   * @throws MalformedStreamException
   *         if the stream ends unexpectedly.
   */
  public String readHeaders () throws MalformedStreamException
  {
    // to support multi-byte characters
    final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ();
    try
    {
      int nHeaderSepIndex = 0;
      int nSize = 0;
      while (nHeaderSepIndex < HEADER_SEPARATOR.length)
      {
        byte b;
        try
        {
          b = readByte ();
        }
        catch (final IOException e)
        {
          throw new MalformedStreamException ("Stream ended unexpectedly", e);
        }
        if (++nSize > HEADER_PART_SIZE_MAX)
        {
          throw new MalformedStreamException ("Header section has more than " +
                                              HEADER_PART_SIZE_MAX +
                                              " bytes (maybe it is not properly terminated)");
        }
        if (b == HEADER_SEPARATOR[nHeaderSepIndex])
          nHeaderSepIndex++;
        else
          nHeaderSepIndex = 0;
        aBAOS.write (b);
      }

      String sHeaders;
      if (m_sHeaderEncoding != null)
        sHeaders = aBAOS.getAsString (CharsetManager.getCharsetFromName (m_sHeaderEncoding));
      else
        sHeaders = aBAOS.getAsString (SystemHelper.getSystemCharset ());
      return sHeaders;
    }
    finally
    {
      StreamUtils.close (aBAOS);
    }
  }

  /**
   * <p>
   * Reads <code>body-data</code> from the current <code>encapsulation</code>
   * and writes its contents into the output <code>Stream</code>.
   * <p>
   * Arbitrary large amounts of data can be processed by this method using a
   * constant size buffer. (see
   * {@link #MultipartStream(InputStream,byte[],int, MultipartStream.ProgressNotifier)
   * constructor}).
   *
   * @param aOS
   *        The <code>Stream</code> to write data into. May be null, in which
   *        case this method is equivalent to {@link #discardBodyData()}.
   * @return the amount of data written.
   * @throws MalformedStreamException
   *         if the stream ends unexpectedly.
   * @throws IOException
   *         if an i/o error occurs.
   */
  @SuppressWarnings ("javadoc")
  public int readBodyData (final OutputStream aOS) throws MalformedStreamException, IOException
  {
    final InputStream istream = newInputStream ();
    return (int) Streams.copy (istream, aOS, false);
  }

  /**
   * Creates a new {@link ItemInputStream}.
   *
   * @return A new instance of {@link ItemInputStream}.
   */
  @Nonnull
  ItemInputStream newInputStream ()
  {
    return new ItemInputStream ();
  }

  /**
   * <p>
   * Reads <code>body-data</code> from the current <code>encapsulation</code>
   * and discards it.
   * <p>
   * Use this method to skip encapsulations you don't need or don't understand.
   *
   * @return The amount of data discarded.
   * @throws MalformedStreamException
   *         if the stream ends unexpectedly.
   * @throws IOException
   *         if an i/o error occurs.
   */
  public int discardBodyData () throws MalformedStreamException, IOException
  {
    return readBodyData (null);
  }

  /**
   * Finds the beginning of the first <code>encapsulation</code>.
   *
   * @return <code>true</code> if an <code>encapsulation</code> was found in the
   *         stream.
   * @throws IOException
   *         if an i/o error occurs.
   */
  public boolean skipPreamble () throws IOException
  {
    // First delimiter may be not preceeded with a CRLF.
    System.arraycopy (m_aBoundary, 2, m_aBoundary, 0, m_aBoundary.length - 2);
    m_nBoundaryLength = m_aBoundary.length - 2;
    try
    {
      // Discard all data up to the delimiter.
      discardBodyData ();

      // Read boundary - if succeded, the stream contains an
      // encapsulation.
      return readBoundary ();
    }
    catch (final MalformedStreamException e)
    {
      return false;
    }
    finally
    {
      // Restore delimiter.
      System.arraycopy (m_aBoundary, 0, m_aBoundary, 2, m_aBoundary.length - 2);
      m_nBoundaryLength = m_aBoundary.length;
      m_aBoundary[0] = CR;
      m_aBoundary[1] = LF;
    }
  }

  /**
   * Compares <code>count</code> first bytes in the arrays <code>a</code> and
   * <code>b</code>.
   *
   * @param a
   *        The first array to compare.
   * @param b
   *        The second array to compare.
   * @param count
   *        How many bytes should be compared.
   * @return <code>true</code> if <code>count</code> first bytes in arrays
   *         <code>a</code> and <code>b</code> are equal.
   */
  public static boolean arrayequals (final byte [] a, final byte [] b, final int count)
  {
    for (int i = 0; i < count; i++)
    {
      if (a[i] != b[i])
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Searches for a byte of specified value in the <code>buffer</code>, starting
   * at the specified <code>position</code>.
   *
   * @param value
   *        The value to find.
   * @param pos
   *        The starting position for searching.
   * @return The position of byte found, counting from beginning of the
   *         <code>buffer</code>, or <code>-1</code> if not found.
   */
  protected int findByte (final byte value, final int pos)
  {
    for (int i = pos; i < m_nTail; i++)
    {
      if (m_aBuffer[i] == value)
      {
        return i;
      }
    }

    return -1;
  }

  /**
   * Searches for the <code>boundary</code> in the <code>buffer</code> region
   * delimited by <code>head</code> and <code>tail</code>.
   *
   * @return The position of the boundary found, counting from the beginning of
   *         the <code>buffer</code>, or <code>-1</code> if not found.
   */
  protected int findSeparator ()
  {
    int first;
    int match = 0;
    final int maxpos = m_nTail - m_nBoundaryLength;
    for (first = m_nHead; (first <= maxpos) && (match != m_nBoundaryLength); first++)
    {
      first = findByte (m_aBoundary[0], first);
      if (first == -1 || (first > maxpos))
      {
        return -1;
      }
      for (match = 1; match < m_nBoundaryLength; match++)
      {
        if (m_aBuffer[first + match] != m_aBoundary[match])
        {
          break;
        }
      }
    }
    if (match == m_nBoundaryLength)
    {
      return first - 1;
    }
    return -1;
  }

  /**
   * Thrown to indicate that the input stream fails to follow the required
   * syntax.
   */
  public static final class MalformedStreamException extends IOException
  {
    /**
     * Constructs a <code>MalformedStreamException</code> with no detail
     * message.
     */
    public MalformedStreamException ()
    {
      super ();
    }

    /**
     * Constructs an <code>MalformedStreamException</code> with the specified
     * detail message.
     *
     * @param message
     *        The detail message.
     */
    public MalformedStreamException (final String message)
    {
      super (message);
    }

    /**
     * Constructs an <code>MalformedStreamException</code> with the specified
     * detail message.
     *
     * @param message
     *        The detail message.
     * @param aCause
     *        The cause of the exception
     */
    public MalformedStreamException (final String message, final Throwable aCause)
    {
      super (message, aCause);
    }
  }

  /**
   * Thrown upon attempt of setting an invalid boundary token.
   */
  public static final class IllegalBoundaryException extends IOException
  {
    /**
     * Constructs an <code>IllegalBoundaryException</code> with no detail
     * message.
     */
    public IllegalBoundaryException ()
    {
      super ();
    }

    /**
     * Constructs an <code>IllegalBoundaryException</code> with the specified
     * detail message.
     *
     * @param message
     *        The detail message.
     */
    public IllegalBoundaryException (final String message)
    {
      super (message);
    }
  }

  /**
   * An {@link InputStream} for reading an items contents.
   */
  public final class ItemInputStream extends InputStream implements ICloseable
  {
    /**
     * The number of bytes, which have been read so far.
     */
    private long m_nTotal;
    /**
     * The number of bytes, which must be hold, because they might be a part of
     * the boundary.
     */
    private int m_nPad;
    /**
     * The current offset in the buffer.
     */
    private int m_nPos;
    /**
     * Whether the stream is already closed.
     */
    private boolean m_bClosed;

    /**
     * Creates a new instance.
     */
    ItemInputStream ()
    {
      _findSeparator ();
    }

    /**
     * Called for finding the separator.
     */
    private void _findSeparator ()
    {
      m_nPos = MultipartStream.this.findSeparator ();
      if (m_nPos == -1)
      {
        if (m_nTail - m_nHead > m_nKeepRegion)
          m_nPad = m_nKeepRegion;
        else
          m_nPad = m_nTail - m_nHead;
      }
    }

    /**
     * Returns the number of bytes, which have been read by the stream.
     *
     * @return Number of bytes, which have been read so far.
     */
    public long getBytesRead ()
    {
      return m_nTotal;
    }

    /**
     * Returns the number of bytes, which are currently available, without
     * blocking.
     *
     * @throws IOException
     *         An I/O error occurs.
     * @return Number of bytes in the buffer.
     */
    @Override
    public int available () throws IOException
    {
      if (m_nPos == -1)
        return m_nTail - m_nHead - m_nPad;
      return m_nPos - m_nHead;
    }

    /**
     * Offset when converting negative bytes to integers.
     */
    private static final int BYTE_POSITIVE_OFFSET = 256;

    /**
     * Returns the next byte in the stream.
     *
     * @return The next byte in the stream, as a non-negative integer, or -1 for
     *         EOF.
     * @throws IOException
     *         An I/O error occurred.
     */
    @Override
    public int read () throws IOException
    {
      if (m_bClosed)
        throw new IFileItemStream.ItemSkippedException ();

      if (available () == 0)
        if (_makeAvailable () == 0)
          return -1;

      ++m_nTotal;
      final int b = m_aBuffer[m_nHead++];
      if (b >= 0)
        return b;

      return b + BYTE_POSITIVE_OFFSET;
    }

    /**
     * Reads bytes into the given buffer.
     *
     * @param b
     *        The destination buffer, where to write to.
     * @param off
     *        Offset of the first byte in the buffer.
     * @param len
     *        Maximum number of bytes to read.
     * @return Number of bytes, which have been actually read, or -1 for EOF.
     * @throws IOException
     *         An I/O error occurred.
     */
    @Override
    public int read (final byte [] b, final int off, final int len) throws IOException
    {
      if (m_bClosed)
        throw new IFileItemStream.ItemSkippedException ();

      if (len == 0)
        return 0;

      int res = available ();
      if (res == 0)
      {
        res = _makeAvailable ();
        if (res == 0)
          return -1;
      }
      res = Math.min (res, len);
      System.arraycopy (m_aBuffer, m_nHead, b, off, res);
      m_nHead += res;
      m_nTotal += res;
      return res;
    }

    /**
     * Closes the input stream.
     *
     * @throws IOException
     *         An I/O error occurred.
     */
    @Override
    public void close () throws IOException
    {
      close (false);
    }

    /**
     * Closes the input stream.
     *
     * @param pCloseUnderlying
     *        Whether to close the underlying stream (hard close)
     * @throws IOException
     *         An I/O error occurred.
     */
    @SuppressFBWarnings ("SR_NOT_CHECKED")
    public void close (final boolean pCloseUnderlying) throws IOException
    {
      if (m_bClosed)
        return;

      if (pCloseUnderlying)
      {
        m_bClosed = true;
        m_aInput.close ();
      }
      else
      {
        while (true)
        {
          int av = available ();
          if (av == 0)
          {
            av = _makeAvailable ();
            if (av == 0)
              break;
          }
          skip (av);
        }
      }
      m_bClosed = true;
    }

    /**
     * Skips the given number of bytes.
     *
     * @param bytes
     *        Number of bytes to skip.
     * @return The number of bytes, which have actually been skipped.
     * @throws IOException
     *         An I/O error occurred.
     */
    @Override
    public long skip (final long bytes) throws IOException
    {
      if (m_bClosed)
        throw new IFileItemStream.ItemSkippedException ();

      int av = available ();
      if (av == 0)
      {
        av = _makeAvailable ();
        if (av == 0)
          return 0;
      }
      final long res = Math.min (av, bytes);
      m_nHead += res;
      return res;
    }

    /**
     * Attempts to read more data.
     *
     * @return Number of available bytes
     * @throws IOException
     *         An I/O error occurred.
     */
    private int _makeAvailable () throws IOException
    {
      if (m_nPos != -1)
        return 0;

      // Move the data to the beginning of the buffer.
      m_nTotal += m_nTail - m_nHead - m_nPad;
      System.arraycopy (m_aBuffer, m_nTail - m_nPad, m_aBuffer, 0, m_nPad);

      // Refill buffer with new data.
      m_nHead = 0;
      m_nTail = m_nPad;

      while (true)
      {
        final int nBytesRead = m_aInput.read (m_aBuffer, m_nTail, m_nBufSize - m_nTail);
        if (nBytesRead == -1)
        {
          // The last pad amount is left in the buffer.
          // Boundary can't be in there so signal an error
          // condition.
          throw new MalformedStreamException ("Stream ended unexpectedly");
        }
        if (m_aNotifier != null)
        {
          m_aNotifier.noteBytesRead (nBytesRead);
        }
        m_nTail += nBytesRead;

        _findSeparator ();
        final int av = available ();

        if (av > 0 || m_nPos != -1)
          return av;
      }
    }

    /**
     * Returns, whether the stream is closed.
     *
     * @return True, if the stream is closed, otherwise false.
     */
    public boolean isClosed ()
    {
      return m_bClosed;
    }
  }
}
