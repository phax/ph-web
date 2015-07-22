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
package com.helger.web.scope.mgr;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * The meta scope factory holding both the factory for non-web scopes as well as
 * the factory for web-scopes. See
 * {@link com.helger.commons.scope.mgr.MetaScopeFactory}
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class MetaWebScopeFactory
{
  @PresentForCodeCoverage
  private static final MetaWebScopeFactory s_aInstance = new MetaWebScopeFactory ();

  private static IWebScopeFactory s_aWebScopeFactory = new DefaultWebScopeFactory ();

  private MetaWebScopeFactory ()
  {}

  /**
   * Set the default web scope factory
   *
   * @param aWebScopeFactory
   *        The scope factory to use. May not be <code>null</code>.
   */
  public static void setWebScopeFactory (@Nonnull final IWebScopeFactory aWebScopeFactory)
  {
    ValueEnforcer.notNull (aWebScopeFactory, "WebScopeFactory");
    s_aWebScopeFactory = aWebScopeFactory;
  }

  /**
   * @return The scope factory for web scopes. Never <code>null</code>.
   */
  @Nonnull
  public static IWebScopeFactory getWebScopeFactory ()
  {
    return s_aWebScopeFactory;
  }
}
