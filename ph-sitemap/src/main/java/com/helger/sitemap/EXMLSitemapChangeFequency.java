/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.sitemap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.string.StringHelper;

/**
 * The determined change frequency of a sitemap entry
 *
 * @author Philip Helger
 */
public enum EXMLSitemapChangeFequency
{
  ALWAYS ("always"),
  HOURLY ("hourly"),
  DAILY ("daily"),
  WEEKLY ("weekly"),
  MONTHLY ("monthly"),
  YEARLY ("yearly"),
  NEVER ("never");

  private final String m_sText;

  EXMLSitemapChangeFequency (@Nonnull @Nonempty final String sText)
  {
    m_sText = sText;
  }

  @Nonnull
  @Nonempty
  public String getText ()
  {
    return m_sText;
  }

  @Nullable
  public static EXMLSitemapChangeFequency getFromTextOrNull (@Nullable final String sText)
  {
    if (StringHelper.hasNoText (sText))
      return null;
    return EnumHelper.findFirst (EXMLSitemapChangeFequency.class, x -> x.m_sText.equals (sText));
  }
}
