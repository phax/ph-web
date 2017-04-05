package com.helger.servlet.http;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;
import com.helger.servlet.async.ExtAsyncContext;
import com.helger.servlet.async.ServletAsyncSpec;

final class AsyncHttpServletHandler implements IHttpServletHandler
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AsyncHttpServletHandler.class);

  private final ServletAsyncSpec m_aAsyncSpec;
  private final IHttpServletHandler m_aOriginalHandler;

  public AsyncHttpServletHandler (@Nonnull final ServletAsyncSpec aAsyncSpec,
                                  @Nonnull final IHttpServletHandler aOriginalHandler)
  {
    m_aAsyncSpec = aAsyncSpec;
    m_aOriginalHandler = aOriginalHandler;
  }

  public void handle (@Nonnull final HttpServletRequest aHttpRequest,
                      @Nonnull final HttpServletResponse aHttpResponse,
                      @Nonnull final EHTTPVersion eHttpVersion,
                      @Nonnull final EHTTPMethod eHttpMethod) throws ServletException, IOException
  {
    final ExtAsyncContext aExtAsyncCtx = ExtAsyncContext.create (aHttpRequest,
                                                                 aHttpResponse,
                                                                 eHttpVersion,
                                                                 eHttpMethod,
                                                                 m_aAsyncSpec);

    // Put into async processing queue
    AbstractAsyncHttpServlet.getAsyncServletRunner ().runAsync (aHttpRequest, aHttpResponse, aExtAsyncCtx, () -> {
      try
      {
        m_aOriginalHandler.handle (aExtAsyncCtx.getRequest (),
                                   aExtAsyncCtx.getResponse (),
                                   aExtAsyncCtx.getHTTPVersion (),
                                   aExtAsyncCtx.getHTTPMethod ());
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

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("AsyncSpec", m_aAsyncSpec)
                                       .append ("OriginalHandler", m_aOriginalHandler)
                                       .getToString ();
  }
}
