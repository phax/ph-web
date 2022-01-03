/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
package com.helger.web.scope.spi;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.scope.IGlobalScope;
import com.helger.scope.spi.IGlobalScopeSPI;

@IsSPIImplementation
public final class MockGlobalWebScopeSPI extends AbstractWebScopeSPI implements IGlobalScopeSPI
{
  public void onGlobalScopeBegin (final IGlobalScope aScope)
  {
    onBegin ();
  }

  public void onGlobalScopeEnd (final IGlobalScope aScope)
  {
    onEnd ();
  }
}
