package com.helger.web.fileupload.exception;

import java.io.IOException;

/**
 * Thrown upon attempt of setting an invalid boundary token.
 */
public final class MultipartIllegalBoundaryException extends IOException
{
  /**
   * Constructs an <code>IllegalBoundaryException</code> with the specified
   * detail message.
   *
   * @param sMsg
   *        The detail message.
   */
  public MultipartIllegalBoundaryException (final String sMsg)
  {
    super (sMsg);
  }
}
