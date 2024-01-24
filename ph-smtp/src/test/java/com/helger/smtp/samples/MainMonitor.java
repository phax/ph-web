/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
import java.util.Properties;

import org.eclipse.angus.mail.imap.IMAPFolder;

import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.lang.priviledged.IPrivilegedAction;

import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;

/* Monitors given mailbox for new mail */

public class MainMonitor
{
  public static void main (final String argv[])
  {
    if (argv.length != 5)
    {
      System.out.println ("Usage: monitor <host> <user> <password> <mbox> <freq>");
      System.exit (1);
    }
    System.out.println ("\nTesting monitor\n");

    try
    {
      final Properties props = IPrivilegedAction.systemGetProperties ().invokeSafe ();

      // Get a Session object
      final Session session = Session.getInstance (props, null);
      // session.setDebug(true);

      // Get a Store object
      final Store store = session.getStore ("imap");

      // Connect
      store.connect (argv[0], argv[1], argv[2]);

      // Open a Folder
      final Folder folder = store.getFolder (argv[3]);
      if (folder == null || !folder.exists ())
      {
        System.out.println ("Invalid folder");
        System.exit (1);
      }

      folder.open (Folder.READ_WRITE);

      // Add messageCountListener to listen for new messages
      folder.addMessageCountListener (new MessageCountAdapter ()
      {
        @Override
        public void messagesAdded (final MessageCountEvent ev)
        {
          final Message [] msgs = ev.getMessages ();
          System.out.println ("Got " + msgs.length + " new messages");

          // Just dump out the new messages
          for (final Message msg : msgs)
          {
            try
            {
              System.out.println ("-----");
              System.out.println ("Message " + msg.getMessageNumber () + ":");
              msg.writeTo (System.out);
            }
            catch (final IOException | MessagingException mex)
            {
              mex.printStackTrace ();
            }
          }
        }
      });

      // Check mail once in "freq" MILLIseconds
      final int freq = Integer.parseInt (argv[4]);
      boolean supportsIdle = false;
      try
      {
        if (folder instanceof IMAPFolder)
        {
          final IMAPFolder f = (IMAPFolder) folder;
          f.idle ();
          supportsIdle = true;
        }
      }
      catch (final FolderClosedException fex)
      {
        throw fex;
      }
      catch (final MessagingException mex)
      {
        supportsIdle = false;
      }
      for (;;)
      {
        if (supportsIdle && folder instanceof IMAPFolder)
        {
          final IMAPFolder f = (IMAPFolder) folder;
          f.idle ();
          System.out.println ("IDLE done");
        }
        else
        {
          // sleep for freq milliseconds
          ThreadHelper.sleep (freq);

          // This is to force the IMAP server to send us
          // EXISTS notifications.
          folder.getMessageCount ();
        }
      }

    }
    catch (final Exception ex)
    {
      ex.printStackTrace ();
    }
  }
}
