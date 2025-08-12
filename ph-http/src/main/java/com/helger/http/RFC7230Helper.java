package com.helger.http;

import java.util.BitSet;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.string.StringHelper;
import com.helger.commons.text.util.ABNF;

/**
 * Helper class for RFC 7230
 *
 * @author Philip Helger
 */
@Immutable
public final class RFC7230Helper
{
  private static final BitSet TOKEN_CHARS = new BitSet (256);

  static
  {
    for (int i = ABNF.CHECK_RANGE_MIN_INCL; i <= ABNF.CHECK_RANGE_MAX_INCL; ++i)
      if (ABNF.isDigit (i) || ABNF.isAlpha (i))
        TOKEN_CHARS.set (i);
    TOKEN_CHARS.set ('!');
    TOKEN_CHARS.set ('#');
    TOKEN_CHARS.set ('$');
    TOKEN_CHARS.set ('%');
    TOKEN_CHARS.set ('&');
    TOKEN_CHARS.set ('\'');
    TOKEN_CHARS.set ('*');
    TOKEN_CHARS.set ('+');
    TOKEN_CHARS.set ('-');
    TOKEN_CHARS.set ('.');
    TOKEN_CHARS.set ('^');
    TOKEN_CHARS.set ('_');
    TOKEN_CHARS.set ('`');
    TOKEN_CHARS.set ('|');
    TOKEN_CHARS.set ('~');
  }

  private RFC7230Helper ()
  {}

  /**
   * Check if the provided String is a valid token.
   *
   * @param s
   *        String to check
   * @return <code>true</code> if it is a valid token, <code>false</code> if not.
   */
  public static boolean isValidToken (@Nullable final String s)
  {
    if (StringHelper.hasNoText (s))
      return false;

    for (final char c : s.toCharArray ())
      if (!TOKEN_CHARS.get (c))
        return false;
    return true;
  }
}
