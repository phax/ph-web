/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import com.helger.datetime.PDTFactory;

/**
 * Test class for class {@link PDTWebDateUtils}.
 *
 * @author Philip Helger
 */
public final class PDTWebDateUtilsTest
{
  @Test
  public void testRfc822 ()
  {
    final DateTime d = PDTFactory.getCurrentDateTime ().withZone (DateTimeZone.forID ("GMT")).withMillisOfSecond (0);
    final String s = PDTWebDateUtils.getAsStringRFC822 (d);
    assertNotNull (s);
    final DateTime d2 = PDTWebDateUtils.getDateTimeFromRFC822 (s);
    assertNotNull (d2);
    assertEquals (d, d2);

    final String sNow = PDTWebDateUtils.getCurrentDateTimeAsStringRFC822 ();
    assertNotNull (PDTWebDateUtils.getDateTimeFromRFC822 (sNow));
    assertNotNull (PDTWebDateUtils.getAsStringRFC822 ((DateTime) null));
    assertNotNull (PDTWebDateUtils.getAsStringRFC822 ((LocalDateTime) null));
  }

  private static void _testW3C (final DateTime aDT)
  {
    final String s = PDTWebDateUtils.getAsStringW3C (aDT);
    assertNotNull (s);
    final DateTime aDT2 = PDTWebDateUtils.getDateTimeFromW3C (s);
    assertNotNull (aDT2);
    assertEquals (aDT, aDT2);
  }

  @Test
  public void testW3CTime ()
  {
    _testW3C (PDTFactory.getCurrentDateTime ().withMillisOfSecond (0));
    _testW3C (PDTFactory.createDateTime (2010, DateTimeConstants.FEBRUARY, 4));
    _testW3C (PDTFactory.createDateTimeFromMillis (12345678000L));

    final String sNow = PDTWebDateUtils.getCurrentDateTimeAsStringW3C ();
    assertNotNull (PDTWebDateUtils.getDateTimeFromW3C (sNow));
    assertNotNull (PDTWebDateUtils.getAsStringW3C ((DateTime) null));
  }

  @Test
  public void testXSDDateTime ()
  {
    final DateTime aDT = new DateTime ().withChronology (ISOChronology.getInstanceUTC ());
    final String s = PDTWebDateUtils.getAsStringXSD (aDT);
    assertNotNull (s);
    assertEquals (aDT, PDTWebDateUtils.getDateTimeFromXSD (s));

    assertNotNull (PDTWebDateUtils.getAsStringXSD ((DateTime) null));
  }

  @Test
  public void testXSDLocalDate ()
  {
    final LocalDate aDT = PDTFactory.getCurrentLocalDate ();
    final String s = PDTWebDateUtils.getAsStringXSD (aDT);
    assertNotNull (s);
    assertEquals (aDT, PDTWebDateUtils.getLocalDateFromXSD (s));

    assertNotNull (PDTWebDateUtils.getAsStringXSD ((LocalDate) null));
  }
}
