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
package com.helger.web.useragent.uaprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;
import com.helger.web.mock.MockHttpServletRequest;

/**
 * Test class for class {@link UAProfileDatabase}.
 *
 * @author Philip Helger
 */
public final class UAProfileDatabaseTest
{
  @Test
  public void testCCPP ()
  {
    final MockHttpServletRequest aHttpRequest = new MockHttpServletRequest ();

    // No headers set
    UAProfile aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNull (aProfile);

    // Too little headers
    aHttpRequest.addHeader ("Man", "\"http://www.w3.org/1999/06/24-CCPPexchange\" ; ns=25");
    aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNull (aProfile);

    aHttpRequest.addHeader ("25-Profile", "\"http://www.ex.com/hw\", \"1-IrQu+5Yf+LgLnY1Y0X9QKA==\"");
    aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertEquals ("http://www.ex.com/hw", aProfile.getProfileURL ());
    assertEquals (0, aProfile.getDiffCount ());

    aHttpRequest.addHeader ("25-Profile-Diff-1",
                            "<?xml version=\"1.0\"?> <rdf:RDF\n" +
                                                 "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n" +
                                                 "xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\"> \n" +
                                                 "<rdf:Description rdf:ID=\"MyDeviceProfile\">\n" +
                                                 "<prf:component>\n" +
                                                 "<rdf:Description rdf:ID=\"HardwarePlatform\">\n" +
                                                 "<rdf:type\n" +
                                                 "rdf:resource=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#HardwarePlatform\"/> \n" +
                                                 "<prf:SoundOutputCapable>No</prf:SoundOutputCapable>\n" +
                                                 "</rdf:Description>\n" +
                                                 "</prf:component>\n" +
                                                 "</rdf:Description>\n" +
                                                 "</rdf:RDF>");
    aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertEquals ("http://www.ex.com/hw", aProfile.getProfileURL ());
    assertEquals (1, aProfile.getDiffCount ());

    // Just some sanity check
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aProfile,
                                                                       UAProfileDatabase.getUAProfileFromRequest (aHttpRequest));
  }

  @Test
  public void testUAProfSingleDiff ()
  {
    final MockHttpServletRequest aHttpRequest = new MockHttpServletRequest ();

    aHttpRequest.addHeader ("x-wap-profile", "\"1-diGacUwQ6eeOlaWB6cFEag==\"");
    UAProfile aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNull (aProfile);

    aHttpRequest.addHeader ("x-wap-profile-diff",
                            "1;<?xml version=\"1.0\"?> <rdf:RDF\n" +
                                                  "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n" +
                                                  "xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\"> \n" +
                                                  "<rdf:Description rdf:ID=\"MyDeviceProfile\">\n" +
                                                  "<prf:component>\n" +
                                                  "<rdf:Description rdf:ID=\"HardwarePlatform\">\n" +
                                                  "<rdf:type\n" +
                                                  "rdf:resource=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#HardwarePlatform\"/> \n" +
                                                  "<prf:SoundOutputCapable>No</prf:SoundOutputCapable> \n" +
                                                  "</rdf:Description>\n" +
                                                  "</prf:component>\n" +
                                                  "</rdf:Description>\n" +
                                                  "</rdf:RDF>");
    aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertNull (aProfile.getProfileURL ());
    assertEquals (1, aProfile.getDiffCount ());

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aProfile,
                                                                       UAProfileDatabase.getUAProfileFromRequest (aHttpRequest));
  }

  @Test
  public void testUAProfMultiDiff ()
  {
    final MockHttpServletRequest aHttpRequest = new MockHttpServletRequest ();

    aHttpRequest.addHeader ("x-wap-profile",
                            "\"1-diGacUwQ6eeOlaWB6cFEag==\", \"2-diGacUwQ6eeOlaWB6cFEag==\", \"3-diGacUwQ6eeOlaWB6cFEag==\"");
    aHttpRequest.addHeader ("x-wap-profile-diff",
                            "1;<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\">1</rdf:RDF>");
    aHttpRequest.addHeader ("x-wap-profile-diff",
                            "2;<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\">1</rdf:RDF>");
    aHttpRequest.addHeader ("x-wap-profile-diff",
                            "3;<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\">1</rdf:RDF>");
    final UAProfile aProfile = UAProfileDatabase.getUAProfileFromRequest (aHttpRequest);
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertNull (aProfile.getProfileURL ());
    assertEquals (3, aProfile.getDiffCount ());

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aProfile,
                                                                       UAProfileDatabase.getUAProfileFromRequest (aHttpRequest));
  }
}
