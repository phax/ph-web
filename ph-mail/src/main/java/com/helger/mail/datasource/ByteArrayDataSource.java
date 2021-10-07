/*
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.string.ToStringGenerator;

/**
 * A DataSource implementation based on a byte array
 *
 * @author Philip Helger
 */
public class ByteArrayDataSource implements IExtendedDataSource
{
  private class WrappedOutputStream extends NonBlockingByteArrayOutputStream
  {
    @Override
    public void close ()
    {
      super.close ();
      ByteArrayDataSource.this.m_aBytes = toByteArray ();
    }
  }

  private final String m_sContentType;
  private byte [] m_aBytes;
  private final String m_sName;

  public ByteArrayDataSource (@Nonnull final byte [] aBytes, @Nullable final String sContentType, @Nullable final String sName)
  {
    ValueEnforcer.notNull (aBytes, "Bytes");
    m_aBytes = aBytes;
    m_sContentType = sContentType == null ? DEFAULT_CONTENT_TYPE.getAsString () : sContentType;
    m_sName = sName;
  }

  @Nonnull
  @ReturnsMutableObject
  public byte [] directGetBytes ()
  {
    return m_aBytes;
  }

  @Nonnull
  public NonBlockingByteArrayInputStream getInputStream ()
  {
    return new NonBlockingByteArrayInputStream (m_aBytes);
  }

  @Nonnull
  public NonBlockingByteArrayOutputStream getOutputStream () throws IOException
  {
    return new WrappedOutputStream ();
  }

  @Nonnull
  public String getContentType ()
  {
    return m_sContentType;
  }

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("byte#", ArrayHelper.getSize (m_aBytes))
                                       .append ("Name", m_sName)
                                       .append ("ContentType", m_sContentType)
                                       .getToString ();
  }
}
