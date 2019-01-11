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
package com.helger.http.csp;

import java.io.Serializable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.base64.Base64;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.url.ISimpleURL;
import com.helger.security.messagedigest.EMessageDigestAlgorithm;

/**
 * A source list to be used in a CSP 2.0 directive ({@link CSP2Directive}). It's
 * just a convenient way to build a CSP directive value.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class CSP2SourceList implements Serializable
{
  public static final String KEYWORD_NONE = "'none'";
  public static final String KEYWORD_SELF = "'self'";
  public static final String KEYWORD_UNSAFE_INLINE = "'unsafe-inline'";
  public static final String KEYWORD_UNSAFE_EVAL = "'unsafe-eval'";
  public static final String NONCE_PREFIX = "'nonce-";
  public static final String NONCE_SUFFIX = "'";
  public static final String HASH_PREFIX = "'";
  public static final String HASH_SUFFIX = "'";

  private final ICommonsOrderedSet <String> m_aList = new CommonsLinkedHashSet <> ();

  public CSP2SourceList ()
  {}

  @Nonnegative
  public int getExpressionCount ()
  {
    return m_aList.size ();
  }

  /**
   * Add a scheme
   *
   * @param sScheme
   *        Scheme in the format <code>scheme ":"</code>
   * @return this
   */
  @Nonnull
  public CSP2SourceList addScheme (@Nonnull @Nonempty final String sScheme)
  {
    ValueEnforcer.notEmpty (sScheme, "Scheme");
    ValueEnforcer.isTrue (sScheme.length () > 1 && sScheme.endsWith (":"),
                          () -> "Passed scheme '" + sScheme + "' is invalid!");
    m_aList.add (sScheme);
    return this;
  }

  /**
   * Add a host
   *
   * @param aHost
   *        Host to add. Must be a valid URL.
   * @return this
   */
  @Nonnull
  public CSP2SourceList addHost (@Nonnull final ISimpleURL aHost)
  {
    ValueEnforcer.notNull (aHost, "Host");
    return addHost (aHost.getAsStringWithEncodedParameters ());
  }

  /**
   * Add a host
   *
   * @param sHost
   *        Host to add. Must be a valid URL or a star prefixed version.
   * @return this
   */
  @Nonnull
  public CSP2SourceList addHost (@Nonnull @Nonempty final String sHost)
  {
    ValueEnforcer.notEmpty (sHost, "Host");
    m_aList.add (sHost);
    return this;
  }

  /**
   * Add a MIME type (for plugin-types)
   *
   * @param aMimeType
   *        MIME type to add. May not be <code>null</code>.
   * @return this
   */
  @Nonnull
  public CSP2SourceList addMimeType (@Nonnull final IMimeType aMimeType)
  {
    ValueEnforcer.notNull (aMimeType, "aMimeType");
    m_aList.add (aMimeType.getAsString ());
    return this;
  }

  /**
   * source expression 'none' represents an empty set of URIs
   *
   * @return this
   */
  @Nonnull
  public CSP2SourceList addKeywordNone ()
  {
    m_aList.add (KEYWORD_NONE);
    return this;
  }

  /**
   * source expression 'self' represents the set of URIs which are in the same
   * origin as the protected resource
   *
   * @return this
   */
  @Nonnull
  public CSP2SourceList addKeywordSelf ()
  {
    m_aList.add (KEYWORD_SELF);
    return this;
  }

  /**
   * source expression 'unsafe-inline' represents content supplied inline in the
   * resource itself
   *
   * @return this
   */
  @Nonnull
  public CSP2SourceList addKeywordUnsafeInline ()
  {
    m_aList.add (KEYWORD_UNSAFE_INLINE);
    return this;
  }

  @Nonnull
  public CSP2SourceList addKeywordUnsafeEval ()
  {
    m_aList.add (KEYWORD_UNSAFE_EVAL);
    return this;
  }

  /**
   * Add the provided nonce value. The {@value #NONCE_PREFIX} and
   * {@link #NONCE_SUFFIX} are added automatically. The byte array is
   * automatically Bas64 encoded!
   *
   * @param aNonceValue
   *        The plain nonce value. May not be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public CSP2SourceList addNonce (@Nonnull @Nonempty final byte [] aNonceValue)
  {
    ValueEnforcer.notEmpty (aNonceValue, "NonceValue");
    return addNonce (Base64.safeEncodeBytes (aNonceValue));
  }

  /**
   * Add the provided Base64 encoded nonce value. The {@value #NONCE_PREFIX} and
   * {@link #NONCE_SUFFIX} are added automatically.
   *
   * @param sNonceBase64Value
   *        The Base64 encoded nonce value
   * @return this for chaining
   */
  @Nonnull
  public CSP2SourceList addNonce (@Nonnull @Nonempty final String sNonceBase64Value)
  {
    ValueEnforcer.notEmpty (sNonceBase64Value, "NonceBase64Value");

    m_aList.add (NONCE_PREFIX + sNonceBase64Value + NONCE_SUFFIX);
    return this;
  }

  /**
   * Add the provided nonce value. The {@value #HASH_PREFIX} and
   * {@link #HASH_SUFFIX} are added automatically. The byte array is automatically
   * Bas64 encoded!
   *
   * @param eMDAlgo
   *        The message digest algorithm used. May only
   *        {@link EMessageDigestAlgorithm#SHA_256},
   *        {@link EMessageDigestAlgorithm#SHA_384} or
   *        {@link EMessageDigestAlgorithm#SHA_512}. May not be <code>null</code>.
   * @param aHashValue
   *        The plain hash digest value. May not be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public CSP2SourceList addHash (@Nonnull final EMessageDigestAlgorithm eMDAlgo,
                                 @Nonnull @Nonempty final byte [] aHashValue)
  {
    ValueEnforcer.notEmpty (aHashValue, "HashValue");
    return addHash (eMDAlgo, Base64.safeEncodeBytes (aHashValue));
  }

  /**
   * Add the provided Base64 encoded hash value. The {@value #HASH_PREFIX} and
   * {@link #HASH_SUFFIX} are added automatically.
   *
   * @param eMDAlgo
   *        The message digest algorithm used. May only
   *        {@link EMessageDigestAlgorithm#SHA_256},
   *        {@link EMessageDigestAlgorithm#SHA_384} or
   *        {@link EMessageDigestAlgorithm#SHA_512}. May not be <code>null</code>.
   * @param sHashBase64Value
   *        The Base64 encoded hash value
   * @return this for chaining
   */
  @Nonnull
  public CSP2SourceList addHash (@Nonnull final EMessageDigestAlgorithm eMDAlgo, @Nonnull final String sHashBase64Value)
  {
    ValueEnforcer.notNull (eMDAlgo, "MDAlgo");
    ValueEnforcer.notEmpty (sHashBase64Value, "HashBase64Value");

    String sAlgorithmName;
    switch (eMDAlgo)
    {
      case SHA_256:
        sAlgorithmName = "sha256";
        break;
      case SHA_384:
        sAlgorithmName = "sha384";
        break;
      case SHA_512:
        sAlgorithmName = "sha512";
        break;
      default:
        throw new IllegalArgumentException ("Only SHA256, SHA384 and SHA512 are supported algorithms");
    }

    m_aList.add (HASH_PREFIX + sAlgorithmName + "-" + sHashBase64Value + HASH_SUFFIX);
    return this;
  }

  /**
   * @return The whole source list as a single string, separated by a blank char.
   */
  @Nonnull
  public String getAsString ()
  {
    return StringHelper.getImploded (' ', m_aList);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final CSP2SourceList rhs = (CSP2SourceList) o;
    return m_aList.equals (rhs.m_aList);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aList).getHashCode ();
  }

  @Override
  @Nonnull
  public String toString ()
  {
    return new ToStringGenerator (this).append ("List", m_aList).getToString ();
  }
}
