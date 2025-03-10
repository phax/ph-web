/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.servlet.logging;

import java.io.IOException;
import java.io.InputStream;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.servlet.io.AbstractServletInputStream;

class LoggingServletInputStream extends AbstractServletInputStream
{
  private final InputStream m_aIS;

  LoggingServletInputStream (final byte [] content)
  {
    m_aIS = new NonBlockingByteArrayInputStream (content);
  }

  @Override
  public boolean isFinished ()
  {
    return true;
  }

  @Override
  public boolean isReady ()
  {
    return true;
  }

  @Override
  public int read () throws IOException
  {
    return m_aIS.read ();
  }

  @Override
  public void close () throws IOException
  {
    super.close ();
    m_aIS.close ();
  }
}
