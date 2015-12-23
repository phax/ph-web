package com.helger.web.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * Abstract base class for Servlet 3.1 {@link ServletOutputStream}
 *
 * @author Philip Helger
 */
public abstract class AbstractServletOutputStream extends ServletOutputStream
{
  @Override
  public boolean isReady ()
  {
    throw new UnsupportedOperationException ("isReady is not supported!");
  }

  @Override
  public void setWriteListener (final WriteListener aWriteListener)
  {
    throw new UnsupportedOperationException ("setWriteListener is not supported!");
  }
}
