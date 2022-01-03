/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.version.Version;

/**
 * Special browser info for Web Spider user agents.
 *
 * @author Philip Helger
 */
public class BrowserInfoMobile extends BrowserInfo
{
  /** Is it not */
  public static final BrowserInfoMobile IS_IT_NOT_MOBILE = new BrowserInfoMobile ();

  private final String m_sUA;

  private BrowserInfoMobile ()
  {
    m_sUA = null;
  }

  public BrowserInfoMobile (@Nullable final String sUA)
  {
    super (EBrowserType.MOBILE, new Version (0));
    m_sUA = sUA;
  }

  @Override
  @Nullable
  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return m_sUA;
  }

  @Override
  public String toString ()
  {
    if (isItNot ())
      return new ToStringGenerator (null).append ("isIt", "not").getToString ();
    return ToStringGenerator.getDerived (super.toString ()).appendIfNotNull ("info", m_sUA).getToString ();
  }
}
