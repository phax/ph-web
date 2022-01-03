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
package com.helger.jsch.scp;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

public class ScpInputStream extends InputStream
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpInputStream.class);

  private final ScpConnection m_aConnection;
  private InputStream m_aIS;

  public ScpInputStream (@Nonnull final ISessionFactory aSessionFactory, final String path, final ECopyMode copyMode) throws JSchException,
                                                                                                                      IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Opening ScpInputStream");
    m_aConnection = new ScpConnection (aSessionFactory, path, EScpMode.FROM, copyMode);
  }

  @Override
  public void close () throws IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Closing ScpInputStream");
    m_aConnection.close ();
    m_aIS = null;
  }

  public void closeEntry () throws IOException
  {
    m_aConnection.closeEntry ();
    m_aIS = null;
  }

  @Nullable
  public ScpEntry getNextEntry () throws IOException
  {
    final ScpEntry entry = m_aConnection.getNextEntry ();
    m_aIS = m_aConnection.getCurrentInputStream ();
    return entry;
  }

  @Override
  public int read () throws IOException
  {
    if (m_aIS == null)
      throw new IllegalStateException ("no current entry, cannot read");
    return m_aIS.read ();
  }

  @Override
  public int read (@Nonnull final byte [] aBuf, @Nonnegative final int nOfs, @Nonnegative final int nLen) throws IOException
  {
    if (m_aIS == null)
      throw new IllegalStateException ("no current entry, cannot read");
    return m_aIS.read (aBuf, nOfs, nLen);
  }
}
