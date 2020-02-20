/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.helger.httpclient.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of {@link TrustStrategy} that trust all callers.
 *
 * @author Philip Helger
 * @since 9.1.9
 */
public class TrustStrategyTrustAll implements TrustStrategy
{
  private static final Logger LOGGER = LoggerFactory.getLogger (TrustStrategyTrustAll.class);

  public boolean isTrusted (final X509Certificate [] aChain, final String sAuthType) throws CertificateException
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("isTrusted(" + Arrays.toString (aChain) + ", " + sAuthType + ")");
    return true;
  }
}