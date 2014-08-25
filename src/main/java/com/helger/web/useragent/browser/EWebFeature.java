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

import javax.annotation.Nonnull;

import com.helger.commons.version.Version;

/**
 * Web feature detection based on user interface
 *
 * @author Philip Helger
 */
public enum EWebFeature
{
  INLINE_IMAGES
  {
    @Override
    public boolean isSupported (@Nonnull final BrowserInfo aBI)
    {
      switch (aBI.getBrowserType ())
      {
        case OPERA:
          return aBI.getVersion ().isGreaterOrEqualThan (V72);
        case FIREFOX:
        case CHROME:
        case GECKO:
        case SAFARI:
          return true;
        case IE:
          return aBI.getVersion ().isGreaterOrEqualThan (V80);
        default:
          return false;
      }
    }
  };

  public static final Version V72 = new Version (7, 2);
  public static final Version V80 = new Version (8);

  /**
   * Check if this feature is supported on the current browser.
   *
   * @param aBI
   *        Thr browser information to use
   * @return <code>true</code> if it is supported, <code>false</code> otherwise.
   */
  public abstract boolean isSupported (@Nonnull BrowserInfo aBI);
}
