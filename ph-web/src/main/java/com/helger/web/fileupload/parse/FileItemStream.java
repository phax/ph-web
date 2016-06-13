/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.parse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.io.file.FilenameHelper;
import com.helger.web.fileupload.IFileItemHeaders;
import com.helger.web.fileupload.IFileItemStream;
import com.helger.web.fileupload.exception.FileSizeLimitExceededException;
import com.helger.web.fileupload.exception.FileUploadIOException;
import com.helger.web.fileupload.io.AbstractLimitedInputStream;
import com.helger.web.fileupload.io.FileUploadHelper;
import com.helger.web.fileupload.io.ICloseable;
import com.helger.web.multipart.MultipartItemSkippedException;
import com.helger.web.multipart.MultipartStream;
import com.helger.web.multipart.MultipartStream.MultipartItemInputStream;

/**
 * Default implementation of {@link IFileItemStream}.
 */
@NotThreadSafe
final class FileItemStream implements IFileItemStream, Closeable
{
  /**
   * The file items content type.
   */
  private final String m_sContentType;
  /**
   * The file items field name.
   */
  private final String m_sFieldName;
  /**
   * The file items file name.
   */
  private final String m_sName;
  /**
   * Whether the file item is a form field.
   */
  private final boolean m_bFormField;
  /**
   * The file items input stream.
   */
  private final InputStream m_aIS;
  /**
   * The headers, if any.
   */
  private IFileItemHeaders m_aHeaders;

  /**
   * Creates a new instance.
   *
   * @param sName
   *        The items file name, or null.
   * @param sFieldName
   *        The items field name.
   * @param sContentType
   *        The items content type, or null.
   * @param bFormField
   *        Whether the item is a form field.
   * @param nContentLength
   *        The items content length, if known, or -1
   * @throws IOException
   *         Creating the file item failed.
   */
  FileItemStream (final String sName,
                  final String sFieldName,
                  final String sContentType,
                  final boolean bFormField,
                  @CheckForSigned final long nContentLength,
                  @Nonnull final MultipartStream aMulti,
                  final long nFileSizeMax) throws IOException
  {
    m_sName = sName;
    m_sFieldName = sFieldName;
    m_sContentType = sContentType;
    m_bFormField = bFormField;
    final MultipartItemInputStream aItemIS = aMulti.createInputStream ();
    InputStream aIS = aItemIS;
    if (nFileSizeMax > 0)
    {
      if (nContentLength >= 0 && nContentLength > nFileSizeMax)
      {
        final FileSizeLimitExceededException ex = new FileSizeLimitExceededException ("The field " +
                                                                                      m_sFieldName +
                                                                                      " exceeds its maximum permitted " +
                                                                                      " size of " +
                                                                                      nFileSizeMax +
                                                                                      " bytes.",
                                                                                      nContentLength,
                                                                                      nFileSizeMax,
                                                                                      sFieldName,
                                                                                      sName);
        throw new FileUploadIOException (ex);
      }

      aIS = new AbstractLimitedInputStream (aIS, nFileSizeMax)
      {
        @Override
        protected void onLimitExceeded (final long nSizeMax, final long nCount) throws IOException
        {
          aItemIS.close (true);
          final FileSizeLimitExceededException ex = new FileSizeLimitExceededException ("The field " +
                                                                                        m_sFieldName +
                                                                                        " exceeds its maximum permitted " +
                                                                                        " size of " +
                                                                                        nSizeMax +
                                                                                        " bytes.",
                                                                                        nCount,
                                                                                        nSizeMax,
                                                                                        m_sFieldName,
                                                                                        m_sFieldName);
          // Wrap as IO exception ;|
          throw new FileUploadIOException (ex);
        }
      };
    }
    m_aIS = aIS;
  }

  /**
   * Returns the items content type, or null.
   *
   * @return Content type, if known, or null.
   */
  public String getContentType ()
  {
    return m_sContentType;
  }

  /**
   * Returns the items field name.
   *
   * @return Field name.
   */
  public String getFieldName ()
  {
    return m_sFieldName;
  }

  @Nullable
  public String getName ()
  {
    return FileUploadHelper.checkFileName (m_sName);
  }

  @Nullable
  public String getNameSecure ()
  {
    return FilenameHelper.getAsSecureValidFilename (m_sName);
  }

  @Nullable
  public String getNameUnchecked ()
  {
    return m_sName;
  }

  /**
   * Returns, whether this is a form field.
   *
   * @return True, if the item is a form field, otherwise false.
   */
  public boolean isFormField ()
  {
    return m_bFormField;
  }

  /**
   * Returns an input stream, which may be used to read the items contents.
   *
   * @return Opened input stream.
   * @throws IOException
   *         An I/O error occurred.
   */
  @Nonnull
  public InputStream openStream () throws IOException
  {
    if (((ICloseable) m_aIS).isClosed ())
      throw new MultipartItemSkippedException ();
    return m_aIS;
  }

  /**
   * Closes the file item.
   *
   * @throws IOException
   *         An I/O error occurred.
   */
  public void close () throws IOException
  {
    m_aIS.close ();
  }

  @Nullable
  public IFileItemHeaders getHeaders ()
  {
    return m_aHeaders;
  }

  public void setHeaders (@Nullable final IFileItemHeaders aHeaders)
  {
    m_aHeaders = aHeaders;
  }
}
