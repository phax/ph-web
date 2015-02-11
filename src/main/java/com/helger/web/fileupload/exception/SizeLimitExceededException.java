package com.helger.web.fileupload.exception;

/**
 * Thrown to indicate that the request size exceeds the configured maximum.
 */
public class SizeLimitExceededException extends AbstractSizeException
{
  /**
   * Constructs a <code>SizeExceededException</code> with the specified detail
   * message, and actual and permitted sizes.
   *
   * @param sMessage
   *        The detail message.
   * @param nActual
   *        The actual request size.
   * @param nPermitted
   *        The maximum permitted request size.
   */
  public SizeLimitExceededException (final String sMessage, final long nActual, final long nPermitted)
  {
    super (sMessage, nActual, nPermitted);
  }
}
