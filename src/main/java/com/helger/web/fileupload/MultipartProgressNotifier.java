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

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;

/**
 * Internal class, which is used to invoke the {@link IProgressListener}.
 */
public final class MultipartProgressNotifier
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (MultipartProgressNotifier.class);
  /**
   * The listener to invoke.
   */
  private final IProgressListener m_aListener;
  /**
   * Number of expected bytes, if known, or -1.
   */
  private final long m_nContentLength;
  /**
   * Number of bytes, which have been read so far.
   */
  private long m_nBytesRead;
  /**
   * Number of items, which have been read so far.
   */
  private int m_nItems;

  /**
   * Creates a new instance with the given listener and content length.
   *
   * @param aListener
   *        The listener to invoke.
   * @param nContentLength
   *        The expected content length.
   */
  MultipartProgressNotifier (@Nullable final IProgressListener aListener, @CheckForSigned final long nContentLength)
  {
    if (aListener != null && s_aLogger.isDebugEnabled ())
      s_aLogger.debug ("setting progress listener " + aListener);
    m_aListener = aListener;
    m_nContentLength = nContentLength;
  }

  /**
   * Called for notifying the listener.
   */
  private void _notifyListener ()
  {
    if (m_aListener != null)
      m_aListener.update (m_nBytesRead, m_nContentLength, m_nItems);
  }

  /**
   * Called to indicate that bytes have been read.
   *
   * @param nBytes
   *        Number of bytes, which have been read.
   */
  void noteBytesRead (@Nonnegative final int nBytes)
  {
    ValueEnforcer.isGE0 (nBytes, "Bytes");
    /*
     * Indicates, that the given number of bytes have been read from the input
     * stream.
     */
    m_nBytesRead += nBytes;
    _notifyListener ();
  }

  /**
   * Called to indicate, that a new file item has been detected.
   */
  void onNextFileItem ()
  {
    ++m_nItems;
    _notifyListener ();
  }
}
