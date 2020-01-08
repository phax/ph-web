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

import java.nio.charset.Charset;

import javax.activation.FileTypeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.ContainsSoftMigration;
import com.helger.commons.base64.Base64;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroElement;
import com.helger.xml.microdom.convert.IMicroTypeConverter;

public final class EmailAttachmentMicroTypeConverter implements IMicroTypeConverter <EmailAttachment>
{
  private static final String ATTR_FILENAME = "filename";
  private static final String ATTR_CHARSET = "charset";
  private static final String ATTR_DISPOSITION = "disposition";
  private static final String ATTR_CONTENT_TYPE = "contenttype";

  @Nonnull
  public IMicroElement convertToMicroElement (@Nonnull final EmailAttachment aAttachment,
                                              @Nullable final String sNamespaceURI,
                                              @Nonnull final String sTagName)
  {
    final IMicroElement eAttachment = new MicroElement (sNamespaceURI, sTagName);
    eAttachment.setAttribute (ATTR_FILENAME, aAttachment.getFilename ());
    if (aAttachment.hasCharset ())
      eAttachment.setAttribute (ATTR_CHARSET, aAttachment.getCharset ().name ());
    eAttachment.setAttribute (ATTR_DISPOSITION, aAttachment.getDisposition ().getID ());
    eAttachment.setAttribute (ATTR_CONTENT_TYPE, aAttachment.getContentType ());
    // Base64 encode
    final byte [] aBytes = StreamHelper.getAllBytes (aAttachment.getInputStream ());
    if (aBytes != null)
      eAttachment.appendText (Base64.encodeBytes (aBytes));
    return eAttachment;
  }

  @Nonnull
  @ContainsSoftMigration
  public EmailAttachment convertToNative (@Nonnull final IMicroElement eAttachment)
  {
    final String sFilename = eAttachment.getAttributeValue (ATTR_FILENAME);

    final String sCharset = eAttachment.getAttributeValue (ATTR_CHARSET);
    final Charset aCharset = sCharset == null ? null : CharsetHelper.getCharsetFromName (sCharset);

    String sContentType = eAttachment.getAttributeValue (ATTR_CONTENT_TYPE);
    if (sContentType == null)
    {
      // Soft migration 8.6.3
      sContentType = FileTypeMap.getDefaultFileTypeMap ().getContentType (sFilename);
    }

    final String sDisposition = eAttachment.getAttributeValue (ATTR_DISPOSITION);
    EEmailAttachmentDisposition eDisposition = EEmailAttachmentDisposition.getFromIDOrNull (sDisposition);
    if (eDisposition == null)
    {
      // migration
      eDisposition = EmailAttachment.DEFAULT_DISPOSITION;
    }

    final byte [] aContent = Base64.safeDecode (eAttachment.getTextContent ());

    return new EmailAttachment (sFilename, aContent, aCharset, sContentType, eDisposition);
  }
}
