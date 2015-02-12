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
package com.helger.web.fileupload.io;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.string.StringHelper;
import com.helger.web.fileupload.exception.InvalidFileNameException;

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
      throw new InvalidFileNameException (sFilename, "Invalid filename: " +
                                                     StringHelper.replaceAll (sFilename, "\u0000", "\\0"));
    }
    return sFilename;
  }
}
