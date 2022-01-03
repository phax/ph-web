/*
 * Copyright (C) 2016-2022 Philip Helger (www.helger.com)
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
package com.helger.httpclient.security;

import java.net.Socket;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;

/**
 * A private key strategy that select the key details from the provided alias.
 * The matching of the alias is case sensitive.
 *
 * @author Philip Helger
 * @since 9.1.9
 */
public class PrivateKeyStrategyFromAliasCaseSensitive implements PrivateKeyStrategy
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PrivateKeyStrategyFromAliasCaseSensitive.class);

  private final String m_sExpectedAlias;

  public PrivateKeyStrategyFromAliasCaseSensitive (@Nonnull final String sExpectedAlias)
  {
    ValueEnforcer.notNull (sExpectedAlias, "ExpectedAlias");
    m_sExpectedAlias = sExpectedAlias;
  }

  /**
   * @return The name of the expected alias as provided in the constructor.
   *         Never <code>null</code>.
   */
  @Nonnull
  public final String getExpectedAlias ()
  {
    return m_sExpectedAlias;
  }

  @Nullable
  public String chooseAlias (@Nonnull final Map <String, PrivateKeyDetails> aAliases, @Nullable final Socket aSocket)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("chooseAlias(" + aAliases + ", " + aSocket + ")");

    for (final String sCurAlias : aAliases.keySet ())
    {
      // Case sensitive alias handling
      if (sCurAlias.equals (m_sExpectedAlias))
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("  Chose alias '" + sCurAlias + "'");
        return sCurAlias;
      }
    }
    if (LOGGER.isWarnEnabled ())
      LOGGER.warn ("Found no certificate alias matching '" + m_sExpectedAlias + "' in the provided aliases " + aAliases.keySet ());
    return null;
  }
}
