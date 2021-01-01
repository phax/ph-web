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
package com.helger.xservlet.servletstatus;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.text.display.IHasDisplayText;

/**
 * Defines the different lifecycle status of a servlet.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public enum EServletStatus implements IHasDisplayText
{
  // Constructor called
  CONSTRUCTED (EServletStatusText.CONSTRUCTED),
  // Init called
  INITED (EServletStatusText.INITED),
  // Destroy called
  DESTROYED (EServletStatusText.DESTROYED);

  private final IHasDisplayText m_aText;

  EServletStatus (@Nonnull final EServletStatusText aText)
  {
    m_aText = aText;
  }

  @Nullable
  public String getDisplayText (@Nonnull final Locale aContentLocale)
  {
    return m_aText.getDisplayText (aContentLocale);
  }

  public static boolean isValidSuccessorOf (@Nullable final EServletStatus eOldStatus, @Nonnull final EServletStatus eNewStatus)
  {
    if (eOldStatus == null)
    {
      // CONSTRUCTED is the first state
      return eNewStatus == CONSTRUCTED;
    }
    if (eOldStatus == CONSTRUCTED)
    {
      // Can only be followed by INITED
      return eNewStatus == INITED;
    }
    if (eOldStatus == INITED)
    {
      // Destroyed upon shutdown or
      // Constructed if initialization fails
      return eNewStatus == DESTROYED || eNewStatus == EServletStatus.CONSTRUCTED;
    }
    return false;
  }
}
