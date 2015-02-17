/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.webscopes.fileupload;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotations.UsedViaReflection;
import com.helger.commons.lang.ServiceLoaderUtils;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.fileupload.IProgressListener;
import com.helger.web.fileupload.IProgressListenerProvider;
import com.helger.webscopes.singleton.GlobalWebSingleton;

/**
 * SPI handler for {@link IProgressListenerProvider} implementations
 * 
 * @author Philip Helger
 */
public final class ProgressListenerProvider extends GlobalWebSingleton
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ProgressListenerProvider.class);

  private final IProgressListenerProvider m_aProgressListenerProvider;

  @Deprecated
  @UsedViaReflection
  public ProgressListenerProvider ()
  {
    m_aProgressListenerProvider = ServiceLoaderUtils.getFirstSPIImplementation (IProgressListenerProvider.class);
    if (m_aProgressListenerProvider != null)
      s_aLogger.info ("Using progress listener provider: " + m_aProgressListenerProvider.getClass ().getName ());
  }

  @Nonnull
  public static ProgressListenerProvider getInstance ()
  {
    return getGlobalSingleton (ProgressListenerProvider.class);
  }

  @Nullable
  public IProgressListener getProgressListener ()
  {
    return m_aProgressListenerProvider == null ? null : m_aProgressListenerProvider.getProgressListener ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("progressListenerProvider", m_aProgressListenerProvider)
                            .toString ();
  }
}
