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
package com.helger.network.proxy.config;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.ArrayHelper;

/**
 * A special authenticator for HTTP/HTTPS/FTPS proxy connections.
 *
 * @author Philip Helger
 */
public class HttpProxyAuthenticator extends Authenticator
{
  private final EHttpProxyType m_eProxyType;

  public HttpProxyAuthenticator (@Nonnull final EHttpProxyType eProxyType)
  {
    m_eProxyType = ValueEnforcer.notNull (eProxyType, "ProxyType");
  }

  @Nonnull
  public final EHttpProxyType getProxyType ()
  {
    return m_eProxyType;
  }

  @Override
  @Nullable
  public PasswordAuthentication getPasswordAuthentication ()
  {
    if (getRequestorType () == RequestorType.PROXY)
    {
      // Get current proxy host
      final String sProxyHost = m_eProxyType.getProxyHost ();
      if (getRequestingHost ().equalsIgnoreCase (sProxyHost))
      {
        // Get current proxy port
        final int nProxyPort = m_eProxyType.getProxyPort ();
        if (nProxyPort == getRequestingPort ())
        {
          // Seems to be OK.
          final String sProxyUser = m_eProxyType.getProxyUserName ();
          final String sProxyPassword = m_eProxyType.getProxyPassword ();
          return new PasswordAuthentication (sProxyUser,
                                             sProxyPassword == null ? ArrayHelper.EMPTY_CHAR_ARRAY : sProxyPassword.toCharArray ());
        }
      }
    }
    return null;
  }
}
