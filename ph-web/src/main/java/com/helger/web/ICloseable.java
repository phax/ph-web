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
package com.helger.web;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface of an object, which may be closed.
 */
public interface ICloseable extends Closeable
{
  /**
   * Returns, whether the object is already closed.
   *
   * @return True, if the object is closed, otherwise false.
   * @throws IOException
   *         An I/O error occurred.
   */
  boolean isClosed () throws IOException;

  /**
   * Closes the object.
   *
   * @throws IOException
   *         An I/O error occurred.
   */
  void close () throws IOException;
}
