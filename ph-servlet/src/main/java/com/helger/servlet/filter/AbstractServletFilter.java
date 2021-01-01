/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.servlet.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.string.ToStringGenerator;

/**
 * An abstract {@link Filter} implementation that performs nothing by default
 *
 * @author Philip Helger
 * @since 8.8.0
 */
public abstract class AbstractServletFilter implements Filter
{
  private FilterConfig m_aFilterConfig;

  /**
   * @return The {@link FilterConfig} object used in the initialization. May
   *         only be <code>null</code> before {@link #init(FilterConfig)} is
   *         called.
   */
  @Nullable
  public final FilterConfig getFilterConfig ()
  {
    return m_aFilterConfig;
  }

  public final void init (@Nonnull final FilterConfig aFilterConfig) throws ServletException
  {
    m_aFilterConfig = aFilterConfig;
    init ();
  }

  /**
   * Init this filter
   *
   * @throws ServletException
   *         In case of an error
   */
  @OverrideOnDemand
  public void init () throws ServletException
  {}

  @OverrideOnDemand
  public void destroy ()
  {}

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("FilterConfig", m_aFilterConfig).getToString ();
  }
}
