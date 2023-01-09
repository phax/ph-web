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
package com.helger.xservlet;

import java.io.IOException;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.callback.CallbackList;
import com.helger.commons.state.EContinue;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.ToStringGenerator;
import com.helger.servlet.filter.AbstractHttpServletFilter;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.impl.RequestWebScope;
import com.helger.web.scope.multipart.RequestWebScopeMultipart;
import com.helger.web.scope.request.RequestScopeInitializer;
import com.helger.xservlet.exception.IXServletExceptionHandler;
import com.helger.xservlet.exception.XServletLoggingExceptionHandler;

/**
 * Abstract HTTP based filter. It is aligned with {@link AbstractXServlet} and
 * should bring similar abstraction level.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@NotThreadSafe
public abstract class AbstractXFilter extends AbstractHttpServletFilter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractXFilter.class);

  private final IMutableStatisticsHandlerCounter m_aCounterRequestsTotal = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                "$requests.total");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsBeforeContinue = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                         "$requests.before-continue");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsBeforeBreak = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                      "$requests.before-break");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsWithException = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                        "$requests.withexception");

  /** The main handler map */
  private final CallbackList <IXServletExceptionHandler> m_aExceptionHandler = new CallbackList <> ();

  /** By default Multipart handling is enabled */
  private boolean m_bIsMultipartEnabled = true;

  /**
   * Constructor.
   */
  public AbstractXFilter ()
  {
    m_aExceptionHandler.add (new XServletLoggingExceptionHandler ());
  }

  /**
   * @return The internal exception handler list. Never <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableObject
  protected final CallbackList <IXServletExceptionHandler> exceptionHandler ()
  {
    return m_aExceptionHandler;
  }

  /**
   * @return <code>true</code> if multipart handling is enabled (default),
   *         <code>false</code> if not.
   * @since 9.1.1
   */
  protected final boolean isMultipartEnabled ()
  {
    return m_bIsMultipartEnabled;
  }

  /**
   * Enable/disable multipart handling in this filter (works only if the request
   * scope is created here)
   * 
   * @param bMultipartEnabled
   *        <code>true</code> to enable, <code>false</code> to disable
   * @since 9.1.1
   */
  protected final void setMultipartEnabled (final boolean bMultipartEnabled)
  {
    m_bIsMultipartEnabled = bMultipartEnabled;
  }

  /**
   * Invoked before the rest of the request is processed.
   *
   * @param aHttpRequest
   *        The HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        Current request scope. Never <code>null</code>.
   * @return {@link EContinue#CONTINUE} to continue processing the request,
   *         {@link EContinue#BREAK} otherwise.
   * @throws IOException
   *         In case of IO error
   * @throws ServletException
   *         In case of business level error
   */
  @Nonnull
  @OverrideOnDemand
  public EContinue onFilterBefore (@Nonnull final HttpServletRequest aHttpRequest,
                                   @Nonnull final HttpServletResponse aHttpResponse,
                                   @Nonnull final IRequestWebScope aRequestScope) throws IOException, ServletException
  {
    // By default continue
    return EContinue.CONTINUE;
  }

  /**
   * Invoked after the rest of the request was processed.
   *
   * @param aHttpRequest
   *        The HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        Current request scope. Never <code>null</code>.
   * @throws IOException
   *         In case of IO error
   * @throws ServletException
   *         In case of business level error
   */
  @OverrideOnDemand
  public void onFilterAfter (@Nonnull final HttpServletRequest aHttpRequest,
                             @Nonnull final HttpServletResponse aHttpResponse,
                             @Nonnull final IRequestWebScope aRequestScope) throws IOException, ServletException
  {}

  @Override
  public void doHttpFilter (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final FilterChain aChain) throws IOException, ServletException
  {
    // Increase counter
    m_aCounterRequestsTotal.increment ();

    // Create a wrapper around the Servlet Response that saves the status code
    final StatusAwareHttpResponseWrapper aHttpResponseWrapper = StatusAwareHttpResponseWrapper.wrap (aHttpResponse);

    // Create request scope
    final BiFunction <? super HttpServletRequest, ? super HttpServletResponse, IRequestWebScope> aFactory;
    aFactory = m_bIsMultipartEnabled ? RequestWebScopeMultipart::new : RequestWebScope::new;

    // Create request scope
    try (final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (aHttpRequest,
                                                                                                  aHttpResponseWrapper,
                                                                                                  aFactory))
    {
      final IRequestWebScope aRequestScope = aRequestScopeInitializer.getRequestScope ();
      try
      {
        if (onFilterBefore (aHttpRequest, aHttpResponseWrapper, aRequestScope).isContinue ())
        {
          // Continue request processing
          m_aCounterRequestsBeforeContinue.increment ();
          aChain.doFilter (aHttpRequest, aHttpResponse);
          onFilterAfter (aHttpRequest, aHttpResponseWrapper, aRequestScope);
        }
        else
        {
          m_aCounterRequestsBeforeBreak.increment ();
        }
      }
      catch (final Exception ex)
      {
        m_aCounterRequestsWithException.increment ();

        if (m_aExceptionHandler.forEachBreakable (x -> x.onException (aRequestScope, ex)).isContinue ())
        {
          // This log entry is mainly present to have an overview on how often
          // this really happens
          LOGGER.error ("Filter exception propagated to the outside", ex);

          // Ensure only exceptions with the correct type are propagated
          if (ex instanceof IOException)
            throw (IOException) ex;
          if (ex instanceof ServletException)
            throw (ServletException) ex;
          throw new ServletException ("Wrapped " + ex.getClass ().getName (), ex);
        }
      }
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ExceptionHandler", m_aExceptionHandler).getToString ();
  }
}
