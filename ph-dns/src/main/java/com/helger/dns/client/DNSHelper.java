/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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
package com.helger.dns.client;

import java.security.Security;

import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.system.SystemProperties;

@Immutable
public final class DNSHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DNSHelper.class);

  @PresentForCodeCoverage
  private static final DNSHelper s_aInstance = new DNSHelper ();

  private DNSHelper ()
  {}

  /**
   * Set special DNS client properties that have influence on the DNS client
   * behavior. This method should be called as soon as possible on startup. In
   * most cases it may be beneficiary if the respective system properties are
   * provided as system properties on the commandline!
   *
   * @param nSeconds
   *        DNS client caching time in seconds.
   */
  public static void setDNSCacheTime (final int nSeconds)
  {
    final String sValue = Integer.toString (nSeconds);
    try
    {
      Security.setProperty ("networkaddress.cache.ttl", sValue);
    }
    catch (final SecurityException ex)
    {
      LOGGER.warn ("Failed to set Security property 'networkaddress.cache.ttl' to '" + sValue + "'");
    }
    try
    {
      Security.setProperty ("networkaddress.cache.negative.ttl", sValue);
    }
    catch (final SecurityException ex)
    {
      LOGGER.warn ("Failed to set Security property 'networkaddress.cache.negative.ttl' to '" + sValue + "'");
    }
    SystemProperties.setPropertyValue ("disableWSAddressCaching", nSeconds == 0);
  }
}
