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
package com.helger.jsch.scp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

public class ScpFile
{
  private final EDestinationOS m_eOS;
  private final String [] m_aPath;
  private final ISessionFactory m_aSessionFactory;

  public ScpFile (@Nonnull final ISessionFactory aSessionFactory, @Nonnull final EDestinationOS eOS, final String... aPath)
  {
    ValueEnforcer.notNull (aSessionFactory, "SessionFactory");
    ValueEnforcer.notNull (eOS, "OS");

    m_aSessionFactory = aSessionFactory;
    m_eOS = eOS;
    m_aPath = aPath;
  }

  public void copyFrom (final File file) throws IOException, JSchException
  {
    copyFrom (file, null);
  }

  public void copyFrom (@Nonnull final File file, @Nullable final String mode) throws IOException, JSchException
  {
    try (final FileInputStream from = new FileInputStream (file);
         final ScpFileOutputStream to = mode == null ? getOutputStream (file.length ()) : getOutputStream (file.length (), mode))
    {
      StreamHelper.copyInputStreamToOutputStream (from, to);
    }
  }

  public void copyTo (@Nonnull final File aFile) throws JSchException, IOException
  {
    try (final ScpFileInputStream from = getInputStream ())
    {
      final String name = from.getName ();
      final String mode = from.getMode ();
      File file = aFile;
      if (file.isDirectory ())
      {
        file = new File (file, name);
      }
      // attempt to set file mode... flakey in java 6 and below
      final int userPerm = Character.getNumericValue (mode.charAt (1));
      final int otherPerm = Character.getNumericValue (mode.charAt (3));
      if ((userPerm & 1) == 1)
      {
        if ((otherPerm & 1) == 1)
          file.setExecutable (true, false);
        else
          file.setExecutable (true, true);
      }
      if ((userPerm & 2) == 2)
      {
        if ((otherPerm & 2) == 2)
          file.setWritable (true, false);
        else
          file.setWritable (true, true);
      }
      if ((userPerm & 4) == 4)
      {
        if ((otherPerm & 4) == 4)
          file.setReadable (true, false);
        else
          file.setReadable (true, true);
      }

      try (final FileOutputStream to = new FileOutputStream (file))
      {
        StreamHelper.copyInputStreamToOutputStream (from, to);
      }
    }
  }

  public void copyTo (final ScpFile file) throws JSchException, IOException
  {
    try (final ScpFileInputStream from = getInputStream ())
    {
      final String mode = from.getMode ();
      final long size = from.getSize ();
      try (final ScpFileOutputStream to = file.getOutputStream (size, mode))
      {
        StreamHelper.copyInputStreamToOutputStream (from, to);
      }
    }
  }

  public ScpFileInputStream getInputStream () throws JSchException, IOException
  {
    return new ScpFileInputStream (m_aSessionFactory, getPath ());
  }

  private ScpFileOutputStream _getOutputStream (final ScpEntry scpEntry) throws JSchException, IOException
  {
    return new ScpFileOutputStream (m_aSessionFactory, getDirectory (), scpEntry);
  }

  public ScpFileOutputStream getOutputStream (final long size) throws JSchException, IOException
  {
    return _getOutputStream (ScpEntry.newFile (getFilename (), size));
  }

  public ScpFileOutputStream getOutputStream (final long size, @Nullable final String mode) throws JSchException, IOException
  {
    return _getOutputStream (ScpEntry.newFile (getFilename (), size, mode));
  }

  String getDirectory ()
  {
    return m_eOS.joinPath (m_aPath, 0, m_aPath.length - 1);
  }

  String getFilename ()
  {
    return m_aPath[m_aPath.length - 1];
  }

  String getPath ()
  {
    return m_eOS.joinPath (m_aPath);
  }

  @Nonnull
  public static ScpFile forUnix (@Nonnull final ISessionFactory aSessionFactory, final String... aPath)
  {
    return new ScpFile (aSessionFactory, EDestinationOS.UNIX, aPath);
  }
}
