package com.helger.servlet.http;

import java.io.IOException;
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;

/**
 * Called by the server (via the <code>service</code> method) to allow a servlet
 * to handle a TRACE request. A TRACE returns the headers sent with the TRACE
 * request to the client, so that they can be used in debugging. There's no need
 * to override this method.
 *
 * @author Servlet Spec 3.1
 * @since 8.7.5
 */
public class HttpServletHandlerTRACE implements IHttpServletHandler
{
  private static final String CRLF = "\r\n";

  public void handle (@Nonnull final HttpServletRequest aHttpRequest,
                      @Nonnull final HttpServletResponse aHttpResponse,
                      @Nonnull final EHTTPVersion eHTTPVersion,
                      @Nonnull final EHTTPMethod eHTTPMethod) throws ServletException, IOException
  {
    final StringBuilder aSB = new StringBuilder ().append (EHTTPMethod.TRACE.getName ())
                                                  .append (' ')
                                                  .append (aHttpRequest.getRequestURI ())
                                                  .append (' ')
                                                  .append (aHttpRequest.getProtocol ())
                                                  .append (CRLF);
    final Enumeration <String> aReqHeaderEnum = aHttpRequest.getHeaderNames ();
    while (aReqHeaderEnum.hasMoreElements ())
    {
      final String sHeaderName = aReqHeaderEnum.nextElement ();
      aSB.append (sHeaderName).append (": ").append (aHttpRequest.getHeader (sHeaderName)).append (CRLF);
    }

    aHttpResponse.setContentType ("message/http");
    aHttpResponse.setContentLength (aSB.length ());
    aHttpResponse.getOutputStream ().print (aSB.toString ());
  }
}
