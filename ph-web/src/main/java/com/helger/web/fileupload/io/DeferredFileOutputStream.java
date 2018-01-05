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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.io.stream.StreamHelper;

/**
 * An output stream which will retain data in memory until a specified threshold
 * is reached, and only then commit it to disk. If the stream is closed before
 * the threshold is reached, the data will not be written to disk at all.
 * <p>
 * This class originated in FileUpload processing. In this use case, you do not
 * know in advance the size of the file being uploaded. If the file is small you
 * want to store it in memory (for speed), but if the file is large you want to
 * store it to file (to avoid memory issues).
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author gaxzerow
 * @version $Id: DeferredFileOutputStream.java 736890 2009-01-23 02:02:22Z
 *          niallp $
 */
public class DeferredFileOutputStream extends AbstractThresholdingOutputStream
{
  /**
   * The output stream to which data will be written prior to the threshold
   * being reached.
   */
  private NonBlockingByteArrayOutputStream m_aMemoryOutputStream;

  /**
   * The output stream to which data will be written at any given time. This
   * will always be one of <code>memoryOutputStream</code> or
   * <code>diskOutputStream</code>.
   */
  private OutputStream m_aCurrentOutputStream;

  /**
   * The file to which output will be directed if the threshold is exceeded.
   */
  private final File m_aOutputFile;

  /**
   * True when close() has been called successfully.
   */
  private boolean m_bClosed = false;

  /**
   * Constructs an instance of this class which will trigger an event at the
   * specified threshold, and save data to a file beyond that point.
   *
   * @param nThreshold
   *        The number of bytes at which to trigger an event.
   * @param aOutputFile
   *        The file to which data is saved beyond the threshold.
   */
  public DeferredFileOutputStream (@Nonnegative final int nThreshold, @Nonnull final File aOutputFile)
  {
    super (nThreshold);
    m_aOutputFile = ValueEnforcer.notNull (aOutputFile, "OutputFile");

    m_aMemoryOutputStream = new NonBlockingByteArrayOutputStream ();
    m_aCurrentOutputStream = m_aMemoryOutputStream;
  }

  /**
   * Returns the current output stream. This may be memory based or disk based,
   * depending on the current state with respect to the threshold.
   *
   * @return The underlying output stream.
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  protected OutputStream getStream () throws IOException
  {
    return m_aCurrentOutputStream;
  }

  /**
   * Switches the underlying output stream from a memory based stream to one
   * that is backed by disk. This is the point at which we realise that too much
   * data is being written to keep in memory, so we elect to switch to
   * disk-based storage.
   *
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  protected void onThresholdReached () throws IOException
  {
    FileOutputStream aFOS = null;
    try
    {
      aFOS = new FileOutputStream (m_aOutputFile);
      m_aMemoryOutputStream.writeTo (aFOS);
      m_aCurrentOutputStream = aFOS;
      m_aMemoryOutputStream = null;
    }
    catch (final IOException ex)
    {
      StreamHelper.close (aFOS);
      throw ex;
    }
  }

  /**
   * Determines whether or not the data for this output stream has been retained
   * in memory.
   *
   * @return <code>true</code> if the data is available in memory;
   *         <code>false</code> otherwise.
   */
  public boolean isInMemory ()
  {
    return m_aMemoryOutputStream != null;
  }

  /**
   * Returns the data for this output stream as an array of bytes, assuming that
   * the data has been retained in memory. If the data was written to disk, this
   * method returns <code>null</code>.
   *
   * @return The data for this output stream, or <code>null</code> if no such
   *         data is available.
   */
  @Nullable
  @ReturnsMutableCopy
  public byte [] getData ()
  {
    if (m_aMemoryOutputStream != null)
      return m_aMemoryOutputStream.toByteArray ();
    return null;
  }

  /**
   * Returns the length of the data for this output stream as number of bytes,
   * assuming that the data has been retained in memory. If the data was written
   * to disk, this method returns <code>0</code>.
   *
   * @return The length of the data for this output stream, or <code>0</code> if
   *         no such data is available.
   */
  @Nonnegative
  public int getDataLength ()
  {
    if (m_aMemoryOutputStream != null)
      return m_aMemoryOutputStream.size ();
    return 0;
  }

  /**
   * Returns either the output file specified in the constructor or the
   * temporary file created or null.
   * <p>
   * If the constructor specifying the file is used then it returns that same
   * output file, even when threshold has not been reached.
   * <p>
   * If constructor specifying a temporary file prefix/suffix is used then the
   * temporary file created once the threshold is reached is returned If the
   * threshold was not reached then <code>null</code> is returned.
   *
   * @return The file for this output stream, or <code>null</code> if no such
   *         file exists.
   */
  @Nonnull
  public File getFile ()
  {
    return m_aOutputFile;
  }

  /**
   * Closes underlying output stream, and mark this as closed
   *
   * @exception IOException
   *            if an error occurs.
   */
  @Override
  public void close () throws IOException
  {
    super.close ();
    m_bClosed = true;
  }

  /**
   * Writes the data from this output stream to the specified output stream,
   * after it has been closed.
   *
   * @param aOS
   *        output stream to write to.
   * @exception IOException
   *            if this stream is not yet closed or an error occurs.
   */
  public void writeTo (@Nonnull @WillNotClose final OutputStream aOS) throws IOException
  {
    // we may only need to check if this is closed if we are working with a file
    // but we should force the habit of closing whether we are working with
    // a file or memory.
    if (!m_bClosed)
      throw new IOException ("Stream not closed");

    if (isInMemory ())
    {
      m_aMemoryOutputStream.writeTo (aOS);
    }
    else
    {
      final InputStream aIS = FileHelper.getInputStream (m_aOutputFile);
      StreamHelper.copyInputStreamToOutputStream (aIS, aOS);
    }
  }
}
