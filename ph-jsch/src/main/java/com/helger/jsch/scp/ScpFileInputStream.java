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
package com.helger.jsch.scp;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

public class ScpFileInputStream extends InputStream
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpFileInputStream.class);

  private final ScpInputStream m_aIS;
  private final ScpEntry m_aScpEntry;

  ScpFileInputStream (@Nonnull final ISessionFactory sessionFactory, final String path) throws JSchException, IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Opening ScpInputStream to " + sessionFactory.getAsString () + " " + path);
    m_aIS = new ScpInputStream (sessionFactory, path, ECopyMode.FILE_ONLY);
    m_aScpEntry = m_aIS.getNextEntry ();
  }

  public String getMode ()
  {
    return m_aScpEntry.getMode ();
  }

  public String getName ()
  {
    return m_aScpEntry.getName ();
  }

  public long getSize ()
  {
    return m_aScpEntry.getSize ();
  }

  @Override
  public void close () throws IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Closing ScpInputStream");
    m_aIS.closeEntry ();
    m_aIS.close ();
  }

  @Override
  public int read () throws IOException
  {
    return m_aIS.read ();
  }

  @Override
  public int read (final byte [] aBuf, final int nOfs, final int nLen) throws IOException
  {
    return m_aIS.read (aBuf, nOfs, nLen);
  }
}
