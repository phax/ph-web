/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.text.display.IHasDisplayText;

/**
 * Contains the major web browsers on the market.
 *
 * @author Philip Helger
 */
public enum EBrowserType implements IHasID <String>, IHasDisplayText
{
  FIREFOX ("firefox", EBrowserText.FIREFOX),
  IE ("ie", EBrowserText.IE),
  OPERA ("opera", EBrowserText.OPERA),
  SAFARI ("safari", EBrowserText.SAFARI),
  CHROME ("chrome", EBrowserText.CHROME),
  LYNX ("lynx", EBrowserText.LYNX),
  KONQUEROR ("konqueror", EBrowserText.KONQUEROR),
  VIVALDI ("vivaldi", EBrowserText.VIVALDI),
  GECKO ("gecko", EBrowserText.GECKO),
  WEBKIT ("webkit", EBrowserText.WEBKIT),
  MOBILE ("mobile", EBrowserText.MOBILE),
  SPIDER ("spider", EBrowserText.SPIDER),
  APPLICATION ("generic", EBrowserText.APPLICATION);

  private final String m_sID;
  private final IHasDisplayText m_aDisplayName;

  EBrowserType (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final IHasDisplayText aDisplayName)
  {
    m_sID = sID;
    m_aDisplayName = aDisplayName;
  }

  @Nonnull
  public String getID ()
  {
    return m_sID;
  }

  @Nonnull
  public String getDisplayText (final Locale aContentLocale)
  {
    return m_aDisplayName.getDisplayText (aContentLocale);
  }

  @Nullable
  public static EBrowserType getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EBrowserType.class, sID);
  }
}
