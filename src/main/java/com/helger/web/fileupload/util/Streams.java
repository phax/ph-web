/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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

import javax.annotation.Nullable;

import com.helger.commons.io.streams.StreamUtils;
import com.helger.web.fileupload.InvalidFileNameException;

/**
 * Utility class for working with streams.
 */
public final class Streams
{
  /**
   * Private constructor, to prevent instantiation. This class has only static
   * methods.
   */
  private Streams ()
  {
    // Does nothing
  }

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
   * @param pInputStream
   *        The input stream, which is being read. It is guaranteed, that
   *        {@link InputStream#close()} is called on the stream.
   * @param pOutputStream
   *        The output stream, to which data should be written. May be null, in
   *        which case the input streams contents are simply discarded.
   * @param pClose
   *        True guarantees, that {@link OutputStream#close()} is called on the
   *        stream. False indicates, that only {@link OutputStream#flush()}
   *        should be called finally.
   * @return Number of bytes, which have been copied.
   * @throws IOException
   *         An I/O error occurred.
   */
  public static long copy (final InputStream pInputStream, final OutputStream pOutputStream, final boolean pClose) throws IOException
  {
    final byte [] pBuffer = new byte [DEFAULT_BUFFER_SIZE];
    OutputStream out = pOutputStream;
    InputStream in = pInputStream;
    try
    {
      long total = 0;
      for (;;)
      {
        final int res = in.read (pBuffer);
        if (res == -1)
          break;
        if (res > 0)
        {
          total += res;
          if (out != null)
          {
            out.write (pBuffer, 0, res);
          }
        }
      }
      if (out != null)
      {
        if (pClose)
        {
          out.close ();
        }
        else
        {
          out.flush ();
        }
        out = null;
      }
      in.close ();
      in = null;
      return total;
    }
    finally
    {
      StreamUtils.close (in);
      if (pClose)
        StreamUtils.close (out);
    }
  }

  /**
   * Checks, whether the given file name is valid in the sense, that it doesn't
   * contain any NUL characters. If the file name is valid, it will be returned
   * without any modifications. Otherwise, an {@link InvalidFileNameException}
   * is raised.
   * 
   * @param pFileName
   *        The file name to check
   * @return Unmodified file name, if valid.
   * @throws InvalidFileNameException
   *         The file name was found to be invalid.
   */
  @Nullable
  public static String checkFileName (@Nullable final String pFileName)
  {
    if (pFileName != null && pFileName.indexOf ('\u0000') != -1)
    {
      // pFileName.replace("\u0000", "\\0")
      final StringBuilder sb = new StringBuilder ();
      for (int i = 0; i < pFileName.length (); i++)
      {
        final char c = pFileName.charAt (i);
        switch (c)
        {
          case 0:
            sb.append ("\\0");
            break;
          default:
            sb.append (c);
            break;
        }
      }
      throw new InvalidFileNameException (pFileName, "Invalid file name: " + sb);
    }
    return pFileName;
  }
}
