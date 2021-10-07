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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.file.FileOperationManager;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.StreamHelper;
import com.jcraft.jsch.Session;

public final class ScpStreamTest extends AbstractScpTestBase
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ScpStreamTest.class);

  private File dir1;
  private String dir1Name;
  private File dir2;
  private String dir2Name;
  private final String expected1 = "This is a test";
  private final String expected2 = "This is only a test";
  private final String expected3 = "Of the Emergency Broadcast System";
  private File file1;
  private String file1Name;
  private File file2;
  private String file2Name;
  private File file3;
  private String file3Name;
  private Session session;

  @After
  public void after ()
  {
    if (session != null && session.isConnected ())
    {
      session.disconnect ();
    }

    FileOperationManager.INSTANCE.deleteFileIfExisting (file3);
    FileOperationManager.INSTANCE.deleteFileIfExisting (file2);
    FileOperationManager.INSTANCE.deleteFileIfExisting (file1);
    FileOperationManager.INSTANCE.deleteDirIfExisting (dir2);
    FileOperationManager.INSTANCE.deleteDirIfExisting (dir1);
  }

  @Before
  public void before ()
  {
    dir1Name = UUID.randomUUID ().toString ();
    dir2Name = "dir";

    dir1 = new File (filesystemPath, dir1Name);
    dir2 = new File (dir1, dir2Name);
    assertTrue (dir2.mkdirs ());
    LOGGER.info ((dir2.exists () ? "succesfully" : "failed to") + " created dir " + dir2);

    file1Name = "file1.txt";
    file1 = new File (dir1, file1Name);
    file2Name = "file2.txt";
    file2 = new File (dir2, file2Name);
    file3Name = "file3.txt";
    file3 = new File (dir1, file3Name);

    try
    {
      session = sessionFactory.newSession ();
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed to initialize session from factory " + sessionFactory.getAsString (), e);
      fail (e.getMessage ());
    }
  }

  private String joinPath (final List <String> dirs, final String file)
  {
    final int fileIndex = dirs.size ();
    final String [] parts = dirs.toArray (new String [fileIndex + 1]);
    parts[fileIndex] = file;
    return joinPath (parts);
  }

  private String joinPath (final String... parts)
  {
    final StringBuilder builder = new StringBuilder ();
    for (int i = 0; i < parts.length; i++)
    {
      if (i > 0)
      {
        builder.append ("/");
      }
      builder.append (parts[i]);
    }
    return builder.toString ();
  }

  @Test
  public void testInputStream ()
  {
    SimpleFileIO.writeFile (file1, expected1, StandardCharsets.UTF_8);
    SimpleFileIO.writeFile (file2, expected2, StandardCharsets.UTF_8);
    SimpleFileIO.writeFile (file3, expected3, StandardCharsets.UTF_8);

    try (ScpInputStream inputStream = new ScpInputStream (sessionFactory, joinPath (scpPath, dir1Name, "*"), ECopyMode.RECURSIVE))
    {
      final Map <String, String> fileNameToContents = new HashMap <> ();
      final List <String> dirs = new ArrayList <> ();
      while (true)
      {
        final ScpEntry entry = inputStream.getNextEntry ();
        if (entry == null)
          break;
        if (entry.isDirectory ())
        {
          dirs.add (entry.getName ());
        }
        else
          if (entry.isEndOfDirectory ())
          {
            dirs.remove (dirs.size () - 1);
          }
        if (entry.isFile ())
        {
          final String path = joinPath (dirs, entry.getName ());
          final String data = StreamHelper.getAllBytesAsString (inputStream, StandardCharsets.UTF_8);
          fileNameToContents.put (path, data);
        }
      }

      Assert.assertEquals (expected1, fileNameToContents.get (file1Name));
      Assert.assertEquals (expected2, fileNameToContents.get (joinPath (dir2Name, file2Name)));
      Assert.assertEquals (expected3, fileNameToContents.get (file3Name));
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed to write to ScpInputStream:", e);
      Assert.fail (e.getMessage ());
    }
  }

  @Test
  public void testOutputStream ()
  {
    try (final ScpOutputStream outputStream = new ScpOutputStream (sessionFactory, joinPath (scpPath, dir1Name), ECopyMode.RECURSIVE))
    {
      outputStream.putNextEntry (file1Name, expected1.length ());
      outputStream.write (expected1.getBytes (StandardCharsets.UTF_8));
      outputStream.closeEntry ();

      outputStream.putNextEntry (dir2Name);

      outputStream.putNextEntry (file2Name, expected2.length ());
      outputStream.write (expected2.getBytes (StandardCharsets.UTF_8));
      outputStream.closeEntry ();

      // instead of outputStream.closeEntry() lets try this:
      outputStream.putNextEntry (ScpEntry.newEndOfDirectory ());

      outputStream.putNextEntry (file3Name, expected3.length ());
      outputStream.write (expected3.getBytes (StandardCharsets.UTF_8));
      outputStream.closeEntry ();
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed to write to ScpOutputStream:", e);
      Assert.fail (e.getMessage ());
    }

    try
    {
      Assert.assertTrue (file1.exists ());
      Assert.assertEquals (expected1, SimpleFileIO.getFileAsString (file1, StandardCharsets.UTF_8));
      Assert.assertTrue (file2.exists ());
      Assert.assertEquals (expected2, SimpleFileIO.getFileAsString (file2, StandardCharsets.UTF_8));
      Assert.assertTrue (file3.exists ());
      Assert.assertEquals (expected3, SimpleFileIO.getFileAsString (file3, StandardCharsets.UTF_8));
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed to read file contents:", e);
      Assert.fail (e.getMessage ());
    }
  }
}
