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

import java.time.LocalDateTime;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonnegative;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.email.IEmailAddress;
import com.helger.collection.commons.ICommonsList;
import com.helger.typeconvert.collection.IStringMap;

/**
 * Contains all possible fields for mail sending in a read-only fashion. If the email data should be
 * used in failed mail data, than only String values are allowed!
 *
 * @author Philip Helger
 */
public interface IEmailData
{
  /**
   * @return The type of the email - text or html.
   */
  @NonNull
  EEmailType getEmailType ();

  /**
   * Get the sender mail address.
   *
   * @return <code>null</code> if no sender is specified.
   */
  @Nullable
  IEmailAddress getFrom ();

  /**
   * Get the reply-to mail addresses.
   *
   * @return never <code>null</code>
   * @since 9.1.9
   */
  @NonNull
  @ReturnsMutableObject
  ICommonsList <IEmailAddress> replyTo ();

  /**
   * Get a list of all TO-receivers.
   *
   * @return Never <code>null</code>.
   * @since 9.1.9
   */
  @NonNull
  @ReturnsMutableObject
  ICommonsList <IEmailAddress> to ();

  /**
   * Get a list of all CC-receivers.
   *
   * @return Never <code>null</code>.
   * @since 9.1.9
   */
  @NonNull
  @ReturnsMutableObject
  ICommonsList <IEmailAddress> cc ();

  /**
   * Get a list of all BCC-receivers.
   *
   * @return Never <code>null</code>.
   * @since 9.1.9
   */
  @NonNull
  @ReturnsMutableObject
  ICommonsList <IEmailAddress> bcc ();

  /**
   * Get the date and time when the mail claims to be sent.
   *
   * @return <code>null</code> if no such date is specified.
   */
  @Nullable
  LocalDateTime getSentDateTime ();

  /**
   * Get the subject line.
   *
   * @return <code>null</code> if no subject is specified.
   */
  @Nullable
  String getSubject ();

  /**
   * Get the mail content.
   *
   * @return <code>null</code> if no content is specified.
   */
  @Nullable
  String getBody ();

  /**
   * Get an optional list of attachments. If attachments are present, the mail is always created as
   * a MIME message and never a simple text message.
   *
   * @return A map from the filename to an {@link java.io.InputStream}-provider containing all
   *         desired attachments or <code>null</code>/an empty container if no attachments are
   *         required.
   */
  @Nullable
  IEmailAttachmentList getAttachments ();

  /**
   * @return The number of contained attachments. Always &ge; 0.
   */
  @Nonnegative
  default int getAttachmentCount ()
  {
    final IEmailAttachmentList ret = getAttachments ();
    return ret == null ? 0 : ret.size ();
  }

  /**
   * @return Custom attributes. Never <code>null</code>.
   */
  @NonNull
  @ReturnsMutableObject
  IStringMap attrs ();
}
