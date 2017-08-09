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

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.state.EContinue;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.handler.IXServletLowLevelHandler;

/**
 * This is an abstract servlet class that combines the following technologies
 * together:
 * <ul>
 * <li>Asynchronous processing</li>
 * <li>WebScope aware handling</li>
 * <li>UnifiedResponse handling</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 8.0.0
 */
public abstract class AbstractSimpleHttpServlet extends AbstractXServlet
{
  public static interface ISimpleHandler
  {
    @Nonnull
    default UnifiedResponse createUnifiedResponse (@Nonnull final EHttpVersion eHttpVersion,
                                                   @Nonnull final EHttpMethod eHttpMethod,
                                                   @Nonnull final HttpServletRequest aHttpRequest)
    {
      return new UnifiedResponse (eHttpVersion, eHttpMethod, aHttpRequest);
    }

    /**
     * This callback method is unconditionally called before the
     * last-modification checks are performed. So this method can be used to
     * determine the requested object from the request. This method is not
     * called if HTTP version or HTTP method are not supported.
     *
     * @param aRequestScope
     *        The request scope that will be used for processing the request.
     *        Never <code>null</code>.
     * @param aUnifiedResponse
     *        The response object to be filled. Never <code>null</code>.
     * @return {@link EContinue#BREAK} to stop processing (e.g. because a
     *         resource does not exist), {@link EContinue#CONTINUE} to continue
     *         processing as usual.
     */
    @OverrideOnDemand
    default EContinue initRequestState (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                        @Nonnull final UnifiedResponse aUnifiedResponse)
    {
      return EContinue.CONTINUE;
    }

    /**
     * Called before a valid request is handled. This method is only called if
     * HTTP version matches, HTTP method is supported and sending a cached HTTP
     * response is not an option.
     *
     * @param aRequestScope
     *        The request scope that will be used for processing the request.
     *        Never <code>null</code>.
     */
    @OverrideOnDemand
    @Nonnull
    default void onRequestBegin (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
    {}

    /**
     * This is the main request handling method. Overwrite this method to fill
     * your HTTP response.
     *
     * @param aRequestScope
     *        The request scope to use. There is no direct access to the
     *        {@link HttpServletResponse}. Everything must be handled with the
     *        unified response! Never <code>null</code>.
     * @param aUnifiedResponse
     *        The response object to be filled. Never <code>null</code>.
     * @throws Exception
     *         In case of an error
     */
    void handleRequest (@Nonnull IRequestWebScopeWithoutResponse aRequestScope,
                        @Nonnull UnifiedResponse aUnifiedResponse) throws Exception;

    /**
     * Called when an exception occurred in
     * {@link #handleRequest(IRequestWebScopeWithoutResponse, UnifiedResponse)}.
     * This method is only called for non-request-cancel operations.
     *
     * @param aRequestScope
     *        The source request scope. Never <code>null</code>.
     * @param aUnifiedResponse
     *        The response to the current request. Never <code>null</code>.
     * @param t
     *        The Throwable that occurred. Never <code>null</code>.
     * @return {@link EContinue#CONTINUE} to propagate the Exception,
     *         {@link EContinue#BREAK} to swallow it. May not be
     *         <code>null</code>.
     */
    @OverrideOnDemand
    @Nonnull
    default EContinue onException (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                   @Nonnull final UnifiedResponse aUnifiedResponse,
                                   @Nonnull final Throwable t)
    {
      // Propagate only in debug mode
      return EContinue.valueOf (GlobalDebug.isDebugMode ());
    }

    /**
     * Called after a valid request was processed. This method is only called if
     * the handleRequest method was invoked. If an exception occurred this
     * method is called after
     * {@link #onException(IRequestWebScopeWithoutResponse, UnifiedResponse, Throwable)}
     *
     * @param bExceptionOccurred
     *        if <code>true</code> an exception occurred in request processing.
     */
    @OverrideOnDemand
    default void onRequestEnd (final boolean bExceptionOccurred)
    {}
  }

  private static final class ToUnifiedResponseHttpServletHandler implements IXServletLowLevelHandler
  {
    private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractSimpleHttpServlet.ToUnifiedResponseHttpServletHandler.class);

    private final ISimpleHandler m_aSimpleHandler;
    private final String m_sApplicationID;

    public ToUnifiedResponseHttpServletHandler (@Nonnull final ISimpleHandler aSimpleHandler,
                                                @Nonnull @Nonempty final String sApplicationID)
    {
      m_aSimpleHandler = aSimpleHandler;
      m_sApplicationID = sApplicationID;
    }

    @Nonnull
    @Nonempty
    protected String getApplicationID ()
    {
      return m_sApplicationID;
    }

    private void _onException (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                               @Nonnull final UnifiedResponse aUnifiedResponse,
                               @Nonnull final Throwable t) throws IOException, ServletException
    {
      s_aLogger.error ("An exception was caught in servlet processing for application '" + m_sApplicationID + "'", t);

      // Invoke exception handler
      if (m_aSimpleHandler.onException (aRequestScope, aUnifiedResponse, t).isContinue ())
      {
        // Propagate exception
        if (t instanceof IOException)
          throw (IOException) t;
        if (t instanceof ServletException)
          throw (ServletException) t;
        throw new ServletException (t);
      }
    }

    public void onRequest (@Nonnull final HttpServletRequest aHttpRequest,
                        @Nonnull final HttpServletResponse aHttpResponse,
                        @Nonnull final EHttpVersion eHttpVersion,
                        @Nonnull final EHttpMethod eHttpMethod,
                        @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
    {
      final UnifiedResponse aUnifiedResponse = m_aSimpleHandler.createUnifiedResponse (eHttpVersion,
                                                                                       eHttpMethod,
                                                                                       aHttpRequest);
      if (m_aSimpleHandler.initRequestState (aRequestScope, aUnifiedResponse).isBreak ())
      {
        if (s_aLogger.isDebugEnabled ())
          s_aLogger.debug ("Cancelled request after initRequestState with response " + aUnifiedResponse);

        // May e.g. be an 404 error for some not-found resource
      }
      else
      {
        // Init was successful

        // On request begin
        try
        {
          m_aSimpleHandler.onRequestBegin (aRequestScope);
        }
        catch (final Throwable t)
        {
          _onException (aRequestScope, aUnifiedResponse, t);
        }

        boolean bExceptionOccurred = true;
        try
        {
          // main servlet handling
          m_aSimpleHandler.handleRequest (aRequestScope, aUnifiedResponse);

          // No error occurred
          bExceptionOccurred = false;

          if (s_aLogger.isDebugEnabled ())
            s_aLogger.debug ("Successfully handled request: " + aRequestScope.getPathWithinServlet ());
        }
        catch (final Throwable t)
        {
          // Invoke exception handler
          // This internally re-throws the exception if needed
          _onException (aRequestScope, aUnifiedResponse, t);
        }
        finally
        {
          // On request end
          try
          {
            m_aSimpleHandler.onRequestEnd (bExceptionOccurred);
          }
          catch (final Throwable t)
          {
            s_aLogger.error ("onRequestEnd failed", t);
            // Don't throw anything here
          }
        }
      }
      aUnifiedResponse.applyToResponse (aHttpResponse);
    }
  }

  protected final void registerHandler (@Nonnull final EHttpMethod eMethod,
                                        @Nonnull final ServletAsyncSpec aAsyncSpec,
                                        @Nonnull @Nonempty final String sApplicationID,
                                        @Nonnull final ISimpleHandler aSimpleHandler)
  {
    final IXServletLowLevelHandler aRealHandler = new AsyncXServletHandler (aAsyncSpec,
                                                                    new ToUnifiedResponseHttpServletHandler (aSimpleHandler,
                                                                                                             sApplicationID));
    handlerRegistry ().registerHandler (eMethod, aRealHandler);
  }
}
