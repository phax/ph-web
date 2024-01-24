/*
 * Copyright (C) 2016-2024 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.lang.NonBlockingProperties;
import com.helger.jsch.session.DefaultSessionFactory;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public final class ConnectionTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ConnectionTest.class);

  private static String hostname;
  private static int port;
  private static String username;
  private static String correctPassword;
  private static String incorrectPassword;

  @BeforeClass
  public static void initializeClass ()
  {
    NonBlockingProperties properties = null;
    try (final InputStream inputStream = ClassLoader.getSystemResourceAsStream ("configuration.properties"))
    {
      Assume.assumeNotNull (inputStream);
      properties = new NonBlockingProperties ();
      properties.load (inputStream);
    }
    catch (final IOException e)
    {
      LOGGER.warn ("cant find properties file (tests will be skipped)", e);
      Assume.assumeNoException (e);
    }

    username = properties.getProperty ("scp.out.test.username");
    hostname = properties.getProperty ("scp.out.test.host");
    correctPassword = properties.getProperty ("scp.out.test.password");
    port = Integer.parseInt (properties.getProperty ("scp.out.test.port"));

    incorrectPassword = correctPassword + ".";
  }

  private ISessionFactory _getKeyboardInteractiveAuthenticatingSessionFactory (final String password)
  {
    final DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory (username, hostname, port);
    defaultSessionFactory.setConfig ("PreferredAuthentications", "keyboard-interactive");
    defaultSessionFactory.setUserInfo (new MockUserInfo (password));
    return defaultSessionFactory;
  }

  private ISessionFactory _getPasswordAuthenticatingSessionFactory (final String password)
  {
    final DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory (username, hostname, port);
    defaultSessionFactory.setConfig ("PreferredAuthentications", "password");
    defaultSessionFactory.setPassword (password);
    return defaultSessionFactory;
  }

  private void _testKeyboardInteractiveConnectionWithPassword (final String password) throws Exception
  {
    Session session = null;
    try
    {
      session = _getKeyboardInteractiveAuthenticatingSessionFactory (password).createSession ();
      if (!session.isConnected ())
        session.connect ();
    }
    finally
    {
      if (session != null)
      {
        session.disconnect ();
      }
    }
  }

  private void _testPasswordConnectionWithPassword (final String password) throws Exception
  {
    Session session = null;
    try
    {
      session = _getPasswordAuthenticatingSessionFactory (password).createSession ();
      if (!session.isConnected ())
        session.connect ();
    }
    finally
    {
      if (session != null)
      {
        session.disconnect ();
      }
    }
  }

  @Ignore
  @Test
  public void testKeyboardInteractiveConnectionWithCorrectPassword ()
  {
    // Doesnt seem to work with cygwin
    Assume.assumeNotNull (username, correctPassword);
    try
    {
      _testKeyboardInteractiveConnectionWithPassword (correctPassword);
    }
    catch (final Exception e)
    {
      fail (e.getMessage ());
    }
  }

  @Ignore
  @Test
  public void testKeyboardInteractiveConnectionWithIncorrectPassword ()
  {
    // Doesnt seem to work with cygwin
    Assume.assumeNotNull (username, incorrectPassword);
    try
    {
      _testKeyboardInteractiveConnectionWithPassword (incorrectPassword);
    }
    catch (final JSchException e)
    {
      assertEquals ("Auth fail", e.getMessage ());
    }
    catch (final Exception e)
    {
      fail ("Unexpected exception: " + e.getMessage ());
    }
  }

  @Test
  public void testPasswordConnectionWithCorrectPassword ()
  {
    Assume.assumeNotNull (username, correctPassword);
    try
    {
      _testPasswordConnectionWithPassword (correctPassword);
    }
    catch (final Exception e)
    {
      fail (e.getMessage ());
    }
  }

  @Test
  public void testPasswordConnectionWithIncorrectPassword ()
  {
    Assume.assumeNotNull (username, incorrectPassword);
    try
    {
      _testPasswordConnectionWithPassword (incorrectPassword);
    }
    catch (final JSchException e)
    {
      assertEquals ("Auth fail", e.getMessage ());
    }
    catch (final Exception e)
    {
      fail ("Unexpected exception: " + e.getMessage ());
    }
  }

  private static final class MockUserInfo implements UserInfo, UIKeyboardInteractive
  {
    private final String m_sPassword;

    public MockUserInfo (final String password)
    {
      m_sPassword = password;
    }

    @Override
    public String [] promptKeyboardInteractive (final String destination,
                                                final String name,
                                                final String instruction,
                                                final String [] prompt,
                                                final boolean [] echo)
    {
      LOGGER.info ("getPassphrase()");
      return new String [] { m_sPassword };
    }

    @Override
    public String getPassphrase ()
    {
      LOGGER.info ("getPassphrase()");
      return null;
    }

    @Override
    public String getPassword ()
    {
      LOGGER.info ("getPassword()");
      return m_sPassword;
    }

    @Override
    public boolean promptPassword (final String message)
    {
      LOGGER.info ("promptPassword(" + message + ")");
      return false;
    }

    @Override
    public boolean promptPassphrase (final String message)
    {
      LOGGER.info ("promptPassphrase(" + message + ")");
      return false;
    }

    @Override
    public boolean promptYesNo (final String message)
    {
      LOGGER.info ("promptYesNo(" + message + ")");
      return false;
    }

    @Override
    public void showMessage (final String message)
    {
      LOGGER.info ("showMessage(" + message + ")");
    }
  }
}
