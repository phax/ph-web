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
package com.helger.xservlet.requesttrack;

import java.util.List;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.base.callback.ICallback;

/**
 * Callback interface to be implemented to get notified when a certain amount of
 * parallel requests are present. requests. See {@link RequestTracker} for
 * registration.
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public interface IParallelRunningRequestCallback extends ICallback
{
  /**
   * Callback invoked when a certain amount of parallel requests run.
   *
   * @param nParallelRequests
   *        The number of parallel requests. Always &gt; 0.
   * @param aRequests
   *        The list of requests currently running. The size should be identical
   *        to the number of parallel requests. Never <code>null</code>.
   */
  void onParallelRunningRequests (@Nonnegative int nParallelRequests, @NonNull @Nonempty List <TrackedRequest> aRequests);

  /**
   * This method is only called after the threshold was exceeded, when it is
   * back to normal.
   *
   * @since 4.0.2
   */
  void onParallelRunningRequestsBelowLimit ();
}
