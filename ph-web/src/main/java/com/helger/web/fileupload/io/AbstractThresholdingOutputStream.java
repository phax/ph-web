/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;

/**
 * An output stream which triggers an event when a specified number of bytes of
 * data have been written to it. The event can be used, for example, to throw an
 * exception if a maximum has been reached, or to switch the underlying stream
 * type when the threshold is exceeded.
 * <p>
 * This class overrides all <code>OutputStream</code> methods. However, these
 * overrides ultimately call the corresponding methods in the underlying output
 * stream implementation.
 * <p>
 * NOTE: This implementation may trigger the event <em>before</em> the threshold
 * is actually reached, since it triggers when a pending write operation would
 * cause the threshold to be exceeded.
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @version $Id: ThresholdingOutputStream.java 736890 2009-01-23 02:02:22Z
 *          niallp $
 */
public abstract class AbstractThresholdingOutputStream extends OutputStream
{
  /**
   * The threshold at which the event will be triggered.
   */
  private final int m_nThreshold;

  /**
   * The number of bytes written to the output stream.
   */
  private long m_nWritten;

  /**
   * Whether or not the configured threshold has been exceeded.
   */
  private boolean m_bThresholdExceeded;

  /**
   * Constructs an instance of this class which will trigger an event at the
   * specified threshold.
   *
   * @param nThreshold
   *        The number of bytes at which to trigger an event.
   */
  public AbstractThresholdingOutputStream (@Nonnegative final int nThreshold)
  {
    m_nThreshold = ValueEnforcer.isGT0 (nThreshold, "Threshold");
  }

  /**
   * Writes the specified byte to this output stream.
   *
   * @param b
   *        The byte to be written.
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  public void write (final int b) throws IOException
  {
    checkThreshold (1);
    getStream ().write (b);
    m_nWritten++;
  }

  /**
   * Writes <code>b.length</code> bytes from the specified byte array to this
   * output stream.
   *
   * @param b
   *        The array of bytes to be written.
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  public void write (@Nonnull final byte [] b) throws IOException
  {
    checkThreshold (b.length);
    getStream ().write (b);
    m_nWritten += b.length;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at
   * offset <code>off</code> to this output stream.
   *
   * @param b
   *        The byte array from which the data will be written.
   * @param off
   *        The start offset in the byte array.
   * @param len
   *        The number of bytes to write.
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  public void write (@Nonnull final byte [] b,
                     @Nonnegative final int off,
                     @Nonnegative final int len) throws IOException
  {
    checkThreshold (len);
    getStream ().write (b, off, len);
    m_nWritten += len;
  }

  /**
   * Flushes this output stream and forces any buffered output bytes to be
   * written out.
   *
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  public void flush () throws IOException
  {
    getStream ().flush ();
  }

  /**
   * Closes this output stream and releases any system resources associated with
   * this stream.
   *
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  public void close () throws IOException
  {
    try
    {
      flush ();
    }
    catch (final IOException ignored)
    {
      // ignore
    }
    getStream ().close ();
  }

  /**
   * Returns the threshold, in bytes, at which an event will be triggered.
   *
   * @return The threshold point, in bytes.
   */
  @Nonnegative
  public int getThreshold ()
  {
    return m_nThreshold;
  }

  /**
   * Returns the number of bytes that have been written to this output stream.
   *
   * @return The number of bytes written.
   */
  public long getByteCount ()
  {
    return m_nWritten;
  }

  /**
   * Determines whether or not the configured threshold has been exceeded for
   * this output stream.
   *
   * @return <code>true</code> if the threshold has been reached;
   *         <code>false</code> otherwise.
   */
  public boolean isThresholdExceeded ()
  {
    return m_nWritten > m_nThreshold;
  }

  /**
   * Checks to see if writing the specified number of bytes would cause the
   * configured threshold to be exceeded. If so, triggers an event to allow a
   * concrete implementation to take action on
   *
   * @param nCount
   *        The number of bytes about to be written to the underlying output
   *        stream.
   * @exception IOException
   *            if an error occurs.
   */
  protected void checkThreshold (final int nCount) throws IOException
  {
    if (!m_bThresholdExceeded && (m_nWritten + nCount > m_nThreshold))
    {
      m_bThresholdExceeded = true;
      onThresholdReached ();
    }
  }

  /**
   * Resets the byteCount to zero. You can call this from
   * {@link #onThresholdReached()} if you want the event to be triggered again.
   */
  protected void resetByteCount ()
  {
    m_bThresholdExceeded = false;
    m_nWritten = 0;
  }

  /**
   * Returns the underlying output stream, to which the corresponding
   * <code>OutputStream</code> methods in this class will ultimately delegate.
   *
   * @return The underlying output stream.
   * @exception IOException
   *            if an error occurs.
   */
  @Nonnull
  protected abstract OutputStream getStream () throws IOException;

  /**
   * Indicates that the configured threshold has been reached, and that a
   * subclass should take whatever action necessary on this event. This may
   * include changing the underlying output stream.
   *
   * @exception IOException
   *            if an error occurs.
   */
  protected abstract void onThresholdReached () throws IOException;
}
