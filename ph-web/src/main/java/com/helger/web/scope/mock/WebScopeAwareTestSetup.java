/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.web.scope.mock;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.cleanup.CommonsCleanup;
import com.helger.http.EHTTPMethod;
import com.helger.servlet.mock.MockHttpServletRequest;
import com.helger.servlet.mock.MockServletContext;
import com.helger.xml.util.XMLCleanup;

/**
 * Contains static initialization methods for web scope tests, that makes it a
 * bit easier to use them without JUnit.
 *
 * @author Philip Helger
 */
@Immutable
public final class WebScopeAwareTestSetup
{
  /** Mock servlet context name */
  public static final String MOCK_CONTEXT_PATH = "/MockContext";

  @PresentForCodeCoverage
  private static final WebScopeAwareTestSetup s_aInstance = new WebScopeAwareTestSetup ();

  private WebScopeAwareTestSetup ()
  {}

  /**
   * Create a new mock servlet context using the context path
   * {@link #MOCK_CONTEXT_PATH} and no context init parameters.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static MockServletContext createDefaultMockServletContext ()
  {
    return createDefaultMockServletContext (MOCK_CONTEXT_PATH, null);
  }

  /**
   * Create a new mock servlet context.
   *
   * @param sContextPath
   *        The context path to use. May be <code>null</code>.
   * @param aInitParams
   *        The initialization context parameters to use. May be
   *        <code>null</code>.
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static MockServletContext createDefaultMockServletContext (@Nullable final String sContextPath,
                                                                    @Nullable final Map <String, String> aInitParams)
  {
    return MockServletContext.create (sContextPath, aInitParams);
  }

  /**
   * Create a new mock request.
   *
   * @param aServletContext
   *        The servlet context to use. May be <code>null</code> but not
   *        recommended.
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static MockHttpServletRequest createDefaultMockRequest (@Nonnull final MockServletContext aServletContext)
  {
    return new MockHttpServletRequest (aServletContext, EHTTPMethod.GET);
  }

  /**
   * Invalidate the passed request and the passed servlet context after the
   * test.
   *
   * @param aRequest
   *        The request to invalidate. May be <code>null</code>.
   * @param aServletContext
   *        The servlet context to invalidate. May be <code>null</code>.
   */
  public static void shutdownWebScopeTests (@Nullable final MockHttpServletRequest aRequest,
                                            @Nullable final MockServletContext aServletContext)
  {
    if (aRequest != null)
    {
      // end request -> triggers HTTP events
      aRequest.invalidate ();
    }

    if (aServletContext != null)
    {
      // shutdown global context -> triggers HTTP events
      aServletContext.invalidate ();
    }

    // Cleanup all ph-commons stuff
    XMLCleanup.cleanup ();
    CommonsCleanup.cleanup ();
  }
}
