/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.web.progress;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.lang.ServiceLoaderHelper;

/**
 * SPI handler for {@link IProgressListenerProviderSPI} implementations
 *
 * @author Philip Helger
 */
public final class ProgressListenerProvider
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ProgressListenerProvider.class);

  private static final IProgressListenerProviderSPI s_aProgressListenerProvider;

  static
  {
    s_aProgressListenerProvider = ServiceLoaderHelper.getFirstSPIImplementation (IProgressListenerProviderSPI.class);
    if (s_aProgressListenerProvider != null)
      s_aLogger.info ("Using progress listener provider " + s_aProgressListenerProvider);
  }

  private ProgressListenerProvider ()
  {}

  @Nullable
  public static IProgressListenerProviderSPI getProgressListenerProvider ()
  {
    return s_aProgressListenerProvider;
  }

  @Nullable
  public static IProgressListener getProgressListener ()
  {
    return s_aProgressListenerProvider == null ? null : s_aProgressListenerProvider.getProgressListener ();
  }
}
