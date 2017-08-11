package com.helger.xservlet.simple;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.state.EContinue;
import com.helger.http.EHttpMethod;
import com.helger.http.EHttpVersion;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

/**
 * Special interface for a simple servlet handler.
 *
 * @author Philip Helger
 */
public interface IXServletSimpleHandler extends Serializable
{
  @Nonnull
  default UnifiedResponse createUnifiedResponse (@Nonnull final EHttpVersion eHttpVersion,
                                                 @Nonnull final EHttpMethod eHttpMethod,
                                                 @Nonnull final HttpServletRequest aHttpRequest)
  {
    return new UnifiedResponse (eHttpVersion, eHttpMethod, aHttpRequest);
  }

  /**
   * This callback method is unconditionally called before the last-modification
   * checks are performed. So this method can be used to determine the requested
   * object from the request. This method is not called if HTTP version or HTTP
   * method are not supported.
   *
   * @param aRequestScope
   *        The request scope that will be used for processing the request.
   *        Never <code>null</code>.
   * @param aUnifiedResponse
   *        The response object to be filled. Never <code>null</code>.
   * @return {@link EContinue#BREAK} to stop processing (e.g. because a resource
   *         does not exist), {@link EContinue#CONTINUE} to continue processing
   *         as usual.
   */
  @OverrideOnDemand
  default EContinue initRequestState (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                      @Nonnull final UnifiedResponse aUnifiedResponse)
  {
    return EContinue.CONTINUE;
  }

  /**
   * Get the last modification date time for the current request. If it was not
   * modified since the last request time, a 304 (not modified) response code is
   * returned. This method is always called for GET and HEAD requests. This
   * method is called after
   * {@link #initRequestState(IRequestWebScopeWithoutResponse, UnifiedResponse)}
   * .
   *
   * @param aRequestScope
   *        The request scope that will be used for processing the request.
   *        Never <code>null</code>.
   * @return <code>null</code> if no last modification date time can be
   *         determined
   */
  @Nullable
  default LocalDateTime getLastModificationDateTime (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    return null;
  }

  /**
   * Get the ETag supported for this request. If an ETag matches, a 304 (not
   * modified) response code is returned. This method is always called for GET
   * and HEAD requests.
   *
   * @param aRequestScope
   *        The request scope that will be used for processing the request.
   *        Never <code>null</code>.
   * @return <code>null</code> if this servlet does not support ETags
   */
  @Nullable
  default String getSupportedETag (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    return null;
  }

  /**
   * Called before a valid request is handled. This method is only called if
   * HTTP version matches, HTTP method is supported and sending a cached HTTP
   * response is not an option.
   *
   * @param aRequestScope
   *        The request scope that will be used for processing the request.
   *        Never <code>null</code>.
   */
  @OverrideOnDemand
  @Nonnull
  default void onRequestBegin (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {}

  /**
   * This is the main request handling method. Overwrite this method to fill
   * your HTTP response.
   *
   * @param aRequestScope
   *        The request scope to use. There is no direct access to the
   *        {@link HttpServletResponse}. Everything must be handled with the
   *        unified response! Never <code>null</code>.
   * @param aUnifiedResponse
   *        The response object to be filled. Never <code>null</code>.
   * @throws Exception
   *         In case of an error
   */
  void handleRequest (@Nonnull IRequestWebScopeWithoutResponse aRequestScope,
                      @Nonnull UnifiedResponse aUnifiedResponse) throws Exception;

  /**
   * Called when an exception occurred in
   * {@link #handleRequest(IRequestWebScopeWithoutResponse, UnifiedResponse)}.
   * This method is only called for non-request-cancel operations.
   *
   * @param aRequestScope
   *        The source request scope. Never <code>null</code>.
   * @param aUnifiedResponse
   *        The response to the current request. Never <code>null</code>.
   * @param t
   *        The Throwable that occurred. Never <code>null</code>.
   * @return {@link EContinue#CONTINUE} to propagate the Exception,
   *         {@link EContinue#BREAK} to swallow it. May not be
   *         <code>null</code>.
   */
  @OverrideOnDemand
  @Nonnull
  default EContinue onException (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                 @Nonnull final UnifiedResponse aUnifiedResponse,
                                 @Nonnull final Throwable t)
  {
    // Propagate only in debug mode
    return EContinue.valueOf (GlobalDebug.isDebugMode ());
  }

  /**
   * Called after a valid request was processed. This method is only called if
   * the handleRequest method was invoked. If an exception occurred this method
   * is called after
   * {@link #onException(IRequestWebScopeWithoutResponse, UnifiedResponse, Throwable)}
   *
   * @param aCaughtException
   *        <code>null</code> if no exception occurred, non-<code>null</code> in
   *        case of exception.
   */
  @OverrideOnDemand
  default void onRequestEnd (@Nullable final Throwable aCaughtException)
  {}
}
