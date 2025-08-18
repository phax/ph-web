/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.OverridingMethodsMustInvokeSuper;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.annotation.style.OverrideOnDemand;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.callback.CallbackList;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.http.CHttp;
import com.helger.http.CHttpHeader;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.scope.mgr.ScopeManager;
import com.helger.servlet.ServletContextPathHolder;
import com.helger.servlet.ServletHelper;
import com.helger.servlet.ServletSettings;
import com.helger.servlet.StaticServerInfo;
import com.helger.servlet.http.CountingOnlyHttpServletResponse;
import com.helger.servlet.request.RequestLogger;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;
import com.helger.statistics.api.IMutableStatisticsHandlerCounter;
import com.helger.statistics.api.IMutableStatisticsHandlerKeyedCounter;
import com.helger.statistics.impl.StatisticsManager;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.impl.RequestWebScope;
import com.helger.web.scope.multipart.RequestWebScopeMultipart;
import com.helger.web.scope.request.RequestScopeInitializer;
import com.helger.xservlet.exception.IXServletExceptionHandler;
import com.helger.xservlet.exception.XServletLoggingExceptionHandler;
import com.helger.xservlet.filter.IXServletHighLevelFilter;
import com.helger.xservlet.filter.IXServletLowLevelFilter;
import com.helger.xservlet.filter.XServletFilterConsistency;
import com.helger.xservlet.filter.XServletFilterSecurityHttpReferrerPolicy;
import com.helger.xservlet.filter.XServletFilterSecurityPoxy;
import com.helger.xservlet.filter.XServletFilterSecurityXFrameOptions;
import com.helger.xservlet.filter.XServletFilterTimer;
import com.helger.xservlet.filter.XServletFilterTrackRequest;
import com.helger.xservlet.forcedredirect.ForcedRedirectException;
import com.helger.xservlet.forcedredirect.ForcedRedirectManager;
import com.helger.xservlet.handler.IXServletHandler;
import com.helger.xservlet.handler.XServletHandlerOPTIONS;
import com.helger.xservlet.handler.XServletHandlerRegistry;
import com.helger.xservlet.handler.XServletHandlerTRACE;
import com.helger.xservlet.servletstatus.ServletStatusManager;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Abstract HTTP based servlet. Compared to the default
 * {@link jakarta.servlet.http.HttpServlet} this class uses a handler map with
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
 * Note: it must be derived from {@link HttpServlet} to be usable with
 * annotation based configuration in Jetty (was GenericServlet previously)
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@NotThreadSafe
public abstract class AbstractXServlet extends HttpServlet
{
  /**
   * Internal request attribute defining whether a request was handled
   * asynchronously. If this attribute is not present, it means synchronous
   */
  public static final String REQUEST_ATTR_HANDLED_ASYNC = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                          "request-is-async";
  /**
   * Internal request attribute defining whether a request scope was created or
   * re-used
   */
  public static final String REQUEST_ATTR_SCOPE_CREATED = ScopeManager.SCOPE_ATTRIBUTE_PREFIX_INTERNAL +
                                                          "request-scope-created";

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractXServlet.class);

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
  private static final AtomicBoolean FIRST_REQUEST = new AtomicBoolean (true);

  /** The main handler map */
  private final XServletHandlerRegistry m_aHandlerRegistry = new XServletHandlerRegistry ();
  private final ICommonsList <IXServletLowLevelFilter> m_aFilterLowLevelList = new CommonsArrayList <> ();
  private final ICommonsList <IXServletHighLevelFilter> m_aFilterHighLevelList = new CommonsArrayList <> ();
  private final CallbackList <IXServletExceptionHandler> m_aExceptionHandler = new CallbackList <> ();
  private final XServletSettings m_aSettings = new XServletSettings ();

  // Status variables
  // Remember to avoid crash on shutdown, when no GlobalScope is present
  private final ServletStatusManager m_aStatusMgr;

  /**
   * Constructor.
   */
  public AbstractXServlet ()
  {
    // This handler is always the same, so it is registered here for convenience
    m_aHandlerRegistry.registerHandler (EHttpMethod.TRACE, new XServletHandlerTRACE (), false);

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
                                        },
                                        false);

    // Default OPTIONS handler
    m_aHandlerRegistry.registerHandler (EHttpMethod.OPTIONS,
                                        new XServletHandlerOPTIONS (m_aHandlerRegistry::getAllowedHttpMethodsString),
                                        false);

    m_aExceptionHandler.add (new XServletLoggingExceptionHandler ());

    // Remember once
    m_aStatusMgr = ServletStatusManager.getInstance ();
    m_aStatusMgr.onServletCtor (getClass ());
  }

  /**
   * @return The handler registry for HTTP method to handler registration. Never
   *         <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableObject
  protected final XServletHandlerRegistry handlerRegistry ()
  {
    return m_aHandlerRegistry;
  }

  /**
   * @return The internal filter list where custom filters can be added. Never
   *         <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableObject
  protected final ICommonsList <IXServletLowLevelFilter> filterLowLevelList ()
  {
    return m_aFilterLowLevelList;
  }

  /**
   * @return The internal filter list where custom filters can be added. Never
   *         <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableObject
  protected final ICommonsList <IXServletHighLevelFilter> filterHighLevelList ()
  {
    return m_aFilterHighLevelList;
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
   * @return Settings for this servlet. May not be <code>null</code>.
   */
  public final XServletSettings settings ()
  {
    return m_aSettings;
  }

  /**
   * @return The servlet status manager stored in the constructor. Never
   *         <code>null</code>.
   */
  @Nonnull
  protected final ServletStatusManager getServletStatusMgr ()
  {
    return m_aStatusMgr;
  }

  @Override
  public final void log (final String sMsg)
  {
    super.log (sMsg);
    LOGGER.info (sMsg);
  }

  @Override
  public final void log (final String sMsg, final Throwable t)
  {
    super.log (sMsg, t);
    LOGGER.error (sMsg, t);
  }

  /**
   * A final overload of "init". Overload "init" instead.
   */
  @Override
  public final void init (@Nonnull final ServletConfig aSC) throws ServletException
  {
    // this indirectly calls "init()"
    super.init (aSC);

    // So this is executed AFTER init()
    m_aStatusMgr.onServletInit (getClass ());
    try
    {
      // Build init parameter map
      final ICommonsMap <String, String> aInitParams = new CommonsHashMap <> ();
      final Enumeration <String> aEnum = aSC.getInitParameterNames ();
      while (aEnum.hasMoreElements ())
      {
        final String sName = aEnum.nextElement ();
        aInitParams.put (sName, aSC.getInitParameter (sName));
      }
      // Invoke each handler for potential initialization
      m_aHandlerRegistry.forEachHandlerThrowing (x -> x.onServletInit (aInitParams));
    }
    catch (final ServletException ex)
    {
      m_aStatusMgr.onServletInitFailed (ex, getClass ());
      throw ex;
    }
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void destroy ()
  {
    // Invoke each handler for potential destruction
    m_aHandlerRegistry.forEachHandler (IXServletHandler::onServletDestroy);

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
        aHttpResponse.sendError (CHttp.HTTP_BAD_REQUEST);
      else
        aHttpResponse.sendError (CHttp.HTTP_METHOD_NOT_ALLOWED);
      return;
    }
    // HTTP method is supported by this servlet implementation
    final ICommonsList <IXServletHighLevelFilter> aEffectiveFilters = new CommonsArrayList <> (2 +
                                                                                               m_aFilterHighLevelList.size ());
    // Add new instance all the time!
    aEffectiveFilters.add (new XServletFilterTimer (this));
    // Add new instance all the time!
    aEffectiveFilters.add (new XServletFilterTrackRequest ());
    aEffectiveFilters.addAll (m_aFilterHighLevelList);

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
        aHttpResponse.setStatus (CHttp.HTTP_MOVED_TEMPORARY);
      }
      else
      {
        // For HTTP 1.1 send 303
        aHttpResponse.setStatus (CHttp.HTTP_SEE_OTHER);
      }
      // Set the location header
      String sTargetURL = ex.getRedirectTargetURL ().getAsStringWithEncodedParameters ();
      if (ServletSettings.isEncodeURLs ())
        sTargetURL = aHttpResponse.encodeRedirectURL (sTargetURL);

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Sending redirect to '" + sTargetURL + "'");

      aHttpResponse.addHeader (CHttpHeader.LOCATION, sTargetURL);
    }
    catch (final Exception ex)
    {
      m_aCounterRequestsWithException.increment ();

      // Invoke exception handler
      if (m_aExceptionHandler.forEachBreakable (x -> x.onException (aRequestScope, ex)).isContinue ())
      {
        // No handler handled it - propagate
        throw ex;
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
        catch (final Exception ex)
        {
          LOGGER.error ("Exception in high-level filter afterRequest of " + aFilter + " - caught and ignored", ex);
        }
    }
  }

  /**
   * This method logs errors, in case a HttpServletRequest object is missing
   * basic information or uses unsupported values for e.g. HTTP version and HTTP
   * method.
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
   * @param aHttpRequest
   *        the {@link HttpServletRequest} object that contains the request the
   *        client made of the servlet
   * @param aHttpResponse
   *        the {@link HttpServletResponse} object that contains the response
   *        the servlet returns to the client
   * @exception IOException
   *            if an input or output error occurs while the servlet is handling
   *            the HTTP request
   * @exception ServletException
   *            if the HTTP request cannot be handled
   * @see jakarta.servlet.Servlet#service
   */
  @Override
  protected final void service (@Nonnull final HttpServletRequest aHttpRequest,
                                @Nonnull final HttpServletResponse aHttpResponse) throws ServletException, IOException
  {
    // Increase counter
    m_aCounterRequestsTotal.increment ();

    // Increase per servlet invocation
    m_aStatusMgr.onServletInvocation (getClass ());

    // Ensure a valid HTTP version is provided
    final String sProtocol = aHttpRequest.getProtocol ();
    final EHttpVersion eHttpVersion = EHttpVersion.getFromNameOrNull (sProtocol);
    if (eHttpVersion == null)
    {
      // HTTP version disallowed
      logInvalidRequestSetup ("Request has unsupported HTTP version (" + sProtocol + ")!", aHttpRequest);
      aHttpResponse.sendError (CHttp.HTTP_VERSION_NOT_SUPPORTED);
      return;
    }
    m_aCounterRequestsPerVersionAccepted.increment (eHttpVersion.getName ());

    // Ensure a valid HTTP method is provided
    final String sMethod = ServletHelper.getRequestMethod (aHttpRequest);
    final EHttpMethod eHttpMethod = EHttpMethod.getFromNameOrNull (sMethod);
    if (eHttpMethod == null)
    {
      // HTTP method unknown
      logInvalidRequestSetup ("Request has unsupported HTTP method (" + sMethod + ")!", aHttpRequest);
      aHttpResponse.sendError (CHttp.HTTP_NOT_IMPLEMENTED);
      return;
    }
    m_aCounterRequestsPerMethodAccepted.increment (eHttpMethod.getName ());

    // here HTTP version and method are valid

    // May already be set in test cases!
    if (FIRST_REQUEST.getAndSet (false) && !StaticServerInfo.isSet ())
    {
      // First set the default web server info
      StaticServerInfo.init (aHttpRequest.getScheme (),
                             aHttpRequest.getServerName (),
                             aHttpRequest.getServerPort (),
                             ServletContextPathHolder.getContextPath ());
    }
    // Create a wrapper around the Servlet Response that saves the status code
    final StatusAwareHttpResponseWrapper aHttpResponseWrapper = StatusAwareHttpResponseWrapper.wrap (aHttpResponse);

    // Create effective filter list with all internal filters as well
    final ICommonsList <IXServletLowLevelFilter> aEffectiveFilterList = new CommonsArrayList <> (4 +
                                                                                                 m_aFilterLowLevelList.size ());
    // Add internal filters - always first
    aEffectiveFilterList.add (XServletFilterSecurityPoxy.INSTANCE);
    aEffectiveFilterList.add (XServletFilterConsistency.INSTANCE);
    if (m_aSettings.hasHttpReferrerPolicy ())
      aEffectiveFilterList.add (new XServletFilterSecurityHttpReferrerPolicy (m_aSettings.getHttpReferrerPolicy ()));
    if (m_aSettings.hasXFrameOptions ())
      aEffectiveFilterList.add (new XServletFilterSecurityXFrameOptions (m_aSettings.getXFrameOptionsType (),
                                                                         m_aSettings.getXFrameOptionsDomain ()));
    // Add custom filters
    aEffectiveFilterList.addAll (m_aFilterLowLevelList);

    // Filter before request scope is created!
    boolean bInvokeHandler = true;
    for (final IXServletLowLevelFilter aFilter : aEffectiveFilterList)
      if (aFilter.beforeRequest (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod).isBreak ())
      {
        bInvokeHandler = false;
        return;
      }

    boolean bIsHandledAsync = false;
    Exception aCaughtException = null;
    try
    {
      if (bInvokeHandler)
      {
        // Create request scope
        final BiFunction <? super HttpServletRequest, ? super HttpServletResponse, IRequestWebScope> aFactory;
        aFactory = m_aSettings.isMultipartEnabled () ? RequestWebScopeMultipart::new : RequestWebScope::new;

        try (final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (aHttpRequest,
                                                                                                      aHttpResponseWrapper,
                                                                                                      aFactory))
        {
          final IRequestWebScope aRequestScope = aRequestScopeInitializer.getRequestScope ();
          aRequestScope.attrs ().putIn (REQUEST_ATTR_SCOPE_CREATED, aRequestScopeInitializer.isNew ());

          // Find and invoke handler
          _invokeHandler (aHttpRequest, aHttpResponseWrapper, eHttpVersion, eHttpMethod, aRequestScope);

          bIsHandledAsync = aRequestScope.attrs ().getAsBoolean (AbstractXServlet.REQUEST_ATTR_HANDLED_ASYNC, false);
          if (bIsHandledAsync)
          {
            // The request scope is needed in the async handler!
            aRequestScopeInitializer.internalSetDontDestroyRequestScope ();
          }
        }
      }
    }
    catch (final Exception ex)
    {
      // Remember
      aCaughtException = ex;

      // This log entry is mainly present to have an overview on how often
      // this really happens
      log ("Servlet exception propagated to the outside", ex);

      // Ensure only exceptions with the correct type are propagated
      if (ex instanceof IOException)
        throw (IOException) ex;
      if (ex instanceof ServletException)
        throw (ServletException) ex;
      throw new ServletException ("Wrapped " + ex.getClass ().getName (), ex);
    }
    finally
    {
      // Filter after
      for (final IXServletLowLevelFilter aFilter : aEffectiveFilterList)
        try
        {
          aFilter.afterRequest (aHttpRequest,
                                aHttpResponseWrapper,
                                eHttpVersion,
                                eHttpMethod,
                                bInvokeHandler,
                                aCaughtException,
                                bIsHandledAsync);
        }
        catch (final ServletException | IOException ex)
        {
          LOGGER.error ("Exception in low-level filter afterRequest of " + aFilter + " - re-thrown", ex);
          // Don't re-throw in finally
          // throw ex;
        }
    }
  }

  // Avoid overloading in sub classes
  @Override
  public final void service (@Nonnull final ServletRequest req, @Nonnull final ServletResponse res)
                                                                                                    throws ServletException,
                                                                                                    IOException
  {
    super.service (req, res);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("HandlerRegistry", m_aHandlerRegistry)
                                       .append ("FilterList", m_aFilterLowLevelList)
                                       .append ("ExceptionHandler", m_aExceptionHandler)
                                       .append ("Settings", m_aSettings)
                                       .getToString ();
  }
}
