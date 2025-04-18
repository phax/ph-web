/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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

  protected static ISessionFactory s_aSessionFactory;
  protected static NonBlockingProperties s_aProperties;
  protected static String s_sScpPath;
  protected static String s_sFileSystemPath;

  @BeforeClass
  public static void initializeClass ()
  {
    try (final InputStream inputStream = ClassLoader.getSystemResourceAsStream ("configuration.properties"))
    {
      Assume.assumeNotNull (inputStream);
      s_aProperties = new NonBlockingProperties ();
      s_aProperties.load (inputStream);
    }
    catch (final IOException e)
    {
      LOGGER.warn ("cant find properties file (tests will be skipped): " + e.getMessage ());
      s_aProperties = null;
      return;
    }

    final String knownHosts = s_aProperties.getProperty ("ssh.knownHosts");
    final String privateKey = s_aProperties.getProperty ("ssh.privateKey");
    s_sScpPath = s_aProperties.getProperty ("scp.out.test.scpPath");
    s_sFileSystemPath = s_aProperties.getProperty ("scp.out.test.filesystemPath");
    final String username = s_aProperties.getProperty ("scp.out.test.username");
    final String hostname = "localhost";
    final int port = Integer.parseInt (s_aProperties.getProperty ("scp.out.test.port"));

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
    s_aSessionFactory = defaultSessionFactory;
  }

  @Before
  public void beforeTest ()
  {
    // skip tests if properties not set
    Assume.assumeNotNull (s_aProperties);
  }
}
