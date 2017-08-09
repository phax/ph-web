package com.helger.xservlet.exception;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.state.EContinue;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

/**
 * Logging implementation of {@link IXServletExceptionHandler}. Registered by
 * default.
 *
 * @author Philip Helger
 */
public class XServletLoggingExceptionHandler implements IXServletExceptionHandler
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (XServletLoggingExceptionHandler.class);

  @Nonnull
  public EContinue onException (@Nonnull @Nonempty final String sApplicationID,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                @Nonnull final UnifiedResponse aUnifiedResponse,
                                @Nonnull final Throwable t)
  {
    final String sMsg = "Internal error on " +
                        aRequestScope.getHttpVersion ().getName () +
                        " " +
                        aRequestScope.getMethod () +
                        " on resource '" +
                        aRequestScope.getURL () +
                        "' - Application ID '" +
                        sApplicationID +
                        "'";

    if (StreamHelper.isKnownEOFException (t))
    {
      // Debug only
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug (sMsg + " - " + ClassHelper.getClassLocalName (t) + " - " + t.getMessage ());

      // Known - nothing more to do
      return EContinue.BREAK;
    }

    // Log always including full exception
    s_aLogger.error (sMsg, t);

    // Invoke next handler
    return EContinue.CONTINUE;
  }
}
