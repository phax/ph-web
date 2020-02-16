/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import java.io.Serializable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.name.IHasName;
import com.helger.commons.string.StringHelper;
import com.helger.commons.text.IHasDescription;

/**
 * Interface describing a single network port.
 *
 * @author Philip Helger
 */
public interface INetworkPort extends IHasName, IHasDescription, Serializable
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
    return StringHelper.hasText (getName ());
  }

  /**
   * @return Description of this ports usage. May not be <code>null</code> but
   *         maybe empty.
   */
  @Nonnull
  String getDescription ();
}
