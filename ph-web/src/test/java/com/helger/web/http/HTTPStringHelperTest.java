/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test class for class {@link HTTPStringHelper}.
 *
 * @author Philip Helger
 */
public final class HTTPStringHelperTest
{
  @Test
  public void testIsChar ()
  {
    assertFalse (HTTPStringHelper.isChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      assertTrue (HTTPStringHelper.isChar (i));
    assertFalse (HTTPStringHelper.isChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testUpperAlphaChar ()
  {
    assertFalse (HTTPStringHelper.isUpperAlphaChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i >= 'A' && i <= 'Z')
        assertTrue (HTTPStringHelper.isUpperAlphaChar (i));
      else
        assertFalse (HTTPStringHelper.isUpperAlphaChar (i));
    assertFalse (HTTPStringHelper.isUpperAlphaChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testLowerAlphaChar ()
  {
    assertFalse (HTTPStringHelper.isLowerAlphaChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i >= 'a' && i <= 'z')
        assertTrue (HTTPStringHelper.isLowerAlphaChar (i));
      else
        assertFalse (HTTPStringHelper.isLowerAlphaChar (i));
    assertFalse (HTTPStringHelper.isLowerAlphaChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testAlphaChar ()
  {
    assertFalse (HTTPStringHelper.isAlphaChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z'))
        assertTrue (HTTPStringHelper.isAlphaChar (i));
      else
        assertFalse (HTTPStringHelper.isAlphaChar (i));
    assertFalse (HTTPStringHelper.isAlphaChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testDigitChar ()
  {
    assertFalse (HTTPStringHelper.isDigitChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i >= '0' && i <= '9')
        assertTrue (HTTPStringHelper.isDigitChar (i));
      else
        assertFalse (HTTPStringHelper.isDigitChar (i));
    assertFalse (HTTPStringHelper.isDigitChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testControlChar ()
  {
    assertFalse (HTTPStringHelper.isControlChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i < ' ' || i == 127)
        assertTrue (HTTPStringHelper.isControlChar (i));
      else
        assertFalse (HTTPStringHelper.isControlChar (i));
    assertFalse (HTTPStringHelper.isControlChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testCRChar ()
  {
    assertFalse (HTTPStringHelper.isCRChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == '\r')
        assertTrue (HTTPStringHelper.isCRChar (i));
      else
        assertFalse (HTTPStringHelper.isCRChar (i));
    assertFalse (HTTPStringHelper.isCRChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testLFChar ()
  {
    assertFalse (HTTPStringHelper.isLFChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == '\n')
        assertTrue (HTTPStringHelper.isLFChar (i));
      else
        assertFalse (HTTPStringHelper.isLFChar (i));
    assertFalse (HTTPStringHelper.isLFChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testSpaceChar ()
  {
    assertFalse (HTTPStringHelper.isSpaceChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == ' ')
        assertTrue (HTTPStringHelper.isSpaceChar (i));
      else
        assertFalse (HTTPStringHelper.isSpaceChar (i));
    assertFalse (HTTPStringHelper.isSpaceChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testTabChar ()
  {
    assertFalse (HTTPStringHelper.isTabChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == '\t')
        assertTrue (HTTPStringHelper.isTabChar (i));
      else
        assertFalse (HTTPStringHelper.isTabChar (i));
    assertFalse (HTTPStringHelper.isTabChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testLinearWhitespaceChar ()
  {
    assertFalse (HTTPStringHelper.isLinearWhitespaceChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == ' ' || i == '\t' || i == '\r' || i == '\n')
        assertTrue (HTTPStringHelper.isLinearWhitespaceChar (i));
      else
        assertFalse (HTTPStringHelper.isLinearWhitespaceChar (i));
    assertFalse (HTTPStringHelper.isSpaceChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testQuoteChar ()
  {
    assertFalse (HTTPStringHelper.isQuoteChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == '"')
        assertTrue (HTTPStringHelper.isQuoteChar (i));
      else
        assertFalse (HTTPStringHelper.isQuoteChar (i));
    assertFalse (HTTPStringHelper.isQuoteChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testHexChar ()
  {
    assertFalse (HTTPStringHelper.isHexChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if ((i >= 'A' && i <= 'F') || (i >= 'a' && i <= 'f') || (i >= '0' && i <= '9'))
        assertTrue (HTTPStringHelper.isHexChar (i));
      else
        assertFalse (HTTPStringHelper.isHexChar (i));
    assertFalse (HTTPStringHelper.isHexChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testNonTokenChar ()
  {
    assertFalse (HTTPStringHelper.isNonTokenChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
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
        assertTrue (HTTPStringHelper.isNonTokenChar (i));
      else
        assertFalse (HTTPStringHelper.isNonTokenChar (i));
    assertFalse (HTTPStringHelper.isNonTokenChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testTokenChar ()
  {
    assertFalse (HTTPStringHelper.isTokenChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (!HTTPStringHelper.isControlChar (i) && !HTTPStringHelper.isNonTokenChar (i))
        assertTrue (HTTPStringHelper.isTokenChar (i));
      else
        assertFalse (HTTPStringHelper.isTokenChar (i));
    assertFalse (HTTPStringHelper.isTokenChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testIsToken ()
  {
    assertFalse (HTTPStringHelper.isToken ((String) null));
    assertFalse (HTTPStringHelper.isToken ((char []) null));
    assertFalse (HTTPStringHelper.isToken (new char [0]));
    assertFalse (HTTPStringHelper.isToken (new char [10]));
    assertFalse (HTTPStringHelper.isToken (""));
    assertFalse (HTTPStringHelper.isToken (" "));
    assertFalse (HTTPStringHelper.isToken ("bla bla"));
    assertFalse (HTTPStringHelper.isToken ("(bla"));
    assertFalse (HTTPStringHelper.isToken ("bl(a"));
    assertFalse (HTTPStringHelper.isToken ("bl)a"));
    assertFalse (HTTPStringHelper.isToken ("bl<a"));
    assertFalse (HTTPStringHelper.isToken ("bl>a"));
    assertTrue (HTTPStringHelper.isToken ("bla"));
    assertTrue (HTTPStringHelper.isToken ("bla_foo_fasel"));
    assertTrue (HTTPStringHelper.isToken ("0123435678"));
  }

  @Test
  public void testTextChar ()
  {
    assertFalse (HTTPStringHelper.isTextChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (!HTTPStringHelper.isControlChar (i) ||
          HTTPStringHelper.isCRChar (i) ||
          HTTPStringHelper.isLFChar (i) ||
          HTTPStringHelper.isTabChar (i) ||
          HTTPStringHelper.isSpaceChar (i))
        assertTrue (HTTPStringHelper.isTextChar (i));
      else
        assertFalse (HTTPStringHelper.isTextChar (i));
    // Any other octet is valid!
    assertTrue (HTTPStringHelper.isTextChar (HTTPStringHelper.MAX_INDEX + 1));
    assertTrue (HTTPStringHelper.isTextChar (255));
    assertFalse (HTTPStringHelper.isTextChar (256));
  }

  @Test
  public void testCommentChar ()
  {
    assertFalse (HTTPStringHelper.isCommentChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if ((!HTTPStringHelper.isControlChar (i) &&
           i != HTTPStringHelper.COMMENT_BEGIN &&
           i != HTTPStringHelper.COMMENT_END) ||
          HTTPStringHelper.isCRChar (i) ||
          HTTPStringHelper.isLFChar (i) ||
          HTTPStringHelper.isTabChar (i) ||
          HTTPStringHelper.isSpaceChar (i))
        assertTrue (HTTPStringHelper.isCommentChar (i));
      else
        assertFalse (HTTPStringHelper.isCommentChar (i));
    // Any other octet is valid!
    assertTrue (HTTPStringHelper.isCommentChar (HTTPStringHelper.MAX_INDEX + 1));
    assertTrue (HTTPStringHelper.isCommentChar (255));
    assertFalse (HTTPStringHelper.isCommentChar (256));
  }

  @Test
  public void testIsComment ()
  {
    assertFalse (HTTPStringHelper.isComment ((String) null));
    assertFalse (HTTPStringHelper.isComment ((char []) null));
    assertFalse (HTTPStringHelper.isComment (new char [0]));
    assertFalse (HTTPStringHelper.isComment (new char [10]));
    assertFalse (HTTPStringHelper.isComment (""));
    assertFalse (HTTPStringHelper.isComment (" "));
    assertFalse (HTTPStringHelper.isComment ("bla bla"));
    assertFalse (HTTPStringHelper.isComment ("(bla"));
    assertFalse (HTTPStringHelper.isComment ("(bl\u0000a)"));
    assertFalse (HTTPStringHelper.isComment ("(bl(a)"));
    assertFalse (HTTPStringHelper.isComment ("(bl)a)"));
    assertFalse (HTTPStringHelper.isComment (" (bla)"));
    assertFalse (HTTPStringHelper.isComment ("(bla) "));
    assertTrue (HTTPStringHelper.isComment ("(bla)"));
    assertTrue (HTTPStringHelper.isComment ("(bla foo fasel)"));
  }

  @Test
  @SuppressFBWarnings ("RpC_REPEATED_CONDITIONAL_TEST")
  public void testQuotedTextChar ()
  {
    assertFalse (HTTPStringHelper.isQuotedTextChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if ((!HTTPStringHelper.isControlChar (i) &&
           i != HTTPStringHelper.QUOTEDTEXT_BEGIN &&
           i != HTTPStringHelper.QUOTEDTEXT_END) ||
          HTTPStringHelper.isCRChar (i) ||
          HTTPStringHelper.isLFChar (i) ||
          HTTPStringHelper.isTabChar (i) ||
          HTTPStringHelper.isSpaceChar (i))
        assertTrue (Integer.toHexString (i), HTTPStringHelper.isQuotedTextChar (i));
      else
        assertFalse (HTTPStringHelper.isQuotedTextChar (i));
    assertFalse (HTTPStringHelper.isQuotedTextChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testIsQuotedText ()
  {
    assertFalse (HTTPStringHelper.isQuotedText ((String) null));
    assertFalse (HTTPStringHelper.isQuotedText ((char []) null));
    assertFalse (HTTPStringHelper.isQuotedText (new char [0]));
    assertFalse (HTTPStringHelper.isQuotedText (new char [10]));
    assertFalse (HTTPStringHelper.isQuotedText (""));
    assertFalse (HTTPStringHelper.isQuotedText (" "));
    assertFalse (HTTPStringHelper.isQuotedText ("bla bla"));
    assertFalse (HTTPStringHelper.isQuotedText ("\"bla"));
    assertFalse (HTTPStringHelper.isQuotedText ("\"bl\u0000a\""));
    assertFalse (HTTPStringHelper.isQuotedText ("\"bl\"a\""));
    assertFalse (HTTPStringHelper.isQuotedText (" \"bla\""));
    assertFalse (HTTPStringHelper.isQuotedText ("\"bla\" "));
    assertTrue (HTTPStringHelper.isQuotedText ("\"bla\""));
    assertTrue (HTTPStringHelper.isQuotedText ("\"bla foo fasel\""));
  }

  @Test
  public void testIsQuotedTextContent ()
  {
    assertFalse (HTTPStringHelper.isQuotedTextContent ((String) null));
    assertFalse (HTTPStringHelper.isQuotedTextContent ((char []) null));
    assertTrue (HTTPStringHelper.isQuotedTextContent (new char [0]));
    assertFalse (HTTPStringHelper.isQuotedTextContent (new char [10]));
    assertTrue (HTTPStringHelper.isQuotedTextContent (""));
    assertTrue (HTTPStringHelper.isQuotedTextContent (" "));
    assertTrue (HTTPStringHelper.isQuotedTextContent ("bla bla"));
    assertTrue (HTTPStringHelper.isQuotedTextContent ("bla"));
    assertFalse (HTTPStringHelper.isQuotedTextContent ("bl\u0000a"));
    assertFalse (HTTPStringHelper.isQuotedTextContent ("bl\"a"));
    assertTrue (HTTPStringHelper.isQuotedTextContent (" bla"));
    assertTrue (HTTPStringHelper.isQuotedTextContent ("bla "));
    assertTrue (HTTPStringHelper.isQuotedTextContent ("bla"));
    assertTrue (HTTPStringHelper.isQuotedTextContent ("bla foo fasel"));
  }

  @Test
  public void testReservedChar ()
  {
    assertFalse (HTTPStringHelper.isReservedChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == ';' || i == '/' || i == '?' || i == ':' || i == '@' || i == '&' || i == '=' || i == '+')
        assertTrue (HTTPStringHelper.isReservedChar (i));
      else
        assertFalse (HTTPStringHelper.isReservedChar (i));
    assertFalse (HTTPStringHelper.isReservedChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testExtraChar ()
  {
    assertFalse (HTTPStringHelper.isExtraChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == '!' || i == '*' || i == '\'' || i == '(' || i == ')' || i == ',')
        assertTrue (HTTPStringHelper.isExtraChar (i));
      else
        assertFalse (HTTPStringHelper.isExtraChar (i));
    assertFalse (HTTPStringHelper.isExtraChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testSafeChar ()
  {
    assertFalse (HTTPStringHelper.isSafeChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (i == '$' || i == '-' || i == '_' || i == '.')
        assertTrue (HTTPStringHelper.isSafeChar (i));
      else
        assertFalse (HTTPStringHelper.isSafeChar (i));
    assertFalse (HTTPStringHelper.isSafeChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testUnsafeChar ()
  {
    assertFalse (HTTPStringHelper.isUnsafeChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (HTTPStringHelper.isControlChar (i) || i == ' ' || i == '"' || i == '#' || i == '%' || i == '<' || i == '>')
        assertTrue (HTTPStringHelper.isUnsafeChar (i));
      else
        assertFalse (HTTPStringHelper.isUnsafeChar (i));
    assertFalse (HTTPStringHelper.isUnsafeChar (HTTPStringHelper.MAX_INDEX + 1));
  }

  @Test
  public void testNationalChar ()
  {
    assertFalse (HTTPStringHelper.isNationalChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (HTTPStringHelper.isAlphaChar (i) ||
          HTTPStringHelper.isDigitChar (i) ||
          HTTPStringHelper.isReservedChar (i) ||
          HTTPStringHelper.isExtraChar (i) ||
          HTTPStringHelper.isSafeChar (i) ||
          HTTPStringHelper.isUnsafeChar (i))
        assertFalse (HTTPStringHelper.isNationalChar (i));
      else
        assertTrue (HTTPStringHelper.isNationalChar (i));
    // Any other octet is valid!
    assertTrue (HTTPStringHelper.isNationalChar (HTTPStringHelper.MAX_INDEX + 1));
    assertTrue (HTTPStringHelper.isNationalChar (255));
    assertFalse (HTTPStringHelper.isNationalChar (256));
  }

  @Test
  public void testUnreservedChar ()
  {
    assertFalse (HTTPStringHelper.isUnreservedChar (HTTPStringHelper.MIN_INDEX - 1));
    for (int i = HTTPStringHelper.MIN_INDEX; i <= HTTPStringHelper.MAX_INDEX; ++i)
      if (HTTPStringHelper.isAlphaChar (i) ||
          HTTPStringHelper.isDigitChar (i) ||
          HTTPStringHelper.isSafeChar (i) ||
          HTTPStringHelper.isExtraChar (i) ||
          HTTPStringHelper.isNationalChar (i))
        assertTrue (HTTPStringHelper.isUnreservedChar (i));
      else
        assertFalse (HTTPStringHelper.isUnreservedChar (i));
    // Any other octet is valid!
    assertTrue (HTTPStringHelper.isUnreservedChar (HTTPStringHelper.MAX_INDEX + 1));
    assertTrue (HTTPStringHelper.isUnreservedChar (255));
    assertFalse (HTTPStringHelper.isUnreservedChar (256));
  }
}
