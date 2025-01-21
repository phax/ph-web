/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.web.scope.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.scope.ISessionScope;
import com.helger.scope.ScopeHelper;
import com.helger.scope.mgr.ScopeManager;
import com.helger.scope.mgr.Scoped;
import com.helger.scope.spi.ScopeSPIManager;

/**
 * Test class for class {@link ScopeSPIManager}.
 *
 * @author Philip Helger
 */
public final class WebScopeSPIManagerFuncTest
{
  static
  {
    ScopeHelper.setLifeCycleDebuggingEnabled (true);
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
    try (final Scoped aScoped = new Scoped ())
    {
      assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
      assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

      // End request scope
      nPrev = AbstractWebScopeSPI.getEnd ();
      nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    }
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
    ISessionScope aSessionScope;
    try (final Scoped aScoped = new Scoped ())
    {
      assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
      assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

      // Begin session scope
      nPrev = AbstractWebScopeSPI.getBegin ();
      nPrevT = AbstractThrowingWebScopeSPI.getBegin ();
      aSessionScope = ScopeManager.getSessionScope ();
      assertEquals (nPrev + 1, AbstractWebScopeSPI.getBegin ());
      assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getBegin ());

      // End request scope
      nPrev = AbstractWebScopeSPI.getEnd ();
      nPrevT = AbstractThrowingWebScopeSPI.getEnd ();
    }
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
    assertEquals (nPrev + 1, AbstractWebScopeSPI.getEnd ());
    assertEquals (nPrevT + 1, AbstractThrowingWebScopeSPI.getEnd ());
  }
}
