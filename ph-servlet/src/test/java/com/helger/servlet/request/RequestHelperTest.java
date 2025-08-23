/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.helger.http.EHttpMethod;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.mock.MockServletContext;
import com.helger.url.ISimpleURL;
import com.helger.url.SimpleURL;

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
      MockHttpServletRequest r = new MockHttpServletRequest (aSC, EHttpMethod.GET).setAllPaths ("/context/servlet/index.xyz?x=1");
      assertEquals ("/context/servlet/index.xyz", RequestHelper.getRequestURIDecoded (r));
      r.addParameter ("abc", "xyz");
      assertEquals ("/context/servlet/index.xyz", RequestHelper.getRequestURIDecoded (r));

      r = new MockHttpServletRequest (aSC, EHttpMethod.GET).setAllPaths ("/context/servlet/in%3adex.xyz?x=1");
      assertEquals ("/context/servlet/in:dex.xyz", RequestHelper.getRequestURIDecoded (r));
      assertEquals ("/context/servlet/in%3adex.xyz", RequestHelper.getRequestURIEncoded (r));
      r.addParameter ("abc", "xyz");
      assertEquals ("/context/servlet/in:dex.xyz", RequestHelper.getRequestURIDecoded (r));
      assertEquals ("/context/servlet/in%3adex.xyz", RequestHelper.getRequestURIEncoded (r));
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
    assertEquals (sURL, aBaseURL.getAsString ());
    final ISimpleURL aStrippedURL = RequestHelper.getWithoutSessionID (aBaseURL);
    assertEquals ("http://127.0.0.1:8080/erb/?p=einvoice_precond_usp#top", aStrippedURL.getAsString ());
  }

  @Test
  public void testGetSessionID ()
  {
    assertNull (RequestHelper.getSessionID ("test.html"));
    assertEquals ("abc", RequestHelper.getSessionID ("test.html;abc"));
    assertEquals ("abc?x=y", RequestHelper.getSessionID ("test.html;abc?x=y"));
    final String sURL = "http://127.0.0.1:8080/erb/;jsessionid=1n3dlmrbng6ieckg4lahc7kpf?p=einvoice_precond_usp#top";
    assertEquals ("jsessionid=1n3dlmrbng6ieckg4lahc7kpf?p=einvoice_precond_usp#top", RequestHelper.getSessionID (sURL));
    assertEquals ("jsessionid=1n3dlmrbng6ieckg4lahc7kpf", RequestHelper.getSessionID (new SimpleURL (sURL)));
  }
}
