package com.helger.servlet.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import javax.annotation.Nonnull;
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
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ext.CommonsEnumMap;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerKeyedTimer;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.timing.StopWatch;
import com.helger.http.CHTTPHeader;
import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;
import com.helger.servlet.request.RequestLogger;

/**
 * Abstract HTTP based servlet. Compared to the default
 * {@link javax.servlet.http.HttpServlet} this class uses a handler map with
 * {@link EHTTPMethod} as the key.<br>
 * The following features are added compared to the default servlet
 * implementation:
 * <ul>
 * <li>It has counting statistics</li>
 * <li>It has timing statistics</li>
 * <li>It enforces a character set on the response</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public abstract class AbstractHttpServlet extends GenericServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractHttpServlet.class);
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsTotal = StatisticsManager.getCounterHandler (AbstractHttpServlet.class.getName () +
                                                                                                                       "$requests-total");
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsAccepted = StatisticsManager.getCounterHandler (AbstractHttpServlet.class.getName () +
                                                                                                                          "$requests-accepted");
  private static final IMutableStatisticsHandlerCounter s_aCounterRequestsHandled = StatisticsManager.getCounterHandler (AbstractHttpServlet.class.getName () +
                                                                                                                         "$requests-handled");
  private static final IMutableStatisticsHandlerKeyedCounter s_aCounterRequestsPerMethodTotal = StatisticsManager.getKeyedCounterHandler (AbstractHttpServlet.class.getName () +
                                                                                                                                          "$requests-per-method-total");
  private static final IMutableStatisticsHandlerKeyedCounter s_aCounterRequestsPerMethodHandled = StatisticsManager.getKeyedCounterHandler (AbstractHttpServlet.class.getName () +
                                                                                                                                            "$requests-per-method-handled");
  private static final IMutableStatisticsHandlerKeyedTimer s_aTimer = StatisticsManager.getKeyedTimerHandler (AbstractHttpServlet.class);

  /** The main handler map */
  private final ICommonsMap <EHTTPMethod, IHttpServletHandler> m_aHandler = new CommonsEnumMap <> (EHTTPMethod.class);
  /** The fallback charset to be used, if none is present! */
  private Charset m_aFallbackCharset = StandardCharsets.UTF_8;

  /**
   * Does nothing, because this is an abstract class.
   */
  public AbstractHttpServlet ()
  {
    // This handler is always the same, so it is registered here for convenience
    setHandler (EHTTPMethod.TRACE, new HttpServletHandlerTRACE ());
    // Default HEAD handler -> invoke with GET
    setHandler (EHTTPMethod.HEAD, (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod) -> {
      final CountingOnlyHttpServletResponse aResponseWrapper = new CountingOnlyHttpServletResponse (aHttpResponse);
      _internalService (aHttpRequest, aResponseWrapper, eHttpVersion, EHTTPMethod.GET);
      aResponseWrapper.setContentLengthAutomatically ();
    });
    // Default OPTIONS handler
    setHandler (EHTTPMethod.OPTIONS, (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod) -> {
      // Build Allow response header - that's it
      aHttpResponse.setHeader (CHTTPHeader.ALLOW, _getAllowString ());
    });
  }

  /**
   * A final overload of "init". Overload "init" instead.
   */
  @Override
  public final void init (@Nonnull final ServletConfig aSC) throws ServletException
  {
    super.init (aSC);
  }

  /**
   * Register a handler for the provided HTTP method. If another handler is
   * already registered, the new registration overwrites the old one.
   *
   * @param eHTTPMethod
   *        The HTTP method to register for. May not be <code>null</code>.
   * @param aHandler
   *        The handler to register. May not be <code>null</code>.
   */
  protected final void setHandler (@Nonnull final EHTTPMethod eHTTPMethod, @Nonnull final IHttpServletHandler aHandler)
  {
    ValueEnforcer.notNull (eHTTPMethod, "HTTPMethod");
    ValueEnforcer.notNull (aHandler, "Handler");
    m_aHandler.put (eHTTPMethod, aHandler);
  }

  /**
   * @return The fallback charset to be used if an HTTP response has no charset
   *         defined. Never <code>null</code>.
   */
  @Nonnull
  protected final Charset getFallbackCharset ()
  {
    return m_aFallbackCharset;
  }

  /**
   * Set the fallback charset for HTTP response if they don't have a charset
   * defined. By default UTF-8 is used.
   *
   * @param aFallbackCharset
   *        The fallback charset to be used. May not be <code>null</code>.
   */
  protected final void setFallbackCharset (@Nonnull final Charset aFallbackCharset)
  {
    ValueEnforcer.notNull (aFallbackCharset, "FallbackCharset");
    m_aFallbackCharset = aFallbackCharset;
  }

  @Nonnull
  @ReturnsMutableCopy
  private EnumSet <EHTTPMethod> _getAllowedHTTPMethods ()
  {
    // Return all methods for which handlers are registered
    final EnumSet <EHTTPMethod> ret = EnumSet.copyOf (m_aHandler.keySet ());
    if (!ret.contains (EHTTPMethod.GET))
    {
      // If GET is not supported, HEAD is also not supported
      ret.remove (EHTTPMethod.HEAD);
    }
    return ret;
  }

  @Nonnull
  private String _getAllowString ()
  {
    return StringHelper.getImplodedMapped (", ", _getAllowedHTTPMethods (), EHTTPMethod::getName);
  }

  /**
   * Invoked the provided handler to execute this service. If you overwrite this
   * method ensure to invoke
   * {@link IHttpServletHandler#handle(HttpServletRequest, HttpServletResponse, EHTTPVersion, EHTTPMethod)}.
   *
   * @param aHandler
   *        Handler. Never <code>null</code>.
   * @param aHttpRequest
   *        Current HTTP request. Never <code>null</code>.
   * @param aHttpResponse
   *        Current HTTP response. Never <code>null</code>.
   * @param eHttpVersion
   *        Current HTTP request version. Never <code>null</code>.
   * @param eHttpMethod
   *        Current HTTP request method. Never <code>null</code>.
   * @throws ServletException
   *         On business error
   * @throws IOException
   *         On IO error
   */
  @OverrideOnDemand
  protected void onServiceRequest (@Nonnull final IHttpServletHandler aHandler,
                                   @Nonnull final HttpServletRequest aHttpRequest,
                                   @Nonnull final HttpServletResponse aHttpResponse,
                                   @Nonnull final EHTTPVersion eHttpVersion,
                                   @Nonnull final EHTTPMethod eHttpMethod) throws ServletException, IOException
  {
    aHandler.handle (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod);
  }

  private void _internalService (@Nonnull final HttpServletRequest aHttpRequest,
                                 @Nonnull final HttpServletResponse aHttpResponse,
                                 @Nonnull final EHTTPVersion eHttpVersion,
                                 @Nonnull final EHTTPMethod eHttpMethod) throws ServletException, IOException
  {
    final IHttpServletHandler aHandler = m_aHandler.get (eHttpMethod);
    if (aHandler != null)
    {
      // Invoke handler
      final StopWatch aSW = StopWatch.createdStarted ();
      try
      {
        // This may indirectly call "_internalService" again (e.g. for HEAD
        // requests)
        onServiceRequest (aHandler, aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod);
        // Handled and no exception
        s_aCounterRequestsHandled.increment ();
        s_aCounterRequestsPerMethodHandled.increment (eHttpMethod.getName ());
      }
      finally
      {
        // Timer per HTTP method
        s_aTimer.addTime (eHttpMethod.getName (), aSW.stopAndGetMillis ());
      }
    }
    else
    {
      // Unsupported method
      aHttpResponse.setHeader (CHTTPHeader.ALLOW, _getAllowString ());
      if (eHttpVersion == EHTTPVersion.HTTP_11)
        aHttpResponse.sendError (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      else
        aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
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
   * This method is required to ensure that the HTTP response is correctly
   * encoded. Normally this is done via the charset filter, but if a
   * non-existing URL is accessed then the error redirect happens without the
   * charset filter ever called.
   *
   * @param aHttpResponse
   *        The current HTTP response. Never <code>null</code>.
   */
  @OverrideOnDemand
  protected void ensureResponseCharset (@Nonnull final HttpServletResponse aHttpResponse)
  {
    if (aHttpResponse.getCharacterEncoding () == null)
    {
      final String sCharsetName = m_aFallbackCharset.name ();
      s_aLogger.warn ("Forcing response charset to " + sCharsetName);
      aHttpResponse.setCharacterEncoding (sCharsetName);
    }
  }

  /**
   * Dispatches client requests to the protected <code>service</code> method.
   * There's no need to override this method.
   *
   * @param req
   *        the {@link HttpServletRequest} object that contains the request the
   *        client made of the servlet
   * @param res
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
  public final void service (@Nonnull final ServletRequest req,
                             @Nonnull final ServletResponse res) throws ServletException, IOException
  {
    ValueEnforcer.isInstanceOf (req, HttpServletRequest.class, "Non-HTTP servlet request");
    ValueEnforcer.isInstanceOf (req, HttpServletResponse.class, "Non-HTTP servlet response");

    final HttpServletRequest aHttpRequest = (HttpServletRequest) req;
    final HttpServletResponse aHttpResponse = (HttpServletResponse) res;

    s_aCounterRequestsTotal.increment ();

    // Determine HTTP version
    final String sProtocol = aHttpRequest.getProtocol ();
    final EHTTPVersion eHTTPVersion = EHTTPVersion.getFromNameOrNull (sProtocol);
    if (eHTTPVersion == null)
    {
      // HTTP version disallowed
      logInvalidRequestSetup ("Request has unsupported HTTP version (" + sProtocol + ")!", aHttpRequest);
      aHttpResponse.sendError (HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
    }
    else
    {
      // Determine HTTP method
      final String sMethod = aHttpRequest.getMethod ();
      final EHTTPMethod eHTTPMethod = EHTTPMethod.getFromNameOrNull (sMethod);
      if (eHTTPMethod == null)
      {
        // HTTP method unknown
        logInvalidRequestSetup ("Request has unsupported HTTP method (" + sMethod + ")!", aHttpRequest);
        aHttpResponse.sendError (HttpServletResponse.SC_NOT_IMPLEMENTED);
      }
      else
      {
        s_aCounterRequestsAccepted.increment ();
        s_aCounterRequestsPerMethodTotal.increment (eHTTPMethod.getName ());

        ensureResponseCharset (aHttpResponse);

        // Determine handler
        _internalService (aHttpRequest, aHttpResponse, eHTTPVersion, eHTTPMethod);
      }
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Handler", m_aHandler)
                                       .append ("FallbackCharset", m_aFallbackCharset.name ())
                                       .getToString ();
  }
}
