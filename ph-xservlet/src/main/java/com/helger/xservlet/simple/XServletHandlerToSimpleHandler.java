package com.helger.xservlet.simple;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScope;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.handler.IXServletHandler;

final class XServletHandlerToSimpleHandler implements IXServletHandler
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (XServletHandlerToSimpleHandler.class);

  private final IXServletSimpleHandler m_aSimpleHandler;
  private final String m_sApplicationID;

  public XServletHandlerToSimpleHandler (@Nonnull final IXServletSimpleHandler aSimpleHandler,
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

      Throwable aCaughtException = null;
      try
      {
        // main servlet handling
        m_aSimpleHandler.handleRequest (aRequestScope, aUnifiedResponse);

        if (s_aLogger.isDebugEnabled ())
          s_aLogger.debug ("Successfully handled request: " + aRequestScope.getPathWithinServlet ());
      }
      catch (final Throwable t)
      {
        // Invoke exception handler
        // This internally re-throws the exception if needed
        aCaughtException = t;
        _onException (aRequestScope, aUnifiedResponse, t);
      }
      finally
      {
        // On request end
        try
        {
          m_aSimpleHandler.onRequestEnd (aCaughtException);
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
