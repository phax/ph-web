/*
 * Copyright (C) 2020-2026 Philip Helger (www.helger.com)
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
package com.helger.dns.dnssec;

import java.net.InetAddress;
import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * The key of an {@link IDnsSecValidatingResolverCache}. It contains all the settings that are fixed
 * when a validating resolver is created, so that only lookups with identical settings share a
 * validating resolver.
 *
 * @author Philip Helger
 * @since 11.4.7
 */
@Immutable
public final class DnsSecValidatingResolverKey
{
  private final ICommonsList <InetAddress> m_aCustomDnsServers;
  private final int m_nMaxRetries;
  private final Duration m_aTimeout;
  private final boolean m_bTcp;
  private final String m_sTrustAnchors;
  private final Duration m_aKeyCacheMaxTtl;

  /**
   * Constructor
   *
   * @param aCustomDnsServers
   *        Optional custom DNS servers. The order matters. May be <code>null</code>.
   * @param nMaxRetries
   *        The maximum number of retries. Must be &ge; 0.
   * @param aTimeout
   *        The optional overall timeout. May be <code>null</code> to use the default.
   * @param bTcp
   *        <code>true</code> to use TCP, <code>false</code> to use UDP.
   * @param sTrustAnchors
   *        The trust anchors in DNS master file format. May neither be <code>null</code> nor empty.
   * @param aKeyCacheMaxTtl
   *        The maximum time to cache validated keys. May not be <code>null</code>.
   */
  public DnsSecValidatingResolverKey (@Nullable final Iterable <? extends InetAddress> aCustomDnsServers,
                                      @Nonnegative final int nMaxRetries,
                                      @Nullable final Duration aTimeout,
                                      final boolean bTcp,
                                      @NonNull @Nonempty final String sTrustAnchors,
                                      @NonNull final Duration aKeyCacheMaxTtl)
  {
    ValueEnforcer.isGE0 (nMaxRetries, "MaxRetries");
    ValueEnforcer.notEmpty (sTrustAnchors, "TrustAnchors");
    ValueEnforcer.notNull (aKeyCacheMaxTtl, "KeyCacheMaxTtl");

    m_aCustomDnsServers = new CommonsArrayList <> (aCustomDnsServers);
    m_nMaxRetries = nMaxRetries;
    m_aTimeout = aTimeout;
    m_bTcp = bTcp;
    m_sTrustAnchors = sTrustAnchors;
    m_aKeyCacheMaxTtl = aKeyCacheMaxTtl;
  }

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <InetAddress> getAllCustomDnsServers ()
  {
    return m_aCustomDnsServers.getClone ();
  }

  @Nonnegative
  public int getMaxRetries ()
  {
    return m_nMaxRetries;
  }

  @Nullable
  public Duration getTimeout ()
  {
    return m_aTimeout;
  }

  public boolean hasTimeout ()
  {
    return m_aTimeout != null;
  }

  public boolean isTcp ()
  {
    return m_bTcp;
  }

  @NonNull
  @Nonempty
  public String getTrustAnchors ()
  {
    return m_sTrustAnchors;
  }

  @NonNull
  public Duration getKeyCacheMaxTtl ()
  {
    return m_aKeyCacheMaxTtl;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final DnsSecValidatingResolverKey rhs = (DnsSecValidatingResolverKey) o;
    return m_aCustomDnsServers.equals (rhs.m_aCustomDnsServers) &&
           m_nMaxRetries == rhs.m_nMaxRetries &&
           EqualsHelper.equals (m_aTimeout, rhs.m_aTimeout) &&
           m_bTcp == rhs.m_bTcp &&
           m_sTrustAnchors.equals (rhs.m_sTrustAnchors) &&
           m_aKeyCacheMaxTtl.equals (rhs.m_aKeyCacheMaxTtl);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aCustomDnsServers)
                                       .append (m_nMaxRetries)
                                       .append (m_aTimeout)
                                       .append (m_bTcp)
                                       .append (m_sTrustAnchors)
                                       .append (m_aKeyCacheMaxTtl)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("CustomDnsServers", m_aCustomDnsServers)
                                       .append ("MaxRetries", m_nMaxRetries)
                                       .append ("Timeout", m_aTimeout)
                                       .append ("Tcp", m_bTcp)
                                       .append ("TrustAnchors", m_sTrustAnchors)
                                       .append ("KeyCacheMaxTtl", m_aKeyCacheMaxTtl)
                                       .getToString ();
  }
}
