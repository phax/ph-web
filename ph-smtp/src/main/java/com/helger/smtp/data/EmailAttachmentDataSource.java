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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.IHasInputStream;
import com.helger.commons.string.ToStringGenerator;
import com.helger.mail.datasource.InputStreamProviderDataSource;

/**
 * Implementation of {@link IEmailAttachmentDataSource}
 *
 * @author Philip Helger
 */
public class EmailAttachmentDataSource extends InputStreamProviderDataSource implements IEmailAttachmentDataSource
{
  private final EEmailAttachmentDisposition m_eDisposition;

  public EmailAttachmentDataSource (@Nonnull final IHasInputStream aISP,
                                    @Nonnull final String sFilename,
                                    @Nullable final String sContentType,
                                    @Nonnull final EEmailAttachmentDisposition eDisposition)
  {
    super (aISP, sFilename, sContentType);
    m_eDisposition = ValueEnforcer.notNull (eDisposition, "Disposition");
  }

  @Nonnull
  public EEmailAttachmentDisposition getDisposition ()
  {
    return m_eDisposition;
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("disposition", m_eDisposition).getToString ();
  }
}
