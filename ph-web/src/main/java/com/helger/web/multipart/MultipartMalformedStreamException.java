/*
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
package com.helger.web.multipart;

import java.io.IOException;

/**
 * Thrown to indicate that the input stream fails to follow the required syntax.
 */
public class MultipartMalformedStreamException extends IOException
{
  /**
   * Constructs a <code>MalformedStreamException</code> with no detail message.
   */
  public MultipartMalformedStreamException ()
  {}

  /**
   * Constructs an <code>MalformedStreamException</code> with the specified
   * detail message.
   *
   * @param sMsg
   *        The detail message.
   */
  public MultipartMalformedStreamException (final String sMsg)
  {
    super (sMsg);
  }

  /**
   * Constructs an <code>MalformedStreamException</code> with the specified
   * detail message.
   *
   * @param sMsg
   *        The detail message.
   * @param aCause
   *        The cause of the exception
   */
  public MultipartMalformedStreamException (final String sMsg, final Throwable aCause)
  {
    super (sMsg, aCause);
  }
}
