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

/**
 * This exception is thrown in case of an invalid file name. A file name is
 * invalid, if it contains a NUL character. Attackers might use this to
 * circumvent security checks: For example, a malicious user might upload a file
 * with the name "foo.exe\0.png". This file name might pass security checks
 * (i.e. checks for the extension ".png"), while, depending on the underlying C
 * library, it might create a file named "foo.exe", as the NUL character is the
 * string terminator in C.
 */
public class InvalidFileNameException extends RuntimeException
{
  private final String m_sName;

  /**
   * Creates a new instance.
   *
   * @param sName
   *        The file name causing the exception.
   * @param sMessage
   *        A human readable error message.
   */
  public InvalidFileNameException (final String sName, final String sMessage)
  {
    super (sMessage);
    m_sName = sName;
  }

  /**
   * @return the invalid file name.
   */
  public String getName ()
  {
    return m_sName;
  }
}
