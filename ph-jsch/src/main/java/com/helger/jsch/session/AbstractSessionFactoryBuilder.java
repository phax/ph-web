/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.jsch.session;

import com.helger.collection.commons.ICommonsMap;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.UserInfo;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class AbstractSessionFactoryBuilder
{
  protected ICommonsMap <String, String> m_aConfig;
  protected String m_sHostname;
  protected JSch m_aJsch;
  protected int m_nPort;
  protected Proxy m_aProxy;
  protected String m_sUsername;
  protected UserInfo m_aUserInfo;

  protected AbstractSessionFactoryBuilder (final JSch jsch,
                                           final String username,
                                           final String hostname,
                                           final int port,
                                           final Proxy proxy,
                                           final ICommonsMap <String, String> config,
                                           final UserInfo userInfo)
  {
    m_aJsch = jsch;
    m_sUsername = username;
    m_sHostname = hostname;
    m_nPort = port;
    m_aProxy = proxy;
    m_aConfig = config;
    m_aUserInfo = userInfo;
  }

  /**
   * Replaces the current config with <code>config</code>
   *
   * @param aConfig
   *        The new config
   * @return This builder
   * @see com.helger.jsch.session.DefaultSessionFactory#setConfig(ICommonsMap)
   */
  @Nonnull
  public AbstractSessionFactoryBuilder setConfig (@Nullable final ICommonsMap <String, String> aConfig)
  {
    m_aConfig = aConfig;
    return this;
  }

  /**
   * Replaces the current hostname with <code>hostname</code>
   *
   * @param sHostname
   *        The new hostname
   * @return This builder
   */
  @Nonnull
  public AbstractSessionFactoryBuilder setHostname (@Nullable final String sHostname)
  {
    m_sHostname = sHostname;
    return this;
  }

  /**
   * Replaces the current port with <code>port</code>
   *
   * @param nPort
   *        The new port
   * @return This builder
   */
  @Nonnull
  public AbstractSessionFactoryBuilder setPort (final int nPort)
  {
    m_nPort = nPort;
    return this;
  }

  /**
   * Replaces the current proxy with <code>proxy</code>
   *
   * @param aProxy
   *        The new proxy
   * @return This builder
   * @see com.helger.jsch.session.DefaultSessionFactory#setProxy(Proxy)
   */
  @Nonnull
  public AbstractSessionFactoryBuilder setProxy (@Nullable final Proxy aProxy)
  {
    m_aProxy = aProxy;
    return this;
  }

  /**
   * Replaces the current username with <code>username</code>
   *
   * @param sUsername
   *        The new username
   * @return This builder
   */
  @Nonnull
  public AbstractSessionFactoryBuilder setUsername (@Nullable final String sUsername)
  {
    m_sUsername = sUsername;
    return this;
  }

  /**
   * Replaces the current userInfo with <code>userInfo</code>
   *
   * @param aUserInfo
   *        The new userInfo
   * @return This builder
   */
  @Nonnull
  public AbstractSessionFactoryBuilder setUserInfo (@Nullable final UserInfo aUserInfo)
  {
    m_aUserInfo = aUserInfo;
    return this;
  }

  /**
   * Builds and returns a the new <code>SessionFactory</code> instance.
   *
   * @return The built <code>SessionFactory</code>
   */
  @Nonnull
  public abstract ISessionFactory build ();
}
