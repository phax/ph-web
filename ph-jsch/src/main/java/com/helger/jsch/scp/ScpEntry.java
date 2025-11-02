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

import java.io.IOException;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;

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

  @NonNull
  @Nonempty
  private static String _standardizeMode (@NonNull final String sMode) throws IOException
  {
    if (!MODE_PATTERN.matcher (sMode).matches ())
      throw new IOException ("invalid file mode '" + sMode + "'");

    if (sMode.length () == 3)
      return "0" + sMode;
    return sMode;
  }

  private ScpEntry (@Nullable final String sName, final long nSize, @Nullable final String sMode, final EType eType) throws IOException
  {
    m_sName = sName;
    m_nSize = nSize;
    m_sMode = eType == EType.END_OF_DIRECTORY ? null : _standardizeMode (sMode);
    m_eType = eType;
  }

  @Nullable
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

  @NonNull
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

  @NonNull
  public static ScpEntry newDirectory (final String sName) throws IOException
  {
    return newDirectory (sName, DEFAULT_DIRECTORY_MODE);
  }

  @NonNull
  public static ScpEntry newDirectory (final String sName, @Nullable final String sMode) throws IOException
  {
    return new ScpEntry (sName, 0L, sMode, EType.DIRECTORY);
  }

  @NonNull
  public static ScpEntry newEndOfDirectory () throws IOException
  {
    return new ScpEntry (null, 0L, null, EType.END_OF_DIRECTORY);
  }

  @NonNull
  public static ScpEntry newFile (final String sName, final long nSize) throws IOException
  {
    return newFile (sName, nSize, DEFAULT_FILE_MODE);
  }

  @NonNull
  public static ScpEntry newFile (final String sName, final long nSize, @Nullable final String sMode) throws IOException
  {
    return new ScpEntry (sName, nSize, sMode, EType.FILE);
  }
}
