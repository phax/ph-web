/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.io.IHasInputStream;
import com.helger.commons.state.EChange;
import com.helger.commons.state.IClearable;

/**
 * This interface represents attachments to be added to a mail message. Messages
 * with attachments are always send as MIME messages.
 *
 * @author Philip Helger
 */
public interface IMutableEmailAttachmentList extends IEmailAttachmentList, IClearable
{
  /**
   * Add an attachment.
   *
   * @param sFilename
   *        The ID/filename of the attachment. May not be <code>null</code>.
   * @param aISS
   *        The {@link IHasInputStream} representing the data. May not be
   *        <code>null</code>.
   */
  default void addAttachment (@Nonnull final String sFilename, @Nonnull final IHasInputStream aISS)
  {
    addAttachment (new EmailAttachment (sFilename, aISS));
  }

  /**
   * Add an attachment.
   *
   * @param aAttachment
   *        The attachment to be added. May not be <code>null</code>.
   */
  void addAttachment (@Nonnull IEmailAttachment aAttachment);

  /**
   * Add all provided attachments.
   *
   * @param aAttachments
   *        The attachments to be added. May be <code>null</code>.
   * @since 8.6.3
   */
  default void addAllAttachments (@Nullable final IEmailAttachment... aAttachments)
  {
    if (aAttachments != null)
      for (final IEmailAttachment aAttachment : aAttachments)
        addAttachment (aAttachment);
  }

  /**
   * Add all provided attachments.
   *
   * @param aAttachments
   *        The attachments to be added. May be <code>null</code>.
   * @since 8.6.3
   */
  default void addAllAttachments (@Nullable final Iterable <? extends IEmailAttachment> aAttachments)
  {
    if (aAttachments != null)
      for (final IEmailAttachment aAttachment : aAttachments)
        addAttachment (aAttachment);
  }

  /**
   * Remove the passed attachment.
   *
   * @param sFilename
   *        The file name of the attachment to be removed. The file name is case
   *        sensitive.
   * @return {@link EChange}
   */
  @Nonnull
  EChange removeAttachment (@Nullable String sFilename);
}
