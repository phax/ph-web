/*
 * Copyright (C) 2016-2022 Philip Helger (www.helger.com)
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
package com.helger.mail.datasource;

import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UnsupportedOperation;
import com.helger.commons.io.IHasInputStream;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.string.ToStringGenerator;

/**
 * A special {@link DataSource} implementation based on data from
 * {@link IHasInputStream}.
 *
 * @author Philip Helger
 */
public class InputStreamProviderDataSource implements IExtendedDataSource
{
  private final IHasInputStream m_aISP;
  private int m_nISAcquired = 0;
  private final String m_sName;
  private final String m_sContentType;

  public InputStreamProviderDataSource (@Nonnull final IHasInputStream aISP, @Nonnull final String sName)
  {
    this (aISP, sName, (String) null);
  }

  public InputStreamProviderDataSource (@Nonnull final IHasInputStream aISP,
                                        @Nonnull final String sName,
                                        @Nullable final IMimeType aContentType)
  {
    this (aISP, sName, aContentType == null ? null : aContentType.getAsString ());
  }

  public InputStreamProviderDataSource (@Nonnull final IHasInputStream aISP,
                                        @Nonnull final String sName,
                                        @Nullable final String sContentType)
  {
    m_aISP = ValueEnforcer.notNull (aISP, "InputStreamProvider");
    m_sName = ValueEnforcer.notNull (sName, "Name");
    m_sContentType = sContentType != null ? sContentType : DEFAULT_CONTENT_TYPE.getAsString ();
  }

  @Nullable
  public InputStream getInputStream ()
  {
    m_nISAcquired++;
    if (!m_aISP.isReadMultiple () && m_nISAcquired > 1)
      throw new IllegalStateException ("The input stream was already acquired " + (m_nISAcquired - 1) + " times!");
    return m_aISP.getInputStream ();
  }

  @UnsupportedOperation
  public OutputStream getOutputStream ()
  {
    throw new UnsupportedOperationException ("Read-only!");
  }

  @Nonnull
  public String getContentType ()
  {
    return m_sContentType;
  }

  @Nonnull
  public String getName ()
  {
    return m_sName;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ISP", m_aISP)
                                       .append ("ISAcquired", m_nISAcquired)
                                       .append ("Name", m_sName)
                                       .append ("ContentType", m_sContentType)
                                       .getToString ();
  }
}
