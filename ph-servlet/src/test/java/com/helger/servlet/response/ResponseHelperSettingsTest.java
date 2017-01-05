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
package com.helger.servlet.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.helger.servlet.response.ResponseHelperSettings;

/**
 * Test class for class {@link ResponseHelperSettings}.
 *
 * @author Philip Helger
 */
public final class ResponseHelperSettingsTest
{
  @Test
  public void testExpires ()
  {
    assertEquals ("Sat, 06 May 1995 12:00:00 GMT", ResponseHelperSettings.EXPIRES_NEVER_STRING);
  }

  @Before
  @After
  public void beforeAndAfter ()
  {
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());
  }

  @Test
  public void testCompress ()
  {
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());

    assertTrue (ResponseHelperSettings.setResponseCompressionEnabled (false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.setResponseCompressionEnabled (false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());

    assertTrue (ResponseHelperSettings.setResponseCompressionEnabled (true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.setResponseCompressionEnabled (true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
  }

  @Test
  public void testGzip ()
  {
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());

    assertTrue (ResponseHelperSettings.setResponseGzipEnabled (false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.setResponseGzipEnabled (false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());

    assertTrue (ResponseHelperSettings.setResponseGzipEnabled (true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.setResponseGzipEnabled (true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
  }

  @Test
  public void testDeflate ()
  {
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());

    assertTrue (ResponseHelperSettings.setResponseDeflateEnabled (false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setResponseDeflateEnabled (false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());

    assertTrue (ResponseHelperSettings.setResponseDeflateEnabled (true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setResponseDeflateEnabled (true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());
  }

  @Test
  public void testAll ()
  {
    assertTrue (ResponseHelperSettings.setAll (false, false, false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setAll (false, false, false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());

    assertTrue (ResponseHelperSettings.setAll (true, false, false).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setAll (true, false, false).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());

    assertTrue (ResponseHelperSettings.setAll (false, true, false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setAll (false, true, false).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
    assertFalse (ResponseHelperSettings.isResponseDeflateEnabled ());

    assertTrue (ResponseHelperSettings.setAll (false, false, true).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setAll (false, false, true).isChanged ());
    assertFalse (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertFalse (ResponseHelperSettings.isResponseGzipEnabled ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());

    assertTrue (ResponseHelperSettings.setAll (true, true, true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());
    assertFalse (ResponseHelperSettings.setAll (true, true, true).isChanged ());
    assertTrue (ResponseHelperSettings.isResponseCompressionEnabled ());
    assertTrue (ResponseHelperSettings.isResponseGzipEnabled ());
    assertTrue (ResponseHelperSettings.isResponseDeflateEnabled ());
  }
}
