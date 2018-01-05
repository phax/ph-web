/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.network.proxy.autoconf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.network.proxy.EHttpProxyType;
import com.helger.network.proxy.HttpProxyConfig;
import com.helger.network.proxy.IProxyConfig;
import com.helger.network.proxy.NoProxyConfig;

/**
 * Unit test class for class {@link ProxyAutoConfigHelper}.
 *
 * @author Philip Helger
 */
public final class ProxyAutoConfigHelperTest
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ProxyAutoConfigHelperTest.class);
  private static final String [] PAC_FILES = new String [] { "brz-proxy.pac",
                                                             "wikipedia-pac.js",
                                                             "returnproxy-complex.js",
                                                             "returnproxy-simple-with-loadbalancing.js",
                                                             "returnproxy-simple.js",
                                                             "ente.regione.emr.it.js",
                                                             "example1.js",
                                                             "example2.js" };

  @Test
  public void testGetProxyListForURL () throws ScriptException
  {
    for (final String sFile : PAC_FILES)
    {
      s_aLogger.info ("Reading " + sFile);
      final IReadableResource aRes = new ClassPathResource ("proxyautoconf/pacfiles/" + sFile);
      assertTrue (aRes.exists ());
      final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (aRes);
      final ICommonsList <IProxyConfig> aPC = aPACHelper.getProxyListForURL ("http://www.orf.at/index.html",
                                                                             "www.orf.at");
      assertNotNull (sFile + " failed", aPC);
      assertFalse (sFile + " failed", aPC.isEmpty ());
    }
  }

  @Nonnull
  private static IProxyConfig _getResolved (final String sJS) throws ScriptException
  {
    final String sCode = "function FindProxyForURL(url, host) { " + sJS + " }";
    final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (sCode);
    return aPACHelper.getProxyListForURL ("any", "host").getFirst ();
  }

  @Test
  public void testExplicit () throws ScriptException
  {
    assertEquals (new HttpProxyConfig (EHttpProxyType.HTTP, "1.2.3.4", 8080),
                  _getResolved ("return 'PROXY 1.2.3.4:8080';"));
    assertEquals (new NoProxyConfig (),
                  _getResolved ("return isInNetEx('127.0.0.1', '127.0.0.0/16') ? 'DIRECT' : 'PROXY 1.2.3.4:8080';"));
    assertEquals (new NoProxyConfig (),
                  _getResolved ("return isInNetEx('127.0.0.1', '127.0.0.0/24') ? 'DIRECT' : 'PROXY 1.2.3.4:8080';"));
    assertEquals (new NoProxyConfig (),
                  _getResolved ("return isInNetEx('127.0.1.0', '127.0.0.0/24') ? 'PROXY 1.2.3.4:8080' : 'DIRECT';"));
  }
}
