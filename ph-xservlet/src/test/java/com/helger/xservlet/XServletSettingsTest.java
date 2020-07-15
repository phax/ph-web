/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
package com.helger.xservlet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;
import com.helger.commons.url.SimpleURL;
import com.helger.http.EHttpReferrerPolicy;
import com.helger.servlet.response.EXFrameOptionType;

/**
 * Test class for class {@link XServletSettings}.
 *
 * @author Philip Helger
 */
public final class XServletSettingsTest
{
  @Test
  public void testDefaultSettings ()
  {
    final XServletSettings x = new XServletSettings ();
    assertNotNull (x.getHttpReferrerPolicy ());
    assertTrue (x.hasHttpReferrerPolicy ());
    assertNotNull (x.getXFrameOptionsType ());
    assertNull (x.getXFrameOptionsDomain ());
    assertTrue (x.hasXFrameOptions ());
    assertTrue (x.isMultipartEnabled ());
  }

  @Test
  public void testDisableAll ()
  {
    final XServletSettings x = new XServletSettings ();
    x.setHttpReferrerPolicy (null);
    x.setXFrameOptions (null, null);
    x.setMultipartEnabled (false);
    assertNull (x.getHttpReferrerPolicy ());
    assertFalse (x.hasHttpReferrerPolicy ());
    assertNull (x.getXFrameOptionsType ());
    assertNull (x.getXFrameOptionsDomain ());
    assertFalse (x.hasXFrameOptions ());
    assertFalse (x.isMultipartEnabled ());
  }

  @Test
  public void testClone ()
  {
    final XServletSettings x = new XServletSettings ();
    x.setHttpReferrerPolicy (null);
    x.setXFrameOptions (null, null);
    x.setMultipartEnabled (false);

    final XServletSettings x2 = x.getClone ();
    assertNull (x2.getHttpReferrerPolicy ());
    assertFalse (x2.hasHttpReferrerPolicy ());
    assertNull (x2.getXFrameOptionsType ());
    assertNull (x2.getXFrameOptionsDomain ());
    assertFalse (x2.hasXFrameOptions ());
    assertFalse (x2.isMultipartEnabled ());

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (x, x2);
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (x, x.getClone ().setMultipartEnabled (true));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (x,
                                                                           x.getClone ()
                                                                            .setHttpReferrerPolicy (EHttpReferrerPolicy.NO_REFERRER));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (x,
                                                                           x.getClone ().setXFrameOptions (EXFrameOptionType.DENY, null));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (x, x.getClone ().setXFrameOptions (null, new SimpleURL ("bla")));
  }
}
