/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.io.stream.StreamHelper;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ScpFile
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpFile.class);

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

  public void copyFrom (@Nonnull final File file) throws IOException, JSchException
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
        final boolean bOwnerOnly = (otherPerm & 1) != 1;
        final boolean bSuccess = file.setExecutable (true, bOwnerOnly);
        if (!bSuccess)
          LOGGER.debug ("Failed to call setExecutable(true, " + bOwnerOnly + ") on " + file);
      }
      if ((userPerm & 2) == 2)
      {
        final boolean bOwnerOnly = (otherPerm & 2) != 2;
        final boolean bSuccess = file.setWritable (true, bOwnerOnly);
        if (!bSuccess)
          LOGGER.debug ("Failed to call setWritable(true, " + bOwnerOnly + ") on " + file);
      }
      if ((userPerm & 4) == 4)
      {
        final boolean bOwnerOnly = (otherPerm & 4) != 4;
        final boolean bSuccess = file.setReadable (true, bOwnerOnly);
        if (!bSuccess)
          LOGGER.debug ("Failed to call setReadable(true, " + bOwnerOnly + ") on " + file);
      }

      try (final FileOutputStream to = new FileOutputStream (file))
      {
        StreamHelper.copyInputStreamToOutputStream (from, to);
      }
    }
  }

  public void copyTo (@Nonnull final ScpFile aFile) throws JSchException, IOException
  {
    try (final ScpFileInputStream aFromIS = getInputStream ())
    {
      final String sMode = aFromIS.getMode ();
      final long nSize = aFromIS.getSize ();
      try (final ScpFileOutputStream aToOS = aFile.getOutputStream (nSize, sMode))
      {
        StreamHelper.copyInputStreamToOutputStream (aFromIS, aToOS);
      }
    }
  }

  @Nonnull
  public ScpFileInputStream getInputStream () throws JSchException, IOException
  {
    return new ScpFileInputStream (m_aSessionFactory, getPath ());
  }

  @Nonnull
  private ScpFileOutputStream _getOutputStream (final ScpEntry scpEntry) throws JSchException, IOException
  {
    return new ScpFileOutputStream (m_aSessionFactory, getDirectory (), scpEntry);
  }

  @Nonnull
  public ScpFileOutputStream getOutputStream (final long size) throws JSchException, IOException
  {
    return _getOutputStream (ScpEntry.newFile (getFilename (), size));
  }

  @Nonnull
  public ScpFileOutputStream getOutputStream (final long size, @Nullable final String mode) throws JSchException, IOException
  {
    return _getOutputStream (ScpEntry.newFile (getFilename (), size, mode));
  }

  @Nonnull
  String getDirectory ()
  {
    return m_eOS.joinPath (m_aPath, 0, m_aPath.length - 1);
  }

  String getFilename ()
  {
    return m_aPath[m_aPath.length - 1];
  }

  @Nonnull
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
