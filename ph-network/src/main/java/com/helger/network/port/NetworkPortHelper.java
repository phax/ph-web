/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.network.port;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * Network port helper class. Contains some utility methods.
 *
 * @author Philip Helger
 */
@Immutable
public final class NetworkPortHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (NetworkPortHelper.class);

  @PresentForCodeCoverage
  private static final NetworkPortHelper s_aInstance = new NetworkPortHelper ();

  private NetworkPortHelper ()
  {}

  /**
   * Check of the passed port number is theoretically valid. Valid ports must be
   * in the range of 0-65535.
   *
   * @param nPort
   *        The port number to be tested.
   * @return <code>true</code> if the port number is valid, <code>false</code>
   *         otherwise.
   */
  public static boolean isValidPort (final int nPort)
  {
    return nPort >= CNetworkPort.MINIMUM_PORT_NUMBER && nPort <= CNetworkPort.MAXIMUM_PORT_NUMBER;
  }

  /**
   * Check the status of a remote port.
   *
   * @param sHostName
   *        Hostname or IP address to check.
   * @param nPort
   *        Port number to check.
   * @param nTimeoutMillisecs
   *        Connection timeout in milliseconds.
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static ENetworkPortStatus checkPortOpen (@Nonnull @Nonempty final String sHostName,
                                                  @Nonnegative final int nPort,
                                                  @Nonnegative final int nTimeoutMillisecs)
  {
    ValueEnforcer.notEmpty (sHostName, "Hostname");
    ValueEnforcer.isGE0 (nPort, "Port");
    ValueEnforcer.isGE0 (nTimeoutMillisecs, "TimeoutMillisecs");

    LOGGER.info ("Checking TCP port status for " +
                 sHostName +
                 ":" +
                 nPort +
                 " with timeouf of " +
                 nTimeoutMillisecs +
                 " ms");

    try (final Socket aSocket = new Socket ())
    {
      aSocket.setReuseAddress (true);
      final SocketAddress aSocketAddr = new InetSocketAddress (sHostName, nPort);
      aSocket.connect (aSocketAddr, nTimeoutMillisecs);
      return ENetworkPortStatus.PORT_IS_OPEN;
    }
    catch (final IOException ex)
    {
      // Can also be:
      // Connection refused: connect
      if (ex.getMessage ().startsWith ("Connection refused"))
        return ENetworkPortStatus.PORT_IS_CLOSED;
      if (ex instanceof java.net.UnknownHostException)
        return ENetworkPortStatus.HOST_NOT_EXISTING;
      if (ex instanceof java.net.SocketTimeoutException)
        return ENetworkPortStatus.CONNECTION_TIMEOUT;
      if (ex instanceof java.net.ConnectException)
      {
        // E.g. for port 0
        return ENetworkPortStatus.GENERIC_IO_ERROR;
      }
      LOGGER.error ("Other error checking TCP port status", ex);
      return ENetworkPortStatus.GENERIC_IO_ERROR;
    }
  }
}
