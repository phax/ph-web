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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.joda.time.DateTime;

import com.helger.commons.collections.attrs.IReadonlyAttributeContainer;
import com.helger.commons.email.IEmailAddress;

/**
 * Contains all possible fields for mail sending in a read-only fashion.<br>
 * Note: the attribute container may only contain String values!
 * 
 * @author Philip Helger
 */
public interface IReadonlyEmailData extends IReadonlyAttributeContainer
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
  List <? extends IEmailAddress> getReplyTo ();

  /**
   * Get the reply-to email addresses.
   * 
   * @param sCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws UnsupportedEncodingException
   * @throws AddressException
   */
  @Nonnull
  @Deprecated
  InternetAddress [] getReplyToArray (@Nullable String sCharset) throws UnsupportedEncodingException, AddressException;

  /**
   * Get the reply-to email addresses.
   * 
   * @param aCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws AddressException
   */
  @Nonnull
  InternetAddress [] getReplyToArray (@Nullable Charset aCharset) throws AddressException;

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
  List <? extends IEmailAddress> getTo ();

  /**
   * Get a list of all TO-receivers.
   * 
   * @param sCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws UnsupportedEncodingException
   * @throws AddressException
   */
  @Nonnull
  @Deprecated
  InternetAddress [] getToArray (@Nullable String sCharset) throws UnsupportedEncodingException, AddressException;

  /**
   * Get a list of all TO-receivers.
   * 
   * @param aCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws AddressException
   */
  @Nonnull
  InternetAddress [] getToArray (@Nullable Charset aCharset) throws AddressException;

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
  List <? extends IEmailAddress> getCc ();

  /**
   * Get a list of all CC-receivers.
   * 
   * @param sCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws UnsupportedEncodingException
   * @throws AddressException
   */
  @Nonnull
  @Deprecated
  InternetAddress [] getCcArray (@Nullable String sCharset) throws UnsupportedEncodingException, AddressException;

  /**
   * Get a list of all CC-receivers.
   * 
   * @param aCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws AddressException
   */
  @Nonnull
  InternetAddress [] getCcArray (@Nullable Charset aCharset) throws AddressException;

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
  List <? extends IEmailAddress> getBcc ();

  /**
   * Get a list of all BCC-receivers.
   * 
   * @param sCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws UnsupportedEncodingException
   * @throws AddressException
   */
  @Nonnull
  InternetAddress [] getBccArray (@Nullable String sCharset) throws UnsupportedEncodingException, AddressException;

  /**
   * Get a list of all BCC-receivers.
   * 
   * @param aCharset
   *        Character set to use. May be <code>null</code>.
   * @return Never <code>null</code>.
   * @throws AddressException
   */
  @Nonnull
  InternetAddress [] getBccArray (@Nullable Charset aCharset) throws AddressException;

  /**
   * @return Number of BCC-receivers. Always &ge; 0.
   */
  @Nonnegative
  int getBccCount ();

  /**
   * Get the date when the mail claims to be sent.
   * 
   * @return <code>null</code> if no such date is specified.
   */
  @Nullable
  DateTime getSentDate ();

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
  IReadonlyEmailAttachmentList getAttachments ();
}
