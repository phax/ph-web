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
package com.helger.web.encoding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.http.RFC5987Encoder;

/**
 * Test class for class {@link RFC5987Encoder}.
 *
 * @author Philip Helger
 */
public final class RFC5987EncoderTest
{
  @Test
  public void testBasic ()
  {
    assertEquals ("abc", RFC5987Encoder.getRFC5987EncodedUTF8 ("abc"));
    assertEquals ("%20", RFC5987Encoder.getRFC5987EncodedUTF8 (" "));
    assertEquals ("a%20b%25c", RFC5987Encoder.getRFC5987EncodedUTF8 ("a b%c"));
  }
}
