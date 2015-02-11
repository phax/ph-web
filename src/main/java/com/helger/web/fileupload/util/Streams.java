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
package com.helger.web.fileupload.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.io.streams.StreamUtils;
import com.helger.web.fileupload.InvalidFileNameException;

/**
 * Utility class for working with streams.
 */
@Immutable
public final class Streams
{
  /**
   * Private constructor, to prevent instantiation. This class has only static
   * methods.
   */
  private Streams ()
  {}

  /**
   * Default buffer size for use in
   * {@link #copy(InputStream, OutputStream, boolean)}.
   */
  private static final int DEFAULT_BUFFER_SIZE = 8192;

  /**
   * Copies the contents of the given {@link InputStream} to the given
   * {@link OutputStream}. Shortcut for
   *
   * <pre>
   * copy (pInputStream, pOutputStream, new byte [8192]);
   * </pre>
   *
   * @param aIS
   *        The input stream, which is being read. It is guaranteed, that
   *        {@link InputStream#close()} is called on the stream.
   * @param aOS
   *        The output stream, to which data should be written. May be null, in
   *        which case the input streams contents are simply discarded.
   * @param bClose
   *        <code>true</code> guarantees, that {@link OutputStream#close()} is
   *        called on the stream. <code>false</code> indicates, that only
   *        {@link OutputStream#flush()} should be called finally.
   * @return Number of bytes, which have been copied.
   * @throws IOException
   *         An I/O error occurred.
   */
  public static long copy (@Nonnull @WillClose final InputStream aIS,
                           @Nonnull final OutputStream aOS,
                           final boolean bClose) throws IOException
  {
    final byte [] pBuffer = new byte [DEFAULT_BUFFER_SIZE];
    OutputStream out = aOS;
    InputStream in = aIS;
    try
    {
      long nTotal = 0;
      for (;;)
      {
        final int nBytesRead = in.read (pBuffer);
        if (nBytesRead == -1)
          break;
        if (nBytesRead > 0)
        {
          nTotal += nBytesRead;
          if (out != null)
            out.write (pBuffer, 0, nBytesRead);
        }
      }
      if (out != null)
      {
        if (bClose)
          out.close ();
        else
          out.flush ();
        out = null;
      }
      in.close ();
      in = null;
      return nTotal;
    }
    finally
    {
      StreamUtils.close (in);
      if (bClose)
        StreamUtils.close (out);
    }
  }

  /**
   * Checks, whether the given file name is valid in the sense, that it doesn't
   * contain any NUL characters. If the file name is valid, it will be returned
   * without any modifications. Otherwise, an {@link InvalidFileNameException}
   * is raised.
   *
   * @param sFilename
   *        The file name to check
   * @return Unmodified file name, if valid.
   * @throws InvalidFileNameException
   *         The file name was found to be invalid.
   */
  @Nullable
  public static String checkFileName (@Nullable final String sFilename) throws InvalidFileNameException
  {
    if (sFilename != null && sFilename.indexOf ('\u0000') != -1)
    {
      final StringBuilder aSB = new StringBuilder ();
      for (final char c : sFilename.toCharArray ())
        if (c == 0)
          aSB.append ("\\0");
        else
          aSB.append (c);
      throw new InvalidFileNameException (sFilename, "Invalid filename: " + aSB.toString ());
    }
    return sFilename;
  }
}
