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
package com.helger.web.scope.mock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.junit.Rule;

import com.helger.commons.mock.AbstractCommonsTestCase;
import com.helger.web.mock.MockHttpServletRequest;
import com.helger.web.mock.MockServletContext;
import com.helger.web.mock.MockServletPool;

/**
 * Base class for JUnit tests where the initialization of the scopes happens
 * before each test execution.
 *
 * @author Philip Helger
 */
public abstract class AbstractWebScopeAwareTestCase extends AbstractCommonsTestCase
{
  protected static final String MOCK_CONTEXT = WebScopeAwareTestSetup.MOCK_CONTEXT_PATH;

  /** JUnit test rule */
  @Rule
  public final WebScopeTestRule m_aWebScope = new WebScopeTestRule ();

  @Nonnull
  protected final MockServletContext getServletContext ()
  {
    return m_aWebScope.getServletContext ();
  }

  @Nonnull
  protected final MockServletPool getServletPool ()
  {
    return m_aWebScope.getServletPool ();
  }

  @Nullable
  protected final MockHttpServletRequest getRequest ()
  {
    return m_aWebScope.getRequest ();
  }

  @Nullable
  protected final HttpSession getSession (final boolean bCreateIfNotExisting)
  {
    return m_aWebScope.getSession (bCreateIfNotExisting);
  }
}
