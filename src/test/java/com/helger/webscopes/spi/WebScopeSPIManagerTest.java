/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.webscopes.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.scopes.ScopeUtils;
import com.helger.scopes.domain.ISessionScope;
import com.helger.scopes.mgr.ScopeManager;
import com.helger.scopes.spi.ScopeSPIManager;
import com.helger.webscopes.mock.WebScopeAwareTestSetup;

/**
 * Test class for class {@link ScopeSPIManager}.
 * 
 * @author Philip Helger
 */
public final class WebScopeSPIManagerTest
{
  static
  {
    WebScopeAwareTestSetup.setCoreMockHttpListeners ();
    ScopeUtils.setLifeCycleDebuggingEnabled (true);
  }

  @Test
  public void testGlobalScope ()
  {
    // Create global scope only
    int nPrev = AbstractWebScopeSPI.getBegin ();
    int nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onGlobalBegin ("global");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // End global scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onGlobalEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());
  }

  @Test
  public void testRequestScope ()
  {
    // Create global scope
    int nPrev = AbstractWebScopeSPI.getBegin ();
    int nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onGlobalBegin ("global");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create request scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onRequestBegin ("appid", "scopeid", "sessionid");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // End request scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onRequestEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());

    // End global scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onGlobalEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());
  }

  @Test
  public void testApplicationScope ()
  {
    // Create global scope
    int nPrev = AbstractWebScopeSPI.getBegin ();
    int nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onGlobalBegin ("global");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create request scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onRequestBegin ("appid", "scopeid", "sessionid");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // End request scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onRequestEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());

    // End global scope and application scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onGlobalEnd ();
    assertEquals (nPrev + 2, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 2, AbstractThrowingWebScopeSPI.getEnd ());
  }

  @Test
  public void testApplicationScopes ()
  {
    // Create global scope
    int nPrev = AbstractWebScopeSPI.getBegin ();
    int nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onGlobalBegin ("global");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create request scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onRequestBegin ("appid", "scopeid", "sessionid");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create second application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ("any other blabla");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // End request scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onRequestEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());

    // End global scope and application scopes
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onGlobalEnd ();
    assertEquals (nPrev + 3, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 3, AbstractThrowingWebScopeSPI.getEnd ());
  }

  @Test
  public void testSessionScopes ()
  {
    // Create global scope
    int nPrev = AbstractWebScopeSPI.getBegin ();
    int nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onGlobalBegin ("global");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create request scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onRequestBegin ("appid", "scopeid", "sessionid");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create second application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ("any other blabla");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Begin session scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    final ISessionScope aSessionScope = ScopeManager.getSessionScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // End request scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onRequestEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());

    // End session scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.destroySessionScope (aSessionScope);
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());

    // End global scope and application scopes
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onGlobalEnd ();
    assertEquals (nPrev + 3, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 3, AbstractThrowingWebScopeSPI.getEnd ());
  }

  @Test
  public void testSessionApplicationScopes ()
  {
    // Create global scope
    int nPrev = AbstractWebScopeSPI.getBegin ();
    int nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onGlobalBegin ("global");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create request scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.onRequestBegin ("appid", "scopeid", "sessionid");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Create second application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getApplicationScope ("any other blabla");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Begin session scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    final ISessionScope aSessionScope = ScopeManager.getSessionScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Get session application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getSessionApplicationScope ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // Get second session application scope
    nPrev = AbstractWebScopeSPI.getBegin ();
    nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
    ScopeManager.getSessionApplicationScope ("session web scope for testing");
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

    // End request scope
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onRequestEnd ();
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());

    // End session scope and session application scopes
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.destroySessionScope (aSessionScope);
    assertEquals (nPrev + 3, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 3, AbstractThrowingWebScopeSPI.getEnd ());

    // End global scope and application scopes
    nPrev = AbstractWebScopeSPI.getEnd ();
    nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    ScopeManager.onGlobalEnd ();
    assertEquals (nPrev + 3, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 3, AbstractThrowingWebScopeSPI.getEnd ());
  }
}
