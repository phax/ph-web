/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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

import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.version.Version;

/**
 * Special browser info for Internet Explorer.
 *
 * @author Philip Helger
 */
public class BrowserInfoIE extends BrowserInfo
{
  /** Is it not */
  public static final BrowserInfoIE IS_IT_NOT_IE = new BrowserInfoIE ();

  private final boolean m_bIsIECompatibilityMode;

  private BrowserInfoIE ()
  {
    m_bIsIECompatibilityMode = false;
  }

  public BrowserInfoIE (@Nonnull final Version aVersion, final boolean bIsIECompatibilityMode)
  {
    super (EBrowserType.IE, aVersion);
    m_bIsIECompatibilityMode = bIsIECompatibilityMode;
  }

  /**
   * @return <code>true</code> if any IE compatibility mode is active
   */
  public boolean isIECompatibilityMode ()
  {
    return m_bIsIECompatibilityMode;
  }

  @Override
  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return super.getDisplayText (aContentLocale) +
           (isIECompatibilityMode () ? EBrowserText.IE_COMPATIBILITY_MODE.getDisplayText (aContentLocale) : "");
  }

  @Override
  public String toString ()
  {
    if (isItNot ())
      return new ToStringGenerator (null).append ("isIt", "not").getToString ();
    return ToStringGenerator.getDerived (super.toString ()).append ("ieCompatibilityMode", m_bIsIECompatibilityMode).getToString ();
  }
}
