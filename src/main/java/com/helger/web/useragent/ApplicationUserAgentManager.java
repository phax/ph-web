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
package com.helger.web.useragent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotations.Nonempty;
import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.microdom.reader.XMLListHandler;
import com.helger.commons.string.StringHelper;

@Immutable
public final class ApplicationUserAgentManager
{
  private static Set <String> s_aSet = new HashSet <String> ();

  static
  {
    _readList ("codelists/appuseragents.xml");
  }

  @PresentForCodeCoverage
  @SuppressWarnings ("unused")
  private static final ApplicationUserAgentManager s_aInstance = new ApplicationUserAgentManager ();

  private ApplicationUserAgentManager ()
  {}

  private static void _readList (@Nonnull @Nonempty final String sPath)
  {
    final List <String> aList = new ArrayList <String> ();
    if (XMLListHandler.readList (new ClassPathResource (sPath), aList).isFailure ())
      throw new IllegalStateException ("Failed to read " + sPath);
    s_aSet.addAll (aList);
  }

  @Nullable
  public static String getFromUserAgent (@Nullable final String sFullUserAgent)
  {
    if (StringHelper.hasText (sFullUserAgent))
      for (final String sUAPart : s_aSet)
        if (sFullUserAgent.contains (sUAPart))
          return sUAPart;
    return null;
  }
}
