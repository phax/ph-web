/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.xservlet.servletstatus;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.text.display.IHasDisplayText;

public enum EServletStatus implements IHasDisplayText
{
  CONSTRUCTED (EServletStatusText.CONSTRUCTED),
  INITED (EServletStatusText.INITED),
  DESTROYED (EServletStatusText.DESTROYED);

  private final IHasDisplayText m_aText;

  private EServletStatus (@Nonnull final EServletStatusText aText)
  {
    m_aText = aText;
  }

  @Nullable
  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return m_aText.getDisplayText (aContentLocale);
  }

  @Nullable
  public static EServletStatus getSuccessorOf (@Nullable final EServletStatus eStatus)
  {
    if (eStatus == null)
      return CONSTRUCTED;
    if (eStatus == CONSTRUCTED)
      return INITED;
    if (eStatus == INITED)
      return DESTROYED;
    return null;
  }
}
