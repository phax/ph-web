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

import java.nio.charset.Charset;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.io.iface.IHasInputStream;
import com.helger.base.io.streamprovider.ByteArrayInputStreamProvider;
import com.helger.base.tostring.ToStringGenerator;

import jakarta.activation.FileTypeMap;

/**
 * Base implementation for interface {@link IEmailAttachment}.
 *
 * @author Philip Helger
 */
public class EmailAttachment implements IEmailAttachment
{
  public static final EEmailAttachmentDisposition DEFAULT_DISPOSITION = EEmailAttachmentDisposition.ATTACHMENT;

  private final String m_sFilename;
  private final IHasInputStream m_aInputStreamProvider;
  private final Charset m_aCharset;
  private final String m_sContentType;
  private final EEmailAttachmentDisposition m_eDisposition;

  public EmailAttachment (@NonNull @Nonempty final String sFilename, @NonNull final byte [] aContent)
  {
    this (sFilename, aContent, (Charset) null);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename, @NonNull final byte [] aContent, @Nullable final Charset aCharset)
  {
    this (sFilename, aContent, aCharset, DEFAULT_DISPOSITION);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename,
                          @NonNull final byte [] aContent,
                          @Nullable final Charset aCharset,
                          @NonNull final EEmailAttachmentDisposition eDisposition)
  {
    this (sFilename, aContent, aCharset, FileTypeMap.getDefaultFileTypeMap ().getContentType (sFilename), eDisposition);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename,
                          @NonNull final byte [] aContent,
                          @Nullable final Charset aCharset,
                          @Nullable final String sContentType,
                          @NonNull final EEmailAttachmentDisposition eDisposition)
  {
    this (sFilename, new ByteArrayInputStreamProvider (aContent), aCharset, sContentType, eDisposition);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename, @NonNull final IHasInputStream aInputStreamProvider)
  {
    this (sFilename, aInputStreamProvider, (Charset) null);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename,
                          @NonNull final IHasInputStream aInputStreamProvider,
                          @Nullable final Charset aCharset)
  {
    this (sFilename, aInputStreamProvider, aCharset, DEFAULT_DISPOSITION);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename,
                          @NonNull final IHasInputStream aInputStreamProvider,
                          @Nullable final Charset aCharset,
                          @NonNull final EEmailAttachmentDisposition eDisposition)
  {
    this (sFilename, aInputStreamProvider, aCharset, FileTypeMap.getDefaultFileTypeMap ().getContentType (sFilename), eDisposition);
  }

  public EmailAttachment (@NonNull @Nonempty final String sFilename,
                          @NonNull final IHasInputStream aInputStreamProvider,
                          @Nullable final Charset aCharset,
                          @Nullable final String sContentType,
                          @NonNull final EEmailAttachmentDisposition eDisposition)
  {
    m_sFilename = ValueEnforcer.notEmpty (sFilename, "Filename");
    m_aInputStreamProvider = ValueEnforcer.notNull (aInputStreamProvider, "InputStreamProvider");
    m_aCharset = aCharset;
    m_sContentType = sContentType;
    m_eDisposition = ValueEnforcer.notNull (eDisposition, "Disposition");
  }

  @NonNull
  @Nonempty
  public String getFilename ()
  {
    return m_sFilename;
  }

  @NonNull
  public IHasInputStream getInputStreamProvider ()
  {
    return m_aInputStreamProvider;
  }

  @Nullable
  public Charset getCharset ()
  {
    return m_aCharset;
  }

  @Nullable
  public String getContentType ()
  {
    return m_sContentType;
  }

  @NonNull
  public EEmailAttachmentDisposition getDisposition ()
  {
    return m_eDisposition;
  }

  @NonNull
  public EmailAttachmentDataSource getAsDataSource ()
  {
    return new EmailAttachmentDataSource (m_aInputStreamProvider, m_sFilename, m_sContentType, m_eDisposition);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final EmailAttachment rhs = (EmailAttachment) o;
    return m_sFilename.equals (rhs.m_sFilename) &&
           // Does not necessarily implement equals!
           // m_aInputStreamProvider.equals (rhs.m_aInputStreamProvider) &&
           EqualsHelper.equals (m_aCharset, rhs.m_aCharset) &&
           EqualsHelper.equals (m_sContentType, rhs.m_sContentType) &&
           m_eDisposition.equals (rhs.m_eDisposition);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sFilename)
                                       // Does not necessarily implement
                                       // hashCode!
                                       // .append (m_aInputStreamProvider)
                                       .append (m_aCharset)
                                       .append (m_sContentType)
                                       .append (m_eDisposition)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Filename", m_sFilename)
                                       .append ("InputStreamProvider", m_aInputStreamProvider)
                                       .appendIfNotNull ("Charset", m_aCharset)
                                       .appendIfNotNull ("ContentType", m_sContentType)
                                       .append ("Disposition", m_eDisposition)
                                       .getToString ();
  }
}
