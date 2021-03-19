/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
  private static final NetworkPortHelper INSTANCE = new NetworkPortHelper ();

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
    return checkPortOpen (sHostName, nPort, nTimeoutMillisecs, false);
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
   * @param bSilentMode
   *        <code>true</code> for silent mode, <code>false</code> if not
   * @return Never <code>null</code>.
   * @since 9.1.2
   */
  @Nonnull
  public static ENetworkPortStatus checkPortOpen (@Nonnull @Nonempty final String sHostName,
                                                  @Nonnegative final int nPort,
                                                  @Nonnegative final int nTimeoutMillisecs,
                                                  final boolean bSilentMode)
  {
    ValueEnforcer.notEmpty (sHostName, "Hostname");
    ValueEnforcer.isGE0 (nPort, "Port");
    ValueEnforcer.isGE0 (nTimeoutMillisecs, "TimeoutMillisecs");

    if (!bSilentMode)
      LOGGER.info ("Checking TCP port status for " + sHostName + ":" + nPort + " with timeouf of " + nTimeoutMillisecs + " ms");

    ENetworkPortStatus ret;
    try (final Socket aSocket = new Socket ())
    {
      aSocket.setReuseAddress (true);
      final SocketAddress aSocketAddr = new InetSocketAddress (sHostName, nPort);
      aSocket.connect (aSocketAddr, nTimeoutMillisecs);
      ret = ENetworkPortStatus.PORT_IS_OPEN;
    }
    catch (final java.net.UnknownHostException ex)
    {
      ret = ENetworkPortStatus.HOST_NOT_EXISTING;
    }
    catch (final java.net.SocketTimeoutException ex)
    {
      ret = ENetworkPortStatus.CONNECTION_TIMEOUT;
    }
    catch (final java.net.ConnectException ex)
    {
      // E.g. for port 0
      ret = ENetworkPortStatus.GENERIC_IO_ERROR;
    }
    catch (final IOException ex)
    {
      // Can also be:
      // Connection refused: connect
      if (ex.getMessage ().startsWith ("Connection refused"))
        ret = ENetworkPortStatus.PORT_IS_CLOSED;
      else
      {
        if (!bSilentMode)
          LOGGER.error ("Other error checking TCP port status", ex);
        ret = ENetworkPortStatus.GENERIC_IO_ERROR;
      }
    }

    if (!bSilentMode)
      LOGGER.info ("  Result of the port check is " + ret);

    return ret;
  }
}
