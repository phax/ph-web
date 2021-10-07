/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.junit.Test;

import com.helger.commons.collection.ArrayHelper;

/**
 * Test class for class {@link ProxySettings}.
 *
 * @author Philip Helger
 */
public final class ProxySettingsTest
{
  @Test
  public void testHttp ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, "b", "c");
    assertSame (Proxy.Type.HTTP, a.getProxyType ());
    assertEquals ("a", a.getProxyHost ());
    assertEquals (17, a.getProxyPort ());
    assertEquals ("b", a.getProxyUserName ());
    assertTrue (a.hasProxyUserName ());
    assertEquals ("c", a.getProxyPassword ());
    assertTrue (a.hasProxyPassword ());

    final Proxy p = a.getAsProxy (false);
    assertNotNull (p);
    assertEquals (Proxy.Type.HTTP, p.type ());
    assertTrue (p.address () instanceof InetSocketAddress);
    assertEquals ("a", ((InetSocketAddress) p.address ()).getHostString ());
    assertEquals (17, ((InetSocketAddress) p.address ()).getPort ());
    assertTrue (a.hasSocketAddress (p.address ()));

    final PasswordAuthentication aPA = a.getAsPasswordAuthentication ();
    assertNotNull (aPA);
    assertEquals ("b", aPA.getUserName ());
    assertArrayEquals ("c".toCharArray (), aPA.getPassword ());
  }

  @Test
  public void testSocks ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.SOCKS, "a", 17, "b", "c");
    assertSame (Proxy.Type.SOCKS, a.getProxyType ());
    assertEquals ("a", a.getProxyHost ());
    assertEquals (17, a.getProxyPort ());
    assertEquals ("b", a.getProxyUserName ());
    assertTrue (a.hasProxyUserName ());
    assertEquals ("c", a.getProxyPassword ());
    assertTrue (a.hasProxyPassword ());

    final Proxy p = a.getAsProxy (false);
    assertNotNull (p);
    assertEquals (Proxy.Type.SOCKS, p.type ());
    assertTrue (p.address () instanceof InetSocketAddress);
    assertEquals ("a", ((InetSocketAddress) p.address ()).getHostString ());
    assertEquals (17, ((InetSocketAddress) p.address ()).getPort ());
    assertTrue (a.hasSocketAddress (p.address ()));

    final PasswordAuthentication aPA = a.getAsPasswordAuthentication ();
    assertNotNull (aPA);
    assertEquals ("b", aPA.getUserName ());
    assertArrayEquals ("c".toCharArray (), aPA.getPassword ());
  }

  @Test
  public void testNoPassword ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, "b", null);
    assertSame (Proxy.Type.HTTP, a.getProxyType ());
    assertEquals ("a", a.getProxyHost ());
    assertEquals (17, a.getProxyPort ());
    assertEquals ("b", a.getProxyUserName ());
    assertTrue (a.hasProxyUserName ());
    assertEquals (null, a.getProxyPassword ());
    assertFalse (a.hasProxyPassword ());

    final Proxy p = a.getAsProxy (false);
    assertNotNull (p);
    assertEquals (Proxy.Type.HTTP, p.type ());
    assertTrue (p.address () instanceof InetSocketAddress);
    assertEquals ("a", ((InetSocketAddress) p.address ()).getHostString ());
    assertEquals (17, ((InetSocketAddress) p.address ()).getPort ());
    assertTrue (a.hasSocketAddress (p.address ()));

    final PasswordAuthentication aPA = a.getAsPasswordAuthentication ();
    assertNotNull (aPA);
    assertEquals ("b", aPA.getUserName ());
    assertArrayEquals (ArrayHelper.EMPTY_CHAR_ARRAY, aPA.getPassword ());
  }

  @Test
  public void testNoUserName ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, null, "c");
    assertSame (Proxy.Type.HTTP, a.getProxyType ());
    assertEquals ("a", a.getProxyHost ());
    assertEquals (17, a.getProxyPort ());
    assertNull (a.getProxyUserName ());
    assertFalse (a.hasProxyUserName ());
    assertEquals ("c", a.getProxyPassword ());
    assertTrue (a.hasProxyPassword ());

    final Proxy p = a.getAsProxy (false);
    assertNotNull (p);
    assertEquals (Proxy.Type.HTTP, p.type ());
    assertTrue (p.address () instanceof InetSocketAddress);
    assertEquals ("a", ((InetSocketAddress) p.address ()).getHostString ());
    assertEquals (17, ((InetSocketAddress) p.address ()).getPort ());
    assertTrue (a.hasSocketAddress (p.address ()));

    final PasswordAuthentication aPA = a.getAsPasswordAuthentication ();
    assertNull (aPA);
  }

  @Test
  public void testNoEmptyName ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, "", "c");
    assertSame (Proxy.Type.HTTP, a.getProxyType ());
    assertEquals ("a", a.getProxyHost ());
    assertEquals (17, a.getProxyPort ());
    assertEquals ("", a.getProxyUserName ());
    assertFalse (a.hasProxyUserName ());
    assertEquals ("c", a.getProxyPassword ());
    assertTrue (a.hasProxyPassword ());

    final Proxy p = a.getAsProxy (false);
    assertNotNull (p);
    assertEquals (Proxy.Type.HTTP, p.type ());
    assertTrue (p.address () instanceof InetSocketAddress);
    assertEquals ("a", ((InetSocketAddress) p.address ()).getHostString ());
    assertEquals (17, ((InetSocketAddress) p.address ()).getPort ());
    assertTrue (a.hasSocketAddress (p.address ()));

    final PasswordAuthentication aPA = a.getAsPasswordAuthentication ();
    assertNull (aPA);
  }

  @Test
  public void testNoCredentials ()
  {
    final ProxySettings a = new ProxySettings (Proxy.Type.HTTP, "a", 17, null, null);
    assertSame (Proxy.Type.HTTP, a.getProxyType ());
    assertEquals ("a", a.getProxyHost ());
    assertEquals (17, a.getProxyPort ());
    assertNull (a.getProxyUserName ());
    assertFalse (a.hasProxyUserName ());
    assertNull (a.getProxyPassword ());
    assertFalse (a.hasProxyPassword ());

    final Proxy p = a.getAsProxy (false);
    assertNotNull (p);
    assertEquals (Proxy.Type.HTTP, p.type ());
    assertTrue (p.address () instanceof InetSocketAddress);
    assertEquals ("a", ((InetSocketAddress) p.address ()).getHostString ());
    assertEquals (17, ((InetSocketAddress) p.address ()).getPort ());
    assertTrue (a.hasSocketAddress (p.address ()));

    final PasswordAuthentication aPA = a.getAsPasswordAuthentication ();
    assertNull (aPA);
  }
}
