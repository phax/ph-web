/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.string.StringParser;
import com.helger.servlet.request.RequestHelper;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.fileupload.IFileItemFactory;
import com.helger.web.fileupload.IFileItemHeaders;
import com.helger.web.fileupload.IFileItemHeadersSupport;
import com.helger.web.fileupload.IFileItemIterator;
import com.helger.web.fileupload.IFileItemStream;
import com.helger.web.fileupload.IRequestContext;
import com.helger.web.fileupload.exception.FileUploadException;
import com.helger.web.fileupload.exception.FileUploadIOException;
import com.helger.web.fileupload.exception.IOFileUploadException;
import com.helger.web.fileupload.exception.InvalidContentTypeException;
import com.helger.web.fileupload.exception.SizeLimitExceededException;
import com.helger.web.fileupload.io.AbstractLimitedInputStream;
import com.helger.web.multipart.MultipartProgressNotifier;
import com.helger.web.multipart.MultipartStream;
import com.helger.web.progress.IProgressListener;

/**
 * <p>
 * High level API for processing file uploads.
 * </p>
 * <p>
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
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
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractFileUploadBase.class);

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

  public AbstractFileUploadBase ()
  {}

  /**
   * Returns the factory class used when creating file items.
   *
   * @return The factory class for new file items.
   */
  @Nonnull
  public abstract IFileItemFactory getFileItemFactory ();

  /**
   * Returns the maximum allowed size of a complete request, as opposed to
   * {@link #getFileSizeMax()}.
   *
   * @return The maximum allowed size, in bytes. The default value of -1
   *         indicates, that there is no limit.
   * @see #setSizeMax(long)
   */
  @CheckForSigned
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
  @CheckForSigned
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
  @Nullable
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
  public void setHeaderEncoding (@Nullable final String sHeaderEncoding)
  {
    m_sHeaderEncoding = sHeaderEncoding;
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
  public IFileItemIterator getItemIterator (@Nonnull final IRequestContext aCtx) throws FileUploadException, IOException
  {
    return new FileItemIterator (aCtx);
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
  public ICommonsList <IFileItem> parseRequest (@Nonnull final IRequestContext aCtx) throws FileUploadException
  {
    final ICommonsList <IFileItem> aItems = new CommonsArrayList <> ();
    boolean bSuccessful = false;
    try
    {
      final IFileItemIterator aItemIter = getItemIterator (aCtx);
      final IFileItemFactory aFileItemFactory = getFileItemFactory ();
      if (aFileItemFactory == null)
        throw new IllegalStateException ("No FileItemFactory has been set.");

      while (aItemIter.hasNext ())
      {
        final IFileItemStream aFileItemStream = aItemIter.next ();
        // Don't use getName() here to prevent an InvalidFileNameException.
        final IFileItem aFileItem = aFileItemFactory.createItem (aFileItemStream.getFieldName (),
                                                                 aFileItemStream.getContentType (),
                                                                 aFileItemStream.isFormField (),
                                                                 aFileItemStream.getNameUnchecked ());
        aItems.add (aFileItem);
        InputStream aIS = null;
        OutputStream aOS = null;
        try
        {
          aIS = aFileItemStream.openStream ();
          aOS = aFileItem.getOutputStream ();
          final byte [] aBuffer = new byte [8192];
          int nBytesRead;
          // potentially blocking read
          while ((nBytesRead = aIS.read (aBuffer, 0, aBuffer.length)) > -1)
          {
            aOS.write (aBuffer, 0, nBytesRead);
          }
        }
        catch (final FileUploadIOException ex)
        {
          throw (FileUploadException) ex.getCause ();
        }
        catch (final IOException ex)
        {
          throw new IOFileUploadException ("Processing of " +
                                           RequestHelper.MULTIPART_FORM_DATA +
                                           " request failed. " +
                                           ex.getMessage (),
                                           ex);
        }
        finally
        {
          StreamHelper.close (aIS);
          StreamHelper.close (aOS);
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
        // Delete all file items
        for (final IFileItem aFileItem : aItems)
        {
          try
          {
            aFileItem.delete ();
          }
          catch (final Exception ex)
          {
            // ignore it
            if (LOGGER.isErrorEnabled ())
              LOGGER.error ("Failed to delete fileItem " + aFileItem, ex);
          }
        }
      }
    }
  }

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
    // Parameter parser can handle null input
    final ICommonsMap <String, String> aParams = new ParameterParser ().setLowerCaseNames (true)
                                                                       .parse (sContentType, new char [] { ';', ',' });
    final String sBoundaryStr = aParams.get ("boundary");
    if (sBoundaryStr == null)
      return null;
    return sBoundaryStr.getBytes (StandardCharsets.ISO_8859_1);
  }

  /**
   * Retrieves the file name from the <code>Content-disposition</code> header.
   *
   * @param aHeaders
   *        The HTTP headers object.
   * @return The file name for the current <code>encapsulation</code>.
   */
  @Nullable
  protected String getFileName (@Nonnull final IFileItemHeaders aHeaders)
  {
    return _getFilename (aHeaders.getHeaderContentDisposition ());
  }

  /**
   * Returns the given content-disposition headers file name.
   *
   * @param sContentDisposition
   *        The content-disposition headers value.
   * @return The file name or <code>null</code>.
   */
  @Nullable
  private static String _getFilename (@Nullable final String sContentDisposition)
  {
    String sFilename = null;
    if (sContentDisposition != null)
    {
      final String sContentDispositionLC = sContentDisposition.toLowerCase (Locale.US);
      if (sContentDispositionLC.startsWith (RequestHelper.FORM_DATA) ||
          sContentDispositionLC.startsWith (RequestHelper.ATTACHMENT))
      {
        // Parameter parser can handle null input
        final ICommonsMap <String, String> aParams = new ParameterParser ().setLowerCaseNames (true)
                                                                           .parse (sContentDisposition, ';');
        if (aParams.containsKey ("filename"))
        {
          sFilename = aParams.get ("filename");
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
  @Nullable
  protected String getFieldName (@Nonnull final IFileItemHeaders aFileItemHeaders)
  {
    return _getFieldName (aFileItemHeaders.getHeaderContentDisposition ());
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
    if (sContentDisposition != null && sContentDisposition.toLowerCase (Locale.US).startsWith (RequestHelper.FORM_DATA))
    {
      // Parameter parser can handle null input
      final ICommonsMap <String, String> aParams = new ParameterParser ().setLowerCaseNames (true)
                                                                         .parse (sContentDisposition, ';');
      sFieldName = aParams.get ("name");
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
  @Nonnull
  protected IFileItemHeaders getParsedHeaders (@Nonnull final String sHeaderPart)
  {
    final int nLen = sHeaderPart.length ();
    final FileItemHeaders aHeaders = createFileItemHeaders ();
    int nStart = 0;
    for (;;)
    {
      int nEnd = _parseEndOfLine (sHeaderPart, nStart);
      if (nStart == nEnd)
      {
        break;
      }
      final StringBuilder aHeader = new StringBuilder (sHeaderPart.substring (nStart, nEnd));
      nStart = nEnd + 2;
      while (nStart < nLen)
      {
        int nNonWs = nStart;
        while (nNonWs < nLen)
        {
          final char c = sHeaderPart.charAt (nNonWs);
          if (c != ' ' && c != '\t')
            break;
          ++nNonWs;
        }
        if (nNonWs == nStart)
          break;

        // Continuation line found
        nEnd = _parseEndOfLine (sHeaderPart, nNonWs);
        aHeader.append (' ').append (sHeaderPart.substring (nNonWs, nEnd));

        nStart = nEnd + 2;
      }
      _parseHeaderLine (aHeaders, aHeader.toString ());
    }
    return aHeaders;
  }

  /**
   * Creates a new instance of {@link IFileItemHeaders}.
   *
   * @return The new instance.
   */
  @Nonnull
  protected FileItemHeaders createFileItemHeaders ()
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
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Found malformed HTTP header line '" + sHeader + "'");
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
  private final class FileItemIterator implements IFileItemIterator
  {
    /**
     * The multi part stream to process.
     */
    private final MultipartStream m_aMulti;
    /**
     * The notifier, which used for triggering the {@link IProgressListener}.
     */
    private final MultipartProgressNotifier m_aNotifier;
    /**
     * The boundary, which separates the various parts.
     */
    private final byte [] m_aBoundary;
    /**
     * The item, which we currently process.
     */
    private FileItemStream m_aCurrentItem;
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
    FileItemIterator (@Nonnull final IRequestContext aCtx) throws FileUploadException, IOException
    {
      ValueEnforcer.notNull (aCtx, "RequestContext");

      final String sContentType = aCtx.getContentType ();
      if (!RequestHelper.isMultipartContent (sContentType))
      {
        throw new InvalidContentTypeException ("the request doesn't contain a " +
                                               RequestHelper.MULTIPART_FORM_DATA +
                                               " or " +
                                               RequestHelper.MULTIPART_MIXED +
                                               " stream, content-type header is '" +
                                               sContentType +
                                               "'");
      }

      InputStream aIS = aCtx.getInputStream ();
      final long nContentLength = aCtx.getContentLength ();

      if (m_nSizeMax >= 0)
      {
        if (nContentLength < 0)
        {
          aIS = new AbstractLimitedInputStream (aIS, m_nSizeMax)
          {
            @Override
            protected void onLimitExceeded (final long nSizeMax, final long nCount) throws IOException
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
          // Request size is known
          if (m_nSizeMax >= 0 && nContentLength > m_nSizeMax)
          {
            throw new SizeLimitExceededException ("the request was rejected because its size (" +
                                                  nContentLength +
                                                  ") exceeds the configured maximum (" +
                                                  m_nSizeMax +
                                                  ")",
                                                  nContentLength,
                                                  m_nSizeMax);
          }
        }
      }

      String sHeaderEncoding = m_sHeaderEncoding;
      if (sHeaderEncoding == null)
        sHeaderEncoding = aCtx.getCharacterEncoding ();

      m_aBoundary = getBoundary (sContentType);
      if (m_aBoundary == null)
        throw new FileUploadException ("the request was rejected because no multipart boundary was found");

      // Content length may be -1 if not specified by sender
      m_aNotifier = new MultipartProgressNotifier (m_aListener, nContentLength);
      m_aMulti = new MultipartStream (aIS, m_aBoundary, m_aNotifier);
      m_aMulti.setHeaderEncoding (sHeaderEncoding);

      m_bSkipPreamble = true;
      _findNextItem ();
    }

    /**
     * Called for finding the next item, if any.
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
        final String sSubContentType = aFileItemHeaders.getHeaderContentType ();

        if (m_sCurrentFieldName == null)
        {
          // We're parsing the outer multipart
          final String sFieldName = getFieldName (aFileItemHeaders);
          if (sFieldName != null)
          {
            if (sSubContentType != null &&
                sSubContentType.toLowerCase (Locale.US).startsWith (RequestHelper.MULTIPART_MIXED))
            {
              m_sCurrentFieldName = sFieldName;
              // Multiple files associated with this field name
              final byte [] aSubBoundary = getBoundary (sSubContentType);
              m_aMulti.setBoundary (aSubBoundary);
              m_bSkipPreamble = true;
              continue;
            }
            final String sFilename = getFileName (aFileItemHeaders);
            m_aCurrentItem = new FileItemStream (sFilename,
                                                 sFieldName,
                                                 sSubContentType,
                                                 sFilename == null,
                                                 _getContentLength (aFileItemHeaders),
                                                 m_aMulti,
                                                 m_nFileSizeMax);
            m_aNotifier.onNextFileItem ();
            m_bItemValid = true;
            return true;
          }
        }
        else
        {
          final String sFilename = getFileName (aFileItemHeaders);
          if (sFilename != null)
          {
            m_aCurrentItem = new FileItemStream (sFilename,
                                                 m_sCurrentFieldName,
                                                 sSubContentType,
                                                 false,
                                                 _getContentLength (aFileItemHeaders),
                                                 m_aMulti,
                                                 m_nFileSizeMax);
            m_aNotifier.onNextFileItem ();
            m_bItemValid = true;
            return true;
          }
        }
        m_aMulti.discardBodyData ();
      }
    }

    private long _getContentLength (@Nonnull final IFileItemHeaders aHeaders)
    {
      return StringParser.parseLong (aHeaders.getHeaderContentLength (), -1L);
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
     * @throws NoSuchElementException
     *         No more items are available. Use {@link #hasNext()} to prevent
     *         this exception.
     * @throws FileUploadException
     *         Parsing or processing the file item failed.
     * @throws IOException
     *         Reading the file item failed.
     * @return FileItemStream instance, which provides access to the next file
     *         item.
     */
    @Nonnull
    public IFileItemStream next () throws FileUploadException, IOException
    {
      if (m_bEOF || (!m_bItemValid && !hasNext ()))
        throw new NoSuchElementException ();
      m_bItemValid = false;
      return m_aCurrentItem;
    }
  }
}
