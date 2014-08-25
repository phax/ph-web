/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
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
package com.helger.web.servlet.response.gzip;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotations.Nonempty;
import com.helger.commons.string.StringHelper;
import com.helger.web.http.AcceptEncodingHandler;
import com.helger.web.http.CHTTPHeader;
import com.helger.web.servlet.response.ResponseHelper;
import com.helger.web.servlet.response.StatusAwareHttpResponseWrapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstract output stream switching
 * {@link javax.servlet.http.HttpServletResponseWrapper}
 * 
 * @author Philip Helger
 */
@NotThreadSafe
public abstract class AbstractCompressedResponseWrapper extends StatusAwareHttpResponseWrapper
{
  /** The minimum size where compression is applied */
  public static final int DEFAULT_MIN_COMPRESSED_SIZE = 256;
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractCompressedResponseWrapper.class);

  private final HttpServletRequest m_aHttpRequest;
  private final String m_sContentEncoding;
  private long m_nContentLength = -1;
  private final int m_nMinCompressSize = DEFAULT_MIN_COMPRESSED_SIZE;
  private AbstractCompressedServletOutputStream m_aCompressedOS;
  private PrintWriter m_aWriter;
  private boolean m_bNoCompression = false;

  public AbstractCompressedResponseWrapper (@Nonnull final HttpServletRequest aHttpRequest,
                                            @Nonnull final HttpServletResponse aHttpResponse,
                                            @Nonnull @Nonempty final String sContentEncoding)
  {
    super (aHttpResponse);
    m_aHttpRequest = ValueEnforcer.notNull (aHttpRequest, "HttpRequest");
    m_sContentEncoding = ValueEnforcer.notEmpty (sContentEncoding, "ContentEncoding");
  }

  public void setNoCompression ()
  {
    m_bNoCompression = true;
    if (m_aCompressedOS != null)
      try
      {
        m_aCompressedOS.doNotCompress ("requested from response wrapper");
      }
      catch (final IOException e)
      {
        throw new IllegalStateException (e);
      }
  }

  private void _setContentLength (final long nLength)
  {
    if (CompressFilterSettings.isDebugModeEnabled ())
      s_aLogger.info ("Explicitly setting content length " + nLength + "; m_bNoCompression=" + m_bNoCompression);

    m_nContentLength = nLength;
    if (m_aCompressedOS != null)
      m_aCompressedOS.setContentLength (nLength);
    else
      if (m_bNoCompression && m_nContentLength >= 0)
        ResponseHelper.setContentLength ((HttpServletResponse) getResponse (), m_nContentLength);
  }

  @Override
  public void setContentType (@Nonnull final String sContentType)
  {
    super.setContentType (sContentType);

    // Is not output stream present yet?
    if (m_aCompressedOS == null || m_aCompressedOS.getOutputStream () == null)
    {
      // Extract the content type without the charset
      String sRealContentType = StringHelper.getUntilFirstExcl (sContentType, ';');
      if (sRealContentType == null)
        sRealContentType = sContentType;

      if (sRealContentType != null &&
          (sRealContentType.contains (AcceptEncodingHandler.DEFLATE_ENCODING) || sRealContentType.contains (AcceptEncodingHandler.GZIP_ENCODING)))
      {
        if (CompressFilterSettings.isDebugModeEnabled ())
          s_aLogger.info ("Explicitly disabling compression because of external content type " + sContentType);

        // Deflate or Gzip was manually set
        setNoCompression ();
      }
    }
  }

  private void _updateStatus (final int nStatusCode)
  {
    // sc<200 || sc==204 || sc==205 || sc>=300
    if (nStatusCode < SC_OK ||
        nStatusCode == SC_NO_CONTENT ||
        nStatusCode == SC_RESET_CONTENT ||
        nStatusCode >= SC_MULTIPLE_CHOICES)
    {
      setNoCompression ();
    }
  }

  @Override
  public void setStatus (final int sc)
  {
    super.setStatus (sc);
    _updateStatus (sc);
  }

  @Override
  @Deprecated
  public void setStatus (final int sc, final String sm)
  {
    super.setStatus (sc, sm);
    _updateStatus (sc);
  }

  @Override
  public void addHeader (final String sHeaderName, final String sHeaderValue)
  {
    if (CHTTPHeader.CONTENT_LENGTH.equalsIgnoreCase (sHeaderName))
    {
      _setContentLength (Long.parseLong (sHeaderValue));
    }
    else
      if (CHTTPHeader.CONTENT_TYPE.equalsIgnoreCase (sHeaderName))
      {
        setContentType (sHeaderValue);
      }
      else
        if (CHTTPHeader.CONTENT_ENCODING.equalsIgnoreCase (sHeaderName))
        {
          if (CompressFilterSettings.isDebugModeEnabled ())
            s_aLogger.info ("Explicitly content encoding in addHeader: " + sHeaderValue);

          super.addHeader (sHeaderName, sHeaderValue);
          if (!isCommitted ())
            setNoCompression ();
        }
        else
          super.addHeader (sHeaderName, sHeaderValue);
  }

  @Override
  public void setHeader (final String sHeaderName, final String sHeaderValue)
  {
    if (CHTTPHeader.CONTENT_LENGTH.equalsIgnoreCase (sHeaderName))
    {
      _setContentLength (Long.parseLong (sHeaderValue));
    }
    else
      if (CHTTPHeader.CONTENT_TYPE.equalsIgnoreCase (sHeaderName))
      {
        setContentType (sHeaderValue);
      }
      else
        if (CHTTPHeader.CONTENT_ENCODING.equalsIgnoreCase (sHeaderName))
        {
          if (CompressFilterSettings.isDebugModeEnabled ())
            s_aLogger.info ("Explicitly content encoding in setHeader: " + sHeaderValue);

          super.setHeader (sHeaderName, sHeaderValue);
          if (!isCommitted ())
            setNoCompression ();
        }
        else
          super.setHeader (sHeaderName, sHeaderValue);
  }

  @Override
  public void setIntHeader (final String sHeaderName, final int nHeaderValue)
  {
    if (CHTTPHeader.CONTENT_LENGTH.equalsIgnoreCase (sHeaderName))
    {
      setContentLength (nHeaderValue);
    }
    else
      super.setIntHeader (sHeaderName, nHeaderValue);
  }

  @Override
  public final void flushBuffer () throws IOException
  {
    if (m_aWriter != null)
    {
      if (CompressFilterSettings.isDebugModeEnabled ())
        s_aLogger.info ("flushBuffer on writer");
      m_aWriter.flush ();
    }
    else
      if (m_aCompressedOS != null)
      {
        if (CompressFilterSettings.isDebugModeEnabled ())
          s_aLogger.warn ("flushBuffer on compressedOS - FINISH and CLOSE!");
        m_aCompressedOS.finishAndClose ();
      }
      else
        getResponse ().flushBuffer ();
  }

  @Override
  public void reset ()
  {
    if (CompressFilterSettings.isDebugModeEnabled ())
      s_aLogger.info ("reset (e.g. because of conditional requests)");

    super.reset ();
    if (m_aCompressedOS != null)
    {
      m_aCompressedOS.resetBuffer ();
      m_aCompressedOS = null;
    }
    m_aWriter = null;
    m_bNoCompression = false;
    m_nContentLength = -1;
  }

  @Override
  public void resetBuffer ()
  {
    if (CompressFilterSettings.isDebugModeEnabled ())
      s_aLogger.info ("resetBuffer");

    super.resetBuffer ();
    if (m_aCompressedOS != null)
    {
      m_aCompressedOS.resetBuffer ();
      m_aCompressedOS = null;
    }
    m_aWriter = null;
  }

  @Override
  public void sendError (final int sc, final String msg) throws IOException
  {
    resetBuffer ();
    super.sendError (sc, msg);
  }

  @Override
  public void sendError (final int sc) throws IOException
  {
    resetBuffer ();
    super.sendError (sc);
  }

  @Override
  public void sendRedirect (final String sLocation) throws IOException
  {
    resetBuffer ();
    super.sendRedirect (sLocation);
  }

  public final void finish () throws IOException
  {
    if (m_aWriter != null && !m_aCompressedOS.isClosed ())
      m_aWriter.flush ();
    if (m_aCompressedOS != null)
      m_aCompressedOS.finishAndClose ();
  }

  @Nonnull
  protected abstract AbstractCompressedServletOutputStream createCompressedOutputStream (@Nonnull final HttpServletRequest aHttpRequest,
                                                                                         @Nonnull final HttpServletResponse aHttpResponse,
                                                                                         @Nonnull @Nonempty String sContentEncoding,
                                                                                         long nContentLength,
                                                                                         @Nonnegative int nMinCompressSize) throws IOException;

  @Nonnull
  private AbstractCompressedServletOutputStream _createCompressedOutputStream () throws IOException
  {
    if (CompressFilterSettings.isDebugModeEnabled ())
      s_aLogger.info ("createCompressedOutputStream(" +
                      m_sContentEncoding +
                      ", " +
                      m_nContentLength +
                      ", " +
                      m_nMinCompressSize +
                      ") on " +
                      m_aHttpRequest.getRequestURI ());

    return createCompressedOutputStream (m_aHttpRequest,
                                         (HttpServletResponse) getResponse (),
                                         m_sContentEncoding,
                                         m_nContentLength,
                                         m_nMinCompressSize);
  }

  @Override
  @Nonnull
  public final ServletOutputStream getOutputStream () throws IOException
  {
    if (m_aCompressedOS == null)
    {
      if (getResponse ().isCommitted () || m_bNoCompression)
        return getResponse ().getOutputStream ();

      m_aCompressedOS = _createCompressedOutputStream ();
    }
    else
      if (m_aWriter != null)
        throw new IllegalStateException ("getWriter() has already been called!");

    return m_aCompressedOS;
  }

  @SuppressFBWarnings ({ "DM_DEFAULT_ENCODING" })
  @Override
  @Nonnull
  public final PrintWriter getWriter () throws IOException
  {
    if (m_aWriter == null)
    {
      if (m_aCompressedOS != null)
        throw new IllegalStateException ("getOutputStream() has already been called!");

      if (getResponse ().isCommitted () || m_bNoCompression)
        return getResponse ().getWriter ();

      m_aCompressedOS = _createCompressedOutputStream ();
      final String sEncoding = getCharacterEncoding ();
      if (sEncoding == null)
        m_aWriter = new PrintWriter (m_aCompressedOS);
      else
        m_aWriter = new PrintWriter (new OutputStreamWriter (m_aCompressedOS, sEncoding));
    }
    return m_aWriter;
  }
}
