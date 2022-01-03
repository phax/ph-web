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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.file.FileOperationManager;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.StreamHelper;

public final class ScpFileTest extends AbstractScpTestBase
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpFileTest.class);

  private File dir;
  private final String expected = "Oh well, lets just use a different string...";
  private File file;
  private String filename;
  private String rootDir;

  @After
  public void after ()
  {
    FileOperationManager.INSTANCE.deleteFileIfExisting (file);
    FileOperationManager.INSTANCE.deleteDirIfExisting (dir);
  }

  @Before
  public void before ()
  {
    rootDir = UUID.randomUUID ().toString ();

    dir = new File (s_sFileSystemPath, rootDir);
    assertTrue (dir.mkdirs ());
    LOGGER.info ((dir.exists () ? "succesfully created" : "failed to create") + " dir " + dir);
    filename = UUID.randomUUID ().toString () + ".txt";
    file = new File (dir, filename);
  }

  @Test
  public void testCopyFromFile ()
  {
    final String toFilename = "actual.txt";
    final File toFile = new File (dir, toFilename);
    try
    {
      SimpleFileIO.writeFile (file, expected, StandardCharsets.UTF_8);
      final ScpFile to = ScpFile.forUnix (s_aSessionFactory, s_sScpPath, rootDir, toFilename);
      to.copyFrom (file);
      final String actual = SimpleFileIO.getFileAsString (toFile, StandardCharsets.UTF_8);
      assertEquals (expected, actual);
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for " + filename, e);
      fail (e.getMessage ());
    }
    finally
    {
      FileOperationManager.INSTANCE.deleteFileIfExisting (toFile);
    }
  }

  @Test
  public void testCopyToFile ()
  {
    final String fromFilename = "expected.txt";
    final File fromFile = new File (dir, fromFilename);
    try
    {
      SimpleFileIO.writeFile (fromFile, expected, StandardCharsets.UTF_8);
      final ScpFile from = ScpFile.forUnix (s_aSessionFactory, s_sScpPath, rootDir, fromFilename);
      from.copyTo (file);
      final String actual = SimpleFileIO.getFileAsString (file, StandardCharsets.UTF_8);
      assertEquals (expected, actual);
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for " + filename, e);
      fail (e.getMessage ());
    }
    finally
    {
      FileOperationManager.INSTANCE.deleteFileIfExisting (fromFile);
    }
  }

  @Test
  public void testCopyToScpFile ()
  {
    final String fromFilename = "expected.txt";
    final File fromFile = new File (dir, fromFilename);
    try
    {
      SimpleFileIO.writeFile (fromFile, expected, StandardCharsets.UTF_8);
      final ScpFile from = ScpFile.forUnix (s_aSessionFactory, s_sScpPath, rootDir, fromFilename);
      final ScpFile to = ScpFile.forUnix (s_aSessionFactory, s_sScpPath, rootDir, filename);
      from.copyTo (to);
      final String actual = SimpleFileIO.getFileAsString (file, StandardCharsets.UTF_8);
      assertEquals (expected, actual);
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for" + filename, e);
      fail (e.getMessage ());
    }
    finally
    {
      FileOperationManager.INSTANCE.deleteFileIfExisting (fromFile);
    }
  }

  @Test
  public void testGetInputStream ()
  {
    try
    {
      SimpleFileIO.writeFile (file, expected, StandardCharsets.UTF_8);
      final ScpFile scpFile = ScpFile.forUnix (s_aSessionFactory, s_sScpPath, rootDir, filename);
      try (ScpFileInputStream scpFileInputStream = scpFile.getInputStream ())
      {
        final String actual = StreamHelper.getAllBytesAsString (scpFileInputStream, StandardCharsets.UTF_8);

        assertEquals (expected, actual);
      }
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for " + filename, e);
      fail (e.getMessage ());
    }
  }

  @Test
  public void testGetOutputStream ()
  {
    try
    {
      final ScpFile scpFile = ScpFile.forUnix (s_aSessionFactory, s_sScpPath, rootDir, filename);
      try (ScpFileOutputStream outputStream = scpFile.getOutputStream (expected.length ()))
      {
        StreamHelper.writeStream (outputStream, expected, StandardCharsets.UTF_8);
      }

      final String actual = SimpleFileIO.getFileAsString (file, StandardCharsets.UTF_8);
      assertEquals (expected, actual);
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for " + filename, e);
      fail (e.getMessage ());
    }
  }
}
