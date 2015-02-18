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
package com.helger.web.scopes.singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.web.scopes.mock.AbstractWebScopeAwareTestCase;
import com.helger.web.scopes.singleton.ApplicationWebSingleton;

/**
 * Test class for class {@link ApplicationWebSingleton}.<br>
 * Note: must reside here for Mock* stuff!
 *
 * @author Philip Helger
 */
public final class ApplicationWebSingletonTest extends AbstractWebScopeAwareTestCase
{
  @Test
  public void testSerialize () throws Exception
  {
    assertTrue (ApplicationWebSingleton.getAllApplicationSingletons ().isEmpty ());
    assertFalse (ApplicationWebSingleton.isApplicationSingletonInstantiated (MockApplicationWebSingleton.class));
    assertNull (ApplicationWebSingleton.getApplicationSingletonIfInstantiated (MockApplicationWebSingleton.class));

    final MockApplicationWebSingleton a = MockApplicationWebSingleton.getInstance ();
    assertNotNull (a);
    assertTrue (ApplicationWebSingleton.isApplicationSingletonInstantiated (MockApplicationWebSingleton.class));
    assertSame (a, ApplicationWebSingleton.getApplicationSingletonIfInstantiated (MockApplicationWebSingleton.class));
    assertEquals (0, a.get ());
    a.inc ();
    assertEquals (1, a.get ());

    final MockApplicationWebSingleton b = MockApplicationWebSingleton.getInstance ();
    assertSame (a, b);
  }
}
