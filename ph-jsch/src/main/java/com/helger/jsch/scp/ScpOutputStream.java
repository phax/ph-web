/**
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
package com.helger.jsch.scp;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

/**
 * Based upon information found
 * <a href="https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works"
 * >here</a>.
 *
 * @author ltheisen
 */
public class ScpOutputStream extends OutputStream
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpOutputStream.class);

  private final ScpConnection m_aConnection;
  private OutputStream m_aOS;

  public ScpOutputStream (final ISessionFactory sessionFactory, final String path, final ECopyMode copyMode) throws JSchException,
                                                                                                             IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Opening ScpOutputStream to " + sessionFactory.getAsString () + " " + path);
    m_aConnection = new ScpConnection (sessionFactory, path, EScpMode.TO, copyMode);
  }

  @Override
  public void close () throws IOException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Closing ScpOutputStream");
    m_aConnection.close ();
    m_aOS = null;
  }

  public void closeEntry () throws IOException
  {
    m_aConnection.closeEntry ();
    m_aOS = null;
  }

  public void putNextEntry (final String name) throws IOException
  {
    m_aConnection.putNextEntry (ScpEntry.newDirectory (name));
    m_aOS = m_aConnection.getCurrentOuputStream ();
  }

  public void putNextEntry (final String name, final long size) throws IOException
  {
    m_aConnection.putNextEntry (ScpEntry.newFile (name, size));
    m_aOS = m_aConnection.getCurrentOuputStream ();
  }

  public void putNextEntry (final ScpEntry entry) throws IOException
  {
    m_aConnection.putNextEntry (entry);
    m_aOS = m_aConnection.getCurrentOuputStream ();
  }

  @Override
  public void write (final int b) throws IOException
  {
    if (m_aOS == null)
      throw new IllegalStateException ("no current entry, cannot write");
    m_aOS.write (b);
  }

  @Override
  public void write (final byte [] aBuf, final int nOfs, final int nLen) throws IOException
  {
    if (m_aOS == null)
      throw new IllegalStateException ("no current entry, cannot write");
    m_aOS.write (aBuf, nOfs, nLen);
  }
}
