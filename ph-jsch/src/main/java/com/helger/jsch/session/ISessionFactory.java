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

import com.helger.annotation.Nonempty;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * An interface for creating {@link Session} objects from a common
 * configuration. Also supports creation of other SessionFactory instances that
 * are initialized from the same configuration and can be modified as necessary.
 */
public interface ISessionFactory extends ISessionProvider
{
  int SSH_PORT = 22;

  /**
   * @return the hostname that sessions built by this factory will connect to.
   */
  String getHostname ();

  /**
   * @return the port that sessions built by this factory will connect to.
   */
  int getPort ();

  /**
   * @return the proxy that sessions built by this factory will connect through,
   *         if any. If none was configured, <code>null</code> will be returned.
   */
  @Nullable
  Proxy getProxy ();

  /**
   * @return the username that sessions built by this factory will connect with.
   */
  String getUsername ();

  /**
   * @return the userInfo that sessions built by this factory will connect with.
   */
  UserInfo getUserInfo ();

  /**
   * Returns a builder for another session factory pre-initialized with the
   * configuration for this session factory.
   *
   * @return A builder for a session factory
   */
  @Nonnull
  AbstractSessionFactoryBuilder newSessionFactoryBuilder ();

  @Nonnull
  @Nonempty
  default String getAsString ()
  {
    final Proxy aProxy = getProxy ();
    return (aProxy == null ? "" : aProxy.toString () + " ") +
           "ssh://" +
           getUsername () +
           "@" +
           getHostname () +
           ":" +
           getPort ();
  }
}
