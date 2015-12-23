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
package com.helger.web.scope.singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;
import com.helger.web.scope.mock.AbstractWebScopeAwareTestCase;

/**
 * Test class for class {@link AbstractSessionApplicationWebSingleton}.<br>
 * Note: must reside here for Mock* stuff!
 *
 * @author Philip Helger
 */
public final class SessionApplicationWebSingletonFuncTest extends AbstractWebScopeAwareTestCase
{
  @Test
  public void testBasic () throws Exception
  {
    assertTrue (AbstractSessionApplicationWebSingleton.getAllSessionApplicationSingletons ().isEmpty ());
    assertFalse (AbstractSessionApplicationWebSingleton.isSessionApplicationSingletonInstantiated (MockSessionApplicationWebSingleton.class));
    assertNull (AbstractSessionApplicationWebSingleton.getSessionApplicationSingletonIfInstantiated (MockSessionApplicationWebSingleton.class));

    final MockSessionApplicationWebSingleton a = MockSessionApplicationWebSingleton.getInstance ();
    assertNotNull (a);
    assertTrue (AbstractSessionApplicationWebSingleton.isSessionApplicationSingletonInstantiated (MockSessionApplicationWebSingleton.class));
    assertSame (a,
                AbstractSessionApplicationWebSingleton.getSessionApplicationSingletonIfInstantiated (MockSessionApplicationWebSingleton.class));
    assertEquals (0, a.get ());
    a.inc ();
    assertEquals (1, a.get ());

    final MockSessionApplicationWebSingleton b = MockSessionApplicationWebSingleton.getInstance ();
    assertSame (a, b);

    CommonsTestHelper.testDefaultSerialization (a);
  }

  @Test
  public void testCtor () throws Exception
  {
    assertTrue (AbstractSessionApplicationWebSingleton.getAllSessionApplicationSingletons ().isEmpty ());
    assertFalse (AbstractSessionApplicationWebSingleton.isSessionApplicationSingletonInstantiated (MockSessionApplicationWebSingletonWithScopeCtor.class));
    assertNull (AbstractSessionApplicationWebSingleton.getSessionApplicationSingletonIfInstantiated (MockSessionApplicationWebSingletonWithScopeCtor.class));

    final MockSessionApplicationWebSingletonWithScopeCtor a = MockSessionApplicationWebSingletonWithScopeCtor.getInstance ();
    assertNotNull (a);
    assertTrue (AbstractSessionApplicationWebSingleton.isSessionApplicationSingletonInstantiated (MockSessionApplicationWebSingletonWithScopeCtor.class));
    assertSame (a,
                AbstractSessionApplicationWebSingleton.getSessionApplicationSingletonIfInstantiated (MockSessionApplicationWebSingletonWithScopeCtor.class));
    assertNotNull (a.getScope ());
    assertEquals (0, a.get ());
    a.inc ();
    assertEquals (1, a.get ());

    final MockSessionApplicationWebSingletonWithScopeCtor b = MockSessionApplicationWebSingletonWithScopeCtor.getInstance ();
    assertNotNull (b.getScope ());
    assertSame (a, b);

    CommonsTestHelper.testDefaultSerialization (a);
  }
}
