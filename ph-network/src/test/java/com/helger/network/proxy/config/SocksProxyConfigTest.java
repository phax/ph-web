/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.network.proxy.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.Proxy;

import org.junit.Test;

import com.helger.network.proxy.config.SocksProxyConfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test class for class {@link SocksProxyConfig}.
 *
 * @author Philip Helger
 */
public final class SocksProxyConfigTest
{
  @Test
  public void testAll ()
  {
    try
    {
      SocksProxyConfig aPC = new SocksProxyConfig ("myserver");
      assertEquals ("myserver", aPC.getHost ());
      assertEquals (SocksProxyConfig.DEFAULT_SOCKS_PROXY_PORT, aPC.getPort ());
      aPC.activateGlobally ();
      assertNotNull (aPC.getAsProxy ());
      assertEquals (Proxy.Type.SOCKS, aPC.getAsProxy ().type ());

      aPC = new SocksProxyConfig ("myserver", 4711);
      assertEquals ("myserver", aPC.getHost ());
      assertEquals (4711, aPC.getPort ());
      assertNotNull (aPC.toString ());
    }
    finally
    {
      SocksProxyConfig.deactivateGlobally ();
    }
  }

  @SuppressWarnings ("unused")
  @Test
  @SuppressFBWarnings ({ "NP_NONNULL_PARAM_VIOLATION", "TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED" })
  public void testInvalid ()
  {
    try
    {
      // null host not allowed
      new SocksProxyConfig (null);
      fail ();
    }
    catch (final NullPointerException ex)
    {}

    try
    {
      // empty host not allowed
      new SocksProxyConfig ("");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}

    try
    {
      // null host not allowed
      new SocksProxyConfig (null, 1234);
      fail ();
    }
    catch (final NullPointerException ex)
    {}
    try
    {
      // empty host not allowed
      new SocksProxyConfig ("", 1234);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    try
    {
      // port too small
      new SocksProxyConfig ("myserver", -1);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    try
    {
      // port too large
      new SocksProxyConfig ("myserver", 100456);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
  }
}
