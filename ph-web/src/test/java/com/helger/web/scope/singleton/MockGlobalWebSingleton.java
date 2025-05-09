/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.web.scope.singleton;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.scope.IScope;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Mock implementation of {@link AbstractGlobalWebSingleton}
 *
 * @author Philip Helger
 */
public final class MockGlobalWebSingleton extends AbstractGlobalWebSingleton
{
  static int s_nCtorCount = 0;
  static int s_nDtorCount = 0;

  @Deprecated (forRemoval = false)
  @UsedViaReflection
  @SuppressFBWarnings ("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  public MockGlobalWebSingleton ()
  {
    s_nCtorCount++;
  }

  @Nonnull
  public static MockGlobalWebSingleton getInstance ()
  {
    return getGlobalSingleton (MockGlobalWebSingleton.class);
  }

  @Override
  protected void onDestroy (@Nonnull final IScope aScopeInDestruction) throws Exception
  {
    s_nDtorCount++;
  }
}
