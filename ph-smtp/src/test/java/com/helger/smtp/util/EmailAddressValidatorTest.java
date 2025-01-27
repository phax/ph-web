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
package com.helger.smtp.util;

import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmailAddressValidatorTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EmailAddressValidatorTest.class);
  // "aa@bb" or "aa.bb@cc" are valid according to the spec, but not correctly
  // handled by the RegEx
  private static final String [] VALID = { "ph@helger.com",
                                           "tim.tester@test.com",
                                           "tim-tester@test.com",
                                           "tim.tester@test.sub.com",
                                           "tim-tester@test.sub.com",
                                           "tim.tester@test.sub.sub.sub.sub.sub.sub.com",
                                           "tim-tester@test.sub.sub.sub.sub.sub.sub.com",
                                           "a.b@c.d",
                                           "abc@bcd.def" };

  @Test
  public void testWithMXCheck ()
  {
    // Note: not all email addresses provided are valid so there may not
    // necessarily be MX records! But there should not be more than 5 warnings
    // (in average 2-3)
    final Random aRandom = new Random ();
    for (int i = 0; i < 20; ++i)
    {
      final int nIndex = aRandom.nextInt (VALID.length);
      final String sValid = VALID[nIndex];
      if (!EmailAddressValidator.isValidWithMXCheck (sValid))
        LOGGER.info ("No MX record for: " + sValid);
    }
  }
}
