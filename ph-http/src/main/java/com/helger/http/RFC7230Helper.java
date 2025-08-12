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
 * @since 10.5.1
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
   * Check if the provided char is a valid token char.
   *
   * @param c
   *        character to check
   * @return <code>true</code> if it is a valid token, <code>false</code> if not.
   */
  public static boolean isValidTokenChar (final char c)
  {
    return TOKEN_CHARS.get (c);
  }

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

    return isValidToken (s.toCharArray ());
  }

  /**
   * Check if the provided char array is a valid token.
   *
   * @param a
   *        Character array to check
   * @return <code>true</code> if it is a valid token, <code>false</code> if not.
   */
  public static boolean isValidToken (@Nullable final char [] a)
  {
    if (a == null || a.length == 0)
      return false;

    for (final char c : a)
      if (!isValidTokenChar (c))
        return false;
    return true;
  }
}
