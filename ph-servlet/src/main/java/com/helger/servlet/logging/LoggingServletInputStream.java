package com.helger.servlet.logging;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;

class LoggingServletInputStream extends ServletInputStream
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
  public void setReadListener (final ReadListener readListener)
  {}

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