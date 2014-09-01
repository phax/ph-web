/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.servlet;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.url.ISimpleURL;

public class ServletRedirectIndicatorException extends RuntimeException
{
  private final ISimpleURL m_aURL;
  private final Map <String, Object> m_aRequestParams;

  public ServletRedirectIndicatorException (@Nonnull final ISimpleURL aURL)
  {
    this (aURL, (Map <String, Object>) null);
  }

  public ServletRedirectIndicatorException (@Nonnull final ISimpleURL aURL,
                                            @Nullable final Map <String, Object> aRequestParams)
  {
    m_aURL = ValueEnforcer.notNull (aURL, "URL");
    m_aRequestParams = aRequestParams;
  }

  @Nonnull
  public ISimpleURL getURL ()
  {
    return m_aURL;
  }

  @Nonnull
  @ReturnsMutableCopy
  public Map <String, Object> getRequestParams ()
  {
    return ContainerHelper.newMap (m_aRequestParams);
  }
}
