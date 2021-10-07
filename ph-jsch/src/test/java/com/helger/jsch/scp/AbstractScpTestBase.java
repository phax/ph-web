/*
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.jsch.scp;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.lang.NonBlockingProperties;
import com.helger.jsch.session.DefaultSessionFactory;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

abstract class AbstractScpTestBase
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractScpTestBase.class);

  protected static ISessionFactory sessionFactory;
  protected static NonBlockingProperties properties;
  protected static String scpPath;
  protected static String filesystemPath;

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
      LOGGER.warn ("cant find properties file (tests will be skipped): " + e.getMessage ());
      properties = null;
      return;
    }

    final String knownHosts = properties.getProperty ("ssh.knownHosts");
    final String privateKey = properties.getProperty ("ssh.privateKey");
    scpPath = properties.getProperty ("scp.out.test.scpPath");
    filesystemPath = properties.getProperty ("scp.out.test.filesystemPath");
    final String username = properties.getProperty ("scp.out.test.username");
    final String hostname = "localhost";
    final int port = Integer.parseInt (properties.getProperty ("scp.out.test.port"));

    final DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory (username, hostname, port);
    try
    {
      defaultSessionFactory.setKnownHosts (knownHosts);
      defaultSessionFactory.setIdentityFromPrivateKey (privateKey);
    }
    catch (final JSchException e)
    {
      Assume.assumeNoException (e);
    }
    sessionFactory = defaultSessionFactory;
  }

  @Before
  public void beforeTest ()
  {
    // skip tests if properties not set
    Assume.assumeNotNull (properties);
  }
}
