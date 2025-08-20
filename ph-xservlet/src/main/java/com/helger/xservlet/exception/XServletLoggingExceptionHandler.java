/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.xservlet.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.stream.StreamHelper;
import com.helger.base.lang.clazz.ClassHelper;
import com.helger.base.state.EContinue;
import com.helger.web.scope.IRequestWebScope;

import jakarta.annotation.Nonnull;

/**
 * Logging implementation of {@link IXServletExceptionHandler}. Registered by
 * default.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public class XServletLoggingExceptionHandler implements IXServletExceptionHandler
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XServletLoggingExceptionHandler.class);

  @Nonnull
  public EContinue onException (@Nonnull final IRequestWebScope aRequestScope, @Nonnull final Throwable t)
  {
    final String sMsg = "Internal error on " +
                        aRequestScope.getHttpVersion ().getName () +
                        " " +
                        aRequestScope.getMethod () +
                        " on resource '" +
                        aRequestScope.getURLEncoded () +
                        "'";

    if (StreamHelper.isKnownEOFException (t))
    {
      // Debug only
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug (sMsg + " - " + ClassHelper.getClassLocalName (t) + " - " + t.getMessage ());

      // Known - nothing more to do
      return EContinue.BREAK;
    }

    // Log always including full exception
    LOGGER.error (sMsg, t);

    // Invoke next handler
    return EContinue.CONTINUE;
  }
}
