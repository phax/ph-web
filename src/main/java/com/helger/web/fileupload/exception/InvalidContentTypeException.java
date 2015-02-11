package com.helger.web.fileupload.exception;

/**
 * Thrown to indicate that the request is not a multipart request.
 */
public class InvalidContentTypeException extends FileUploadException
{
  /**
   * Constructs a <code>InvalidContentTypeException</code> with no detail
   * message.
   */
  public InvalidContentTypeException ()
  {}

  /**
   * Constructs an <code>InvalidContentTypeException</code> with the specified
   * detail message.
   *
   * @param sMsg
   *        The detail message.
   */
  public InvalidContentTypeException (final String sMsg)
  {
    super (sMsg);
  }
}
