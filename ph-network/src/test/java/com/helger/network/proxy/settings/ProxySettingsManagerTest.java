/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.network.proxy.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.Proxy;

import org.junit.Before;
import org.junit.Test;

import com.helger.commons.collection.impl.CommonsArrayList;

/**
 * Test class for class {@link ProxySettings}.
 *
 * @author Philip Helger
 */
public final class ProxySettingsManagerTest
{
  @Before
  public void before ()
  {
    ProxySettingsManager.removeAllProviders ();
  }

  @Test
  public void testEmpty ()
  {
    assertNotNull (ProxySettingsManager.getAllProviders ());
    assertTrue (ProxySettingsManager.getAllProviders ().isEmpty ());
  }

  @Test
  public void testRegister ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, "b", "c");
    final IProxySettingsProvider aPSP = (sProtocol, sHost, nPort) -> new CommonsArrayList <> (a);
    ProxySettingsManager.registerProvider (aPSP);
    assertEquals (1, ProxySettingsManager.getAllProviders ().size ());
    ProxySettingsManager.registerProvider ( (sProtocol, sHost, nPort) -> new CommonsArrayList <> (a));
    assertEquals (2, ProxySettingsManager.getAllProviders ().size ());
    assertTrue (ProxySettingsManager.unregisterProvider (aPSP).isChanged ());
    assertEquals (1, ProxySettingsManager.getAllProviders ().size ());
    assertFalse (ProxySettingsManager.unregisterProvider (aPSP).isChanged ());
    assertEquals (1, ProxySettingsManager.getAllProviders ().size ());
  }

  @Test
  public void testRegisterSameTwice ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, "b", "c");
    final IProxySettingsProvider aPSP = (sProtocol, sHost, nPort) -> new CommonsArrayList <> (a);
    ProxySettingsManager.registerProvider (aPSP);
    assertEquals (1, ProxySettingsManager.getAllProviders ().size ());
    ProxySettingsManager.registerProvider (aPSP);
    assertEquals (2, ProxySettingsManager.getAllProviders ().size ());
    assertTrue (ProxySettingsManager.unregisterProvider (aPSP).isChanged ());
    assertEquals (1, ProxySettingsManager.getAllProviders ().size ());
    assertTrue (ProxySettingsManager.unregisterProvider (aPSP).isChanged ());
    assertEquals (0, ProxySettingsManager.getAllProviders ().size ());
  }
}
