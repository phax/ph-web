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
import com.helger.commons.string.StringParser;
import com.helger.web.fileupload.MultipartStream.ItemInputStream;
import com.helger.web.fileupload.util.AbstractLimitedInputStream;
import com.helger.web.fileupload.util.FileItemHeadersImpl;
import com.helger.web.fileupload.util.ICloseable;
import com.helger.web.fileupload.util.Streams;

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
    final String contentType = ctx.getContentType ();
    if (contentType == null)
      return false;
    if (contentType.toLowerCase (Locale.US).startsWith (MULTIPART))
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
   * @param sizeMax
   *        The maximum allowed size, in bytes. The default value of -1
   *        indicates, that there is no limit.
   * @see #getSizeMax()
   */
  public void setSizeMax (final long sizeMax)
  {
    m_nSizeMax = sizeMax;
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
   * @param fileSizeMax
   *        Maximum size of a single uploaded file.
   */
  public void setFileSizeMax (final long fileSizeMax)
  {
    m_nFileSizeMax = fileSizeMax;
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
   * @param encoding
   *        The encoding used to read part headers.
   */
  public void setHeaderEncoding (final String encoding)
  {
    m_sHeaderEncoding = encoding;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
   * compliant <code>multipart/form-data</code> stream.
   *
   * @param ctx
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
  public IFileItemIterator getItemIterator (@Nonnull final IRequestContext ctx) throws FileUploadException, IOException
  {
    return new FileItemIteratorImpl (ctx);
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
   * compliant <code>multipart/form-data</code> stream.
   *
   * @param ctx
   *        The context for the request to be parsed.
   * @return A list of <code>FileItem</code> instances parsed from the request,
   *         in the order that they were transmitted.
   * @throws FileUploadException
   *         if there are problems reading/parsing the request or storing files.
   */
  @Nonnull
  @ReturnsMutableCopy
  public List <IFileItem> parseRequest (final IRequestContext ctx) throws FileUploadException
  {
    final List <IFileItem> items = new ArrayList <IFileItem> ();
    boolean bSuccessful = false;
    try
    {
      final IFileItemIterator iter = getItemIterator (ctx);
      final IFileItemFactory aFileItemFactory = getFileItemFactory ();
      if (aFileItemFactory == null)
        throw new NullPointerException ("No FileItemFactory has been set.");

      while (iter.hasNext ())
      {
        final IFileItemStream item = iter.next ();
        // Don't use getName() here to prevent an InvalidFileNameException.
        final String fileName = ((AbstractFileUploadBase.FileItemIteratorImpl.FileItemStreamImpl) item).m_sName;
        final IFileItem fileItem = aFileItemFactory.createItem (item.getFieldName (),
                                                                item.getContentType (),
                                                                item.isFormField (),
                                                                fileName);
        items.add (fileItem);
        try
        {
          Streams.copy (item.openStream (), fileItem.getOutputStream (), true);
        }
        catch (final FileUploadIOException e)
        {
          throw (FileUploadException) e.getCause ();
        }
        catch (final IOException e)
        {
          throw new IOFileUploadException ("Processing of " +
                                           MULTIPART_FORM_DATA +
                                           " request failed. " +
                                           e.getMessage (), e);
        }
        if (fileItem instanceof IFileItemHeadersSupport)
        {
          final IFileItemHeaders fih = item.getHeaders ();
          ((IFileItemHeadersSupport) fileItem).setHeaders (fih);
        }
      }
      bSuccessful = true;
      return items;
    }
    catch (final FileUploadIOException e)
    {
      throw (FileUploadException) e.getCause ();
    }
    catch (final IOException e)
    {
      throw new FileUploadException (e.getMessage (), e);
    }
    finally
    {
      if (!bSuccessful)
      {
        for (final IFileItem fileItem : items)
        {
          try
          {
            fileItem.delete ();
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
   * @param contentType
   *        The value of the content type header from which to extract the
   *        boundary value.
   * @return The boundary, as a byte array.
   */
  @Nullable
  protected byte [] getBoundary (final String contentType)
  {
    final ParameterParser parser = new ParameterParser ();
    parser.setLowerCaseNames (true);
    // Parameter parser can handle null input
    final Map <String, String> params = parser.parse (contentType, new char [] { ';', ',' });
    final String boundaryStr = params.get ("boundary");

    if (boundaryStr == null)
      return null;
    return CharsetManager.getAsBytes (boundaryStr, CCharset.CHARSET_ISO_8859_1_OBJ);
  }

  /**
   * Retrieves the file name from the <code>Content-disposition</code> header.
   *
   * @param headers
   *        The HTTP headers object.
   * @return The file name for the current <code>encapsulation</code>.
   */
  protected String getFileName (final IFileItemHeaders headers)
  {
    return _getFileName (headers.getHeader (CONTENT_DISPOSITION));
  }

  /**
   * Returns the given content-disposition headers file name.
   *
   * @param pContentDisposition
   *        The content-disposition headers value.
   * @return The file name
   */
  private String _getFileName (final String pContentDisposition)
  {
    String fileName = null;
    if (pContentDisposition != null)
    {
      final String cdl = pContentDisposition.toLowerCase (Locale.US);
      if (cdl.startsWith (FORM_DATA) || cdl.startsWith (ATTACHMENT))
      {
        final ParameterParser parser = new ParameterParser ();
        parser.setLowerCaseNames (true);
        // Parameter parser can handle null input
        final Map <String, String> params = parser.parse (pContentDisposition, ';');
        if (params.containsKey ("filename"))
        {
          fileName = params.get ("filename");
          if (fileName != null)
          {
            fileName = fileName.trim ();
          }
          else
          {
            // Even if there is no value, the parameter is present,
            // so we return an empty file name rather than no file
            // name.
            fileName = "";
          }
        }
      }
    }
    return fileName;
  }

  /**
   * Retrieves the field name from the <code>Content-disposition</code> header.
   *
   * @param headers
   *        A <code>Map</code> containing the HTTP request headers.
   * @return The field name for the current <code>encapsulation</code>.
   */
  protected String getFieldName (final IFileItemHeaders headers)
  {
    return _getFieldName (headers.getHeader (CONTENT_DISPOSITION));
  }

  /**
   * Returns the field name, which is given by the content-disposition header.
   *
   * @param pContentDisposition
   *        The content-dispositions header value.
   * @return The field jake
   */
  private String _getFieldName (final String pContentDisposition)
  {
    String fieldName = null;
    if (pContentDisposition != null && pContentDisposition.toLowerCase (Locale.US).startsWith (FORM_DATA))
    {
      final ParameterParser parser = new ParameterParser ();
      parser.setLowerCaseNames (true);
      // Parameter parser can handle null input
      final Map <String, String> params = parser.parse (pContentDisposition, ';');
      fieldName = params.get ("name");
      if (fieldName != null)
      {
        fieldName = fieldName.trim ();
      }
    }
    return fieldName;
  }

  /**
   * <p>
   * Parses the <code>header-part</code> and returns as key/value pairs.
   * <p>
   * If there are multiple headers of the same names, the name will map to a
   * comma-separated list containing the values.
   *
   * @param headerPart
   *        The <code>header-part</code> of the current
   *        <code>encapsulation</code>.
   * @return A <code>Map</code> containing the parsed HTTP request headers.
   */
  protected IFileItemHeaders getParsedHeaders (final String headerPart)
  {
    final int len = headerPart.length ();
    final FileItemHeadersImpl headers = newFileItemHeaders ();
    int start = 0;
    for (;;)
    {
      int end = _parseEndOfLine (headerPart, start);
      if (start == end)
      {
        break;
      }
      final StringBuilder header = new StringBuilder (headerPart.substring (start, end));
      start = end + 2;
      while (start < len)
      {
        int nonWs = start;
        while (nonWs < len)
        {
          final char c = headerPart.charAt (nonWs);
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
        end = _parseEndOfLine (headerPart, nonWs);
        header.append (' ').append (headerPart.substring (nonWs, end));

        start = end + 2;
      }
      _parseHeaderLine (headers, header.toString ());
    }
    return headers;
  }

  /**
   * Creates a new instance of {@link IFileItemHeaders}.
   *
   * @return The new instance.
   */
  protected FileItemHeadersImpl newFileItemHeaders ()
  {
    return new FileItemHeadersImpl ();
  }

  /**
   * Skips bytes until the end of the current line.
   *
   * @param headerPart
   *        The headers, which are being parsed.
   * @param end
   *        Index of the last byte, which has yet been processed.
   * @return Index of the \r\n sequence, which indicates end of line.
   */
  private int _parseEndOfLine (final String headerPart, final int end)
  {
    int index = end;
    for (;;)
    {
      final int offset = headerPart.indexOf ('\r', index);
      if (offset == -1 || offset + 1 >= headerPart.length ())
      {
        throw new IllegalStateException ("Expected headers to be terminated by an empty line.");
      }
      if (headerPart.charAt (offset + 1) == '\n')
      {
        return offset;
      }
      index = offset + 1;
    }
  }

  /**
   * Reads the next header line.
   *
   * @param headers
   *        String with all headers.
   * @param header
   *        Map where to store the current header.
   */
  private void _parseHeaderLine (final FileItemHeadersImpl headers, final String header)
  {
    final int colonOffset = header.indexOf (':');
    if (colonOffset == -1)
    {
      // This header line is malformed, skip it.
      return;
    }
    final String headerName = header.substring (0, colonOffset).trim ();
    final String headerValue = header.substring (header.indexOf (':') + 1).trim ();
    headers.addHeader (headerName, headerValue);
  }

  /**
   * The iterator, which is returned by
   * {@link AbstractFileUploadBase#getItemIterator(IRequestContext)}.
   */
  private class FileItemIteratorImpl implements IFileItemIterator
  {
    /**
     * Default implementation of {@link IFileItemStream}.
     */
    class FileItemStreamImpl implements IFileItemStream, Closeable
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
      private final InputStream m_aStream;
      /**
       * The headers, if any.
       */
      private IFileItemHeaders m_aHeaders;

      /**
       * Creates a new instance.
       *
       * @param pName
       *        The items file name, or null.
       * @param pFieldName
       *        The items field name.
       * @param pContentType
       *        The items content type, or null.
       * @param pFormField
       *        Whether the item is a form field.
       * @param pContentLength
       *        The items content length, if known, or -1
       * @throws IOException
       *         Creating the file item failed.
       */
      FileItemStreamImpl (final String pName,
                          final String pFieldName,
                          final String pContentType,
                          final boolean pFormField,
                          final long pContentLength) throws IOException
      {
        m_sName = pName;
        m_sFieldName = pFieldName;
        m_sContentType = pContentType;
        m_bFormField = pFormField;
        final ItemInputStream itemStream = m_aMulti.newInputStream ();
        InputStream istream = itemStream;
        if (m_nFileSizeMax != -1)
        {
          if (pContentLength != -1 && pContentLength > m_nFileSizeMax)
          {
            final FileSizeLimitExceededException e = new FileSizeLimitExceededException ("The field " +
                                                                                             m_sFieldName +
                                                                                             " exceeds its maximum permitted " +
                                                                                             " size of " +
                                                                                             m_nFileSizeMax +
                                                                                             " bytes.",
                                                                                         pContentLength,
                                                                                         m_nFileSizeMax);
            e.setFileName (pName);
            e.setFieldName (pFieldName);
            throw new FileUploadIOException (e);
          }
          istream = new AbstractLimitedInputStream (istream, m_nFileSizeMax)
          {
            @Override
            protected void raiseError (final long pSizeMax, final long pCount) throws IOException
            {
              itemStream.close (true);
              final FileSizeLimitExceededException e = new FileSizeLimitExceededException ("The field " +
                                                                                               m_sFieldName +
                                                                                               " exceeds its maximum permitted " +
                                                                                               " size of " +
                                                                                               pSizeMax +
                                                                                               " bytes.",
                                                                                           pCount,
                                                                                           pSizeMax);
              e.setFieldName (m_sFieldName);
              e.setFileName (m_sName);
              throw new FileUploadIOException (e);
            }
          };
        }
        m_aStream = istream;
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

      /**
       * Returns the items file name.
       *
       * @return File name, if known, or null.
       * @throws InvalidFileNameException
       *         The file name contains a NUL character, which might be an
       *         indicator of a security attack. If you intend to use the file
       *         name anyways, catch the exception and use
       *         InvalidFileNameException#getName().
       */
      public String getName ()
      {
        return Streams.checkFileName (m_sName);
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
        if (((ICloseable) m_aStream).isClosed ())
          throw new IFileItemStream.ItemSkippedException ();
        return m_aStream;
      }

      /**
       * Closes the file item.
       *
       * @throws IOException
       *         An I/O error occurred.
       */
      public void close () throws IOException
      {
        m_aStream.close ();
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
       * @param pHeaders
       *        The items header object
       */
      public void setHeaders (final IFileItemHeaders pHeaders)
      {
        m_aHeaders = pHeaders;
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
    private boolean m_bEof;

    /**
     * Creates a new instance.
     *
     * @param ctx
     *        The request context.
     * @throws FileUploadException
     *         An error occurred while parsing the request.
     * @throws IOException
     *         An I/O error occurred.
     */
    FileItemIteratorImpl (@Nonnull final IRequestContext ctx) throws FileUploadException, IOException
    {
      ValueEnforcer.notNull (ctx, "RequestContext");

      final String sContentType = ctx.getContentType ();
      if (sContentType == null || !sContentType.toLowerCase (Locale.US).startsWith (MULTIPART))
      {
        throw new InvalidContentTypeException ("the request doesn't contain a " +
                                               MULTIPART_FORM_DATA +
                                               " or " +
                                               MULTIPART_MIXED +
                                               " stream, content type header is " +
                                               sContentType);
      }

      InputStream input = ctx.getInputStream ();

      if (m_nSizeMax >= 0)
      {
        final long requestSize = ctx.getContentLength ();
        if (requestSize == -1)
        {
          input = new AbstractLimitedInputStream (input, m_nSizeMax)
          {
            @Override
            protected void raiseError (final long pSizeMax, final long pCount) throws IOException
            {
              final FileUploadException ex = new SizeLimitExceededException ("the request was rejected because" +
                                                                             " its size (" +
                                                                             pCount +
                                                                             ") exceeds the configured maximum" +
                                                                             " (" +
                                                                             pSizeMax +
                                                                             ")", pCount, pSizeMax);
              throw new FileUploadIOException (ex);
            }
          };
        }
        else
        {
          if (m_nSizeMax >= 0 && requestSize > m_nSizeMax)
          {
            throw new SizeLimitExceededException ("the request was rejected because its size (" +
                                                  requestSize +
                                                  ") exceeds the configured maximum (" +
                                                  m_nSizeMax +
                                                  ")", requestSize, m_nSizeMax);
          }
        }
      }

      String charEncoding = m_sHeaderEncoding;
      if (charEncoding == null)
      {
        charEncoding = ctx.getCharacterEncoding ();
      }

      m_aBoundary = getBoundary (sContentType);
      if (m_aBoundary == null)
      {
        throw new FileUploadException ("the request was rejected because " + "no multipart boundary was found");
      }

      // Content length may be -1 if not specified by sender
      final long nContentLength = ctx.getContentLength ();
      m_aNotifier = new MultipartStream.ProgressNotifier (m_aListener, nContentLength);
      m_aMulti = new MultipartStream (input, m_aBoundary, m_aNotifier);
      m_aMulti.setHeaderEncoding (charEncoding);

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
      if (m_bEof)
        return false;

      if (m_aCurrentItem != null)
      {
        m_aCurrentItem.close ();
        m_aCurrentItem = null;
      }
      for (;;)
      {
        boolean nextPart;
        if (m_bSkipPreamble)
        {
          nextPart = m_aMulti.skipPreamble ();
        }
        else
        {
          nextPart = m_aMulti.readBoundary ();
        }
        if (!nextPart)
        {
          if (m_sCurrentFieldName == null)
          {
            // Outer multipart terminated -> No more data
            m_bEof = true;
            return false;
          }
          // Inner multipart terminated -> Return to parsing the outer
          m_aMulti.setBoundary (m_aBoundary);
          m_sCurrentFieldName = null;
          continue;
        }
        final IFileItemHeaders headers = getParsedHeaders (m_aMulti.readHeaders ());
        if (m_sCurrentFieldName == null)
        {
          // We're parsing the outer multipart
          final String fieldName = getFieldName (headers);
          if (fieldName != null)
          {
            final String subContentType = headers.getHeader (CONTENT_TYPE);
            if (subContentType != null && subContentType.toLowerCase (Locale.US).startsWith (MULTIPART_MIXED))
            {
              m_sCurrentFieldName = fieldName;
              // Multiple files associated with this field name
              final byte [] subBoundary = getBoundary (subContentType);
              m_aMulti.setBoundary (subBoundary);
              m_bSkipPreamble = true;
              continue;
            }
            final String fileName = getFileName (headers);
            m_aCurrentItem = new FileItemStreamImpl (fileName,
                                                     fieldName,
                                                     headers.getHeader (CONTENT_TYPE),
                                                     fileName == null,
                                                     _getContentLength (headers));
            m_aNotifier.noteItem ();
            m_bItemValid = true;
            return true;
          }
        }
        else
        {
          final String fileName = getFileName (headers);
          if (fileName != null)
          {
            m_aCurrentItem = new FileItemStreamImpl (fileName,
                                                     m_sCurrentFieldName,
                                                     headers.getHeader (CONTENT_TYPE),
                                                     false,
                                                     _getContentLength (headers));
            m_aNotifier.noteItem ();
            m_bItemValid = true;
            return true;
          }
        }
        m_aMulti.discardBodyData ();
      }
    }

    private long _getContentLength (final IFileItemHeaders pHeaders)
    {
      return StringParser.parseLong (pHeaders.getHeader (CONTENT_LENGTH), -1L);
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
      if (m_bEof)
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
      if (m_bEof || (!m_bItemValid && !hasNext ()))
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
  protected abstract static class SizeException extends FileUploadException
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
    protected SizeException (final String message, final long actual, final long permitted)
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
  public static class SizeLimitExceededException extends SizeException
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
  public static class FileSizeLimitExceededException extends SizeException
  {
    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = 8150776562029630058L;

    /**
     * File name of the item, which caused the exception.
     */
    private String m_sFileName;

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
    public String getFileName ()
    {
      return m_sFileName;
    }

    /**
     * Sets the file name of the item, which caused the exception.
     * 
     * @param pFileName
     *        File name
     */
    public void setFileName (final String pFileName)
    {
      m_sFileName = pFileName;
    }

    /**
     * Returns the field name of the item, which caused the exception.
     *
     * @return Field name, if known, or null.
     */
    public String getFieldName ()
    {
      return m_sFieldName;
    }

    /**
     * Sets the field name of the item, which caused the exception.
     *
     * @param pFieldName
     *        Field name
     */
    public void setFieldName (final String pFieldName)
    {
      m_sFieldName = pFieldName;
    }
  }

  /**
   * Returns the progress listener.
   *
   * @return The progress listener, if any, or null.
   */
  public IProgressListener getProgressListener ()
  {
    return m_aListener;
  }

  /**
   * Sets the progress listener.
   *
   * @param pListener
   *        The progress listener, if any. Defaults to null.
   */
  public void setProgressListener (final IProgressListener pListener)
  {
    m_aListener = pListener;
  }
}
