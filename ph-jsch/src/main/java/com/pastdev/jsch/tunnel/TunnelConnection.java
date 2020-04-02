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

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.StreamHelper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.pastdev.jsch.ISessionFactory;

/**
 * A TunnelConnection represents an ssh connection that opens one or more
 * {@link com.pastdev.jsch.tunnel.Tunnel Tunnel's}.
 */
public class TunnelConnection implements Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (TunnelConnection.class);

  private final Map <String, Tunnel> m_aTunnelsByDestination;
  private Session m_aSession;
  private final ISessionFactory m_aSessionFactory;
  private final Iterable <Tunnel> m_aTunnels;

  /**
   * Creates a TunnelConnection using the the <code>sessionFactory</code> to
   * obtain its ssh connection with a single tunnel defined by
   * {@link com.pastdev.jsch.tunnel.Tunnel#Tunnel(int, String, int)
   * Tunnel(localPort, destinationHostname, destinationPort)}.
   *
   * @param sessionFactory
   *        The sessionFactory
   * @param localPort
   *        The local port to bind to
   * @param destinationHostname
   *        The destination hostname to tunnel to
   * @param destinationPort
   *        The destination port to tunnel to
   */
  public TunnelConnection (final ISessionFactory sessionFactory,
                           final int localPort,
                           final String destinationHostname,
                           final int destinationPort)
  {
    this (sessionFactory, new Tunnel (localPort, destinationHostname, destinationPort));
  }

  /**
   * Creates a TunnelConnection using the the <code>sessionFactory</code> to
   * obtain its ssh connection with a list of
   * {@link com.pastdev.jsch.tunnel.Tunnel Tunnel's}.
   *
   * @param sessionFactory
   *        The sessionFactory
   * @param tunnels
   *        The tunnels
   */
  public TunnelConnection (final ISessionFactory sessionFactory, final Tunnel... tunnels)
  {
    this (sessionFactory, Arrays.asList (tunnels));
  }

  /**
   * Creates a TunnelConnection using the the <code>sessionFactory</code> to
   * obtain its ssh connection with a list of
   * {@link com.pastdev.jsch.tunnel.Tunnel Tunnel's}.
   *
   * @param sessionFactory
   *        The sessionFactory
   * @param tunnels
   *        The tunnels
   */
  public TunnelConnection (final ISessionFactory sessionFactory, final List <Tunnel> tunnels)
  {
    this.m_aSessionFactory = sessionFactory;
    this.m_aTunnels = tunnels;
    this.m_aTunnelsByDestination = new HashMap <> ();

    for (final Tunnel tunnel : tunnels)
    {
      m_aTunnelsByDestination.put (hostnamePortKey (tunnel), tunnel);
    }
  }

  /**
   * Closes the underlying ssh session causing all tunnels to be closed.
   */
  public void close () throws IOException
  {
    if (m_aSession != null && m_aSession.isConnected ())
    {
      m_aSession.disconnect ();
    }
    m_aSession = null;

    // unnecessary, but seems right to undo what we did
    for (final Tunnel tunnel : m_aTunnels)
    {
      tunnel.setAssignedLocalPort (0);
    }
  }

  /**
   * Returns the tunnel matching the supplied values, or <code>null</code> if
   * there isn't one that matches.
   *
   * @param destinationHostname
   *        The tunnels destination hostname
   * @param destinationPort
   *        The tunnels destination port
   * @return The tunnel matching the supplied values
   */
  public Tunnel getTunnel (final String destinationHostname, final int destinationPort)
  {
    return m_aTunnelsByDestination.get (hostnamePortKey (destinationHostname, destinationPort));
  }

  private String hostnamePortKey (final Tunnel tunnel)
  {
    return hostnamePortKey (tunnel.getDestinationHostname (), tunnel.getDestinationPort ());
  }

  private String hostnamePortKey (final String hostname, final int port)
  {
    return hostname + ":" + port;
  }

  /**
   * Returns true if the underlying ssh session is open.
   *
   * @return True if the underlying ssh session is open
   */
  public boolean isOpen ()
  {
    return m_aSession != null && m_aSession.isConnected ();
  }

  /**
   * Opens a session and connects all of the tunnels.
   *
   * @throws JSchException
   *         If unable to connect
   */
  public void open () throws JSchException
  {
    if (isOpen ())
    {
      return;
    }
    m_aSession = m_aSessionFactory.newSession ();

    LOGGER.debug ("connecting session");
    m_aSession.connect ();

    for (final Tunnel tunnel : m_aTunnels)
    {
      int assignedPort = 0;
      if (tunnel.getLocalAlias () == null)
      {
        assignedPort = m_aSession.setPortForwardingL (tunnel.getLocalPort (),
                                                   tunnel.getDestinationHostname (),
                                                   tunnel.getDestinationPort ());
      }
      else
      {
        assignedPort = m_aSession.setPortForwardingL (tunnel.getLocalAlias (),
                                                   tunnel.getLocalPort (),
                                                   tunnel.getDestinationHostname (),
                                                   tunnel.getDestinationPort ());
      }
      tunnel.setAssignedLocalPort (assignedPort);
      LOGGER.debug ("added tunnel {}", tunnel);
    }
    LOGGER.info ("forwarding {}", this);
  }

  /**
   * Closes, and re-opens the session and all its tunnels. Effectively calls
   * {@link #close()} followed by a call to {@link #open()}.
   *
   * @throws JSchException
   *         If unable to connect
   */
  public void reopen () throws JSchException
  {
    StreamHelper.close (this);
    open ();
  }

  @Override
  public String toString ()
  {
    final StringBuilder builder = new StringBuilder (m_aSessionFactory.toString ());
    for (final Tunnel tunnel : m_aTunnels)
    {
      builder.append (" -L ").append (tunnel);
    }
    return builder.toString ();
  }
}
