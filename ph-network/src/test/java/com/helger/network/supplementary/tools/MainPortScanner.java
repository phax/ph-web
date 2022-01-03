/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.network.port.CNetworkPort;
import com.helger.network.port.DefaultNetworkPorts;
import com.helger.network.port.ENetworkProtocol;

/**
 * Small tool to list all TCP and UDP ports that are currently in use on this
 * machine.
 *
 * @author Philip Helger
 */
public final class MainPortScanner
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainPortScanner.class);

  public static void main (final String [] args)
  {
    final int nStartPort = CNetworkPort.MINIMUM_PORT_NUMBER;
    final int nEndPort = CNetworkPort.MAXIMUM_PORT_NUMBER;
    final EnumSet <ENetworkProtocol> aTypes = EnumSet.allOf (ENetworkProtocol.class);

    final StopWatch aSW = StopWatch.createdStarted ();
    for (int nPort = nStartPort; nPort <= nEndPort; ++nPort)
      for (final ENetworkProtocol eType : aTypes)
      {
        final int nFinalPort = nPort;
        final boolean bIsUsed = eType.isPortUsed (nPort);
        if (bIsUsed)
        {
          LOGGER.info (eType.name () + " Port " + nPort + " is used");
          DefaultNetworkPorts.forEachPort (x -> x.getPort () == nFinalPort && x.getProtocol () == eType,
                                           x -> LOGGER.info ("  " +
                                                             StringHelper.getConcatenatedOnDemand (x.getName (),
                                                                                                   ": ",
                                                                                                   x.getDescription ())));
        }
      }
    aSW.stop ();

    final int nCount = (nEndPort - nStartPort) * aTypes.size ();
    LOGGER.info (nCount + " ports checked in " + aSW.getMillis () + " ms");
  }
}
