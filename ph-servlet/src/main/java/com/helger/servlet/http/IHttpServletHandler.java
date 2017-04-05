package com.helger.servlet.http;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.http.EHTTPMethod;
import com.helger.http.EHTTPVersion;

/**
 * HTTP servlet handler for a single HTTP methods.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
@FunctionalInterface
public interface IHttpServletHandler extends Serializable
{
  /**
   * Handle
   *
   * @param aHttpRequest
   *        HTTP servlet request. Never <code>null</code>.
   * @param aHttpResponse
   *        HTTP servlet response. Never <code>null</code>.
   * @param eHTTPVersion
   *        HTTP version. Never <code>null</code>.
   * @param eHTTPMethod
   *        HTTP method. Never <code>null</code>.
   * @throws ServletException
   *         On business error
   * @throws IOException
   *         On IO error
   */
  void handle (@Nonnull HttpServletRequest aHttpRequest,
               @Nonnull HttpServletResponse aHttpResponse,
               @Nonnull EHTTPVersion eHTTPVersion,
               @Nonnull EHTTPMethod eHTTPMethod) throws ServletException, IOException;
}
