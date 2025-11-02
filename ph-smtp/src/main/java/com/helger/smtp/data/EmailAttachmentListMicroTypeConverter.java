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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroElement;
import com.helger.xml.microdom.convert.IMicroTypeConverter;
import com.helger.xml.microdom.convert.MicroTypeConverter;

/**
 * Micro type converter for class {@link EmailAttachmentList}.
 *
 * @author Philip Helger
 */
public final class EmailAttachmentListMicroTypeConverter implements IMicroTypeConverter <EmailAttachmentList>
{
  private static final String ELEMENT_ATTACHMENT = "attachment";

  @NonNull
  public IMicroElement convertToMicroElement (@NonNull final EmailAttachmentList aAttachmentList,
                                              @Nullable final String sNamespaceURI,
                                              @NonNull final String sTagName)
  {
    final IMicroElement eAttachmentList = new MicroElement (sNamespaceURI, sTagName);
    for (final IEmailAttachment aAttachment : aAttachmentList.directGetAllAttachments ())
      eAttachmentList.addChild (MicroTypeConverter.convertToMicroElement (aAttachment,
                                                                          sNamespaceURI,
                                                                          ELEMENT_ATTACHMENT));
    return eAttachmentList;
  }

  @NonNull
  public EmailAttachmentList convertToNative (@NonNull final IMicroElement eAttachmentList)
  {
    final EmailAttachmentList ret = new EmailAttachmentList ();
    for (final IMicroElement eAttachment : eAttachmentList.getAllChildElements (ELEMENT_ATTACHMENT))
      ret.addAttachment (MicroTypeConverter.convertToNative (eAttachment, EmailAttachment.class));
    return ret;
  }
}
