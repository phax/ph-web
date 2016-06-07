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
package com.helger.web.useragent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.CommonsHashSet;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.collection.ext.ICommonsSet;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.string.StringHelper;
import com.helger.xml.microdom.util.XMLListHandler;

@Immutable
public final class ApplicationUserAgentManager
{
  private static ICommonsSet <String> s_aSet = new CommonsHashSet <> ();

  static
  {
    _readList ("codelists/appuseragents.xml");
  }

  @PresentForCodeCoverage
  private static final ApplicationUserAgentManager s_aInstance = new ApplicationUserAgentManager ();

  private ApplicationUserAgentManager ()
  {}

  private static void _readList (@Nonnull @Nonempty final String sPath)
  {
    final ICommonsList <String> aList = new CommonsArrayList <> ();
    if (XMLListHandler.readList (new ClassPathResource (sPath), aList).isFailure ())
      throw new IllegalStateException ("Failed to read " + sPath);
    s_aSet.addAll (aList);
  }

  @Nullable
  public static String getFromUserAgent (@Nullable final String sFullUserAgent)
  {
    if (StringHelper.hasNoText (sFullUserAgent))
      return null;
    return s_aSet.findFirst (sUAPart -> sFullUserAgent.contains (sUAPart));
  }
}
