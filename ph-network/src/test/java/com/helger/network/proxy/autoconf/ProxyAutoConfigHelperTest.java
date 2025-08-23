/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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

import java.net.Proxy;

import javax.script.ScriptException;

import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.url.URLHelper;
import com.helger.collection.commons.ICommonsList;
import com.helger.io.resource.ClassPathResource;
import com.helger.io.resource.IReadableResource;
import com.helger.network.proxy.settings.IProxySettings;
import com.helger.network.proxy.settings.ProxySettings;

import jakarta.annotation.Nonnull;

/**
 * Unit test class for class {@link ProxyAutoConfigHelper}.
 *
 * @author Philip Helger
 */
public final class ProxyAutoConfigHelperTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ProxyAutoConfigHelperTest.class);
  private static final String [] PAC_FILES = { "brz-proxy.pac",
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
    Assume.assumeTrue (ProxyAutoConfigHelper.isNashornScriptEngineAvailable ());

    for (final String sFile : PAC_FILES)
    {
      LOGGER.info ("Reading " + sFile);
      final IReadableResource aRes = new ClassPathResource ("external/proxyautoconf/pacfiles/" + sFile);
      assertTrue (aRes.exists ());
      final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (aRes);
      final ICommonsList <IProxySettings> aPC = aPACHelper.getProxyListForURL (URLHelper.getAsURI ("http://www.orf.at/index.html"));
      assertNotNull (sFile + " failed", aPC);
      assertFalse (sFile + " failed", aPC.isEmpty ());
      LOGGER.info ("  Found the following " + aPC.size () + " entries: " + aPC);
    }
  }

  @Nonnull
  private static IProxySettings _getResolved (final String sJS) throws ScriptException
  {
    final String sCode = "function FindProxyForURL(url, host) { " + sJS + " }";
    final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (sCode);
    return aPACHelper.getProxyListForURL ("any", "host").getFirstOrNull ();
  }

  @Test
  public void testExplicit () throws ScriptException
  {
    Assume.assumeTrue (ProxyAutoConfigHelper.isNashornScriptEngineAvailable ());

    assertEquals (new ProxySettings (Proxy.Type.HTTP, "1.2.3.4", 8080), _getResolved ("return 'PROXY 1.2.3.4:8080';"));
    assertEquals (ProxySettings.createNoProxySettings (),
                  _getResolved ("return isInNetEx('127.0.0.1', '127.0.0.0/16') ? 'DIRECT' : 'PROXY 1.2.3.4:8080';"));
    assertEquals (ProxySettings.createNoProxySettings (),
                  _getResolved ("return isInNetEx('127.0.0.1', '127.0.0.0/24') ? 'DIRECT' : 'PROXY 1.2.3.4:8080';"));
    assertEquals (ProxySettings.createNoProxySettings (),
                  _getResolved ("return isInNetEx('127.0.1.0', '127.0.0.0/24') ? 'PROXY 1.2.3.4:8080' : 'DIRECT';"));
  }
}
