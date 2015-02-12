package com.helger.web.fileupload.exception;

import javax.annotation.Nullable;

/**
 * Thrown to indicate that A files size exceeds the configured maximum.
 */
public class FileSizeLimitExceededException extends AbstractSizeException
{
  /**
   * Field name of the item, which caused the exception.
   */
  private final String m_sFieldName;

  /**
   * File name of the item, which caused the exception.
   */
  private final String m_sFilename;

  /**
   * Constructs a <code>SizeExceededException</code> with the specified detail
   * message, and actual and permitted sizes.
   *
   * @param sMsg
   *        The detail message.
   * @param nActual
   *        The actual request size.
   * @param nPermitted
   *        The maximum permitted request size.
   * @param sFieldName
   *        Field name of the item, which caused the exception.
   * @param sFilename
   *        File name of the item, which caused the exception.
   */
  public FileSizeLimitExceededException (final String sMsg,
                                         final long nActual,
                                         final long nPermitted,
                                         @Nullable final String sFieldName,
                                         @Nullable final String sFilename)
  {
    super (sMsg, nActual, nPermitted);
    m_sFieldName = sFieldName;
    m_sFilename = sFilename;
  }

  /**
   * Returns the file name of the item, which caused the exception.
   *
   * @return File name, if known, or null.
   */
  @Nullable
  public String getFileName ()
  {
    return m_sFilename;
  }

  /**
   * Returns the field name of the item, which caused the exception.
   *
   * @return Field name, if known, or null.
   */
  @Nullable
  public String getFieldName ()
  {
    return m_sFieldName;
  }
}
