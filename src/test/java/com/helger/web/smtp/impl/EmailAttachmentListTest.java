/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.smtp.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.PHTestUtils;

/**
 * Test class for class {@link EmailAttachment}.
 *
 * @author Philip Helger
 */
public final class EmailAttachmentListTest
{
  @Test
  public void testBasic ()
  {
    final EmailAttachmentList aList = new EmailAttachmentList ();
    assertTrue (aList.isEmpty ());
    assertEquals (0, aList.size ());
    assertNotNull (aList.getAllAttachmentFilenames ());
    assertNotNull (aList.getAllAttachments ());
    assertNotNull (aList.getAsDataSourceList ());

    aList.addAttachment (new EmailAttachment ("test.txt", "Inhalt".getBytes ()));
    assertFalse (aList.isEmpty ());
    assertEquals (1, aList.size ());

    aList.addAttachment (new EmailAttachment ("test2.txt", "Inhalt2".getBytes ()));
    assertFalse (aList.isEmpty ());
    assertEquals (2, aList.size ());

    aList.addAttachment (new EmailAttachment ("test2.txt", "Override".getBytes ()));
    assertFalse (aList.isEmpty ());
    assertEquals (2, aList.size ());

    PHTestUtils.testMicroTypeConversion (aList);
  }
}
