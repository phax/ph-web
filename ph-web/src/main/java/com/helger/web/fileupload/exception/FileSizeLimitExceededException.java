/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.exception;

import javax.annotation.Nullable;

/**
 * Thrown to indicate that A files size exceeds the configured maximum.
 */
public class FileSizeLimitExceededException extends AbstractSizeException
{
  /**
   * Field name of the item, which caused the exception.
   */
  private final String m_sFieldName;

  /**
   * File name of the item, which caused the exception.
   */
  private final String m_sFilename;

  /**
   * Constructs a <code>SizeExceededException</code> with the specified detail
   * message, and actual and permitted sizes.
   *
   * @param sMsg
   *        The detail message.
   * @param nActual
   *        The actual request size.
   * @param nPermitted
   *        The maximum permitted request size.
   * @param sFieldName
   *        Field name of the item, which caused the exception.
   * @param sFilename
   *        File name of the item, which caused the exception.
   */
  public FileSizeLimitExceededException (final String sMsg,
                                         final long nActual,
                                         final long nPermitted,
                                         @Nullable final String sFieldName,
                                         @Nullable final String sFilename)
  {
    super (sMsg, nActual, nPermitted);
    m_sFieldName = sFieldName;
    m_sFilename = sFilename;
  }

  /**
   * Returns the file name of the item, which caused the exception.
   *
   * @return File name, if known, or null.
   */
  @Nullable
  public String getFileName ()
  {
    return m_sFilename;
  }

  /**
   * Returns the field name of the item, which caused the exception.
   *
   * @return Field name, if known, or null.
   */
  @Nullable
  public String getFieldName ()
  {
    return m_sFieldName;
  }
}
