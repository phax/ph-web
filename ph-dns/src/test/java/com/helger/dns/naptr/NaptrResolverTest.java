/*
 * Copyright (C) 2020-2026 Philip Helger (www.helger.com)
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
package com.helger.dns.naptr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import com.helger.collection.commons.CommonsArrayList;

/**
 * Test class for class {@link NaptrResolver}.
 *
 * @author Philip Helger
 */
public class NaptrResolverTest
{
  private static final String DOMAIN = "example.org";

  private static NAPTRRecord _createRecord (final int nOrder,
                                            final int nPreference,
                                            final String sFlags,
                                            final String sService,
                                            final String sRegexp) throws TextParseException
  {
    return new NAPTRRecord (Name.fromString (DOMAIN + "."),
                            DClass.IN,
                            3600,
                            nOrder,
                            nPreference,
                            sFlags,
                            sService,
                            sRegexp,
                            Name.root);
  }

  @Test
  public void testGetAppliedNAPTRRegEx ()
  {
    assertEquals ("http://test-infra.peppol.at",
                  NaptrResolver.getAppliedNAPTRRegEx ("!^.*$!http://test-infra.peppol.at!", "bla.foo.example.org"));
    assertEquals ("http://test-infra.peppol.at",
                  NaptrResolver.getAppliedNAPTRRegEx ("!.*!http://test-infra.peppol.at!", "bla.foo.example.org"));
  }

  @Test
  public void testGetAppliedNAPTRRegExWithDifferentSeparators ()
  {
    // '~' as separator
    assertEquals ("http://example.com",
                  NaptrResolver.getAppliedNAPTRRegEx ("~^.*$~http://example.com~", "test.domain.org"));
    // '#' as separator
    assertEquals ("https://example.com",
                  NaptrResolver.getAppliedNAPTRRegEx ("#^.*$#https://example.com#", "test.domain.org"));
  }

  @Test
  public void testGetAppliedNAPTRRegExWithCaseInsensitiveFlag ()
  {
    // "i" flag -> case insensitive
    assertEquals ("http://result.com",
                  NaptrResolver.getAppliedNAPTRRegEx ("!^.*$!http://result.com!i", "UPPER.CASE.ORG"));
    // "I" flag -> also case insensitive
    assertEquals ("http://result.com",
                  NaptrResolver.getAppliedNAPTRRegEx ("!^.*$!http://result.com!I", "UPPER.CASE.ORG"));
  }

  @Test
  public void testGetAppliedNAPTRRegExWithCaptureGroups ()
  {
    // Capture group replacement: domain "sub.foo.example.com" has 4 dot-separated parts
    // greedy matching: group1=sub, group2=foo, group3=example.com
    assertEquals ("http://sub.example.com",
                  NaptrResolver.getAppliedNAPTRRegEx ("!^([^.]+)\\.([^.]+)\\.(.+)$!http://$1.$3!",
                                                      "sub.foo.example.com"));
  }

  @Test
  public void testGetAppliedNAPTRRegExNoFlags ()
  {
    // Empty flags string -> no special options
    assertEquals ("http://noflag.com",
                  NaptrResolver.getAppliedNAPTRRegEx ("!^.*$!http://noflag.com!", "any.domain.org"));
  }

  @Test
  public void testGetAppliedNAPTRRegExMissingSecondSeparator ()
  {
    // Only one separator char -> null
    assertNull (NaptrResolver.getAppliedNAPTRRegEx ("!noSeparator", "test.org"));
  }

  @Test
  public void testGetAppliedNAPTRRegExMissingThirdSeparator ()
  {
    // Two separators but no third -> null
    assertNull (NaptrResolver.getAppliedNAPTRRegEx ("!^.*$!http://example.com", "test.org"));
  }

  @Test
  public void testResolveUNAPTRWithSingleMatchingRecord () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRNoMatchingFlags () throws TextParseException
  {
    // Record has flag "S" but we look for "U"
    final NAPTRRecord aRecord = _createRecord (100, 10, "S", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertNull (aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRNoMatchingService () throws TextParseException
  {
    // Flags match but service doesn't
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:OTHER"));
    assertNull (aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTREmptyRecordList ()
  {
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertNull (aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTROrderAndPreference () throws TextParseException
  {
    // Record with higher order but better regex
    final NAPTRRecord aRecord1 = _createRecord (200, 10, "U", "Meta:SMP", "!^.*$!http://second.example.com!");
    // Record with lower order -> should be selected first
    final NAPTRRecord aRecord2 = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://first.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord1, aRecord2),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://first.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRPreferenceTiebreak () throws TextParseException
  {
    // Same order, different preference
    final NAPTRRecord aRecord1 = _createRecord (100, 20, "U", "Meta:SMP", "!^.*$!http://second.example.com!");
    final NAPTRRecord aRecord2 = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://first.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord1, aRecord2),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://first.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRCaseInsensitiveFlagsAndService () throws TextParseException
  {
    // Lower-case flag "u" should match default "U" matcher
    final NAPTRRecord aRecord = _createRecord (100, 10, "u", "meta:smp", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRRecordWithTooShortRegex () throws TextParseException
  {
    // Regex of length <= 3 should be skipped (need at least 3 separator chars)
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!!!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertNull (aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRRecordWithEmptyRegex () throws TextParseException
  {
    // Empty regex
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertNull (aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRMultipleRecordsMixedFlags () throws TextParseException
  {
    // Only the "U" record should be considered
    final NAPTRRecord aRecordS = _createRecord (50, 10, "S", "Meta:SMP", "!^.*$!http://wrong.example.com!");
    final NAPTRRecord aRecordU = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://right.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecordS, aRecordU),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://right.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testResolveUNAPTRSkipsInvalidRegexPicksNext () throws TextParseException
  {
    // First record has a malformed regex (missing third separator), second is valid
    final NAPTRRecord aRecord1 = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://bad.example.com");
    final NAPTRRecord aRecord2 = _createRecord (100, 20, "U", "Meta:SMP", "!^.*$!http://good.example.com!");
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord1, aRecord2),
                                                       NaptrResolver.getDefaultFlagsMatcher ("U"),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://good.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testDeprecatedConstructor () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    @SuppressWarnings ("removal")
    final NaptrResolver aResolver = new NaptrResolver (DOMAIN,
                                                       new CommonsArrayList <> (aRecord),
                                                       NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderMinimal () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .naptrRecords (new CommonsArrayList <> (aRecord))
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderWithNameObject () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (Name.fromString (DOMAIN + "."))
                                                 .naptrRecords (new CommonsArrayList <> (aRecord))
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertNotNull (aResolver);
  }

  @Test
  public void testBuilderDomainNameNull ()
  {
    try
    {
      NaptrResolver.builder ()
                   .domainName ((String) null)
                   .serviceName ("Meta:SMP")
                   .naptrRecords (new CommonsArrayList <> ())
                   .build ();
      fail ("Expected IllegalStateException");
    }
    catch (final IllegalStateException ex)
    {
      // expected
    }
  }

  @Test
  public void testBuilderMissingServiceName () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    try
    {
      NaptrResolver.builder ().domainName (DOMAIN).naptrRecords (new CommonsArrayList <> (aRecord)).build ();
      fail ("Expected IllegalStateException");
    }
    catch (final IllegalStateException ex)
    {
      // expected - service name matcher is required
    }
  }

  @Test
  public void testBuilderWithCustomFlags () throws TextParseException
  {
    // Use flag "S" instead of default "U"
    final NAPTRRecord aRecordU = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://wrong.example.com!");
    final NAPTRRecord aRecordS = _createRecord (100, 10, "S", "Meta:SMP", "!^.*$!http://right.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .naptrRecords (new CommonsArrayList <> (aRecordU, aRecordS))
                                                 .flags ("S")
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://right.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderWithPredicateFlags () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    // Custom predicate that accepts any flag
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .naptrRecords (new CommonsArrayList <> (aRecord))
                                                 .flags (x -> true)
                                                 .serviceName (x -> true)
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderNaptrRecordSingular () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .naptrRecord (aRecord)
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderNaptrRecordNullClears () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    // Setting a record then clearing with null
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .naptrRecord (aRecord)
                                                 .naptrRecord (null)
                                                 .naptrRecord (aRecord)
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderAddNaptrRecordIgnoresNull () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .addNaptrRecord (null)
                                                 .addNaptrRecord (aRecord)
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderAddNaptrRecordsArray () throws TextParseException
  {
    final NAPTRRecord aRecord1 = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://first.example.com!");
    final NAPTRRecord aRecord2 = _createRecord (200, 10, "U", "Meta:SMP", "!^.*$!http://second.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .addNaptrRecords (aRecord1, aRecord2)
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://first.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderAddNaptrRecordsIterable () throws TextParseException
  {
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .addNaptrRecords (new CommonsArrayList <> (aRecord))
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testBuilderNaptrRecordsArrayNull () throws TextParseException
  {
    // Passing null array clears
    final NAPTRRecord aRecord = _createRecord (100, 10, "U", "Meta:SMP", "!^.*$!http://smp.example.com!");
    final NaptrResolver aResolver = NaptrResolver.builder ()
                                                 .domainName (DOMAIN)
                                                 .naptrRecords (aRecord)
                                                 .naptrRecords ((NAPTRRecord []) null)
                                                 .naptrRecords (aRecord)
                                                 .serviceName ("Meta:SMP")
                                                 .build ();
    assertEquals ("http://smp.example.com", aResolver.resolveUNAPTR ());
  }

  @Test
  public void testGetDefaultFlagsMatcher ()
  {
    assertNotNull (NaptrResolver.getDefaultFlagsMatcher ("U"));
    assertNotNull (NaptrResolver.getDefaultFlagsMatcher ("S"));
  }

  @Test
  public void testGetDefaultServiceNameMatcher ()
  {
    assertNotNull (NaptrResolver.getDefaultServiceNameMatcher ("Meta:SMP"));
  }
}
