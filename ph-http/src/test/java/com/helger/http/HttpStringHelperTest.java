/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test class for class {@link HttpStringHelper}.
 *
 * @author Philip Helger
 */
public final class HttpStringHelperTest
{
  @Test
  public void testIsChar ()
  {
    assertFalse (HttpStringHelper.isChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      assertTrue (HttpStringHelper.isChar (i));
    assertFalse (HttpStringHelper.isChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testUpperAlphaChar ()
  {
    assertFalse (HttpStringHelper.isUpperAlphaChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i >= 'A' && i <= 'Z')
        assertTrue (HttpStringHelper.isUpperAlphaChar (i));
      else
        assertFalse (HttpStringHelper.isUpperAlphaChar (i));
    assertFalse (HttpStringHelper.isUpperAlphaChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testLowerAlphaChar ()
  {
    assertFalse (HttpStringHelper.isLowerAlphaChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i >= 'a' && i <= 'z')
        assertTrue (HttpStringHelper.isLowerAlphaChar (i));
      else
        assertFalse (HttpStringHelper.isLowerAlphaChar (i));
    assertFalse (HttpStringHelper.isLowerAlphaChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testAlphaChar ()
  {
    assertFalse (HttpStringHelper.isAlphaChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z'))
        assertTrue (HttpStringHelper.isAlphaChar (i));
      else
        assertFalse (HttpStringHelper.isAlphaChar (i));
    assertFalse (HttpStringHelper.isAlphaChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testDigitChar ()
  {
    assertFalse (HttpStringHelper.isDigitChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i >= '0' && i <= '9')
        assertTrue (HttpStringHelper.isDigitChar (i));
      else
        assertFalse (HttpStringHelper.isDigitChar (i));
    assertFalse (HttpStringHelper.isDigitChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testControlChar ()
  {
    assertFalse (HttpStringHelper.isControlChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i < ' ' || i == 127)
        assertTrue (HttpStringHelper.isControlChar (i));
      else
        assertFalse (HttpStringHelper.isControlChar (i));
    assertFalse (HttpStringHelper.isControlChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testCRChar ()
  {
    assertFalse (HttpStringHelper.isCRChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '\r')
        assertTrue (HttpStringHelper.isCRChar (i));
      else
        assertFalse (HttpStringHelper.isCRChar (i));
    assertFalse (HttpStringHelper.isCRChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testLFChar ()
  {
    assertFalse (HttpStringHelper.isLFChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '\n')
        assertTrue (HttpStringHelper.isLFChar (i));
      else
        assertFalse (HttpStringHelper.isLFChar (i));
    assertFalse (HttpStringHelper.isLFChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testSpaceChar ()
  {
    assertFalse (HttpStringHelper.isSpaceChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == ' ')
        assertTrue (HttpStringHelper.isSpaceChar (i));
      else
        assertFalse (HttpStringHelper.isSpaceChar (i));
    assertFalse (HttpStringHelper.isSpaceChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testTabChar ()
  {
    assertFalse (HttpStringHelper.isTabChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '\t')
        assertTrue (HttpStringHelper.isTabChar (i));
      else
        assertFalse (HttpStringHelper.isTabChar (i));
    assertFalse (HttpStringHelper.isTabChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testLinearWhitespaceChar ()
  {
    assertFalse (HttpStringHelper.isLinearWhitespaceChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == ' ' || i == '\t' || i == '\r' || i == '\n')
        assertTrue (HttpStringHelper.isLinearWhitespaceChar (i));
      else
        assertFalse (HttpStringHelper.isLinearWhitespaceChar (i));
    assertFalse (HttpStringHelper.isSpaceChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testQuoteChar ()
  {
    assertFalse (HttpStringHelper.isQuoteChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '"')
        assertTrue (HttpStringHelper.isQuoteChar (i));
      else
        assertFalse (HttpStringHelper.isQuoteChar (i));
    assertFalse (HttpStringHelper.isQuoteChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testHexChar ()
  {
    assertFalse (HttpStringHelper.isHexChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if ((i >= 'A' && i <= 'F') || (i >= 'a' && i <= 'f') || (i >= '0' && i <= '9'))
        assertTrue (HttpStringHelper.isHexChar (i));
      else
        assertFalse (HttpStringHelper.isHexChar (i));
    assertFalse (HttpStringHelper.isHexChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testNonTokenChar ()
  {
    assertFalse (HttpStringHelper.isNonTokenChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '(' ||
          i == ')' ||
          i == '<' ||
          i == '>' ||
          i == '@' ||
          i == ',' ||
          i == ';' ||
          i == ':' ||
          i == '\\' ||
          i == '"' ||
          i == '/' ||
          i == '[' ||
          i == ']' ||
          i == '?' ||
          i == '=' ||
          i == '{' ||
          i == '}' ||
          i == ' ' ||
          i == '\t')
        assertTrue (HttpStringHelper.isNonTokenChar (i));
      else
        assertFalse (HttpStringHelper.isNonTokenChar (i));
    assertFalse (HttpStringHelper.isNonTokenChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testTokenChar ()
  {
    assertFalse (HttpStringHelper.isTokenChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (!HttpStringHelper.isControlChar (i) && !HttpStringHelper.isNonTokenChar (i))
        assertTrue (HttpStringHelper.isTokenChar (i));
      else
        assertFalse (HttpStringHelper.isTokenChar (i));
    assertFalse (HttpStringHelper.isTokenChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testIsToken ()
  {
    assertFalse (HttpStringHelper.isToken ((String) null));
    assertFalse (HttpStringHelper.isToken ((char []) null));
    assertFalse (HttpStringHelper.isToken (new char [0]));
    assertFalse (HttpStringHelper.isToken (new char [10]));
    assertFalse (HttpStringHelper.isToken (""));
    assertFalse (HttpStringHelper.isToken (" "));
    assertFalse (HttpStringHelper.isToken ("bla bla"));
    assertFalse (HttpStringHelper.isToken ("(bla"));
    assertFalse (HttpStringHelper.isToken ("bl(a"));
    assertFalse (HttpStringHelper.isToken ("bl)a"));
    assertFalse (HttpStringHelper.isToken ("bl<a"));
    assertFalse (HttpStringHelper.isToken ("bl>a"));
    assertTrue (HttpStringHelper.isToken ("bla"));
    assertTrue (HttpStringHelper.isToken ("bla_foo_fasel"));
    assertTrue (HttpStringHelper.isToken ("0123435678"));
  }

  @Test
  public void testTextChar ()
  {
    assertFalse (HttpStringHelper.isTextChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (!HttpStringHelper.isControlChar (i) ||
          HttpStringHelper.isCRChar (i) ||
          HttpStringHelper.isLFChar (i) ||
          HttpStringHelper.isTabChar (i) ||
          HttpStringHelper.isSpaceChar (i))
        assertTrue (HttpStringHelper.isTextChar (i));
      else
        assertFalse (HttpStringHelper.isTextChar (i));
    // Any other octet is valid!
    assertTrue (HttpStringHelper.isTextChar (HttpStringHelper.MAX_INDEX + 1));
    assertTrue (HttpStringHelper.isTextChar (255));
    assertFalse (HttpStringHelper.isTextChar (256));
  }

  @Test
  public void testCommentChar ()
  {
    assertFalse (HttpStringHelper.isCommentChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if ((!HttpStringHelper.isControlChar (i) &&
           i != HttpStringHelper.COMMENT_BEGIN &&
           i != HttpStringHelper.COMMENT_END) ||
          HttpStringHelper.isCRChar (i) ||
          HttpStringHelper.isLFChar (i) ||
          HttpStringHelper.isTabChar (i) ||
          HttpStringHelper.isSpaceChar (i))
        assertTrue (HttpStringHelper.isCommentChar (i));
      else
        assertFalse (HttpStringHelper.isCommentChar (i));
    // Any other octet is valid!
    assertTrue (HttpStringHelper.isCommentChar (HttpStringHelper.MAX_INDEX + 1));
    assertTrue (HttpStringHelper.isCommentChar (255));
    assertFalse (HttpStringHelper.isCommentChar (256));
  }

  @Test
  public void testIsComment ()
  {
    assertFalse (HttpStringHelper.isComment ((String) null));
    assertFalse (HttpStringHelper.isComment ((char []) null));
    assertFalse (HttpStringHelper.isComment (new char [0]));
    assertFalse (HttpStringHelper.isComment (new char [10]));
    assertFalse (HttpStringHelper.isComment (""));
    assertFalse (HttpStringHelper.isComment (" "));
    assertFalse (HttpStringHelper.isComment ("bla bla"));
    assertFalse (HttpStringHelper.isComment ("(bla"));
    assertFalse (HttpStringHelper.isComment ("(bl\u0000a)"));
    assertFalse (HttpStringHelper.isComment ("(bl(a)"));
    assertFalse (HttpStringHelper.isComment ("(bl)a)"));
    assertFalse (HttpStringHelper.isComment (" (bla)"));
    assertFalse (HttpStringHelper.isComment ("(bla) "));
    assertTrue (HttpStringHelper.isComment ("(bla)"));
    assertTrue (HttpStringHelper.isComment ("(bla foo fasel)"));
  }

  @Test
  @SuppressFBWarnings ("RpC_REPEATED_CONDITIONAL_TEST")
  public void testQuotedTextChar ()
  {
    assertFalse (HttpStringHelper.isQuotedTextChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if ((!HttpStringHelper.isControlChar (i) &&
           i != HttpStringHelper.QUOTEDTEXT_BEGIN &&
           i != HttpStringHelper.QUOTEDTEXT_END) ||
          HttpStringHelper.isCRChar (i) ||
          HttpStringHelper.isLFChar (i) ||
          HttpStringHelper.isTabChar (i) ||
          HttpStringHelper.isSpaceChar (i))
        assertTrue (Integer.toHexString (i), HttpStringHelper.isQuotedTextChar (i));
      else
        assertFalse (HttpStringHelper.isQuotedTextChar (i));
    assertFalse (HttpStringHelper.isQuotedTextChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testIsQuotedText ()
  {
    assertFalse (HttpStringHelper.isQuotedText ((String) null));
    assertFalse (HttpStringHelper.isQuotedText ((char []) null));
    assertFalse (HttpStringHelper.isQuotedText (new char [0]));
    assertFalse (HttpStringHelper.isQuotedText (new char [10]));
    assertFalse (HttpStringHelper.isQuotedText (""));
    assertFalse (HttpStringHelper.isQuotedText (" "));
    assertFalse (HttpStringHelper.isQuotedText ("bla bla"));
    assertFalse (HttpStringHelper.isQuotedText ("\"bla"));
    assertFalse (HttpStringHelper.isQuotedText ("\"bl\u0000a\""));
    assertFalse (HttpStringHelper.isQuotedText ("\"bl\"a\""));
    assertFalse (HttpStringHelper.isQuotedText (" \"bla\""));
    assertFalse (HttpStringHelper.isQuotedText ("\"bla\" "));
    assertTrue (HttpStringHelper.isQuotedText ("\"bla\""));
    assertTrue (HttpStringHelper.isQuotedText ("\"bla foo fasel\""));
  }

  @Test
  public void testIsQuotedTextContent ()
  {
    assertFalse (HttpStringHelper.isQuotedTextContent ((String) null));
    assertFalse (HttpStringHelper.isQuotedTextContent ((char []) null));
    assertTrue (HttpStringHelper.isQuotedTextContent (new char [0]));
    assertFalse (HttpStringHelper.isQuotedTextContent (new char [10]));
    assertTrue (HttpStringHelper.isQuotedTextContent (""));
    assertTrue (HttpStringHelper.isQuotedTextContent (" "));
    assertTrue (HttpStringHelper.isQuotedTextContent ("bla bla"));
    assertTrue (HttpStringHelper.isQuotedTextContent ("bla"));
    assertFalse (HttpStringHelper.isQuotedTextContent ("bl\u0000a"));
    assertFalse (HttpStringHelper.isQuotedTextContent ("bl\"a"));
    assertTrue (HttpStringHelper.isQuotedTextContent (" bla"));
    assertTrue (HttpStringHelper.isQuotedTextContent ("bla "));
    assertTrue (HttpStringHelper.isQuotedTextContent ("bla"));
    assertTrue (HttpStringHelper.isQuotedTextContent ("bla foo fasel"));
  }

  @Test
  public void testReservedChar ()
  {
    assertFalse (HttpStringHelper.isReservedChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == ';' || i == '/' || i == '?' || i == ':' || i == '@' || i == '&' || i == '=' || i == '+')
        assertTrue (HttpStringHelper.isReservedChar (i));
      else
        assertFalse (HttpStringHelper.isReservedChar (i));
    assertFalse (HttpStringHelper.isReservedChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testExtraChar ()
  {
    assertFalse (HttpStringHelper.isExtraChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '!' || i == '*' || i == '\'' || i == '(' || i == ')' || i == ',')
        assertTrue (HttpStringHelper.isExtraChar (i));
      else
        assertFalse (HttpStringHelper.isExtraChar (i));
    assertFalse (HttpStringHelper.isExtraChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testSafeChar ()
  {
    assertFalse (HttpStringHelper.isSafeChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (i == '$' || i == '-' || i == '_' || i == '.')
        assertTrue (HttpStringHelper.isSafeChar (i));
      else
        assertFalse (HttpStringHelper.isSafeChar (i));
    assertFalse (HttpStringHelper.isSafeChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testUnsafeChar ()
  {
    assertFalse (HttpStringHelper.isUnsafeChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (HttpStringHelper.isControlChar (i) || i == ' ' || i == '"' || i == '#' || i == '%' || i == '<' || i == '>')
        assertTrue (HttpStringHelper.isUnsafeChar (i));
      else
        assertFalse (HttpStringHelper.isUnsafeChar (i));
    assertFalse (HttpStringHelper.isUnsafeChar (HttpStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testNationalChar ()
  {
    assertFalse (HttpStringHelper.isNationalChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (HttpStringHelper.isAlphaChar (i) ||
          HttpStringHelper.isDigitChar (i) ||
          HttpStringHelper.isReservedChar (i) ||
          HttpStringHelper.isExtraChar (i) ||
          HttpStringHelper.isSafeChar (i) ||
          HttpStringHelper.isUnsafeChar (i))
        assertFalse (HttpStringHelper.isNationalChar (i));
      else
        assertTrue (HttpStringHelper.isNationalChar (i));
    // Any other octet is valid!
    assertTrue (HttpStringHelper.isNationalChar (HttpStringHelper.MAX_INDEX + 1));
    assertTrue (HttpStringHelper.isNationalChar (255));
    assertFalse (HttpStringHelper.isNationalChar (256));
  }

  @Test
  public void testUnreservedChar ()
  {
    assertFalse (HttpStringHelper.isUnreservedChar (HttpStringHelper.MIN_INDEX - 1));
    for (int i = HttpStringHelper.MIN_INDEX; i <= HttpStringHelper.MAX_INDEX; ++i)
      if (HttpStringHelper.isAlphaChar (i) ||
          HttpStringHelper.isDigitChar (i) ||
          HttpStringHelper.isSafeChar (i) ||
          HttpStringHelper.isExtraChar (i) ||
          HttpStringHelper.isNationalChar (i))
        assertTrue (HttpStringHelper.isUnreservedChar (i));
      else
        assertFalse (HttpStringHelper.isUnreservedChar (i));
    // Any other octet is valid!
    assertTrue (HttpStringHelper.isUnreservedChar (HttpStringHelper.MAX_INDEX + 1));
    assertTrue (HttpStringHelper.isUnreservedChar (255));
    assertFalse (HttpStringHelper.isUnreservedChar (256));
  }
}
