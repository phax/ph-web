/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

/**
 * This exception is thrown, if a requests permitted size is exceeded.
 */
abstract class AbstractSizeException extends FileUploadException
{
  /**
   * The actual size of the request.
   */
  private final long m_nActual;

  /**
   * The maximum permitted size of the request.
   */
  private final long m_nPermitted;

  /**
   * Creates a new instance.
   *
   * @param sMessage
   *        The detail message.
   * @param nActual
   *        The actual number of bytes in the request.
   * @param nPermitted
   *        The requests size limit, in bytes.
   */
  protected AbstractSizeException (final String sMessage, final long nActual, final long nPermitted)
  {
    super (sMessage);
    m_nActual = nActual;
    m_nPermitted = nPermitted;
  }

  /**
   * Retrieves the actual size of the request.
   *
   * @return The actual size of the request.
   */
  public long getActualSize ()
  {
    return m_nActual;
  }

  /**
   * Retrieves the permitted size of the request.
   *
   * @return The permitted size of the request.
   */
  public long getPermittedSize ()
  {
    return m_nPermitted;
  }
}
