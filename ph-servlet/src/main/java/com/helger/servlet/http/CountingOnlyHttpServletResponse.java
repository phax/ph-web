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
package com.helger.servlet.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.helger.commons.http.CHttpHeader;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * A response that includes no body, for use in (dumb) "HEAD" support. This just
 * swallows that body, counting the bytes in order to set the content length
 * appropriately. All other methods delegate directly to the wrapped HTTP
 * Servlet Response object.
 *
 * @author Servlet Spec 3.1
 * @since 8.8.0
 */
public class CountingOnlyHttpServletResponse extends HttpServletResponseWrapper
{
  private final CountingOnlyServletOutputStream m_aCountOnlyOS;
  private PrintWriter m_aWriter;
  private boolean m_bContentLengthSet;
  private boolean m_bUsingOutputStream;

  public CountingOnlyHttpServletResponse (final HttpServletResponse aResponse)
  {
    super (aResponse);
    m_aCountOnlyOS = new CountingOnlyServletOutputStream ();
  }

  public boolean isContentLengthSet ()
  {
    return m_bContentLengthSet;
  }

  public void setContentLengthAutomatically ()
  {
    if (!m_bContentLengthSet)
    {
      if (m_aWriter != null)
        m_aWriter.flush ();
      setContentLengthLong (m_aCountOnlyOS.getContentLength ());
    }
  }

  @Override
  public void setContentLength (final int len)
  {
    super.setContentLength (len);
    m_bContentLengthSet = true;
  }

  @Override
  public void setContentLengthLong (final long len)
  {
    super.setContentLengthLong (len);
    m_bContentLengthSet = true;
  }

  private void _checkHeader (final String name)
  {
    if (CHttpHeader.CONTENT_LENGTH.equalsIgnoreCase (name))
      m_bContentLengthSet = true;
  }

  @Override
  public void setHeader (final String name, final String value)
  {
    super.setHeader (name, value);
    _checkHeader (name);
  }

  @Override
  public void addHeader (final String name, final String value)
  {
    super.addHeader (name, value);
    _checkHeader (name);
  }

  @Override
  public void setIntHeader (final String name, final int value)
  {
    super.setIntHeader (name, value);
    _checkHeader (name);
  }

  @Override
  public void addIntHeader (final String name, final int value)
  {
    super.addIntHeader (name, value);
    _checkHeader (name);
  }

  @Override
  public ServletOutputStream getOutputStream () throws IOException
  {
    if (m_aWriter != null)
      throw new IllegalStateException ("You already called getWriter!");
    m_bUsingOutputStream = true;
    return m_aCountOnlyOS;
  }

  @Override
  public PrintWriter getWriter () throws UnsupportedEncodingException
  {
    if (m_bUsingOutputStream)
      throw new IllegalStateException ("You already called getOutputStream!");

    if (m_aWriter == null)
    {
      final OutputStreamWriter aOSW = new OutputStreamWriter (m_aCountOnlyOS, getCharacterEncoding ());
      m_aWriter = new PrintWriter (aOSW);
    }
    return m_aWriter;
  }
}
