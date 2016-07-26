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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;

/**
 * Handler for the request HTTP header field "Accept-Language"
 *
 * @author Philip Helger
 */
@Immutable
public final class AcceptLanguageHandler
{
  /** Any language */
  public static final String ANY_LANGUAGE = "*";

  @PresentForCodeCoverage
  private static final AcceptLanguageHandler s_aInstance = new AcceptLanguageHandler ();

  private AcceptLanguageHandler ()
  {}

  @Nonnull
  public static AcceptLanguageList getAcceptLanguages (@Nullable final String sAcceptLanguage)
  {
    final AcceptLanguageList ret = new AcceptLanguageList ();
    if (StringHelper.hasNoText (sAcceptLanguage))
    {
      // No definition - access all
      ret.addLanguage (ANY_LANGUAGE, QValue.MAX_QUALITY);
    }
    else
    {
      // Languages are separated by "," or ", "
      for (final String sItem : StringHelper.getExploded (',', sAcceptLanguage))
      {
        // Qualities are separated by ";"
        final String [] aParts = StringHelper.getExplodedArray (';', sItem.trim (), 2);

        // Default quality is 1
        double dQuality = QValue.MAX_QUALITY;
        if (aParts.length == 2 && aParts[1].trim ().startsWith ("q="))
          dQuality = StringParser.parseDouble (aParts[1].trim ().substring (2), QValue.MAX_QUALITY);
        ret.addLanguage (aParts[0], dQuality);
      }
    }
    return ret;
  }

  @Nonnull
  public static AcceptLanguageList getAcceptLanguages (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Check if a value is cached in the HTTP request
    AcceptLanguageList aValue = (AcceptLanguageList) aHttpRequest.getAttribute (AcceptLanguageList.class.getName ());
    if (aValue == null)
    {
      final String sAcceptLanguage = aHttpRequest.getHeader (CHTTPHeader.ACCEPT_LANGUAGE);
      aValue = getAcceptLanguages (sAcceptLanguage);
      aHttpRequest.setAttribute (AcceptLanguageList.class.getName (), aValue);
    }
    return aValue;
  }
}
