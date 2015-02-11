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
package com.helger.web.fileupload;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.charset.CCharset;
import com.helger.commons.charset.CharsetManager;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.string.StringParser;
import com.helger.web.fileupload.MultipartStream.ItemInputStream;
import com.helger.web.fileupload.io.AbstractLimitedInputStream;
import com.helger.web.fileupload.io.ICloseable;
import com.helger.web.fileupload.io.Streams;

/**
 * <p>
 * High level API for processing file uploads.
 * </p>
 * <p>
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by <a
 * href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * </p>
 * <p>
 * How the data for individual parts is stored is determined by the factory used
 * to create them; a given part may be in memory, on disk, or somewhere else.
 * </p>
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 * @version $Id: FileUploadBase.java 963609 2010-07-13 06:56:47Z jochen $
 */
public abstract class AbstractFileUploadBase
{
  /**
   * <p>
   * Utility method that determines whether the request contains multipart
   * content.
   * </p>
   * <p>
   * <strong>NOTE:</strong>This method will be moved to the
   * <code>ServletFileUpload</code> class after the FileUpload 1.1 release.
   * Unfortunately, since this method is static, it is not possible to provide
   * its replacement until this method is removed.
   * </p>
   *
   * @param ctx
   *        The request context to be evaluated. Must be non-null.
   * @return <code>true</code> if the request is multipart; <code>false</code>
   *         otherwise.
   */
  public static final boolean isMultipartContent (@Nonnull final IRequestContext ctx)
  {
    final String sContentType = ctx.getContentType ();
    if (sContentType == null)
      return false;
    if (sContentType.toLowerCase (Locale.US).startsWith (MULTIPART))
      return true;
    return false;
  }

  /**
   * HTTP content type header name.
   */
  public static final String CONTENT_TYPE = "Content-type";

  /**
   * HTTP content disposition header name.
   */
  public static final String CONTENT_DISPOSITION = "Content-disposition";

  /**
   * HTTP content length header name.
   */
  public static final String CONTENT_LENGTH = "Content-length";

  /**
   * Content-disposition value for form data.
   */
  public static final String FORM_DATA = "form-data";

  /**
   * Content-disposition value for file attachment.
   */
  public static final String ATTACHMENT = "attachment";

  /**
   * Part of HTTP content type header.
   */
  public static final String MULTIPART = "multipart/";

  /**
   * HTTP content type header for multipart forms.
   */
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";

  /**
   * HTTP content type header for multiple uploads.
   */
  public static final String MULTIPART_MIXED = "multipart/mixed";

  // ----------------------------------------------------------- Data members

  /**
   * The maximum size permitted for the complete request, as opposed to
   * {@link #m_nFileSizeMax}. A value of -1 indicates no maximum.
   */
  private long m_nSizeMax = -1;

  /**
   * The maximum size permitted for a single uploaded file, as opposed to
   * {@link #m_nSizeMax}. A value of -1 indicates no maximum.
   */
  private long m_nFileSizeMax = -1;

  /**
   * The content encoding to use when reading part headers.
   */
  private String m_sHeaderEncoding;

  /**
   * The progress listener.
   */
  private IProgressListener m_aListener;

  // ----------------------------------------------------- Property accessors

  /**
   * Returns the factory class used when creating file items.
   *
   * @return The factory class for new file items.
   */
  public abstract IFileItemFactory getFileItemFactory ();

  /**
   * Returns the maximum allowed size of a complete request, as opposed to
   * {@link #getFileSizeMax()}.
   *
   * @return The maximum allowed size, in bytes. The default value of -1
   *         indicates, that there is no limit.
   * @see #setSizeMax(long)
   */
  public long getSizeMax ()
  {
    return m_nSizeMax;
  }

  /**
   * Sets the maximum allowed size of a complete request, as opposed to
   * {@link #setFileSizeMax(long)}.
   *
   * @param nSizeMax
   *        The maximum allowed size, in bytes. The default value of -1
   *        indicates, that there is no limit.
   * @see #getSizeMax()
   */
  public void setSizeMax (final long nSizeMax)
  {
    m_nSizeMax = nSizeMax;
  }

  /**
   * Returns the maximum allowed size of a single uploaded file, as opposed to
   * {@link #getSizeMax()}.
   *
   * @see #setFileSizeMax(long)
   * @return Maximum size of a single uploaded file.
   */
  public long getFileSizeMax ()
  {
    return m_nFileSizeMax;
  }

  /**
   * Sets the maximum allowed size of a single uploaded file, as opposed to
   * {@link #getSizeMax()}.
   *
   * @see #getFileSizeMax()
   * @param nFileSizeMax
   *        Maximum size of a single uploaded file.
   */
  public void setFileSizeMax (final long nFileSizeMax)
  {
    m_nFileSizeMax = nFileSizeMax;
  }

  /**
   * Retrieves the character encoding used when reading the headers of an
   * individual part. When not specified, or <code>null</code>, the request
   * encoding is used. If that is also not specified, or <code>null</code>, the
   * platform default encoding is used.
   *
   * @return The encoding used to read part headers.
   */
  public String getHeaderEncoding ()
  {
    return m_sHeaderEncoding;
  }

  /**
   * Specifies the character encoding to be used when reading the headers of
   * individual part. When not specified, or <code>null</code>, the request
   * encoding is used. If that is also not specified, or <code>null</code>, the
   * platform default encoding is used.
   *
   * @param sHeaderEncoding
   *        The encoding used to read part headers.
   */
  public void setHeaderEncoding (final String sHeaderEncoding)
  {
    m_sHeaderEncoding = sHeaderEncoding;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
   * compliant <code>multipart/form-data</code> stream.
   *
   * @param aCtx
   *        The context for the request to be parsed.
   * @return An iterator to instances of <code>FileItemStream</code> parsed from
   *         the request, in the order that they were transmitted.
   * @throws FileUploadException
   *         if there are problems reading/parsing the request or storing files.
   * @throws IOException
   *         An I/O error occurred. This may be a network error while
   *         communicating with the client or a problem while storing the
   *         uploaded content.
   */
  @Nonnull
  public IFileItemIterator getItemIterator (@Nonnull final IRequestContext aCtx) throws FileUploadException,
                                                                                IOException
  {
    return new FileItemIteratorImpl (aCtx);
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
   * compliant <code>multipart/form-data</code> stream.
   *
   * @param aCtx
   *        The context for the request to be parsed.
   * @return A list of <code>FileItem</code> instances parsed from the request,
   *         in the order that they were transmitted.
   * @throws FileUploadException
   *         if there are problems reading/parsing the request or storing files.
   */
  @Nonnull
  @ReturnsMutableCopy
  public List <IFileItem> parseRequest (@Nonnull final IRequestContext aCtx) throws FileUploadException
  {
    final List <IFileItem> aItems = new ArrayList <IFileItem> ();
    boolean bSuccessful = false;
    try
    {
      final IFileItemIterator aItemIter = getItemIterator (aCtx);
      final IFileItemFactory aFileItemFactory = getFileItemFactory ();
      if (aFileItemFactory == null)
        throw new NullPointerException ("No FileItemFactory has been set.");

      while (aItemIter.hasNext ())
      {
        final IFileItemStream aFileItemStream = aItemIter.next ();
        // Don't use getName() here to prevent an InvalidFileNameException.
        final String sFilename = ((AbstractFileUploadBase.FileItemIteratorImpl.FileItemStreamImpl) aFileItemStream).m_sName;
        final IFileItem aFileItem = aFileItemFactory.createItem (aFileItemStream.getFieldName (),
                                                                 aFileItemStream.getContentType (),
                                                                 aFileItemStream.isFormField (),
                                                                 sFilename);
        aItems.add (aFileItem);
        try
        {
          Streams.copy (aFileItemStream.openStream (), aFileItem.getOutputStream (), true);
        }
        catch (final FileUploadIOException ex)
        {
          throw (FileUploadException) ex.getCause ();
        }
        catch (final IOException ex)
        {
          throw new IOFileUploadException ("Processing of " +
                                           MULTIPART_FORM_DATA +
                                           " request failed. " +
                                           ex.getMessage (), ex);
        }
        if (aFileItem instanceof IFileItemHeadersSupport)
        {
          final IFileItemHeaders aFileItemHeaders = aFileItemStream.getHeaders ();
          ((IFileItemHeadersSupport) aFileItem).setHeaders (aFileItemHeaders);
        }
      }
      bSuccessful = true;
      return aItems;
    }
    catch (final FileUploadIOException ex)
    {
      throw (FileUploadException) ex.getCause ();
    }
    catch (final IOException ex)
    {
      throw new FileUploadException (ex.getMessage (), ex);
    }
    finally
    {
      if (!bSuccessful)
      {
        for (final IFileItem aFileItem : aItems)
        {
          try
          {
            aFileItem.delete ();
          }
          catch (final Throwable e)
          {
            // ignore it
          }
        }
      }
    }
  }

  // ------------------------------------------------------ Protected methods

  /**
   * Retrieves the boundary from the <code>Content-type</code> header.
   *
   * @param sContentType
   *        The value of the content type header from which to extract the
   *        boundary value.
   * @return The boundary, as a byte array.
   */
  @Nullable
  protected byte [] getBoundary (@Nonnull final String sContentType)
  {
    final ParameterParser parser = new ParameterParser ();
    parser.setLowerCaseNames (true);
    // Parameter parser can handle null input
    final Map <String, String> params = parser.parse (sContentType, new char [] { ';', ',' });
    final String boundaryStr = params.get ("boundary");

    if (boundaryStr == null)
      return null;
    return CharsetManager.getAsBytes (boundaryStr, CCharset.CHARSET_ISO_8859_1_OBJ);
  }

  /**
   * Retrieves the file name from the <code>Content-disposition</code> header.
   *
   * @param aHeaders
   *        The HTTP headers object.
   * @return The file name for the current <code>encapsulation</code>.
   */
  protected String getFileName (final IFileItemHeaders aHeaders)
  {
    return _getFileName (aHeaders.getHeader (CONTENT_DISPOSITION));
  }

  /**
   * Returns the given content-disposition headers file name.
   *
   * @param sContentDisposition
   *        The content-disposition headers value.
   * @return The file name
   */
  private String _getFileName (final String sContentDisposition)
  {
    String sFilename = null;
    if (sContentDisposition != null)
    {
      final String cdl = sContentDisposition.toLowerCase (Locale.US);
      if (cdl.startsWith (FORM_DATA) || cdl.startsWith (ATTACHMENT))
      {
        final ParameterParser parser = new ParameterParser ();
        parser.setLowerCaseNames (true);
        // Parameter parser can handle null input
        final Map <String, String> params = parser.parse (sContentDisposition, ';');
        if (params.containsKey ("filename"))
        {
          sFilename = params.get ("filename");
          if (sFilename != null)
          {
            sFilename = sFilename.trim ();
          }
          else
          {
            // Even if there is no value, the parameter is present,
            // so we return an empty file name rather than no file
            // name.
            sFilename = "";
          }
        }
      }
    }
    return sFilename;
  }

  /**
   * Retrieves the field name from the <code>Content-disposition</code> header.
   *
   * @param aFileItemHeaders
   *        A <code>Map</code> containing the HTTP request headers.
   * @return The field name for the current <code>encapsulation</code>.
   */
  protected String getFieldName (final IFileItemHeaders aFileItemHeaders)
  {
    return _getFieldName (aFileItemHeaders.getHeader (CONTENT_DISPOSITION));
  }

  /**
   * Returns the field name, which is given by the content-disposition header.
   *
   * @param sContentDisposition
   *        The content-dispositions header value.
   * @return The field name
   */
  @Nullable
  private static String _getFieldName (@Nullable final String sContentDisposition)
  {
    String sFieldName = null;
    if (sContentDisposition != null && sContentDisposition.toLowerCase (Locale.US).startsWith (FORM_DATA))
    {
      final ParameterParser parser = new ParameterParser ();
      parser.setLowerCaseNames (true);
      // Parameter parser can handle null input
      final Map <String, String> params = parser.parse (sContentDisposition, ';');
      sFieldName = params.get ("name");
      if (sFieldName != null)
        sFieldName = sFieldName.trim ();
    }
    return sFieldName;
  }

  /**
   * <p>
   * Parses the <code>header-part</code> and returns as key/value pairs.
   * <p>
   * If there are multiple headers of the same names, the name will map to a
   * comma-separated list containing the values.
   *
   * @param sHeaderPart
   *        The <code>header-part</code> of the current
   *        <code>encapsulation</code>.
   * @return A <code>Map</code> containing the parsed HTTP request headers.
   */
  protected IFileItemHeaders getParsedHeaders (@Nonnull final String sHeaderPart)
  {
    final int nLen = sHeaderPart.length ();
    final FileItemHeaders aHeaders = newFileItemHeaders ();
    int start = 0;
    for (;;)
    {
      int end = _parseEndOfLine (sHeaderPart, start);
      if (start == end)
      {
        break;
      }
      final StringBuilder header = new StringBuilder (sHeaderPart.substring (start, end));
      start = end + 2;
      while (start < nLen)
      {
        int nonWs = start;
        while (nonWs < nLen)
        {
          final char c = sHeaderPart.charAt (nonWs);
          if (c != ' ' && c != '\t')
          {
            break;
          }
          ++nonWs;
        }
        if (nonWs == start)
        {
          break;
        }
        // Continuation line found
        end = _parseEndOfLine (sHeaderPart, nonWs);
        header.append (' ').append (sHeaderPart.substring (nonWs, end));

        start = end + 2;
      }
      _parseHeaderLine (aHeaders, header.toString ());
    }
    return aHeaders;
  }

  /**
   * Creates a new instance of {@link IFileItemHeaders}.
   *
   * @return The new instance.
   */
  @Nonnull
  protected FileItemHeaders newFileItemHeaders ()
  {
    return new FileItemHeaders ();
  }

  /**
   * Skips bytes until the end of the current line.
   *
   * @param sHeaderPart
   *        The headers, which are being parsed.
   * @param nEnd
   *        Index of the last byte, which has yet been processed.
   * @return Index of the \r\n sequence, which indicates end of line.
   */
  private static int _parseEndOfLine (@Nonnull final String sHeaderPart, final int nEnd)
  {
    int nIndex = nEnd;
    for (;;)
    {
      final int nOffset = sHeaderPart.indexOf ('\r', nIndex);
      if (nOffset == -1 || nOffset + 1 >= sHeaderPart.length ())
        throw new IllegalStateException ("Expected headers to be terminated by an empty line.");

      if (sHeaderPart.charAt (nOffset + 1) == '\n')
        return nOffset;
      nIndex = nOffset + 1;
    }
  }

  /**
   * Reads the next header line.
   *
   * @param aHeaders
   *        String with all headers.
   * @param sHeader
   *        Map where to store the current header.
   */
  private static void _parseHeaderLine (@Nonnull final FileItemHeaders aHeaders, @Nonnull final String sHeader)
  {
    final int nColonOffset = sHeader.indexOf (':');
    if (nColonOffset == -1)
    {
      // This header line is malformed, skip it.
      return;
    }
    final String sHeaderName = sHeader.substring (0, nColonOffset).trim ();
    final String sHeaderValue = sHeader.substring (sHeader.indexOf (':') + 1).trim ();
    aHeaders.addHeader (sHeaderName, sHeaderValue);
  }

  /**
   * The iterator, which is returned by
   * {@link AbstractFileUploadBase#getItemIterator(IRequestContext)}.
   */
  private final class FileItemIteratorImpl implements IFileItemIterator
  {
    /**
     * Default implementation of {@link IFileItemStream}.
     */
    final class FileItemStreamImpl implements IFileItemStream, Closeable
    {
      /**
       * The file items content type.
       */
      private final String m_sContentType;
      /**
       * The file items field name.
       */
      private final String m_sFieldName;
      /**
       * The file items file name.
       */
      private final String m_sName;
      /**
       * Whether the file item is a form field.
       */
      private final boolean m_bFormField;
      /**
       * The file items input stream.
       */
      private final InputStream m_aIS;
      /**
       * The headers, if any.
       */
      private IFileItemHeaders m_aHeaders;

      /**
       * Creates a new instance.
       *
       * @param sName
       *        The items file name, or null.
       * @param sFieldName
       *        The items field name.
       * @param sContentType
       *        The items content type, or null.
       * @param bFormField
       *        Whether the item is a form field.
       * @param nContentLength
       *        The items content length, if known, or -1
       * @throws IOException
       *         Creating the file item failed.
       */
      FileItemStreamImpl (final String sName,
                          final String sFieldName,
                          final String sContentType,
                          final boolean bFormField,
                          final long nContentLength) throws IOException
      {
        m_sName = sName;
        m_sFieldName = sFieldName;
        m_sContentType = sContentType;
        m_bFormField = bFormField;
        final ItemInputStream aItemIS = m_aMulti.newInputStream ();
        InputStream aIS = aItemIS;
        if (m_nFileSizeMax > 0)
        {
          if (nContentLength != -1 && nContentLength > m_nFileSizeMax)
          {
            final FileSizeLimitExceededException ex = new FileSizeLimitExceededException ("The field " +
                                                                                              m_sFieldName +
                                                                                              " exceeds its maximum permitted " +
                                                                                              " size of " +
                                                                                              m_nFileSizeMax +
                                                                                              " bytes.",
                                                                                          nContentLength,
                                                                                          m_nFileSizeMax);
            ex.setFileName (sName);
            ex.setFieldName (sFieldName);
            throw new FileUploadIOException (ex);
          }
          aIS = new AbstractLimitedInputStream (aIS, m_nFileSizeMax)
          {
            @Override
            protected void raiseError (final long nSizeMax, final long nCount) throws IOException
            {
              aItemIS.close (true);
              final FileSizeLimitExceededException ex = new FileSizeLimitExceededException ("The field " +
                                                                                                m_sFieldName +
                                                                                                " exceeds its maximum permitted " +
                                                                                                " size of " +
                                                                                                nSizeMax +
                                                                                                " bytes.",
                                                                                            nCount,
                                                                                            nSizeMax);
              ex.setFieldName (m_sFieldName);
              ex.setFileName (m_sName);
              throw new FileUploadIOException (ex);
            }
          };
        }
        m_aIS = aIS;
      }

      /**
       * Returns the items content type, or null.
       *
       * @return Content type, if known, or null.
       */
      public String getContentType ()
      {
        return m_sContentType;
      }

      /**
       * Returns the items field name.
       *
       * @return Field name.
       */
      public String getFieldName ()
      {
        return m_sFieldName;
      }

      public String getName ()
      {
        return Streams.checkFileName (m_sName);
      }

      public String getNameSecure ()
      {
        return FilenameHelper.getAsSecureValidFilename (m_sName);
      }

      /**
       * Returns, whether this is a form field.
       *
       * @return True, if the item is a form field, otherwise false.
       */
      public boolean isFormField ()
      {
        return m_bFormField;
      }

      /**
       * Returns an input stream, which may be used to read the items contents.
       *
       * @return Opened input stream.
       * @throws IOException
       *         An I/O error occurred.
       */
      @Nonnull
      public InputStream openStream () throws IOException
      {
        if (((ICloseable) m_aIS).isClosed ())
          throw new IFileItemStream.ItemSkippedException ();
        return m_aIS;
      }

      /**
       * Closes the file item.
       *
       * @throws IOException
       *         An I/O error occurred.
       */
      public void close () throws IOException
      {
        m_aIS.close ();
      }

      /**
       * Returns the file item headers.
       *
       * @return The items header object
       */
      public IFileItemHeaders getHeaders ()
      {
        return m_aHeaders;
      }

      /**
       * Sets the file item headers.
       *
       * @param aHeaders
       *        The items header object
       */
      public void setHeaders (final IFileItemHeaders aHeaders)
      {
        m_aHeaders = aHeaders;
      }
    }

    /**
     * The multi part stream to process.
     */
    private final MultipartStream m_aMulti;
    /**
     * The notifier, which used for triggering the {@link IProgressListener}.
     */
    private final MultipartStream.ProgressNotifier m_aNotifier;
    /**
     * The boundary, which separates the various parts.
     */
    private final byte [] m_aBoundary;
    /**
     * The item, which we currently process.
     */
    private FileItemStreamImpl m_aCurrentItem;
    /**
     * The current items field name.
     */
    private String m_sCurrentFieldName;
    /**
     * Whether we are currently skipping the preamble.
     */
    private boolean m_bSkipPreamble;
    /**
     * Whether the current item may still be read.
     */
    private boolean m_bItemValid;
    /**
     * Whether we have seen the end of the file.
     */
    private boolean m_bEOF;

    /**
     * Creates a new instance.
     *
     * @param aCtx
     *        The request context.
     * @throws FileUploadException
     *         An error occurred while parsing the request.
     * @throws IOException
     *         An I/O error occurred.
     */
    FileItemIteratorImpl (@Nonnull final IRequestContext aCtx) throws FileUploadException, IOException
    {
      ValueEnforcer.notNull (aCtx, "RequestContext");

      final String sContentType = aCtx.getContentType ();
      if (sContentType == null || !sContentType.toLowerCase (Locale.US).startsWith (MULTIPART))
      {
        throw new InvalidContentTypeException ("the request doesn't contain a " +
                                               MULTIPART_FORM_DATA +
                                               " or " +
                                               MULTIPART_MIXED +
                                               " stream, content type header is " +
                                               sContentType);
      }

      InputStream aIS = aCtx.getInputStream ();

      if (m_nSizeMax >= 0)
      {
        final long nRequestSize = aCtx.getContentLength ();
        if (nRequestSize == -1)
        {
          aIS = new AbstractLimitedInputStream (aIS, m_nSizeMax)
          {
            @Override
            protected void raiseError (final long nSizeMax, final long nCount) throws IOException
            {
              final FileUploadException ex = new SizeLimitExceededException ("the request was rejected because its size (" +
                                                                                 nCount +
                                                                                 ") exceeds the configured maximum (" +
                                                                                 nSizeMax +
                                                                                 ")",
                                                                             nCount,
                                                                             nSizeMax);
              throw new FileUploadIOException (ex);
            }
          };
        }
        else
        {
          if (m_nSizeMax >= 0 && nRequestSize > m_nSizeMax)
          {
            throw new SizeLimitExceededException ("the request was rejected because its size (" +
                                                  nRequestSize +
                                                  ") exceeds the configured maximum (" +
                                                  m_nSizeMax +
                                                  ")", nRequestSize, m_nSizeMax);
          }
        }
      }

      String sHeaderEncoding = m_sHeaderEncoding;
      if (sHeaderEncoding == null)
        sHeaderEncoding = aCtx.getCharacterEncoding ();

      m_aBoundary = getBoundary (sContentType);
      if (m_aBoundary == null)
      {
        throw new FileUploadException ("the request was rejected because " + "no multipart boundary was found");
      }

      // Content length may be -1 if not specified by sender
      final long nContentLength = aCtx.getContentLength ();
      m_aNotifier = new MultipartStream.ProgressNotifier (m_aListener, nContentLength);
      m_aMulti = new MultipartStream (aIS, m_aBoundary, m_aNotifier);
      m_aMulti.setHeaderEncoding (sHeaderEncoding);

      m_bSkipPreamble = true;
      _findNextItem ();
    }

    /**
     * Called for finding the nex item, if any.
     *
     * @return True, if an next item was found, otherwise false.
     * @throws IOException
     *         An I/O error occurred.
     */
    private boolean _findNextItem () throws IOException
    {
      if (m_bEOF)
        return false;

      if (m_aCurrentItem != null)
      {
        m_aCurrentItem.close ();
        m_aCurrentItem = null;
      }
      for (;;)
      {
        boolean bNextPart;
        if (m_bSkipPreamble)
          bNextPart = m_aMulti.skipPreamble ();
        else
          bNextPart = m_aMulti.readBoundary ();

        if (!bNextPart)
        {
          if (m_sCurrentFieldName == null)
          {
            // Outer multipart terminated -> No more data
            m_bEOF = true;
            return false;
          }
          // Inner multipart terminated -> Return to parsing the outer
          m_aMulti.setBoundary (m_aBoundary);
          m_sCurrentFieldName = null;
          continue;
        }
        final IFileItemHeaders aFileItemHeaders = getParsedHeaders (m_aMulti.readHeaders ());
        if (m_sCurrentFieldName == null)
        {
          // We're parsing the outer multipart
          final String sFieldName = getFieldName (aFileItemHeaders);
          if (sFieldName != null)
          {
            final String sSubContentType = aFileItemHeaders.getHeader (CONTENT_TYPE);
            if (sSubContentType != null && sSubContentType.toLowerCase (Locale.US).startsWith (MULTIPART_MIXED))
            {
              m_sCurrentFieldName = sFieldName;
              // Multiple files associated with this field name
              final byte [] aSubBoundary = getBoundary (sSubContentType);
              m_aMulti.setBoundary (aSubBoundary);
              m_bSkipPreamble = true;
              continue;
            }
            final String sFilename = getFileName (aFileItemHeaders);
            m_aCurrentItem = new FileItemStreamImpl (sFilename,
                                                     sFieldName,
                                                     aFileItemHeaders.getHeader (CONTENT_TYPE),
                                                     sFilename == null,
                                                     _getContentLength (aFileItemHeaders));
            m_aNotifier.noteItem ();
            m_bItemValid = true;
            return true;
          }
        }
        else
        {
          final String sFilename = getFileName (aFileItemHeaders);
          if (sFilename != null)
          {
            m_aCurrentItem = new FileItemStreamImpl (sFilename,
                                                     m_sCurrentFieldName,
                                                     aFileItemHeaders.getHeader (CONTENT_TYPE),
                                                     false,
                                                     _getContentLength (aFileItemHeaders));
            m_aNotifier.noteItem ();
            m_bItemValid = true;
            return true;
          }
        }
        m_aMulti.discardBodyData ();
      }
    }

    private long _getContentLength (@Nonnull final IFileItemHeaders aHeaders)
    {
      return StringParser.parseLong (aHeaders.getHeader (CONTENT_LENGTH), -1L);
    }

    /**
     * Returns, whether another instance of {@link IFileItemStream} is
     * available.
     *
     * @throws FileUploadException
     *         Parsing or processing the file item failed.
     * @throws IOException
     *         Reading the file item failed.
     * @return True, if one or more additional file items are available,
     *         otherwise false.
     */
    public boolean hasNext () throws FileUploadException, IOException
    {
      if (m_bEOF)
        return false;
      if (m_bItemValid)
        return true;
      return _findNextItem ();
    }

    /**
     * Returns the next available {@link IFileItemStream}.
     *
     * @throws java.util.NoSuchElementException
     *         No more items are available. Use {@link #hasNext()} to prevent
     *         this exception.
     * @throws FileUploadException
     *         Parsing or processing the file item failed.
     * @throws IOException
     *         Reading the file item failed.
     * @return FileItemStream instance, which provides access to the next file
     *         item.
     */
    public IFileItemStream next () throws FileUploadException, IOException
    {
      if (m_bEOF || (!m_bItemValid && !hasNext ()))
        throw new NoSuchElementException ();
      m_bItemValid = false;
      return m_aCurrentItem;
    }
  }

  /**
   * This exception is thrown for hiding an inner {@link FileUploadException} in
   * an {@link IOException}.
   */
  public static class FileUploadIOException extends IOException
  {
    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = -7047616958165584154L;

    /**
     * Creates a <code>FileUploadIOException</code> with the given cause.
     *
     * @param pCause
     *        The exceptions cause, if any, or null.
     */
    public FileUploadIOException (final FileUploadException pCause)
    {
      super (pCause);
    }
  }

  /**
   * Thrown to indicate that the request is not a multipart request.
   */
  public static class InvalidContentTypeException extends FileUploadException
  {
    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = -9073026332015646668L;

    /**
     * Constructs a <code>InvalidContentTypeException</code> with no detail
     * message.
     */
    public InvalidContentTypeException ()
    {
      // Nothing to do.
    }

    /**
     * Constructs an <code>InvalidContentTypeException</code> with the specified
     * detail message.
     *
     * @param message
     *        The detail message.
     */
    public InvalidContentTypeException (final String message)
    {
      super (message);
    }
  }

  /**
   * Thrown to indicate an IOException.
   */
  public static class IOFileUploadException extends FileUploadException
  {
    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = 1749796615868477269L;

    /**
     * Creates a new instance with the given cause.
     *
     * @param pMsg
     *        The detail message.
     * @param pException
     *        The exceptions cause.
     */
    public IOFileUploadException (final String pMsg, final IOException pException)
    {
      super (pMsg, pException);
    }
  }

  /**
   * This exception is thrown, if a requests permitted size is exceeded.
   */
  protected abstract static class AbstractSizeException extends FileUploadException
  {
    private static final long serialVersionUID = -8776225574705254126L;

    /**
     * The actual size of the request.
     */
    private final long m_nActual;

    /**
     * The maximum permitted size of the request.
     */
    private final long m_nPermitted;

    /**
     * Creates a new instance.
     *
     * @param message
     *        The detail message.
     * @param actual
     *        The actual number of bytes in the request.
     * @param permitted
     *        The requests size limit, in bytes.
     */
    protected AbstractSizeException (final String message, final long actual, final long permitted)
    {
      super (message);
      m_nActual = actual;
      m_nPermitted = permitted;
    }

    /**
     * Retrieves the actual size of the request.
     *
     * @return The actual size of the request.
     */
    public long getActualSize ()
    {
      return m_nActual;
    }

    /**
     * Retrieves the permitted size of the request.
     *
     * @return The permitted size of the request.
     */
    public long getPermittedSize ()
    {
      return m_nPermitted;
    }
  }

  /**
   * Thrown to indicate that the request size exceeds the configured maximum.
   */
  public static class SizeLimitExceededException extends AbstractSizeException
  {
    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = -2474893167098052828L;

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
    public SizeLimitExceededException (final String message, final long actual, final long permitted)
    {
      super (message, actual, permitted);
    }
  }

  /**
   * Thrown to indicate that A files size exceeds the configured maximum.
   */
  public static class FileSizeLimitExceededException extends AbstractSizeException
  {
    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = 8150776562029630058L;

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

  /**
   * Returns the progress listener.
   *
   * @return The progress listener, if any. Maybe <code>null</code>.
   */
  @Nullable
  public IProgressListener getProgressListener ()
  {
    return m_aListener;
  }

  /**
   * Sets the progress listener.
   *
   * @param aListener
   *        The progress listener, if any. May be to <code>null</code>.
   */
  public void setProgressListener (@Nullable final IProgressListener aListener)
  {
    m_aListener = aListener;
  }
}
