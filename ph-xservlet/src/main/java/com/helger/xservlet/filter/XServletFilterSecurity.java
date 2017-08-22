package com.helger.xservlet.filter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.http.CHttpHeader;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.state.EContinue;
import com.helger.http.EHttpVersion;
import com.helger.servlet.request.RequestLogger;
import com.helger.web.scope.IRequestWebScope;

/**
 * Handle special security related stuff that needs to be processed for every
 * servlet. Currently handled are:
 * <ul>
 * <li>Httpoxy attack using the 'Proxy' HTTP header</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletFilterSecurity implements IXServletLowLevelFilter
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (XServletFilterSecurity.class);
  public static final XServletFilterSecurity INSTANCE = new XServletFilterSecurity ();

  public XServletFilterSecurity ()
  {}

  @Nonnull
  protected EContinue checkForHttpPoxy (@Nonnull final HttpServletRequest aHttpRequest,
                                        @Nonnull final HttpServletResponse aHttpResponse) throws IOException
  {
    final String sPoxy = aHttpRequest.getHeader (CHttpHeader.PROXY);
    if (sPoxy != null)
    {
      // potentially malicious request - log and block
      s_aLogger.warn ("httpoxy request successfully blocked: " + aHttpRequest);
      RequestLogger.logRequestComplete (aHttpRequest);
      aHttpResponse.sendError (HttpServletResponse.SC_BAD_REQUEST);
      return EContinue.BREAK;
    }
    return EContinue.CONTINUE;
  }

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod,
                                  @Nonnull final IRequestWebScope aRequestScope) throws IOException
  {
    if (checkForHttpPoxy (aHttpRequest, aHttpResponse).isBreak ())
      return EContinue.BREAK;

    // Further checks go here
    return EContinue.CONTINUE;
  }

  public void afterRequest (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final EHttpVersion eHttpVersion,
                            @Nonnull final EHttpMethod eHttpMethod,
                            @Nonnull final IRequestWebScope aRequestScope,
                            final boolean bInvokeHandler,
                            @Nullable final Throwable aCaughtException)
  {}
}
