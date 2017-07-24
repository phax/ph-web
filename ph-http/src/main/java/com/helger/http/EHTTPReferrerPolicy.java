package com.helger.http;

import javax.annotation.Nonnull;

/**
 * HTTP response header "Referrer-Policy" values. See
 * https://scotthelme.co.uk/a-new-security-header-referrer-policy/
 *
 * @author Philip Helger
 */
public enum EHTTPReferrerPolicy
{
  NONE (""),
  NO_REFERRER ("no-referrer"),
  NO_REFERRER_WHEN_DOWNGRADE ("no-referrer-when-downgrade"),
  // Not supported in Chrome 59
  SAME_ORIGIN ("same-origin"),
  ORIGIN ("origin"),
  // Not supported in Chrome 59
  STRICT_ORIGIN ("strict-origin"),
  ORIGIN_WHEN_CROSS_ORIGIN ("origin-when-cross-origin"),
  // Not supported in Chrome 59
  STRICT_ORIGIN_WHEN_CROSS_ORIGIN ("strict-origin-when-cross-origin"),
  UNSAFE_URL ("unsafe-url");

  private String m_sValue;

  private EHTTPReferrerPolicy (@Nonnull final String sValue)
  {
    m_sValue = sValue;
  }

  @Nonnull
  public String getValue ()
  {
    return m_sValue;
  }
}
