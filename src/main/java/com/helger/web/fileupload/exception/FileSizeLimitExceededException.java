package com.helger.web.fileupload.exception;

import javax.annotation.Nullable;

/**
 * Thrown to indicate that A files size exceeds the configured maximum.
 */
public class FileSizeLimitExceededException extends AbstractSizeException
{
  /**
   * File name of the item, which caused the exception.
   */
  private String m_sFilename;

  /**
   * Field name of the item, which caused the exception.
   */
  private String m_sFieldName;

  /**
   * Constructs a <code>SizeExceededException</code> with the specified detail
   * message, and actual and permitted sizes.
   *
   * @param message
   *        The detail message.
   * @param actual
   *        The actual request size.
   * @param permitted
   *        The maximum permitted request size.
   */
  public FileSizeLimitExceededException (final String message, final long actual, final long permitted)
  {
    super (message, actual, permitted);
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
   * Sets the file name of the item, which caused the exception.
   *
   * @param sFilename
   *        File name
   */
  public void setFileName (@Nullable final String sFilename)
  {
    m_sFilename = sFilename;
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

  /**
   * Sets the field name of the item, which caused the exception.
   *
   * @param sFieldName
   *        Field name
   */
  public void setFieldName (@Nullable final String sFieldName)
  {
    m_sFieldName = sFieldName;
  }
}
