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
package com.helger.web.http.digestauth;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.charset.CCharset;
import com.helger.commons.messagedigest.EMessageDigestAlgorithm;
import com.helger.commons.messagedigest.MessageDigestGeneratorHelper;
import com.helger.commons.string.StringHelper;
import com.helger.web.http.CHTTPHeader;
import com.helger.web.http.EHTTPMethod;
import com.helger.web.http.HTTPStringHelper;

/**
 * Handling for HTTP Digest Authentication
 *
 * @author Philip Helger
 */
@Immutable
public final class HTTPDigestAuth
{
  public static final String HEADER_VALUE_PREFIX_DIGEST = "Digest";

  public static final String ALGORITHM_MD5 = "MD5";
  public static final String ALGORITHM_MD5_SESS = "MD5-sess";
  public static final String DEFAULT_ALGORITHM = ALGORITHM_MD5;

  public static final String QOP_AUTH = "auth";
  public static final String QOP_AUTH_INT = "auth-int";
  public static final String DEFAULT_QOP = QOP_AUTH;

  private static final Logger s_aLogger = LoggerFactory.getLogger (HTTPDigestAuth.class);
  private static final char SEPARATOR = ':';
  private static final Charset CHARSET = CCharset.CHARSET_ISO_8859_1_OBJ;

  @PresentForCodeCoverage
  private static final HTTPDigestAuth s_aInstance = new HTTPDigestAuth ();

  private HTTPDigestAuth ()
  {}

  /**
   * Get the parameters of a Digest authentication string. It may be used for
   * both client and server handling.
   *
   * @param sAuthHeader
   *        The HTTP header value to be interpreted. May be <code>null</code>.
   * @return <code>null</code> if the passed value cannot be parsed as a HTTP
   *         Digest Authentication value, a {@link LinkedHashMap} with all
   *         parameter name-value pairs in the order they are contained.
   */
  @Nullable
  public static Map <String, String> getDigestAuthParams (@Nullable final String sAuthHeader)
  {
    final String sRealHeader = StringHelper.trim (sAuthHeader);
    if (StringHelper.hasNoText (sRealHeader))
      return null;

    if (!sRealHeader.startsWith (HEADER_VALUE_PREFIX_DIGEST))
    {
      s_aLogger.error ("String does not start with 'Digest'");
      return null;
    }

    final char [] aChars = sRealHeader.toCharArray ();
    int nIndex = HEADER_VALUE_PREFIX_DIGEST.length ();

    if (nIndex >= aChars.length || !HTTPStringHelper.isLinearWhitespaceChar (aChars[nIndex]))
    {
      s_aLogger.error ("No whitespace after 'Digest'");
      return null;
    }
    nIndex++;

    final Map <String, String> aParams = new LinkedHashMap <String, String> ();
    while (true)
    {
      // Skip all spaces
      while (nIndex < aChars.length && HTTPStringHelper.isLinearWhitespaceChar (aChars[nIndex]))
        nIndex++;

      // Find token name
      int nStartIndex = nIndex;
      while (nIndex < aChars.length && HTTPStringHelper.isTokenChar (aChars[nIndex]))
        nIndex++;
      if (nStartIndex == nIndex)
      {
        s_aLogger.error ("No token and no whitespace found for auth-param name: '" + aChars[nIndex] + "'");
        return null;
      }
      final String sToken = sRealHeader.substring (nStartIndex, nIndex);

      // Skip all spaces
      while (nIndex < aChars.length && HTTPStringHelper.isLinearWhitespaceChar (aChars[nIndex]))
        nIndex++;

      if (nIndex >= aChars.length || aChars[nIndex] != '=')
      {
        s_aLogger.error ("No separator char '=' found after '" + sToken + "'");
        return null;
      }
      nIndex++;

      // Skip all spaces
      while (nIndex < aChars.length && HTTPStringHelper.isLinearWhitespaceChar (aChars[nIndex]))
        nIndex++;

      if (nIndex >= aChars.length)
      {
        s_aLogger.error ("Found nothing after '=' of '" + sToken + "'");
        return null;
      }

      String sValue;
      if (aChars[nIndex] == HTTPStringHelper.QUOTEDTEXT_BEGIN)
      {
        // Quoted string
        ++nIndex;
        nStartIndex = nIndex;
        while (nIndex < aChars.length && HTTPStringHelper.isQuotedTextChar (aChars[nIndex]))
          nIndex++;
        if (nIndex >= aChars.length)
        {
          s_aLogger.error ("Unexpected EOF in quoted text for '" + sToken + "'");
          return null;
        }
        if (aChars[nIndex] != HTTPStringHelper.QUOTEDTEXT_END)
        {
          s_aLogger.error ("Quoted string of token '" +
                           sToken +
                           "' is not terminated correctly: '" +
                           aChars[nIndex] +
                           "'");
          return null;
        }
        sValue = sRealHeader.substring (nStartIndex, nIndex);

        // Skip termination char
        nIndex++;
      }
      else
      {
        // Token
        nStartIndex = nIndex;
        while (nIndex < aChars.length && HTTPStringHelper.isTokenChar (aChars[nIndex]))
          nIndex++;
        if (nStartIndex == nIndex)
        {
          s_aLogger.error ("No token and no whitespace found for auth-param value of '" +
                           sToken +
                           "': '" +
                           aChars[nIndex] +
                           "'");
          return null;
        }
        sValue = sRealHeader.substring (nStartIndex, nIndex);
      }

      // Remember key/value pair
      aParams.put (sToken, sValue);

      // Skip all spaces
      while (nIndex < aChars.length && HTTPStringHelper.isLinearWhitespaceChar (aChars[nIndex]))
        nIndex++;

      // Check if there are any additional parameters
      if (nIndex >= aChars.length)
      {
        // No more tokens - we're done
        break;
      }

      // If there is a comma, another parameter is expected
      if (aChars[nIndex] != ',')
      {
        s_aLogger.error ("Illegal character after auth-param '" + sToken + "': '" + aChars[nIndex] + "'");
        return null;
      }
      ++nIndex;

      if (nIndex >= aChars.length)
      {
        s_aLogger.error ("Found nothing after continuation of auth-param '" + sToken + "'");
        return null;
      }
    }

    return aParams;
  }

  /**
   * Get the Digest authentication credentials from the passed HTTP servlet
   * request from the HTTP header {@link CHTTPHeader#AUTHORIZATION}.
   *
   * @param aHttpRequest
   *        The HTTP request to be interpreted. May be <code>null</code>.
   * @return <code>null</code> if the passed request does not contain a valid
   *         HTTP Digest Authentication header value.
   */
  @Nullable
  public static DigestAuthClientCredentials getDigestAuthClientCredentials (@Nonnull final HttpServletRequest aHttpRequest)
  {
    ValueEnforcer.notNull (aHttpRequest, "HttpRequest");

    final String sHeaderValue = aHttpRequest.getHeader (CHTTPHeader.AUTHORIZATION);
    return getDigestAuthClientCredentials (sHeaderValue);
  }

  /**
   * Get the Digest authentication credentials from the passed HTTP header
   * value.
   *
   * @param sAuthHeader
   *        The HTTP header value to be interpreted. May be <code>null</code>.
   * @return <code>null</code> if the passed value is not a correct HTTP Digest
   *         Authentication header value.
   */
  @Nullable
  public static DigestAuthClientCredentials getDigestAuthClientCredentials (@Nullable final String sAuthHeader)
  {
    final Map <String, String> aParams = getDigestAuthParams (sAuthHeader);
    if (aParams == null)
      return null;

    final String sUserName = aParams.remove ("username");
    if (sUserName == null)
    {
      s_aLogger.error ("Digest Auth does not container 'username'");
      return null;
    }
    final String sRealm = aParams.remove ("realm");
    if (sRealm == null)
    {
      s_aLogger.error ("Digest Auth does not container 'realm'");
      return null;
    }
    final String sNonce = aParams.remove ("nonce");
    if (sNonce == null)
    {
      s_aLogger.error ("Digest Auth does not container 'nonce'");
      return null;
    }
    final String sDigestURI = aParams.remove ("uri");
    if (sDigestURI == null)
    {
      s_aLogger.error ("Digest Auth does not container 'uri'");
      return null;
    }
    final String sResponse = aParams.remove ("response");
    if (sResponse == null)
    {
      s_aLogger.error ("Digest Auth does not container 'response'");
      return null;
    }
    final String sAlgorithm = aParams.remove ("algorithm");
    final String sCNonce = aParams.remove ("cnonce");
    final String sOpaque = aParams.remove ("opaque");
    final String sMessageQOP = aParams.remove ("qop");
    final String sNonceCount = aParams.remove ("nc");
    if (!aParams.isEmpty ())
      s_aLogger.warn ("Digest Auth contains unhandled parameters: " + aParams.toString ());

    return new DigestAuthClientCredentials (sUserName,
                                            sRealm,
                                            sNonce,
                                            sDigestURI,
                                            sResponse,
                                            sAlgorithm,
                                            sCNonce,
                                            sOpaque,
                                            sMessageQOP,
                                            sNonceCount);
  }

  @Nullable
  public static String getNonceCountString (@CheckForSigned final int nNonceCount)
  {
    return nNonceCount <= 0 ? null : StringHelper.getLeadingZero (StringHelper.getHexString (nNonceCount), 8);
  }

  @Nonnull
  private static String _md5 (@Nonnull final String s)
  {
    final byte [] aMD5 = MessageDigestGeneratorHelper.getAllDigestBytes (s, CHARSET, EMessageDigestAlgorithm.MD5);
    return MessageDigestGeneratorHelper.getHexValueFromDigest (aMD5);
  }

  /**
   * Create HTTP Digest auth credentials for a client
   *
   * @param eMethod
   *        The HTTP method of the request. May not be <code>null</code>.
   * @param sDigestURI
   *        The URI from Request-URI of the Request-Line; duplicated here
   *        because proxies are allowed to change the Request-Line in transit.
   *        May neither be <code>null</code> nor empty.
   * @param sUserName
   *        User name to use. May neither be <code>null</code> nor empty.
   * @param sPassword
   *        The user's password. May not be <code>null</code>.
   * @param sRealm
   *        The realm as provided by the server. May neither be
   *        <code>null</code> nor empty.
   * @param sServerNonce
   *        The nonce as supplied by the server. May neither be
   *        <code>null</code> nor empty.
   * @param sAlgorithm
   *        The algorithm as provided by the server. Currently only
   *        {@link #ALGORITHM_MD5} and {@link #ALGORITHM_MD5_SESS} is supported.
   *        If it is <code>null</code> than {@link #ALGORITHM_MD5} is used as
   *        default.
   * @param sClientNonce
   *        The client nonce to be used. Must be present if message QOP is
   *        specified or if algorithm is {@link #ALGORITHM_MD5_SESS}.<br>
   *        This MUST be specified if a qop directive is sent, and MUST NOT be
   *        specified if the server did not send a qop directive in the
   *        WWW-Authenticate header field. The cnonce-value is an opaque quoted
   *        string value provided by the client and used by both client and
   *        server to avoid chosen plain text attacks, to provide mutual
   *        authentication, and to provide some message integrity protection.
   *        See the descriptions below of the calculation of the response-
   *        digest and request-digest values.
   * @param sOpaque
   *        The opaque value as supplied by the server. May be <code>null</code>
   *        .
   * @param sMessageQOP
   *        The message QOP. Currently only {@link #QOP_AUTH} is supported. If
   *        <code>null</code> is passed, than {@link #QOP_AUTH} with backward
   *        compatibility handling for RFC 2069 is applied.<br>
   *        Indicates what "quality of protection" the client has applied to the
   *        message. If present, its value MUST be one of the alternatives the
   *        server indicated it supports in the WWW-Authenticate header. These
   *        values affect the computation of the request-digest. Note that this
   *        is a single token, not a quoted list of alternatives as in WWW-
   *        Authenticate. This directive is optional in order to preserve
   *        backward compatibility with a minimal implementation of RFC 2069
   *        [6], but SHOULD be used if the server indicated that qop is
   *        supported by providing a qop directive in the WWW-Authenticate
   *        header field.
   * @param nNonceCount
   *        This MUST be specified if a qop directive is sent (see above), and
   *        MUST NOT be specified if the server did not send a qop directive in
   *        the WWW-Authenticate header field. The nc-value is the hexadecimal
   *        count of the number of requests (including the current request) that
   *        the client has sent with the nonce value in this request. For
   *        example, in the first request sent in response to a given nonce
   *        value, the client sends "nc=00000001". The purpose of this directive
   *        is to allow the server to detect request replays by maintaining its
   *        own copy of this count - if the same nc-value is seen twice, then
   *        the request is a replay.
   * @return The created DigestAuthCredentials
   */
  @Nonnull
  public static DigestAuthClientCredentials createDigestAuthClientCredentials (@Nonnull final EHTTPMethod eMethod,
                                                                               @Nonnull @Nonempty final String sDigestURI,
                                                                               @Nonnull @Nonempty final String sUserName,
                                                                               @Nonnull final String sPassword,
                                                                               @Nonnull @Nonempty final String sRealm,
                                                                               @Nonnull @Nonempty final String sServerNonce,
                                                                               @Nullable final String sAlgorithm,
                                                                               @Nullable final String sClientNonce,
                                                                               @Nullable final String sOpaque,
                                                                               @Nullable final String sMessageQOP,
                                                                               @CheckForSigned final int nNonceCount)
  {
    ValueEnforcer.notNull (eMethod, "Method");
    ValueEnforcer.notEmpty (sDigestURI, "DigestURI");
    ValueEnforcer.notEmpty (sUserName, "UserName");
    ValueEnforcer.notNull (sPassword, "Password");
    ValueEnforcer.notEmpty (sRealm, "Realm");
    ValueEnforcer.notEmpty (sServerNonce, "ServerNonce");
    if (sMessageQOP != null && StringHelper.hasNoText (sClientNonce))
      throw new IllegalArgumentException ("If a QOP is defined, client nonce must be set!");
    if (sMessageQOP != null && nNonceCount <= 0)
      throw new IllegalArgumentException ("If a QOP is defined, nonce count must be positive!");

    final String sRealAlgorithm = sAlgorithm == null ? DEFAULT_ALGORITHM : sAlgorithm;
    if (!sRealAlgorithm.equals (ALGORITHM_MD5) && !sRealAlgorithm.equals (ALGORITHM_MD5_SESS))
      throw new IllegalArgumentException ("Currently only '" +
                                          ALGORITHM_MD5 +
                                          "' and '" +
                                          ALGORITHM_MD5_SESS +
                                          "' algorithms are supported!");

    if (sMessageQOP != null && !sMessageQOP.equals (QOP_AUTH))
      throw new IllegalArgumentException ("Currently only '" + QOP_AUTH + "' QOP is supported!");

    // Nonce must always by 8 chars long
    final String sNonceCount = getNonceCountString (nNonceCount);

    // Create HA1
    String sHA1 = _md5 (sUserName + SEPARATOR + sRealm + SEPARATOR + sPassword);
    if (sRealAlgorithm.equals (ALGORITHM_MD5_SESS))
    {
      if (StringHelper.hasNoText (sClientNonce))
        throw new IllegalArgumentException ("Algorithm requires client nonce!");
      sHA1 = _md5 (sHA1 + SEPARATOR + sServerNonce + SEPARATOR + sClientNonce);
    }

    // Create HA2
    // Method name must be upper-case!
    final String sHA2 = _md5 (eMethod.getName () + SEPARATOR + sDigestURI);

    // Create the request digest - result must be all lowercase hex chars!
    String sRequestDigest;
    if (sMessageQOP == null)
    {
      // RFC 2069 backwards compatibility
      sRequestDigest = _md5 (sHA1 + SEPARATOR + sServerNonce + SEPARATOR + sHA2);
    }
    else
    {
      sRequestDigest = _md5 (sHA1 +
                             SEPARATOR +
                             sServerNonce +
                             SEPARATOR +
                             sNonceCount +
                             SEPARATOR +
                             sClientNonce +
                             SEPARATOR +
                             sMessageQOP +
                             SEPARATOR +
                             sHA2);
    }

    return new DigestAuthClientCredentials (sUserName,
                                            sRealm,
                                            sServerNonce,
                                            sDigestURI,
                                            sRequestDigest,
                                            sAlgorithm,
                                            sClientNonce,
                                            sOpaque,
                                            sMessageQOP,
                                            sNonceCount);
  }
}
