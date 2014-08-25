/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
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
package com.helger.web.fileupload;

/**
 * Exception for errors encountered while processing the request.
 * 
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: FileUploadException.java 551000 2007-06-27 00:59:16Z jochen $
 */
public class FileUploadException extends Exception
{
  /**
   * Constructs a new <code>FileUploadException</code> without message.
   */
  public FileUploadException ()
  {
    this (null, null);
  }

  /**
   * Constructs a new <code>FileUploadException</code> with specified detail
   * message.
   * 
   * @param msg
   *        the error message.
   */
  public FileUploadException (final String msg)
  {
    this (msg, null);
  }

  /**
   * Creates a new <code>FileUploadException</code> with the given detail
   * message and cause.
   * 
   * @param msg
   *        The exceptions detail message.
   * @param cause
   *        The exceptions cause.
   */
  public FileUploadException (final String msg, final Throwable cause)
  {
    super (msg, cause);
  }
}
