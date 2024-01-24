/*
 * Copyright (C) 2016-2024 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jsch.session.DefaultSessionFactory;
import com.jcraft.jsch.JSchException;

public final class TunnelConnectionManagerTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (TunnelConnectionManagerTest.class);

  @Test
  public void testGetTunnel ()
  {
    try (final TunnelConnectionManager manager = new TunnelConnectionManager (new DefaultSessionFactory ()))
    {
      assertNotNull (manager);

      final List <String> pathAndSpecList = new ArrayList <> ();
      pathAndSpecList.add ("joe@crabshack|crab:10:imitationcrab:20");
      pathAndSpecList.add ("bob@redlobster|lobster:15:tail:20");
      try
      {
        manager.setTunnelConnections (pathAndSpecList);
      }
      catch (final JSchException e)
      {
        LOGGER.info ("unable to set pathAndSpecList: ", e);
        fail ("unable to setPathAndSpecList: " + e.getMessage ());
      }

      Tunnel tunnel = manager.getTunnel ("imitationcrab", 20);
      assertEquals ("crab", tunnel.getLocalAlias ());
      assertEquals (10, tunnel.getLocalPort ());
      tunnel = manager.getTunnel ("tail", 20);
      assertEquals ("lobster", tunnel.getLocalAlias ());
      assertEquals (15, tunnel.getLocalPort ());
    }
    catch (final JSchException e)
    {
      LOGGER.info ("unable to create TunnelConnectionManager: ", e);
      fail ("unable to create TunnelConnectionManager: " + e.getMessage ());
    }
  }
}
