/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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
package com.helger.xservlet.handler;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.attr.IAttributeContainerAny;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpVersion;
import com.helger.servlet.async.AsyncServletRunnerDefault;
import com.helger.servlet.async.ExtAsyncContext;
import com.helger.servlet.async.IAsyncServletRunner;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.mgr.WebScoped;
import com.helger.xservlet.AbstractXServlet;

/**
 * A special {@link IXServletHandler} that allows to run requests
 * asynchronously.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public final class XServletAsyncHandler implements IXServletHandler
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XServletAsyncHandler.class);
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
  private final IXServletHandler m_aNestedHandler;

  public XServletAsyncHandler (@Nonnull final ServletAsyncSpec aAsyncSpec, @Nonnull final IXServletHandler aNestedHandler)
  {
    m_aAsyncSpec = ValueEnforcer.notNull (aAsyncSpec, "AsyncSpec");
    m_aNestedHandler = ValueEnforcer.notNull (aNestedHandler, "NestedHandler");
  }

  @Override
  public void onServletInit (@Nonnull final ICommonsMap <String, String> aInitParams) throws ServletException
  {
    m_aNestedHandler.onServletInit (aInitParams);
  }

  @Override
  public void onServletDestroy ()
  {
    m_aNestedHandler.onServletDestroy ();
  }

  private void _handleAsync (@Nonnull final HttpServletRequest aHttpRequest,
                             @Nonnull final HttpServletResponse aHttpResponse,
                             @Nonnull final EHttpVersion eHttpVersion,
                             @Nonnull final EHttpMethod eHttpMethod,
                             @Nonnull final IRequestWebScope aRequestScope)
  {
    final ExtAsyncContext aExtAsyncCtx = ExtAsyncContext.create (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, m_aAsyncSpec);

    // Remember outside before it is too late :)
    final IAttributeContainerAny <String> aAttrs = aRequestScope.attrs ().getClone ();
    final IAttributeContainerAny <String> aParams = aRequestScope.params ().getClone ();

    // Put into async processing queue
    s_aAsyncServletRunner.runAsync (aHttpRequest, aHttpResponse, aExtAsyncCtx, () -> {
      try (final WebScoped aWebScoped = new WebScoped (aHttpRequest, aHttpResponse))
      {
        // Restore all attributes (display locale etc.) that are missing
        aWebScoped.getRequestScope ().attrs ().putAllIn (aAttrs);
        aWebScoped.getRequestScope ().params ().putAllIn (aParams);

        m_aNestedHandler.onRequest (aExtAsyncCtx.getRequest (),
                                    aExtAsyncCtx.getResponse (),
                                    aExtAsyncCtx.getHTTPVersion (),
                                    aExtAsyncCtx.getHTTPMethod (),
                                    aWebScoped.getRequestScope ());
      }
      catch (final Exception ex)
      {
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Error processing async request " + aExtAsyncCtx.getRequest (), ex);

        try
        {
          final String sErrorMsg = "Internal error processing your request. Please try again later. Technical details: " +
                                   ex.getClass ().getName () +
                                   ":" +
                                   ex.getMessage ();
          aExtAsyncCtx.getResponse ().getWriter ().write (sErrorMsg);
        }
        catch (final Exception ex2)
        {
          LOGGER.error ("Error writing first exception to response", ex2);
        }
      }
      finally
      {
        try
        {
          aExtAsyncCtx.complete ();
        }
        catch (final Exception ex)
        {
          LOGGER.error ("Error completing async context", ex);
        }
      }
    });
  }

  public void onRequest (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final EHttpVersion eHttpVersion,
                         @Nonnull final EHttpMethod eHttpMethod,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    if (m_aAsyncSpec.isAsynchronous () && aHttpRequest.isAsyncSupported ())
    {
      // Run asynchronously

      // remember before invoking handler to avoid request scope destruction
      aRequestScope.attrs ().putIn (AbstractXServlet.REQUEST_ATTR_HANDLED_ASYNC, true);

      // Main async handler
      _handleAsync (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope);
    }
    else
    {
      // Run synchronously
      m_aNestedHandler.onRequest (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope);
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("AsyncSpec", m_aAsyncSpec).append ("OriginalHandler", m_aNestedHandler).getToString ();
  }
}
