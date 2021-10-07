/*
 * Copyright (C) 2020-2021 Philip Helger (www.helger.com)
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
package com.helger.dns.naptr;

import java.time.Duration;

import javax.annotation.Nonnull;

import com.helger.commons.callback.ICallback;

/**
 * Callback interface to be used to notify interested parties when a NAPTR
 * lookup takes too long.
 *
 * @author Philip Helger
 * @since 9.5.0
 */
@FunctionalInterface
public interface INaptrLookupTimeExceededCallback extends ICallback
{
  /**
   * Called when the execution time was exceeded
   *
   * @param sMsg
   *        The message to locate the source. May not be <code>null</code>.
   * @param aExecutionDuration
   *        The duration the execution took. Always &gt; 0.
   * @param aLimitDuration
   *        The duration the execution should not exceed. So the maximum
   *        configured execution time. Always &gt; 0.
   */
  void onLookupTimeExceeded (@Nonnull String sMsg, @Nonnull Duration aExecutionDuration, @Nonnull Duration aLimitDuration);
}
