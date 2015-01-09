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
package com.helger.web.smtp.transport;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.helger.commons.GlobalDebug;
import com.helger.commons.concurrent.ThreadUtils;
import com.helger.commons.email.EmailAddress;
import com.helger.commons.idfactory.GlobalIDFactory;
import com.helger.commons.idfactory.MemoryIntIDFactory;
import com.helger.commons.io.IReadableResource;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.microdom.convert.MicroTypeConverter;
import com.helger.commons.microdom.serialize.MicroReader;
import com.helger.web.smtp.EEmailType;
import com.helger.web.smtp.EmailGlobalSettings;
import com.helger.web.smtp.IEmailData;
import com.helger.web.smtp.impl.EmailData;
import com.helger.web.smtp.impl.SMTPSettings;
import com.helger.web.smtp.transport.listener.LoggingConnectionListener;
import com.helger.web.smtp.transport.listener.LoggingTransportListener;

/**
 * Test class for class {@link MailAPI}.
 *
 * @author Philip Helger
 */
public final class MailAPITest
{
  static
  {
    GlobalIDFactory.setPersistentIntIDFactory (new MemoryIntIDFactory ());
  }

  @Ignore ("to avoid spamming my mailbox")
  @Test
  public void testBasic ()
  {
    // This file might not be present, as it contains the real-life SMTP
    // settings. It should reside in src/test/resource and is SVN ignored by
    // name
    final IReadableResource aRes = new ClassPathResource ("smtp-settings.xml");
    if (aRes.exists ())
    {
      final boolean bOldIsDebug = GlobalDebug.isDebugMode ();
      GlobalDebug.setDebugModeDirect (true);
      try
      {
        EmailGlobalSettings.enableJavaxMailDebugging (GlobalDebug.isDebugMode ());

        // Setup debug listeners
        EmailGlobalSettings.setConnectionListener (new LoggingConnectionListener ());
        EmailGlobalSettings.setTransportListener (new LoggingTransportListener ());

        final SMTPSettings aSMTPSettings = MicroTypeConverter.convertToNative (MicroReader.readMicroXML (aRes)
                                                                                          .getDocumentElement (),
                                                                               SMTPSettings.class);
        final IEmailData aMailData = new EmailData (EEmailType.TEXT);
        aMailData.setTo (new EmailAddress ("ph@helger.com"));
        aMailData.setFrom (new EmailAddress ("auto@helger.com"));
        aMailData.setSubject ("JÜnit test with späcial käräktärs");
        aMailData.setBody ("Hi there\nLine 2\n4 special chars: äöüß\n123456789\nBest regards: ph-web");
        MailAPI.queueMail (aSMTPSettings, aMailData);
        MailAPI.stop ();

        // try to queue again after MailAPI was stopped - should end up in
        // failed
        // mail queue
        assertEquals (0, MailAPI.getFailedMailQueue ().size ());
        MailAPI.queueMail (aSMTPSettings, aMailData);
        assertEquals (1, MailAPI.getFailedMailQueue ().size ());
      }
      finally
      {
        GlobalDebug.setDebugModeDirect (bOldIsDebug);
      }
    }
  }

  @Ignore ("to avoid spamming my mailbox")
  @Test
  public void testStopImmediately ()
  {
    // This file might not be present, as it contains the real-life SMTP
    // settings. It should reside in src/test/resource and is SVN ignored by
    // name
    final IReadableResource aRes = new ClassPathResource ("smtp-settings.xml");
    if (aRes.exists ())
    {
      final SMTPSettings aSMTPSettings = MicroTypeConverter.convertToNative (MicroReader.readMicroXML (aRes)
                                                                                        .getDocumentElement (),
                                                                             SMTPSettings.class);
      final IEmailData aMailData = new EmailData (EEmailType.TEXT);
      aMailData.setTo (new EmailAddress ("ph@helger.com"));
      aMailData.setFrom (new EmailAddress ("auto@helger.com"));
      aMailData.setSubject ("JÜnit test with späcial käräktärs");
      aMailData.setBody ("Hi there\nLine 2\n4 special chars: äöüß\n123456789\nBest regards: ph-web");

      final List <IEmailData> aMails = new ArrayList <IEmailData> ();
      for (int i = 0; i < 10; ++i)
        aMails.add (aMailData);

      MailAPI.queueMails (aSMTPSettings, aMails);
      ThreadUtils.sleep (20);

      // Stop immediately
      MailAPI.stop (true);
    }
  }
}
