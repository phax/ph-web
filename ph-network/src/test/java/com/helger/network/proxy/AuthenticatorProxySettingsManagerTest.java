/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
  public void testNoConfigurationSimpleAPI ()
  {
    final PasswordAuthentication aPA = AuthenticatorProxySettingsManager.requestProxyPasswordAuthentication ("orf.at",
                                                                                                             80,
                                                                                                             "http");
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
  public void testProxySimpleAPI ()
  {
    ProxySettingsManager.registerProvider ( (sProtocol,
                                             sHostName,
                                             nPort) -> new CommonsArrayList <> (new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv",
                                                                                                   8080,
                                                                                                   sHostName.contains ("orf.at") ? "user"
                                                                                                                                 : null,
                                                                                                   "pw")));

    PasswordAuthentication aPA = AuthenticatorProxySettingsManager.requestProxyPasswordAuthentication ("orf.at",
                                                                                                       80,
                                                                                                       "http");
    assertNotNull (aPA);
    assertEquals ("user", aPA.getUserName ());
    assertEquals ("pw", new String (aPA.getPassword ()));

    aPA = AuthenticatorProxySettingsManager.requestProxyPasswordAuthentication ("helger.com", 80, "http");
    assertNull (aPA);
  }

  @Test
  public void testProxyMultipleNone ()
  {
    ProxySettingsManager.registerProvider ( (sProtocol,
                                             sHostName,
                                             nPort) -> new CommonsArrayList <> (new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv",
                                                                                                   8080),
                                                                                new ProxySettings (Proxy.Type.HTTP,
                                                                                                   "http://proxysrv2",
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
