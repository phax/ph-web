/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.servlet.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;

/**
 * The possible values for the X-Frame-Options HTTP header.
 *
 * @author Philip Helger
 * @since 6.0.5
 */
public enum EXFrameOptionType implements IHasID <String>
{
  /**
   * The page cannot be displayed in a frame, regardless of the site attempting
   * to do so.
   */
  DENY (CHttpHeader.VALUE_DENY),
  /**
   * The page can only be displayed in a frame on the same origin as the page
   * itself.
   */
  SAMEORIGIN (CHttpHeader.VALUE_SAMEORIGIN),
  /**
   * The page can only be displayed in a frame on the specified origin.
   */
  ALLOW_FROM (CHttpHeader.VALUE_ALLOW_FROM);

  public static final EXFrameOptionType DEFAULT = SAMEORIGIN;

  private final String m_sID;

  EXFrameOptionType (@Nonnull @Nonempty final String sID)
  {
    m_sID = sID;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  public boolean isURLRequired ()
  {
    return this == ALLOW_FROM;
  }

  @Nullable
  public static EXFrameOptionType getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EXFrameOptionType.class, sID);
  }
}
