/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.email.IEmailAddress;

/**
 * Contains all possible fields for mail sending.<br>
 * Note: the attribute container may only contain String values!
 *
 * @author Philip Helger
 */
public interface IMutableEmailData extends IEmailData
{
  @Nonnull
  IMutableEmailData setEmailType (@Nonnull EEmailType eType);

  @Nonnull
  IMutableEmailData setFrom (@Nullable IEmailAddress aFrom);

  @Nonnull
  IMutableEmailData setSentDateTime (@Nullable LocalDateTime aDateTime);

  @Nonnull
  IMutableEmailData setSubject (@Nullable String sSubject);

  @Nonnull
  IMutableEmailData setBody (@Nullable String sBody);

  // Change return type
  @Nullable
  IMutableEmailAttachmentList getAttachments ();

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
  IMutableEmailData setAttachments (@Nullable IEmailAttachmentList aAttachments);
}
