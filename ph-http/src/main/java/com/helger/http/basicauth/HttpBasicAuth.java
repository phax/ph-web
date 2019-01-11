/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.http.basicauth;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.base64.Base64;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.http.digestauth.HttpDigestAuth;

/**
 * Handling for HTTP Basic Authentication
 *
 * @author Philip Helger
 */
@Immutable
public final class HttpBasicAuth
{
  public static final String HEADER_VALUE_PREFIX_BASIC = "Basic";
  public static final char USERNAME_PASSWORD_SEPARATOR = ':';
  public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

  private static final Logger LOGGER = LoggerFactory.getLogger (HttpDigestAuth.class);

  @PresentForCodeCoverage
  private static final HttpBasicAuth s_aInstance = new HttpBasicAuth ();

  private HttpBasicAuth ()
  {}

  /**
   * Get the Basic authentication credentials from the passed HTTP header value.
   *
   * @param sAuthHeader
   *        The HTTP header value to be interpreted. May be <code>null</code>.
   * @return <code>null</code> if the passed value is not a correct HTTP Basic
   *         Authentication header value.
   */
  @Nullable
  public static BasicAuthClientCredentials getBasicAuthClientCredentials (@Nullable final String sAuthHeader)
  {
    final String sRealHeader = StringHelper.trim (sAuthHeader);
    if (StringHelper.hasNoText (sRealHeader))
      return null;

    final String [] aElements = RegExHelper.getSplitToArray (sRealHeader, "\\s+", 2);
    if (aElements.length != 2)
    {
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("String is not Basic Auth: " + sRealHeader);
      return null;
    }

    if (!aElements[0].equals (HEADER_VALUE_PREFIX_BASIC))
    {
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("String does not start with 'Basic': " + sRealHeader);
      return null;
    }

    // Apply Base64 decoding
    final String sEncodedCredentials = aElements[1];
    final String sUsernamePassword = Base64.safeDecodeAsString (sEncodedCredentials, CHARSET);
    if (sUsernamePassword == null)
    {
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("Illegal Base64 encoded value '" + sEncodedCredentials + "'");
      return null;
    }

    // Do we have a username/password separator?
    final int nIndex = sUsernamePassword.indexOf (USERNAME_PASSWORD_SEPARATOR);
    if (nIndex >= 0)
      return new BasicAuthClientCredentials (sUsernamePassword.substring (0, nIndex),
                                             sUsernamePassword.substring (nIndex + 1));
    return new BasicAuthClientCredentials (sUsernamePassword);
  }
}
