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
package com.helger.httpclient.security;

import java.util.Map;

import javax.net.ssl.SSLParameters;

import org.apache.hc.core5.ssl.PrivateKeyDetails;
import org.apache.hc.core5.ssl.PrivateKeyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A private key strategy that select the key details from the provided alias.
 * The matching of the alias is case insensitive (so that the expected alias
 * <code>ABC</code> matches the keystore alias <code>Abc</code>).
 *
 * @author Philip Helger
 * @since 9.1.9
 */
public class PrivateKeyStrategyFromAliasCaseInsensitive implements PrivateKeyStrategy
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PrivateKeyStrategyFromAliasCaseInsensitive.class);

  private final String m_sExpectedAlias;
  private boolean m_bWarnOnCaseDifference = false;

  public PrivateKeyStrategyFromAliasCaseInsensitive (@Nonnull final String sExpectedAlias)
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

  public final boolean isWarnOnCaseDifference ()
  {
    return m_bWarnOnCaseDifference;
  }

  public final void setWarnOnCaseDifference (final boolean bWarnOnCaseDifference)
  {
    m_bWarnOnCaseDifference = bWarnOnCaseDifference;
  }

  @Nullable
  public String chooseAlias (@Nonnull final Map <String, PrivateKeyDetails> aAliases,
                             @Nullable final SSLParameters aSSLParameters)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("chooseAlias(" + aAliases + ", " + aSSLParameters + ")");
    for (final String sCurAlias : aAliases.keySet ())
    {
      // Case insensitive alias handling
      if (sCurAlias.equalsIgnoreCase (m_sExpectedAlias))
      {
        if (sCurAlias.equals (m_sExpectedAlias))
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("  Chose alias '" + sCurAlias + "'");
        }
        else
        {
          // Case insensitive match
          if (m_bWarnOnCaseDifference)
            LOGGER.warn ("Chose the keystore alias '" +
                         sCurAlias +
                         "' but the configured alias '" +
                         m_sExpectedAlias +
                         "' has a different casing. It is recommended to adopt the expected alias accordingly.");
        }
        return sCurAlias;
      }
    }
    LOGGER.warn ("Found no certificate alias matching '" +
                 m_sExpectedAlias +
                 "' in the provided aliases " +
                 aAliases.keySet ());
    return null;
  }
}
