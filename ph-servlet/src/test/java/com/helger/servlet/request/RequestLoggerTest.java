/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.servlet.request;

import static org.junit.Assert.assertNotNull;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.mock.MockServletContext;

/**
 * Test class for class {@link RequestLogger}.
 *
 * @author Philip Helger
 */
public final class RequestLoggerTest
{
  @Test
  public void testBasic ()
  {
    final MockServletContext aSC = MockServletContext.create ();
    try
    {
      final HttpServletRequest aHttpRequest = new MockHttpServletRequest (aSC);
      assertNotNull (RequestLogger.getRequestFieldMap (aHttpRequest));
    }
    finally
    {
      aSC.invalidate ();
    }
  }
}
