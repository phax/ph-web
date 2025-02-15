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
package com.helger.smtp.samples;

/*
 * Copyright (c) 1996-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.helger.commons.lang.priviledged.IPrivilegedAction;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * sendfile will create a multipart message with the second block of the message
 * being the given file.
 * <p>
 * This demonstrates how to use the FileDataSource to send a file via mail.
 * <p>
 * usage: <code>java sendfile <i>to from smtp file true|false</i></code> where
 * <i>to</i> and <i>from</i> are the destination and origin email addresses,
 * respectively, and <i>smtp</i> is the hostname of the machine that has smtp
 * server running. <i>file</i> is the file to send. The next parameter either
 * turns on or turns off debugging during sending.
 *
 * @author Christopher Cotton
 */
public class MainSendFile
{

  public static void main (final String [] args)
  {
    if (args.length != 5)
    {
      System.out.println ("usage: java sendfile <to> <from> <smtp> <file> true|false");
      System.exit (1);
    }

    final String to = args[0];
    final String from = args[1];
    final String host = args[2];
    final String filename = args[3];
    final boolean debug = Boolean.valueOf (args[4]).booleanValue ();
    final String msgText1 = "Sending a file.\n";
    final String subject = "Sending a file";

    // create some properties and get the default Session
    final Properties props = IPrivilegedAction.systemGetProperties ().invokeSafe ();
    props.put ("mail.smtp.host", host);

    final Session session = Session.getInstance (props, null);
    session.setDebug (debug);

    try
    {
      // create a message
      final MimeMessage msg = new MimeMessage (session);
      msg.setFrom (new InternetAddress (from));
      final InternetAddress [] address = { new InternetAddress (to) };
      msg.setRecipients (Message.RecipientType.TO, address);
      msg.setSubject (subject);

      // create and fill the first message part
      final MimeBodyPart mbp1 = new MimeBodyPart ();
      mbp1.setText (msgText1);

      // create the second message part
      final MimeBodyPart mbp2 = new MimeBodyPart ();

      // attach the file to the message
      mbp2.attachFile (filename);

      /*
       * Use the following approach instead of the above line if you want to
       * control the MIME type of the attached file. Normally you should never
       * need to do this. FileDataSource fds = new FileDataSource(filename) {
       * public String getContentType() { return "application/octet-stream"; }
       * }; mbp2.setDataHandler(new DataHandler(fds));
       * mbp2.setFileName(fds.getName());
       */

      // create the Multipart and add its parts to it
      final Multipart mp = new MimeMultipart ();
      mp.addBodyPart (mbp1);
      mp.addBodyPart (mbp2);

      // add the Multipart to the message
      msg.setContent (mp);

      // set the Date: header
      msg.setSentDate (new Date ());

      /*
       * If you want to control the Content-Transfer-Encoding of the attached
       * file, do the following. Normally you should never need to do this.
       * msg.saveChanges(); mbp2.setHeader("Content-Transfer-Encoding",
       * "base64");
       */

      // send the message
      Transport.send (msg);
    }
    catch (final MessagingException mex)
    {
      mex.printStackTrace ();
      Exception ex = null;
      if ((ex = mex.getNextException ()) != null)
      {
        ex.printStackTrace ();
      }
    }
    catch (final IOException ioex)
    {
      ioex.printStackTrace ();
    }
  }
}
