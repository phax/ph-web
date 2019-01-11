/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.web.encoding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.codec.IEncoder;

/**
 * Heuristic Android filename encoder based on <a href=
 * "http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http/6745788#6745788"
 * >Stack Overflow</a>
 *
 * @author Philip Helger
 */
public class AndroidFilenameEncoder implements IEncoder <String, String>
{
  public static final char DEFAULT_REPLACEMENT_CHAR = '_';
  private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ._-+,@£$€!½§~'=()[]{}0123456789";

  @Nonnull
  public static String getSafeAndroidFilename (@Nonnull final String sSrc)
  {
    return getSafeAndroidFilename (sSrc, DEFAULT_REPLACEMENT_CHAR);
  }

  @Nonnull
  public static String getSafeAndroidFilename (@Nonnull final String sSrc, final char cReplacement)
  {
    ValueEnforcer.notNull (sSrc, "Src");
    if (ALLOWED_CHARS.indexOf (cReplacement) < 0)
      throw new IllegalArgumentException ("Passed character is an invalid replacement char: " + cReplacement);

    final StringBuilder aSB = new StringBuilder (sSrc.length () * 2);
    for (final char c : sSrc.toCharArray ())
      if (ALLOWED_CHARS.indexOf (c) >= 0)
        aSB.append (c);
      else
        aSB.append (cReplacement);
    return aSB.toString ();
  }

  @Nullable
  public String getEncoded (@Nullable final String sSrc)
  {
    if (sSrc == null)
      return null;
    return getSafeAndroidFilename (sSrc);
  }
}
