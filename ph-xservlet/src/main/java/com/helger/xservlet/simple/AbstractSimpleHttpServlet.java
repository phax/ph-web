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
package com.helger.xservlet.simple;

import javax.annotation.Nonnull;

import com.helger.http.EHttpMethod;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.xservlet.AbstractXServlet;
import com.helger.xservlet.handler.IXServletHandler;
import com.helger.xservlet.handler.XServletAsyncHandler;

/**
 * This is an abstract servlet class that combines the following technologies
 * together:
 * <ul>
 * <li>Asynchronous processing</li>
 * <li>UnifiedResponse handling</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public abstract class AbstractSimpleHttpServlet extends AbstractXServlet
{
  protected final void registerSyncHandler (@Nonnull final EHttpMethod eMethod,
                                            @Nonnull final IXServletSimpleHandler aSimpleHandler)
  {
    registerHandler (eMethod, ServletAsyncSpec.getSync (), aSimpleHandler);
  }

  protected final void registerHandler (@Nonnull final EHttpMethod eMethod,
                                        @Nonnull final ServletAsyncSpec aAsyncSpec,
                                        @Nonnull final IXServletSimpleHandler aSimpleHandler)
  {
    // Always invoke the simple handler
    IXServletHandler aRealHandler = new XServletHandlerToSimpleHandler (aSimpleHandler);

    // Add the async handler only in front if necessary
    if (aAsyncSpec.isAsynchronous ())
      aRealHandler = new XServletAsyncHandler (aAsyncSpec, aRealHandler);

    // Register as a regular handler
    handlerRegistry ().registerHandler (eMethod, aRealHandler);
  }
}
