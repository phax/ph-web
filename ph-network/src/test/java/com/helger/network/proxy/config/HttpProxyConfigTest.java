/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.Proxy;

import org.junit.Test;

import com.helger.commons.collection.impl.CommonsArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test class for class {@link HttpProxyConfig}.
 *
 * @author Philip Helger
 */
public final class HttpProxyConfigTest
{
  @Test
  public void testAll ()
  {
    try
    {
      final HttpProxyConfig aPC = new HttpProxyConfig (EHttpProxyType.HTTP, "host", 8080);
      assertEquals (EHttpProxyType.HTTP, aPC.getType ());
      assertEquals ("host", aPC.getHost ());
      assertEquals (8080, aPC.getPort ());
      assertNotNull (aPC.getNonProxyHosts ());
      assertTrue (aPC.getNonProxyHosts ().isEmpty ());
      assertNotNull (aPC.getAsProxy ());
      assertEquals (Proxy.Type.HTTP, aPC.getAsProxy ().type ());
      aPC.activateGlobally ();
    }
    finally
    {
      HttpProxyConfig.deactivateGlobally ();
    }
  }

  @SuppressWarnings ("unused")
  @Test
  @SuppressFBWarnings ({ "NP_NONNULL_PARAM_VIOLATION", "TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED" })
  public void testInvalid ()
  {
    try
    {
      // null type not allowed
      new HttpProxyConfig (null, "host", 8080);
      fail ();
    }
    catch (final NullPointerException ex)
    {}

    try
    {
      // null host not allowed
      new HttpProxyConfig (EHttpProxyType.HTTPS, null, 8080);
      fail ();
    }
    catch (final NullPointerException ex)
    {}

    try
    {
      // empty host not allowed
      new HttpProxyConfig (EHttpProxyType.HTTPS, "", 8080);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    try
    {
      // port invalid
      new HttpProxyConfig (EHttpProxyType.HTTPS, "host", -1);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
    try
    {
      // port invalid
      new HttpProxyConfig (EHttpProxyType.HTTPS, "host", 100456);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {}
  }

  @Test
  public void testNonProxyPorts ()
  {
    try
    {
      HttpProxyConfig aPC = new HttpProxyConfig (EHttpProxyType.HTTP, "host", 8080);
      assertNotNull (aPC.getNonProxyHosts ());
      assertTrue (aPC.getNonProxyHosts ().isEmpty ());
      aPC.activateGlobally ();

      aPC = new HttpProxyConfig (EHttpProxyType.HTTP, "host", 8080, new CommonsArrayList<> ("localhost"));
      assertNotNull (aPC.getNonProxyHosts ());
      assertEquals (1, aPC.getNonProxyHosts ().size ());
      assertTrue (aPC.getNonProxyHosts ().contains ("localhost"));
      assertFalse (aPC.getNonProxyHosts ().contains ("127.0.0.1"));
      aPC.activateGlobally ();

      aPC = new HttpProxyConfig (EHttpProxyType.HTTP, "host", 8080, new CommonsArrayList<> ("localhost", "127.0.0.1"));
      assertNotNull (aPC.getNonProxyHosts ());
      assertEquals (2, aPC.getNonProxyHosts ().size ());
      assertTrue (aPC.getNonProxyHosts ().contains ("localhost"));
      assertTrue (aPC.getNonProxyHosts ().contains ("127.0.0.1"));
      aPC.activateGlobally ();

      aPC = new HttpProxyConfig (EHttpProxyType.HTTP,
                                 "host",
                                 8080,
                                 new CommonsArrayList<> (null, "localhost", "", "127.0.0.1", "", "", "", ""));
      assertNotNull (aPC.getNonProxyHosts ());
      assertEquals (2, aPC.getNonProxyHosts ().size ());
      assertTrue (aPC.getNonProxyHosts ().contains ("localhost"));
      assertTrue (aPC.getNonProxyHosts ().contains ("127.0.0.1"));
      aPC.activateGlobally ();
    }
    finally
    {
      HttpProxyConfig.deactivateGlobally ();
    }
  }
}
