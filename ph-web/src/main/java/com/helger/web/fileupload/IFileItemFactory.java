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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * <p>
 * A factory interface for creating {@link IFileItem} instances. Factories can
 * provide their own custom configuration, over and above that provided by the
 * default file upload implementation.
 * </p>
 *
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @version $Id: FileItemFactory.java 479262 2006-11-26 03:09:24Z niallp $
 */
public interface IFileItemFactory
{
  /**
   * Define where to store files
   *
   * @param aRepository
   *        The directory to use. May be <code>null</code>.
   */
  void setRepository (@Nullable File aRepository);

  /**
   * Create a new {@link IFileItem} instance from the supplied parameters and
   * any local factory configuration.
   *
   * @param sFieldName
   *        The name of the form field.
   * @param sContentType
   *        The content type of the form field.
   * @param bIsFormField
   *        <code>true</code> if this is a plain form field; <code>false</code>
   *        otherwise.
   * @param sFileName
   *        The name of the uploaded file, if any, as supplied by the browser or
   *        other client.
   * @return The newly created file item.
   */
  @Nonnull
  IFileItem createItem (@Nullable String sFieldName, @Nullable String sContentType, boolean bIsFormField, @Nullable String sFileName);

  @Nonnull
  @ReturnsMutableCopy
  ICommonsList <File> getAllTemporaryFiles ();
}
