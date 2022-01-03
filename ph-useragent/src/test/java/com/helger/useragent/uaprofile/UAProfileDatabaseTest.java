/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Test;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsCollection;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.mock.CommonsTestHelper;

/**
 * Test class for class {@link UAProfileDatabase}.
 *
 * @author Philip Helger
 */
public final class UAProfileDatabaseTest
{
  private static final class MockProvider implements IUAProfileHeaderProvider
  {
    private final HttpHeaderMap m_aMap;

    public MockProvider (@Nonnull final HttpHeaderMap aMap)
    {
      m_aMap = aMap;
    }

    @Nonnull
    @ReturnsMutableCopy
    public ICommonsCollection <String> getAllHeaderNames ()
    {
      return m_aMap.getAllHeaderNames ();
    }

    @Nonnull
    @ReturnsMutableCopy
    public ICommonsCollection <String> getHeaders (final String sName)
    {
      return m_aMap.getAllHeaderValues (sName);
    }

    @Nullable
    public String getHeaderValue (final String sName)
    {
      return m_aMap.getFirstHeaderValue (sName);
    }
  }

  @Test
  public void testCCPP ()
  {
    final HttpHeaderMap aMap = new HttpHeaderMap ();

    // No headers set
    UAProfile aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNull (aProfile);

    // Too little headers
    aMap.addHeader ("Man", "\"http://www.w3.org/1999/06/24-CCPPexchange\" ; ns=25");
    aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNull (aProfile);

    aMap.addHeader ("25-Profile", "\"http://www.ex.com/hw\", \"1-IrQu+5Yf+LgLnY1Y0X9QKA==\"");
    aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertEquals ("http://www.ex.com/hw", aProfile.getProfileURL ());
    assertEquals (0, aProfile.getDiffCount ());

    aMap.addHeader ("25-Profile-Diff-1",
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
    aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertEquals ("http://www.ex.com/hw", aProfile.getProfileURL ());
    assertEquals (1, aProfile.getDiffCount ());

    // Just some sanity check
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aProfile,
                                                                       UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap)));
  }

  @Test
  public void testUAProfSingleDiff ()
  {
    final HttpHeaderMap aMap = new HttpHeaderMap ();

    aMap.addHeader ("x-wap-profile", "\"1-diGacUwQ6eeOlaWB6cFEag==\"");
    UAProfile aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNull (aProfile);

    aMap.addHeader ("x-wap-profile-diff",
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
    aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertNull (aProfile.getProfileURL ());
    assertEquals (1, aProfile.getDiffCount ());

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aProfile,
                                                                       UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap)));
  }

  @Test
  public void testUAProfMultiDiff ()
  {
    final HttpHeaderMap aMap = new HttpHeaderMap ();
    aMap.addHeader ("x-wap-profile", "\"1-diGacUwQ6eeOlaWB6cFEag==\", \"2-diGacUwQ6eeOlaWB6cFEag==\", \"3-diGacUwQ6eeOlaWB6cFEag==\"");
    aMap.addHeader ("x-wap-profile-diff",
                    "1;<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\">1</rdf:RDF>");
    aMap.addHeader ("x-wap-profile-diff",
                    "2;<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\">1</rdf:RDF>");
    aMap.addHeader ("x-wap-profile-diff",
                    "3;<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prf=\"http://www.wapforum.org/profiles/UAPROF/ccppschema-20010430#\">1</rdf:RDF>");
    final UAProfile aProfile = UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap));
    assertNotNull (aProfile);
    assertTrue (aProfile.isSet ());
    assertNull (aProfile.getProfileURL ());
    assertEquals (3, aProfile.getDiffCount ());

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aProfile,
                                                                       UAProfileDatabase.getUAProfileFromRequest (new MockProvider (aMap)));
  }
}
