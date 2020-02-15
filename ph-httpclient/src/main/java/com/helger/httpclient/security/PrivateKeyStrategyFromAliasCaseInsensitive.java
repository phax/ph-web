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
  public String chooseAlias (@Nonnull final Map <String, PrivateKeyDetails> aAliases, @Nullable final Socket aSocket)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("chooseAlias(" + aAliases + ", " + aSocket + ")");

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
            if (LOGGER.isWarnEnabled ())
              LOGGER.warn ("Chose the keystore alias '" +
                           sCurAlias +
                           "' but the configured alias '" +
                           m_sExpectedAlias +
                           "' has a different casing. It is recommended to adopt the expected alias accordingly.");
        }
        return sCurAlias;
      }
    }
    if (LOGGER.isWarnEnabled ())
      LOGGER.warn ("Found no certificate alias matching '" +
                   m_sExpectedAlias +
                   "' in the provided aliases " +
                   aAliases.keySet ());
    return null;
  }

}
