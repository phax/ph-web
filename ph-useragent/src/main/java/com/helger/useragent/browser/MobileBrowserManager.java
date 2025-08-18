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
package com.helger.useragent.browser;

import java.util.Locale;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsSet;
import com.helger.io.resource.ClassPathResource;
import com.helger.xml.microdom.util.XMLListHandler;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Immutable
public final class MobileBrowserManager
{
  private static final ICommonsSet <String> SET = new CommonsHashSet <> ();

  static
  {
    _readList ("codelists/mobileuseragents.xml");
  }

  @PresentForCodeCoverage
  private static final MobileBrowserManager INSTANCE = new MobileBrowserManager ();

  private MobileBrowserManager ()
  {}

  @Nonnull
  private static String _unify (@Nonnull final String sItem)
  {
    return sItem.toLowerCase (Locale.US);
  }

  private static void _readList (@Nonnull final String sPath)
  {
    final ICommonsList <String> aList = new CommonsArrayList <> ();
    if (XMLListHandler.readList (new ClassPathResource (sPath), aList).isFailure ())
      throw new IllegalStateException ("Failed to read " + sPath);

    SET.addAllMapped (aList, MobileBrowserManager::_unify);
  }

  @Nullable
  public static String getFromUserAgent (@Nullable final String sFullUserAgent)
  {
    if (StringHelper.isEmpty (sFullUserAgent))
      return null;

    final String sUnifiedUA = _unify (sFullUserAgent);
    return SET.findFirst (sUnifiedUA::contains);
  }
}
