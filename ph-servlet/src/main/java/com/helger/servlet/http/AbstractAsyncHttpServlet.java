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
package com.helger.servlet.http;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;
import com.helger.servlet.async.AsyncServletRunnerDefault;
import com.helger.servlet.async.IAsyncServletRunner;
import com.helger.servlet.async.ServletAsyncSpec;

/**
 * Abstract handler based HTTP servlet with support for async processing.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public abstract class AbstractAsyncHttpServlet extends AbstractHttpServlet
{
  private static IAsyncServletRunner s_aAsyncServletRunner = new AsyncServletRunnerDefault ();

  /**
   * Set the async runner to be used.
   *
   * @param aAsyncServletRunner
   *        The runner to be used. May not be <code>null</code>.
   */
  public static void setAsyncServletRunner (@Nonnull final IAsyncServletRunner aAsyncServletRunner)
  {
    ValueEnforcer.notNull (aAsyncServletRunner, "AsyncServletRunner");
    s_aAsyncServletRunner = aAsyncServletRunner;
  }

  /**
   * @return The global async runner. Never <code>null</code>.
   */
  @Nonnull
  public static IAsyncServletRunner getAsyncServletRunner ()
  {
    return s_aAsyncServletRunner;
  }

  private final ServletAsyncSpec m_aAsyncSpec;

  /**
   * Default constructor for synchronous servlets.
   */
  protected AbstractAsyncHttpServlet ()
  {
    // By default synchronous
    this (ServletAsyncSpec.createSync ());
  }

  /**
   * Constructor.
   *
   * @param aAsyncSpec
   *        The async/sync spec to be used. May not be <code>null</code>.
   */
  protected AbstractAsyncHttpServlet (@Nonnull final ServletAsyncSpec aAsyncSpec)
  {
    m_aAsyncSpec = ValueEnforcer.notNull (aAsyncSpec, "AsyncSpec");
  }

  /**
   * @return <code>true</code> if this servlet acts synchronously (for certain
   *         HTTP methods), <code>false</code> if it acts asynchronously.
   */
  public final boolean isAsynchronous ()
  {
    return m_aAsyncSpec.isAsynchronous ();
  }

  /**
   * @return The internal async spec. Never <code>null</code>.
   */
  @Nonnull
  protected final ServletAsyncSpec internalGetAsyncSpec ()
  {
    return m_aAsyncSpec;
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  protected IHttpServletHandler getEffectiveHandler (@Nonnull final IHttpServletHandler aHandler,
                                                     @Nonnull final HttpServletRequest aHttpRequest,
                                                     @Nonnull final HttpServletResponse aHttpResponse,
                                                     @Nonnull final EHTTPVersion eHttpVersion,
                                                     @Nonnull final EHTTPMethod eHttpMethod)
  {
    final IHttpServletHandler aHandlerToBeWrapped = super.getEffectiveHandler (aHandler,
                                                                               aHttpRequest,
                                                                               aHttpResponse,
                                                                               eHttpVersion,
                                                                               eHttpMethod);

    if (m_aAsyncSpec.isAsynchronous () && m_aAsyncSpec.isAsyncHTTPMethod (eHttpMethod))
    {
      // Run asynchronously
      return new AsyncHttpServletHandler (m_aAsyncSpec, aHandlerToBeWrapped);
    }

    // Run synchronously
    return aHandlerToBeWrapped;
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("AsyncSpec", m_aAsyncSpec).getToString ();
  }
}
