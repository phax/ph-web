/**
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.xservlet.requesttrack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.callback.ICallback;
import com.helger.web.scope.IRequestWebScope;

/**
 * Callback interface to be implemented to get notified on long running
 * requests. See {@link RequestTracker} for registration.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
@FunctionalInterface
public interface ILongRunningRequestCallback extends ICallback
{
  /**
   * Callback invoked for a single long running request
   *
   * @param sUniqueRequestID
   *        The unique request ID. Never <code>null</code> nor empty.
   * @param aRequestScope
   *        The request scope that is long running. Never <code>null</code>.
   * @param nRunningMilliseconds
   *        The milliseconds this request is already running.
   */
  void onLongRunningRequest (@Nonnull @Nonempty String sUniqueRequestID,
                             @Nonnull IRequestWebScope aRequestScope,
                             @Nonnegative long nRunningMilliseconds);
}
