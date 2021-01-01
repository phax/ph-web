/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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

import org.junit.Test;

import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.http.CHttpHeader;
import com.helger.servlet.mock.MockHttpServletRequest;

/**
 * Test class for class {@link UnifiedResponse}.
 *
 * @author Boris Gregorcic
 */
public final class UnifiedResponseTest
{
  @Test
  public void testSetStrictTransportSecurity ()
  {
    final UnifiedResponse aResponse = UnifiedResponse.createSimple (new MockHttpServletRequest ());
    assertTrue (aResponse.responseHeaderMap ().containsHeaders (CHttpHeader.STRICT_TRANSPORT_SECURITY));
    final int nMaxAgeSeconds = 60000;
    final boolean bIncludeSubdomains = true;
    aResponse.setStrictTransportSecurity (nMaxAgeSeconds, bIncludeSubdomains);
    assertTrue (aResponse.responseHeaderMap ().containsHeaders (CHttpHeader.STRICT_TRANSPORT_SECURITY));
    final ICommonsList <String> aValues = aResponse.responseHeaderMap ().getAllHeaderValues (CHttpHeader.STRICT_TRANSPORT_SECURITY);
    assertEquals (1, aValues.size ());
    assertEquals ("max-age=" + nMaxAgeSeconds + ";" + CHttpHeader.VALUE_INCLUDE_SUBDOMAINS, aValues.get (0));
  }

  @Test
  public void testSetAllowMimeSniffing ()
  {
    final UnifiedResponse aResponse = UnifiedResponse.createSimple (new MockHttpServletRequest ());
    assertTrue (aResponse.responseHeaderMap ().containsHeaders (CHttpHeader.X_CONTENT_TYPE_OPTIONS));
    aResponse.setAllowMimeSniffing (true);
    assertFalse (aResponse.responseHeaderMap ().containsHeaders (CHttpHeader.X_CONTENT_TYPE_OPTIONS));
    aResponse.setAllowMimeSniffing (false);
    assertTrue (aResponse.responseHeaderMap ().containsHeaders (CHttpHeader.X_CONTENT_TYPE_OPTIONS));
    final ICommonsList <String> aValues = aResponse.responseHeaderMap ().getAllHeaderValues (CHttpHeader.X_CONTENT_TYPE_OPTIONS);
    assertEquals (1, aValues.size ());
    assertEquals (CHttpHeader.VALUE_NOSNIFF, aValues.get (0));
  }

  @Test
  public void testAddRemoveCustomResponseHeader ()
  {
    final String sName = "FOO";
    final String sValue = "BAR";
    final String sValue2 = "NARF";
    final UnifiedResponse aResponse = UnifiedResponse.createSimple (new MockHttpServletRequest ());
    assertFalse (aResponse.responseHeaderMap ().containsHeaders (sName));
    {
      aResponse.addCustomResponseHeader (sName, sValue);
      assertTrue (aResponse.responseHeaderMap ().containsHeaders (sName));
      final ICommonsList <String> aValues = aResponse.responseHeaderMap ().getAllHeaderValues (sName);
      assertEquals (1, aValues.size ());
      assertEquals (sValue, aValues.get (0));
    }
    {
      aResponse.addCustomResponseHeader (sName, sValue2);
      assertTrue (aResponse.responseHeaderMap ().containsHeaders (sName));
      final ICommonsList <String> aValues = aResponse.responseHeaderMap ().getAllHeaderValues (sName);
      assertEquals (2, aValues.size ());
      assertEquals (sValue, aValues.get (0));
      assertEquals (sValue2, aValues.get (1));
    }
    {
      aResponse.removeCustomResponseHeaders (sName);
      assertFalse (aResponse.responseHeaderMap ().containsHeaders (sName));
    }
  }
}
