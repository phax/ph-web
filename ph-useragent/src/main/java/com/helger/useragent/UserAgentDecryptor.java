/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.useragent;

import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.collection.pair.Pair;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.util.StringScanner;
import com.helger.commons.url.URLProtocolRegistry;

/**
 * This class converts an user agent string to an {@link IUserAgent} object if
 * possible.
 *
 * @author Philip Helger
 */
@Immutable
public final class UserAgentDecryptor
{
  private static final String SKIP_PREFIX = "User-Agent: ";

  @PresentForCodeCoverage
  private static final UserAgentDecryptor s_aInstance = new UserAgentDecryptor ();

  private UserAgentDecryptor ()
  {}

  /**
   * Parse the passed user agent.
   *
   * @param sUserAgent
   *        The user agent string to parse.
   * @return A list than can contain {@link ReadOnlyPair}, {@link String} and
   *         {@link ICommonsList} of String objects.
   */
  @Nonnull
  private static UserAgentElementList _decryptUserAgent (@Nonnull final String sUserAgent)
  {
    final UserAgentElementList ret = new UserAgentElementList ();
    final StringScanner aSS = new StringScanner (sUserAgent.trim ());
    while (true)
    {
      aSS.skipWhitespaces ();
      final int nIndex = aSS.findFirstIndex ('/', ' ', '[', '(');
      if (nIndex == -1)
        break;

      switch (aSS.getCharAtIndex (nIndex))
      {
        case '/':
        {
          // e.g. "a/b"
          final String sKey = aSS.getUntilIndex (nIndex);
          final String sValue = aSS.skip (1).getUntilWhiteSpace ();
          final String sFullValue = sKey + "/" + sValue;
          // Special handling of URLs :)
          if (URLProtocolRegistry.getInstance ().hasKnownProtocol (sFullValue))
            ret.add (sFullValue);
          else
            ret.add (Pair.create (sKey, sValue));
          break;
        }
        case ' ':
        {
          // e.g. "Opera 6.03"
          // e.g. "HTC_P3700 Opera/9.50 (Windows NT 5.1; U; de)"
          final String sText = aSS.getUntilIndex (nIndex).trim ();
          final Matcher aMatcher = RegExHelper.getMatcher ("([^\\s]+)\\s+([0-9]+\\.[0-9]+)", sText);
          if (aMatcher.matches ())
            ret.add (Pair.create (aMatcher.group (1), aMatcher.group (2)));
          else
            ret.add (sText);
          break;
        }
        case '[':
        {
          // e.g. "[en]"
          aSS.setIndex (nIndex).skip (1); // skip incl. "["
          ret.add (aSS.getUntil (']'));
          aSS.skip (1);
          break;
        }
        case '(':
        {
          // e.g. "(compatible; MSIE 5.0; Windows 2000)"
          aSS.setIndex (nIndex).skip (1); // skip incl. "("
          final String sParams = aSS.getUntilBalanced (1, '(', ')');

          // convert to ";" separated list
          final ICommonsList <String> aParams = StringHelper.getExploded (';', sParams);
          ret.add (aParams.getAllMapped (String::trim));
          break;
        }
        default:
          throw new IllegalStateException ("Invalid character: " + aSS.getCharAtIndex (nIndex));
      }
    }

    // add all remaining parts as is
    final String sRest = aSS.getRest ().trim ();
    if (sRest.length () > 0)
      ret.add (sRest);
    return ret;
  }

  /**
   * Decrypt the passed user agent string.
   *
   * @param sUserAgent
   *        The user agent string to decrypt. May not be <code>null</code>.
   * @return The user agent object. Never <code>null</code>.
   */
  @Nonnull
  public static IUserAgent decryptUserAgentString (@Nonnull final String sUserAgent)
  {
    ValueEnforcer.notNull (sUserAgent, "UserAgent");

    String sRealUserAgent = sUserAgent;

    // Check if surrounded with '"' or '''
    if (sRealUserAgent.length () >= 2)
    {
      final char cFirst = sRealUserAgent.charAt (0);
      if ((cFirst == '\'' || cFirst == '"') && StringHelper.getLastChar (sRealUserAgent) == cFirst)
        sRealUserAgent = sRealUserAgent.substring (1, sRealUserAgent.length () - 1);
    }

    // Skip a certain prefix that is known to occur frequently
    if (sRealUserAgent.startsWith (SKIP_PREFIX))
      sRealUserAgent = sRealUserAgent.substring (SKIP_PREFIX.length ());

    return new UserAgent (sRealUserAgent, _decryptUserAgent (sRealUserAgent));
  }
}
