/*
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
package com.helger.servlet.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.io.stream.StreamHelper;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper
{
  private final LoggingServletOutpuStream m_aOS = new LoggingServletOutpuStream ();
  private final HttpServletResponse m_aDelegate;

  public LoggingHttpServletResponseWrapper (@Nonnull final HttpServletResponse aDelegate)
  {
    super (aDelegate);
    m_aDelegate = aDelegate;
  }

  @Override
  @Nonnull
  public ServletOutputStream getOutputStream () throws IOException
  {
    return m_aOS;
  }

  @Override
  @Nonnull
  public PrintWriter getWriter () throws IOException
  {
    return new PrintWriter (StreamHelper.createWriter (m_aOS.m_aBAOS, _getCharset ()));
  }

  @Nonnull
  private Charset _getCharset ()
  {
    final String sResponseEncoding = m_aDelegate.getCharacterEncoding ();
    return CharsetHelper.getCharsetFromNameOrDefault (sResponseEncoding, StandardCharsets.UTF_8);
  }

  @Nonnull
  public String getContentAsString ()
  {
    return m_aOS.m_aBAOS.getAsString (_getCharset ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public byte [] getContentAsBytes ()
  {
    return m_aOS.m_aBAOS.toByteArray ();
  }

  public void writeContentTo (@Nonnull final OutputStream aOS) throws IOException
  {
    if (!m_aDelegate.isCommitted () && m_aOS.m_aBAOS.isNotEmpty ())
      m_aOS.m_aBAOS.writeTo (aOS);
  }

  private static class LoggingServletOutpuStream extends ServletOutputStream
  {
    private final NonBlockingByteArrayOutputStream m_aBAOS = new NonBlockingByteArrayOutputStream ();

    @Override
    public boolean isReady ()
    {
      return true;
    }

    @Override
    public void setWriteListener (final WriteListener writeListener)
    {}

    @Override
    public void write (final int b) throws IOException
    {
      m_aBAOS.write (b);
    }

    @Override
    public void write (final byte [] b) throws IOException
    {
      m_aBAOS.write (b);
    }

    @Override
    public void write (final byte [] b, final int nOfs, final int nLen) throws IOException
    {
      m_aBAOS.write (b, nOfs, nLen);
    }
  }
}
