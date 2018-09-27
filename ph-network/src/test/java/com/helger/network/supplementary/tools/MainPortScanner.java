package com.helger.network.supplementary.tools;

import java.util.EnumSet;

import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.network.port.CNetworkPort;
import com.helger.network.port.DefaultNetworkPorts;
import com.helger.network.port.ENetworkProtocol;

public final class MainPortScanner
{
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
          System.out.println (eType.name () + " Port " + nPort + " is used");
          DefaultNetworkPorts.forEachPort (x -> x.getPort () == nFinalPort && x.getProtocol () == eType,
                                           x -> System.out.println ("  " +
                                                                    StringHelper.getConcatenatedOnDemand (x.getName (),
                                                                                                          ": ",
                                                                                                          x.getDescription ())));
        }
      }
    aSW.stop ();
    System.out.println (((nEndPort - nStartPort) * aTypes.size ()) + " ports checked in " + aSW.getMillis () + " ms");
  }
}
