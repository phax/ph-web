/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.xservlet.handler;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;

/**
 * Base interface for regular and simpler handler
 *
 * @author Philip Helger
 * @since 9.0.0
 */
public interface IXServletBasicHandler extends Serializable
{
  /**
   * Called upon Servlet initialization
   *
   * @param sApplicationID
   *        The application ID determined. Neither <code>null</code> nor empty.
   * @param aInitParams
   *        The init parameters. Never <code>null</code> but maybe empty.
   */
  default void onServletInit (@Nonnull @Nonempty final String sApplicationID,
                              @Nonnull final Map <String, String> aInitParams)
  {}

  /**
   * Called upon Servlet destruction
   */
  default void onServletDestroy ()
  {}
}
