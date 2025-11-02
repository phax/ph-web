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
package com.helger.jsch.tunnel;

import java.util.Locale;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.string.StringHelper;
import com.helger.base.tostring.ToStringGenerator;

/**
 * Tunnel stores all the information needed to define an ssh port-forwarding
 * tunnel.
 *
 * @see <a href="http://tools.ietf.org/html/rfc4254#section-7">rfc4254</a>
 */
public class Tunnel
{
  private String m_sSpec;
  private String m_sDestinationHostname;
  private int m_nDestinationPort;
  private String m_sLocalAlias;
  private int m_nLocalPort;
  private int m_nAssignedLocalPort;

  /**
   * Creates a Tunnel from a <code>spec</code> string. For details on this
   * string, see {@link #getSpec()}.
   * <p>
   * Both <code>localAlias</code> and <code>localPort</code> are optional, in
   * which case they default to <code>localhost</code> and <code>0</code>
   * respectively.
   * </p>
   * <p>
   * Examples:
   *
   * <pre>
   * // Equivalaent to new Tunnel(&quot;localhost&quot;, 0, &quot;foobar&quot;, 1234);
   * new Tunnel (&quot;foobar:1234&quot;);
   * // Equivalaent to new Tunnel(&quot;localhost&quot;, 1234, &quot;foobar&quot;, 1234);
   * new Tunnel (&quot;1234:foobar:1234&quot;);
   * // Equivalaent to new Tunnel(&quot;local_foobar&quot;, 1234, &quot;foobar&quot;, 1234);
   * new Tunnel (&quot;local_foobar:1234:foobar:1234&quot;);
   * </pre>
   *
   * @param sSpec
   *        A tunnel spec string
   * @see #Tunnel(String, int, String, int)
   * @see <a href="http://tools.ietf.org/html/rfc4254#section-7">rfc4254</a>
   */
  public Tunnel (@NonNull @Nonempty final String sSpec)
  {
    final String [] aParts = StringHelper.getExplodedArray (':', sSpec, 4);
    if (aParts.length == 4)
    {
      m_sLocalAlias = aParts[0];
      m_nLocalPort = Integer.parseInt (aParts[1]);
      m_sDestinationHostname = aParts[2];
      m_nDestinationPort = Integer.parseInt (aParts[3]);
    }
    else
      if (aParts.length == 3)
      {
        m_nLocalPort = Integer.parseInt (aParts[0]);
        m_sDestinationHostname = aParts[1];
        m_nDestinationPort = Integer.parseInt (aParts[2]);
      }
      else
        if (aParts.length == 2)
        {
          // dynamically assigned port
          m_nLocalPort = 0;
          m_sDestinationHostname = aParts[0];
          m_nDestinationPort = Integer.parseInt (aParts[1]);
        }
        else
          throw new IllegalStateException ("Failed to parse Tunnel spec '" + sSpec + "'");
  }

  /**
   * Creates a Tunnel to <code>destinationPort</code> on
   * <code>destinationHostname</code> from a dynamically assigned port on
   * <code>localhost</code>. Simply calls
   *
   * @param destinationHostname
   *        The hostname to tunnel to
   * @param destinationPort
   *        The port to tunnel to
   * @see #Tunnel(int, String, int)
   * @see <a href="http://tools.ietf.org/html/rfc4254#section-7">rfc4254</a>
   */
  public Tunnel (final String destinationHostname, final int destinationPort)
  {
    this (0, destinationHostname, destinationPort);
  }

  /**
   * Creates a Tunnel to <code>destinationPort</code> on
   * <code>destinationHostname</code> from <code>localPort</code> on
   * <code>localhost</code>.
   *
   * @param localPort
   *        The local port to bind to
   * @param destinationHostname
   *        The hostname to tunnel to
   * @param destinationPort
   *        The port to tunnel to
   * @see #Tunnel(String, int, String, int)
   * @see <a href="http://tools.ietf.org/html/rfc4254#section-7">rfc4254</a>
   */
  public Tunnel (final int localPort, final String destinationHostname, final int destinationPort)
  {
    this (null, localPort, destinationHostname, destinationPort);
  }

  /**
   * Creates a Tunnel to <code>destinationPort</code> on
   * <code>destinationHostname</code> from <code>localPort</code> on
   * <code>localAlias</code>.
   * <p>
   * This is similar in behavior to the <code>-L</code> option in ssh, with the
   * exception that you can specify <code>0</code> for the local port in which
   * case the port will be dynamically allocated and you can
   * {@link #getAssignedLocalPort()} after the tunnel has been started.
   * </p>
   * <p>
   * A common use case for <code>localAlias</code> might be to link your
   * loopback interfaces to names via an entries in <code>/etc/hosts</code>
   * which would allow you to use the same port number for more than one tunnel.
   * For example:
   *
   * <pre>
   * 127.0.0.2 foo
   * 127.0.0.3 bar
   * </pre>
   *
   * Would allow you to have both of these open at the same time:
   *
   * <pre>
   * new Tunnel (&quot;foo&quot;, 1234, &quot;remote_foo&quot;, 1234);
   * new Tunnel (&quot;bar&quot;, 1234, &quot;remote_bar&quot;, 1234);
   * </pre>
   *
   * @param localAlias
   *        The local interface to bind to
   * @param localPort
   *        The local port to bind to
   * @param destinationHostname
   *        The hostname to tunnel to
   * @param destinationPort
   *        The port to tunnel to
   * @see com.jcraft.jsch.Session#setPortForwardingL(String, int, String, int)
   * @see <a href="http://tools.ietf.org/html/rfc4254#section-7">rfc4254</a>
   */
  public Tunnel (final String localAlias, final int localPort, final String destinationHostname, final int destinationPort)
  {
    m_sLocalAlias = localAlias;
    m_nLocalPort = localPort;
    m_sDestinationHostname = destinationHostname;
    m_nDestinationPort = destinationPort;
  }

  /**
   * Returns the local port currently bound to. If <code>0</code> was specified
   * as the port to bind to, this will return the dynamically allocated port,
   * otherwise it will return the port specified.
   *
   * @return The local port currently bound to
   */
  public int getAssignedLocalPort ()
  {
    return m_nAssignedLocalPort == 0 ? m_nLocalPort : m_nAssignedLocalPort;
  }

  /**
   * Returns the hostname of the destination.
   *
   * @return The hostname of the destination
   */
  public String getDestinationHostname ()
  {
    return m_sDestinationHostname;
  }

  /**
   * Returns the port of the destination.
   *
   * @return The port of the destination
   */
  public int getDestinationPort ()
  {
    return m_nDestinationPort;
  }

  /**
   * Returns the local alias bound to. See
   * <a href="http://tools.ietf.org/html/rfc4254#section-7">rfc4254</a> for
   * details on acceptible values.
   *
   * @return The local alias bound to
   */
  public String getLocalAlias ()
  {
    return m_sLocalAlias;
  }

  /**
   * Returns the port this tunnel was configured with. If you want to get the
   * runtime port, use {@link #getAssignedLocalPort()}.
   *
   * @return The port this tunnel was configured with
   */
  public int getLocalPort ()
  {
    return m_nLocalPort;
  }

  /**
   * Returns the spec string (either calculated or specified) for this tunnel.
   * <p>
   * A spec string is composed of 4 parts separated by a colon (<code>:</code>
   * ):
   * <ol>
   * <li><code>localAlias</code> (<i>optional</i>)</li>
   * <li><code>localPort</code> (<i>optional</i>)</li>
   * <li><code>destinationHostname</code></li>
   * <li><code>destinationPort</code></li>
   * </ol>
   *
   * @return The spec string
   */
  @NonNull
  public String getSpec ()
  {
    if (m_sSpec == null)
      m_sSpec = getAsString ().toLowerCase (Locale.US);
    return m_sSpec;
  }

  void setAssignedLocalPort (final int port)
  {
    m_nAssignedLocalPort = port;
  }

  @NonNull
  public String getAsString ()
  {
    return (m_sLocalAlias == null ? "" : m_sLocalAlias + ":") +
           (m_nAssignedLocalPort == 0 ? Integer.toString (m_nLocalPort) : "(0)" + m_nAssignedLocalPort) +
           ":" +
           m_sDestinationHostname +
           ":" +
           m_nDestinationPort;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final Tunnel rhs = (Tunnel) o;
    return getSpec ().equals (rhs.getSpec ());
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (getSpec ()).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Spec", m_sSpec)
                                       .append ("DestinationHostname", m_sDestinationHostname)
                                       .append ("DestinationPort", m_nDestinationPort)
                                       .append ("LocalAlias", m_sLocalAlias)
                                       .append ("LocalPort", m_nLocalPort)
                                       .append ("AssignedLocalPort", m_nAssignedLocalPort)
                                       .getToString ();
  }
}
