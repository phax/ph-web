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

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerTimer;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.timing.StopWatch;
import com.helger.http.EHTTPMethod;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.async.AsyncServletRunnerExecutorService;
import com.helger.servlet.async.ExtAsyncContext;
import com.helger.servlet.async.IAsyncServletRunner;
import com.helger.servlet.async.ServletAsyncSpec;
import com.helger.servlet.request.RequestLogger;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.request.RequestScopeInitializer;
import com.helger.xml.serialize.write.XMLWriterSettings;

/**
 * A thin wrapper around an {@link HttpServlet} that encapsulates the correct
 * Scope handling before and after a request.<br>
 * It overrides all the protected "do*" methods from {@link HttpServlet} and
 * replaced them with protected "on*" methods that can be overridden. The "do*"
 * methods are final to avoid overriding the without the usage of scopes. The
 * default operations of the "on*" methods is to call the original "do*"
 * functionality from the parent class.
 *
 * @author Philip Helger
 */
public abstract class AbstractScopeAwareHttpServlet extends HttpServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractScopeAwareHttpServlet.class);
  private static final IMutableStatisticsHandlerCounter s_aCounterRequests = StatisticsManager.getCounterHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                                  "$requests");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlDelete = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                             "$DELETE");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlGet = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                          "$GET");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlHead = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                           "$HEAD");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlOptions = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                              "$OPTIONS");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlPost = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                           "$POST");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlPut = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                          "$PUT");
  private static final IMutableStatisticsHandlerTimer s_aTimerHdlTrace = StatisticsManager.getTimerHandler (AbstractScopeAwareHttpServlet.class.getName () +
                                                                                                            "$TRACE");

  private static IAsyncServletRunner s_aAsyncServletRunner = new AsyncServletRunnerExecutorService ();

  /**
   * Set the async runner to be used.
   *
   * @param aAsyncServletRunner
   *        The runner to be used. May not be <code>null</code>.
   * @since 8.7.5
   */
  public static void setAsyncServletRunner (@Nonnull final IAsyncServletRunner aAsyncServletRunner)
  {
    ValueEnforcer.notNull (aAsyncServletRunner, "AsyncServletRunner");
    s_aAsyncServletRunner = aAsyncServletRunner;
  }

  /**
   * @return The global async runner. Never <code>null</code>.
   * @since 8.7.5
   */
  @Nonnull
  public static IAsyncServletRunner getAsyncServletRunner ()
  {
    return s_aAsyncServletRunner;
  }

  private final ServletAsyncSpec m_aAsyncSpec;
  // Determine in "init" method
  private transient String m_sStatusApplicationID;

  /**
   * Default constructor for synchronous servlets.
   */
  protected AbstractScopeAwareHttpServlet ()
  {
    // By default synchronous
    this (ServletAsyncSpec.createSync ());
  }

  /**
   * Constructor.
   *
   * @param aAsyncSpec
   *        The async/sync spec to be used. May not be <code>null</code>.
   * @since 8.7.5
   */
  protected AbstractScopeAwareHttpServlet (@Nonnull final ServletAsyncSpec aAsyncSpec)
  {
    m_aAsyncSpec = ValueEnforcer.notNull (aAsyncSpec, "AsyncSpec");
  }

  /**
   * @return <code>true</code> if this servlet acts synchronously,
   *         <code>false</code> if it acts asynchronously.
   * @since 8.7.5
   */
  public final boolean isAsynchronous ()
  {
    return m_aAsyncSpec.isAsynchronous ();
  }

  /**
   * @return The internal async spec. Never <code>null</code>.
   * @since 8.7.5
   */
  @Nonnull
  protected final ServletAsyncSpec internalGetAsyncSpec ()
  {
    return m_aAsyncSpec;
  }

  /**
   * @return The application ID for this servlet.
   */
  @OverrideOnDemand
  protected String getApplicationID ()
  {
    return ClassHelper.getClassLocalName (getClass ());
  }

  /**
   * Add custom init code in overridden implementations of this method.
   *
   * @throws ServletException
   *         to conform to the outer specifications
   */
  @OverrideOnDemand
  protected void onInit () throws ServletException
  {
    /* empty */
  }

  @Override
  public final void init () throws ServletException
  {
    super.init ();
    m_sStatusApplicationID = getApplicationID ();
    if (StringHelper.hasNoText (m_sStatusApplicationID))
      throw new InitializationException ("Failed retrieve a valid application ID! Please override getApplicationID()");
    onInit ();
  }

  /**
   * Add custom destruction code in overridden implementations of this method.
   */
  @OverrideOnDemand
  protected void onDestroy ()
  {
    /* empty */
  }

  @Override
  public final void destroy ()
  {
    onDestroy ();
    s_aAsyncServletRunner.shutdown ();
    super.destroy ();
  }

  /*
   * This method is required to ensure that the HTTP response is correctly
   * encoded. Normally this is done via the charset filter, but if a
   * non-existing URL is accesses then the error redirect happens without the
   * charset filter ever called.
   */
  private static void _ensureResponseCharset (@Nonnull final HttpServletResponse aHttpResponse)
  {
    if (aHttpResponse.getCharacterEncoding () == null)
    {
      s_aLogger.info ("Setting response charset to " + XMLWriterSettings.DEFAULT_XML_CHARSET);
      aHttpResponse.setCharacterEncoding (XMLWriterSettings.DEFAULT_XML_CHARSET);
    }
  }

  /**
   * This method logs errors, in case a HttpServletRequest object is missing
   * basic information
   *
   * @param sMsg
   *        The concrete message to emit. May not be <code>null</code>.
   * @param aHttpRequest
   *        The current HTTP request. May not be <code>null</code>.
   */
  @OverrideOnDemand
  protected void logInvalidRequestSetup (@Nonnull final String sMsg, @Nonnull final HttpServletRequest aHttpRequest)
  {
    final StringBuilder aSB = new StringBuilder (sMsg).append (":\n");
    aSB.append (RequestLogger.getRequestComplete (aHttpRequest));
    s_aLogger.error (aSB.toString ());
  }

  /**
   * Called before every request, independent of the method
   *
   * @param aHttpRequest
   *        The HTTP servlet request
   * @param aHttpResponse
   *        The HTTP servlet response
   * @return the created request scope
   */
  @OverrideOnDemand
  @OverridingMethodsMustInvokeSuper
  protected RequestScopeInitializer beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                                   @Nonnull final HttpServletResponse aHttpResponse)
  {
    if (aHttpRequest.getScheme () == null)
      logInvalidRequestSetup ("HTTP request has no scheme", aHttpRequest);
    if (aHttpRequest.getProtocol () == null)
      logInvalidRequestSetup ("HTTP request has no protocol", aHttpRequest);
    if (ServletHelper.getRequestContextPath (aHttpRequest) == null)
      logInvalidRequestSetup ("HTTP request has no context path", aHttpRequest);

    final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (m_sStatusApplicationID,
                                                                                             aHttpRequest,
                                                                                             aHttpResponse);
    _ensureResponseCharset (aHttpResponse);
    s_aCounterRequests.increment ();
    return aRequestScopeInitializer;
  }

  /**
   * Dummy interface matching the on... protected methods so I can pass them as
   * method references.
   *
   * @author Philip Helger
   * @since 8.7.5
   */
  private static interface IRunner
  {
    void run (@Nonnull final HttpServletRequest aHttpRequest,
              @Nonnull final HttpServletResponse aHttpResponse,
              @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException;
  }

  /**
   * Run a servlet request synchronously.
   *
   * @param aHttpRequest
   *        HTTP request
   * @param aHttpResponse
   *        HTTP response
   * @param aRunner
   *        Method reference to the protected "on..." method that does the main
   *        work
   * @param aTimer
   *        The time to which this is to be added.
   * @throws ServletException
   *         On error
   * @throws IOException
   *         On error
   * @since 8.7.5
   */
  private void _perform (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final IRunner aRunner,
                         @Nonnull final IMutableStatisticsHandlerTimer aTimer) throws ServletException, IOException
  {
    final RequestScopeInitializer aRequestScopeInitializer = beforeRequest (aHttpRequest, aHttpResponse);
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      aRunner.run (aHttpRequest, aHttpResponse, aRequestScopeInitializer.getRequestScope ());
    }
    finally
    {
      aTimer.addTime (aSW.stopAndGetMillis ());
      aRequestScopeInitializer.destroyScope ();
    }
  }

  private void _run (@Nonnull final HttpServletRequest aHttpRequest,
                     @Nonnull final HttpServletResponse aHttpResponse,
                     @Nonnull final EHTTPMethod eHTTPMethod,
                     @Nonnull final IRunner aRunner,
                     @Nonnull final IMutableStatisticsHandlerTimer aTimer) throws ServletException, IOException
  {
    if (s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("Servlet(" +
                       getClass ().getSimpleName () +
                       "): asyncSupported=" +
                       aHttpRequest.isAsyncSupported () +
                       "; asyncStarted=" +
                       aHttpRequest.isAsyncStarted ());

    if (m_aAsyncSpec.isAsynchronous () && m_aAsyncSpec.isAsyncHTTPMethod (eHTTPMethod))
    {
      // Run asynchronously
      final ExtAsyncContext aAsyncContext = ExtAsyncContext.create (aHttpRequest, aHttpResponse, m_aAsyncSpec);
      // This could theoretically a member except that it references aRunner and
      // aTimer
      final Consumer <ExtAsyncContext> aAsyncRunner = aEAC -> {
        try
        {
          if (s_aLogger.isDebugEnabled ())
            s_aLogger.debug ("ASYNC request processing started: " + aEAC.getRequest ());
          _perform (aEAC.getRequest (), aEAC.getResponse (), aRunner, aTimer);
        }
        catch (final Throwable t)
        {
          s_aLogger.error ("Error processing async request " + aEAC.getRequest (), t);
          try
          {
            aEAC.getResponse ().getWriter ().write (
                                                    "Internal error processing your request. Please try again later. Technical details: " +
                                                    t.getClass ().getName () +
                                                    ":" +
                                                    t.getMessage ());
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
            aEAC.complete ();
          }
          catch (final Throwable t)
          {
            s_aLogger.error ("Error completing async context", t);
          }
        }
      };

      // Put into async processing queue
      s_aAsyncServletRunner.runAsync (aHttpRequest, aHttpResponse, aAsyncRunner, aAsyncContext);
    }
    else
    {
      // Run synchronously
      _perform (aHttpRequest, aHttpResponse, aRunner, aTimer);
    }
  }

  /**
   * Implement HTTP DELETE
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onDelete (@Nonnull final HttpServletRequest aHttpRequest,
                           @Nonnull final HttpServletResponse aHttpResponse,
                           @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doDelete (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doDelete (@Nonnull final HttpServletRequest aHttpRequest,
                                 @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.DELETE, this::onDelete, s_aTimerHdlDelete);
  }

  /**
   * Implement HTTP GET
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onGet (@Nonnull final HttpServletRequest aHttpRequest,
                        @Nonnull final HttpServletResponse aHttpResponse,
                        @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doGet (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doGet (@Nonnull final HttpServletRequest aHttpRequest,
                              @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.GET, this::onGet, s_aTimerHdlGet);
  }

  /**
   * Implement HTTP HEAD
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onHead (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doHead (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doHead (@Nonnull final HttpServletRequest aHttpRequest,
                               @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.HEAD, this::onHead, s_aTimerHdlHead);
  }

  /**
   * Implement HTTP OPTIONS
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onOptions (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doOptions (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doOptions (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.OPTIONS, this::onOptions, s_aTimerHdlOptions);
  }

  /**
   * Implement HTTP POST
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onPost (@Nonnull final HttpServletRequest aHttpRequest,
                         @Nonnull final HttpServletResponse aHttpResponse,
                         @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doPost (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doPost (@Nonnull final HttpServletRequest aHttpRequest,
                               @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.POST, this::onPost, s_aTimerHdlPost);
  }

  /**
   * Implement HTTP PUT
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onPut (@Nonnull final HttpServletRequest aHttpRequest,
                        @Nonnull final HttpServletResponse aHttpResponse,
                        @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doPut (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doPut (@Nonnull final HttpServletRequest aHttpRequest,
                              @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.PUT, this::onPut, s_aTimerHdlPut);
  }

  /**
   * Implement HTTP TRACE
   *
   * @param aHttpRequest
   *        The original HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        The original HTTP response. Never <code>null</code>.
   * @param aRequestScope
   *        The request scope to be used. Never <code>null</code>.
   * @throws ServletException
   *         In case of an error.
   * @throws IOException
   *         In case of an error.
   */
  @OverrideOnDemand
  protected void onTrace (@Nonnull final HttpServletRequest aHttpRequest,
                          @Nonnull final HttpServletResponse aHttpResponse,
                          @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    super.doTrace (aHttpRequest, aHttpResponse);
  }

  @Override
  protected final void doTrace (@Nonnull final HttpServletRequest aHttpRequest,
                                @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    _run (aHttpRequest, aHttpResponse, EHTTPMethod.TRACE, this::onTrace, s_aTimerHdlTrace);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("AsyncSpec", m_aAsyncSpec)
                                       .append ("ApplicationID", m_sStatusApplicationID)
                                       .getToString ();
  }
}
