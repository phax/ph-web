/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.helger.web.mock.MockHttpServletRequest;
import com.helger.web.mock.MockHttpServletResponse;
import com.helger.web.scope.impl.RequestWebScope;
import com.helger.web.scope.impl.RequestWebScopeNoMultipart;
import com.helger.web.scope.mock.WebScopeTestRule;
import com.helger.web.servlet.request.IRequestParamMap;

/**
 * Test class for class {@link RequestWebScopeNoMultipart}.
 *
 * @author Philip Helger
 */
public final class RequestWebScopeNoMultipartTest
{
  @Rule
  public final WebScopeTestRule m_aRule = new WebScopeTestRule ();

  @Test
  public void testRequestParamMap ()
  {
    final MockHttpServletRequest aRequest = new MockHttpServletRequest ();
    aRequest.addParameter ("a", "...");
    aRequest.addParameter ("page_name[de]", "deutscher name");
    aRequest.addParameter ("page_name[en]", "english name");
    aRequest.addParameter ("b", "...");
    aRequest.addParameter ("c", "...");
    assertEquals (5, aRequest.getParameterMap ().size ());
    final RequestWebScope aRequestScope = new RequestWebScope (aRequest, new MockHttpServletResponse ());
    aRequestScope.initScope ();
    final IRequestParamMap aRPM = aRequestScope.getRequestParamMap ();
    assertNotNull (aRPM);
    assertSame (aRPM, aRequestScope.getRequestParamMap ());

    assertEquals (aRPM.toString (), 4, aRPM.getSize ());
    assertTrue (aRPM.containsKey ("a"));
    assertTrue (aRPM.containsKey ("b"));
    assertTrue (aRPM.containsKey ("c"));
    assertTrue (aRPM.containsKey ("page_name"));
    assertFalse (aRPM.containsKey ("page_name[de]"));

    // get page_name[de] and page_name[en]
    final IRequestParamMap aNames = aRPM.getMap ("page_name");
    assertEquals (2, aNames.getSize ());
    final Map <String, String> aValueMap = aNames.getAsValueMap ();
    assertEquals (2, aValueMap.size ());
    assertEquals ("deutscher name", aValueMap.get ("de"));
    assertEquals ("english name", aValueMap.get ("en"));
  }
}
