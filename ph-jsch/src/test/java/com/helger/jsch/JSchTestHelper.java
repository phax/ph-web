/*
 * Copyright (C) 2016-2026 Philip Helger (www.helger.com)
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
package com.helger.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.rt.NonBlockingProperties;
import com.helger.base.string.StringParser;
import com.helger.jsch.session.DefaultSessionFactory;
import com.jcraft.jsch.JSchException;

public class JSchTestHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (JSchTestHelper.class);

  private JSchTestHelper ()
  {}

  @Nullable
  public static NonBlockingProperties loadTestConfig ()
  {
    try (final InputStream aIS = ClassLoader.getSystemResourceAsStream ("configuration.properties"))
    {
      if (aIS != null)
      {
        final NonBlockingProperties aProperties = new NonBlockingProperties ();
        aProperties.load (aIS);
        return aProperties;
      }
      // fall through
    }
    catch (final IOException ex)
    {
      LOGGER.warn ("cant find properties file (tests will be skipped)", ex);
    }
    return null;
  }

  @Nullable
  public static DefaultSessionFactory createSessionFactoryFromConfig ()
  {
    return createSessionFactoryFromConfig (null);
  }

  @Nullable
  public static DefaultSessionFactory createSessionFactoryFromConfig (@Nullable final Consumer <NonBlockingProperties> aPropsHolder)
  {
    final NonBlockingProperties aProperties = loadTestConfig ();
    Assume.assumeNotNull (aProperties);

    final String sUsername = aProperties.getProperty ("scp.out.test.username");
    Assume.assumeNotNull (sUsername);
    final String sHostname = "localhost";
    final int nPort = StringParser.parseInt (aProperties.getProperty ("scp.out.test.port"), -1);
    Assume.assumeTrue (nPort > 0);

    final DefaultSessionFactory aSF = new DefaultSessionFactory (sUsername, sHostname, nPort);
    try
    {
      final String sKnownHosts = aProperties.getProperty ("ssh.knownHosts");
      aSF.setKnownHosts (sKnownHosts);

      final String sPrivateKey = aProperties.getProperty ("ssh.privateKey");
      aSF.setIdentityFromPrivateKey (sPrivateKey);
    }
    catch (final JSchException ex)
    {
      LOGGER.error ("Failed to configure default session, skipping tests", ex);
      Assume.assumeNoException (ex);
    }

    if (aPropsHolder != null)
      aPropsHolder.accept (aProperties);

    return aSF;
  }
}
