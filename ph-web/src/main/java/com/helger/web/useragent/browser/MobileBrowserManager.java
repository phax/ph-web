/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.useragent.browser;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.CommonsHashSet;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsSet;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.microdom.util.XMLListHandler;
import com.helger.commons.string.StringHelper;

@Immutable
public final class MobileBrowserManager
{
  private static ICommonsSet <String> s_aSet = new CommonsHashSet <> ();

  static
  {
    _readList ("codelists/mobileuseragents.xml");
  }

  @PresentForCodeCoverage
  private static final MobileBrowserManager s_aInstance = new MobileBrowserManager ();

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
    s_aSet.addAllMapped (aList, sItem -> _unify (sItem));
  }

  @Nullable
  public static String getFromUserAgent (@Nullable final String sFullUserAgent)
  {
    if (StringHelper.hasNoText (sFullUserAgent))
      return null;

    final String sUnifiedUA = _unify (sFullUserAgent);
    return s_aSet.findFirst (sUAPart -> sUnifiedUA.contains (sUAPart));
  }
}