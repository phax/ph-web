package com.helger.web.servlet;

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
