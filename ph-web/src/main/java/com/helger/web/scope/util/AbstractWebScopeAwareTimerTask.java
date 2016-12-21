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
package com.helger.web.scope.util;

import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.servlet.mock.MockHttpServletResponse;
import com.helger.servlet.mock.OfflineHttpServletRequest;
import com.helger.web.scope.mgr.WebScopeManager;

/**
 * An abstract base class that handles scopes correctly for {@link TimerTask}
 * implementations.
 *
 * @author Philip Helger
 */
public abstract class AbstractWebScopeAwareTimerTask extends TimerTask
{
  private final ServletContext m_aSC;
  private final String m_sApplicationID;

  public AbstractWebScopeAwareTimerTask (@Nonnull @Nonempty final String sApplicationID)
  {
    // Don't use "MockServletContext" to avoid that the global Servlet Context
    // Path is overriden!
    this (WebScopeManager.getGlobalScope ().getServletContext (), sApplicationID);
  }

  public AbstractWebScopeAwareTimerTask (@Nonnull final ServletContext aSC,
                                         @Nonnull @Nonempty final String sApplicationID)
  {
    m_aSC = ValueEnforcer.notNull (aSC, "ServletContext");
    m_sApplicationID = ValueEnforcer.notEmpty (sApplicationID, "ApplicationID");
  }

  @Nonnull
  @Nonempty
  public String getApplicationID ()
  {
    return m_sApplicationID;
  }

  /**
   * Implement this method to perform the actual task. It is called within a
   * valid web request scope.
   */
  protected abstract void onRunTask ();

  @Override
  public final void run ()
  {
    // Create the scope
    WebScopeManager.onRequestBegin (m_sApplicationID,
                                    new OfflineHttpServletRequest (m_aSC, false),
                                    new MockHttpServletResponse ());
    try
    {
      onRunTask ();
    }
    finally
    {
      // Don't forget to end the scope
      WebScopeManager.onRequestEnd ();
    }
  }
}
