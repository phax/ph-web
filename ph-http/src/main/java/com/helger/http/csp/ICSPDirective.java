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
package com.helger.http.csp;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.name.IHasName;
import com.helger.commons.string.StringHelper;

/**
 * A single CSP directive. It's a name-value-pair.
 *
 * @author Philip Helger
 */
public interface ICSPDirective extends IHasName, Serializable
{
  /**
   * @return The name of this directive.
   */
  @Nonnull
  @Nonempty
  String getName ();

  /**
   * @return The value of this directive. May be <code>null</code> or empty.
   */
  @Nullable
  String getValue ();

  default boolean hasValue ()
  {
    return StringHelper.hasText (getValue ());
  }

  @Nonnull
  @Nonempty
  default String getAsString ()
  {
    return StringHelper.getConcatenatedOnDemand (getName (), " ", getValue ());
  }
}
