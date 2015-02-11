package com.helger.web.fileupload.exception;

import java.io.IOException;

/**
 * Thrown to indicate an IOException.
 */
public class IOFileUploadException extends FileUploadException
{
  /**
   * Creates a new instance with the given cause.
   *
   * @param sMsg
   *        The detail message.
   * @param aException
   *        The exceptions cause.
   */
  public IOFileUploadException (final String sMsg, final IOException aException)
  {
    super (sMsg, aException);
  }
}
