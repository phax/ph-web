/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
 * Thrown to indicate that the number of parts (files and form fields) of a multipart request
 * exceeds the configured maximum.
 *
 * @author Philip Helger
 * @since 11.5.0
 */
public class FileCountLimitExceededException extends AbstractSizeException
{
  /**
   * Constructor
   *
   * @param sMessage
   *        The detail message.
   * @param nActual
   *        The actual number of parts.
   * @param nPermitted
   *        The maximum permitted number of parts.
   */
  public FileCountLimitExceededException (final String sMessage, final long nActual, final long nPermitted)
  {
    super (sMessage, nActual, nPermitted);
  }
}
