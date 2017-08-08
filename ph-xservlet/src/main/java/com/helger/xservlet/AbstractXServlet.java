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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.state.EChange;
import com.helger.commons.state.EContinue;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedTimer;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.timing.StopWatch;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.scope.mgr.ScopeManager;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.StaticServerInfo;
import com.helger.servlet.http.CountingOnlyHttpServletResponse;
import com.helger.servlet.request.RequestLogger;
import com.helger.servlet.response.ERedirectMode;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.web.scope.request.RequestScopeInitializer;
import com.helger.xservlet.filter.IXServletFilter;
import com.helger.xservlet.filter.XServletFilterConsistency;
import com.helger.xservlet.filter.XServletFilterSecurity;
import com.helger.xservlet.forcedredirect.ForcedRedirectException;
import com.helger.xservlet.forcedredirect.ForcedRedirectManager;
import com.helger.xservlet.handler.IXServletHandler;
import com.helger.xservlet.handler.XServletHandlerOPTIONS;
import com.helger.xservlet.handler.XServletHandlerRegistry;
import com.helger.xservlet.handler.XServletHandlerTRACE;
import com.helger.xservlet.requesttrack.RequestTracker;
import com.helger.xservlet.servletstatus.ServletStatusManager;

/**
 * Abstract HTTP based servlet. Compared to the default
 * {@link javax.servlet.http.HttpServlet} this class uses a handler map with
 * {@link EHttpMethod} as the key.<br>
 * The following features are added compared to the default servlet
 * implementation:
 * <ul>
 * <li>It has counting statistics</li>
 * <li>It has timing statistics</li>
 * <li>It enforces a character set on the response</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 8.0.0
 */
public abstract class AbstractXServlet extends GenericServlet
{
  /** The name of the request attribute uniquely identifying the request ID */
  public static final String REQUEST_ATTR_ID = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL + "request.id";

  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractXServlet.class);
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsTotal = StatisticsManager.getCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                       "$requests.total");
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsAccepted = StatisticsManager.getCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                          "$requests.accepted");
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsHandled = StatisticsManager.getCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                         "$requests.handled");
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsPRG = StatisticsManager.getCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                     "$requests.post-redirect-get");
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsWithException = StatisticsManager.getCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                               "$requests.withexception");
  private static final IMutableStatisticsHandlerKeyedCounter s_aCounterRequestsPerVersionAccepted = StatisticsManager.getKeyedCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                                              "$requests-per-version.accepted");
  private static final IMutableStatisticsHandlerKeyedCounter s_aCounterRequestsPerVersionHandled = StatisticsManager.getKeyedCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                                             "$requests-per-version.handled");
  private static final IMutableStatisticsHandlerKeyedCounter s_aCounterRequestsPerMethodAccepted = StatisticsManager.getKeyedCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                                             "$requests-per-method.accepted");
  private static final IMutableStatisticsHandlerKeyedCounter s_aCounterRequestsPerMethodHandled = StatisticsManager.getKeyedCounterHandler (AbstractXServlet.class.getName () +
                                                                                                                                            "$requests-per-method.handled");
  private final IMutableStatisticsHandlerKeyedCounter m_aCounterHttpMethodUnhandled = StatisticsManager.getKeyedCounterHandler (getClass ().getName () +
                                                                                                                                "$method.unhandled");
  private static final IMutableStatisticsHandlerKeyedTimer s_aTimer = StatisticsManager.getKeyedTimerHandler (AbstractXServlet.class);

  /** Thread-safe request counter */
  private static final AtomicLong s_aRequestID = new AtomicLong (0);
  /** Indicator whether it is the first request or not */
  private static final AtomicBoolean s_aFirstRequest = new AtomicBoolean (true);

  private final ServletStatusManager m_aStatusMgr;

  /** The main handler map */
  private final XServletHandlerRegistry m_aHandlerRegistry = new XServletHandlerRegistry ();
  private final ICommonsList <IXServletFilter> m_aFilterList = new CommonsArrayList <> ();

  // Status variables
  // Determined in "init" method
  private transient String m_sApplicationID;

  /**
   * Does nothing, because this is an abstract class.
   */
  public AbstractXServlet ()
  {
    // This handler is always the same, so it is registered here for convenience
    m_aHandlerRegistry.registerHandler (EHttpMethod.TRACE, new XServletHandlerTRACE ());

    // Default HEAD handler -> invoke with GET
    m_aHandlerRegistry.registerHandler (EHttpMethod.HEAD,
                                        (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope) -> {
                                          final CountingOnlyHttpServletResponse aResponseWrapper = new CountingOnlyHttpServletResponse (aHttpResponse);
                                          _internalService (aHttpRequest,
                                                            aResponseWrapper,
                                                            eHttpVersion,
                                                            EHttpMethod.GET,
                                                            aRequestScope);
                                          aResponseWrapper.setContentLengthAutomatically ();
                                        });

    // Default OPTIONS handler
    m_aHandlerRegistry.registerHandler (EHttpMethod.OPTIONS,
                                        new XServletHandlerOPTIONS (m_aHandlerRegistry::getAllowedHttpMethodsString));

    // Remember to avoid crash on shutdown, when no GlobalScope is present
    m_aStatusMgr = ServletStatusManager.getInstance ();
    m_aStatusMgr.onServletCtor (getClass ());
  }

  @Nonnull
  @ReturnsMutableObject
  protected final XServletHandlerRegistry handlerRegistry ()
  {
    return m_aHandlerRegistry;
  }

  @Nonnull
  @ReturnsMutableObject
  protected final ICommonsList <IXServletFilter> filterList ()
  {
    return m_aFilterList;
  }

  /**
   * @return The application ID for this servlet. Called only once during
   *         initialization!
   */
  @OverrideOnDemand
  protected String getInitApplicationID ()
  {
    return ClassHelper.getClassLocalName (getClass ());
  }

  /**
   * A final overload of "init". Overload "init" instead.
   */
  @Override
  public final void init (@Nonnull final ServletConfig aSC) throws ServletException
  {
    super.init (aSC);
    m_aStatusMgr.onServletInit (getClass ());

    m_sApplicationID = getInitApplicationID ();
    if (StringHelper.hasNoText (m_sApplicationID))
      throw new InitializationException ("Failed retrieve a valid application ID! Please check your overriden getInitApplicationID()");
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void destroy ()
  {
    m_aStatusMgr.onServletDestroy (getClass ());
    super.destroy ();
  }

  @Nonnull
  @OverrideOnDemand
  protected UnifiedResponse createUnifiedResponse (@Nonnull final EHttpVersion eHttpVersion,
                                                   @Nonnull final EHttpMethod eHttpMethod,
                                                   @Nonnull final HttpServletRequest aHttpRequest)
  {
    return new UnifiedResponse (eHttpVersion, eHttpMethod, aHttpRequest);
  }

  @Nonnull
  private static EChange _trackBeforeHandleRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    // Check if an attribute is already present
    // An ID may already be present, if the request is internally dispatched
    // (e.g. via the error handler)
    String sID = aRequestScope.attrs ().getAsString (REQUEST_ATTR_ID);
    if (sID != null)
    {
      s_aLogger.info ("Request already contains an ID (" + sID + ") - so this is an recursive request...");
      return EChange.UNCHANGED;
    }

    // Create a unique ID for the request
    sID = Long.toString (s_aRequestID.incrementAndGet ());
    aRequestScope.attrs ().putIn (REQUEST_ATTR_ID, sID);
    RequestTracker.addRequest (sID, aRequestScope);
    return EChange.CHANGED;
  }

  private static void _trackAfterHandleRequest (@Nonnull final IRequestWebScope aRequestScope)
  {
    final String sID = aRequestScope.attrs ().getAsString (REQUEST_ATTR_ID);
    RequestTracker.removeRequest (sID);
  }

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
  protected EContinue onException (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                   @Nonnull final UnifiedResponse aUnifiedResponse,
                                   @Nonnull final Throwable t)
  {
    final String sMsg = "Internal error on HTTP " +
                        aRequestScope.getMethod () +
                        " on resource '" +
                        aRequestScope.getURL () +
                        "' - Application ID '" +
                        m_sApplicationID +
                        "'";

    if (StreamHelper.isKnownEOFException (t))
    {
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug (sMsg + " - " + ClassHelper.getClassLocalName (t) + " - " + t.getMessage ());

      // Never propagate
      return EContinue.BREAK;
    }

    // Log always including full exception
    s_aLogger.error (sMsg, t);

    // Propagate only in debug mode
    return EContinue.valueOf (GlobalDebug.isDebugMode ());
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
  protected void onRequestBegin (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {}

  /**
   * Called after a valid request was processed. This method is only called if
   * the handleRequest method was invoked. If an exception occurred this method
   * is called after
   * {@link #onException(IRequestWebScopeWithoutResponse, UnifiedResponse, Throwable)}
   *
   * @param bExceptionOccurred
   *        if <code>true</code> an exception occurred in request processing.
   */
  @OverrideOnDemand
  protected void onRequestEnd (final boolean bExceptionOccurred)
  {}

  private void _internalService (@Nonnull final HttpServletRequest aHttpRequest,
                                 @Nonnull final HttpServletResponse aHttpResponse,
                                 @Nonnull final EHttpVersion eHttpVersion,
                                 @Nonnull final EHttpMethod eHttpMethod,
                                 @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    // HTTP version and method are valid
    s_aCounterRequestsAccepted.increment ();

    // Find the handler for the HTTP method
    final IXServletHandler aHandler = m_aHandlerRegistry.getHandler (eHttpMethod);
    if (aHandler == null)
    {
      // HTTP method is not supported by this servlet!
      m_aCounterHttpMethodUnhandled.increment (eHttpMethod.getName ());

      aHttpResponse.setHeader (CHttpHeader.ALLOW, m_aHandlerRegistry.getAllowedHttpMethodsString ());
      if (eHttpVersion == EHttpVersion.HTTP_11)
        aHttpResponse.sendError (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      else
        aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // before-callback
    try
    {
      onRequestBegin (aRequestScope);
    }
    catch (final Throwable t)
    {
      s_aLogger.error ("onRequestBegin failed", t);
    }

    // Build the response
    final UnifiedResponse aUnifiedResponse = createUnifiedResponse (eHttpVersion, eHttpMethod, aHttpRequest);

    // HTTP method is supported by this servlet implementation
    final StopWatch aSW = StopWatch.createdStarted ();
    boolean bTrackedRequest = false;
    boolean bExceptionOccurred = true;
    try
    {
      bTrackedRequest = _trackBeforeHandleRequest (aRequestScope).isChanged ();

      // This may indirectly call "_internalService" again (e.g. for HEAD
      // requests, which calls GET internally)
      aHandler.handle (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope);

      // No error occurred
      bExceptionOccurred = false;

      // Handled and no exception
      s_aCounterRequestsHandled.increment ();
      s_aCounterRequestsPerVersionHandled.increment (eHttpVersion.getName ());
      s_aCounterRequestsPerMethodHandled.increment (eHttpMethod.getName ());
    }
    catch (final ForcedRedirectException ex)
    {
      s_aCounterRequestsPRG.increment ();

      // Remember the content
      ForcedRedirectManager.getInstance ().createForcedRedirect (ex);
      // And set the redirect
      aUnifiedResponse.setRedirect (ex.getRedirectTargetURL (), ERedirectMode.POST_REDIRECT_GET);
      // Stop exception handling
      aUnifiedResponse.applyToResponse (aHttpResponse);
    }
    catch (final Throwable t)
    {
      s_aCounterRequestsWithException.increment ();

      // Invoke exception handler (includes Post-Redirect-Handling)
      if (onException (aRequestScope, aUnifiedResponse, t).isContinue ())
      {
        // Propagate exception
        if (t instanceof IOException)
          throw (IOException) t;
        if (t instanceof ServletException)
          throw (ServletException) t;
        throw new ServletException (t);
      }

      // E.g. Post-Redirect-Get is handled with this
      aUnifiedResponse.applyToResponse (aHttpResponse);
    }
    finally
    {
      if (bTrackedRequest)
      {
        // Track after only if tracked on the beginning
        _trackAfterHandleRequest (aRequestScope);
      }

      // after-callback
      try
      {
        onRequestEnd (bExceptionOccurred);
      }
      catch (final Throwable t)
      {
        s_aLogger.error ("onRequestEnd failed", t);
      }

      // Timer per HTTP method
      s_aTimer.addTime (eHttpMethod.getName (), aSW.stopAndGetMillis ());
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
    final String sFullMsg = aSB.toString ();
    s_aLogger.error (sFullMsg);
    log (sFullMsg);
  }

  /**
   * Dispatches client requests to the protected <code>service</code> method.
   * There's no need to override this method.
   *
   * @param aRequest
   *        the {@link HttpServletRequest} object that contains the request the
   *        client made of the servlet
   * @param aResponse
   *        the {@link HttpServletResponse} object that contains the response
   *        the servlet returns to the client
   * @exception IOException
   *            if an input or output error occurs while the servlet is handling
   *            the HTTP request
   * @exception ServletException
   *            if the HTTP request cannot be handled
   * @see javax.servlet.Servlet#service
   */
  @Override
  public final void service (@Nonnull final ServletRequest aRequest,
                             @Nonnull final ServletResponse aResponse) throws ServletException, IOException
  {
    ValueEnforcer.isInstanceOf (aRequest, HttpServletRequest.class, "Non-HTTP servlet request");
    ValueEnforcer.isInstanceOf (aRequest, HttpServletResponse.class, "Non-HTTP servlet response");

    final HttpServletRequest aHttpRequest = (HttpServletRequest) aRequest;
    final HttpServletResponse aHttpResponse = (HttpServletResponse) aResponse;

    // Increase counter
    s_aCounterRequestsTotal.increment ();

    // Increase per servlet invocation
    m_aStatusMgr.onServletInvocation (getClass ());

    // Set the last application ID in the session
    // PhotonSessionState.getInstance ().setLastApplicationID
    // (m_sApplicationID);

    // Ensure a valid HTTP version is provided
    final String sProtocol = aHttpRequest.getProtocol ();
    final EHttpVersion eHttpVersion = EHttpVersion.getFromNameOrNull (sProtocol);
    if (eHttpVersion == null)
    {
      // HTTP version disallowed
      logInvalidRequestSetup ("Request has unsupported HTTP version (" + sProtocol + ")!", aHttpRequest);
      aHttpResponse.sendError (HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
      return;
    }
    s_aCounterRequestsPerVersionAccepted.increment (eHttpVersion.getName ());

    // Ensure a valid HTTP method is provided
    final String sMethod = aHttpRequest.getMethod ();
    final EHttpMethod eHttpMethod = EHttpMethod.getFromNameOrNull (sMethod);
    if (eHttpMethod == null)
    {
      // HTTP method unknown
      logInvalidRequestSetup ("Request has unsupported HTTP method (" + sMethod + ")!", aHttpRequest);
      aHttpResponse.sendError (HttpServletResponse.SC_NOT_IMPLEMENTED);
      return;
    }
    s_aCounterRequestsPerMethodAccepted.increment (eHttpMethod.getName ());

    // May already be set in test cases!
    if (s_aFirstRequest.getAndSet (false) && !StaticServerInfo.isSet ())
    {
      // First set the default web server info
      StaticServerInfo.init (aHttpRequest.getScheme (),
                             aHttpRequest.getServerName (),
                             aHttpRequest.getServerPort (),
                             ServletContextPathHolder.getContextPath ());
    }

    // Create a wrapper around the Servlet Response that saves the status code
    final StatusAwareHttpResponseWrapper aHttpResponseWrapper = new StatusAwareHttpResponseWrapper (aHttpResponse);

    // Create effective filter list with all internal filters as well
    final ICommonsList <IXServletFilter> aEffectiveFilterList = new CommonsArrayList <> ();
    aEffectiveFilterList.add (XServletFilterSecurity.INSTANCE);
    aEffectiveFilterList.add (new XServletFilterConsistency ());
    aEffectiveFilterList.addAll (m_aFilterList);

    // Create request scope
    final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (m_sApplicationID,
                                                                                             aHttpRequest,
                                                                                             aHttpResponseWrapper);
    try
    {
      final IRequestWebScope aRequestScope = aRequestScopeInitializer.getRequestScope ();

      // Filter before
      for (final IXServletFilter aFilter : aEffectiveFilterList)
        if (aFilter.beforeRequest (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod, aRequestScope)
                   .isBreak ())
          return;

      try
      {
        // Determine handler
        _internalService (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod, aRequestScope);
      }
      finally
      {
        // Filter after
        for (final IXServletFilter aFilter : aEffectiveFilterList)
          aFilter.afterRequest (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod, aRequestScope);
      }
    }
    finally
    {
      // Destroy request scope
      aRequestScopeInitializer.destroyScope ();
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("HandlerRegistry", m_aHandlerRegistry)
                                       .append ("ApplicationID", m_sApplicationID)
                                       .getToString ();
  }
}
