package com.helger.web.servlets.scope;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;
import com.helger.servlet.http.IHttpServletHandler;
import com.helger.web.scope.request.RequestScopeInitializer;

final class ScopingHttpServletHandler implements IHttpServletHandler
{
  private final String m_sApplicationID;
  private final IHttpServletHandler m_aOriginalHandler;

  public ScopingHttpServletHandler (@Nonnull @Nonempty final String sApplicationID,
                                    @Nonnull final IHttpServletHandler aOriginalHandler)
  {
    m_sApplicationID = sApplicationID;
    m_aOriginalHandler = aOriginalHandler;
  }

  public void handle (@Nonnull final HttpServletRequest aHttpRequest,
                      @Nonnull final HttpServletResponse aHttpResponse,
                      @Nonnull final EHTTPVersion eHttpVersion,
                      @Nonnull final EHTTPMethod eHttpMethod) throws ServletException, IOException
  {
    final RequestScopeInitializer aRequestScopeInitializer = RequestScopeInitializer.create (m_sApplicationID,
                                                                                             aHttpRequest,
                                                                                             aHttpResponse);
    try
    {
      // Pass to original handler
      m_aOriginalHandler.handle (aHttpRequest, aHttpResponse, eHttpVersion, eHttpMethod);
    }
    finally
    {
      aRequestScopeInitializer.destroyScope ();
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ApplicationID", m_sApplicationID)
                                       .append ("OriginalHandler", m_aOriginalHandler)
                                       .getToString ();
  }
}
