/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

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
    final String strData = "foobar";
    final byte [] contents = strData.getBytes ();
    final InputStream input = new ByteArrayInputStream (contents);
    final byte [] boundary = BOUNDARY_TEXT.getBytes ();
    final int iBufSize = boundary.length;
    final MultipartStream ms = new MultipartStream (input,
                                                    boundary,
                                                    iBufSize,
                                                    new MultipartProgressNotifier (null, contents.length));
    assertNotNull (ms);
  }

  @Test
  public void testTwoParamConstructor () throws Exception
  {
    final String strData = "foobar";
    final byte [] contents = strData.getBytes ();
    final InputStream input = new ByteArrayInputStream (contents);
    final byte [] boundary = BOUNDARY_TEXT.getBytes ();
    final MultipartStream ms = new MultipartStream (input,
                                                    boundary,
                                                    new MultipartProgressNotifier (null, contents.length));
    assertNotNull (ms);
  }
}
