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

import javax.annotation.Nonnull;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 * Provider interface for {@link CloseableHttpClient} objects.
 *
 * @author Philip Helger
 * @since 8.7.2
 */
@FunctionalInterface
public interface IHttpClientProvider
{
  /**
   * Create a new closeable HttpClient object.
   * 
   * @return A new object. May not be <code>null</code>.
   */
  @Nonnull
  CloseableHttpClient createHttpClient ();
}
