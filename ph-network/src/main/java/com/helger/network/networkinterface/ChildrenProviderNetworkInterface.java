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
package com.helger.network.networkinterface;

import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.collection.IteratorHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsCollection;
import com.helger.commons.hierarchy.IChildrenProvider;
import com.helger.commons.string.ToStringGenerator;

/**
 * Default implementation of {@link IChildrenProvider} for
 * {@link NetworkInterface}.
 *
 * @author Philip Helger
 */
@Immutable
public class ChildrenProviderNetworkInterface implements IChildrenProvider <NetworkInterface>
{
  @Override
  public boolean hasChildren (@Nonnull final NetworkInterface aCurrent)
  {
    return aCurrent.getSubInterfaces ().hasMoreElements ();
  }

  @Nonnegative
  public int getChildCount (@Nonnull final NetworkInterface aCurrent)
  {
    return IteratorHelper.getSize (aCurrent.getSubInterfaces ());
  }

  @Nullable
  public ICommonsCollection <NetworkInterface> getAllChildren (@Nonnull final NetworkInterface aCurrent)
  {
    final Enumeration <NetworkInterface> aSubIFs = aCurrent.getSubInterfaces ();
    return aSubIFs.hasMoreElements () ? new CommonsArrayList <> (aSubIFs) : null;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).getToString ();
  }
}
