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
package com.helger.useragent.browser;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.text.display.IHasDisplayText;
import com.helger.commons.version.Version;

/**
 * Represents information about a certain browser.
 *
 * @author Philip Helger
 */
@Immutable
public class BrowserInfo implements IHasDisplayText
{
  /** Is it not */
  public static final BrowserInfo IS_IT_NOT = new BrowserInfo ();

  private final EBrowserType m_eBrowserType;
  private final Version m_aVersion;

  /**
   * Only to indicate that this is not the desired browser.
   */
  protected BrowserInfo ()
  {
    m_eBrowserType = null;
    m_aVersion = new Version (0);
  }

  public BrowserInfo (@Nonnull final EBrowserType eBrowserType, @Nonnull final Version aVersion)
  {
    m_eBrowserType = ValueEnforcer.notNull (eBrowserType, "BrowserType");
    m_aVersion = ValueEnforcer.notNull (aVersion, "Version");
  }

  public final boolean isIt ()
  {
    return m_eBrowserType != null;
  }

  public final boolean isItNot ()
  {
    return m_eBrowserType == null;
  }

  @Nullable
  public final EBrowserType getBrowserType ()
  {
    return m_eBrowserType;
  }

  @Nullable
  @OverrideOnDemand
  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return m_eBrowserType == null ? null
                                  : (m_eBrowserType.getDisplayText (aContentLocale) + " " + m_aVersion.getAsString ());
  }

  @Nonnull
  public final Version getVersion ()
  {
    return m_aVersion;
  }

  @Override
  public String toString ()
  {
    if (isItNot ())
      return new ToStringGenerator (null).append ("isIt", "not").toString ();
    return new ToStringGenerator (null).appendIfNotNull ("type", m_eBrowserType)
                                       .appendIfNotNull ("version", m_aVersion)
                                       .toString ();
  }
}
