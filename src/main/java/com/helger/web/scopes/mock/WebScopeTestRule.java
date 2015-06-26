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
package com.helger.web.scopes.mock;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpSession;

import org.junit.rules.ExternalResource;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.web.mock.MockHttpListener;
import com.helger.web.mock.MockHttpServletRequest;
import com.helger.web.mock.MockServletContext;
import com.helger.web.mock.MockServletPool;

/**
 * JUnit test rule for unit tests requiring web scopes.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class WebScopeTestRule extends ExternalResource
{
  /** Mock servlet context name */
  public static final String MOCK_CONTEXT_PATH = WebScopeAwareTestSetup.MOCK_CONTEXT_PATH;

  private String m_sContextPath = MOCK_CONTEXT_PATH;
  private Map <String, String> m_aServletContextInitParameters;
  private MockServletContext m_aServletContext;
  private MockHttpServletRequest m_aRequest;

  public WebScopeTestRule ()
  {
    this (null);
  }

  public WebScopeTestRule (@Nullable final Map <String, String> aServletContextInitParameters)
  {
    m_aServletContextInitParameters = aServletContextInitParameters;
  }

  @Nonnull
  public final WebScopeTestRule setContextPath (@Nullable final String sContextPath)
  {
    m_sContextPath = sContextPath;
    return this;
  }

  @Nullable
  public final String getContextPath ()
  {
    return m_sContextPath;
  }

  @Nonnull
  public final WebScopeTestRule setServletContextInitParameters (@Nullable final Map <String, String> aServletContextInitParameters)
  {
    m_aServletContextInitParameters = aServletContextInitParameters;
    return this;
  }

  @Nonnull
  @ReturnsMutableCopy
  public final Map <String, String> getServletContextInitParameters ()
  {
    return CollectionHelper.newMap (m_aServletContextInitParameters);
  }

  /**
   * This method triggers the initialization of the {@link MockHttpListener}. It
   * is called before the main servlet context is created.
   */
  @OverrideOnDemand
  protected void initListener ()
  {
    // Ensure that the default-default listeners are present
    WebScopeAwareTestSetup.setCoreMockHttpListeners ();
  }

  /**
   * Create a new mock servlet context
   *
   * @param sContextPath
   *        The context path to use. May be <code>null</code>.
   * @param aInitParams
   *        The initialization context parameters to use. May be
   *        <code>null</code>.
   * @return Never <code>null</code>.
   */
  @Nonnull
  @OverrideOnDemand
  protected MockServletContext createMockServletContext (@Nullable final String sContextPath,
                                                         @Nullable final Map <String, String> aInitParams)
  {
    return WebScopeAwareTestSetup.createDefaultMockServletContext (sContextPath, aInitParams);
  }

  /**
   * Create a new mock request
   *
   * @param aServletContext
   *        The servlet context to use. Never <code>null</code>.
   * @return May be <code>null</code> to indicate that the request is added
   *         manually - this is helpful for servlet testing.
   */
  @Nullable
  @OverrideOnDemand
  protected MockHttpServletRequest createMockRequest (@Nonnull final MockServletContext aServletContext)
  {
    return WebScopeAwareTestSetup.createDefaultMockRequest (aServletContext);
  }

  @Override
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  public void before ()
  {
    // init HTTP event listener BEFORE creating the servlet context etc.!
    initListener ();

    // Start global scope -> triggers HTTP events
    m_aServletContext = createMockServletContext (m_sContextPath, m_aServletContextInitParameters);
    if (m_aServletContext == null)
      throw new IllegalStateException ("Failed to created MockServletContext");

    // Start request scope -> triggers HTTP events
    m_aRequest = createMockRequest (m_aServletContext);
  }

  @Override
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  public void after ()
  {
    WebScopeAwareTestSetup.shutdownWebScopeTests (m_aRequest, m_aServletContext);
    m_aRequest = null;
    m_aServletContext = null;
  }

  /**
   * @return The created {@link MockServletContext} or <code>null</code> if non
   *         has been created yet.
   */
  @Nullable
  public final MockServletContext getServletContext ()
  {
    return m_aServletContext;
  }

  /**
   * @return The {@link MockServletPool} of the {@link MockServletContext} or
   *         <code>null</code> if no servlet context has been created yet.
   */
  @Nullable
  public final MockServletPool getServletPool ()
  {
    return m_aServletContext == null ? null : m_aServletContext.getServletPool ();
  }

  /**
   * @return The created {@link MockHttpServletRequest} or <code>null</code> if
   *         non has been created yet.
   */
  @Nullable
  public final MockHttpServletRequest getRequest ()
  {
    return m_aRequest;
  }

  /**
   * @param bCreateIfNotExisting
   *        <code>true</code> to create a new session, if non is existing yet.
   *        This has only an effect if a request is present.
   * @return The {@link HttpSession} or <code>null</code> if no session was
   *         created or if no request is present.
   */
  @Nullable
  public final HttpSession getSession (final boolean bCreateIfNotExisting)
  {
    return m_aRequest == null ? null : m_aRequest.getSession (bCreateIfNotExisting);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).appendIfNotNull ("contextPath", m_sContextPath)
                                       .appendIfNotNull ("servletContextInitParams", m_aServletContextInitParameters)
                                       .append ("servletContext", m_aServletContext)
                                       .appendIfNotNull ("request", m_aRequest)
                                       .toString ();
  }
}
