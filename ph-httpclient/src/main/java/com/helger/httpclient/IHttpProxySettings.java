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
package com.helger.httpclient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.core5.http.HttpHost;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.ICommonsSet;

/**
 * Read-only interface for {@link HttpProxySettings}
 *
 * @author Philip Helger
 * @since 10.5.0
 */
public interface IHttpProxySettings
{
  /**
   * @return <code>true</code> if a proxy host is defined, <code>false</code> otherwise
   */
  default boolean hasProxyHost ()
  {
    return getProxyHost () != null;
  }

  /**
   * @return The proxy host to be used. May be <code>null</code>.
   */
  @Nullable
  HttpHost getProxyHost ();

  /**
   * @return <code>true</code> if proxy credentials are present, <code>false</code> if not.
   */
  default boolean hasProxyCredentials ()
  {
    return getProxyCredentials () != null;
  }

  /**
   * @return The proxy server credentials to be used. May be <code>null</code>.
   */
  @Nullable
  Credentials getProxyCredentials ();

  /**
   * @return The mutable original set of all host names and IP addresses for which no proxy should
   *         be used. Never <code>null</code> and mutable.
   */
  @Nonnull
  @ReturnsMutableObject
  ICommonsSet <String> nonProxyHosts ();

  /**
   * @return A mutable copy of the set of all host names and IP addresses for which no proxy should
   *         be used. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  default ICommonsSet <String> getAllNonProxyHosts ()
  {
    return nonProxyHosts ().getClone ();
  }
}
