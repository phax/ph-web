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
package com.helger.web.fileupload;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.ext.CommonsHashMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.string.StringHelper;

/**
 * A simple parser intended to parse sequences of name/value pairs. Parameter
 * values are expected to be enclosed in quotes if they contain unsafe
 * characters, such as '=' characters or separators. Parameter values are
 * optional and can be omitted.
 * <p>
 * <code>param1 = value; param2 = "anything goes; really"; param3</code>
 * </p>
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
@NotThreadSafe
public final class ParameterParser
{
  /**
   * String to be parsed.
   */
  private char [] m_aChars;

  /**
   * Current position in the string.
   */
  private int m_nPos = 0;

  /**
   * Maximum position in the string.
   */
  private int m_nLen = 0;

  /**
   * Start of a token.
   */
  private int m_nIndex1 = 0;

  /**
   * End of a token.
   */
  private int m_nIndex2 = 0;

  /**
   * Whether names stored in the map should be converted to lower case.
   */
  private boolean m_bLowerCaseNames = false;

  /**
   * Default ParameterParser constructor.
   */
  public ParameterParser ()
  {}

  /**
   * Are there any characters left to parse?
   *
   * @return <tt>true</tt> if there are unparsed characters, <tt>false</tt>
   *         otherwise.
   */
  private boolean _hasChar ()
  {
    return m_nPos < m_nLen;
  }

  /**
   * A helper method to process the parsed token. This method removes leading
   * and trailing blanks as well as enclosing quotation marks, when necessary.
   *
   * @param bQuoted
   *        <tt>true</tt> if quotation marks are expected, <tt>false</tt>
   *        otherwise.
   * @return the token
   */
  @Nullable
  private String _getToken (final boolean bQuoted)
  {
    // Trim leading white spaces
    while (m_nIndex1 < m_nIndex2 && Character.isWhitespace (m_aChars[m_nIndex1]))
    {
      m_nIndex1++;
    }
    // Trim trailing white spaces
    while (m_nIndex2 > m_nIndex1 && Character.isWhitespace (m_aChars[m_nIndex2 - 1]))
    {
      m_nIndex2--;
    }
    // Strip away quotation marks if necessary
    if (bQuoted)
    {
      if ((m_nIndex2 - m_nIndex1) >= 2 && m_aChars[m_nIndex1] == '"' && m_aChars[m_nIndex2 - 1] == '"')
      {
        m_nIndex1++;
        m_nIndex2--;
      }
    }
    String ret = null;
    if (m_nIndex2 > m_nIndex1)
    {
      ret = new String (m_aChars, m_nIndex1, m_nIndex2 - m_nIndex1);
    }
    return ret;
  }

  /**
   * Parses out a token until any of the given terminators is encountered.
   *
   * @param cTerminator1
   *        the first terminating character. Any when encountered signify the
   *        end of the token
   * @param cTerminator2
   *        the second terminating character. Any when encountered signify the
   *        end of the token
   * @return the token
   */
  @Nullable
  private String _parseToken (final char cTerminator1, final char cTerminator2)
  {
    char ch;
    m_nIndex1 = m_nPos;
    m_nIndex2 = m_nPos;
    while (_hasChar ())
    {
      ch = m_aChars[m_nPos];
      if (ch == cTerminator1 || ch == cTerminator2)
        break;
      m_nIndex2++;
      m_nPos++;
    }
    return _getToken (false);
  }

  /**
   * Parses out a token until any of the given terminators is encountered
   * outside the quotation marks.
   *
   * @param cTerminator
   *        the terminating character. Any of these characters when encountered
   *        outside the quotation marks signify the end of the token
   * @return the token
   */
  @Nullable
  private String _parseQuotedToken (final char cTerminator)
  {
    char ch;
    m_nIndex1 = m_nPos;
    m_nIndex2 = m_nPos;
    boolean bQuoted = false;
    boolean bCharEscaped = false;
    while (_hasChar ())
    {
      ch = m_aChars[m_nPos];
      if (!bQuoted && cTerminator == ch)
        break;

      if (!bCharEscaped && ch == '"')
        bQuoted = !bQuoted;
      bCharEscaped = (!bCharEscaped && ch == '\\');
      m_nIndex2++;
      m_nPos++;
    }
    return _getToken (true);
  }

  /**
   * Returns <tt>true</tt> if parameter names are to be converted to lower case
   * when name/value pairs are parsed.
   *
   * @return <tt>true</tt> if parameter names are to be converted to lower case
   *         when name/value pairs are parsed. Otherwise returns <tt>false</tt>
   */
  public boolean isLowerCaseNames ()
  {
    return m_bLowerCaseNames;
  }

  /**
   * Sets the flag if parameter names are to be converted to lower case when
   * name/value pairs are parsed.
   *
   * @param bLowerCaseNames
   *        <tt>true</tt> if parameter names are to be converted to lower case
   *        when name/value pairs are parsed. <tt>false</tt> otherwise.
   * @return this
   */
  @Nonnull
  public ParameterParser setLowerCaseNames (final boolean bLowerCaseNames)
  {
    m_bLowerCaseNames = bLowerCaseNames;
    return this;
  }

  /**
   * Extracts a map of name/value pairs from the given string. Names are
   * expected to be unique. Multiple separators may be specified and the
   * earliest found in the input string is used.
   *
   * @param sStr
   *        the string that contains a sequence of name/value pairs
   * @param aSeparators
   *        the name/value pairs separators
   * @return a map of name/value pairs
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <String, String> parse (@Nullable final String sStr, @Nullable final char [] aSeparators)
  {
    if (ArrayHelper.isEmpty (aSeparators))
      return new CommonsHashMap <> ();

    char cSep = aSeparators[0];
    if (sStr != null)
    {
      // Find the first separator to use
      int idx = sStr.length ();
      for (final char cSep2 : aSeparators)
      {
        final int tmp = sStr.indexOf (cSep2);
        if (tmp != -1 && tmp < idx)
        {
          idx = tmp;
          cSep = cSep2;
        }
      }
    }
    return parse (sStr, cSep);
  }

  /**
   * Extracts a map of name/value pairs from the given string. Names are
   * expected to be unique.
   *
   * @param sStr
   *        the string that contains a sequence of name/value pairs
   * @param cSeparator
   *        the name/value pairs separator
   * @return a map of name/value pairs
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <String, String> parse (@Nullable final String sStr, final char cSeparator)
  {
    final ICommonsMap <String, String> ret = new CommonsHashMap <> ();
    if (sStr != null)
    {
      final char [] aChars = sStr.toCharArray ();
      m_aChars = aChars;
      m_nPos = 0;
      m_nLen = sStr.length ();

      while (_hasChar ())
      {
        String sParamName = _parseToken ('=', cSeparator);
        String sParamValue = null;
        if (_hasChar () && aChars[m_nPos] == '=')
        {
          // skip '='
          m_nPos++;
          sParamValue = _parseQuotedToken (cSeparator);
        }
        if (_hasChar () && aChars[m_nPos] == cSeparator)
        {// skip separator
          m_nPos++;
        }
        if (StringHelper.hasText (sParamName))
        {
          if (m_bLowerCaseNames)
            sParamName = sParamName.toLowerCase (Locale.US);
          ret.put (sParamName, sParamValue);
        }
      }
    }
    return ret;
  }
}
