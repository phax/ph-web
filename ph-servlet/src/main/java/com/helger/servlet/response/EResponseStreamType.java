/**
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
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;

/**
 * Determine the response stream type to be used.
 *
 * @author Philip Helger
 */
public enum EResponseStreamType implements IHasID <String>
{
  PLAIN ("plain"),
  GZIP ("gzip"),
  DEFLATE ("deflate");

  private final String m_sID;

  EResponseStreamType (@Nonnull @Nonempty final String sID)
  {
    m_sID = sID;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return <code>true</code> if the response stream type is uncompressed.
   */
  public boolean isUncompressed ()
  {
    return this == PLAIN;
  }

  /**
   * @return <code>true</code> if the response stream type is compressed.
   */
  public boolean isCompressed ()
  {
    return this == GZIP || this == DEFLATE;
  }

  @Nullable
  public static EResponseStreamType getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EResponseStreamType.class, sID);
  }
}
