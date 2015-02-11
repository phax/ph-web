package com.helger.web.fileupload.exception;

import java.io.IOException;

/**
 * This exception is thrown for hiding an inner {@link FileUploadException} in
 * an {@link IOException}.
 */
public class FileUploadIOException extends IOException
{
  /**
   * Creates a <code>FileUploadIOException</code> with the given cause.
   *
   * @param aCause
   *        The exceptions cause, if any, or null.
   */
  public FileUploadIOException (final FileUploadException aCause)
  {
    super (aCause);
  }
}
