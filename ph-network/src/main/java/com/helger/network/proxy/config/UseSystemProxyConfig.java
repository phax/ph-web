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
package com.helger.network.proxy.config;

import java.net.Proxy;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.string.StringParser;
import com.helger.base.system.SystemProperties;
import com.helger.base.tostring.ToStringGenerator;

import jakarta.annotation.Nullable;

/**
 * Proxy configuration that uses the system default proxy settings.
 *
 * @author Philip Helger
 */
@Immutable
public class UseSystemProxyConfig implements IProxyConfig
{
  public static final String SYSPROP_JAVA_NET_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";

  public UseSystemProxyConfig ()
  {}

  public boolean isUseSystemProxies ()
  {
    return StringParser.parseBool (SystemProperties.getPropertyValueOrNull (SYSPROP_JAVA_NET_USE_SYSTEM_PROXIES),
                                   false);
  }

  public void activateGlobally ()
  {
    activateGloballyStatic ();
  }

  public static void activateGloballyStatic ()
  {
    // Deactivate other proxy configurations
    HttpProxyConfig.deactivateGlobally ();
    SocksProxyConfig.deactivateGlobally ();

    SystemProperties.setPropertyValue (SYSPROP_JAVA_NET_USE_SYSTEM_PROXIES, Boolean.TRUE.toString ());
  }

  public static void deactivateGlobally ()
  {
    SystemProperties.removePropertyValue (SYSPROP_JAVA_NET_USE_SYSTEM_PROXIES);
  }

  @Nullable
  public Proxy getAsProxy ()
  {
    return null;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    return true;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).getToString ();
  }
}
