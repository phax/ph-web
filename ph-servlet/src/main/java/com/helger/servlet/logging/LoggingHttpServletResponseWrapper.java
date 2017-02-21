package com.helger.servlet.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.helger.commons.charset.CharsetManager;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper
{
  private final LoggingServletOutpuStream m_aOS = new LoggingServletOutpuStream ();
  private final HttpServletResponse m_aDelegate;

  public LoggingHttpServletResponseWrapper (final HttpServletResponse response)
  {
    super (response);
    m_aDelegate = response;
  }

  @Override
  public ServletOutputStream getOutputStream () throws IOException
  {
    return m_aOS;
  }

  @Override
  public PrintWriter getWriter () throws IOException
  {
    return new PrintWriter (m_aOS.m_aBAOS);
  }

  public String getContentAsString ()
  {
    final String responseEncoding = m_aDelegate.getCharacterEncoding ();
    final Charset aCharset = responseEncoding != null ? CharsetManager.getCharsetFromName (responseEncoding)
                                                      : StandardCharsets.UTF_8;
    return m_aOS.m_aBAOS.getAsString (aCharset);
  }

  public byte [] getContentAsBytes ()
  {
    return m_aOS.m_aBAOS.toByteArray ();
  }

  public void writeContentTo (final OutputStream aOS) throws IOException
  {
    if (!m_aDelegate.isCommitted () && m_aOS.m_aBAOS.isNotEmpty ())
      m_aOS.m_aBAOS.writeTo (aOS);
  }

  private class LoggingServletOutpuStream extends ServletOutputStream
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
    public void write (final byte [] b, final int off, final int len) throws IOException
    {
      m_aBAOS.write (b, off, len);
    }
  }
}
