/**
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
package com.helger.web.smtp.failed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.helger.commons.charset.CCharset;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.idfactory.GlobalIDFactory;
import com.helger.commons.idfactory.MemoryIntIDFactory;
import com.helger.commons.mock.MockException;
import com.helger.commons.mock.PHTestUtils;
import com.helger.datetime.PDTFactory;
import com.helger.web.smtp.EEmailType;
import com.helger.web.smtp.ISMTPSettings;
import com.helger.web.smtp.impl.EmailAttachment;
import com.helger.web.smtp.impl.EmailAttachmentList;
import com.helger.web.smtp.impl.EmailData;
import com.helger.web.smtp.impl.SMTPSettings;

/**
 * Test class for class {@link FailedMailData}.
 *
 * @author Philip Helger
 */
public final class FailedMailDataTest
{
  static
  {
    GlobalIDFactory.setPersistentIntIDFactory (new MemoryIntIDFactory ());
  }

  @Nonnull
  private static SMTPSettings _createSMTPSettings ()
  {
    return new SMTPSettings ("mail.example.com", 19, "anyuser", "secret", CCharset.CHARSET_UTF_8, true);
  }

  @Nonnull
  private static EmailData _createEmailData ()
  {
    final EmailData aData = new EmailData (EEmailType.TEXT);
    aData.setFrom (new EmailAddress ("from@example.org"));
    aData.setReplyTo (new EmailAddress ("replyto1@example.org"),
                      new EmailAddress ("replyto2@example.org"),
                      new EmailAddress ("replyto3@example.org"));
    aData.setTo (new EmailAddress ("to1@example.org"),
                 new EmailAddress ("to2@example.org"),
                 new EmailAddress ("to3@example.org"));
    aData.setCc (new EmailAddress ("cc1@example.org"),
                 new EmailAddress ("cc2@example.org"),
                 new EmailAddress ("cc3@example.org"));
    aData.setBcc (new EmailAddress ("bcc1@example.org"),
                  new EmailAddress ("bcc2@example.org"),
                  new EmailAddress ("bcc3@example.org"));
    aData.setSentDate (PDTFactory.getCurrentDateTime ());
    aData.setSubject ("This is a test");
    aData.setBody ("This is my mail that failed\nIt contains a lot of information.\nBla bla bla\nAll the best to you.");
    final EmailAttachmentList aAttachments = new EmailAttachmentList ();
    aAttachments.addAttachment (new EmailAttachment ("file1.txt", "Bla foo fasel".getBytes (CCharset.CHARSET_UTF_8_OBJ)));
    aAttachments.addAttachment (new EmailAttachment ("file2.txt",
                                                     "Bla foo fasel. Bla foo fasel.".getBytes (CCharset.CHARSET_UTF_8_OBJ)));
    aData.setAttachments (aAttachments);
    // Some custom attributes for fun
    aData.setAttribute ("Attr1", "3.14");
    aData.setAttribute ("Attr2", "Test attribute\nWe are multiline!");
    return aData;
  }

  @Test
  public void testWithException ()
  {
    final ISMTPSettings aSettings = _createSMTPSettings ();
    final Throwable aError = new MockException ("Test error");

    final FailedMailData aFMD = new FailedMailData (aSettings, aError);
    assertNotNull (aFMD.getID ());
    assertNotNull (aFMD.getErrorDateTime ());
    assertEquals (aSettings, aFMD.getSMTPSettings ());
    assertNull (aFMD.getOriginalSentDateTime ());
    assertNull (aFMD.getEmailData ());
    assertNotNull (aFMD.getMessageDisplayText ());
    assertNotNull (aFMD.getError ());

    PHTestUtils.testMicroTypeConversion (aFMD);
  }

  @Test
  public void testWithData ()
  {
    final ISMTPSettings aSettings = _createSMTPSettings ();
    final EmailData aData = _createEmailData ();

    final FailedMailData aFMD = new FailedMailData (aSettings, aData);
    assertNotNull (aFMD.getID ());
    assertNotNull (aFMD.getErrorDateTime ());
    assertEquals (aSettings, aFMD.getSMTPSettings ());
    assertEquals (aData.getSentDate (), aFMD.getOriginalSentDateTime ());
    assertEquals (aData, aFMD.getEmailData ());
    assertNull (aFMD.getMessageDisplayText ());
    assertNull (aFMD.getError ());

    PHTestUtils.testMicroTypeConversion (aFMD);
  }

  @Test
  public void testWithExceptionAndData ()
  {
    final ISMTPSettings aSettings = _createSMTPSettings ();
    final EmailData aData = _createEmailData ();
    final Throwable aError = new MockException ("Test error");

    final FailedMailData aFMD = new FailedMailData (aSettings, aData, aError);
    assertNotNull (aFMD.getID ());
    assertNotNull (aFMD.getErrorDateTime ());
    assertEquals (aSettings, aFMD.getSMTPSettings ());
    assertEquals (aData.getSentDate (), aFMD.getOriginalSentDateTime ());
    assertEquals (aData, aFMD.getEmailData ());
    assertNotNull (aFMD.getMessageDisplayText ());
    assertNotNull (aFMD.getError ());

    PHTestUtils.testMicroTypeConversion (aFMD);
  }
}
