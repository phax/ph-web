/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
 * Copyright (c) 1998-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

import com.helger.commons.lang.priviledged.IPrivilegedAction;

import javax.activation.DataHandler;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

/**
 * Demo app that shows how to construct and send a single part html message.
 * Note that the same basic technique can be used to send data of any type.
 *
 * @author John Mani
 * @author Bill Shannon
 * @author Max Spivak
 */
public class MainSendHTML
{
  @SuppressWarnings ("resource")
  public static void main (final String [] argv)
  {
    String to, subject = null, from = null, cc = null, bcc = null, url = null;
    String mailhost = null;
    final String mailer = "sendhtml";
    String protocol = null, host = null, user = null, password = null;
    String record = null; // name of folder in which to record mail
    boolean debug = false;
    final BufferedReader in = new BufferedReader (new InputStreamReader (System.in));
    int optind;

    for (optind = 0; optind < argv.length; optind++)
    {
      if (argv[optind].equals ("-T"))
      {
        protocol = argv[++optind];
      }
      else
        if (argv[optind].equals ("-H"))
        {
          host = argv[++optind];
        }
        else
          if (argv[optind].equals ("-U"))
          {
            user = argv[++optind];
          }
          else
            if (argv[optind].equals ("-P"))
            {
              password = argv[++optind];
            }
            else
              if (argv[optind].equals ("-M"))
              {
                mailhost = argv[++optind];
              }
              else
                if (argv[optind].equals ("-f"))
                {
                  record = argv[++optind];
                }
                else
                  if (argv[optind].equals ("-s"))
                  {
                    subject = argv[++optind];
                  }
                  else
                    if (argv[optind].equals ("-o"))
                    { // originator
                      from = argv[++optind];
                    }
                    else
                      if (argv[optind].equals ("-c"))
                      {
                        cc = argv[++optind];
                      }
                      else
                        if (argv[optind].equals ("-b"))
                        {
                          bcc = argv[++optind];
                        }
                        else
                          if (argv[optind].equals ("-L"))
                          {
                            url = argv[++optind];
                          }
                          else
                            if (argv[optind].equals ("-d"))
                            {
                              debug = true;
                            }
                            else
                              if (argv[optind].equals ("--"))
                              {
                                optind++;
                                break;
                              }
                              else
                                if (argv[optind].startsWith ("-"))
                                {
                                  System.out.println ("Usage: sendhtml [[-L store-url] | [-T prot] [-H host] [-U user] [-P passwd]]");
                                  System.out.println ("\t[-s subject] [-o from-address] [-c cc-addresses] [-b bcc-addresses]");
                                  System.out.println ("\t[-f record-mailbox] [-M transport-host] [-d] [address]");
                                  System.exit (1);
                                }
                                else
                                {
                                  break;
                                }
    }

    try
    {
      if (optind < argv.length)
      {
        // - concatenate all remaining arguments
        to = argv[optind];
        System.out.println ("To: " + to);
      }
      else
      {
        System.out.print ("To: ");
        System.out.flush ();
        to = in.readLine ();
      }
      if (subject == null)
      {
        System.out.print ("Subject: ");
        System.out.flush ();
        subject = in.readLine ();
      }
      else
      {
        System.out.println ("Subject: " + subject);
      }

      final Properties props = IPrivilegedAction.systemGetProperties ().invokeSafe ();
      // - could use Session.getTransport() and Transport.connect()
      // - assume we're using SMTP
      if (mailhost != null)
        props.put ("mail.smtp.host", mailhost);

      // Get a Session object
      final Session session = Session.getInstance (props, null);
      if (debug)
        session.setDebug (true);

      // construct the message
      final Message msg = new MimeMessage (session);
      if (from != null)
        msg.setFrom (new InternetAddress (from));
      else
        msg.setFrom ();

      msg.setRecipients (Message.RecipientType.TO, InternetAddress.parse (to, false));
      if (cc != null)
        msg.setRecipients (Message.RecipientType.CC, InternetAddress.parse (cc, false));
      if (bcc != null)
        msg.setRecipients (Message.RecipientType.BCC, InternetAddress.parse (bcc, false));

      msg.setSubject (subject);

      collect (in, msg);

      msg.setHeader ("X-Mailer", mailer);
      msg.setSentDate (new Date ());

      // send the thing off
      Transport.send (msg);

      System.out.println ("\nMail was sent successfully.");

      // Keep a copy, if requested.

      if (record != null)
      {
        // Get a Store object
        Store store = null;
        if (url != null)
        {
          final URLName urln = new URLName (url);
          store = session.getStore (urln);
          store.connect ();
        }
        else
        {
          if (protocol != null)
            store = session.getStore (protocol);
          else
            store = session.getStore ();

          // Connect
          if (host != null || user != null || password != null)
            store.connect (host, user, password);
          else
            store.connect ();
        }

        // Get record Folder. Create if it does not exist.
        final Folder folder = store.getFolder (record);
        if (folder == null)
        {
          System.err.println ("Can't get record folder.");
          System.exit (1);
        }
        if (!folder.exists ())
          folder.create (Folder.HOLDS_MESSAGES);

        final Message [] msgs = new Message [1];
        msgs[0] = msg;
        folder.appendMessages (msgs);

        System.out.println ("Mail was recorded successfully.");
      }

    }
    catch (final IOException | MessagingException e)
    {
      e.printStackTrace ();
    }
  }

  private static void collect (final BufferedReader in, final Message msg) throws MessagingException, IOException
  {
    String line;
    final String subject = msg.getSubject ();
    final StringBuilder sb = new StringBuilder ();
    sb.append ("<HTML>\n");
    sb.append ("<HEAD>\n");
    sb.append ("<TITLE>\n");
    sb.append (subject + "\n");
    sb.append ("</TITLE>\n");
    sb.append ("</HEAD>\n");

    sb.append ("<BODY>\n");
    sb.append ("<H1>" + subject + "</H1>" + "\n");

    while ((line = in.readLine ()) != null)
    {
      sb.append (line);
      sb.append ("\n");
    }

    sb.append ("</BODY>\n");
    sb.append ("</HTML>\n");

    msg.setDataHandler (new DataHandler (new ByteArrayDataSource (sb.toString (), "text/html")));
  }
}
