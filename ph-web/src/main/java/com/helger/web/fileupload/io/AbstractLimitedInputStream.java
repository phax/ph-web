/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.web.ICloseable;

/**
 * An input stream, which limits its data size. This stream is used, if the
 * content length is unknown.
 */
public abstract class AbstractLimitedInputStream extends FilterInputStream implements ICloseable
{
  /**
   * The maximum size of an item, in bytes.
   */
  private final long m_nSizeMax;
  /**
   * The current number of bytes.
   */
  private long m_nCount;
  /**
   * Whether this stream is already closed.
   */
  private boolean m_bClosed;

  /**
   * Creates a new instance.
   *
   * @param aIS
   *        The input stream, which shall be limited.
   * @param nSizeMax
   *        The limit; no more than this number of bytes shall be returned by
   *        the source stream.
   */
  public AbstractLimitedInputStream (@Nonnull final InputStream aIS, @Nonnegative final long nSizeMax)
  {
    super (aIS);
    m_nSizeMax = ValueEnforcer.isGE0 (nSizeMax, "SizeMax");
  }

  /**
   * Called to indicate, that the input streams limit has been exceeded.
   *
   * @param nSizeMax
   *        The input streams limit, in bytes.
   * @param nCount
   *        The actual number of bytes.
   * @throws IOException
   *         The called method is expected to raise an IOException.
   */
  protected abstract void onLimitExceeded (@Nonnegative long nSizeMax, @Nonnegative long nCount) throws IOException;

  /**
   * Called to check, whether the input streams limit is reached.
   *
   * @throws IOException
   *         The given limit is exceeded.
   */
  private void _checkLimit () throws IOException
  {
    if (m_nCount > m_nSizeMax)
      onLimitExceeded (m_nSizeMax, m_nCount);
  }

  /**
   * Reads the next byte of data from this input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream has
   * been reached, the value <code>-1</code> is returned. This method blocks
   * until input data is available, the end of the stream is detected, or an
   * exception is thrown.
   * <p>
   * This method simply performs <code>in.read()</code> and returns the result.
   *
   * @return the next byte of data, or <code>-1</code> if the end of the stream
   *         is reached.
   * @exception IOException
   *            if an I/O error occurs.
   * @see java.io.FilterInputStream
   */
  @Override
  public int read () throws IOException
  {
    final int res = super.read ();
    if (res != -1)
    {
      m_nCount++;
      _checkLimit ();
    }
    return res;
  }

  /**
   * Reads up to <code>len</code> bytes of data from this input stream into an
   * array of bytes. If <code>len</code> is not zero, the method blocks until
   * some input is available; otherwise, no bytes are read and <code>0</code> is
   * returned.
   * <p>
   * This method simply performs <code>in.read(b, off, len)</code> and returns
   * the result.
   *
   * @param b
   *        the buffer into which the data is read.
   * @param nOfs
   *        The start offset in the destination array <code>b</code>.
   * @param nLen
   *        the maximum number of bytes read.
   * @return the total number of bytes read into the buffer, or <code>-1</code>
   *         if there is no more data because the end of the stream has been
   *         reached.
   * @exception IndexOutOfBoundsException
   *            If <code>off</code> is negative, <code>len</code> is negative,
   *            or <code>len</code> is greater than <code>b.length - off</code>
   * @exception IOException
   *            if an I/O error occurs.
   * @see java.io.FilterInputStream
   */
  @Override
  public int read (@Nonnull final byte [] b,
                   @Nonnegative final int nOfs,
                   @Nonnegative final int nLen) throws IOException
  {
    ValueEnforcer.isArrayOfsLen (b, nOfs, nLen);
    final int res = super.read (b, nOfs, nLen);
    if (res > 0)
    {
      m_nCount += res;
      _checkLimit ();
    }
    return res;
  }

  /**
   * Returns, whether this stream is already closed.
   *
   * @return True, if the stream is closed, otherwise false.
   * @throws IOException
   *         An I/O error occurred.
   */
  public boolean isClosed () throws IOException
  {
    return m_bClosed;
  }

  /**
   * Closes this input stream and releases any system resources associated with
   * the stream. This method simply performs <code>in.close()</code>.
   *
   * @exception IOException
   *            if an I/O error occurs.
   * @see java.io.FilterInputStream
   */
  @Override
  public void close () throws IOException
  {
    m_bClosed = true;
    super.close ();
  }
}
