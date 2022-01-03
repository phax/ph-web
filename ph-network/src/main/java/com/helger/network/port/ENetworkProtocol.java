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
package com.helger.network.port;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ServerSocketFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;

/**
 * Network protocol enumeration.
 *
 * @author Philip Helger
 */
public enum ENetworkProtocol implements IHasID <String>
{
  TCP ("tcp")
  {
    @Override
    public boolean isPortAvailable (final int nPort)
    {
      if (nPort >= CNetworkPort.MINIMUM_PORT_NUMBER)
        try (final ServerSocket aSocket = ServerSocketFactory.getDefault ()
                                                             .createServerSocket (nPort, 1, InetAddress.getByName ("localhost")))
        {
          aSocket.setReuseAddress (true);
          return true;
        }
        catch (final Exception ex)
        {
          // Fall through
        }
      return false;
    }
  },

  UDP ("udp")
  {
    @Override
    public boolean isPortAvailable (final int nPort)
    {
      if (nPort >= CNetworkPort.MINIMUM_PORT_NUMBER)
        try (final DatagramSocket aSocket = new DatagramSocket (nPort, InetAddress.getByName ("localhost")))
        {
          aSocket.setReuseAddress (true);
          return true;
        }
        catch (final Exception ex)
        {
          // Fall through
        }
      return false;
    }
  };

  private final String m_sID;

  ENetworkProtocol (@Nonnull @Nonempty final String sID)
  {
    m_sID = sID;
  }

  /**
   * Determine if the specified port for this type is currently available on
   * localhost.
   *
   * @param nPort
   *        The port to check. Must be &gt; 0 and &lt; 0xffff
   * @return <code>true</code> if the port is available, <code>false</code> if
   *         not.
   * @see #isPortUsed(int)
   */
  public abstract boolean isPortAvailable (int nPort);

  /**
   * Determine if the specified port for this type is currently used on
   * localhost.
   *
   * @param nPort
   *        The port to check. Must be &gt; 0 and &lt; 0xffff
   * @return <code>true</code> if the port is used, <code>false</code> if not.
   * @see #isPortAvailable(int)
   */
  public boolean isPortUsed (final int nPort)
  {
    return !isPortAvailable (nPort);
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nullable
  public static ENetworkProtocol getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (ENetworkProtocol.class, sID);
  }
}
