/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.pastdev.jsch.tunnel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.StreamHelper;
import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.ISessionFactory;

public final class TunnelConnectionTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (TunnelConnectionTest.class);
  private static ISessionFactory sessionFactory;
  private static Properties properties;

  private final String expected = "This will be amazing if it works";
  // Must be a StringBuffer
  private StringBuffer serviceBuffer;
  private final int servicePort = 59703;
  private Thread serviceThread;

  private final ReentrantLock serviceLock = new ReentrantLock ();
  private final Condition serviceBufferReady = serviceLock.newCondition ();
  private final Condition serviceConnectAccepted = serviceLock.newCondition ();
  private final Condition serviceConnected = serviceLock.newCondition ();
  private final Condition serviceReady = serviceLock.newCondition ();
  private final Condition serviceWrittenTo = serviceLock.newCondition ();

  @BeforeClass
  public static void initializeClass ()
  {
    try (final InputStream inputStream = ClassLoader.getSystemResourceAsStream ("configuration.properties"))
    {
      Assume.assumeNotNull (inputStream);
      properties = new Properties ();
      properties.load (inputStream);
    }
    catch (final IOException e)
    {
      LOGGER.warn ("cant find properties file (tests will be skipped)", e);
      LOGGER.debug ("cant find properties file:", e);
      properties = null;
      return;
    }

    final String knownHosts = properties.getProperty ("ssh.knownHosts");
    final String privateKey = properties.getProperty ("ssh.privateKey");
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
      LOGGER.error ("Failed to configure default session, skipping tests: {}", e.getMessage ());
      LOGGER.debug ("Failed to configure default session, skipping tests:", e);
      Assume.assumeNoException (e);
    }
    sessionFactory = defaultSessionFactory;
  }

  @After
  public void afterTest () throws InterruptedException
  {
    serviceThread.join ();
  }

  @Before
  public void beforeTest () throws InterruptedException
  {
    // skip tests if properties not set
    Assume.assumeNotNull (properties);

    serviceBuffer = new StringBuffer ();
    serviceThread = new Thread ( () -> {
      serviceLock.lock ();
      try (ServerSocket serverSocket = new ServerSocket (servicePort))
      {
        LOGGER.debug ("opening service on port " + servicePort);
        serviceReady.signalAll ();
        serviceConnected.await ();
        LOGGER.trace ("waiting for connection");
        try (InputStream inputStream = serverSocket.accept ().getInputStream ())
        {
          serviceConnectAccepted.signalAll ();
          LOGGER.trace ("connected, now wait for write");
          serviceWrittenTo.await ();
          LOGGER.trace ("accepted connection, now read data");
          final String data = StreamHelper.getAllBytesAsString (inputStream, StandardCharsets.UTF_8);
          LOGGER.trace ("read {}", data);
          serviceBuffer.append (data);
          serviceBufferReady.signalAll ();
        }
      }
      catch (final Exception e)
      {
        LOGGER.error ("failed to open service on port " + servicePort + ": ", e);
      }
      finally
      {
        serviceLock.unlock ();
        LOGGER.debug ("closing down service on port " + servicePort);
      }
    });
    LOGGER.debug ("starting service");
    serviceThread.start ();

    serviceLock.lock ();
    try
    {
      LOGGER.trace ("wait for serviceReady");
      serviceReady.await (); // wait for service to open socket
      LOGGER.trace ("service is ready now!");
    }
    finally
    {
      serviceLock.unlock ();
    }
  }

  @Test
  public void testService ()
  {
    assertEquals (expected, _writeToService (servicePort, expected));
  }

  @Test
  public void testConnection ()
  {
    final int tunnelPort1 = 59701;
    try (TunnelConnection tunnelConnection = new TunnelConnection (sessionFactory,
                                                                   new Tunnel (tunnelPort1, "localhost", servicePort)))
    {
      tunnelConnection.open ();

      assertEquals (expected, _writeToService (tunnelPort1, expected));
    }
    catch (final Exception e)
    {
      LOGGER.debug ("failed:", e);
      fail (e.getMessage ());
    }
  }

  @Test
  public void testDynamicPortConnection ()
  {
    final String hostname = "localhost";
    try (TunnelConnection tunnelConnection = new TunnelConnection (sessionFactory, new Tunnel (hostname, servicePort)))
    {
      tunnelConnection.open ();

      assertEquals (expected,
                    _writeToService (tunnelConnection.getTunnel (hostname, servicePort).getAssignedLocalPort (),
                                     expected));
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed:", e);
      fail (e.getMessage ());
    }
  }

  private String _writeToService (final int port, final String data)
  {
    serviceLock.lock ();
    try
    {
      LOGGER.debug ("connecting to service through port: " + port);
      try (final Socket socket = new Socket ("localhost", port))
      {
        LOGGER.trace ("connected");
        serviceConnected.signalAll ();
        serviceConnectAccepted.await ();
        LOGGER.trace ("now write to service");

        try (final OutputStream outputStream = socket.getOutputStream ())
        {
          StreamHelper.writeStream (outputStream, data, StandardCharsets.UTF_8);
        }

        LOGGER.trace ("service written to");
        serviceWrittenTo.signalAll ();
        LOGGER.trace ("wait for serviceBuffer");
        // wait for buffer to be updated
        serviceBufferReady.await ();
        return serviceBuffer.toString ();
      }
    }
    catch (final Exception e)
    {
      LOGGER.error ("failed:", e);
      fail (e.getMessage ());
      return null;
    }
    finally
    {
      serviceLock.unlock ();
      LOGGER.debug ("close");
    }
  }
}
