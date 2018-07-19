package com.helger.network.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.junit.Before;
import org.junit.Test;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.network.proxy.settings.ProxySettings;
import com.helger.network.proxy.settings.ProxySettingsManager;

/**
 * Test class for class {@link AuthenticatorProxySettingsManager}.
 *
 * @author Philip Helger
 */
public final class AuthenticatorProxySettingsManagerTest
{
  @Before
  public void before ()
  {
    ProxySettingsManager.removeAllProviders ();
    AuthenticatorProxySettingsManager.setAsDefault ();
  }

  @Test
  public void testNoConfiguration ()
  {
    final PasswordAuthentication aPA = Authenticator.requestPasswordAuthentication ("orf.at",
                                                                                    null,
                                                                                    80,
                                                                                    "http",
                                                                                    "bla",
                                                                                    null,
                                                                                    null,
                                                                                    RequestorType.PROXY);
    assertNull (aPA);
  }

  @Test
  public void testProxyNoUser ()
  {
    ProxySettingsManager.registerProvider ( (sProtocol,
                                             sHostName,
                                             nPort) -> new CommonsArrayList <> (new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv",
                                                                                                   8080)));

    final PasswordAuthentication aPA = Authenticator.requestPasswordAuthentication ("orf.at",
                                                                                    null,
                                                                                    80,
                                                                                    "http",
                                                                                    "bla",
                                                                                    null,
                                                                                    null,
                                                                                    RequestorType.PROXY);
    assertNull (aPA);
  }

  @Test
  public void testProxy ()
  {
    ProxySettingsManager.registerProvider ( (sProtocol,
                                             sHostName,
                                             nPort) -> new CommonsArrayList <> (new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv",
                                                                                                   8080,
                                                                                                   "user",
                                                                                                   "pw")));

    final PasswordAuthentication aPA = Authenticator.requestPasswordAuthentication ("orf.at",
                                                                                    null,
                                                                                    80,
                                                                                    "http",
                                                                                    "bla",
                                                                                    null,
                                                                                    null,
                                                                                    RequestorType.PROXY);
    assertNotNull (aPA);
    assertEquals ("user", aPA.getUserName ());
    assertEquals ("pw", new String (aPA.getPassword ()));
  }

  @Test
  public void testProxyMultipleMixed ()
  {
    ProxySettingsManager.registerProvider ( (sProtocol,
                                             sHostName,
                                             nPort) -> new CommonsArrayList <> (new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv",
                                                                                                   8080),
                                                                                new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv2",
                                                                                                   8080,
                                                                                                   "user",
                                                                                                   "pw")));

    final PasswordAuthentication aPA = Authenticator.requestPasswordAuthentication ("orf.at",
                                                                                    null,
                                                                                    80,
                                                                                    "http",
                                                                                    "bla",
                                                                                    null,
                                                                                    null,
                                                                                    RequestorType.PROXY);
    assertNotNull (aPA);
    assertEquals ("user", aPA.getUserName ());
    assertEquals ("pw", new String (aPA.getPassword ()));
  }

  @Test
  public void testProxyMultipleBoth ()
  {
    ProxySettingsManager.registerProvider ( (sProtocol,
                                             sHostName,
                                             nPort) -> new CommonsArrayList <> (new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv",
                                                                                                   8080,
                                                                                                   "user1",
                                                                                                   "pw1"),
                                                                                new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv2",
                                                                                                   8080,
                                                                                                   "user2",
                                                                                                   "pw2")));

    final PasswordAuthentication aPA = Authenticator.requestPasswordAuthentication ("orf.at",
                                                                                    null,
                                                                                    80,
                                                                                    "http",
                                                                                    "bla",
                                                                                    null,
                                                                                    null,
                                                                                    RequestorType.PROXY);
    assertNotNull (aPA);
    assertEquals ("user1", aPA.getUserName ());
    assertEquals ("pw1", new String (aPA.getPassword ()));
  }
}
