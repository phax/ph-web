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
package com.helger.xservlet;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.async.AsyncServletRunnerDefault;
import com.helger.servlet.async.ExtAsyncContext;
import com.helger.servlet.async.IAsyncServletRunner;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.web.scope.IRequestWebScope;
import com.helger.xservlet.handler.IXServletHandler;

/**
 * A special {@link IXServletHandler} that allows to run requests
 * asynchronously.
 *
 * @author Philip Helger
 */
public final class AsyncXServletHandler implements IXServletHandler
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AsyncXServletHandler.class);
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

  public AsyncXServletHandler (@Nonnull final ServletAsyncSpec aAsyncSpec,
                               @Nonnull final IXServletHandler aNestedHandler)
  {
    m_aAsyncSpec = ValueEnforcer.notNull (aAsyncSpec, "AsyncSpec");
    m_aNestedHandler = ValueEnforcer.notNull (aNestedHandler, "NestedHandler");
  }

  private void _handleAsync (@Nonnull final HttpServletRequest aHttpRequest,
                             @Nonnull final HttpServletResponse aHttpResponse,
                             @Nonnull final EHttpVersion eHttpVersion,
                             @Nonnull final EHttpMethod eHttpMethod,
                             @Nonnull final IRequestWebScope aRequestScope)
  {
    final ExtAsyncContext aExtAsyncCtx = ExtAsyncContext.create (aHttpRequest,
                                                                 aHttpResponse,
                                                                 eHttpVersion,
                                                                 eHttpMethod,
                                                                 m_aAsyncSpec);

    // Put into async processing queue
    s_aAsyncServletRunner.runAsync (aHttpRequest, aHttpResponse, aExtAsyncCtx, () -> {
      try
      {
        m_aNestedHandler.handle (aExtAsyncCtx.getRequest (),
                                 aExtAsyncCtx.getResponse (),
                                 aExtAsyncCtx.getHTTPVersion (),
                                 aExtAsyncCtx.getHTTPMethod (),
                                 aRequestScope);
      }
      catch (final Throwable t)
      {
        s_aLogger.error ("Error processing async request " + aExtAsyncCtx.getRequest (), t);
        try
        {
          final String sErrorMsg = "Internal error processing your request. Please try again later. Technical details: " +
                                   t.getClass ().getName () +
                                   ":" +
                                   t.getMessage ();
          aExtAsyncCtx.getResponse ().getWriter ().write (sErrorMsg);
        }
        catch (final Throwable t2)
        {
          s_aLogger.error ("Error writing first exception to response", t2);
        }
      }
      finally
      {
        try
        {
          aExtAsyncCtx.complete ();
        }
        catch (final Throwable t)
        {
          s_aLogger.error ("Error completing async context", t);
        }
      }
    });
  }

  public boolean isRunAsynchronously (@Nonnull final EHttpMethod eHttpMethod)
  {
    return m_aAsyncSpec.isAsynchronous () && m_aAsyncSpec.isAsyncHTTPMethod (eHttpMethod);
  }

  public void handle (@Nonnull final HttpServletRequest aHttpRequest,
                      @Nonnull final HttpServletResponse aHttpResponse,
                      @Nonnull final EHttpVersion eHttpVersion,
                      @Nonnull final EHttpMethod eHttpMethod,
                      @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    if (isRunAsynchronously (eHttpMethod))
    {
      // Run asynchronously
      _handleAsync (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope);
    }
    else
    {
      // Run synchronously
      m_aNestedHandler.handle (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope);
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("AsyncSpec", m_aAsyncSpec)
                                       .append ("OriginalHandler", m_aNestedHandler)
                                       .getToString ();
  }
}
