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
package com.helger.xservlet.forcedredirect;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.url.ISimpleURL;

/**
 * This is a hack to allow for easy POST/Redirect/GET pattern implementation.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class ForcedRedirectException extends RuntimeException
{
  public static final String DEFAULT_MENU_ITEM_ID = "$default$";

  private final String m_sSourceMenuItemID;
  private final ISimpleURL m_aRedirectTargetURL;
  private final Serializable m_aContent;

  /**
   * Constructor
   *
   * @param sSourceMenuItemID
   *        The source menu item ID that triggered the PRG.
   * @param aRedirectTargetURL
   *        The redirect target URL. May not be <code>null</code>.
   * @param aContent
   *        The additional content to be displayed upon the next get. May be
   *        <code>null</code>. Usually this is an <code>IHCNode</code> but this
   *        class is not accessible from here!
   */
  public ForcedRedirectException (@Nonnull @Nonempty final String sSourceMenuItemID,
                                  @Nonnull final ISimpleURL aRedirectTargetURL,
                                  @Nullable final Serializable aContent)
  {
    m_sSourceMenuItemID = ValueEnforcer.notEmpty (sSourceMenuItemID, "SourceMenuItemID");
    m_aRedirectTargetURL = ValueEnforcer.notNull (aRedirectTargetURL, "RedirectTargetURL");
    m_aContent = aContent;
  }

  /**
   * @return The source menu item ID that triggered the PRG as specified in the
   *         constructor. Never <code>null</code>.
   */
  @Nonnull
  @Nonempty
  public String getSourceMenuItemID ()
  {
    return m_sSourceMenuItemID;
  }

  /**
   * @return The redirect target URL as specified in the constructor. Never
   *         <code>null</code>.
   */
  @Nonnull
  public ISimpleURL getRedirectTargetURL ()
  {
    return m_aRedirectTargetURL;
  }

  /**
   * @return The content to be displayed when the GET is executed. May be
   *         <code>null</code>. Usually this is an <code>IHCNode</code> but this
   *         class is not accessible from here!
   */
  @Nullable
  public Serializable getContent ()
  {
    return m_aContent;
  }
}
