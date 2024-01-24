/*
 * Copyright (C) 2016-2024 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * An interface for creating {@link Session} objects. This is a base interface
 * for {@link ISessionFactory}.
 *
 * @since 9.7.2
 */
public interface ISessionProvider
{
  /**
   * @return a new session using the configured properties.
   * @throws JSchException
   *         If <code>username</code> or <code>hostname</code> are invalid
   * @see com.jcraft.jsch.JSch#getSession(String, String, int)
   */
  @Nonnull
  Session createSession () throws JSchException;
}
