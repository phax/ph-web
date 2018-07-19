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
    assertArrayEquals (new char [0], aPA.getPassword ());
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
