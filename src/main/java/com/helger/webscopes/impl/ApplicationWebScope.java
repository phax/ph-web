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
package com.helger.webscopes.impl;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.helger.commons.annotations.Nonempty;
import com.helger.scopes.impl.ApplicationScope;
import com.helger.webscopes.domain.IApplicationWebScope;

/**
 * Represents a single application web scope. It has no additional features
 * compared to the regular {@link ApplicationScope} but implements the specific
 * {@link IApplicationWebScope} interface.
 * 
 * @author Philip Helger
 */
@ThreadSafe
public class ApplicationWebScope extends ApplicationScope implements IApplicationWebScope
{
  /**
   * Create a new application web scope with the given ID.
   * 
   * @param sScopeID
   *        The scope ID to be used. May neither be <code>null</code> nor empty.
   */
  public ApplicationWebScope (@Nonnull @Nonempty final String sScopeID)
  {
    super (sScopeID);
  }
}
