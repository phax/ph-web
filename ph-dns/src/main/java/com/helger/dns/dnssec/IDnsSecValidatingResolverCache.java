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

import org.jspecify.annotations.NonNull;
import org.xbill.DNS.dnssec.ValidatingResolver;

/**
 * A cache for DNSSEC {@link ValidatingResolver} instances. Each validating resolver contains the
 * cache of the validated DNSSEC keys, so reusing it avoids validating the chain of trust from the
 * DNS root on every lookup. Implementations must be thread-safe.
 *
 * @author Philip Helger
 * @since 11.4.7
 */
public interface IDnsSecValidatingResolverCache
{
  /**
   * Get the validating resolver for the provided settings, creating it if necessary. The returned
   * resolver may be shared between threads and must not be modified.
   *
   * @param aKey
   *        The settings of the validating resolver. May not be <code>null</code>.
   * @return The validating resolver. Never <code>null</code>.
   * @throws IOException
   *         If the validating resolver could not be created (e.g. invalid trust anchors)
   */
  @NonNull
  ValidatingResolver getValidatingResolver (@NonNull DnsSecValidatingResolverKey aKey) throws IOException;

  /**
   * Remove all cached validating resolvers, so that the next lookups start with an empty key cache.
   */
  void clear ();
}
