/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.codec.DecodeException;
import com.helger.commons.codec.RFC1522BCodec;
import com.helger.commons.codec.RFC1522QCodec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * RFC 2047 Helper. MIME (Multipurpose Internet Mail Extensions) Part Three:
 * Message Header Extensions for Non-ASCII Text
 *
 * @author Apache Abdera
 */
@Immutable
@SuppressFBWarnings ("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public final class RFC2047Helper
{
  public enum ECodec
  {
    B,
    Q
  }

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  @PresentForCodeCoverage
  private static final RFC2047Helper INSTANCE = new RFC2047Helper ();

  private RFC2047Helper ()
  {}

  @Nullable
  public static String encode (@Nullable final String sValue)
  {
    return encode (sValue, DEFAULT_CHARSET, ECodec.B);
  }

  @Nullable
  public static String encode (@Nullable final String sValue, @Nonnull final Charset aCharset)
  {
    return encode (sValue, aCharset, ECodec.B);
  }

  /**
   * Used to encode a string as specified by RFC 2047
   *
   * @param sValue
   *        The string to encode
   * @param aCharset
   *        The character set to use for the encoding
   * @param eCodec
   *        Codec type
   * @return Encoded String
   */
  @Nullable
  public static String encode (@Nullable final String sValue, @Nonnull final Charset aCharset, final ECodec eCodec)
  {
    if (sValue == null)
      return null;

    try
    {
      switch (eCodec)
      {
        case Q:
          return new RFC1522QCodec (aCharset).getEncoded (sValue);
        case B:
        default:
          return new RFC1522BCodec (aCharset).getEncoded (sValue);
      }
    }
    catch (final Exception ex)
    {
      return sValue;
    }
  }

  /**
   * Used to decode a string as specified by RFC 2047
   *
   * @param sValue
   *        The encoded string
   * @return Decoded String
   */
  @Nullable
  public static String decode (@Nullable final String sValue)
  {
    if (sValue == null)
      return null;

    try
    {
      // try BCodec first
      return new RFC1522BCodec ().getDecoded (sValue);
    }
    catch (final DecodeException de)
    {
      // try QCodec next
      try
      {
        return new RFC1522QCodec ().getDecoded (sValue);
      }
      catch (final Exception ex)
      {
        return sValue;
      }
    }
    catch (final Exception e)
    {
      return sValue;
    }
  }
}
