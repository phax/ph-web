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
package com.helger.web.servlet.request;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.SimpleURL;
import com.helger.http.EHTTPMethod;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.mock.MockHttpServletRequest;
import com.helger.web.mock.MockServletContext;

/**
 * Test class for class {@link RequestHelper}.
 *
 * @author Philip Helger
 */
public final class RequestHelperTest
{
  @Test
  public void testGetRequestURI ()
  {
    final MockServletContext aSC = MockServletContext.create ();
    try
    {
      final MockHttpServletRequest r = new MockHttpServletRequest (aSC,
                                                                   EHTTPMethod.GET).setAllPaths ("/context/servlet/index.xyz?x=1");
      assertEquals ("/context/servlet/index.xyz", RequestHelper.getRequestURI (r));
      r.addParameter ("abc", "xyz");
      assertEquals ("/context/servlet/index.xyz", RequestHelper.getRequestURI (r));
    }
    finally
    {
      aSC.invalidate ();
    }
  }

  @Test
  public void testGetRequestString ()
  {
    assertEquals ("http://server:517/context/servlet/index.xyz?x=1",
                  RequestHelper.getFullServerNameAndPath ("http", "server", 517, "/context/servlet/index.xyz", "x=1"));
  }

  @Test
  public void testGetWithoutSessionID ()
  {
    final String sURL = "http://127.0.0.1:8080/erb/;jsessionid=1n3dlmrbng6ieckg4lahc7kpf?p=einvoice_precond_usp#top";
    final ISimpleURL aBaseURL = new SimpleURL (sURL);
    // Just a sanity check that parsing works :)
    assertEquals (sURL, aBaseURL.getAsStringWithEncodedParameters ());
    final ISimpleURL aStrippedURL = RequestHelper.getWithoutSessionID (aBaseURL);
    assertEquals ("http://127.0.0.1:8080/erb/?p=einvoice_precond_usp#top",
                  aStrippedURL.getAsStringWithEncodedParameters ());
  }
}
