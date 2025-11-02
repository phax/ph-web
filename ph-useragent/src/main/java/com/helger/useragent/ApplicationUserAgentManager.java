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
package com.helger.useragent;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsCollection;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsSet;
import com.helger.io.resource.ClassPathResource;
import com.helger.xml.microdom.util.XMLListHandler;

@Immutable
public final class ApplicationUserAgentManager
{
  private static final ICommonsSet <String> SET = new CommonsHashSet <> ();

  static
  {
    _readList ("codelists/appuseragents.xml");
  }

  @PresentForCodeCoverage
  private static final ApplicationUserAgentManager INSTANCE = new ApplicationUserAgentManager ();

  private ApplicationUserAgentManager ()
  {}

  private static void _readList (@NonNull @Nonempty final String sPath)
  {
    final ICommonsList <String> aList = new CommonsArrayList <> ();
    if (XMLListHandler.readList (new ClassPathResource (sPath), aList).isFailure ())
      throw new IllegalStateException ("Failed to read " + sPath);
    SET.addAll (aList);
  }

  @Nullable
  public static String getFromUserAgent (@Nullable final String sFullUserAgent)
  {
    if (StringHelper.isEmpty (sFullUserAgent))
      return null;
    return SET.findFirst (sFullUserAgent::contains);
  }

  @NonNull
  @ReturnsMutableCopy
  public static ICommonsCollection <String> getAllItems ()
  {
    return SET.getClone ();
  }
}
