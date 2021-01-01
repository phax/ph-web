/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.web.scope;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for a single web request scope object that also offers access to
 * the HTTP servlet response.
 *
 * @author Philip Helger
 */
public interface IRequestWebScope extends IRequestWebScopeWithoutResponse
{
  /**
   * @return The underlying HTTP servlet response object
   */
  @Nonnull
  HttpServletResponse getResponse ();

  /**
   * @return The {@link OutputStream} to write to the HTTP servlet response
   * @throws IOException
   *         In case of an error
   */
  @Nonnull
  default OutputStream getOutputStream () throws IOException
  {
    return getResponse ().getOutputStream ();
  }
}
