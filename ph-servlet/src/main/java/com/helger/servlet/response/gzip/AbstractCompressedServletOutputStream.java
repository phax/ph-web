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
package com.helger.servlet.response.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.http.CHTTPHeader;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.servlet.io.AbstractServletOutputStream;
import com.helger.servlet.response.ResponseHelper;

/**
 * Special {@link ServletOutputStream} that knows whether it is closed or not
 *
 * @author Philip Helger
 */
public abstract class AbstractCompressedServletOutputStream extends AbstractServletOutputStream
{
  private static final int DEFAULT_BUFSIZE = 8192;
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractCompressedServletOutputStream.class);
  private final HttpServletRequest m_aHttpRequest;
  private final HttpServletResponse m_aHttpResponse;
  private final String m_sContentEncoding;
  private OutputStream m_aOS;
  private NonBlockingByteArrayOutputStream m_aBAOS;
  private DeflaterOutputStream m_aCompressedOS;
  private boolean m_bClosed = false;
  private boolean m_bDoNotCompress = false;
  private long m_nContentLength;
  private final long m_nMinCompressSize;

  public AbstractCompressedServletOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                                @Nonnull final HttpServletResponse aHttpResponse,
                                                @Nonnull final String sContentEncoding,
                                                final long nContentLength,
                                                @Nonnegative final int nMinCompressSize) throws IOException
  {
    m_aHttpRequest = ValueEnforcer.notNull (aHttpRequest, "HttpRequest");
    m_aHttpResponse = ValueEnforcer.notNull (aHttpResponse, "HttpResponse");
    m_sContentEncoding = ValueEnforcer.notEmpty (sContentEncoding, "ContentEncoding");
    m_nContentLength = nContentLength;
    m_nMinCompressSize = nMinCompressSize;
    if (nMinCompressSize == 0)
      doCompress ("ctor: no min compress size");
  }

  private static void _debugLog (final boolean bCompress, final String sMsg)
  {
    if (CompressFilterSettings.isDebugModeEnabled ())
      s_aLogger.info ((bCompress ? "Compressing: " : "Not compressing: ") + sMsg);
  }

  public final void resetBuffer ()
  {
    if (m_aHttpResponse.isCommitted ())
      throw new IllegalStateException ("Committed");
    m_aOS = null;
    m_aBAOS = null;
    if (m_aCompressedOS != null)
    {
      // Remove header again
      m_aHttpResponse.setHeader (CHTTPHeader.CONTENT_ENCODING, null);
      m_aCompressedOS = null;
    }
    m_bClosed = false;
    m_bDoNotCompress = false;
  }

  public final void setContentLength (final long nLength)
  {
    if (CompressFilterSettings.isDebugModeEnabled ())
      s_aLogger.info ("Setting content length to " + nLength + "; doNotCompress=" + m_bDoNotCompress);
    m_nContentLength = nLength;
    if (m_bDoNotCompress && nLength >= 0)
      ResponseHelper.setContentLength (m_aHttpResponse, m_nContentLength);
  }

  @Nonnull
  protected abstract DeflaterOutputStream createDeflaterOutputStream (@Nonnull OutputStream aOS) throws IOException;

  public final void doCompress (@Nullable final String sDebugInfo) throws IOException
  {
    if (m_aCompressedOS == null)
    {
      if (m_aHttpResponse.isCommitted ())
        throw new IllegalStateException ("Response already committed");

      m_aHttpResponse.setHeader (CHTTPHeader.CONTENT_ENCODING, m_sContentEncoding);

      // Check if header was really set (may e.g. not be the case when something
      // is included like a JSP)
      if (m_aHttpResponse.containsHeader (CHTTPHeader.CONTENT_ENCODING))
      {
        _debugLog (true, sDebugInfo);

        m_aCompressedOS = createDeflaterOutputStream (m_aHttpResponse.getOutputStream ());
        m_aOS = m_aCompressedOS;
        if (m_aBAOS != null)
        {
          // Copy cached content to new OS
          m_aBAOS.writeTo (m_aOS);
          m_aBAOS = null;
        }
      }
      else
        doNotCompress ("from compress: included request");
    }
    else
    {
      if (CompressFilterSettings.isDebugModeEnabled ())
        s_aLogger.info ("doCompress on already compressed stream");
    }
  }

  public final void doNotCompress (final String sDebugInfo) throws IOException
  {
    if (m_aCompressedOS != null)
      throw new IllegalStateException ("Compressed output stream is already assigned.");

    if (m_aOS == null || m_aBAOS != null)
    {
      m_bDoNotCompress = true;
      _debugLog (false, sDebugInfo);

      m_aOS = m_aHttpResponse.getOutputStream ();
      setContentLength (m_nContentLength);
      if (m_aBAOS != null)
      {
        // Copy all cached content
        m_aBAOS.writeTo (m_aOS);
        m_aBAOS = null;
      }
    }
  }

  @Override
  public final void flush () throws IOException
  {
    if (m_aOS == null || m_aBAOS != null)
    {
      if (m_nContentLength > 0 && m_nContentLength < m_nMinCompressSize)
        doNotCompress ("flush");
      else
        doCompress ("flush");
    }

    m_aOS.flush ();
  }

  @Override
  public final void close () throws IOException
  {
    if (!m_bClosed)
    {
      final Object aIncluded = m_aHttpRequest.getAttribute ("javax.servlet.include.request_uri");
      if (aIncluded != null)
      {
        if (CompressFilterSettings.isDebugModeEnabled ())
          s_aLogger.info ("No close because we're including " + aIncluded);
        flush ();
      }
      else
      {
        if (m_aBAOS != null)
        {
          if (m_nContentLength < 0)
            m_nContentLength = m_aBAOS.getSize ();
          if (m_nContentLength < m_nMinCompressSize)
            doNotCompress ("close with buffer");
          else
            doCompress ("close with buffer");
        }
        else
          if (m_aOS == null)
            doNotCompress ("close without buffer");

        if (CompressFilterSettings.isDebugModeEnabled ())
          s_aLogger.info ("Closing stream. compressed=" + (m_aCompressedOS != null));
        if (m_aCompressedOS != null)
          m_aCompressedOS.close ();
        else
          m_aOS.close ();
        m_bClosed = true;
      }
    }
  }

  public final boolean isClosed ()
  {
    return m_bClosed;
  }

  public final void finishAndClose () throws IOException
  {
    if (!m_bClosed)
    {
      if (m_aOS == null || m_aBAOS != null)
      {
        if (m_nContentLength > 0 && m_nContentLength < m_nMinCompressSize)
          doNotCompress ("finish");
        else
          doCompress ("finish");
      }

      if (m_aCompressedOS != null && !m_bClosed)
      {
        if (CompressFilterSettings.isDebugModeEnabled ())
          s_aLogger.info ("Closing compressed stream in finish!");
        m_bClosed = true;
        m_aCompressedOS.close ();
      }
      else
      {
        if (CompressFilterSettings.isDebugModeEnabled ())
          s_aLogger.info ("Not closing anything in finish!");
      }
    }
  }

  private void _prepareToWrite (@Nonnegative final int nLength) throws IOException
  {
    if (m_bClosed)
      throw new IOException ("Already closed");

    if (m_aOS == null)
    {
      if (m_aHttpResponse.isCommitted ())
        doNotCompress ("_prepareToWrite new - response already committed");
      else
        if (m_nContentLength >= 0 && m_nContentLength < m_nMinCompressSize)
          doNotCompress ("_prepareToWrite new " + m_nContentLength);
        else
          if (nLength > m_nMinCompressSize)
            doCompress ("_prepareToWrite new " + nLength);
          else
          {
            if (CompressFilterSettings.isDebugModeEnabled ())
              s_aLogger.info ("Starting new output buffering!");
            m_aBAOS = new NonBlockingByteArrayOutputStream (DEFAULT_BUFSIZE);
            m_aOS = m_aBAOS;
          }
    }
    else
      if (m_aBAOS != null)
      {
        if (m_aHttpResponse.isCommitted ())
          doNotCompress ("_prepareToWrite buffered - response already committed");
        else
          if (m_nContentLength >= 0 && m_nContentLength < m_nMinCompressSize)
            doNotCompress ("_prepareToWrite buffered " + m_nContentLength);
          else
            if (nLength >= (m_aBAOS.getBufferSize () - m_aBAOS.getSize ()))
              doCompress ("_prepareToWrite buffered " + nLength);
            else
            {
              if (CompressFilterSettings.isDebugModeEnabled ())
                s_aLogger.info ("Continue buffering!");
            }
      }
    // Else a regular non-buffered OS is present (m_aOS != null)
  }

  @Override
  public final void write (final int nByte) throws IOException
  {
    _prepareToWrite (1);
    m_aOS.write ((byte) nByte);
  }

  @Override
  public final void write (@Nonnull final byte [] aBytes) throws IOException
  {
    write (aBytes, 0, aBytes.length);
  }

  @Override
  public final void write (@Nonnull final byte [] aBytes,
                           @Nonnegative final int nOfs,
                           @Nonnegative final int nLen) throws IOException
  {
    _prepareToWrite (nLen);
    m_aOS.write (aBytes, nOfs, nLen);
  }

  @Nullable
  public final OutputStream getOutputStream ()
  {
    return m_aOS;
  }
}
