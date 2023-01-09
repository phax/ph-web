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
package com.helger.network.port;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.string.StringHelper;

/**
 * Default port mapper. By default it contains the following mappings:
 * <ul>
 * <li>ftp - 21</li>
 * <li>http - 80</li>
 * <li>https - 443</li>
 * </ul>
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class SchemeDefaultPortMapper
{
  /** The scheme for HTTP */
  public static final String SCHEME_FTP = "ftp";
  /** The scheme for HTTP */
  public static final String SCHEME_HTTP = "http";
  /** The scheme for HTTPS */
  public static final String SCHEME_HTTPS = "https";

  @PresentForCodeCoverage
  private static final SchemeDefaultPortMapper INSTANCE = new SchemeDefaultPortMapper ();

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static final ICommonsMap <String, Integer> MAP = new CommonsHashMap <> ();

  static
  {
    registerDefaultPort (SCHEME_FTP, 21);
    registerDefaultPort (SCHEME_HTTP, 80);
    registerDefaultPort (SCHEME_HTTPS, 443);
  }

  private SchemeDefaultPortMapper ()
  {}

  public static void registerDefaultPort (@Nonnull @Nonempty final String sSchemeName, @Nonnegative final int nPort)
  {
    ValueEnforcer.notEmpty (sSchemeName, "SchemeName");
    ValueEnforcer.isTrue (NetworkPortHelper.isValidPort (nPort), "Invalid port provided");

    RW_LOCK.writeLocked ( () -> {
      if (MAP.containsKey (sSchemeName))
        throw new IllegalArgumentException ("A default port for scheme '" + sSchemeName + "' is already registered!");
      MAP.put (sSchemeName, Integer.valueOf (nPort));
    });
  }

  public static int getDefaultPort (@Nullable final String sSchemeName, final int nDefault)
  {
    if (StringHelper.hasText (sSchemeName))
    {
      final Integer aDefaultPort = RW_LOCK.readLockedGet ( () -> MAP.get (sSchemeName));
      if (aDefaultPort != null)
        return aDefaultPort.intValue ();
    }
    return nDefault;
  }

  public static int getDefaultPortOrInvalid (@Nullable final String sSchemeName)
  {
    return getDefaultPort (sSchemeName, CNetworkPort.INVALID_PORT_NUMBER);
  }

  public static int getDefaultPortOrThrow (@Nullable final String sSchemeName)
  {
    final int nPort = getDefaultPortOrInvalid (sSchemeName);
    if (nPort == CNetworkPort.INVALID_PORT_NUMBER)
      throw new IllegalArgumentException ("No default port present for scheme '" + sSchemeName + "'");
    return nPort;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsMap <String, Integer> getAll ()
  {
    return RW_LOCK.readLockedGet (MAP::getClone);
  }
}
