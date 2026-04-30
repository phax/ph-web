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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xbill.DNS.Lookup;

/**
 * Test class for {@link ENaptrLookupStatus}.
 *
 * @author Philip Helger
 */
public class ENaptrLookupStatusTest
{
  @Test
  public void testSuccessful ()
  {
    final ENaptrLookupStatus e = ENaptrLookupStatus.SUCCESSFUL;
    assertTrue (e.isSuccess ());
    assertFalse (e.isFunctionalNotFound ());
    assertFalse (e.isTechnicalFailure ());
    assertFalse (e.isRetryable ());
    assertEquals (Lookup.SUCCESSFUL, e.getDnsJavaResultCode ());
  }

  @Test
  public void testHostNotFound ()
  {
    final ENaptrLookupStatus e = ENaptrLookupStatus.HOST_NOT_FOUND;
    assertFalse (e.isSuccess ());
    assertTrue (e.isFunctionalNotFound ());
    assertFalse (e.isTechnicalFailure ());
    assertFalse (e.isRetryable ());
    assertEquals (Lookup.HOST_NOT_FOUND, e.getDnsJavaResultCode ());
  }

  @Test
  public void testTypeNotFound ()
  {
    final ENaptrLookupStatus e = ENaptrLookupStatus.TYPE_NOT_FOUND;
    assertFalse (e.isSuccess ());
    assertTrue (e.isFunctionalNotFound ());
    assertFalse (e.isTechnicalFailure ());
    assertFalse (e.isRetryable ());
    assertEquals (Lookup.TYPE_NOT_FOUND, e.getDnsJavaResultCode ());
  }

  @Test
  public void testTryAgain ()
  {
    final ENaptrLookupStatus e = ENaptrLookupStatus.TRY_AGAIN;
    assertFalse (e.isSuccess ());
    assertFalse (e.isFunctionalNotFound ());
    assertTrue (e.isTechnicalFailure ());
    assertTrue (e.isRetryable ());
    assertEquals (Lookup.TRY_AGAIN, e.getDnsJavaResultCode ());
  }

  @Test
  public void testUnrecoverable ()
  {
    final ENaptrLookupStatus e = ENaptrLookupStatus.UNRECOVERABLE;
    assertFalse (e.isSuccess ());
    assertFalse (e.isFunctionalNotFound ());
    assertTrue (e.isTechnicalFailure ());
    assertFalse (e.isRetryable ());
    assertEquals (Lookup.UNRECOVERABLE, e.getDnsJavaResultCode ());
  }

  @Test
  public void testFromDnsJavaResultCodeAllKnown ()
  {
    assertSame (ENaptrLookupStatus.SUCCESSFUL, ENaptrLookupStatus.fromDnsJavaResultCode (Lookup.SUCCESSFUL));
    assertSame (ENaptrLookupStatus.UNRECOVERABLE, ENaptrLookupStatus.fromDnsJavaResultCode (Lookup.UNRECOVERABLE));
    assertSame (ENaptrLookupStatus.TRY_AGAIN, ENaptrLookupStatus.fromDnsJavaResultCode (Lookup.TRY_AGAIN));
    assertSame (ENaptrLookupStatus.HOST_NOT_FOUND, ENaptrLookupStatus.fromDnsJavaResultCode (Lookup.HOST_NOT_FOUND));
    assertSame (ENaptrLookupStatus.TYPE_NOT_FOUND, ENaptrLookupStatus.fromDnsJavaResultCode (Lookup.TYPE_NOT_FOUND));
  }

  @Test
  public void testFromDnsJavaResultCodeFallback ()
  {
    // Unknown codes fall back to UNRECOVERABLE (defensive against future dnsjava additions)
    assertSame (ENaptrLookupStatus.UNRECOVERABLE, ENaptrLookupStatus.fromDnsJavaResultCode (-1));
    assertSame (ENaptrLookupStatus.UNRECOVERABLE, ENaptrLookupStatus.fromDnsJavaResultCode (999));
  }
}
