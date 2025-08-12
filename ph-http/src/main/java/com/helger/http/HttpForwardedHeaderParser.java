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
package com.helger.http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.StringHelper;

/**
 * Parser for RFC 7239 compliant "Forwarded" header values. This class can parse a forwarded-element
 * string into a {@link HttpForwardedHeader}. The syntax according to RFC 7239 is:
 *
 * <pre>
 * forwarded-element = [ forwarded-pair ] *( ";" [ forwarded-pair ] )
 * forwarded-pair    = token "=" value
 * value             = token / quoted-string
 * </pre>
 *
 * @author Philip Helger
 * @since 10.5.1
 */
@Immutable
public final class HttpForwardedHeaderParser
{
  /**
   * Internal helper class for parsing context.
   */
  private static final class ParseContext
  {
    private final char [] m_aInput;
    private int m_nPos = 0;

    ParseContext (@Nonnull final String sInput)
    {
      m_aInput = sInput.toCharArray ();
    }

    boolean hasMore ()
    {
      return m_nPos < m_aInput.length;
    }

    char getCurrentChar ()
    {
      return m_aInput[m_nPos];
    }

    void advance ()
    {
      m_nPos++;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger (HttpForwardedHeaderParser.class);

  private HttpForwardedHeaderParser ()
  {}

  /**
   * Skip whitespace characters.
   *
   * @param aContext
   *        The parsing context. May not be <code>null</code>.
   */
  private static void _skipWhitespace (@Nonnull final ParseContext aContext)
  {
    while (aContext.hasMore ())
    {
      final char c = aContext.getCurrentChar ();
      if (!RFC7230Helper.isWhitespace (c))
        break;
      aContext.advance ();
    }
  }

  /**
   * Parse a token according to RFC 7230.
   *
   * @param aContext
   *        The parsing context. May not be <code>null</code>.
   * @return The parsed token or <code>null</code> if parsing failed.
   */
  @Nullable
  private static String _parseToken (@Nonnull final ParseContext aContext)
  {
    final int nStart = aContext.m_nPos;

    while (aContext.hasMore ())
    {
      final char c = aContext.getCurrentChar ();
      if (!RFC7230Helper.isValidTokenChar (c))
        break;
      aContext.advance ();
    }

    if (aContext.m_nPos == nStart)
    {
      // No token characters found
      return null;
    }

    final String sToken = new String (aContext.m_aInput, nStart, aContext.m_nPos - nStart);

    // Validate the complete token
    if (!RFC7230Helper.isValidToken (sToken))
    {
      LOGGER.warn ("Found internal inconsistency parsing '" + sToken + "' as an RFC 7230 token");
      return null;
    }

    return sToken;
  }

  /**
   * Expect a specific character at the current position.
   *
   * @param aContext
   *        The parsing context. May not be <code>null</code>.
   * @param cExpected
   *        The expected character.
   * @return <code>true</code> if the character was found and consumed, <code>false</code>
   *         otherwise.
   */
  private static boolean _expectChar (@Nonnull final ParseContext aContext, final char cExpected)
  {
    if (!aContext.hasMore () || aContext.getCurrentChar () != cExpected)
      return false;
    aContext.advance ();
    return true;
  }

  /**
   * Check if a character is valid inside a quoted string.
   *
   * @param c
   *        The character to check.
   * @return <code>true</code> if valid, <code>false</code> otherwise.
   */
  private static boolean _isValidQuotedStringChar (final char c)
  {
    // Allow most printable ASCII characters except quote and backslash
    // Control characters (0-31 and 127) are not allowed
    return c >= 32 && c <= 126 && c != '"' && c != '\\';
  }

  /**
   * Parse a quoted-string according to RFC 7230.
   *
   * @param aContext
   *        The parsing context. May not be <code>null</code>.
   * @return The parsed quoted string content (without quotes) or <code>null</code> if parsing
   *         failed.
   */
  @Nullable
  private static String _parseQuotedString (@Nonnull final ParseContext aContext)
  {
    if (!aContext.hasMore () || aContext.getCurrentChar () != '"')
      return null;

    // Skip opening quote
    aContext.advance ();

    final StringBuilder aSB = new StringBuilder ();
    while (aContext.hasMore ())
    {
      final char c = aContext.getCurrentChar ();
      if (c == '"')
      {
        // End of quoted string
        aContext.advance ();
        return aSB.toString ();
      }
      else
        if (c == '\\')
        {
          // Escape sequence
          aContext.advance ();
          if (!aContext.hasMore ())
          {
            // Incomplete escape sequence
            LOGGER.warn ("Found incomplete escape sequence in Forwarded header value parsing");
            return null;
          }

          final char cEscaped = aContext.getCurrentChar ();
          // According to RFC 7230, only certain characters can be escaped
          if (cEscaped == '"' || cEscaped == '\\')
          {
            aSB.append (cEscaped);
            aContext.advance ();
          }
          else
          {
            // Invalid escape sequence
            return null;
          }
        }
        else
          if (_isValidQuotedStringChar (c))
          {
            aSB.append (c);
            aContext.advance ();
          }
          else
          {
            // Invalid character in quoted string
            LOGGER.warn ("Found invalid character in quoted string on Forwarded header value parsing");
            return null;
          }
    }

    // Unterminated quoted string
    return null;
  }

  /**
   * Parse a value (either a token or a quoted-string).
   *
   * @param aContext
   *        The parsing context. May not be <code>null</code>.
   * @return The parsed value or <code>null</code> if parsing failed.
   */
  @Nullable
  private static String _parseValue (@Nonnull final ParseContext aContext)
  {
    if (!aContext.hasMore ())
      return null;

    final char c = aContext.getCurrentChar ();
    if (c == '"')
    {
      // Parse quoted-string
      return _parseQuotedString (aContext);
    }
    // Parse token
    return _parseToken (aContext);
  }

  /**
   * Parse an optional forwarded-pair.
   *
   * @param aContext
   *        The parsing context. May not be <code>null</code>.
   * @param aResult
   *        The result list to add the pair to. May not be <code>null</code>.
   * @return <code>true</code> if parsing succeeded, <code>false</code> otherwise.
   */
  private static boolean _parseOptionalPair (@Nonnull final ParseContext aContext, @Nonnull final HttpForwardedHeader aResult)
  {
    _skipWhitespace (aContext);

    if (!aContext.hasMore ())
    {
      // Empty pair is allowed
      return true;
    }

    // Parse token
    final String sToken = _parseToken (aContext);
    if (sToken == null)
      return false;

    _skipWhitespace (aContext);

    // Expect '='
    if (!_expectChar (aContext, '='))
      return false;

    _skipWhitespace (aContext);

    // Parse value (token or quoted-string)
    final String sValue = _parseValue (aContext);
    if (sValue == null)
      return false;

    _skipWhitespace (aContext);

    // Add the pair to the result
    try
    {
      aResult.addPair (sToken, sValue);
      return true;
    }
    catch (final Exception ex)
    {
      // Invalid token according to RFC 7230
      return false;
    }
  }

  /**
   * Parse a forwarded-element string according to RFC 7239.
   *
   * @param sForwardedElement
   *        The forwarded-element string to parse. May be <code>null</code>.
   * @return A new {@link HttpForwardedHeader} containing the parsed pairs, or <code>null</code> if
   *         parsing failed or the input was invalid.
   */
  @Nullable
  public static HttpForwardedHeader parse (@Nullable final String sForwardedElement)
  {
    if (StringHelper.hasNoText (sForwardedElement))
    {
      // Empty string returns empty list
      return new HttpForwardedHeader ();
    }

    try
    {
      final HttpForwardedHeader aResult = new HttpForwardedHeader ();
      final String sTrimmed = sForwardedElement.trim ();
      if (sTrimmed.isEmpty ())
      {
        // Empty but valid
        return aResult;
      }

      final ParseContext aContext = new ParseContext (sTrimmed);

      // Parse first pair (optional)
      if (!_parseOptionalPair (aContext, aResult))
        return null;

      // Parse subsequent pairs preceded by semicolons
      while (aContext.hasMore ())
      {
        _skipWhitespace (aContext);
        if (!_expectChar (aContext, ';'))
          return null;
        _skipWhitespace (aContext);

        if (!_parseOptionalPair (aContext, aResult))
          return null;
      }

      return aResult;
    }
    catch (final Exception ex)
    {
      // Any parsing error results in null return
      LOGGER.error ("Failed to parse HTTP 'Forwarded' header value", ex);
      return null;
    }
  }
}
