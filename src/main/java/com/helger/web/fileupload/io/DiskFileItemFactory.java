/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.web.fileupload.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.annotations.VisibleForTesting;
import com.helger.commons.collections.ContainerHelper;
import com.helger.commons.io.file.FileIOError;
import com.helger.commons.io.file.FileOperations;
import com.helger.web.fileupload.IFileItemFactory;

/**
 * <p>
 * The default {@link com.helger.web.fileupload.IFileItemFactory} implementation.
 * This implementation creates {@link DiskFileItem} instances which keep their
 * content either in memory, for smaller items, or in a temporary file on disk,
 * for larger items. The size threshold, above which content will be stored on
 * disk, is configurable, as is the directory in which temporary files will be
 * created.
 * </p>
 * <p>
 * If not otherwise configured, the default configuration values are as follows:
 * <ul>
 * <li>Size threshold is 10KB.</li>
 * <li>Repository is the system default temp directory, as returned by
 * <code>System.getProperty("java.io.tmpdir")</code>.</li>
 * </ul>
 * </p>
 * <p>
 * Temporary files, which are created for file items, should be deleted later
 * on.
 * </p>
 * 
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Philip Helger
 */
@ThreadSafe
public class DiskFileItemFactory implements IFileItemFactory
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DiskFileItemFactory.class);

  protected final ReadWriteLock m_aRWLock = new ReentrantReadWriteLock ();

  /**
   * The directory in which uploaded files will be stored, if stored on disk.
   */
  private File m_aRepository;

  /**
   * The threshold above which uploads will be stored on disk.
   */
  private final int m_nSizeThreshold;

  private final List <File> m_aTempFiles = new ArrayList <File> ();

  @VisibleForTesting
  public DiskFileItemFactory (@Nonnegative final int nSizeThreshold)
  {
    this (nSizeThreshold, null);
  }

  /**
   * Constructs a preconfigured instance of this class.
   * 
   * @param nSizeThreshold
   *        The threshold, in bytes, below which items will be retained in
   *        memory and above which they will be stored as a file.
   * @param aRepository
   *        The data repository, which is the directory in which files will be
   *        created, should the item size exceed the threshold.
   */
  public DiskFileItemFactory (@Nonnegative final int nSizeThreshold, @Nullable final File aRepository)
  {
    m_nSizeThreshold = nSizeThreshold;
    m_aRepository = aRepository;
  }

  public void setRepository (@Nullable final File aRepository)
  {
    m_aRepository = aRepository;
  }

  private void _addTempFile (@Nonnull final File aFile)
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      m_aTempFiles.add (aFile);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Create a new {@link com.helger.web.fileupload.io.DiskFileItem} instance from
   * the supplied parameters and the local factory configuration.
   * 
   * @param sFieldName
   *        The name of the form field.
   * @param sContentType
   *        The content type of the form field.
   * @param bIsFormField
   *        <code>true</code> if this is a plain form field; <code>false</code>
   *        otherwise.
   * @param sFileName
   *        The name of the uploaded file, if any, as supplied by the browser or
   *        other client.
   * @return The newly created file item.
   */
  @Nonnull
  public DiskFileItem createItem (final String sFieldName,
                                  final String sContentType,
                                  final boolean bIsFormField,
                                  final String sFileName)
  {
    final DiskFileItem result = new DiskFileItem (sFieldName,
                                                  sContentType,
                                                  bIsFormField,
                                                  sFileName,
                                                  m_nSizeThreshold,
                                                  m_aRepository);
    // Add the temp file - may be non-existing if the size is below the
    // threshold
    _addTempFile (result.getTempFile ());
    return result;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <File> getAllTemporaryFiles ()
  {
    m_aRWLock.readLock ().lock ();
    try
    {
      return ContainerHelper.newList (m_aTempFiles);
    }
    finally
    {
      m_aRWLock.readLock ().unlock ();
    }
  }

  public void deleteAllTemporaryFiles ()
  {
    List <File> aTempFiles;
    m_aRWLock.writeLock ().lock ();
    try
    {
      aTempFiles = ContainerHelper.newList (m_aTempFiles);
      m_aTempFiles.clear ();
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    for (final File aFile : aTempFiles)
    {
      final FileIOError aIOError = FileOperations.deleteFileIfExisting (aFile);
      if (aIOError.isFailure ())
      {
        s_aLogger.error ("Failed to delete temporary file " + aFile + " with error " + aIOError.toString ());
        _addTempFile (aFile);
      }
    }
  }
}
