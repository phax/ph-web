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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotations.ReturnsMutableObject;
import com.helger.commons.charset.CCharset;
import com.helger.commons.charset.CharsetManager;
import com.helger.commons.equals.EqualsUtils;
import com.helger.commons.io.file.FileOperations;
import com.helger.commons.io.file.FileUtils;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.streams.NonBlockingByteArrayInputStream;
import com.helger.commons.io.streams.StreamUtils;
import com.helger.commons.state.ESuccess;
import com.helger.commons.state.ISuccessIndicator;
import com.helger.web.fileupload.FileUploadException;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemHeaders;
import com.helger.web.fileupload.IFileItemHeadersSupport;
import com.helger.web.fileupload.ParameterParser;

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
  private static final AtomicInteger s_nCounter = new AtomicInteger (0);

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
  private final File m_aRepository;

  /**
   * Cached contents of the file.
   */
  private byte [] m_aCachedContent;

  /**
   * Output stream for this item.
   */
  private transient DeferredFileOutputStream m_aDfos;

  /**
   * The temporary file to use.
   */
  private transient File m_aTempFile;

  /**
   * File to allow for serialization of the content of this item.
   */
  private File m_aDfosFile;

  /**
   * The file items headers.
   */
  private IFileItemHeaders m_aHeaders;

  // ----------------------------------------------------------- Constructors

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
   */
  public DiskFileItem (final String sFieldName,
                       @Nullable final String sContentType,
                       final boolean bIsFormField,
                       @Nullable final String sFilename,
                       final int nSizeThreshold,
                       @Nullable final File aRepository)
  {
    m_sFieldName = sFieldName;
    m_sContentType = sContentType;
    m_bIsFormField = bIsFormField;
    m_sFilename = sFilename;
    m_nSizeThreshold = nSizeThreshold;
    m_aRepository = aRepository;
  }

  // ------------------------------- Methods from javax.activation.DataSource

  /**
   * @return An {@link InputStream} that can be used to retrieve the contents of
   *         the file.
   */
  @Nonnull
  public InputStream getInputStream ()
  {
    if (!isInMemory ())
      return FileUtils.getInputStream (m_aDfos.getFile ());

    if (m_aCachedContent == null)
      m_aCachedContent = m_aDfos.getData ();

    return new NonBlockingByteArrayInputStream (m_aCachedContent);
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
    final ParameterParser aParser = new ParameterParser ();
    aParser.setLowerCaseNames (true);
    // Parameter parser can handle null input
    final Map <String, String> params = aParser.parse (getContentType (), ';');
    return params.get ("charset");
  }

  @Nullable
  public String getNameUnchecked ()
  {
    return m_sFilename;
  }

  @Nullable
  public String getName ()
  {
    return Streams.checkFileName (m_sFilename);
  }

  @Nullable
  public String getNameSecure ()
  {
    final String sSecureName = FilenameHelper.getAsSecureValidFilename (m_sFilename);
    if (!EqualsUtils.equals (sSecureName, m_sFilename))
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
    return m_aCachedContent != null || m_aDfos.isInMemory ();
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
    if (m_aDfos.isInMemory ())
      return m_aDfos.getData ().length;
    return m_aDfos.getFile ().length ();
  }

  /**
   * Returns the contents of the file as an array of bytes. If the contents of
   * the file were not yet cached in memory, they will be loaded from the disk
   * storage and cached.
   *
   * @return The contents of the file as an array of bytes.
   */
  @ReturnsMutableObject (reason = "Speed")
  @SuppressFBWarnings ("EI_EXPOSE_REP")
  @Nullable
  public byte [] get ()
  {
    if (isInMemory ())
    {
      if (m_aCachedContent == null)
        m_aCachedContent = m_aDfos.getData ();
      return m_aCachedContent;
    }

    return SimpleFileIO.readFileBytes (m_aDfos.getFile ());
  }

  /**
   * Returns the contents of the file as a String, using the specified encoding.
   * This method uses {@link #get()} to retrieve the contents of the file.
   *
   * @param sCharset
   *        The charset to use.
   * @return The contents of the file, as a string.
   * @throws UnsupportedEncodingException
   *         if the requested character encoding is not available.
   */
  @Nonnull
  public String getString (final String sCharset) throws UnsupportedEncodingException
  {
    return new String (get (), sCharset);
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
    return new String (get (), aCharset);
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
    final byte [] rawdata = get ();
    final String charset = getCharSet ();
    final Charset aCharset = charset == null ? DEFAULT_CHARSET_OBJ : CharsetManager.getCharsetFromName (charset);
    return CharsetManager.getAsString (rawdata, aCharset);
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
   * @param aFile
   *        The <code>File</code> into which the uploaded item should be stored.
   * @throws FileUploadException
   *         if an error occurs.
   */
  @Nonnull
  public ISuccessIndicator write (@Nonnull final File aFile) throws FileUploadException
  {
    if (isInMemory ())
      return SimpleFileIO.writeFile (aFile, get ());

    final File aOutputFile = getStoreLocation ();
    if (aOutputFile != null)
    {
      // Save the length of the file
      m_nSize = aOutputFile.length ();

      /*
       * The uploaded file is being stored on disk in a temporary location so
       * move it to the desired file.
       */
      if (FileOperations.renameFile (aOutputFile, aFile).isSuccess ())
        return ESuccess.SUCCESS;

      // Copying needed
      return FileOperations.copyFile (aOutputFile, aFile);
    }

    // For whatever reason we cannot write the file to disk.
    throw new FileUploadException ("Cannot write uploaded file to disk!");
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
    final File outputFile = getStoreLocation ();
    if (outputFile != null && outputFile.exists ())
      FileOperations.deleteFile (outputFile);
  }

  /**
   * Returns the name of the field in the multipart form corresponding to this
   * file item.
   *
   * @return The name of the form field.
   * @see #setFieldName(java.lang.String)
   */
  public String getFieldName ()
  {
    return m_sFieldName;
  }

  /**
   * Sets the field name used to reference this file item.
   *
   * @param fieldName
   *        The name of the form field.
   * @see #getFieldName()
   */
  public void setFieldName (final String fieldName)
  {
    m_sFieldName = fieldName;
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
   * @param state
   *        <code>true</code> if the instance represents a simple form field;
   *        <code>false</code> if it represents an uploaded file.
   * @see #isFormField()
   */
  public void setFormField (final boolean state)
  {
    m_bIsFormField = state;
  }

  @Nonnull
  public DeferredFileOutputStream getOutputStream ()
  {
    if (m_aDfos == null)
    {
      final File outputFile = getTempFile ();
      m_aDfos = new DeferredFileOutputStream (m_nSizeThreshold, outputFile);
    }
    return m_aDfos;
  }

  // --------------------------------------------------------- Public methods

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
    return m_aDfos == null ? null : m_aDfos.getFile ();
  }

  // ------------------------------------------------------ Protected methods

  /**
   * Removes the file contents from the temporary storage.
   *
   * @throws Throwable
   *         as declared by super.finalize()
   */
  @Override
  protected void finalize () throws Throwable
  {
    final File outputFile = m_aDfos.getFile ();
    if (outputFile != null && outputFile.exists ())
      FileOperations.deleteFile (outputFile);
    super.finalize ();
  }

  /**
   * Creates and returns a {@link java.io.File File} representing a uniquely
   * named temporary file in the configured repository path. The lifetime of the
   * file is tied to the lifetime of the <code>FileItem</code> instance; the
   * file will be deleted when the instance is garbage collected.
   *
   * @return The {@link java.io.File File} to be used for temporary storage.
   */
  @Nonnull
  protected File getTempFile ()
  {
    if (m_aTempFile == null)
    {
      File tempDir = m_aRepository;
      if (tempDir == null)
        tempDir = new File (System.getProperty ("java.io.tmpdir"));

      final String tempFileName = "upload_" + UID + "_" + _getUniqueId () + ".tmp";
      m_aTempFile = new File (tempDir, tempFileName);
    }
    return m_aTempFile;
  }

  // -------------------------------------------------------- Private methods

  /**
   * Returns an identifier that is unique within the class loader used to load
   * this class, but does not have random-like apearance.
   *
   * @return A String with the non-random looking instance identifier.
   */
  @Nonnull
  private static String _getUniqueId ()
  {
    final int limit = 100000000;
    final int current = s_nCounter.getAndIncrement ();
    String id = Integer.toString (current);

    // If you manage to get more than 100 million of ids, you'll
    // start getting ids longer than 8 characters.
    if (current < limit)
    {
      id = ("00000000" + id).substring (id.length ());
    }
    return id;
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object.
   */
  @Override
  public String toString ()
  {
    return "name=" +
           getName () +
           ", StoreLocation=" +
           getStoreLocation () +
           ", size=" +
           getSize () +
           "bytes, " +
           "isFormField=" +
           isFormField () +
           ", FieldName=" +
           getFieldName ();
  }

  // -------------------------------------------------- Serialization methods

  /**
   * Writes the state of this object during serialization.
   *
   * @param out
   *        The stream to which the state should be written.
   * @throws IOException
   *         if an error occurs.
   */
  private void writeObject (final ObjectOutputStream out) throws IOException
  {
    // Read the data
    if (m_aDfos.isInMemory ())
    {
      m_aCachedContent = get ();
    }
    else
    {
      m_aCachedContent = null;
      m_aDfosFile = m_aDfos.getFile ();
    }

    // write out values
    out.defaultWriteObject ();
  }

  /**
   * Reads the state of this object during deserialization.
   *
   * @param in
   *        The stream from which the state should be read.
   * @throws IOException
   *         if an error occurs.
   * @throws ClassNotFoundException
   *         if class cannot be found.
   */
  private void readObject (final ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    // read values
    in.defaultReadObject ();

    final OutputStream output = getOutputStream ();
    if (m_aCachedContent != null)
    {
      output.write (m_aCachedContent);
    }
    else
    {
      final InputStream input = FileUtils.getInputStream (m_aDfosFile);
      StreamUtils.copyInputStreamToOutputStream (input, output);
      FileOperations.deleteFile (m_aDfosFile);
      m_aDfosFile = null;
    }
    output.close ();

    m_aCachedContent = null;
  }

  /**
   * Returns the file item headers.
   *
   * @return The file items headers.
   */
  public IFileItemHeaders getHeaders ()
  {
    return m_aHeaders;
  }

  /**
   * Sets the file item headers.
   *
   * @param pHeaders
   *        The file items headers.
   */
  public void setHeaders (final IFileItemHeaders pHeaders)
  {
    m_aHeaders = pHeaders;
  }
}
