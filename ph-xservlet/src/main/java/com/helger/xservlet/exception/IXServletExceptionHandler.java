package com.helger.xservlet.exception;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.callback.ICallback;
import com.helger.commons.state.EContinue;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

/**
 * High level exception handler for XServlet.
 *
 * @author Philip Helger
 */
@FunctionalInterface
public interface IXServletExceptionHandler extends ICallback
{
  /**
   * Invoked upon an exception. This handler can stop the propagation of an
   * exception e.g by creating a "clean" HTML response instead of showing the
   * stack trace. In this case the unified response provided as a parameter must
   * be filled.
   *
   * @param sApplicationID
   *        The application ID of the servlet that triggered the exception.
   *        Neither <code>null</code> nor empty.
   * @param aRequestScope
   *        Current request scope. Never <code>null</code>.
   * @param aUnifiedResponse
   *        Current response. Never <code>null</code>.
   * @param t
   *        The thrown exception. Never <code>null</code>.
   * @return {@link EContinue#CONTINUE} if further exception handlers should be
   *         invoked, {@link EContinue#BREAK} if the exception was finally
   *         handled.
   */
  @Nonnull
  EContinue onException (@Nonnull @Nonempty String sApplicationID,
                         @Nonnull IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull UnifiedResponse aUnifiedResponse,
                         @Nonnull Throwable t);
}
