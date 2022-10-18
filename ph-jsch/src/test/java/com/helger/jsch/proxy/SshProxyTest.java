/*
 * Copyright (C) 2016-2022 Philip Helger (www.helger.com)
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
package com.helger.jsch.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.NonBlockingProperties;
import com.helger.jsch.session.DefaultSessionFactory;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;

public final class SshProxyTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (SshProxyTest.class);

  private static DefaultSessionFactory sessionFactory;
  private static NonBlockingProperties properties;
  private static String username;
  private static String hostname;
  private static int port;

  private static final String EXPECTD = "there is absolutely no chance this is gonna work!";

  @BeforeClass
  public static void initializeClass ()
  {
    try (final InputStream inputStream = ClassLoader.getSystemResourceAsStream ("configuration.properties"))
    {
      Assume.assumeNotNull (inputStream);
      properties = new NonBlockingProperties ();
      properties.load (inputStream);
    }
    catch (final IOException e)
    {
      LOGGER.warn ("cant find properties file (tests will be skipped)", e);
      properties = null;
      return;
    }

    final String knownHosts = properties.getProperty ("ssh.knownHosts");
    final String privateKey = properties.getProperty ("ssh.privateKey");
    username = properties.getProperty ("scp.out.test.username");
    hostname = "localhost";
    port = Integer.parseInt (properties.getProperty ("scp.out.test.port"));

    sessionFactory = new DefaultSessionFactory (username, hostname, port);
    try
    {
      sessionFactory.setKnownHosts (knownHosts);
      sessionFactory.setIdentityFromPrivateKey (privateKey);
    }
    catch (final JSchException e)
    {
      Assume.assumeNoException (e);
    }
  }

  @Test
  public void testSshProxy ()
  {
    final Proxy proxy = null;
    Session session = null;
    Channel channel = null;
    try
    {
      final ISessionFactory proxySessionFactory = sessionFactory.newSessionFactoryBuilder ()
                                                                .setHostname ("localhost")
                                                                .setPort (ISessionFactory.SSH_PORT)
                                                                .build ();
      final ISessionFactory destinationSessionFactory = sessionFactory.newSessionFactoryBuilder ()
                                                                      .setProxy (new SshProxy (proxySessionFactory))
                                                                      .build ();
      session = destinationSessionFactory.createSession ();
      if (!session.isConnected ())
        session.connect ();

      channel = session.openChannel ("exec");
      ((ChannelExec) channel).setCommand ("echo " + EXPECTD);
      final InputStream inputStream = channel.getInputStream ();
      channel.connect ();

      // echo adds \n
      assertEquals (EXPECTD + "\n", StreamHelper.getAllBytesAsString (inputStream, StandardCharsets.UTF_8));
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed for proxy " + proxy, e);
      fail (e.getMessage ());
    }
    finally
    {
      if (channel != null && channel.isConnected ())
        channel.disconnect ();
      if (session != null && session.isConnected ())
        session.disconnect ();
    }
  }
}
