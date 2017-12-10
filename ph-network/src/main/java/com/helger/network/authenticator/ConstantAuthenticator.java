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
package com.helger.network.authenticator;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;

/**
 * A simple implementation of the abstract {@link Authenticator} class using a
 * static user name and password for all requested sites.<br>
 * Call to enable:
 * <code>Authenticator.setDefault (new ConstantAuthenticator (user, pw));</code>
 *
 * @author Philip Helger
 */
public class ConstantAuthenticator extends Authenticator
{
  public static final String DOMAIN_SEPARATOR = "\\";

  private final String m_sUserName;
  private final String m_sPassword;

  public ConstantAuthenticator (@Nullable final String sDomain,
                                @Nonnull final String sUserName,
                                @Nonnull final String sPassword)
  {
    this (StringHelper.getConcatenatedOnDemand (sDomain, DOMAIN_SEPARATOR, sUserName), sPassword);
  }

  public ConstantAuthenticator (@Nonnull final String sUserName, @Nonnull final String sPassword)
  {
    m_sUserName = ValueEnforcer.notNull (sUserName, "UserName");
    m_sPassword = ValueEnforcer.notNull (sPassword, "Password");
  }

  /**
   * @return The provided user name, including an eventually present domain.
   *         Never <code>null</code>.
   */
  @Nonnull
  public String getUserName ()
  {
    return m_sUserName;
  }

  /**
   * @return The provided password. Never <code>null</code>.
   */
  @Nonnull
  public String getPassword ()
  {
    return m_sPassword;
  }

  @Override
  public PasswordAuthentication getPasswordAuthentication ()
  {
    return new PasswordAuthentication (m_sUserName, m_sPassword.toCharArray ());
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("UserName", m_sUserName).appendPassword ("Password").getToString ();
  }
}
