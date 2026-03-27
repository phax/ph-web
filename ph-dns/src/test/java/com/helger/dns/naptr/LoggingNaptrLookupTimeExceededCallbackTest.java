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
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.Test;

/**
 * Test class for class {@link LoggingNaptrLookupTimeExceededCallback}.
 *
 * @author Philip Helger
 */
public class LoggingNaptrLookupTimeExceededCallbackTest
{
  @Test
  public void testConstructorNoStackTrace ()
  {
    final LoggingNaptrLookupTimeExceededCallback aCB = new LoggingNaptrLookupTimeExceededCallback (false);
    assertFalse (aCB.isEmitStackTrace ());
  }

  @Test
  public void testConstructorWithStackTrace ()
  {
    final LoggingNaptrLookupTimeExceededCallback aCB = new LoggingNaptrLookupTimeExceededCallback (true);
    assertTrue (aCB.isEmitStackTrace ());
  }

  @Test
  public void testSetEmitStackTrace ()
  {
    final LoggingNaptrLookupTimeExceededCallback aCB = new LoggingNaptrLookupTimeExceededCallback (false);
    assertFalse (aCB.isEmitStackTrace ());

    final LoggingNaptrLookupTimeExceededCallback ret = aCB.setEmitStackTrace (true);
    assertTrue (aCB.isEmitStackTrace ());
    assertEquals (aCB, ret);
  }

  @Test
  public void testOnLookupTimeExceededNoStackTrace ()
  {
    final LoggingNaptrLookupTimeExceededCallback aCB = new LoggingNaptrLookupTimeExceededCallback (false);
    // Should not throw
    aCB.onLookupTimeExceeded ("test message", Duration.ofMillis (2000), Duration.ofMillis (1000));
  }

  @Test
  public void testOnLookupTimeExceededWithStackTrace ()
  {
    final LoggingNaptrLookupTimeExceededCallback aCB = new LoggingNaptrLookupTimeExceededCallback (true);
    // Should not throw
    aCB.onLookupTimeExceeded ("test message", Duration.ofMillis (5000), Duration.ofMillis (1000));
  }

  @Test
  public void testToString ()
  {
    final LoggingNaptrLookupTimeExceededCallback aCB = new LoggingNaptrLookupTimeExceededCallback (false);
    final String sStr = aCB.toString ();
    assertNotNull (sStr);
    assertTrue (sStr.contains ("EmitStackTrace"));
  }
}
