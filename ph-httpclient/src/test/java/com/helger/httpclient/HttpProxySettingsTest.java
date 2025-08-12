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
    final HttpProxySettings source = new HttpProxySettings ();
    final HttpProxySettings target = new HttpProxySettings ();

    // Set up source with all properties
    final HttpHost proxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials creds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());
    source.setProxyHost (proxyHost);
    source.setProxyCredentials (creds);
    source.nonProxyHosts ().add ("host1.example.com");
    source.nonProxyHosts ().add ("host2.example.com");

    // Add some initial data to target
    target.nonProxyHosts ().add ("initial.example.com");

    // Test setAllFrom
    assertSame (target, target.setAllFrom (source));

    // Verify all properties are copied and old data is replaced
    assertSame (proxyHost, target.getProxyHost ());
    assertSame (creds, target.getProxyCredentials ());
    assertEquals (2, target.nonProxyHosts ().size ());
    assertTrue (target.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (target.nonProxyHosts ().contains ("host2.example.com"));
    assertFalse (target.nonProxyHosts ().contains ("initial.example.com"));
  }

  @Test
  public void testGetClone ()
  {
    final HttpProxySettings original = new HttpProxySettings ();

    // Set up original with all properties
    final HttpHost proxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials creds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());
    original.setProxyHost (proxyHost);
    original.setProxyCredentials (creds);
    original.nonProxyHosts ().add ("host1.example.com");
    original.nonProxyHosts ().add ("host2.example.com");

    // Test clone
    final HttpProxySettings clone = original.getClone ();

    // Verify clone is a different object but has same data
    assertTrue (clone != original);
    assertSame (proxyHost, clone.getProxyHost ());
    assertSame (creds, clone.getProxyCredentials ());
    assertEquals (2, clone.nonProxyHosts ().size ());
    assertTrue (clone.nonProxyHosts ().contains ("host1.example.com"));
    assertTrue (clone.nonProxyHosts ().contains ("host2.example.com"));

    // Verify modifying clone doesn't affect original
    clone.nonProxyHosts ().add ("clone-only.example.com");
    assertEquals (3, clone.nonProxyHosts ().size ());
    assertEquals (2, original.nonProxyHosts ().size ());
    assertFalse (original.nonProxyHosts ().contains ("clone-only.example.com"));
  }

  @Test
  public void testToString ()
  {
    final HttpProxySettings x = new HttpProxySettings ();

    // Test toString with empty settings
    String str = x.toString ();
    assertNotNull (str);
    assertTrue (str.contains ("HttpProxySettings"));

    // Test toString with proxy host
    x.setProxyHost (new HttpHost ("proxy.example.com", 8080));
    str = x.toString ();
    assertNotNull (str);
    assertTrue (str.contains ("proxy.example.com"));

    // Test toString with credentials
    x.setProxyCredentials (new UsernamePasswordCredentials ("user", "pass".toCharArray ()));
    str = x.toString ();
    assertNotNull (str);

    // Test toString with non-proxy hosts
    x.nonProxyHosts ().add ("host1.example.com");
    str = x.toString ();
    assertNotNull (str);
    assertTrue (str.contains ("host1.example.com"));
  }

  @Test
  public void testChaining ()
  {
    final HttpProxySettings x = new HttpProxySettings ();
    final HttpHost proxyHost = new HttpHost ("proxy.example.com", 8080);
    final UsernamePasswordCredentials creds = new UsernamePasswordCredentials ("user", "pass".toCharArray ());

    // Test method chaining
    final HttpProxySettings result = x.setProxyHost (proxyHost)
                                      .setProxyCredentials (creds)
                                      .addNonProxyHostsFromPipeString ("host1.example.com|host2.example.com")
                                      .setNonProxyHostsFromPipeString ("host3.example.com");

    // Verify chaining returns the same instance
    assertSame (x, result);

    // Verify final state
    assertSame (proxyHost, x.getProxyHost ());
    assertSame (creds, x.getProxyCredentials ());
    assertEquals (1, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("host3.example.com"));
  }
}
