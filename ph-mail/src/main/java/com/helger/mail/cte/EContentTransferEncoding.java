/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.helger.mail.cte;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.codec.Base64Codec;
import com.helger.commons.codec.QuotedPrintableCodec;
import com.helger.commons.codec.RFC1522QCodec;
import com.helger.commons.lang.EnumHelper;

/**
 * Predefined Content Transfer Encoding types as per
 * https://www.ietf.org/rfc/rfc2045.txt section 6.1.<br>
 * Of course additional transfer encodings can be used.
 * <p>
 * Three transformations are currently defined: identity, the "quoted-
 * printable" encoding, and the "base64" encoding. The domains are "binary",
 * "8bit" and "7bit".
 * <p>
 * The Content-Transfer-Encoding values "7bit", "8bit", and "binary" all mean
 * that the identity (i.e. NO) encoding transformation has been performed. As
 * such, they serve simply as indicators of the domain of the body data, and
 * provide useful information about the sort of encoding that might be needed
 * for transmission in a given transport system.
 *
 * @author Philip Helger
 */
public enum EContentTransferEncoding implements IContentTransferEncoding
{
  /**
   * "7bit data" refers to data that is all represented as relatively short
   * lines with 998 octets or less between CRLF line separation sequences
   * [RFC-821]. No octets with decimal values greater than 127 are allowed and
   * neither are NULs (octets with decimal value 0). CR (decimal value 13) and
   * LF (decimal value 10) octets only occur as part of CRLF line separation
   * sequences.
   */
  _7BIT ("7bit")
  {
    @Override
    public IdentityByteArrayCodec createCodec ()
    {
      // Identity codec
      return IdentityByteArrayCodec.INSTANCE;
    }
  },
  /**
   * "8bit data" refers to data that is all represented as relatively short
   * lines with 998 octets or less between CRLF line separation sequences
   * [RFC-821]), but octets with decimal values greater than 127 may be used. As
   * with "7bit data" CR and LF octets only occur as part of CRLF line
   * separation sequences and no NULs are allowed.
   */
  _8BIT ("8bit")
  {
    @Override
    public IdentityByteArrayCodec createCodec ()
    {
      // Identity codec
      return IdentityByteArrayCodec.INSTANCE;
    }
  },
  /**
   * "Binary data" refers to data where any sequence of octets whatsoever is
   * allowed.
   */
  BINARY ("binary")
  {
    @Override
    public IdentityByteArrayCodec createCodec ()
    {
      // Identity codec
      return IdentityByteArrayCodec.INSTANCE;
    }
  },
  QUOTED_PRINTABLE ("quoted-printable")
  {
    @Override
    public QuotedPrintableCodec createCodec ()
    {
      return new QuotedPrintableCodec (RFC1522QCodec.getAllPrintableChars ());
    }
  },
  BASE64 ("base64")
  {
    @Override
    public Base64Codec createCodec ()
    {
      return new Base64Codec ();
    }
  };

  /** AS2 default CTE is "binary" */
  public static final EContentTransferEncoding AS2_DEFAULT = BINARY;

  private final String m_sID;

  private EContentTransferEncoding (@Nonnull @Nonempty final String sID)
  {
    m_sID = sID;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nullable
  public static EContentTransferEncoding getFromIDCaseInsensitiveOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDCaseInsensitiveOrNull (EContentTransferEncoding.class, sID);
  }

  @Nullable
  public static EContentTransferEncoding getFromIDCaseInsensitiveOrDefault (@Nullable final String sID,
                                                                            @Nullable final EContentTransferEncoding eDefault)
  {
    return EnumHelper.getFromIDCaseInsensitiveOrDefault (EContentTransferEncoding.class, sID, eDefault);
  }
}
