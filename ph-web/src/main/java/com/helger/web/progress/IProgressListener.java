/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.web.progress;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;

/**
 * The {@link IProgressListener} may be used to display a progress bar or do
 * stuff like that.
 */
@FunctionalInterface
public interface IProgressListener
{
  /**
   * Updates the listeners status information.
   *
   * @param nBytesRead
   *        The total number of bytes, which have been read so far. Always &ge;
   *        0.
   * @param nContentLength
   *        The total number of bytes, which are being read. May be -1, if this
   *        number is unknown.
   * @param nItems
   *        The number of the field, which is currently being read. (0 = no item
   *        so far, 1 = first item is being read, ...)
   */
  void update (@Nonnegative long nBytesRead, @CheckForSigned long nContentLength, @Nonnegative int nItems);
}
