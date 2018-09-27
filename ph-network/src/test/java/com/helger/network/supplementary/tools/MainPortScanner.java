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
                                           x -> System.out.println ("  " +
                                                                    StringHelper.getConcatenatedOnDemand (x.getName (),
                                                                                                          ": ",
                                                                                                          x.getDescription ())));
        }
      }
    aSW.stop ();
    LOGGER.info (((nEndPort - nStartPort) * aTypes.size ()) + " ports checked in " + aSW.getMillis () + " ms");
  }
}
