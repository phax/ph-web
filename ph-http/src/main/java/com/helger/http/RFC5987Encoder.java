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
package com.helger.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.codec.IEncoder;
import com.helger.commons.serialize.convert.SerializationConverter;
import com.helger.commons.string.StringHelper;

/**
 * RFC 5987 Encoder. Character Set and Language Encoding for Hypertext Transfer
 * Protocol (HTTP) Header Field Parameters
 *
 * @author Philip Helger
 */
public class RFC5987Encoder implements IEncoder <String, String>
{
  public static final char ESCAPE_CHAR = '%';
  // Order is important for binary search!
  private static final byte [] ALLOWED_BYTES = { '!',
                                                 '#',
                                                 '$',
                                                 '&',
                                                 '+',
                                                 '-',
                                                 '.',
                                                 '0',
                                                 '1',
                                                 '2',
                                                 '3',
                                                 '4',
                                                 '5',
                                                 '6',
                                                 '7',
                                                 '8',
                                                 '9',
                                                 'A',
                                                 'B',
                                                 'C',
                                                 'D',
                                                 'E',
                                                 'F',
                                                 'G',
                                                 'H',
                                                 'I',
                                                 'J',
                                                 'K',
                                                 'L',
                                                 'M',
                                                 'N',
                                                 'O',
                                                 'P',
                                                 'Q',
                                                 'R',
                                                 'S',
                                                 'T',
                                                 'U',
                                                 'V',
                                                 'W',
                                                 'X',
                                                 'Y',
                                                 'Z',
                                                 '^',
                                                 '_',
                                                 '`',
                                                 'a',
                                                 'b',
                                                 'c',
                                                 'd',
                                                 'e',
                                                 'f',
                                                 'g',
                                                 'h',
                                                 'i',
                                                 'j',
                                                 'k',
                                                 'l',
                                                 'm',
                                                 'n',
                                                 'o',
                                                 'p',
                                                 'q',
                                                 'r',
                                                 's',
                                                 't',
                                                 'u',
                                                 'v',
                                                 'w',
                                                 'x',
                                                 'y',
                                                 'z',
                                                 '|',
                                                 '~' };
  private transient Charset m_aCharset;

  public RFC5987Encoder ()
  {
    this (StandardCharsets.UTF_8);
  }

  public RFC5987Encoder (@Nonnull final Charset aCharset)
  {
    m_aCharset = ValueEnforcer.notNull (aCharset, "Charset");
  }

  private void writeObject (@Nonnull final ObjectOutputStream aOOS) throws IOException
  {
    aOOS.defaultWriteObject ();
    SerializationConverter.writeConvertedObject (m_aCharset, aOOS);
  }

  private void readObject (@Nonnull final ObjectInputStream aOIS) throws IOException, ClassNotFoundException
  {
    aOIS.defaultReadObject ();
    m_aCharset = SerializationConverter.readConvertedObject (aOIS, Charset.class);
  }

  @Nonnull
  public static String getRFC5987Encoded (@Nonnull final String sSrc, @Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (sSrc, "Src");

    final StringBuilder aSB = new StringBuilder (sSrc.length () * 2);
    for (final byte b : sSrc.getBytes (aCharset))
    {
      if (Arrays.binarySearch (ALLOWED_BYTES, b) >= 0)
        aSB.append ((char) b);
      else
      {
        aSB.append (ESCAPE_CHAR)
           .append (StringHelper.getHexChar ((b >> 4) & 0xf))
           .append (StringHelper.getHexChar (b & 0xf));
      }
    }

    return aSB.toString ();
  }

  @Nonnull
  public static String getRFC5987EncodedUTF8 (@Nonnull final String sSrc)
  {
    return getRFC5987Encoded (sSrc, StandardCharsets.UTF_8);
  }

  @Nullable
  public String getEncoded (@Nullable final String sSrc)
  {
    if (sSrc == null)
      return null;
    return getRFC5987Encoded (sSrc, m_aCharset);
  }
}
