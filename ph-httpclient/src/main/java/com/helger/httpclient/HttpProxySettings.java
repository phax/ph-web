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
package com.helger.httpclient;

import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.core5.http.HttpHost;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.clone.ICloneable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsLinkedHashSet;
import com.helger.collection.commons.ICommonsOrderedSet;

/**
 * All the easily configurable settings for a single HTTP proxy server
 *
 * @author Philip Helger
 * @since 10.5.0
 */
@NotThreadSafe
public class HttpProxySettings implements IHttpProxySettings, ICloneable <HttpProxySettings>
{
  private HttpHost m_aProxyHost;
  private Credentials m_aProxyCredentials;
  private final ICommonsOrderedSet <String> m_aNonProxyHosts = new CommonsLinkedHashSet <> ();

  /**
   * Default constructor.
   */
  public HttpProxySettings ()
  {}

  /**
   * "Copy" constructor.
   *
   * @param aSource
   *        The source settings to copy from. May not be <code>null</code>.
   */
  public HttpProxySettings (@NonNull final IHttpProxySettings aSource)
  {
    setAllFrom (aSource);
  }

  /**
   * Apply all settings from the provided HTTP proxy settings
   *
   * @param aSource
   *        The source settings to copy from. May not be <code>null</code>.
   * @return this for chaining.
   */
  @NonNull
  public final HttpProxySettings setAllFrom (@NonNull final IHttpProxySettings aSource)
  {
    ValueEnforcer.notNull (aSource, "Source");
    setProxyHost (aSource.getProxyHost ());
    setProxyCredentials (aSource.getProxyCredentials ());
    nonProxyHosts ().setAll (aSource.nonProxyHosts ());
    return this;
  }

  /**
   * @return The proxy host to be used. May be <code>null</code>.
   */
  @Nullable
  public final HttpHost getProxyHost ()
  {
    return m_aProxyHost;
  }

  /**
   * Set a proxy host without proxy server credentials.
   *
   * @param aProxyHost
   *        The proxy host to be used. May be <code>null</code>.
   * @return this for chaining
   * @see #setProxyCredentials(Credentials)
   */
  @NonNull
  public final HttpProxySettings setProxyHost (@Nullable final HttpHost aProxyHost)
  {
    m_aProxyHost = aProxyHost;
    return this;
  }

  /**
   * @return The proxy server credentials to be used. May be <code>null</code>.
   */
  @Nullable
  public final Credentials getProxyCredentials ()
  {
    return m_aProxyCredentials;
  }

  /**
   * Set proxy credentials.
   *
   * @param aProxyCredentials
   *        The proxy server credentials to be used. May be <code>null</code>. They are only used if
   *        a proxy host is present! Usually they are of type
   *        {@link org.apache.hc.client5.http.auth.UsernamePasswordCredentials}.
   * @return this for chaining
   * @see #setProxyHost(HttpHost)
   */
  @NonNull
  public final HttpProxySettings setProxyCredentials (@Nullable final Credentials aProxyCredentials)
  {
    m_aProxyCredentials = aProxyCredentials;
    return this;
  }

  /**
   * @return The set of all host names and IP addresses for which no proxy should be used. Never
   *         <code>null</code> and mutable.
   */
  @NonNull
  @ReturnsMutableObject
  public final ICommonsOrderedSet <String> nonProxyHosts ()
  {
    return m_aNonProxyHosts;
  }

  /**
   * Add all non-proxy hosts from a piped string as in <code>127.0.0.1 | localhost</code>. Every
   * entry must be separated by a pipe, and the values are trimmed.
   *
   * @param sDefinition
   *        The definition string. May be <code>null</code> or empty or invalid. Every non-empty
   *        trimmed text between pipes is interpreted as a host name.
   * @return this for chaining
   */
  @NonNull
  public final HttpProxySettings addNonProxyHostsFromPipeString (@Nullable final String sDefinition)
  {
    if (StringHelper.isNotEmpty (sDefinition))
      StringHelper.explode ('|', sDefinition, sHost -> {
        final String sTrimmedHost = sHost.trim ();
        if (StringHelper.isNotEmpty (sTrimmedHost))
          m_aNonProxyHosts.add (sTrimmedHost);
      });
    return this;
  }

  /**
   * Set all non-proxy hosts from a piped string as in <code>127.0.0.1 | localhost</code>. Every
   * entry must be separated by a pipe, and the values are trimmed.<br>
   * This is a shortcut for first clearing the list and then calling
   * {@link #addNonProxyHostsFromPipeString(String)}
   *
   * @param sDefinition
   *        The definition string. May be <code>null</code> or empty or invalid. Every non-empty
   *        trimmed text between pipes is interpreted as a host name.
   * @return this for chaining
   * @see #addNonProxyHostsFromPipeString(String)
   * @since 10.0.0
   */
  @NonNull
  public final HttpProxySettings setNonProxyHostsFromPipeString (@Nullable final String sDefinition)
  {
    m_aNonProxyHosts.clear ();
    return addNonProxyHostsFromPipeString (sDefinition);
  }

  @NonNull
  @ReturnsMutableCopy
  public HttpProxySettings getClone ()
  {
    return new HttpProxySettings (this);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ProxyHost", m_aProxyHost)
                                       .append ("ProxyCredentials", m_aProxyCredentials)
                                       .append ("NonProxyHosts", m_aNonProxyHosts)
                                       .getToString ();
  }
}
