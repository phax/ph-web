/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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

import com.helger.commons.annotations.Translatable;
import com.helger.commons.name.IHasDisplayText;
import com.helger.commons.text.ITextProvider;
import com.helger.commons.text.impl.TextProvider;
import com.helger.commons.text.resolve.DefaultTextResolver;

@Translatable
public enum EBrowserText implements IHasDisplayText
{
  FIREFOX ("Mozilla Firefox", "Mozilla Firefox"),
  IE ("Microsoft Internet Explorer", "Microsoft Internet Explorer"),
  OPERA ("Opera", "Opera"),
  SAFARI ("Apple Safari", "Apple Safari"),
  CHROME ("Google Chrome", "Google Chrome"),
  LYNX ("Lynx", "Lynx"),
  KONQUEROR ("Konqueror", "Konqueror"),
  GECKO ("Gecko basierend", "Gecko based"),
  WEBKIT ("WebKit basierend", "WebKit based"),
  MOBILE ("Mobiler Browser", "Mobile browser"),
  SPIDER ("Web Spieder", "Web Spider"),
  APPLICATION ("Anwendung oder API", "Application or API"),
  IE_COMPATIBILITY_MODE (" (Kompatibilitätsmodus)", " (compatibility mode)");

  private final ITextProvider m_aTP;

  private EBrowserText (final String sDE, final String sEN)
  {
    m_aTP = TextProvider.create_DE_EN (sDE, sEN);
  }

  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return DefaultTextResolver.getText (this, m_aTP, aContentLocale);
  }
}
