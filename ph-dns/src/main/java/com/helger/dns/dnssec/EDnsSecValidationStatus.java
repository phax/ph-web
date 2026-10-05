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

/**
 * The outcome of a DNSSEC validation of DNS responses.
 *
 * @author Philip Helger
 * @since 11.4.7
 */
public enum EDnsSecValidationStatus
{
  /**
   * No DNSSEC validation took place - either because it was not enabled, or because no DNS response
   * was received at all (e.g. network error).
   */
  NOT_VALIDATED,
  /** All DNS responses were validated successfully along the chain of trust. */
  SECURE,
  /**
   * At least one DNS response was probably unsigned (e.g. the zone is not signed), but no response
   * was bogus.
   */
  INSECURE,
  /**
   * At least one DNS response failed validation (e.g. invalid or missing signatures although the
   * zone is signed, expired signatures, broken chain of trust).
   */
  BOGUS;

  /**
   * @return <code>true</code> only for {@link #SECURE}.
   */
  public boolean isSecure ()
  {
    return this == SECURE;
  }

  /**
   * @return <code>true</code> only for {@link #BOGUS}.
   */
  public boolean isBogus ()
  {
    return this == BOGUS;
  }
}
