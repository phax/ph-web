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
package com.helger.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.HttpHost;
import org.junit.Test;

/**
 * Test class for class {@link HttpProxySettings}.
 *
 * @author Philip Helger
 */
public final class HttpProxySettingsTest
{
  @Test
  public void testAddNonProxyHostsFromPipeString ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString (null);

    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("");

    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("          ");

    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("   |    ");

    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("  |||||   ||  |||| |   |");

    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString (" 127.0.0.1 | localhost ");

    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("127.0.0.1"));
    assertTrue (x.nonProxyHosts ().contains ("localhost"));

    x.addNonProxyHostsFromPipeString ("127.0.0.1|localhost");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("127.0.0.1"));
    assertTrue (x.nonProxyHosts ().contains ("localhost"));

    x.addNonProxyHostsFromPipeString ("127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|localhost");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("127.0.0.1"));
    assertTrue (x.nonProxyHosts ().contains ("localhost"));
  }

  @Test
  public void testDefaultConstructor ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test initial state
    assertNull (x.getProxyHost ());
    assertFalse (x.hasProxyHost ());
    assertNull (x.getProxyCredentials ());
    assertFalse (x.hasProxyCredentials ());
    assertEquals (0, x.nonProxyHosts ().size ());
    assertEquals (0, x.getAllNonProxyHosts ().size ());

    // Test toString doesn't crash
    assertNotNull (x.toString ());
  }

  @Test
  public void testProxyHost ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test initial state
    assertNull (x.getProxyHost ());
    assertFalse (x.hasProxyHost ());

    // Test setting a proxy host
    final HttpHost aProxyHost = new HttpHost ("proxy.example.com", 8080);
    assertSame (x, x.setProxyHost (aProxyHost));
    assertSame (aProxyHost, x.getProxyHost ());
    assertTrue (x.hasProxyHost ());

    // Test setting null proxy host
    assertSame (x, x.setProxyHost (null));
    assertNull (x.getProxyHost ());
    assertFalse (x.hasProxyHost ());
  }

  @Test
  public void testProxyCredentials ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test initial state
    assertNull (x.getProxyCredentials ());
    assertFalse (x.hasProxyCredentials ());

    // Test setting credentials
    final UsernamePasswordCredentials aCreds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());
    assertSame (x, x.setProxyCredentials (aCreds));
    assertSame (aCreds, x.getProxyCredentials ());
    assertTrue (x.hasProxyCredentials ());

    // Test setting null credentials
    assertSame (x, x.setProxyCredentials (null));
    assertNull (x.getProxyCredentials ());
    assertFalse (x.hasProxyCredentials ());
  }

  @Test
  public void testNonProxyHosts ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test initial state
    assertNotNull (x.nonProxyHosts ());
    assertEquals (0, x.nonProxyHosts ().size ());
    assertNotNull (x.getAllNonProxyHosts ());
    assertEquals (0, x.getAllNonProxyHosts ().size ());

    // Test adding hosts directly
    x.nonProxyHosts ().add ("host1.example.com");
    x.nonProxyHosts ().add ("host2.example.com");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertEquals (2, x.getAllNonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (x.nonProxyHosts ().contains ("host2.example.com"));
    assertTrue (x.getAllNonProxyHosts ().contains ("host1.example.com"));
    assertTrue (x.getAllNonProxyHosts ().contains ("host2.example.com"));

    // Test that getAllNonProxyHosts returns a copy
    x.getAllNonProxyHosts ().clear ();
    // Original should still have 2 elements
    assertEquals (2, x.nonProxyHosts ().size ());
  }

  @Test
  public void testSetNonProxyHostsFromPipeString ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Add some initial hosts
    x.nonProxyHosts ().add ("initial.example.com");
    assertEquals (1, x.nonProxyHosts ().size ());

    // Test setting (should clear existing and add new)
    assertSame (x, x.setNonProxyHostsFromPipeString ("host1.example.com|host2.example.com"));
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (x.nonProxyHosts ().contains ("host2.example.com"));
    assertFalse (x.nonProxyHosts ().contains ("initial.example.com"));

    // Test setting null (should clear all)
    assertSame (x, x.setNonProxyHostsFromPipeString (null));
    assertEquals (0, x.nonProxyHosts ().size ());

    // Test setting empty (should remain empty)
    assertSame (x, x.setNonProxyHostsFromPipeString (""));
    assertEquals (0, x.nonProxyHosts ().size ());
  }

  @Test
  public void testCopyConstructor ()
  {
    final HttpProxySettings aSource = new HttpProxySettings ();

    // Set up source with all properties
    final HttpHost aProxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials aCreds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());
    aSource.setProxyHost (aProxyHost);
    aSource.setProxyCredentials (aCreds);
    aSource.nonProxyHosts ().add ("host1.example.com");
    aSource.nonProxyHosts ().add ("host2.example.com");

    // Test copy constructor
    final HttpProxySettings aCopy = new HttpProxySettings (aSource);

    // Verify all properties are copied
    assertSame (aProxyHost, aCopy.getProxyHost ());
    assertSame (aCreds, aCopy.getProxyCredentials ());
    assertEquals (2, aCopy.nonProxyHosts ().size ());
    assertTrue (aCopy.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (aCopy.nonProxyHosts ().contains ("host2.example.com"));
  }

  @Test
  public void testSetAllFrom ()
  {
    final HttpProxySettings aSource = new HttpProxySettings ();
    final HttpProxySettings aTarget = new HttpProxySettings ();

    // Set up source with all properties
    final HttpHost aProxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials aCreds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());
    aSource.setProxyHost (aProxyHost);
    aSource.setProxyCredentials (aCreds);
    aSource.nonProxyHosts ().add ("host1.example.com");
    aSource.nonProxyHosts ().add ("host2.example.com");

    // Add some initial data to target
    aTarget.nonProxyHosts ().add ("initial.example.com");

    // Test setAllFrom
    assertSame (aTarget, aTarget.setAllFrom (aSource));

    // Verify all properties are copied and old data is replaced
    assertSame (aProxyHost, aTarget.getProxyHost ());
    assertSame (aCreds, aTarget.getProxyCredentials ());
    assertEquals (2, aTarget.nonProxyHosts ().size ());
    assertTrue (aTarget.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (aTarget.nonProxyHosts ().contains ("host2.example.com"));
    assertFalse (aTarget.nonProxyHosts ().contains ("initial.example.com"));
  }

  @Test
  public void testGetClone ()
  {
    final HttpProxySettings aOriginal = new HttpProxySettings ();

    // Set up original with all properties
    final HttpHost aProxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials creds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());
    aOriginal.setProxyHost (aProxyHost);
    aOriginal.setProxyCredentials (creds);
    aOriginal.nonProxyHosts ().add ("host1.example.com");
    aOriginal.nonProxyHosts ().add ("host2.example.com");

    // Test clone
    final HttpProxySettings aClone = aOriginal.getClone ();

    // Verify clone is a different object but has same data
    assertTrue (aClone != aOriginal);
    assertSame (aProxyHost, aClone.getProxyHost ());
    assertSame (creds, aClone.getProxyCredentials ());
    assertEquals (2, aClone.nonProxyHosts ().size ());
    assertTrue (aClone.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (aClone.nonProxyHosts ().contains ("host2.example.com"));

    // Verify modifying clone doesn't affect original
    aClone.nonProxyHosts ().add ("clone-only.example.com");

    assertEquals (3, aClone.nonProxyHosts ().size ());
    assertEquals (2, aOriginal.nonProxyHosts ().size ());
    assertFalse (aOriginal.nonProxyHosts ().contains ("clone-only.example.com"));
  }

  @Test
  public void testToString ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test toString with empty settings
    String s = x.toString ();
    assertNotNull (s);
    assertTrue (s.contains ("HttpProxySettings"));

    // Test toString with proxy host
    x.setProxyHost (new HttpHost ("proxy.example.com", 8080));
    s = x.toString ();
    assertNotNull (s);
    assertTrue (s.contains ("proxy.example.com"));

    // Test toString with credentials
    x.setProxyCredentials (new UsernamePasswordCredentials ("user", "pass".toCharArray ()));
    s = x.toString ();
    assertNotNull (s);

    // Test toString with non-proxy hosts
    x.nonProxyHosts ().add ("host1.example.com");
    s = x.toString ();
    assertNotNull (s);
    assertTrue (s.contains ("host1.example.com"));
  }

  @Test
  public void testChaining ()
  {
    final HttpProxySettings x = new HttpProxySettings ();
    final HttpHost aProxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials aCreds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());

    // Test method chaining
    final HttpProxySettings aResult = x.setProxyHost (aProxyHost)
                                       .setProxyCredentials (aCreds)
                                       .addNonProxyHostsFromPipeString ("host1.example.com|host2.example.com")
                                       .setNonProxyHostsFromPipeString ("host3.example.com");

    // Verify chaining returns the same instance
    assertSame (x, aResult);

    // Verify final state
    assertSame (aProxyHost, x.getProxyHost ());
    assertSame (aCreds, x.getProxyCredentials ());
    assertEquals (1, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host3.example.com"));
  }

  @Test
  public void testErrorHandling ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test setAllFrom with null - should throw exception
    try
    {
      x.setAllFrom (null);
      fail ();
    }
    catch (final RuntimeException ex)
    {
      // Expected
      assertNotNull (ex.getMessage ());
    }
  }

  @Test
  public void testEdgeCases ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test with various proxy host configurations
    HttpHost aProxyHost = new HttpHost ("localhost", 80);
    x.setProxyHost (aProxyHost);
    assertTrue (x.hasProxyHost ());
    assertEquals ("localhost", aProxyHost.getHostName ());
    assertEquals (80, aProxyHost.getPort ());

    // Test with HTTPS proxy
    aProxyHost = new HttpHost ("https", "secure-proxy.example.com", 443);
    x.setProxyHost (aProxyHost);
    assertTrue (x.hasProxyHost ());
    assertEquals ("secure-proxy.example.com", aProxyHost.getHostName ());
    assertEquals (443, aProxyHost.getPort ());
    assertEquals ("https", aProxyHost.getSchemeName ());

    // Test with credentials having empty password
    final UsernamePasswordCredentials aCreds = new UsernamePasswordCredentials ("user", new char [0]);
    x.setProxyCredentials (aCreds);
    assertTrue (x.hasProxyCredentials ());
    assertEquals ("user", aCreds.getUserPrincipal ().getName ());

    // Test with single non-proxy host from pipe string
    x.setNonProxyHostsFromPipeString ("single-host.example.com");
    assertEquals (1, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("single-host.example.com"));

    // Test with trailing and leading spaces in pipe string
    x.setNonProxyHostsFromPipeString ("  host1.example.com  |  host2.example.com  ");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (x.nonProxyHosts ().contains ("host2.example.com"));
  }

  @Test
  public void testInterfaceMethods ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test interface methods through the implementation
    final IHttpProxySettings aIface = x;

    // Test initial state through interface
    assertFalse (aIface.hasProxyHost ());
    assertNull (aIface.getProxyHost ());
    assertFalse (aIface.hasProxyCredentials ());
    assertNull (aIface.getProxyCredentials ());
    assertNotNull (aIface.nonProxyHosts ());
    assertEquals (0, aIface.nonProxyHosts ().size ());
    assertNotNull (aIface.getAllNonProxyHosts ());
    assertEquals (0, aIface.getAllNonProxyHosts ().size ());

    // Set values and test through interface
    final HttpHost aProxyHost = new HttpHost ("interface-proxy.example.com", 9090);
    final UsernamePasswordCredentials aCreds = new UsernamePasswordCredentials ("interface-user",
                                                                                "interface-pass".toCharArray ());

    x.setProxyHost (aProxyHost);
    x.setProxyCredentials (aCreds);
    x.nonProxyHosts ().add ("interface-host.example.com");

    // Verify through interface
    assertTrue (aIface.hasProxyHost ());
    assertSame (aProxyHost, aIface.getProxyHost ());
    assertTrue (aIface.hasProxyCredentials ());
    assertSame (aCreds, aIface.getProxyCredentials ());
    assertEquals (1, aIface.nonProxyHosts ().size ());
    assertTrue (aIface.nonProxyHosts ().contains ("interface-host.example.com"));
    assertEquals (1, aIface.getAllNonProxyHosts ().size ());
    assertTrue (aIface.getAllNonProxyHosts ().contains ("interface-host.example.com"));
  }

  @Test
  public void testComplexPipeStringScenarios ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test with mixed spacing and multiple separators
    x.addNonProxyHostsFromPipeString ("host1.example.com||host2.example.com|  |host3.example.com  ||  ");
    assertEquals (3, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (x.nonProxyHosts ().contains ("host2.example.com"));
    assertTrue (x.nonProxyHosts ().contains ("host3.example.com"));

    // Test adding more hosts to existing set
    // host1 is duplicate
    x.addNonProxyHostsFromPipeString ("host4.example.com|host1.example.com");
    // Should still be 4 (set semantics)
    assertEquals (4, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host4.example.com"));

    // Test with just separators (should add nothing)
    final int nSizeBefore = x.nonProxyHosts ().size ();
    x.addNonProxyHostsFromPipeString ("|||");
    assertEquals (nSizeBefore, x.nonProxyHosts ().size ());
  }
}
