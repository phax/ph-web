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
package com.helger.web.servlet.response.gzip;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for class {@link CompressFilterSettings}.
 *
 * @author Philip Helger
 */
public final class CompressFilterSettingsTest
{
  @Before
  @After
  public void beforeAndAfter ()
  {
    assertFalse (CompressFilterSettings.isDebugModeEnabled ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());
  }

  @Test
  public void testDebugMode ()
  {
    assertFalse (CompressFilterSettings.isDebugModeEnabled ());

    assertTrue (CompressFilterSettings.setDebugModeEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isDebugModeEnabled ());
    assertFalse (CompressFilterSettings.setDebugModeEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isDebugModeEnabled ());

    assertTrue (CompressFilterSettings.setDebugModeEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isDebugModeEnabled ());
    assertFalse (CompressFilterSettings.setDebugModeEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isDebugModeEnabled ());
  }

  @Test
  public void testCompress ()
  {
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());

    assertTrue (CompressFilterSettings.setResponseCompressionEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.setResponseCompressionEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());

    assertTrue (CompressFilterSettings.setResponseCompressionEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.setResponseCompressionEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
  }

  @Test
  public void testGzip ()
  {
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());

    assertTrue (CompressFilterSettings.setResponseGzipEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.setResponseGzipEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());

    assertTrue (CompressFilterSettings.setResponseGzipEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.setResponseGzipEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
  }

  @Test
  public void testDeflate ()
  {
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());

    assertTrue (CompressFilterSettings.setResponseDeflateEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setResponseDeflateEnabled (false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());

    assertTrue (CompressFilterSettings.setResponseDeflateEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setResponseDeflateEnabled (true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());
  }

  @Test
  public void testAll ()
  {
    assertTrue (CompressFilterSettings.setAll (false, false, false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setAll (false, false, false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());

    assertTrue (CompressFilterSettings.setAll (true, false, false).isChanged ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setAll (true, false, false).isChanged ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());

    assertTrue (CompressFilterSettings.setAll (false, true, false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setAll (false, true, false).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
    assertFalse (CompressFilterSettings.isResponseDeflateEnabled ());

    assertTrue (CompressFilterSettings.setAll (false, false, true).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setAll (false, false, true).isChanged ());
    assertFalse (CompressFilterSettings.isResponseCompressionEnabled ());
    assertFalse (CompressFilterSettings.isResponseGzipEnabled ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());

    assertTrue (CompressFilterSettings.setAll (true, true, true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());
    assertFalse (CompressFilterSettings.setAll (true, true, true).isChanged ());
    assertTrue (CompressFilterSettings.isResponseCompressionEnabled ());
    assertTrue (CompressFilterSettings.isResponseGzipEnabled ());
    assertTrue (CompressFilterSettings.isResponseDeflateEnabled ());
  }
}
