/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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

import javax.annotation.Nullable;

import com.helger.web.fileupload.exception.InvalidFileNameException;

/**
 * <p>
 * This interface provides access to a file or form item that was received
 * within a <code>multipart/form-data</code> POST request. The items contents
 * are retrieved by calling {@link #openStream()}.
 * </p>
 * <p>
 * Instances of this class are created by accessing the iterator, returned by
 * {@link com.helger.web.fileupload.parse.AbstractFileUploadBase#getItemIterator(IRequestContext)}
 * .
 * </p>
 * <p>
 * <em>Note</em>: There is an interaction between the iterator and its
 * associated instances of {@link IFileItemStream}: By invoking
 * {@link java.util.Iterator#hasNext()} on the iterator, you discard all data,
 * which hasn't been read so far from the previous data.
 * </p>
 */
public interface IFileItemStream extends IFileItemHeadersSupport
{
  /**
   * Creates an {@link InputStream}, which allows to read the items contents.
   *
   * @return The input stream, from which the items data may be read.
   * @throws IllegalStateException
   *         The method was already invoked on this item. It is not possible to
   *         recreate the data stream.
   * @throws IOException
   *         An I/O error occurred.
   */
  InputStream openStream () throws IOException;

  /**
   * Returns the content type passed by the browser or <code>null</code> if not
   * defined.
   *
   * @return The content type passed by the browser or <code>null</code> if not
   *         defined.
   */
  String getContentType ();

  /**
   * Returns the original filename in the client's filesystem, as provided by
   * the browser (or other client software). In most cases, this will be the
   * base file name, without path information. However, some clients, such as
   * the Opera browser, do include path information.
   *
   * @return The original filename in the client's filesystem.
   * @throws InvalidFileNameException
   *         The file name contains a NUL character, which might be an indicator
   *         of a security attack. If you intend to use the file name anyways,
   *         catch the exception and use InvalidFileNameException#getName().
   */
  String getName ();

  /**
   * Returns the original filename in the client's filesystem, as provided by
   * the browser (or other client software). In most cases, this will be the
   * base file name, without path information. However, some clients, such as
   * the Opera browser, do include path information. Compared to
   * {@link #getName()} this method automatically removes everything and
   * including a NUL byte and therefore does not throw an
   * {@link InvalidFileNameException}.
   *
   * @return The original filename in the client's filesystem.
   * @since 6.1.0
   */
  String getNameSecure ();

  /**
   * Returns the original filename in the client's filesystem, as provided by
   * the browser (or other client software). In most cases, this will be the
   * base file name, without path information. However, some clients, such as
   * the Opera browser, do include path information.
   *
   * @return The original filename in the client's filesystem.
   * @since 6.1.0
   */
  @Nullable
  String getNameUnchecked ();

  /**
   * Returns the name of the field in the multipart form corresponding to this
   * file item.
   *
   * @return The name of the form field.
   */
  String getFieldName ();

  /**
   * Determines whether or not a <code>FileItem</code> instance represents a
   * simple form field.
   *
   * @return <code>true</code> if the instance represents a simple form field;
   *         <code>false</code> if it represents an uploaded file.
   */
  boolean isFormField ();
}
