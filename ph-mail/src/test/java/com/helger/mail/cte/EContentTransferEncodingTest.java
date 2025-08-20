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
package com.helger.mail.cte;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.helger.base.codec.IByteArrayCodec;

/**
 * Test class for class {@link EContentTransferEncoding}.
 *
 * @author Philip Helger
 */
public class EContentTransferEncodingTest
{
  @Test
  public void testEncodeDecode ()
  {
    final byte [] aSrc = "Hello wörld".getBytes (StandardCharsets.UTF_16);

    for (final EContentTransferEncoding e : EContentTransferEncoding.values ())
    {
      final IByteArrayCodec aCodec = e.createCodec ();

      // Encode
      final byte [] aEncoded = aCodec.getEncoded (aSrc);
      assertNotNull (aEncoded);

      // Decode
      final byte [] aDecoded = aCodec.getDecoded (aEncoded);
      assertNotNull (aDecoded);

      // Consistency check
      assertArrayEquals (aSrc, aDecoded);
    }
  }
}
