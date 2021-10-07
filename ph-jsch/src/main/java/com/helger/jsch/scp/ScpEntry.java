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

import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;

public class ScpEntry
{
  public enum EType
  {
    FILE,
    DIRECTORY,
    END_OF_DIRECTORY
  }

  private static final String DEFAULT_DIRECTORY_MODE = "0750";
  private static final String DEFAULT_FILE_MODE = "0640";
  private static final Pattern MODE_PATTERN = Pattern.compile ("[0-2]?[0-7]{3}");

  private final String m_sMode;
  private final String m_sName;
  private final long m_nSize;
  private final EType m_eType;

  @Nonnull
  @Nonempty
  private static String _standardizeMode (final String mode) throws IOException
  {
    if (!MODE_PATTERN.matcher (mode).matches ())
      throw new IOException ("invalid file mode " + mode);

    if (mode.length () == 3)
      return "0" + mode;
    return mode;
  }

  private ScpEntry (final String name, final long size, final String mode, final EType type) throws IOException
  {
    m_sName = name;
    m_nSize = size;
    m_sMode = type == EType.END_OF_DIRECTORY ? null : _standardizeMode (mode);
    m_eType = type;
  }

  public String getMode ()
  {
    return m_sMode;
  }

  public String getName ()
  {
    return m_sName;
  }

  public long getSize ()
  {
    return m_nSize;
  }

  public boolean isDirectory ()
  {
    return m_eType == EType.DIRECTORY;
  }

  public boolean isEndOfDirectory ()
  {
    return m_eType == EType.END_OF_DIRECTORY;
  }

  public boolean isFile ()
  {
    return m_eType == EType.FILE;
  }

  @Nonnull
  @Nonempty
  public String getAsString ()
  {
    switch (m_eType)
    {
      case FILE:
        return "C" + m_sMode + " " + m_nSize + " " + m_sName;
      case DIRECTORY:
        return "D" + m_sMode + " " + m_nSize + " " + m_sName;
      case END_OF_DIRECTORY:
        return "E";
      default:
        throw new IllegalStateException ("Unhandled type");
    }
  }

  @Nonnull
  public static ScpEntry newDirectory (final String name) throws IOException
  {
    return newDirectory (name, DEFAULT_DIRECTORY_MODE);
  }

  @Nonnull
  public static ScpEntry newDirectory (final String name, final String mode) throws IOException
  {
    return new ScpEntry (name, 0L, mode, EType.DIRECTORY);
  }

  @Nonnull
  public static ScpEntry newEndOfDirectory () throws IOException
  {
    return new ScpEntry (null, 0L, null, EType.END_OF_DIRECTORY);
  }

  @Nonnull
  public static ScpEntry newFile (final String name, final long size) throws IOException
  {
    return newFile (name, size, DEFAULT_FILE_MODE);
  }

  @Nonnull
  public static ScpEntry newFile (final String name, final long size, final String mode) throws IOException
  {
    return new ScpEntry (name, size, mode, EType.FILE);
  }
}
