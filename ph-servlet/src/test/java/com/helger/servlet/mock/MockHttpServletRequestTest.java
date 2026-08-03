/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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

import com.helger.http.EHttpMethod;
import com.helger.servlet.SafeHttpServletRequest;

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

      final SafeHttpServletRequest aSafeHttpRequest = SafeHttpServletRequest.wrap (c);

      assertNull (aSafeHttpRequest.getScheme ());
      assertNull (aSafeHttpRequest.getServerName ());
      assertEquals (-1, aSafeHttpRequest.getServerPort ());
      assertEquals (sContextPath, aSafeHttpRequest.getContextPath ());
      assertEquals ("/servlet", aSafeHttpRequest.getServletPath ());
      assertEquals ("", aSafeHttpRequest.getPathInfo ());
      assertEquals (sContextPath + "/servlet", aSafeHttpRequest.getRequestURI ());
      assertEquals ("x=y", aSafeHttpRequest.getQueryString ());

      c.setAllPaths (sContextPath + "/servlet/path/in/servlet#anchor");
      assertNull (c.getScheme ());
      assertNull (c.getServerName ());
      assertEquals (-1, c.getServerPort ());
      assertEquals (sContextPath, aSafeHttpRequest.getContextPath ());
      assertEquals ("/servlet", aSafeHttpRequest.getServletPath ());
      assertEquals ("/path/in/servlet", aSafeHttpRequest.getPathInfo ());
      assertEquals (sContextPath + "/servlet/path/in/servlet", aSafeHttpRequest.getRequestURI ());
      assertNull (aSafeHttpRequest.getQueryString ());
    }
    finally
    {
      aSC.invalidate ();
    }
  }
}
