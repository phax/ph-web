/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
import com.helger.useragent.spider.WebSpiderInfo;

/**
 * Special browser info for Web Spider user agents.
 *
 * @author Philip Helger
 */
public class BrowserInfoSpider extends BrowserInfo
{
  /** Is it not */
  public static final BrowserInfoSpider IS_IT_NOT_SPIDER = new BrowserInfoSpider ();

  private final WebSpiderInfo m_aWebSpiderInfo;

  private BrowserInfoSpider ()
  {
    m_aWebSpiderInfo = null;
  }

  public BrowserInfoSpider (@Nullable final WebSpiderInfo aWebSpiderInfo)
  {
    super (EBrowserType.SPIDER, new Version (0));
    m_aWebSpiderInfo = aWebSpiderInfo;
  }

  @Nullable
  public String getSearchEngineName ()
  {
    return m_aWebSpiderInfo == null ? null : m_aWebSpiderInfo.getName ();
  }

  @Override
  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return getSearchEngineName ();
  }

  @Override
  public String toString ()
  {
    if (isItNot ())
      return new ToStringGenerator (null).append ("isIt", "not").getToString ();
    return ToStringGenerator.getDerived (super.toString ()).appendIfNotNull ("info", m_aWebSpiderInfo).getToString ();
  }
}
