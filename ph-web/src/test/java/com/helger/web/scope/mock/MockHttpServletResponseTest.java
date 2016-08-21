/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.scope.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.charset.CCharset;
import com.helger.commons.system.SystemHelper;
import com.helger.web.mock.MockHttpServletResponse;

/**
 * Test class for class {@link MockHttpServletResponse}.
 *
 * @author Philip Helger
 */
public final class MockHttpServletResponseTest
{
  private static final String TEST_STRING = "Test äöü";

  @Test
  public void testRequestResponse ()
  {
    // create a new Servlet context for testing
    final MockHttpServletResponse aResp = new MockHttpServletResponse ();
    assertEquals (CCharset.CHARSET_UTF_8, aResp.getCharacterEncoding ());
    aResp.getWriter ().write (TEST_STRING);
    assertFalse (aResp.isCommitted ());
    assertEquals (TEST_STRING, aResp.getContentAsString (CCharset.CHARSET_UTF_8_OBJ));
    assertTrue (aResp.isCommitted ());

    // Start over
    aResp.setCommitted (false);
    aResp.reset ();
    assertNull (aResp.getCharacterEncoding ());
    assertFalse (aResp.isCommitted ());
    // Set character encoding before writing
    aResp.setCharacterEncoding (CCharset.CHARSET_ISO_8859_1);
    aResp.getWriter ().write (TEST_STRING);
    assertEquals (CCharset.CHARSET_ISO_8859_1, aResp.getCharacterEncoding ());
    assertEquals (TEST_STRING, aResp.getContentAsString (CCharset.CHARSET_ISO_8859_1_OBJ));

    // Start over again
    aResp.setCommitted (false);
    aResp.reset ();
    assertNull (aResp.getCharacterEncoding ());
    assertFalse (aResp.isCommitted ());
    // Write in the system charset
    aResp.getWriter ().write (TEST_STRING);
    // Set character encoding after writing
    aResp.setCharacterEncoding ("UTF-16");
    assertEquals ("UTF-16", aResp.getCharacterEncoding ());
    // It will fail in the selected charset
    assertNotEquals (TEST_STRING, aResp.getContentAsString (CCharset.CHARSET_UTF_16_OBJ));
    // Retrieving in the system charset will succeed
    assertEquals (TEST_STRING, aResp.getContentAsString (SystemHelper.getSystemCharset ()));
  }
}
