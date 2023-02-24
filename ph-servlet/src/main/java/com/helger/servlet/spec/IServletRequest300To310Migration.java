/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.servlet.spec;

import jakarta.servlet.ServletRequest;

/**
 * Dummy interface with all ServletRequest default methods for new methods in
 * Servlet Spec 3.1.0 compared to 3.0.0
 *
 * @author Philip Helger
 */
public interface IServletRequest300To310Migration extends ServletRequest
{
  /**
   * Returns the length, in bytes, of the request body and made available by the
   * input stream, or -1 if the length is not known. For HTTP servlets, same as
   * the value of the CGI variable CONTENT_LENGTH.
   *
   * @return a long containing the length of the request body or -1L if the
   *         length is not known
   * @since Servlet 3.1
   */
  default long getContentLengthLong ()
  {
    throw new UnsupportedOperationException ();
  }
}
