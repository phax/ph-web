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
package com.helger.useragent.uaprofile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.array.ArrayHelper;
import com.helger.base.codec.base64.Base64;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.security.messagedigest.EMessageDigestAlgorithm;
import com.helger.security.messagedigest.MessageDigestValue;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.serialize.MicroReader;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Represents a single UA profile diff.
 *
 * @author Philip Helger
 */
@Immutable
public class UAProfileDiff
{
  public static final int EXPECTED_MD5_DIGEST_BYTES = 16;
  private static final Logger LOGGER = LoggerFactory.getLogger (UAProfileDiff.class);

  private final String m_sData;
  private final byte [] m_aMD5Digest;

  // State variables
  private final IMicroDocument m_aDocument;

  public UAProfileDiff (@Nonnull @Nonempty final String sData, @Nullable final byte [] aMD5Digest)
  {
    ValueEnforcer.notEmpty (sData, "Data");
    if (aMD5Digest != null && aMD5Digest.length != EXPECTED_MD5_DIGEST_BYTES)
      throw new IllegalArgumentException ("invalid MD5 digest length: " + aMD5Digest.length);

    m_sData = sData;
    m_aMD5Digest = ArrayHelper.getCopy (aMD5Digest);

    if (m_aMD5Digest != null)
    {
      // Verify MD5 digest
      final byte [] aCalcedDigest = MessageDigestValue.create (sData.getBytes (StandardCharsets.UTF_8),
                                                               EMessageDigestAlgorithm.MD5).bytes ();
      if (!Arrays.equals (m_aMD5Digest, aCalcedDigest))
        LOGGER.warn ("MD5 digest mismatch of profile diff data! Expected '" +
                     Base64.encodeBytes (aCalcedDigest) +
                     "' but have '" +
                     Base64.encodeBytes (m_aMD5Digest) +
                     "'");
    }
    m_aDocument = MicroReader.readMicroXML (sData);
    if (m_aDocument == null)
      LOGGER.warn ("Failed to parse profile diff data as XML '" + sData + "'");
  }

  @Nonnull
  @Nonempty
  public String getData ()
  {
    return m_sData;
  }

  /**
   * @return A copy of the MD5 digest data or <code>null</code> if non was provided.
   */
  @Nullable
  @ReturnsMutableCopy
  public byte [] getMD5Digest ()
  {
    return ArrayHelper.getCopy (m_aMD5Digest);
  }

  /**
   * @return The parsed XML document or <code>null</code> in parsing failed.
   */
  @Nullable
  public IMicroDocument getDocument ()
  {
    return m_aDocument;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final UAProfileDiff rhs = (UAProfileDiff) o;
    return m_sData.equals (rhs.m_sData) && EqualsHelper.equals (m_aMD5Digest, rhs.m_aMD5Digest);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sData).append (m_aMD5Digest).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("data", m_sData)
                                       .appendIfNotNull ("digest", m_aMD5Digest)
                                       .appendIfNotNull ("document", m_aDocument)
                                       .getToString ();
  }
}
