/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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

import org.jspecify.annotations.Nullable;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.servlet.io.AbstractServletInputStream;

import jakarta.servlet.ServletInputStream;

class LoggingServletInputStream extends AbstractServletInputStream
{
  private final NonBlockingByteArrayInputStream m_aIS;
  private final ServletInputStream m_aRemainingIS;

  /**
   * @param content
   *        The buffered content that is returned first.
   * @param aRemainingIS
   *        The original input stream that delivers the content that was not buffered. May be
   *        <code>null</code> if the complete content was buffered.
   */
  LoggingServletInputStream (final byte [] content, @Nullable final ServletInputStream aRemainingIS)
  {
    m_aIS = new NonBlockingByteArrayInputStream (content);
    m_aRemainingIS = aRemainingIS;
  }

  @Override
  public boolean isFinished ()
  {
    return m_aIS.available () == 0 && (m_aRemainingIS == null || m_aRemainingIS.isFinished ());
  }

  @Override
  public boolean isReady ()
  {
    return m_aIS.available () > 0 || m_aRemainingIS == null || m_aRemainingIS.isReady ();
  }

  @Override
  public int read () throws IOException
  {
    final int ret = m_aIS.read ();
    if (ret >= 0 || m_aRemainingIS == null)
      return ret;
    return m_aRemainingIS.read ();
  }

  @Override
  public void close () throws IOException
  {
    super.close ();
    m_aIS.close ();
    if (m_aRemainingIS != null)
      m_aRemainingIS.close ();
  }
}
