/**
 * Copyright (C) 2016-2018 Philip Helger (www.helger.com)
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
import com.helger.commons.codec.ICodec;
import com.helger.commons.codec.IdentityCodec;
import com.helger.commons.codec.QuotedPrintableCodec;
import com.helger.commons.codec.RFC1522QCodec;
import com.helger.commons.lang.EnumHelper;

/**
 * Predefined Content Transfer Encoding types as per
 * https://www.ietf.org/rfc/rfc2045.txt section 6.1.<br>
 * Of course additional transfer encodings can be used.
 *
 * @author Philip Helger
 */
public enum EContentTransferEncoding implements IContentTransferEncoding
{
  _7BIT ("7bit")
  {
    @Override
    public IdentityCodec <byte []> createDecoder ()
    {
      // Nothing to decode
      return ICodec.identity ();
    }
  },
  _8BIT ("8bit")
  {
    @Override
    public IdentityCodec <byte []> createDecoder ()
    {
      // Nothing to decode
      return ICodec.identity ();
    }
  },
  BINARY ("binary")
  {
    @Override
    public IdentityCodec <byte []> createDecoder ()
    {
      // Nothing to decode
      return ICodec.identity ();
    }
  },
  QUOTED_PRINTABLE ("quoted-printable")
  {
    @Override
    public QuotedPrintableCodec createDecoder ()
    {
      return new QuotedPrintableCodec (RFC1522QCodec.getAllPrintableChars ());
    }
  },
  BASE64 ("base64")
  {
    @Override
    public Base64Codec createDecoder ()
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
