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
package com.helger.web.servlets.scope;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.helger.web.scope.mgr.WebScopeManager;

/**
 * A default implementation of a web scope aware {@link ServletContextListener}
 * and {@link HttpSessionListener}. Manages global web scopes and session web
 * scopes.
 *
 * @author Philip Helger
 */
public class WebScopeListener implements ServletContextListener, HttpSessionListener
{
  @OverridingMethodsMustInvokeSuper
  public void contextInitialized (@Nonnull final ServletContextEvent aEvent)
  {
    // Init the global scope
    WebScopeManager.onGlobalBegin (aEvent.getServletContext ());
  }

  @OverridingMethodsMustInvokeSuper
  public void contextDestroyed (@Nonnull final ServletContextEvent aEvent)
  {
    // End the global scope
    WebScopeManager.onGlobalEnd ();
  }

  @OverridingMethodsMustInvokeSuper
  public void sessionCreated (@Nonnull final HttpSessionEvent aEvent)
  {
    // Create a new session
    WebScopeManager.onSessionBegin (aEvent.getSession ());
  }

  @OverridingMethodsMustInvokeSuper
  public void sessionDestroyed (@Nonnull final HttpSessionEvent aEvent)
  {
    // End an existing session
    WebScopeManager.onSessionEnd (aEvent.getSession ());
  }
}
