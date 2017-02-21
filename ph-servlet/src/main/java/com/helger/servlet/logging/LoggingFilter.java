package com.helger.servlet.logging;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.collection.ext.CommonsHashSet;
import com.helger.commons.collection.ext.ICommonsSet;
import com.helger.commons.string.StringHelper;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.ResponseHelper;

public class LoggingFilter implements Filter
{
  private Logger m_aLogger = LoggerFactory.getLogger (getClass ());
  private int m_nMaxContentSize = 1024;
  private final ICommonsSet <String> m_aExcludedPaths = new CommonsHashSet<> ();
  private String m_sRequestPrefix = "REQUEST: ";
  private String m_sResponsePrefix = "RESPONSE: ";

  public LoggingFilter ()
  {}

  @Override
  public void init (final FilterConfig aFilterConfig) throws ServletException
  {
    final String sLoggerName = aFilterConfig.getInitParameter ("loggerName");
    if (StringHelper.hasText (sLoggerName))
      m_aLogger = LoggerFactory.getLogger (sLoggerName);

    final String sMaxContentSize = aFilterConfig.getInitParameter ("maxContentSize");
    if (sMaxContentSize != null)
      m_nMaxContentSize = Integer.parseInt (sMaxContentSize);

    final String sExcludedPaths = aFilterConfig.getInitParameter ("excludedPaths");
    if (StringHelper.hasText (sExcludedPaths))
      m_aExcludedPaths.setAll (sExcludedPaths.split ("\\s*,\\s*"));

    final String sRequestPrefix = aFilterConfig.getInitParameter ("requestPrefix");
    if (StringHelper.hasText (sRequestPrefix))
      m_sRequestPrefix = sRequestPrefix;

    final String sResponsePrefix = aFilterConfig.getInitParameter ("responsePrefix");
    if (StringHelper.hasText (sResponsePrefix))
      m_sResponsePrefix = sResponsePrefix;
  }

  @Nonnull
  @OverrideOnDemand
  protected String getRequestDescription (@Nonnull final LoggingHttpServletRequestWrapper requestWrapper)
  {
    final LoggingRequest aLoggingRequest = new LoggingRequest ();
    aLoggingRequest.setSender (requestWrapper.getLocalAddr ());
    aLoggingRequest.setMethod (requestWrapper.getMethod ());
    aLoggingRequest.setPath (requestWrapper.getRequestURI ());
    aLoggingRequest.setParams (requestWrapper.isFormPost () ? null : requestWrapper.getParameters ());
    aLoggingRequest.setHeaders (RequestHelper.getRequestHeaderMap (requestWrapper));
    final String content = requestWrapper.getContent ();
    if (m_aLogger.isTraceEnabled () || m_nMaxContentSize <= 0)
    {
      aLoggingRequest.setBody (content);
    }
    else
    {
      aLoggingRequest.setBody (content.substring (0, Math.min (content.length (), m_nMaxContentSize)));
    }

    return aLoggingRequest.getAsJson ().getAsJsonString ();
  }

  @Nonnull
  @OverrideOnDemand
  protected String getResponseDescription (@Nonnull final LoggingHttpServletResponseWrapper responseWrapper)
  {
    final LoggingResponse aLoggingResponse = new LoggingResponse ();
    aLoggingResponse.setStatus (responseWrapper.getStatus ());
    aLoggingResponse.setHeaders (ResponseHelper.getResponseHeaderMap (responseWrapper));
    final String content = responseWrapper.getContentAsString ();
    if (m_aLogger.isTraceEnabled () || m_nMaxContentSize <= 0)
    {
      aLoggingResponse.setBody (content);
    }
    else
    {
      aLoggingResponse.setBody (content.substring (0, Math.min (content.length (), m_nMaxContentSize)));
    }

    return aLoggingResponse.getAsJson ().getAsJsonString ();
  }

  /**
   * Check if this request should be logged or not.
   *
   * @param aHttpRequest
   *        Current HTTP servlet request. Never <code>null</code>.
   * @param aHttpResponse
   *        Current HTTP servlet response. Never <code>null</code>.
   * @return <code>true</code> to log, <code>false</code> to not log the request
   */
  @OverrideOnDemand
  protected boolean isLogRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse)
  {
    boolean bLog = m_aLogger.isDebugEnabled ();
    if (bLog)
    {
      // Check for excluded path
      final String sRequestURI = aHttpRequest.getRequestURI ();
      for (final String excludedPath : m_aExcludedPaths)
      {
        if (sRequestURI.startsWith (excludedPath))
        {
          bLog = false;
          break;
        }
      }
    }
    return bLog;
  }

  @Override
  public void doFilter (final ServletRequest aRequest,
                        final ServletResponse aResponse,
                        final FilterChain aFilterChain) throws IOException, ServletException
  {
    if (!(aRequest instanceof HttpServletRequest) || !(aResponse instanceof HttpServletResponse))
      throw new ServletException ("LoggingFilter just supports HTTP requests");

    final HttpServletRequest aHttpRequest = (HttpServletRequest) aRequest;
    final HttpServletResponse aHttpResponse = (HttpServletResponse) aResponse;
    if (isLogRequest (aHttpRequest, aHttpResponse))
    {
      final LoggingHttpServletRequestWrapper aRequestWrapper = new LoggingHttpServletRequestWrapper (aHttpRequest);
      final LoggingHttpServletResponseWrapper aResponseWrapper = new LoggingHttpServletResponseWrapper (aHttpResponse);

      m_aLogger.debug (m_sRequestPrefix + getRequestDescription (aRequestWrapper));
      aFilterChain.doFilter (aRequestWrapper, aResponseWrapper);
      m_aLogger.debug (m_sResponsePrefix + getResponseDescription (aResponseWrapper));

      aResponseWrapper.writeContentTo (aHttpResponse.getOutputStream ());
    }
    else
    {
      aFilterChain.doFilter (aHttpRequest, aHttpResponse);
    }
  }

  @Override
  public void destroy ()
  {}

}
