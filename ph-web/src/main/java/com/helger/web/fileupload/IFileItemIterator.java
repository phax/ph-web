/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;

import com.helger.web.fileupload.exception.FileUploadException;

/**
 * An iterator, as returned by
 * {@link com.helger.web.fileupload.parse.AbstractFileUploadBase#getItemIterator(IRequestContext)}
 * .
 */
public interface IFileItemIterator
{
  /**
   * Returns, whether another instance of {@link IFileItemStream} is available.
   *
   * @throws FileUploadException
   *         Parsing or processing the file item failed.
   * @throws IOException
   *         Reading the file item failed.
   * @return True, if one or more additional file items are available, otherwise
   *         false.
   */
  boolean hasNext () throws FileUploadException, IOException;

  /**
   * Returns the next available {@link IFileItemStream}.
   *
   * @throws java.util.NoSuchElementException
   *         No more items are available. Use {@link #hasNext()} to prevent this
   *         exception.
   * @throws FileUploadException
   *         Parsing or processing the file item failed.
   * @throws IOException
   *         Reading the file item failed.
   * @return FileItemStream instance, which provides access to the next file
   *         item.
   */
  @Nonnull
  IFileItemStream next () throws FileUploadException, IOException;
}
