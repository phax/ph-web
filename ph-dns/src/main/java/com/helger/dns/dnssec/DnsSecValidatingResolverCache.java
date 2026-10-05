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

import java.io.IOException;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.dnssec.ValidatingResolver;

import com.helger.annotation.Nonnegative;
import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.dns.resolve.ResolverHelper;

/**
 * Default implementation of {@link IDnsSecValidatingResolverCache} with a limited number of
 * entries. If the limit is reached, the least recently used validating resolver is removed.<br>
 * A global default instance is available via {@link #getDefaultInstance()} and can be replaced via
 * {@link #setDefaultInstance(IDnsSecValidatingResolverCache)}.
 *
 * @author Philip Helger
 * @since 11.4.7
 */
@ThreadSafe
public class DnsSecValidatingResolverCache implements IDnsSecValidatingResolverCache
{
  /** The default maximum number of cached validating resolvers */
  public static final int DEFAULT_MAX_SIZE = 16;

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static IDnsSecValidatingResolverCache s_aDefaultInstance = new DnsSecValidatingResolverCache ();

  private final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();
  private final int m_nMaxSize;
  @GuardedBy ("m_aRWLock")
  private final ICommonsOrderedMap <DnsSecValidatingResolverKey, ValidatingResolver> m_aMap;

  public DnsSecValidatingResolverCache ()
  {
    this (DEFAULT_MAX_SIZE);
  }

  public DnsSecValidatingResolverCache (@Nonnegative final int nMaxSize)
  {
    ValueEnforcer.isGT0 (nMaxSize, "MaxSize");
    m_nMaxSize = nMaxSize;
    // Access order for LRU behaviour
    m_aMap = new CommonsLinkedHashMap <> (16, 0.75f, true)
    {
      @Override
      protected boolean removeEldestEntry (final Map.Entry <DnsSecValidatingResolverKey, ValidatingResolver> aEldest)
      {
        return size () > m_nMaxSize;
      }
    };
  }

  @NonNull
  private static ValidatingResolver _createValidatingResolver (@NonNull final DnsSecValidatingResolverKey aKey) throws IOException
  {
    final ExtendedResolver aHeadResolver = ResolverHelper.createExtendedResolver (aKey.getAllCustomDnsServers ());
    // Retries are handled internally by the ExtendedResolver
    aHeadResolver.setRetries (aKey.getMaxRetries ());
    if (aKey.hasTimeout ())
      ResolverHelper.setTimeout (aHeadResolver, aKey.getTimeout ());
    aHeadResolver.setTCP (aKey.isTcp ());
    return DnsSecHelper.createValidatingResolver (aHeadResolver, aKey.getTrustAnchors (), aKey.getKeyCacheMaxTtl ());
  }

  /**
   * @return The maximum number of cached validating resolvers. Always &gt; 0.
   */
  @Nonnegative
  public final int getMaxSize ()
  {
    return m_nMaxSize;
  }

  /**
   * @return The number of currently cached validating resolvers. Always &ge; 0.
   */
  @Nonnegative
  public int getSize ()
  {
    return m_aRWLock.readLockedInt (m_aMap::size);
  }

  @NonNull
  public ValidatingResolver getValidatingResolver (@NonNull final DnsSecValidatingResolverKey aKey) throws IOException
  {
    ValueEnforcer.notNull (aKey, "Key");

    // Write lock, because of the access order of the map. Creation is cheap, as it does not
    // perform any network operation
    return m_aRWLock.writeLockedGetThrowing (() -> {
      ValidatingResolver ret = m_aMap.get (aKey);
      if (ret == null)
      {
        ret = _createValidatingResolver (aKey);
        m_aMap.put (aKey, ret);
      }
      return ret;
    });
  }

  public void clear ()
  {
    m_aRWLock.writeLocked (m_aMap::clear);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("MaxSize", m_nMaxSize).append ("Size", getSize ()).getToString ();
  }

  /**
   * @return The global default cache, used by all lookups that do not specify a cache. Never
   *         <code>null</code>.
   */
  @NonNull
  public static IDnsSecValidatingResolverCache getDefaultInstance ()
  {
    return RW_LOCK.readLockedGet (() -> s_aDefaultInstance);
  }

  /**
   * Replace the global default cache.
   *
   * @param aCache
   *        The new default cache. May not be <code>null</code>.
   */
  public static void setDefaultInstance (@NonNull final IDnsSecValidatingResolverCache aCache)
  {
    ValueEnforcer.notNull (aCache, "Cache");
    RW_LOCK.writeLocked (() -> s_aDefaultInstance = aCache);
  }
}
