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
package com.helger.web.scopes.singleton;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.hashcode.HashCodeGenerator;

/**
 * Mock implementation of {@link AbstractRequestWebSingleton}
 *
 * @author Philip Helger
 */
public final class MockRequestWebSingleton extends AbstractRequestWebSingleton
{
  private int i = 0;

  @Deprecated
  @UsedViaReflection
  public MockRequestWebSingleton ()
  {}

  @Nonnull
  public static MockRequestWebSingleton getInstance ()
  {
    return getRequestSingleton (MockRequestWebSingleton.class);
  }

  public void inc ()
  {
    i++;
  }

  public int get ()
  {
    return i;
  }

  // For testing!
  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!(o instanceof MockRequestWebSingleton))
      return false;
    return i == ((MockRequestWebSingleton) o).i;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (i).getHashCode ();
  }
}
