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
package com.helger.mail.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.helger.commons.email.EmailAddress;
import com.helger.commons.typeconvert.TypeConverter;

import jakarta.mail.internet.InternetAddress;

/**
 * Test {@link TypeConverter} registrations.
 *
 * @author Philip Helger
 */
public final class TypeConverterFuncTest
{
  @Test
  public void testEmailAddress ()
  {
    final EmailAddress aEA = new EmailAddress ("spam@helger.com", "Philip");
    final InternetAddress aIA = TypeConverter.convert (aEA, InternetAddress.class);
    assertNotNull (aIA);
    final EmailAddress aEA2 = TypeConverter.convert (aIA, EmailAddress.class);
    assertNotNull (aEA2);
    assertEquals (aEA, aEA2);
  }
}
