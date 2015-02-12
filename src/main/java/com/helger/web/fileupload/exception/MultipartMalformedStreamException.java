package com.helger.web.fileupload.exception;

import java.io.IOException;

/**
 * Thrown to indicate that the input stream fails to follow the required syntax.
 */
public final class MultipartMalformedStreamException extends IOException
{
  /**
   * Constructs a <code>MalformedStreamException</code> with no detail message.
   */
  public MultipartMalformedStreamException ()
  {}

  /**
   * Constructs an <code>MalformedStreamException</code> with the specified
   * detail message.
   *
   * @param sMsg
   *        The detail message.
   */
  public MultipartMalformedStreamException (final String sMsg)
  {
    super (sMsg);
  }

  /**
   * Constructs an <code>MalformedStreamException</code> with the specified
   * detail message.
   *
   * @param sMsg
   *        The detail message.
   * @param aCause
   *        The cause of the exception
   */
  public MultipartMalformedStreamException (final String sMsg, final Throwable aCause)
  {
    super (sMsg, aCause);
  }
}
