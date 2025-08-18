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
package com.helger.smtp.transport;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.helger.base.concurrent.ThreadHelper;
import com.helger.base.debug.GlobalDebug;
import com.helger.base.email.EmailAddress;
import com.helger.base.id.factory.GlobalIDFactory;
import com.helger.base.id.factory.MemoryIntIDFactory;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.level.EErrorLevel;
import com.helger.io.resource.ClassPathResource;
import com.helger.io.resource.IReadableResource;
import com.helger.smtp.EmailGlobalSettings;
import com.helger.smtp.data.EEmailType;
import com.helger.smtp.data.EmailData;
import com.helger.smtp.data.IMutableEmailData;
import com.helger.smtp.settings.SMTPSettings;
import com.helger.smtp.transport.listener.LoggingConnectionListener;
import com.helger.smtp.transport.listener.LoggingTransportListener;
import com.helger.xml.microdom.convert.MicroTypeConverter;
import com.helger.xml.microdom.serialize.MicroReader;

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
    /*
     * This file might not be present, as it contains the real-life SMTP
     * settings. It should reside in src/test/resource and is SVN ignored by
     * name
     */
    final IReadableResource aRes = new ClassPathResource ("smtp-settings.xml");
    if (aRes.exists ())
    {
      final boolean bOldIsDebug = GlobalDebug.isDebugMode ();
      GlobalDebug.setDebugModeDirect (true);
      try
      {
        EmailGlobalSettings.enableJavaxMailDebugging (GlobalDebug.isDebugMode ());

        // Setup debug listeners
        EmailGlobalSettings.addConnectionListener (new LoggingConnectionListener ());
        EmailGlobalSettings.addConnectionListener (new LoggingConnectionListener (EErrorLevel.WARN));
        EmailGlobalSettings.addEmailDataTransportListener (new LoggingTransportListener ());
        EmailGlobalSettings.addEmailDataTransportListener (new LoggingTransportListener (EErrorLevel.WARN));

        final SMTPSettings aSMTPSettings = MicroTypeConverter.convertToNative (MicroReader.readMicroXML (aRes).getDocumentElement (),
                                                                               SMTPSettings.class);
        final IMutableEmailData aMailData = new EmailData (EEmailType.TEXT);
        aMailData.to ().add (new EmailAddress ("ph@helger.com"));
        aMailData.cc ().add (new EmailAddress ("hudri@helger.com"));
        aMailData.setFrom (new EmailAddress ("auto@helger.com"));
        aMailData.setSubject ("JÜnit test with späcial käräktärs");
        aMailData.setBody ("Hi there\nLine 2\n4 special chars: äöüß\n123456789\nBest regards: ph-smtp");
        MailAPI.queueMail (aSMTPSettings, aMailData);
        MailAPI.stop ();

        /*
         * try to queue again after MailAPI was stopped - should end up in
         * failed mail queue
         */
        assertEquals (0, MailAPI.getFailedMailQueue ().size ());
        MailAPI.queueMail (aSMTPSettings, aMailData);
        assertEquals (1, MailAPI.getFailedMailQueue ().size ());
      }
      finally
      {
        GlobalDebug.setDebugModeDirect (bOldIsDebug);
        EmailGlobalSettings.setToDefault ();
      }
    }
  }

  @Ignore ("to avoid spamming my mailbox")
  @Test
  public void testStopImmediately ()
  {
    /*
     * This file might not be present, as it contains the real-life SMTP
     * settings. It should reside in src/test/resource and is SVN ignored by
     * name
     */
    final IReadableResource aRes = new ClassPathResource ("smtp-settings.xml");
    if (aRes.exists ())
    {
      final SMTPSettings aSMTPSettings = MicroTypeConverter.convertToNative (MicroReader.readMicroXML (aRes).getDocumentElement (),
                                                                             SMTPSettings.class);
      final IMutableEmailData aMailData = new EmailData (EEmailType.TEXT);
      aMailData.to ().add (new EmailAddress ("ph@helger.com"));
      aMailData.setFrom (new EmailAddress ("auto@helger.com"));
      aMailData.setSubject ("JÜnit test with späcial käräktärs");
      aMailData.setBody ("Hi there\nLine 2\n4 special chars: äöüß\n123456789\nBest regards: ph-smtp");

      final ICommonsList <IMutableEmailData> aMails = new CommonsArrayList <> ();
      for (int i = 0; i < 10; ++i)
        aMails.add (aMailData);

      MailAPI.queueMails (aSMTPSettings, aMails);
      ThreadHelper.sleep (20);

      // Stop immediately
      MailAPI.stop (true);
    }
  }
}
