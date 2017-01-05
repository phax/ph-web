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
package com.helger.servlet.io;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * Abstract base class for Servlet 3.1 {@link ServletInputStream}
 *
 * @author Philip Helger
 */
public abstract class AbstractServletInputStream extends ServletInputStream
{
  @Override
  public boolean isReady ()
  {
    throw new UnsupportedOperationException ("isReady is not supported!");
  }

  @Override
  public boolean isFinished ()
  {
    throw new UnsupportedOperationException ("isFinished is not supported!");
  }

  @Override
  public void setReadListener (final ReadListener readListener)
  {
    throw new UnsupportedOperationException ("setReadListener is not supported!");
  }
}
