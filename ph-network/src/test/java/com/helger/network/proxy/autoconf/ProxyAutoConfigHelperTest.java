/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import javax.script.ScriptException;

import org.junit.Test;

import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.network.proxy.EHttpProxyType;
import com.helger.network.proxy.HttpProxyConfig;
import com.helger.network.proxy.IProxyConfig;

public final class ProxyAutoConfigHelperTest
{
  private static final String [] PAC_FILES = new String [] { "brz-proxy.pac",
                                                             "wikipedia-pac.js",
                                                             "returnproxy-complex.js",
                                                             "returnproxy-simple-with-loadbalancing.js",
                                                             "returnproxy-simple.js",
                                                             "ente.regione.emr.it.js" };

  @Test
  public void testFindProxyForURL () throws ScriptException
  {
    for (final String sFile : PAC_FILES)
    {
      final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (new ClassPathResource ("proxyautoconf/pacfiles/" +
                                                                                                 sFile));
      assertNotNull (sFile + " failed", aPACHelper.findProxyForURL ("http://www.orf.at/index.html", "www.orf.at"));
    }
  }

  @Test
  public void testGetProxyListForURL () throws ScriptException
  {
    for (final String sFile : PAC_FILES)
    {
      final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (new ClassPathResource ("proxyautoconf/pacfiles/" +
                                                                                                 sFile));
      final ICommonsList <IProxyConfig> aPC = aPACHelper.getProxyListForURL ("http://www.orf.at/index.html",
                                                                             "www.orf.at");
      assertNotNull (sFile + " failed", aPC);
      assertFalse (sFile + " failed", aPC.isEmpty ());
    }
  }

  @Test
  public void testExplicit () throws ScriptException
  {
    final String sCode = "function FindProxyForURL(url, host) { return \"PROXY 1.2.3.4:8080\"; }";
    final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (sCode);
    assertEquals (new HttpProxyConfig (EHttpProxyType.HTTP, "1.2.3.4", 8080),
                  aPACHelper.getProxyListForURL ("any", "host").getFirst ());
  }
}
