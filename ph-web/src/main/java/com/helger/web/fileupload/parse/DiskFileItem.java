/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.web.fileupload.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.charset.CCharset;
import com.helger.commons.charset.CharsetManager;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.ext.ICommonsMap;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.file.FileIOError;
import com.helger.commons.io.file.FileOperations;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.state.ESuccess;
import com.helger.commons.state.ISuccessIndicator;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.system.SystemProperties;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemHeaders;
import com.helger.web.fileupload.IFileItemHeadersSupport;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.exception.InvalidFileNameException;
import com.helger.web.fileupload.io.DeferredFileOutputStream;
import com.helger.web.fileupload.io.FileUploadHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * <p>
 * The default implementation of the {@link IFileItem} interface.
 * <p>
 * After retrieving an instance you may either request all contents of file at
 * once using {@link #get()} or request an {@link java.io.InputStream
 * InputStream} with {@link #getInputStream()} and process the file without
 * attempting to load it into memory, which may come handy with large files.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 * @since FileUpload 1.1
 * @version $Id: DiskFileItem.java 963609 2010-07-13 06:56:47Z jochen $
 */
@NotThreadSafe
public class DiskFileItem implements IFileItem, IFileItemHeadersSupport
{
  // Because of transient field
  private static final long serialVersionUID = 1379943273879417L;

  private static final Logger s_aLogger = LoggerFactory.getLogger (DiskFileItem.class);

  /**
   * Default content charset to be used when no explicit charset parameter is
   * provided by the sender. Media subtypes of the "text" type are defined to
   * have a default charset value of "ISO-8859-1" when received via HTTP.
   */
  public static final String DEFAULT_CHARSET = CCharset.CHARSET_ISO_8859_1;

  /**
   * Default content charset to be used when no explicit charset parameter is
   * provided by the sender. Media subtypes of the "text" type are defined to
   * have a default charset value of "ISO-8859-1" when received via HTTP.
   */
  public static final Charset DEFAULT_CHARSET_OBJ = CCharset.CHARSET_ISO_8859_1_OBJ;

  /**
   * UID used in unique file name generation.
   */
  private static final String UID = UUID.randomUUID ().toString ().replace (':', '_').replace ('-', '_');

  /**
   * Counter used in unique identifier generation.
   */
  private static final AtomicInteger s_aTempFileCounter = new AtomicInteger (0);

  /**
   * The name of the form field as provided by the browser.
   */
  private String m_sFieldName;

  /**
   * The content type passed by the browser, or <code>null</code> if not
   * defined.
   */
  private final String m_sContentType;

  /**
   * Whether or not this item is a simple form field.
   */
  private boolean m_bIsFormField;

  /**
   * The original filename in the user's filesystem.
   */
  private final String m_sFilename;

  /**
   * The size of the item, in bytes. This is used to cache the size when a file
   * item is moved from its original location.
   */
  private long m_nSize = -1;

  /**
   * The threshold above which uploads will be stored on disk.
   */
  private final int m_nSizeThreshold;

  /**
   * The directory in which uploaded files will be stored, if stored on disk.
   */
  private final File m_aTempDir;

  /**
   * Cached contents of the file.
   */
  private byte [] m_aCachedContent;

  /**
   * Output stream for this item.
   */
  private transient DeferredFileOutputStream m_aDFOS;

  /**
   * The temporary file to use.
   */
  private transient File m_aTempFile;

  /**
   * File to allow for serialization of the content of this item.
   */
  private File m_aDFOSFile;

  /**
   * The file items headers.
   */
  private IFileItemHeaders m_aHeaders;

  /**
   * Constructs a new <code>DiskFileItem</code> instance.
   *
   * @param sFieldName
   *        The name of the form field.
   * @param sContentType
   *        The content type passed by the browser or <code>null</code> if not
   *        specified.
   * @param bIsFormField
   *        Whether or not this item is a plain form field, as opposed to a file
   *        upload.
   * @param sFilename
   *        The original filename in the user's filesystem, or <code>null</code>
   *        if not specified.
   * @param nSizeThreshold
   *        The threshold, in bytes, below which items will be retained in
   *        memory and above which they will be stored as a file.
   * @param aRepository
   *        The data repository, which is the directory in which files will be
   *        created, should the item size exceed the threshold.
   *        <code>null</code> means default temp directory.
   */
  public DiskFileItem (@Nullable final String sFieldName,
                       @Nullable final String sContentType,
                       final boolean bIsFormField,
                       @Nullable final String sFilename,
                       @Nonnegative final int nSizeThreshold,
                       @Nullable final File aRepository)
  {
    m_sFieldName = sFieldName;
    m_sContentType = sContentType;
    m_bIsFormField = bIsFormField;
    m_sFilename = sFilename;
    m_nSizeThreshold = ValueEnforcer.isGT0 (nSizeThreshold, "SizeThreshold");
    m_aTempDir = aRepository != null ? aRepository : new File (SystemProperties.getTmpDir ());
    if (!FileHelper.existsDir (m_aTempDir))
      throw new IllegalArgumentException ("The tempory directory for file uploads is not existing: " +
                                          m_aTempDir.getAbsolutePath ());
    if (!FileHelper.canRead (m_aTempDir))
      throw new IllegalArgumentException ("The tempory directory for file uploads cannot be read: " +
                                          m_aTempDir.getAbsolutePath ());
    if (!FileHelper.canWrite (m_aTempDir))
      throw new IllegalArgumentException ("The tempory directory for file uploads cannot be written: " +
                                          m_aTempDir.getAbsolutePath ());
  }

  /**
   * Writes the state of this object during serialization.
   *
   * @param aOS
   *        The stream to which the state should be written.
   * @throws IOException
   *         if an error occurs.
   */
  private void writeObject (final ObjectOutputStream aOS) throws IOException
  {
    // Read the data
    if (m_aDFOS.isInMemory ())
    {
      _ensureCachedContentIsPresent ();
    }
    else
    {
      m_aCachedContent = null;
      m_aDFOSFile = m_aDFOS.getFile ();
    }

    // write out values
    aOS.defaultWriteObject ();
  }

  /**
   * Reads the state of this object during deserialization.
   *
   * @param aOIS
   *        The stream from which the state should be read.
   * @throws IOException
   *         if an error occurs.
   * @throws ClassNotFoundException
   *         if class cannot be found.
   */
  private void readObject (@Nonnull final ObjectInputStream aOIS) throws IOException, ClassNotFoundException
  {
    // read values
    aOIS.defaultReadObject ();

    try (final OutputStream aOS = getOutputStream ())
    {
      if (m_aCachedContent != null)
      {
        aOS.write (m_aCachedContent);
      }
      else
      {
        final InputStream aIS = FileHelper.getInputStream (m_aDFOSFile);
        StreamHelper.copyInputStreamToOutputStream (aIS, aOS);
        FileOperations.deleteFile (m_aDFOSFile);
        m_aDFOSFile = null;
      }
    }

    m_aCachedContent = null;
  }

  /**
   * @return The base directory for all temporary files.
   */
  @Nonnull
  public final File getTempDirectory ()
  {
    return m_aTempDir;
  }

  /**
   * Creates and returns a {@link File} representing a uniquely named temporary
   * file in the configured repository path. The lifetime of the file is tied to
   * the lifetime of the <code>FileItem</code> instance; the file will be
   * deleted when the instance is garbage collected.
   *
   * @return The {@link File} to be used for temporary storage.
   */
  @Nonnull
  protected File getTempFile ()
  {
    if (m_aTempFile == null)
    {
      // If you manage to get more than 100 million of ids, you'll
      // start getting ids longer than 8 characters.
      final String sUniqueID = StringHelper.getLeadingZero (s_aTempFileCounter.getAndIncrement (), 8);
      final String sTempFileName = "upload_" + UID + "_" + sUniqueID + ".tmp";
      m_aTempFile = new File (m_aTempDir, sTempFileName);
    }
    return m_aTempFile;
  }

  private void _ensureCachedContentIsPresent ()
  {
    if (m_aCachedContent == null)
      m_aCachedContent = m_aDFOS.getData ();
  }

  /**
   * @return An {@link InputStream} that can be used to retrieve the contents of
   *         the file.
   */
  @Nonnull
  public InputStream getInputStream ()
  {
    if (isInMemory ())
    {
      _ensureCachedContentIsPresent ();
      return new NonBlockingByteArrayInputStream (m_aCachedContent);
    }

    return FileHelper.getInputStream (m_aDFOS.getFile ());
  }

  @Nullable
  public String getContentType ()
  {
    return m_sContentType;
  }

  /**
   * Returns the content charset passed by the agent or <code>null</code> if not
   * defined.
   *
   * @return The content charset passed by the agent or <code>null</code> if not
   *         defined.
   */
  @Nullable
  public String getCharSet ()
  {
    // Parameter parser can handle null input
    final ICommonsMap <String, String> aParams = new ParameterParser ().setLowerCaseNames (true)
                                                                       .parse (getContentType (), ';');
    return aParams.get ("charset");
  }

  @Nullable
  public String getNameUnchecked ()
  {
    return m_sFilename;
  }

  @Nullable
  public String getName () throws InvalidFileNameException
  {
    return FileUploadHelper.checkFileName (m_sFilename);
  }

  @Nullable
  public String getNameSecure ()
  {
    final String sSecureName = FilenameHelper.getAsSecureValidFilename (m_sFilename);
    if (!EqualsHelper.equals (sSecureName, m_sFilename))
      s_aLogger.info ("FileItem filename was changed from '" + m_sFilename + "' to '" + sSecureName + "'");
    return sSecureName;
  }

  /**
   * Provides a hint as to whether or not the file contents will be read from
   * memory.
   *
   * @return <code>true</code> if the file contents will be read from memory;
   *         <code>false</code> otherwise.
   */
  public boolean isInMemory ()
  {
    return m_aCachedContent != null || m_aDFOS.isInMemory ();
  }

  /**
   * Returns the size of the file.
   *
   * @return The size of the file, in bytes.
   */
  @Nonnegative
  public long getSize ()
  {
    if (m_nSize >= 0)
      return m_nSize;
    if (m_aCachedContent != null)
      return m_aCachedContent.length;
    if (m_aDFOS.isInMemory ())
      return m_aDFOS.getDataLength ();
    return m_aDFOS.getFile ().length ();
  }

  /**
   * Returns the contents of the file as an array of bytes. If the contents of
   * the file were not yet cached in memory, they will be loaded from the disk
   * storage and cached.
   *
   * @return The contents of the file as an array of bytes.
   */
  @ReturnsMutableObject ("Speed")
  @SuppressFBWarnings ("EI_EXPOSE_REP")
  @Nullable
  public byte [] get ()
  {
    if (isInMemory ())
    {
      _ensureCachedContentIsPresent ();
      return m_aCachedContent;
    }

    return SimpleFileIO.getAllFileBytes (m_aDFOS.getFile ());
  }

  @Nullable
  @ReturnsMutableCopy
  public byte [] getCopy ()
  {
    if (isInMemory ())
    {
      _ensureCachedContentIsPresent ();
      return ArrayHelper.getCopy (m_aCachedContent);
    }

    return SimpleFileIO.getAllFileBytes (m_aDFOS.getFile ());
  }

  /**
   * Returns the contents of the file as a String, using the specified encoding.
   * This method uses {@link #get()} to retrieve the contents of the file.
   *
   * @param aCharset
   *        The charset to use.
   * @return The contents of the file, as a string.
   */
  @Nonnull
  public String getString (@Nonnull final Charset aCharset)
  {
    return CharsetManager.getAsString (get (), aCharset);
  }

  /**
   * Returns the contents of the file as a String, using the default character
   * encoding. This method uses {@link #get()} to retrieve the contents of the
   * file.
   *
   * @return The contents of the file, as a string.
   */
  @Nonnull
  public String getString ()
  {
    final byte [] aRawData = get ();
    final String asCharset = getCharSet ();
    final Charset aCharset = asCharset == null ? DEFAULT_CHARSET_OBJ : CharsetManager.getCharsetFromName (asCharset);
    return CharsetManager.getAsString (aRawData, aCharset);
  }

  /**
   * A convenience method to write an uploaded item to disk. The client code is
   * not concerned with whether or not the item is stored in memory, or on disk
   * in a temporary location. They just want to write the uploaded item to a
   * file.
   * <p>
   * This implementation first attempts to rename the uploaded item to the
   * specified destination file, if the item was originally written to disk.
   * Otherwise, the data will be copied to the specified file.
   * <p>
   * This method is only guaranteed to work <em>once</em>, the first time it is
   * invoked for a particular item. This is because, in the event that the
   * method renames a temporary file, that file will no longer be available to
   * copy or rename again at a later time.
   *
   * @param aDstFile
   *        The <code>File</code> into which the uploaded item should be stored.
   * @throws FileUploadException
   *         if an error occurs.
   */
  @Nonnull
  public ISuccessIndicator write (@Nonnull final File aDstFile) throws FileUploadException
  {
    ValueEnforcer.notNull (aDstFile, "DstFile");

    if (isInMemory ())
      return SimpleFileIO.writeFile (aDstFile, get ());

    final File aOutputFile = getStoreLocation ();
    if (aOutputFile != null)
    {
      // Save the length of the file
      m_nSize = aOutputFile.length ();

      /*
       * The uploaded file is being stored on disk in a temporary location so
       * move it to the desired file.
       */
      if (FileOperations.renameFile (aOutputFile, aDstFile).isSuccess ())
        return ESuccess.SUCCESS;

      // Copying needed
      return FileOperations.copyFile (aOutputFile, aDstFile);
    }

    // For whatever reason we cannot write the file to disk.
    throw new FileUploadException ("Cannot write uploaded file to: " + aDstFile.getAbsolutePath ());
  }

  /**
   * Deletes the underlying storage for a file item, including deleting any
   * associated temporary disk file. Although this storage will be deleted
   * automatically when the <code>FileItem</code> instance is garbage collected,
   * this method can be used to ensure that this is done at an earlier time,
   * thus preserving system resources.
   */
  public void delete ()
  {
    m_aCachedContent = null;
    final File aTempFile = getStoreLocation ();
    if (aTempFile != null)
    {
      final FileIOError aIOError = FileOperations.deleteFileIfExisting (aTempFile);
      if (aIOError.isFailure ())
        s_aLogger.error ("Failed to delete temporary file " + aTempFile + " with error " + aIOError.toString ());
    }
  }

  /**
   * Returns the name of the field in the multipart form corresponding to this
   * file item.
   *
   * @return The name of the form field.
   * @see #setFieldName(java.lang.String)
   */
  @Nullable
  public String getFieldName ()
  {
    return m_sFieldName;
  }

  /**
   * Sets the field name used to reference this file item.
   *
   * @param sFieldName
   *        The name of the form field.
   * @see #getFieldName()
   */
  public void setFieldName (@Nullable final String sFieldName)
  {
    m_sFieldName = sFieldName;
  }

  /**
   * Determines whether or not a <code>FileItem</code> instance represents a
   * simple form field.
   *
   * @return <code>true</code> if the instance represents a simple form field;
   *         <code>false</code> if it represents an uploaded file.
   * @see #setFormField(boolean)
   */
  public boolean isFormField ()
  {
    return m_bIsFormField;
  }

  /**
   * Specifies whether or not a <code>FileItem</code> instance represents a
   * simple form field.
   *
   * @param bIsFormField
   *        <code>true</code> if the instance represents a simple form field;
   *        <code>false</code> if it represents an uploaded file.
   * @see #isFormField()
   */
  public void setFormField (final boolean bIsFormField)
  {
    m_bIsFormField = bIsFormField;
  }

  @Nullable
  public IFileItemHeaders getHeaders ()
  {
    return m_aHeaders;
  }

  public void setHeaders (@Nullable final IFileItemHeaders aHeaders)
  {
    m_aHeaders = aHeaders;
  }

  @Nonnull
  public DeferredFileOutputStream getOutputStream ()
  {
    if (m_aDFOS == null)
    {
      final File aTempFile = getTempFile ();
      m_aDFOS = new DeferredFileOutputStream (m_nSizeThreshold, aTempFile);
    }
    return m_aDFOS;
  }

  /**
   * Returns the {@link java.io.File} object for the <code>FileItem</code>'s
   * data's temporary location on the disk. Note that for <code>FileItem</code>s
   * that have their data stored in memory, this method will return
   * <code>null</code>. When handling large files, you can use
   * {@link java.io.File#renameTo(java.io.File)} to move the file to new
   * location without copying the data, if the source and destination locations
   * reside within the same logical volume.
   *
   * @return The data file, or <code>null</code> if the data is stored in
   *         memory.
   */
  @Nullable
  public File getStoreLocation ()
  {
    return m_aDFOS == null ? null : m_aDFOS.getFile ();
  }

  /**
   * Removes the file contents from the temporary storage.
   *
   * @throws Throwable
   *         as declared by super.finalize()
   */
  @Override
  protected void finalize () throws Throwable
  {
    FileOperations.deleteFileIfExisting (m_aDFOS.getFile ());
    super.finalize ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("nameSecure", getNameSecure ())
                                       .appendIfNotNull ("storeLocation", getStoreLocation ())
                                       .append ("size", getSize ())
                                       .append ("isFormField", m_bIsFormField)
                                       .append ("fieldName", m_sFieldName)
                                       .append ("headers", m_aHeaders)
                                       .toString ();
  }
}
