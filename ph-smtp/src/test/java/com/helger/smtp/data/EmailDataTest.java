/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.smtp.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.commons.email.EmailAddress;
import com.helger.commons.mock.CommonsTestHelper;
import com.helger.xml.mock.XMLTestHelper;

public final class EmailDataTest
{
  @Test
  public void testBasic ()
  {
    final EmailData aEmailData = new EmailData (EEmailType.TEXT);
    final EmailAddress aMA = new EmailAddress ("ph@helger.com", "Philip");

    aEmailData.setFrom (aMA);
    assertEquals (aMA, aEmailData.getFrom ());

    aEmailData.setReplyTo (aMA);
    assertEquals (aMA, aEmailData.getAllReplyTo ().get (0));

    aEmailData.setTo (aMA);
    assertEquals (aMA, aEmailData.getAllTo ().get (0));

    aEmailData.setCc (aMA);
    assertEquals (aMA, aEmailData.getAllCc ().get (0));

    aEmailData.setBcc (aMA);
    assertEquals (aMA, aEmailData.getAllBcc ().get (0));

    CommonsTestHelper.testDefaultSerialization (aEmailData);
    XMLTestHelper.testMicroTypeConversion (aEmailData);

    assertEquals (0, aEmailData.attrs ().size ());
    aEmailData.attrs ().setAttribute ("test", "foo");
    assertEquals (1, aEmailData.attrs ().size ());
    aEmailData.attrs ().setAttribute ("test2", "bar");
    assertEquals (2, aEmailData.attrs ().size ());

    XMLTestHelper.testMicroTypeConversion (aEmailData);
    CommonsTestHelper.testDefaultSerialization (aEmailData);
  }
}
