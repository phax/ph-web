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
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.helger.commons.callback.CallbackList;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedCounter;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletSettings;
import com.helger.servlet.StaticServerInfo;
import com.helger.servlet.http.CountingOnlyHttpServletResponse;
import com.helger.servlet.request.RequestLogger;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.request.RequestScopeInitializer;
import com.helger.xservlet.exception.IXServletExceptionHandler;
import com.helger.xservlet.exception.XServletLoggingExceptionHandler;
import com.helger.xservlet.filter.IXServletHighLevelFilter;
import com.helger.xservlet.filter.IXServletLowLevelFilter;
import com.helger.xservlet.filter.XServletFilterConsistency;
import com.helger.xservlet.filter.XServletFilterSecurity;
import com.helger.xservlet.filter.XServletFilterTimer;
import com.helger.xservlet.filter.XServletFilterTrackRequest;
import com.helger.xservlet.forcedredirect.ForcedRedirectException;
import com.helger.xservlet.forcedredirect.ForcedRedirectManager;
import com.helger.xservlet.handler.IXServletHandler;
import com.helger.xservlet.handler.XServletHandlerOPTIONS;
import com.helger.xservlet.handler.XServletHandlerRegistry;
import com.helger.xservlet.handler.XServletHandlerTRACE;
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
 * <li>It checks for known security attacks (like httpoxy)</li>
 * <li>It has custom handler per HTTP method</li>
 * <li>It has custom filter</li>
 * <li>It has custom exception handler</li>
 * <li>It handles Post-Redirect-Get centrally.</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public abstract class AbstractXServlet extends GenericServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractXServlet.class);

  private final IMutableStatisticsHandlerCounter m_aCounterRequestsTotal = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                "$requests.total");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsAccepted = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                   "$requests.accepted");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsHandled = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                  "$requests.handled");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsPRG = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                              "$requests.post-redirect-get");
  private final IMutableStatisticsHandlerCounter m_aCounterRequestsWithException = StatisticsManager.getCounterHandler (getClass ().getName () +
                                                                                                                        "$requests.withexception");
  private final IMutableStatisticsHandlerKeyedCounter m_aCounterRequestsPerVersionAccepted = StatisticsManager.getKeyedCounterHandler (getClass ().getName () +
                                                                                                                                       "$requests-per-version.accepted");
  private final IMutableStatisticsHandlerKeyedCounter m_aCounterRequestsPerVersionHandled = StatisticsManager.getKeyedCounterHandler (getClass ().getName () +
                                                                                                                                      "$requests-per-version.handled");
  private final IMutableStatisticsHandlerKeyedCounter m_aCounterRequestsPerMethodAccepted = StatisticsManager.getKeyedCounterHandler (getClass ().getName () +
                                                                                                                                      "$requests-per-method.accepted");
  private final IMutableStatisticsHandlerKeyedCounter m_aCounterRequestsPerMethodHandled = StatisticsManager.getKeyedCounterHandler (getClass ().getName () +
                                                                                                                                     "$requests-per-method.handled");
  private final IMutableStatisticsHandlerKeyedCounter m_aCounterHttpMethodUnhandled = StatisticsManager.getKeyedCounterHandler (getClass ().getName () +
                                                                                                                                "$method.unhandled");

  /** Indicator whether it is the first request or not */
  private static final AtomicBoolean s_aFirstRequest = new AtomicBoolean (true);
  /** The main handler map */
  private final XServletHandlerRegistry m_aHandlerRegistry = new XServletHandlerRegistry ();
  private final ICommonsList <IXServletLowLevelFilter> m_aFilterList = new CommonsArrayList <> ();
  private final CallbackList <IXServletExceptionHandler> m_aExceptionHandler = new CallbackList <> ();

  // Status variables
  // Remember to avoid crash on shutdown, when no GlobalScope is present
  private final ServletStatusManager m_aStatusMgr;
  // Determined in "init" method
  private String m_sApplicationID;

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
                                          // Change method from HEAD to GET!
                                          _invokeHandler (aHttpRequest,
                                                          aResponseWrapper,
                                                          eHttpVersion,
                                                          EHttpMethod.GET,
                                                          aRequestScope);
                                          aResponseWrapper.setContentLengthAutomatically ();
                                        });

    // Default OPTIONS handler
    m_aHandlerRegistry.registerHandler (EHttpMethod.OPTIONS,
                                        new XServletHandlerOPTIONS (m_aHandlerRegistry::getAllowedHttpMethodsString));

    m_aExceptionHandler.add (new XServletLoggingExceptionHandler ());

    // Remember once
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
  protected final ICommonsList <IXServletLowLevelFilter> filterList ()
  {
    return m_aFilterList;
  }

  @Nonnull
  @ReturnsMutableObject
  protected final CallbackList <IXServletExceptionHandler> exceptionHandler ()
  {
    return m_aExceptionHandler;
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

  @Override
  public final void log (final String sMsg)
  {
    super.log (sMsg);
    s_aLogger.info (sMsg);
  }

  @Override
  public final void log (final String sMsg, final Throwable t)
  {
    super.log (sMsg, t);
    s_aLogger.error (sMsg, t);
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

    // Build init parameter map
    final ICommonsMap <String, String> aInitParams = new CommonsHashMap <> ();
    final Enumeration <String> aEnum = aSC.getInitParameterNames ();
    while (aEnum.hasMoreElements ())
    {
      final String sName = aEnum.nextElement ();
      aInitParams.put (sName, aSC.getInitParameter (sName));
    }
    // Invoke each handler for potential initialization
    m_aHandlerRegistry.forEachHandler (x -> x.onServletInit (m_sApplicationID, aInitParams));
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public final void destroy ()
  {
    // Invoke each handler for potential destruction
    m_aHandlerRegistry.forEachHandler (x -> x.onServletDestroy ());

    // Unregister
    m_aStatusMgr.onServletDestroy (getClass ());

    // Further cleanup
    super.destroy ();
  }

  private void _invokeHandler (@Nonnull final HttpServletRequest aHttpRequest,
                               @Nonnull final HttpServletResponse aHttpResponse,
                               @Nonnull final EHttpVersion eHttpVersion,
                               @Nonnull final EHttpMethod eHttpMethod,
                               @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    // HTTP version and method are valid
    m_aCounterRequestsAccepted.increment ();

    // Find the handler for the HTTP method
    // Important: must be done inside this method to handle "HEAD" requests
    // properly!
    final IXServletHandler aServletHandler = m_aHandlerRegistry.getHandler (eHttpMethod);
    if (aServletHandler == null)
    {
      // HTTP method is not supported by this servlet!
      m_aCounterHttpMethodUnhandled.increment (eHttpMethod.getName ());

      aHttpResponse.setHeader (CHttpHeader.ALLOW, m_aHandlerRegistry.getAllowedHttpMethodsString ());
      if (eHttpVersion.is10 ())
        aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
      else
        aHttpResponse.sendError (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    // HTTP method is supported by this servlet implementation
    final ICommonsList <IXServletHighLevelFilter> aEffectiveFilters = new CommonsArrayList <> ();
    aEffectiveFilters.add (new XServletFilterTimer (this));
    aEffectiveFilters.add (new XServletFilterTrackRequest ());

    try
    {
      // High level filters before
      for (final IXServletHighLevelFilter aFilter : aEffectiveFilters)
        aFilter.beforeRequest (aRequestScope);

      // This may indirectly call "_internalService" again (e.g. for HEAD
      // requests, which calls GET internally)
      aServletHandler.onRequest (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod, aRequestScope);

      // Handled and no exception
      m_aCounterRequestsHandled.increment ();
      m_aCounterRequestsPerVersionHandled.increment (eHttpVersion.getName ());
      m_aCounterRequestsPerMethodHandled.increment (eHttpMethod.getName ());
    }
    catch (final ForcedRedirectException ex)
    {
      // Handle Post-Redirect-Get here
      m_aCounterRequestsPRG.increment ();

      // Remember the content
      ForcedRedirectManager.getInstance ().createForcedRedirect (ex);

      // And set the redirect
      if (eHttpVersion.is10 ())
      {
        // For HTTP 1.0 send 302
        aHttpResponse.setStatus (HttpServletResponse.SC_FOUND);
      }
      else
      {
        // For HTTP 1.1 send 303
        aHttpResponse.setStatus (HttpServletResponse.SC_SEE_OTHER);
      }

      // Set the location header
      String sTargetURL = ex.getRedirectTargetURL ().getAsStringWithEncodedParameters ();
      if (ServletSettings.isEncodeURLs ())
        sTargetURL = aHttpResponse.encodeRedirectURL (sTargetURL);
      aHttpResponse.addHeader (CHttpHeader.LOCATION, sTargetURL);
    }
    catch (final Throwable t)
    {
      m_aCounterRequestsWithException.increment ();

      // Invoke exception handler
      if (m_aExceptionHandler.forEachBreakable (x -> x.onException (m_sApplicationID, aRequestScope, t)).isContinue ())
      {
        // No handler handled it - propagate
        throw t;
      }

      // One exception handled did it - no need to propagate
    }
    finally
    {
      // High level filters after
      for (final IXServletHighLevelFilter aFilter : aEffectiveFilters)
        try
        {
          aFilter.afterRequest (aRequestScope);
        }
        catch (final Throwable t)
        {
          s_aLogger.error ("Exception in high-level filter afterRequest of " + aFilter + " - caught and ignored", t);
        }
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
    log (sMsg + ":\n" + RequestLogger.getRequestDebugString (aHttpRequest).toString ());
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
    ValueEnforcer.isInstanceOf (aResponse, HttpServletResponse.class, "Non-HTTP servlet response");

    final HttpServletRequest aHttpRequest = (HttpServletRequest) aRequest;
    final HttpServletResponse aHttpResponse = (HttpServletResponse) aResponse;

    // Increase counter
    m_aCounterRequestsTotal.increment ();

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
    m_aCounterRequestsPerVersionAccepted.increment (eHttpVersion.getName ());

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
    m_aCounterRequestsPerMethodAccepted.increment (eHttpMethod.getName ());

    // here HTTP version and method are valid

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
    final ICommonsList <IXServletLowLevelFilter> aEffectiveFilterList = new CommonsArrayList <> ();
    aEffectiveFilterList.add (XServletFilterSecurity.INSTANCE);
    aEffectiveFilterList.add (new XServletFilterConsistency ());
    aEffectiveFilterList.addAll (m_aFilterList);

    // Create request scope
    final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (m_sApplicationID,
                                                                                             aHttpRequest,
                                                                                             aHttpResponseWrapper);
    final IRequestWebScope aRequestScope = aRequestScopeInitializer.getRequestScope ();

    boolean bInvokeHandler = true;
    Throwable aCaughtException = null;
    try
    {
      // Filter before
      for (final IXServletLowLevelFilter aFilter : aEffectiveFilterList)
        if (aFilter.beforeRequest (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod, aRequestScope)
                   .isBreak ())
        {
          bInvokeHandler = false;
          return;
        }

      if (bInvokeHandler)
      {
        // Find and invoke handler
        _invokeHandler (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod, aRequestScope);
      }
    }
    catch (final Throwable t)
    {
      // Remember
      aCaughtException = t;

      // This log entry is mainly present to have an overview on how often this
      // really happens
      log ("Servlet exception propagated to the outside", t);

      // Ensure only exceptions with the correct type are propagated
      if (t instanceof IOException)
        throw (IOException) t;
      if (t instanceof ServletException)
        throw (ServletException) t;
      throw new ServletException ("Wrapped " + t.getClass ().getName (), t);
    }
    finally
    {
      try
      {
        // Filter after
        for (final IXServletLowLevelFilter aFilter : aEffectiveFilterList)
          try
          {
            aFilter.afterRequest (aHttpRequest,
                                  aHttpResponseWrapper,
                                  eHttpVersion,
                                  eHttpMethod,
                                  aRequestScope,
                                  bInvokeHandler,
                                  aCaughtException);
          }
          catch (final ServletException | IOException ex)
          {
            s_aLogger.error ("Exception in low-level filter afterRequest of " + aFilter + " - re-thrown", ex);
            // Re-throw
            throw ex;
          }
      }
      finally
      {
        // Destroy request scope (if it was created)
        aRequestScopeInitializer.destroyScope ();
      }
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("HandlerRegistry", m_aHandlerRegistry)
                                       .append ("FilterList", m_aFilterList)
                                       .append ("ExceptionHandler", m_aExceptionHandler)
                                       .append ("ApplicationID", m_sApplicationID)
                                       .getToString ();
  }
}
