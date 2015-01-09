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
package com.helger.web.smtp.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.convert.IMicroTypeConverter;
import com.helger.commons.microdom.convert.MicroTypeConverter;
import com.helger.commons.microdom.impl.MicroElement;
import com.helger.web.smtp.IEmailAttachment;

public final class EmailAttachmentListMicroTypeConverter implements IMicroTypeConverter
{
  private static final String ELEMENT_ATTACHMENT = "attachment";

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final Object aSource,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final EmailAttachmentList aAttachmentList = (EmailAttachmentList) aSource;
    final IMicroElement eAttachmentList = new MicroElement (sNamespaceURI, sTagName);
    for (final IEmailAttachment aAttachment : aAttachmentList.directGetAllAttachments ())
      eAttachmentList.appendChild (MicroTypeConverter.convertToMicroElement (aAttachment,
                                                                             sNamespaceURI,
                                                                             ELEMENT_ATTACHMENT));
    return eAttachmentList;
  }

  @Nonnull
  public EmailAttachmentList convertToNative (@Nonnull final IMicroElement eAttachmentList)
  {
    final EmailAttachmentList ret = new EmailAttachmentList ();
    for (final IMicroElement eAttachment : eAttachmentList.getAllChildElements (ELEMENT_ATTACHMENT))
      ret.addAttachment (MicroTypeConverter.convertToNative (eAttachment, EmailAttachment.class));
    return ret;
  }
}
