/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;

import com.helger.commons.collections.attrs.IAttributeContainer;
import com.helger.commons.email.IEmailAddress;

/**
 * Contains all possible fields for mail sending.<br>
 * Note: the attribute container may only contain String values!
 *
 * @author Philip Helger
 */
public interface IEmailData extends IReadonlyEmailData, IAttributeContainer
{
  @Nonnull
  IEmailData setEmailType (@Nonnull EEmailType eType);

  @Nonnull
  IEmailData setFrom (@Nullable IEmailAddress aFrom);

  @Nonnull
  IEmailData setReplyTo (@Nullable IEmailAddress aReplyTo);

  @Nonnull
  IEmailData setReplyTo (@Nullable IEmailAddress... aTo);

  @Nonnull
  IEmailData setReplyTo (@Nullable List <? extends IEmailAddress> aTo);

  @Nonnull
  IEmailData setTo (@Nullable IEmailAddress aTo);

  @Nonnull
  IEmailData setTo (@Nullable IEmailAddress... aTo);

  @Nonnull
  IEmailData setTo (@Nullable List <? extends IEmailAddress> aTo);

  @Nonnull
  IEmailData setCc (@Nullable IEmailAddress aCc);

  @Nonnull
  IEmailData setCc (@Nullable IEmailAddress... aCc);

  @Nonnull
  IEmailData setCc (@Nullable List <? extends IEmailAddress> aCc);

  @Nonnull
  IEmailData setBcc (@Nullable IEmailAddress aBcc);

  @Nonnull
  IEmailData setBcc (@Nullable IEmailAddress... aBcc);

  @Nonnull
  IEmailData setBcc (@Nullable List <? extends IEmailAddress> aBcc);

  @Nonnull
  IEmailData setSentDate (@Nullable DateTime aDate);

  @Nonnull
  IEmailData setSubject (@Nullable String sSubject);

  @Nonnull
  IEmailData setBody (@Nullable String sBody);

  @Nullable
  IEmailAttachmentList getAttachments ();

  /**
   * @return The number of contained attachments. Always &ge; 0.
   */
  @Nonnegative
  int getAttachmentCount ();

  /**
   * Specify a set of attachments to be send together with the mail. Pass
   * <code>null</code> to indicate that no attachments are desired (this is the
   * default).
   *
   * @param aAttachments
   *        The attachments to be used. May be <code>null</code> or empty.
   * @return this
   */
  @Nonnull
  IEmailData setAttachments (@Nullable IEmailAttachmentList aAttachments);
}
