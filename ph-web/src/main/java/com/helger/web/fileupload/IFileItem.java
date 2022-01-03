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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.activation.DataSource;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.io.IHasInputStream;
import com.helger.commons.state.ISuccessIndicator;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.exception.InvalidFileNameException;

/**
 * <p>
 * This class represents a file or form item that was received within a
 * <code>multipart/form-data</code> POST request.
 * <p>
 * After retrieving an instance of this class from a
 * {@link com.helger.web.fileupload.parse.FileUpload FileUpload} instance (see
 * {@link com.helger.web.fileupload.servlet.ServletFileUpload#parseRequest(javax.servlet.http.HttpServletRequest)}
 * ), you may either request all contents of the file at once using
 * {@link #directGet()} or request an {@link java.io.InputStream InputStream}
 * with {@link #getInputStream()} and process the file without attempting to
 * load it into memory, which may come handy with large files.
 * <p>
 * While this interface does not extend <code>javax.activation.DataSource</code>
 * per se (to avoid a seldom used dependency), several of the defined methods
 * are specifically defined with the same signatures as methods in that
 * interface. This allows an implementation of this interface to also implement
 * <code>javax.activation.DataSource</code> with minimal additional work.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @version $Id: FileItem.java 963609 2010-07-13 06:56:47Z jochen $
 */
public interface IFileItem extends DataSource, IHasInputStream
{
  /**
   * @return An {@link InputStream} that can be used to retrieve the contents of
   *         the file.
   */
  @Nonnull
  InputStream getInputStream ();

  /**
   * @return An {@link OutputStream} that can be used for storing the contents
   *         of the file.
   */
  @Nonnull
  OutputStream getOutputStream ();

  /**
   * @return The content type passed by the user agent or <code>null</code> if
   *         not defined.
   */
  @Nullable
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
  @Nullable
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
   * @return The original filename in the client's filesystem without invalid
   *         characters.
   * @since 6.1.0
   */
  @Nullable
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
   * Provides a hint as to whether or not the file contents will be read from
   * memory.
   *
   * @return <code>true</code> if the file contents will be read from memory;
   *         <code>false</code> otherwise.
   */
  boolean isInMemory ();

  /**
   * @return The size of the file item, in bytes.
   */
  @Nonnegative
  long getSize ();

  /**
   * @return The contents of the file item as an array of bytes.
   */
  byte [] directGet ();

  /**
   * Returns the contents of the file item as a String, using the specified
   * encoding. This method uses {@link #directGet()} to retrieve the contents of
   * the item.
   *
   * @param aEncoding
   *        The character encoding to use.
   * @return The contents of the item, as a string.
   */
  @Nonnull
  default String getString (@Nonnull final Charset aEncoding)
  {
    return new String (directGet (), aEncoding);
  }

  /**
   * Returns the contents of the file item as a String, using the default
   * character encoding (if one provided, it is used). This method uses
   * {@link #directGet()} to retrieve the contents of the item.
   *
   * @return The contents of the item, as a string.
   */
  @Nonnull
  String getString ();

  /**
   * A convenience method to write an uploaded item to disk. The client code is
   * not concerned with whether or not the item is stored in memory, or on disk
   * in a temporary location. They just want to write the uploaded item to a
   * file.
   * <p>
   * This method is not guaranteed to succeed if called more than once for the
   * same item. This allows a particular implementation to use, for example,
   * file renaming, where possible, rather than copying all of the underlying
   * data, thus gaining a significant performance benefit.
   *
   * @param aDstFile
   *        The <code>File</code> into which the uploaded item should be stored.
   * @return Never null
   * @throws FileUploadException
   *         if an error occurs.
   */
  @Nonnull
  ISuccessIndicator write (@Nonnull File aDstFile) throws FileUploadException;

  /**
   * Deletes the underlying storage for a file item, including deleting any
   * associated temporary disk file. Although this storage will be deleted
   * automatically when the <code>FileItem</code> instance is garbage collected,
   * this method can be used to ensure that this is done at an earlier time,
   * thus preserving system resources.
   */
  void delete ();

  /**
   * Returns the name of the field in the multipart form corresponding to this
   * file item.
   *
   * @return The name of the form field.
   */
  String getFieldName ();

  /**
   * Sets the field name used to reference this file item.
   *
   * @param sFieldName
   *        The name of the form field.
   */
  void setFieldName (String sFieldName);

  /**
   * Determines whether or not a <code>FileItem</code> instance represents a
   * simple form field.
   *
   * @return <code>true</code> if the instance represents a simple form field;
   *         <code>false</code> if it represents an uploaded file.
   */
  boolean isFormField ();

  /**
   * Specifies whether or not a <code>FileItem</code> instance represents a
   * simple form field.
   *
   * @param bIsFormField
   *        <code>true</code> if the instance represents a simple form field;
   *        <code>false</code> if it represents an uploaded file.
   */
  void setFormField (boolean bIsFormField);
}
