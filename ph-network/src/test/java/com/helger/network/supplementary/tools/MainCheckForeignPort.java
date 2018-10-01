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
package com.helger.network.supplementary.tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;

/**
 * Small tool to list all TCP and UDP ports that are currently in use on this
 * machine.
 *
 * @author Philip Helger
 */
public final class MainCheckForeignPort
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainCheckForeignPort.class);

  static enum EPortStatus
  {
    PORT_IS_OPEN,
    PORT_IS_CLOSED,
    HOST_NOT_EXISTING,
    CONNECTION_TIMEOUT,
    GENERIC_IO_ERROR;

    public boolean isPortOpen ()
    {
      return this == PORT_IS_OPEN;
    }
  }

  @Nonnull
  public static EPortStatus checkPortOpen (@Nonnull @Nonempty final String sHostName,
                                           @Nonnegative final int nPort,
                                           @Nonnegative final int nTimeoutMillisecs)
  {
    ValueEnforcer.notEmpty (sHostName, "Hostname");
    ValueEnforcer.isGE0 (nPort, "Port");
    ValueEnforcer.isGE0 (nTimeoutMillisecs, "TimeoutMillisecs");

    try (final Socket aSocket = new Socket ())
    {
      aSocket.setReuseAddress (true);
      final SocketAddress aSocketAddr = new InetSocketAddress (sHostName, nPort);
      aSocket.connect (aSocketAddr, nTimeoutMillisecs);
      return EPortStatus.PORT_IS_OPEN;
    }
    catch (final IOException ex)
    {
      if (ex.getMessage ().equals ("Connection refused"))
        return EPortStatus.PORT_IS_CLOSED;
      if (ex instanceof java.net.UnknownHostException)
        return EPortStatus.HOST_NOT_EXISTING;
      if (ex instanceof java.net.SocketTimeoutException)
        return EPortStatus.CONNECTION_TIMEOUT;
      if (ex instanceof java.net.ConnectException)
      {
        // E.g. for port 0
        return EPortStatus.GENERIC_IO_ERROR;
      }
      LOGGER.warn ("Other error", ex);
      return EPortStatus.GENERIC_IO_ERROR;
    }
  }

  private static void _check (final String sHostName, final int nPort)
  {
    final EPortStatus eError = checkPortOpen (sHostName, nPort, 2 * 1000);
    LOGGER.info (sHostName + ":" + nPort + " = " + eError);
  }

  public static void main (final String [] args)
  {
    LOGGER.info ("Start");
    // Good
    _check ("directory.peppol.eu", 80);
    _check ("directory.peppol.eu", 443);
    _check ("www.erechnung.gv.at", 80);
    _check ("www.erechnung.gv.at", 443);
    _check ("phax.mooo.com", 80);
    _check ("phax.mooo.com", 443);
    // HOST_NOT_EXISTING
    _check ("bla.peppol.eu", 443);
    _check ("erechnung", 80);
    _check ("e.rechnung", 80);
    // CONNECTION_TIMEOUT
    _check ("directory.peppol.eu", 79);
    _check ("phax.mooo.com", 79);
  }
}
