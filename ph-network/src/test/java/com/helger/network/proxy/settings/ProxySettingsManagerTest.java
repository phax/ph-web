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
