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
package com.helger.web.mock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;

/**
 * Mock implementation of the {@link RequestDispatcher} interface
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class MockRequestDispatcher implements RequestDispatcher
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (MockRequestDispatcher.class);

  private final String m_sURL;

  /**
   * Create a new MockRequestDispatcher for the given URL.
   *
   * @param sURL
   *        the URL to dispatch to.
   */
  public MockRequestDispatcher (@Nonnull final String sURL)
  {
    m_sURL = ValueEnforcer.notNull (sURL, "URL");
  }

  public void forward (@Nonnull final ServletRequest aRequest, @Nonnull final ServletResponse aResponse)
  {
    ValueEnforcer.notNull (aRequest, "Request");
    ValueEnforcer.notNull (aResponse, "Response");
    if (aResponse.isCommitted ())
      throw new IllegalStateException ("Cannot perform forward - response is already committed");

    getMockHttpServletResponse (aResponse).setForwardedUrl (m_sURL);
    if (s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("MockRequestDispatcher: forwarding to URL '" + m_sURL + "'");
  }

  public void include (@Nonnull final ServletRequest aRequest, @Nonnull final ServletResponse aResponse)
  {
    ValueEnforcer.notNull (aRequest, "Request");
    ValueEnforcer.notNull (aResponse, "Response");

    getMockHttpServletResponse (aResponse).setIncludedUrl (m_sURL);
    if (s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("MockRequestDispatcher: including URL '" + m_sURL + "'");
  }

  /**
   * Obtain the underlying MockHttpServletResponse, unwrapping
   * {@link HttpServletResponseWrapper} decorators if necessary.
   *
   * @param aResponse
   *        Original response
   * @return The matching {@link MockHttpServletResponse}
   */
  @Nonnull
  protected MockHttpServletResponse getMockHttpServletResponse (@Nonnull final ServletResponse aResponse)
  {
    if (aResponse instanceof MockHttpServletResponse)
      return (MockHttpServletResponse) aResponse;
    if (aResponse instanceof HttpServletResponseWrapper)
      return getMockHttpServletResponse (((HttpServletResponseWrapper) aResponse).getResponse ());

    throw new IllegalArgumentException ("MockRequestDispatcher requires MockHttpServletResponse");
  }
}
