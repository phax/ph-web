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

public enum EDestinationOS
{
  UNIX ('/'),
  WINDOWS ('\\');

  private char m_cSep;

  private EDestinationOS (final char separator)
  {
    m_cSep = separator;
  }

  public String joinPath (final String [] parts)
  {
    return joinPath (parts, 0, parts.length);
  }

  public String joinPath (final String [] parts, final int start, final int count)
  {
    final StringBuilder builder = new StringBuilder ();
    for (int i = start, end = start + count; i < end; i++)
    {
      if (i > start)
        builder.append (m_cSep);
      builder.append (parts[i]);
    }
    return builder.toString ();
  }
}
