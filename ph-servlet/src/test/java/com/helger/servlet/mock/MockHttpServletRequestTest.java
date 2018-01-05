/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.servlet.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.helger.commons.http.EHttpMethod;
import com.helger.servlet.ServletHelper;

/**
 * Test class for class {@link MockHttpServletRequest}.
 *
 * @author Philip Helger
 */
public final class MockHttpServletRequestTest
{
  @Test
  public void testSetPathsFromRequestURI ()
  {
    final String sContextPath = "/ctx";
    final MockServletContext aSC = MockServletContext.create (sContextPath);
    try
    {
      final MockHttpServletRequest c = new MockHttpServletRequest (aSC, EHttpMethod.GET);

      c.setAllPaths (sContextPath + "/servlet?x=y");
      assertNull (c.getScheme ());
      assertNull (c.getServerName ());
      assertEquals (-1, c.getServerPort ());
      assertEquals (sContextPath, ServletHelper.getRequestContextPath (c));
      assertEquals ("/servlet", ServletHelper.getRequestServletPath (c));
      assertEquals ("", ServletHelper.getRequestPathInfo (c));
      assertEquals (sContextPath + "/servlet", ServletHelper.getRequestRequestURI (c));
      assertEquals ("x=y", ServletHelper.getRequestQueryString (c));

      c.setAllPaths (sContextPath + "/servlet/path/in/servlet#anchor");
      assertNull (c.getScheme ());
      assertNull (c.getServerName ());
      assertEquals (-1, c.getServerPort ());
      assertEquals (sContextPath, ServletHelper.getRequestContextPath (c));
      assertEquals ("/servlet", ServletHelper.getRequestServletPath (c));
      assertEquals ("/path/in/servlet", ServletHelper.getRequestPathInfo (c));
      assertEquals (sContextPath + "/servlet/path/in/servlet", ServletHelper.getRequestRequestURI (c));
      assertNull (c.getQueryString ());
    }
    finally
    {
      aSC.invalidate ();
    }
  }
}
