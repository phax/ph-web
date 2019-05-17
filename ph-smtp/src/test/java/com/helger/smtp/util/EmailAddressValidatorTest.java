/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.email.EmailAddressHelper;

public final class EmailAddressValidatorTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EmailAddressValidatorTest.class);
  private static final String [] VALID = new String [] { "ph@helger.com" };
  private static final String [] INVALID = new String [] { "ph@helger", "karin@gmx-net" };

  @Test
  public void testWithoutMXCheck ()
  {
    for (final String sValid : VALID)
      assertTrue (sValid, EmailAddressHelper.isValid (sValid));
    for (final String sInvalid : INVALID)
      assertFalse (sInvalid, EmailAddressHelper.isValid (sInvalid));
  }

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
