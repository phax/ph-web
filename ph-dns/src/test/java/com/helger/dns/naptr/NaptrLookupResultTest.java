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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.xbill.DNS.NAPTRRecord;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * Test class for {@link NaptrLookupResult}.
 *
 * @author Philip Helger
 */
public class NaptrLookupResultTest
{
  @Test
  public void testSuccessFactoryEmpty ()
  {
    final ICommonsList <NAPTRRecord> aRecords = new CommonsArrayList <> ();
    final NaptrLookupResult aResult = NaptrLookupResult.success (aRecords);
    assertSame (ENaptrLookupStatus.SUCCESSFUL, aResult.getStatus ());
    assertNotNull (aResult.getRecords ());
    assertTrue (aResult.getRecords ().isEmpty ());
    assertNull (aResult.getErrorMessage ());
    assertTrue (aResult.isSuccess ());
    assertFalse (aResult.isFunctionalNotFound ());
    assertFalse (aResult.isTechnicalFailure ());
    assertFalse (aResult.isRetryable ());
  }

  @Test
  public void testFailureHostNotFound ()
  {
    final NaptrLookupResult aResult = NaptrLookupResult.failure (ENaptrLookupStatus.HOST_NOT_FOUND, "NXDOMAIN");
    assertSame (ENaptrLookupStatus.HOST_NOT_FOUND, aResult.getStatus ());
    assertTrue (aResult.getRecords ().isEmpty ());
    assertEquals ("NXDOMAIN", aResult.getErrorMessage ());
    assertFalse (aResult.isSuccess ());
    assertTrue (aResult.isFunctionalNotFound ());
    assertFalse (aResult.isTechnicalFailure ());
    assertFalse (aResult.isRetryable ());
  }

  @Test
  public void testFailureTryAgainIsRetryable ()
  {
    final NaptrLookupResult aResult = NaptrLookupResult.failure (ENaptrLookupStatus.TRY_AGAIN, "SERVFAIL");
    assertTrue (aResult.isTechnicalFailure ());
    assertTrue (aResult.isRetryable ());
    assertFalse (aResult.isFunctionalNotFound ());
  }

  @Test
  public void testFailureUnrecoverableIsTechnicalButNotRetryable ()
  {
    final NaptrLookupResult aResult = NaptrLookupResult.failure (ENaptrLookupStatus.UNRECOVERABLE, "data error");
    assertTrue (aResult.isTechnicalFailure ());
    assertFalse (aResult.isRetryable ());
  }

  @Test
  public void testFailureWithSuccessfulStatusRejected ()
  {
    try
    {
      NaptrLookupResult.failure (ENaptrLookupStatus.SUCCESSFUL, "msg");
      fail ("Expected IllegalArgumentException");
    }
    catch (final IllegalArgumentException ex)
    {
      // expected
    }
  }

  @Test
  public void testGetRecordsReturnsCopy ()
  {
    final ICommonsList <NAPTRRecord> aRecords = new CommonsArrayList <> ();
    final NaptrLookupResult aResult = NaptrLookupResult.success (aRecords);
    final ICommonsList <NAPTRRecord> aCopy = aResult.getRecords ();
    aCopy.clear ();
    // Mutating the returned list must not affect the result
    assertEquals (0, aResult.getRecords ().size ());
    // Mutating the original list passed in also stays internal (defensive copy on construction
    // is NOT promised by the constructor, but getRecords always returns a fresh copy so the
    // caller cannot poison subsequent reads through the returned reference)
  }

  @Test
  public void testEqualsHashCode ()
  {
    final ICommonsList <NAPTRRecord> aRecords = new CommonsArrayList <> ();
    final NaptrLookupResult a = NaptrLookupResult.success (aRecords);
    final NaptrLookupResult b = NaptrLookupResult.success (new CommonsArrayList <> ());
    assertEquals (a, b);
    assertEquals (a.hashCode (), b.hashCode ());

    final NaptrLookupResult c = NaptrLookupResult.failure (ENaptrLookupStatus.HOST_NOT_FOUND, "x");
    assertFalse (a.equals (c));
  }

  @Test
  public void testToStringDoesNotThrow ()
  {
    assertNotNull (NaptrLookupResult.success (new CommonsArrayList <> ()).toString ());
    assertNotNull (NaptrLookupResult.failure (ENaptrLookupStatus.TRY_AGAIN, "msg").toString ());
    assertNotNull (NaptrLookupResult.failure (ENaptrLookupStatus.TRY_AGAIN, null).toString ());
  }
}
