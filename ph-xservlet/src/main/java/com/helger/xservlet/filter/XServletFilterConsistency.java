package com.helger.xservlet.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.state.EContinue;
import com.helger.commons.string.StringHelper;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.request.RequestHelper;
import com.helger.servlet.response.ResponseHelper;
import com.helger.servlet.response.StatusAwareHttpResponseWrapper;
import com.helger.web.scope.IRequestWebScope;

/**
 * Handle special content related stuff that needs to be processed for every
 * servlet. Currently handled are:
 * <ul>
 * <li>Request fallback charset</li>
 * <li>Response fallback charset</li>
 * </ul>
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletFilterConsistency implements IXServletLowLevelFilter
{
  public static final XServletFilterConsistency INSTANCE = new XServletFilterConsistency ();
  private static final Logger s_aLogger = LoggerFactory.getLogger (XServletFilterConsistency.class);

  /** The request fallback charset to be used, if none is present! */
  private Charset m_aRequestFallbackCharset = StandardCharsets.UTF_8;

  /** The response fallback charset to be used, if none is present! */
  private Charset m_aResponseFallbackCharset = StandardCharsets.UTF_8;

  public XServletFilterConsistency ()
  {}

  /**
   * @return The fallback charset to be used if an HTTP request has no charset
   *         defined. Never <code>null</code>.
   */
  @Nonnull
  protected final Charset getRequestFallbackCharset ()
  {
    return m_aRequestFallbackCharset;
  }

  /**
   * Set the fallback charset for HTTP request if they don't have a charset
   * defined. By default UTF-8 is used.
   *
   * @param aFallbackCharset
   *        The fallback charset to be used. May not be <code>null</code>.
   */
  protected final void setRequestFallbackCharset (@Nonnull final Charset aFallbackCharset)
  {
    ValueEnforcer.notNull (aFallbackCharset, "FallbackCharset");
    m_aRequestFallbackCharset = aFallbackCharset;
  }

  /**
   * @return The fallback charset to be used if an HTTP response has no charset
   *         defined. Never <code>null</code>.
   */
  @Nonnull
  protected final Charset getResponseFallbackCharset ()
  {
    return m_aResponseFallbackCharset;
  }

  /**
   * Set the fallback charset for HTTP response if they don't have a charset
   * defined. By default UTF-8 is used.
   *
   * @param aFallbackCharset
   *        The fallback charset to be used. May not be <code>null</code>.
   */
  protected final void setResponseFallbackCharset (@Nonnull final Charset aFallbackCharset)
  {
    ValueEnforcer.notNull (aFallbackCharset, "FallbackCharset");
    m_aResponseFallbackCharset = aFallbackCharset;
  }

  /**
   * This method is required to ensure that the HTTP request is correctly
   * encoded. Normally this is done via the charset filter, but if a
   * non-existing URL is accessed then the error redirect happens without the
   * charset filter ever called.
   *
   * @param aHttpRequest
   *        The current HTTP request. Never <code>null</code>.
   * @throws IOException
   *         In invalid charset
   */
  @OverrideOnDemand
  protected void ensureRequestCharset (@Nonnull final HttpServletRequest aHttpRequest) throws IOException
  {
    if (aHttpRequest.getCharacterEncoding () == null)
    {
      final String sCharsetName = m_aRequestFallbackCharset.name ();
      s_aLogger.warn ("Forcing request charset to " + sCharsetName);
      aHttpRequest.setCharacterEncoding (sCharsetName);
    }
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
      final String sCharsetName = m_aResponseFallbackCharset.name ();
      s_aLogger.warn ("Forcing response charset to " + sCharsetName);
      aHttpResponse.setCharacterEncoding (sCharsetName);
    }
  }

  @Nonnull
  public EContinue beforeRequest (@Nonnull final HttpServletRequest aHttpRequest,
                                  @Nonnull final HttpServletResponse aHttpResponse,
                                  @Nonnull final EHttpVersion eHttpVersion,
                                  @Nonnull final EHttpMethod eHttpMethod,
                                  @Nonnull final IRequestWebScope aRequestScope) throws ServletException, IOException
  {
    ensureRequestCharset (aHttpRequest);
    ensureResponseCharset (aHttpResponse);
    return EContinue.CONTINUE;
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkStatusCode (@Nonnull final String sRequestURL, final int nStatusCode)
  {
    // < 300 || >= 400?
    if (nStatusCode < HttpServletResponse.SC_MULTIPLE_CHOICES || nStatusCode >= HttpServletResponse.SC_BAD_REQUEST)
      s_aLogger.warn ("Status code " + nStatusCode + " in response to '" + sRequestURL + "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param sCharacterEncoding
   *        The response character encoding.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkCharacterEncoding (@Nonnull final String sRequestURL,
                                         @Nullable final String sCharacterEncoding,
                                         final int nStatusCode)
  {
    if (StringHelper.hasNoText (sCharacterEncoding) && !ResponseHelper.isEmptyStatusCode (nStatusCode))
      s_aLogger.warn ("No character encoding on " + nStatusCode + " response to '" + sRequestURL + "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param sContentType
   *        The response content type.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkContentType (@Nonnull final String sRequestURL,
                                   @Nullable final String sContentType,
                                   final int nStatusCode)
  {
    if (StringHelper.hasNoText (sContentType) && !ResponseHelper.isEmptyStatusCode (nStatusCode))
      s_aLogger.warn ("No content type on " + nStatusCode + " response to '" + sRequestURL + "'");
  }

  /**
   * @param sRequestURL
   *        The request URL.
   * @param aHeaders
   *        All response HTTP headers.
   * @param nStatusCode
   *        The response status code.
   */
  @OverrideOnDemand
  protected void checkHeaders (@Nonnull final String sRequestURL,
                               @Nonnull final ICommonsMap <String, ICommonsList <String>> aHeaders,
                               final int nStatusCode)
  {
    if (nStatusCode != HttpServletResponse.SC_OK && aHeaders.isNotEmpty ())
      s_aLogger.warn ("Headers on " + nStatusCode + " response to '" + sRequestURL + "': " + aHeaders);
  }

  public void afterRequest (@Nonnull final HttpServletRequest aHttpRequest,
                            @Nonnull final HttpServletResponse aHttpResponse,
                            @Nonnull final EHttpVersion eHttpVersion,
                            @Nonnull final EHttpMethod eHttpMethod,
                            @Nonnull final IRequestWebScope aRequestScope,
                            final boolean bInvokeHandler,
                            @Nullable final Throwable aCaughtException)
  {
    ValueEnforcer.isTrue (aHttpResponse instanceof StatusAwareHttpResponseWrapper,
                          "Must be a StatusAwareHttpResponseWrapper");
    final String sRequestURL = RequestHelper.getURL (aHttpRequest);
    final int nStatusCode = ((StatusAwareHttpResponseWrapper) aHttpResponse).getStatusCode ();
    final ICommonsMap <String, ICommonsList <String>> aHeaders = ((StatusAwareHttpResponseWrapper) aHttpResponse).headerMap ()
                                                                                                                 .getAllHeaders ();
    final String sCharacterEncoding = aHttpResponse.getCharacterEncoding ();
    final String sContentType = aHttpResponse.getContentType ();

    checkStatusCode (sRequestURL, nStatusCode);
    checkCharacterEncoding (sRequestURL, sCharacterEncoding, nStatusCode);
    checkContentType (sRequestURL, sContentType, nStatusCode);
    checkHeaders (sRequestURL, aHeaders, nStatusCode);
  }
}
