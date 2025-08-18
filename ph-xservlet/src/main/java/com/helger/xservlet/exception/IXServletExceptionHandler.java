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

import com.helger.base.callback.ICallback;
import com.helger.base.state.EContinue;
import com.helger.web.scope.IRequestWebScope;

import jakarta.annotation.Nonnull;

/**
 * High level exception handler for XServlet.
 *
 * @author Philip Helger
 * @since 9.0.0
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
   * @param aRequestScope
   *        Current request scope incl. http response object. Never
   *        <code>null</code>.
   * @param t
   *        The thrown exception. Never <code>null</code>.
   * @return {@link EContinue#CONTINUE} if further exception handlers should be
   *         invoked, {@link EContinue#BREAK} if the exception was finally
   *         handled.
   */
  @Nonnull
  EContinue onException (@Nonnull IRequestWebScope aRequestScope, @Nonnull Throwable t);
}
