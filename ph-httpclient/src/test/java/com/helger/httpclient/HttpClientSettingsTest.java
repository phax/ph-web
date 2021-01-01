/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.http.tls.ETLSConfigurationMode_2020_02;

/**
 * Test class for class {@link HttpClientSettings}.
 *
 * @author Philip Helger
 */
public final class HttpClientSettingsTest
{
  @Test
  public void testAddNonProxyHostsFromPipeString ()
  {
    final HttpClientSettings x = new HttpClientSettings ();
    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString (null);
    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("");
    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("          ");
    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("   |    ");
    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString ("  |||||   ||  |||| |   |");
    assertEquals (0, x.nonProxyHosts ().size ());
    x.addNonProxyHostsFromPipeString (" 127.0.0.1 | localhost ");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("127.0.0.1"));
    assertTrue (x.nonProxyHosts ().contains ("localhost"));
    x.addNonProxyHostsFromPipeString ("127.0.0.1|localhost");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("127.0.0.1"));
    assertTrue (x.nonProxyHosts ().contains ("localhost"));
    x.addNonProxyHostsFromPipeString ("127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|127.0.0.1|localhost");
    assertEquals (2, x.nonProxyHosts ().size ());
    assertTrue (x.nonProxyHosts ().contains ("127.0.0.1"));
    assertTrue (x.nonProxyHosts ().contains ("localhost"));
  }

  @Test
  public void testClone ()
  {
    final HttpClientSettings x = new HttpClientSettings ();
    assertNotNull (x.getClone ());
    x.setTLSConfigurationMode (ETLSConfigurationMode_2020_02.MODERN);
    assertSame (ETLSConfigurationMode_2020_02.MODERN, x.getClone ().getTLSConfigurationMode ());
    x.setUserAgent ("bla");
    assertEquals ("bla", x.getClone ().getUserAgent ());
  }
}
