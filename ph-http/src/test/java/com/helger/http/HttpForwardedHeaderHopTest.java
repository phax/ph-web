/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;

/**
 * Test class for {@link HttpForwardedHeaderHop}.
 *
 * @author Philip Helger
 */
public final class HttpForwardedHeaderHopTest
{
  @Test
  public void testBasicFunctionality ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    assertTrue (aList.isEmpty ());
    assertFalse (aList.isNotEmpty ());
    assertEquals (0, aList.size ());
    assertEquals ("", aList.getAsString ());

    // Add a basic pair
    aList.setFor ("192.168.1.1");
    assertFalse (aList.isEmpty ());
    assertTrue (aList.isNotEmpty ());
    assertEquals (1, aList.size ());
    assertTrue (aList.containsToken (HttpForwardedHeaderHop.PARAM_FOR));
    assertEquals ("192.168.1.1", aList.getFor ());
    assertEquals ("for=192.168.1.1", aList.getAsString ());
  }

  @Test
  public void testStandardParameters ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();

    // Test "for" parameter
    aList.setFor ("192.168.1.1");
    assertEquals ("192.168.1.1", aList.getFor ());

    // Test "host" parameter
    aList.setHost ("example.com");
    assertEquals ("example.com", aList.getHost ());

    // Test "by" parameter
    aList.setBy ("proxy.example.com");
    assertEquals ("proxy.example.com", aList.getBy ());

    // Test "proto" parameter
    aList.setProto ("https");
    assertEquals ("https", aList.getProto ());

    // Check string representation
    final String sResult = aList.getAsString ();
    assertTrue (sResult.contains ("for=192.168.1.1"));
    assertTrue (sResult.contains ("host=example.com"));
    assertTrue (sResult.contains ("by=proxy.example.com"));
    assertTrue (sResult.contains ("proto=https"));
  }

  @Test
  public void testQuotedValues ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();

    // Add a value that needs quoting (contains colon which is not a valid token char)
    aList.setFor ("192.168.1.1:8080");
    aList.setHost ("example.com with spaces");

    final String sResult = aList.getAsString ();
    // Colon is not a valid token character, so the IP:port should be quoted
    assertTrue (sResult.contains ("for=\"192.168.1.1:8080\""));
    assertTrue (sResult.contains ("host=\"example.com with spaces\""));
  }

  @Test
  public void testIPv6Address ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();

    // IPv6 addresses in brackets should not be quoted
    aList.setFor ("[2001:db8::1]");
    assertEquals ("for=\"[2001:db8::1]\"", aList.getAsString ());
  }

  @Test
  public void testRemovePair ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    aList.setFor ("192.168.1.1");
    aList.setHost ("example.com");

    assertEquals (2, aList.size ());
    assertTrue (aList.removePair (HttpForwardedHeaderHop.PARAM_FOR).isChanged ());
    assertEquals (1, aList.size ());
    assertFalse (aList.containsToken (HttpForwardedHeaderHop.PARAM_FOR));
    assertNull (aList.getFirstValue (HttpForwardedHeaderHop.PARAM_FOR));

    // Remove non-existing key
    assertTrue (aList.removePair ("nonexisting").isUnchanged ());
  }

  @Test
  public void testRemoveAll ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    aList.setFor ("192.168.1.1");
    aList.setHost ("example.com");

    assertEquals (2, aList.size ());
    aList.removeAll ();
    assertEquals (0, aList.size ());
    assertTrue (aList.isEmpty ());
  }

  @Test
  public void testGetAllTokensAndPairs ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    aList.setFor ("192.168.1.1");
    aList.setHost ("example.com");

    assertEquals (2, aList.getAllTokens ().size ());
    assertTrue (aList.getAllTokens ().contains (HttpForwardedHeaderHop.PARAM_FOR));
    assertTrue (aList.getAllTokens ().contains (HttpForwardedHeaderHop.PARAM_HOST));

    assertEquals (2, aList.getAllPairs ().size ());
    assertEquals ("192.168.1.1", aList.getAllPairs ().get (HttpForwardedHeaderHop.PARAM_FOR));
    assertEquals ("example.com", aList.getAllPairs ().get (HttpForwardedHeaderHop.PARAM_HOST));
  }

  @Test
  public void testToString ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    aList.setProto ("https");
    aList.setFor ("192.168.1.1");

    assertNotNull (aList.toString ());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidToken ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    // This should fail because "for=" contains an invalid character
    aList.addPair ("for=", "value");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testEmptyToken ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    aList.addPair ("", "value");
  }

  @Test (expected = NullPointerException.class)
  public void testNullValue ()
  {
    final HttpForwardedHeaderHop aList = new HttpForwardedHeaderHop ();
    aList.setFor (null);
  }

  @Test
  public void testEqualsAndHashCode ()
  {
    // Test with empty objects
    final HttpForwardedHeaderHop aList1 = new HttpForwardedHeaderHop ();
    final HttpForwardedHeaderHop aList2 = new HttpForwardedHeaderHop ();
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aList1, aList2);

    // Test with identical content
    aList1.setFor ("192.168.1.1");
    aList1.setHost ("example.com");
    aList1.setProto ("https");

    aList2.setFor ("192.168.1.1");
    aList2.setHost ("example.com");
    aList2.setProto ("https");
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aList1, aList2);

    // Test with different content
    final HttpForwardedHeaderHop aList3 = new HttpForwardedHeaderHop ();
    // Different IP
    aList3.setFor ("192.168.1.2");
    aList3.setHost ("example.com");
    aList3.setProto ("https");
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (aList1, aList3);

    // Test with different number of pairs
    final HttpForwardedHeaderHop aList4 = new HttpForwardedHeaderHop ();
    aList4.setFor ("192.168.1.1");
    aList4.setHost ("example.com");
    // Missing proto parameter
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (aList1, aList4);

    // Test with same values but different order (are considered equals)
    // Problem lays in the LinkedHashMap implementation of equals and hashCode
    final HttpForwardedHeaderHop aList5 = new HttpForwardedHeaderHop ();
    // Different order
    aList5.setProto ("https");
    aList5.setHost ("example.com");
    aList5.setFor ("192.168.1.1");
    assertTrue (aList1.equals (aList5));
    assertFalse (aList1.hashCode () == aList5.hashCode ());

    // Test with custom parameters
    final HttpForwardedHeaderHop aList6 = new HttpForwardedHeaderHop ();
    aList6.addPair ("custom", "value1");
    aList6.addPair ("another", "value2");

    final HttpForwardedHeaderHop aList7 = new HttpForwardedHeaderHop ();
    aList7.addPair ("custom", "value1");
    aList7.addPair ("another", "value2");
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aList6, aList7);

    // Test with same keys but different values
    final HttpForwardedHeaderHop aList8 = new HttpForwardedHeaderHop ();
    aList8.addPair ("custom", "different_value");
    aList8.addPair ("another", "value2");
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (aList6, aList8);
  }
}
