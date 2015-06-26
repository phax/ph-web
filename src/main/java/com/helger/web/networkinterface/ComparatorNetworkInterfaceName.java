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
package com.helger.web.networkinterface;

import java.net.NetworkInterface;

import javax.annotation.Nonnull;

import com.helger.commons.compare.AbstractComparator;

/**
 * Comparator to compare {@link NetworkInterface} objects by their name.
 *
 * @author Philip Helger
 */
public class ComparatorNetworkInterfaceName extends AbstractComparator <NetworkInterface>
{
  /**
   * Comparator with default sort order and no nested comparator.
   */
  public ComparatorNetworkInterfaceName ()
  {}

  @Override
  protected int mainCompare (@Nonnull final NetworkInterface aElement1, @Nonnull final NetworkInterface aElement2)
  {
    return aElement1.getName ().compareTo (aElement2.getName ());
  }
}
