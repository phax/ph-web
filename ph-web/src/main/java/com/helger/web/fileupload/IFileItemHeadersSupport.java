/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload;

import javax.annotation.Nullable;

/**
 * Interface that will indicate that {@link IFileItem} or
 * {@link IFileItemStream} implementations will accept the headers read for the
 * item.
 *
 * @author Michael C. Macaluso
 * @since 1.3
 * @see IFileItem
 * @see IFileItemStream
 */
public interface IFileItemHeadersSupport
{
  /**
   * Returns the collection of headers defined locally within this item.
   *
   * @return the {@link IFileItemHeaders} present for this item.
   */
  @Nullable
  IFileItemHeaders getHeaders ();

  /**
   * Sets the headers read from within an item. Implementations of
   * {@link IFileItem} or {@link IFileItemStream} should implement this
   * interface to be able to get the raw headers found within the item header
   * block.
   *
   * @param aHeaders
   *        the instance that holds onto the headers for this instance.
   */
  void setHeaders (@Nullable IFileItemHeaders aHeaders);
}
