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
package com.helger.network.port;

import com.helger.annotation.Nonnegative;
import com.helger.base.name.IHasName;
import com.helger.base.string.StringHelper;
import com.helger.text.IHasDescription;

import jakarta.annotation.Nonnull;

/**
 * Interface describing a single network port.
 *
 * @author Philip Helger
 */
public interface INetworkPort extends IHasName, IHasDescription
{
  /**
   * @return The numeric port number
   */
  @Nonnegative
  int getPort ();

  /**
   * @return The underlying network protocol (TCP / UDP)
   */
  @Nonnull
  ENetworkProtocol getProtocol ();

  /**
   * @return Port name. May not be <code>null</code> but maybe empty.
   */
  @Nonnull
  String getName ();

  default boolean hasName ()
  {
    return StringHelper.isNotEmpty (getName ());
  }

  /**
   * @return Description of this ports usage. May not be <code>null</code> but maybe empty.
   */
  @Nonnull
  String getDescription ();
}
