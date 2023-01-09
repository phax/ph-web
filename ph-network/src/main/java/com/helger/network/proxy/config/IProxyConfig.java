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

import java.net.Proxy;

import javax.annotation.Nullable;

import com.helger.commons.annotation.MustImplementEqualsAndHashcode;

/**
 * Base interface for the proxy configuration.
 *
 * @author Philip Helger
 */
@MustImplementEqualsAndHashcode
public interface IProxyConfig
{
  /**
   * Activate this proxy configuration.
   */
  void activateGlobally ();

  /**
   * @return This proxy configuration as standard {@link java.net.Proxy} object.
   *         May be <code>null</code> if no adequate object can be created.
   */
  @Nullable
  Proxy getAsProxy ();
}
