/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UnsupportedOperation;
import com.helger.commons.io.IHasInputStream;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.string.ToStringGenerator;

/**
 * A special {@link DataSource} implementation based on data from
 * {@link InputStream}.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class InputStreamDataSource implements IExtendedDataSource, IHasInputStream
{
  public static final boolean DEFAULT_READ_MULTIPLE = false;

  private final InputStream m_aIS;
  private int m_nISAcquired = 0;
  private final String m_sName;
  private final String m_sContentType;
  private final boolean m_bReadMultiple;

  public InputStreamDataSource (@Nonnull final InputStream aIS, @Nonnull final String sName)
  {
    this (aIS, sName, (String) null, DEFAULT_READ_MULTIPLE);
  }

  public InputStreamDataSource (@Nonnull final InputStream aIS,
                                @Nonnull final String sName,
                                final boolean bReadMultiple)
  {
    this (aIS, sName, (String) null, bReadMultiple);
  }

  public InputStreamDataSource (@Nonnull final InputStream aIS,
                                @Nonnull final String sName,
                                @Nullable final IMimeType aContentType,
                                final boolean bReadMultiple)
  {
    this (aIS, sName, aContentType == null ? null : aContentType.getAsString (), bReadMultiple);
  }

  public InputStreamDataSource (@Nonnull final InputStream aIS,
                                @Nonnull final String sName,
                                @Nullable final IMimeType aContentType)
  {
    this (aIS, sName, aContentType == null ? null : aContentType.getAsString (), DEFAULT_READ_MULTIPLE);
  }

  public InputStreamDataSource (@Nonnull final InputStream aIS,
                                @Nonnull final String sName,
                                @Nullable final String sContentType)
  {
    this (aIS, sName, sContentType, DEFAULT_READ_MULTIPLE);
  }

  public InputStreamDataSource (@Nonnull final InputStream aIS,
                                @Nonnull final String sName,
                                @Nullable final String sContentType,
                                final boolean bReadMultiple)
  {
    m_aIS = ValueEnforcer.notNull (aIS, "InputStream");
    m_sName = ValueEnforcer.notNull (sName, "Name");
    m_sContentType = sContentType != null ? sContentType : DEFAULT_CONTENT_TYPE.getAsString ();
    m_bReadMultiple = bReadMultiple;
  }

  public final boolean isReadMultiple ()
  {
    return m_bReadMultiple;
  }

  /**
   * @return How often the input stream was already acquired. Always &ge; 0.
   */
  @Nonnegative
  public final int getISAcquisitionCount ()
  {
    return m_nISAcquired;
  }

  @Nonnull
  public InputStream getInputStream ()
  {
    m_nISAcquired++;
    if (!m_bReadMultiple && m_nISAcquired > 1)
      throw new IllegalStateException ("The input stream was already acquired " + (m_nISAcquired - 1) + " times!");
    return m_aIS;
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
    return new ToStringGenerator (this).append ("IS", m_aIS)
                                       .append ("ISAcquired", m_nISAcquired)
                                       .append ("Name", m_sName)
                                       .append ("ContentType", m_sContentType)
                                       .append ("ReadMultiple", m_bReadMultiple)
                                       .getToString ();
  }
}
