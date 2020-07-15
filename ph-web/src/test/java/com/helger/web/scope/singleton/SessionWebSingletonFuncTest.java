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

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;
import com.helger.web.scope.mock.AbstractWebScopeAwareTestCase;

/**
 * Test class for class {@link AbstractSessionWebSingleton}.<br>
 * Note: must reside here for Mock* stuff!
 *
 * @author Philip Helger
 */
public final class SessionWebSingletonFuncTest extends AbstractWebScopeAwareTestCase
{
  @Test
  public void testBasic ()
  {
    assertTrue (AbstractSessionWebSingleton.getAllSessionSingletons ().isEmpty ());
    assertFalse (AbstractSessionWebSingleton.isSessionSingletonInstantiated (MockSessionWebSingleton.class));
    assertNull (AbstractSessionWebSingleton.getSessionSingletonIfInstantiated (MockSessionWebSingleton.class));

    final MockSessionWebSingleton a = MockSessionWebSingleton.getInstance ();
    assertNotNull (a);
    assertTrue (AbstractSessionWebSingleton.isSessionSingletonInstantiated (MockSessionWebSingleton.class));
    assertSame (a, AbstractSessionWebSingleton.getSessionSingletonIfInstantiated (MockSessionWebSingleton.class));
    assertEquals (0, a.get ());
    a.inc ();
    assertEquals (1, a.get ());

    final MockSessionWebSingleton b = MockSessionWebSingleton.getInstance ();
    assertSame (a, b);

    CommonsTestHelper.testDefaultSerialization (a);
  }

  @Test
  public void testSerialize ()
  {
    assertTrue (AbstractSessionWebSingleton.getAllSessionSingletons ().isEmpty ());
    assertFalse (AbstractSessionWebSingleton.isSessionSingletonInstantiated (MockSessionWebSingletonWithScopeCtor.class));
    assertNull (AbstractSessionWebSingleton.getSessionSingletonIfInstantiated (MockSessionWebSingletonWithScopeCtor.class));

    final MockSessionWebSingletonWithScopeCtor a = MockSessionWebSingletonWithScopeCtor.getInstance ();
    assertNotNull (a);
    assertTrue (AbstractSessionWebSingleton.isSessionSingletonInstantiated (MockSessionWebSingletonWithScopeCtor.class));
    assertSame (a, AbstractSessionWebSingleton.getSessionSingletonIfInstantiated (MockSessionWebSingletonWithScopeCtor.class));
    assertNotNull (a.getScope ());
    assertEquals (0, a.get ());
    a.inc ();
    assertEquals (1, a.get ());

    final MockSessionWebSingletonWithScopeCtor b = MockSessionWebSingletonWithScopeCtor.getInstance ();
    assertNotNull (b.getScope ());
    assertSame (a, b);

    CommonsTestHelper.testDefaultSerialization (a);
  }
}
