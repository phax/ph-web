/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.smtp.failed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.helger.commons.charset.CCharset;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.exception.mock.MockException;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.id.factory.MemoryIntIDFactory;
import com.helger.smtp.data.EEmailType;
import com.helger.smtp.data.EmailAttachment;
import com.helger.smtp.data.EmailAttachmentList;
import com.helger.smtp.data.EmailData;
import com.helger.smtp.settings.ISMTPSettings;
import com.helger.smtp.settings.SMTPSettings;
import com.helger.smtp.transport.MailTransportError;
import com.helger.xml.mock.XMLTestHelper;

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
    return new SMTPSettings ("mail.example.com", 19, "anyuser", "secret", CCharset.CHARSET_UTF_8_OBJ, true);
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
    aData.setSentDateTime (PDTFactory.getCurrentLocalDateTime ());
    aData.setSubject ("This is a test");
    aData.setBody ("This is my mail that failed\nIt contains a lot of information.\nBla bla bla\nAll the best to you.");
    final EmailAttachmentList aAttachments = new EmailAttachmentList ();
    aAttachments.addAttachment (new EmailAttachment ("file1.txt",
                                                     "Bla foo fasel".getBytes (CCharset.CHARSET_UTF_8_OBJ)));
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

    final FailedMailData aFMD = new FailedMailData (aSettings, new MailTransportError (aError));
    assertNotNull (aFMD.getID ());
    assertNotNull (aFMD.getErrorDateTime ());
    assertEquals (aSettings, aFMD.getSMTPSettings ());
    assertNull (aFMD.getOriginalSentDateTime ());
    assertNull (aFMD.getEmailData ());
    assertTrue (aFMD.hasTransportError ());
    assertNotNull (aFMD.getTransportThrowableMessage ());
    assertNotNull (aFMD.getTransportError ());

    XMLTestHelper.testMicroTypeConversion (aFMD);
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
    assertEquals (aData.getSentDateTime (), aFMD.getOriginalSentDateTime ());
    assertEquals (aData, aFMD.getEmailData ());
    assertFalse (aFMD.hasTransportError ());
    assertNull (aFMD.getTransportThrowableMessage ());
    assertNull (aFMD.getTransportError ());

    XMLTestHelper.testMicroTypeConversion (aFMD);
  }

  @Test
  public void testWithExceptionAndData ()
  {
    final ISMTPSettings aSettings = _createSMTPSettings ();
    final EmailData aData = _createEmailData ();
    final Throwable aError = new MockException ("Test error");

    final FailedMailData aFMD = new FailedMailData (aSettings, aData, new MailTransportError (aError));
    assertNotNull (aFMD.getID ());
    assertNotNull (aFMD.getErrorDateTime ());
    assertEquals (aSettings, aFMD.getSMTPSettings ());
    assertEquals (aData.getSentDateTime (), aFMD.getOriginalSentDateTime ());
    assertEquals (aData, aFMD.getEmailData ());
    assertTrue (aFMD.hasTransportError ());
    assertNotNull (aFMD.getTransportThrowableMessage ());
    assertNotNull (aFMD.getTransportError ());

    XMLTestHelper.testMicroTypeConversion (aFMD);
  }
}
