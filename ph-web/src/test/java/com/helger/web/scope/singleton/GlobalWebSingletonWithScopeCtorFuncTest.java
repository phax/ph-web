/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.web.scope.mock.AbstractWebScopeAwareTestCase;

/**
 * Test class for class {@link AbstractGlobalWebSingleton}.
 *
 * @author Philip Helger
 */
public final class GlobalWebSingletonWithScopeCtorFuncTest extends AbstractWebScopeAwareTestCase
{
  @BeforeClass
  public static void beforeClass ()
  {
    assertEquals (0, MockGlobalWebSingletonWithScopeCtor.s_nCtorCount);
    assertEquals (0, MockGlobalWebSingletonWithScopeCtor.s_nDtorCount);
  }

  @AfterClass
  public static void afterClass ()
  {
    assertEquals (1, MockGlobalWebSingletonWithScopeCtor.s_nCtorCount);
    assertEquals (1, MockGlobalWebSingletonWithScopeCtor.s_nDtorCount);
  }

  @Test
  public void testCtor ()
  {
    assertTrue (AbstractGlobalWebSingleton.getAllGlobalSingletons ().isEmpty ());
    assertFalse (AbstractGlobalWebSingleton.isGlobalSingletonInstantiated (MockGlobalWebSingletonWithScopeCtor.class));
    assertNull (AbstractGlobalWebSingleton.getGlobalSingletonIfInstantiated (MockGlobalWebSingletonWithScopeCtor.class));

    final MockGlobalWebSingletonWithScopeCtor a = MockGlobalWebSingletonWithScopeCtor.getInstance ();
    assertNotNull (a);
    assertTrue (AbstractGlobalWebSingleton.isGlobalSingletonInstantiated (MockGlobalWebSingletonWithScopeCtor.class));
    assertSame (a, AbstractGlobalWebSingleton.getGlobalSingletonIfInstantiated (MockGlobalWebSingletonWithScopeCtor.class));
    assertNotNull (a.getScope ());

    final MockGlobalWebSingletonWithScopeCtor b = MockGlobalWebSingletonWithScopeCtor.getInstance ();
    assertNotNull (b.getScope ());
    assertSame (a, b);
  }
}
