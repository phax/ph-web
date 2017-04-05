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
