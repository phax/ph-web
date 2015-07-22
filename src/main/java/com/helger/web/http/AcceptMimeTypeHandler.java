/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.mime.EMimeContentType;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.mime.MimeType;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.mime.MimeTypeParserException;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;

/**
 * Handler for the request HTTP header field "Accept"
 *
 * @author Philip Helger
 */
@Immutable
public final class AcceptMimeTypeHandler
{
  /** Any MIME type */
  public static final IMimeType ANY_MIMETYPE = new MimeType (EMimeContentType._STAR, "*");

  private static final Logger s_aLogger = LoggerFactory.getLogger (AcceptMimeTypeHandler.class);

  @PresentForCodeCoverage
  private static final AcceptMimeTypeHandler s_aInstance = new AcceptMimeTypeHandler ();

  private AcceptMimeTypeHandler ()
  {}

  @Nonnull
  public static AcceptMimeTypeList getAcceptMimeTypes (@Nullable final String sAcceptMimeTypes)
  {
    final AcceptMimeTypeList ret = new AcceptMimeTypeList ();
    if (StringHelper.hasNoText (sAcceptMimeTypes))
    {
      // No definition - access all
      ret.addMimeType (ANY_MIMETYPE, QValue.MAX_QUALITY);
    }
    else
    {
      // Charsets are separated by "," or ", "
      for (final String sItem : StringHelper.getExploded (',', sAcceptMimeTypes))
      {
        // Qualities are separated by ";"
        final String [] aParts = StringHelper.getExplodedArray (';', sItem.trim (), 2);

        // Default quality is 1
        double dQuality = QValue.MAX_QUALITY;
        if (aParts.length == 2 && aParts[1].trim ().startsWith ("q="))
          dQuality = StringParser.parseDouble (aParts[1].trim ().substring (2), QValue.MAX_QUALITY);

        final String sMimeType = aParts[0];
        IMimeType aMimeType = safeParseMimeType (sMimeType);
        if (aMimeType != null)
        {
          if (aMimeType.hasAnyParameters ())
          {
            s_aLogger.warn ("Ignoring all contained MIME type parameter from '" + sMimeType + "'!");
            aMimeType = aMimeType.getCopyWithoutParameters ();
          }
          ret.addMimeType (aMimeType, dQuality);
        }
        else
          if ("*".equals (sMimeType))
            ret.addMimeType (ANY_MIMETYPE, dQuality);
          else
          {
            // Certain invalid MIME types occur often so they are manually
            // ignored
            if (!"xml/xml".equals (sMimeType))
              s_aLogger.warn ("Failed to parse Mime type '" + sMimeType + "' as part of '" + sAcceptMimeTypes + "'!");
          }
      }
    }
    return ret;
  }

  @Nonnull
  public static AcceptMimeTypeList getAcceptMimeTypes (@Nonnull final HttpServletRequest aHttpRequest)
  {
    // Check if a value is cached in the HTTP request
    AcceptMimeTypeList aValue = (AcceptMimeTypeList) aHttpRequest.getAttribute (AcceptMimeTypeList.class.getName ());
    if (aValue == null)
    {
      final String sAcceptMimeTypes = aHttpRequest.getHeader (CHTTPHeader.ACCEPT);
      aValue = getAcceptMimeTypes (sAcceptMimeTypes);
      aHttpRequest.setAttribute (AcceptMimeTypeList.class.getName (), aValue);
    }
    return aValue;
  }

  @Nullable
  public static IMimeType safeParseMimeType (@Nullable final String sMimeType)
  {
    try
    {
      return MimeTypeParser.parseMimeType (sMimeType);
    }
    catch (final MimeTypeParserException ex)
    {
      if ("*".equals (sMimeType))
        return new MimeType (EMimeContentType._STAR, "*");
    }
    s_aLogger.warn ("Unparsable MIME type '" + sMimeType + "'");
    return null;
  }
}
