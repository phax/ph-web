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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;

/**
 * Test class for {@link HttpForwardedHeaderParser}.
 *
 * @author Philip Helger
 */
public final class HttpForwardedHeaderParserTest
{
  @Test
  public void testValidSinglePair ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=192.168.1.1");
    assertNotNull (aResult);
    assertEquals (1, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
  }

  @Test
  public void testValidSinglePairCaseInsensitive ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("FOR=192.168.1.1");
    assertNotNull (aResult);
    assertEquals (1, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
  }

  @Test
  public void testValidMultiplePairs ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=192.168.1.1;host=example.com;proto=https");
    assertNotNull (aResult);
    assertEquals (3, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
    assertEquals ("example.com", aResult.getHost ());
    assertEquals ("https", aResult.getProto ());
  }

  @Test
  public void testQuotedValues ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=\"192.168.1.1:8080\";host=\"example.com with spaces\"");
    assertNotNull (aResult);
    assertEquals (2, aResult.size ());
    assertEquals ("192.168.1.1:8080", aResult.getFor ());
    assertEquals ("example.com with spaces", aResult.getHost ());
  }

  @Test
  public void testEscapedCharacters ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=\"test\\\"value\\\\\";host=\"simple\"");
    assertNotNull (aResult);
    assertEquals (2, aResult.size ());
    assertEquals ("test\"value\\", aResult.getFor ());
    assertEquals ("simple", aResult.getHost ());
  }

  @Test
  public void testWhitespaceHandling ()
  {
    // Basic case without whitespace
    HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=192.168.1.1;host=example.com");
    assertNotNull (aResult);
    assertEquals (2, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
    assertEquals ("example.com", aResult.getHost ());

    // Test with whitespace around semicolons
    aResult = HttpForwardedHeaderParser.parse ("for=192.168.1.1 ; host=example.com");
    assertNotNull (aResult);
    assertEquals (2, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
    assertEquals ("example.com", aResult.getHost ());

    // Test with leading/trailing whitespace
    aResult = HttpForwardedHeaderParser.parse ("  for=192.168.1.1;host=example.com  ");
    assertNotNull (aResult);
    assertEquals (2, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
    assertEquals ("example.com", aResult.getHost ());
  }

  @Test
  public void testIPv6Address ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=\"[2001:db8::1]\";proto=https");
    assertNotNull (aResult);
    assertEquals (2, aResult.size ());
    assertEquals ("[2001:db8::1]", aResult.getFor ());
    assertEquals ("https", aResult.getProto ());
  }

  @Test
  public void testCustomParameters ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=192.168.1.1;custom=value123;another=\"quoted value\"");
    assertNotNull (aResult);
    assertEquals (3, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
    assertEquals ("value123", aResult.getFirstValue ("custom"));
    assertEquals ("quoted value", aResult.getFirstValue ("another"));
  }

  @Test
  public void testEmptyString ()
  {
    HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("");
    assertNotNull (aResult);
    assertTrue (aResult.isEmpty ());

    aResult = HttpForwardedHeaderParser.parse ("   ");
    assertNotNull (aResult);
    assertTrue (aResult.isEmpty ());

    assertNotNull (HttpForwardedHeaderParser.parse (null));
    assertTrue (HttpForwardedHeaderParser.parse (null).isEmpty ());
  }

  @Test
  public void testEmptyPairs ()
  {
    // A standalone semicolon is actually invalid according to RFC 7239
    // The grammar is: forwarded-element = [ forwarded-pair ] *( ";" [ forwarded-pair ] )
    // So ";" alone would be invalid
    assertNull (HttpForwardedHeaderParser.parse (";"));
  }

  @Test
  public void testTrailingSemicolon ()
  {
    final HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=192.168.1.1;");
    assertNotNull (aResult);
    assertEquals (1, aResult.size ());
    assertEquals ("192.168.1.1", aResult.getFor ());
  }

  @Test
  public void testMultipleSemicolons ()
  {
    // Multiple consecutive semicolons should be invalid
    // ";;" means there's an empty pair between semicolons
    assertNull (HttpForwardedHeaderParser.parse ("for=192.168.1.1;;host=example.com"));
  }

  // Error cases - all should return null

  @Test
  public void testInvalidToken ()
  {
    assertNull (HttpForwardedHeaderParser.parse ("for@invalid=192.168.1.1"));
    assertNull (HttpForwardedHeaderParser.parse ("for=value;host@invalid=example.com"));
  }

  @Test
  public void testMissingEquals ()
  {
    assertNull (HttpForwardedHeaderParser.parse ("for192.168.1.1"));
    assertNull (HttpForwardedHeaderParser.parse ("for=192.168.1.1;hostexample.com"));
  }

  @Test
  public void testMissingValue ()
  {
    assertNull (HttpForwardedHeaderParser.parse ("for="));
    assertNull (HttpForwardedHeaderParser.parse ("for=192.168.1.1;host="));
  }

  @Test
  public void testUnterminatedQuotedString ()
  {
    assertNull (HttpForwardedHeaderParser.parse ("for=\"192.168.1.1"));
    assertNull (HttpForwardedHeaderParser.parse ("for=\"192.168.1.1;host=example.com"));
  }

  @Test
  public void testInvalidQuotedStringCharacters ()
  {
    // Control characters are not allowed in quoted strings
    assertNull (HttpForwardedHeaderParser.parse ("for=\"test\u0001value\""));
    assertNull (HttpForwardedHeaderParser.parse ("for=\"test\u007f\""));
  }

  @Test
  public void testIncompleteEscapeSequence ()
  {
    assertNull (HttpForwardedHeaderParser.parse ("for=\"test\\"));
  }

  @Test
  public void testInvalidTokenCharacters ()
  {
    assertNull (HttpForwardedHeaderParser.parse ("for with space=value"));
    assertNull (HttpForwardedHeaderParser.parse ("for=value;host(invalid)=example.com"));
  }

  @Test
  public void testRealWorldExamples ()
  {
    // Example from RFC 7239
    HttpForwardedHeader aResult = HttpForwardedHeaderParser.parse ("for=192.0.2.60;proto=http;by=203.0.113.43");
    assertNotNull (aResult);
    assertEquals (3, aResult.size ());
    assertEquals ("192.0.2.60", aResult.getFor ());
    assertEquals ("http", aResult.getProto ());
    assertEquals ("203.0.113.43", aResult.getBy ());

    // Another RFC example
    aResult = HttpForwardedHeaderParser.parse ("for=192.0.2.43");
    assertNotNull (aResult);
    assertEquals (1, aResult.size ());
    assertEquals ("192.0.2.43", aResult.getFor ());

    // Complex quoted example
    aResult = HttpForwardedHeaderParser.parse ("for=\"[2001:db8:cafe::17]:4711\"");
    assertNotNull (aResult);
    assertEquals (1, aResult.size ());
    assertEquals ("[2001:db8:cafe::17]:4711", aResult.getFor ());
  }

  @Test
  public void testRoundTripConversion ()
  {
    // Test that parsing and then converting back to string works
    for (final String sOriginal : new String [] { "for=\"192.168.1.1:8080\"",
                                                  "for=\"192.168.1.1:8080\";host=\"example.com with spaces\"",
                                                  "for=\"192.168.1.1:8080\";host=example.com;proto=https",
                                                  "for=\"192.168.1.1:8080\";host=\"example.com with spaces\";proto=https",
                                                  "for=\"192.168.1.1:8080\";host=\"example.com with spaces\";proto=https;custom1=value1",
                                                  "for=\"_gazonk\"",
                                                  "For=\"[2001:db8:cafe::17]:4711\"",
                                                  "for=192.0.2.60;proto=http;by=203.0.113.43",
                                                  "for=192.0.2.43; for=198.51.100.17",
                                                  "for=192.0.2.43; for=\"[2001:db8:cafe::17]\"" })
    {
      // Parse
      final HttpForwardedHeader aParsed = HttpForwardedHeaderParser.parse (sOriginal);
      assertNotNull (aParsed);

      // Format
      final String sRecreated = aParsed.getAsString ();
      assertNotNull (sRecreated);

      // Parse the recreated string to verify it's still valid
      final HttpForwardedHeader aReparsed = HttpForwardedHeaderParser.parse (sRecreated);
      assertNotNull (aReparsed);

      // Check they are equal
      CommonsTestHelper.testDefaultImplementationWithEqualContentObject (aParsed, aReparsed);
    }
  }
}
