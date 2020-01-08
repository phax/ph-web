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
package com.helger.smtp.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.attr.IStringMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.email.IEmailAddress;

/**
 * Contains all possible fields for mail sending in a read-only fashion. If the
 * email data should be serializable, the attribute values must implement
 * Serializable. If the email data should be used in failed mail data, than only
 * String values are allowed!
 *
 * @author Philip Helger
 */
public interface IEmailData extends Serializable
{
  /**
   * @return The type of the email - text or html.
   */
  @Nonnull
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
   */
  @Nonnull
  ICommonsList <IEmailAddress> getAllReplyTo ();

  /**
   * @return Number of reply-to addresses. Always &ge; 0.
   */
  @Nonnegative
  int getReplyToCount ();

  /**
   * Get a list of all TO-receivers.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  ICommonsList <IEmailAddress> getAllTo ();

  /**
   * Perform something for each TO-receiver
   *
   * @param aConsumer
   *        The consumer to be invoked. May not be <code>null</code>.
   */
  void forEachTo (@Nonnull Consumer <? super IEmailAddress> aConsumer);

  /**
   * @return Number of TO-receivers. Always &ge; 0.
   */
  @Nonnegative
  int getToCount ();

  /**
   * Get a list of all CC-receivers.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  ICommonsList <IEmailAddress> getAllCc ();

  /**
   * Perform something for each CC-receiver
   *
   * @param aConsumer
   *        The consumer to be invoked. May not be <code>null</code>.
   */
  void forEachCc (@Nonnull Consumer <? super IEmailAddress> aConsumer);

  /**
   * @return Number of CC-receivers. Always &ge; 0.
   */
  @Nonnegative
  int getCcCount ();

  /**
   * Get a list of all BCC-receivers.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  ICommonsList <IEmailAddress> getAllBcc ();

  /**
   * Perform something for each BCC-receiver
   *
   * @param aConsumer
   *        The consumer to be invoked. May not be <code>null</code>.
   */
  void forEachBcc (@Nonnull Consumer <? super IEmailAddress> aConsumer);

  /**
   * @return Number of BCC-receivers. Always &ge; 0.
   */
  @Nonnegative
  int getBccCount ();

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
   * Get an optional list of attachments. If attachments are present, the mail
   * is always created as a MIME message and never a simple text message.
   *
   * @return A map from the filename to an {@link java.io.InputStream}-provider
   *         containing all desired attachments or <code>null</code>/an empty
   *         container if no attachments are required.
   */
  @Nullable
  IEmailAttachmentList getAttachments ();

  /**
   * @return Custom attributes. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableObject
  IStringMap attrs ();
}
