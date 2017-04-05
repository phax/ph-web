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
package com.helger.servlet.http;

import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.servlet.io.AbstractServletOutputStream;

/**
 * Servlet output stream that gobbles up all its data.
 *
 * @author Servlet Spec 3.1
 * @since 8.7.5
 */
class CountingOnlyServletOutputStream extends AbstractServletOutputStream
{
  private int m_nContentLength = 0;

  public CountingOnlyServletOutputStream ()
  {}

  @Nonnegative
  public int getContentLength ()
  {
    return m_nContentLength;
  }

  @Override
  public void write (final int b)
  {
    m_nContentLength++;
  }

  @Override
  public void write (@Nonnull final byte [] aBuf,
                     @Nonnegative final int nOfs,
                     @Nonnegative final int nLen) throws IOException
  {
    ValueEnforcer.isArrayOfsLen (aBuf, nOfs, nLen);
    m_nContentLength += nLen;
  }
}
