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
import java.io.OutputStream;

import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

public class ScpFileOutputStream extends OutputStream
{
  private final ScpOutputStream m_aOS;

  ScpFileOutputStream (final ISessionFactory sessionFactory,
                       final String directory,
                       final ScpEntry scpEntry) throws JSchException, IOException
  {
    m_aOS = new ScpOutputStream (sessionFactory, directory, ECopyMode.FILE_ONLY);
    m_aOS.putNextEntry (scpEntry);
  }

  @Override
  public void close () throws IOException
  {
    m_aOS.closeEntry ();
    m_aOS.close ();
  }

  @Override
  public void write (final int b) throws IOException
  {
    m_aOS.write (b);
  }

  @Override
  public void write (final byte [] aBuf, final int nOfs, final int nLen) throws IOException
  {
    m_aOS.write (aBuf, nOfs, nLen);
  }
}
