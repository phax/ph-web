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
package com.helger.jsch.tunnel;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.NonBlockingBufferedReader;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.string.StringHelper;
import com.helger.jsch.proxy.SshProxy;
import com.helger.jsch.session.AbstractSessionFactoryBuilder;
import com.helger.jsch.session.ISessionFactory;
import com.jcraft.jsch.JSchException;

/**
 * Manages a collection of tunnels. This implementation will:
 * <ul>
 * <li>Ensure a minimum number of ssh connections are made</li>
 * <li>Ensure all connections are open/closed at the same time</li>
 * <li>Provide a convenient syntax for defining tunnels</li>
 * </ul>
 */
public class TunnelConnectionManager implements Closeable
{
  private static final Pattern PATTERN_TUNNELS_CFG_COMMENT_LINE = Pattern.compile ("^\\s*(?:#.*)?$");
  private static final Logger LOGGER = LoggerFactory.getLogger (TunnelConnectionManager.class);

  private final ISessionFactory m_aBaseSessionFactory;
  private ICommonsList <TunnelConnection> m_aTunnelConnections;

  /**
   * Creates a TunnelConnectionManager that will use the
   * <code>baseSessionFactory</code> to obtain its session connections. Because
   * this constructor does not set the tunnel connections for you, you will need
   * to call {@link #setTunnelConnections(Iterable)}.
   *
   * @param baseSessionFactory
   *        The session factory
   * @throws JSchException
   *         For connection failures
   * @see #setTunnelConnections(Iterable)
   */
  public TunnelConnectionManager (final ISessionFactory baseSessionFactory) throws JSchException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Creating TunnelConnectionManager");
    m_aBaseSessionFactory = baseSessionFactory;
  }

  /**
   * Creates a TunnelConnectionManager that will use the
   * <code>baseSessionFactory</code> to obtain its session connections and
   * provide the tunnels specified.
   *
   * @param baseSessionFactory
   *        The session factory
   * @param pathAndSpecList
   *        A list of {@link #setTunnelConnections(Iterable) path and spec}
   *        strings
   * @throws JSchException
   *         For connection failures
   * @see #setTunnelConnections(Iterable)
   */
  public TunnelConnectionManager (final ISessionFactory baseSessionFactory,
                                  final String... pathAndSpecList) throws JSchException
  {
    this (baseSessionFactory, Arrays.asList (pathAndSpecList));
  }

  /**
   * Creates a TunnelConnectionManager that will use the
   * <code>baseSessionFactory</code> to obtain its session connections and
   * provide the tunnels specified.
   *
   * @param baseSessionFactory
   *        The session factory
   * @param pathAndSpecList
   *        A list of {@link #setTunnelConnections(Iterable) path and spec}
   *        strings
   * @throws JSchException
   *         For connection failures
   * @see #setTunnelConnections(Iterable)
   */
  public TunnelConnectionManager (final ISessionFactory baseSessionFactory,
                                  final Iterable <String> pathAndSpecList) throws JSchException
  {
    this (baseSessionFactory);
    setTunnelConnections (pathAndSpecList);
  }

  /**
   * Closes all sessions and their associated tunnels.
   *
   * @see com.helger.jsch.tunnel.TunnelConnection#close()
   */
  @Override
  public void close ()
  {
    for (final TunnelConnection tunnelConnection : m_aTunnelConnections)
      StreamHelper.close (tunnelConnection);
  }

  /**
   * Will re-open any connections that are not still open.
   *
   * @throws JSchException
   *         For connection failures
   */
  public void ensureOpen () throws JSchException
  {
    for (final TunnelConnection tunnelConnection : m_aTunnelConnections)
      if (!tunnelConnection.isOpen ())
        tunnelConnection.reopen ();
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
   * @see com.helger.jsch.tunnel.TunnelConnection#getTunnel(String, int)
   */
  public Tunnel getTunnel (final String destinationHostname, final int destinationPort)
  {
    // might be better to cache, but dont anticipate massive numbers
    // of tunnel connections...
    for (final TunnelConnection tunnelConnection : m_aTunnelConnections)
    {
      final Tunnel tunnel = tunnelConnection.getTunnel (destinationHostname, destinationPort);
      if (tunnel != null)
        return tunnel;
    }
    return null;
  }

  /**
   * Opens all the necessary sessions and connects all of the tunnels.
   *
   * @throws JSchException
   *         For connection failures
   * @see com.helger.jsch.tunnel.TunnelConnection#open()
   */
  public void open () throws JSchException
  {
    for (final TunnelConnection tunnelConnection : m_aTunnelConnections)
      tunnelConnection.open ();
  }

  /**
   * Creates a set of tunnel connections based upon the contents of
   * <code>tunnelsConfig</code>. The format of this file is one path and tunnel
   * per line. Comments and empty lines are allowed and are excluded using the
   * pattern <code>^\s*(?:#.*)?$</code>.
   *
   * @param tunnelsConfig
   *        A file containing tunnel configuration
   * @param aCharset
   *        Charset to use. May not be <code>null</code>.
   * @throws IOException
   *         If unable to read from <code>tunnelsConfig</code>
   * @throws JSchException
   *         For connection failures
   */
  public void setTunnelConnectionsFromFile (final File tunnelsConfig,
                                            @Nonnull final Charset aCharset) throws IOException, JSchException
  {
    final List <String> aLines = new ArrayList <> ();
    try (final NonBlockingBufferedReader reader = new NonBlockingBufferedReader (FileHelper.getReader (tunnelsConfig,
                                                                                                       aCharset)))
    {
      String sLine;
      while ((sLine = reader.readLine ()) != null)
      {
        if (PATTERN_TUNNELS_CFG_COMMENT_LINE.matcher (sLine).matches ())
          continue;
        aLines.add (sLine);
      }
    }
    setTunnelConnections (aLines);
  }

  /**
   * Creates a set of tunnel connections based upon the pathAndTunnels. Each
   * entry of pathAndTunnels must be of the form (in
   * <a href="https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_Form"
   * >EBNF</a>):
   *
   * <pre>
   * path and tunnels = path and tunnel, {new line, path and tunnel}
   * path and tunnel = path, "|", tunnel
   * new line = "\n"
   * path = path part, {"-&gt;", path part}
   * path part = {user, "@"}, hostname
   * tunnel = {local part}, ":", destination hostname, ":", destination port
   * local part = {local alias, ":"}, local port
   * local alias = hostname
   * local port = port
   * destination hostname = hostname
   * destination port = port
   * user = ? user name ?
   * hostname = ? hostname ?
   * port = ? port ?
   * </pre>
   * <p>
   * For example:
   * </p>
   * <p>
   * <code>
   * jimhenson@admin.muppets.com-&gt;animal@drteethandtheelectricmahem.muppets.com|drteeth:8080:drteeth.muppets.com:80
   * </code>
   * </p>
   * <p>
   * Says open an ssh connection as user <code>jimhenson</code> to host
   * <code>admin.muppets.com</code>. Then, through that connection, open a
   * connection as user <code>animal</code> to host
   * <code>drteethandtheelectricmahem.muppets.com</code>. Then map local port
   * <code>8080</code> on the interface with alias <code>drteeth</code> through
   * the two-hop tunnel to port <code>80</code> on
   * <code>drteeth.muppets.com</code>.
   * </p>
   *
   * @param pathAndSpecList
   *        A list of path and spec entries
   * @throws JSchException
   *         For connection failures
   */
  public void setTunnelConnections (final Iterable <String> pathAndSpecList) throws JSchException
  {
    final Map <String, Set <Tunnel>> tunnelMap = new HashMap <> ();
    for (final String pathAndSpecString : pathAndSpecList)
    {
      final String [] pathAndSpec = StringHelper.getExplodedArray ('|', pathAndSpecString.trim (), 2);
      tunnelMap.computeIfAbsent (pathAndSpec[0], k -> new HashSet <> ()).add (new Tunnel (pathAndSpec[1]));
    }

    m_aTunnelConnections = new CommonsArrayList <> ();
    final SessionFactoryCache sessionFactoryCache = new SessionFactoryCache (m_aBaseSessionFactory);
    for (final String path : tunnelMap.keySet ())
    {
      m_aTunnelConnections.add (new TunnelConnection (sessionFactoryCache.getSessionFactory (path),
                                                      new CommonsArrayList <> (tunnelMap.get (path))));
    }
  }

  /*
   * Used to ensure duplicate paths are not created which will minimize the
   * number of connections needed.
   */
  static class SessionFactoryCache
  {
    private final Map <String, ISessionFactory> sessionFactoryByPath;
    private final ISessionFactory defaultSessionFactory;

    SessionFactoryCache (final ISessionFactory baseSessionFactory)
    {
      this.defaultSessionFactory = baseSessionFactory;
      this.sessionFactoryByPath = new HashMap <> ();
    }

    public ISessionFactory getSessionFactory (final String path) throws JSchException
    {
      ISessionFactory sessionFactory = null;
      String key = null;
      for (final String part : StringHelper.getExploded ("->", path))
      {
        if (key == null)
          key = part;
        else
          key += "->" + part;

        if (sessionFactoryByPath.containsKey (key))
        {
          sessionFactory = sessionFactoryByPath.get (key);
          continue;
        }

        final AbstractSessionFactoryBuilder builder;
        if (sessionFactory == null)
          builder = defaultSessionFactory.newSessionFactoryBuilder ();
        else
          builder = sessionFactory.newSessionFactoryBuilder ().setProxy (new SshProxy (sessionFactory));

        // start with [username@]hostname[:port]
        final String [] userAtHost = StringHelper.getExplodedArray ('@', part, 2);
        String hostname = null;
        if (userAtHost.length == 2)
        {
          builder.setUsername (userAtHost[0]);
          hostname = userAtHost[1];
        }
        else
        {
          hostname = userAtHost[0];
        }

        // left with hostname[:port]
        final String [] hostColonPort = StringHelper.getExplodedArray (':', hostname, 2);
        builder.setHostname (hostColonPort[0]);
        if (hostColonPort.length == 2)
        {
          builder.setPort (Integer.parseInt (hostColonPort[1]));
        }

        sessionFactory = builder.build ();
      }
      return sessionFactory;
    }
  }
}
