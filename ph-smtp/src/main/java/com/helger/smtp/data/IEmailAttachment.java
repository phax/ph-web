/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.smtp.data;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.io.iface.IHasInputStream;
import com.helger.base.string.StringHelper;

/**
 * This interface represents attachments to be added to a mail message. Messages with attachments
 * are always send as MIME messages.
 *
 * @author Philip Helger
 */
public interface IEmailAttachment extends IHasInputStream
{
  /**
   * @return The filename of the attachment
   */
  @NonNull
  @Nonempty
  String getFilename ();

  /**
   * @return The object holding the input stream to the data.
   */
  @NonNull
  IHasInputStream getInputStreamProvider ();

  @NonNull
  default InputStream getInputStream ()
  {
    return getInputStreamProvider ().getInputStream ();
  }

  default boolean isReadMultiple ()
  {
    return getInputStreamProvider ().isReadMultiple ();
  }

  /**
   * @return The charset of the email attachment. May be <code>null</code> if not specified.
   */
  @Nullable
  Charset getCharset ();

  default boolean hasCharset ()
  {
    return getCharset () != null;
  }

  /**
   * @return The content type (MIME type) of the attachment
   */
  @Nullable
  String getContentType ();

  default boolean hasContentType ()
  {
    return StringHelper.isNotEmpty (getContentType ());
  }

  /**
   * @return The disposition type to use. Never <code>null</code>.
   */
  @NonNull
  EEmailAttachmentDisposition getDisposition ();

  /**
   * @return The attachment as a {@link jakarta.activation.DataSource}.
   */
  @NonNull
  IEmailAttachmentDataSource getAsDataSource ();
}
