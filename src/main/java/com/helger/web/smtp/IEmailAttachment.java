/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.smtp;

import java.io.Serializable;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.io.IInputStreamProvider;

/**
 * This interface represents attachments to be added to a mail message. Messages
 * with attachments are always send as MIME messages.
 * 
 * @author Philip Helger
 */
public interface IEmailAttachment extends IInputStreamProvider, Serializable
{
  /**
   * @return The filename of the attachment
   */
  @Nonnull
  @Nonempty
  String getFilename ();

  /**
   * @return The object holding the input stream to the data.
   */
  @Nonnull
  IInputStreamProvider getInputStreamProvider ();

  /**
   * @return The charset of the email attachment. May be <code>null</code> if
   *         not specified.
   */
  @Nullable
  Charset getCharset ();

  /**
   * @return The content type (MIME type) of the attachment
   */
  @Nullable
  String getContentType ();

  /**
   * @return The disposition type to use. Never <code>null</code>.
   */
  @Nonnull
  EEmailAttachmentDisposition getDisposition ();

  /**
   * @return The attachment as a {@link javax.activation.DataSource}.
   */
  @Nonnull
  IEmailAttachmentDataSource getAsDataSource ();
}
