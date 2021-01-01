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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Represents a single network port.
 *
 * @author Philip Helger
 */
@Immutable
public class NetworkPort implements INetworkPort
{
  private final int m_nPort;
  private final ENetworkProtocol m_eProtocol;
  private final String m_sName;
  private final String m_sDescription;

  public NetworkPort (@Nonnegative final int nPort,
                      @Nonnull final ENetworkProtocol eProtocol,
                      @Nonnull final String sName,
                      @Nonnull final String sDescription)
  {
    ValueEnforcer.isTrue (NetworkPortHelper.isValidPort (nPort), () -> "Port is illegal: " + nPort);
    m_nPort = nPort;
    m_eProtocol = ValueEnforcer.notNull (eProtocol, "Protocol");
    m_sName = ValueEnforcer.notNull (sName, "Name");
    m_sDescription = ValueEnforcer.notNull (sDescription, "Description");
  }

  @Nonnegative
  public int getPort ()
  {
    return m_nPort;
  }

  @Nonnull
  public ENetworkProtocol getProtocol ()
  {
    return m_eProtocol;
  }

  @Nonnull
  public String getName ()
  {
    return m_sName;
  }

  @Nonnull
  public String getDescription ()
  {
    return m_sDescription;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final NetworkPort rhs = (NetworkPort) o;
    return m_nPort == rhs.m_nPort &&
           m_eProtocol.equals (rhs.m_eProtocol) &&
           m_sName.equals (rhs.m_sName) &&
           m_sDescription.equals (rhs.m_sDescription);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_nPort).append (m_eProtocol).append (m_sName).append (m_sDescription).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("port", m_nPort)
                                       .append ("protocol", m_eProtocol)
                                       .append ("name", m_sName)
                                       .append ("description", m_sDescription)
                                       .getToString ();
  }
}
