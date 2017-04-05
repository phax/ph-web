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
package com.helger.web.servlets.scope;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.servlet.http.AbstractAsyncHttpServlet;
import com.helger.servlet.http.IHttpServletHandler;

/**
 * A thin wrapper around an {AbstractAsyncHttpServlet} that encapsulates the
 * correct Scope handling before and after a request.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public abstract class AbstractScopeAwareAsyncHttpServlet extends AbstractAsyncHttpServlet
{
  // Determined in "init" method
  private transient String m_sStatusApplicationID;

  /**
   * Default constructor for synchronous servlets.
   */
  protected AbstractScopeAwareAsyncHttpServlet ()
  {
    super ();
  }

  /**
   * Constructor.
   *
   * @param aAsyncSpec
   *        The async/sync specification to be used. May not be
   *        <code>null</code>.
   */
  protected AbstractScopeAwareAsyncHttpServlet (@Nonnull final ServletAsyncSpec aAsyncSpec)
  {
    super (aAsyncSpec);
  }

  /**
   * @return The application ID for this servlet. May neither be
   *         <code>null</code> nor empty.
   */
  @OverrideOnDemand
  @Nonnull
  @Nonempty
  protected String getApplicationID ()
  {
    return ClassHelper.getClassLocalName (getClass ());
  }

  @Override
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  public void init () throws ServletException
  {
    super.init ();
    m_sStatusApplicationID = getApplicationID ();
    if (StringHelper.hasNoText (m_sStatusApplicationID))
      throw new InitializationException ("Failed retrieve a valid application ID! Please override getApplicationID()");
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  protected IHttpServletHandler getEffectiveHandler (@Nonnull final IHttpServletHandler aHandler,
                                                     @Nonnull final HttpServletRequest aHttpRequest,
                                                     @Nonnull final HttpServletResponse aHttpResponse,
                                                     @Nonnull final EHTTPVersion eHttpVersion,
                                                     @Nonnull final EHTTPMethod eHttpMethod)
  {
    final IHttpServletHandler aHandlerToBeScoped = super.getEffectiveHandler (aHandler,
                                                                              aHttpRequest,
                                                                              aHttpResponse,
                                                                              eHttpVersion,
                                                                              eHttpMethod);
    return new ScopingHttpServletHandler (m_sStatusApplicationID, aHandlerToBeScoped);
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("ApplicationID", m_sStatusApplicationID)
                            .getToString ();
  }
}
