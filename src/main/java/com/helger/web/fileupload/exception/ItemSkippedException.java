package com.helger.web.fileupload.exception;

import java.io.IOException;
import java.io.InputStream;

import com.helger.web.fileupload.IFileItemStream;

/**
 * This exception is thrown, if an attempt is made to read data from the
 * {@link InputStream}, which has been returned by
 * {@link IFileItemStream#openStream()}, after
 * {@link java.util.Iterator#hasNext()} has been invoked on the iterator, which
 * created the {@link IFileItemStream}.
 */
public class ItemSkippedException extends IOException
{
  public ItemSkippedException ()
  {}
}
