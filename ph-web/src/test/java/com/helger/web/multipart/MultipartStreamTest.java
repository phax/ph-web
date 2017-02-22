/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.web.multipart;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;

/**
 * Unit tests {@link MultipartStream}.
 *
 * @author Sean C. Sullivan
 */
public final class MultipartStreamTest
{
  private static final String BOUNDARY_TEXT = "myboundary";

  @Test
  public void testThreeParamConstructor () throws Exception
  {
    final String sStrData = "foobar";
    final byte [] aContents = sStrData.getBytes (StandardCharsets.ISO_8859_1);
    final InputStream aIS = new NonBlockingByteArrayInputStream (aContents);
    final byte [] aBoundary = BOUNDARY_TEXT.getBytes (StandardCharsets.ISO_8859_1);
    final int nBufSize = aBoundary.length;
    final MultipartStream ms = new MultipartStream (aIS,
                                                    aBoundary,
                                                    nBufSize,
                                                    new MultipartProgressNotifier (null, aContents.length));
    assertNotNull (ms);
  }

  @Test
  public void testTwoParamConstructor () throws Exception
  {
    final String sStrData = "foobar";
    final byte [] contents = sStrData.getBytes (StandardCharsets.ISO_8859_1);
    final InputStream aIS = new NonBlockingByteArrayInputStream (contents);
    final byte [] aBoundary = BOUNDARY_TEXT.getBytes (StandardCharsets.ISO_8859_1);
    final MultipartStream ms = new MultipartStream (aIS,
                                                    aBoundary,
                                                    new MultipartProgressNotifier (null, contents.length));
    assertNotNull (ms);
  }
}
