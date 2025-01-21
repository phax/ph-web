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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum EDestinationOS
{
  UNIX ('/'),
  WINDOWS ('\\');

  private final char m_cSep;

  EDestinationOS (final char separator)
  {
    m_cSep = separator;
  }

  @Nonnull
  public String joinPath (@Nonnull final String [] aParts)
  {
    return joinPath (aParts, 0, aParts.length);
  }

  @Nonnull
  public String joinPath (@Nonnull final String [] aParts, @Nonnegative final int nOfs, @Nonnegative final int nLen)
  {
    final StringBuilder aSB = new StringBuilder ();
    for (int i = nOfs, end = nOfs + nLen; i < end; i++)
    {
      if (i > nOfs)
        aSB.append (m_cSep);
      aSB.append (aParts[i]);
    }
    return aSB.toString ();
  }
}
