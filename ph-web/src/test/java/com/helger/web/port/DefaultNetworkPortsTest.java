/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Comparator;

import org.junit.Ignore;
import org.junit.Test;

import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.MicroDocument;
import com.helger.commons.microdom.serialize.MicroWriter;
import com.helger.commons.string.StringHelper;

/**
 * Test class for class {@link DefaultNetworkPorts}.
 *
 * @author Philip Helger
 */
public final class DefaultNetworkPortsTest
{
  @Test
  public void testGetPortOfKey ()
  {
    assertNotNull (DefaultNetworkPorts.getAllPorts ());
    assertFalse (DefaultNetworkPorts.getAllPorts (80).isEmpty ());
  }

  @Test
  public void test ()
  {
    assertEquals (175, DefaultNetworkPorts.UDP_175_vmnet.getPort ());
    assertEquals (ENetworkProtocol.UDP, DefaultNetworkPorts.UDP_175_vmnet.getProtocol ());
  }

  @Ignore
  @Test
  public void exportIntoCodelist ()
  {
    final IMicroDocument aDoc = new MicroDocument ();
    final IMicroElement ePorts = aDoc.appendElement ("ports");
    for (final INetworkPort aPort : CollectionHelper.getSorted (DefaultNetworkPorts.getAllPorts (),
                                                                Comparator.comparingInt (NetworkPort::getPort)
                                                                          .thenComparing (Comparator.comparingInt (p -> p.getProtocol ()
                                                                                                                         .ordinal ()))
                                                                          .thenComparing (NetworkPort::getName)))
    {
      final IMicroElement ePort = ePorts.appendElement ("defaultport");
      ePort.setAttribute ("port", aPort.getPort ());
      ePort.setAttribute ("protocol", aPort.getProtocol ().getID ());
      if (StringHelper.hasText (aPort.getName ()))
        ePort.setAttribute ("name", aPort.getName ());
      if (StringHelper.hasText (aPort.getDescription ()))
        ePort.setAttribute ("desc", aPort.getDescription ());
    }
    MicroWriter.writeToFile (aDoc, new File ("src/main/resources/codelists/default-network-ports.xml"));
  }
}
