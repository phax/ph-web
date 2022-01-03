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
package com.helger.web.fileupload;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <p>
 * Abstracts access to the request information needed for file uploads. This
 * interfsace should be implemented for each type of request that may be handled
 * by FileUpload, such as servlets and portlets.
 * </p>
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @since FileUpload 1.1
 * @version $Id: RequestContext.java 479262 2006-11-26 03:09:24Z niallp $
 */
public interface IRequestContext
{
  /**
   * Retrieve the character encoding for the request.
   *
   * @return The character encoding for the request.
   */
  @Nullable
  String getCharacterEncoding ();

  /**
   * Retrieve the content type of the request.
   *
   * @return The content type of the request.
   */
  @Nullable
  String getContentType ();

  /**
   * Retrieve the content length of the request.
   *
   * @return The content length of the request. My be -1 to indicate an unknown
   *         content length.
   */
  @CheckForSigned
  long getContentLength ();

  /**
   * Retrieve the input stream for the request.
   *
   * @return The input stream for the request.
   * @throws IOException
   *         if a problem occurs.
   */
  @Nonnull
  InputStream getInputStream () throws IOException;
}
