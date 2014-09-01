/**
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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.state.EChange;

/**
 * Contains the settings for the CompressFilter class.
 * 
 * @author Philip Helger
 */
@ThreadSafe
public final class CompressFilterSettings
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (CompressFilterSettings.class);
  private static final ReadWriteLock s_aRWLock = new ReentrantReadWriteLock ();
  private static boolean s_bFilterLoaded = false;
  private static boolean s_bResponseCompressionEnabled = true;
  private static boolean s_bResponseGzipEnabled = true;
  private static boolean s_bResponseDeflateEnabled = true;
  private static boolean s_bDebugModeEnabled = false;

  @PresentForCodeCoverage
  @SuppressWarnings ("unused")
  private static final CompressFilterSettings s_aInstance = new CompressFilterSettings ();

  private CompressFilterSettings ()
  {}

  /**
   * Mark the filter as loaded.
   */
  public static void markFilterLoaded ()
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      s_bFilterLoaded = true;
      s_aLogger.info ("CompressFilter is loaded");
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> if the filter is loaded, <code>false</code> if
   *         not
   */
  public static boolean isFilterLoaded ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bFilterLoaded;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Enable or disable the overall compression.
   * 
   * @param bResponseCompressionEnabled
   *        <code>true</code> to enable it, <code>false</code> to disable it
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setResponseCompressionEnabled (final boolean bResponseCompressionEnabled)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_bResponseCompressionEnabled == bResponseCompressionEnabled)
        return EChange.UNCHANGED;
      s_bResponseCompressionEnabled = bResponseCompressionEnabled;
      s_aLogger.info ("CompressFilter responseCompressionEnabled=" + bResponseCompressionEnabled);
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> if overall compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseCompressionEnabled ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bResponseCompressionEnabled;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Enable or disable Gzip compression. This only has an effect if
   * {@link #isResponseCompressionEnabled()} is <code>true</code>
   * 
   * @param bResponseGzipEnabled
   *        <code>true</code> to enable it, <code>false</code> to disable it
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setResponseGzipEnabled (final boolean bResponseGzipEnabled)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_bResponseGzipEnabled == bResponseGzipEnabled)
        return EChange.UNCHANGED;
      s_bResponseGzipEnabled = bResponseGzipEnabled;
      s_aLogger.info ("CompressFilter responseGzipEnabled=" + bResponseGzipEnabled);
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> if GZip compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseGzipEnabled ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bResponseGzipEnabled;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Enable or disable Deflate compression. This only has an effect if
   * {@link #isResponseCompressionEnabled()} is <code>true</code>
   * 
   * @param bResponseDeflateEnabled
   *        <code>true</code> to enable it, <code>false</code> to disable it
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setResponseDeflateEnabled (final boolean bResponseDeflateEnabled)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_bResponseDeflateEnabled == bResponseDeflateEnabled)
        return EChange.UNCHANGED;
      s_bResponseDeflateEnabled = bResponseDeflateEnabled;
      s_aLogger.info ("CompressFilter responseDeflateEnabled=" + bResponseDeflateEnabled);
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> if Deflate compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseDeflateEnabled ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bResponseDeflateEnabled;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Set all parameters at once as an atomic transaction
   * 
   * @param bResponseCompressionEnabled
   *        <code>true</code> to overall enable the usage
   * @param bResponseGzipEnabled
   *        <code>true</code> to enable GZip if compression is enabled
   * @param bResponseDeflateEnabled
   *        <code>true</code> to enable Deflate if compression is enabled
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setAll (final boolean bResponseCompressionEnabled,
                                final boolean bResponseGzipEnabled,
                                final boolean bResponseDeflateEnabled)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      EChange eChange = EChange.UNCHANGED;
      if (s_bResponseCompressionEnabled != bResponseCompressionEnabled)
      {
        s_bResponseCompressionEnabled = bResponseCompressionEnabled;
        eChange = EChange.CHANGED;
        s_aLogger.info ("CompressFilter responseCompressionEnabled=" + bResponseCompressionEnabled);
      }
      if (s_bResponseGzipEnabled != bResponseGzipEnabled)
      {
        s_bResponseGzipEnabled = bResponseGzipEnabled;
        eChange = EChange.CHANGED;
        s_aLogger.info ("CompressFilter responseGzipEnabled=" + bResponseGzipEnabled);
      }
      if (s_bResponseDeflateEnabled != bResponseDeflateEnabled)
      {
        s_bResponseDeflateEnabled = bResponseDeflateEnabled;
        eChange = EChange.CHANGED;
        s_aLogger.info ("CompressFilter responseDeflateEnabled=" + bResponseDeflateEnabled);
      }
      return eChange;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Enable or disable debug mode
   * 
   * @param bDebugModeEnabled
   *        <code>true</code> to enable it, <code>false</code> to disable it
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setDebugModeEnabled (final boolean bDebugModeEnabled)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      if (s_bDebugModeEnabled == bDebugModeEnabled)
        return EChange.UNCHANGED;
      s_bDebugModeEnabled = bDebugModeEnabled;
      s_aLogger.info ("CompressFilter debugMode=" + bDebugModeEnabled);
      return EChange.CHANGED;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * @return <code>true</code> if debugMode is enabled, <code>false</code> if
   *         not
   */
  public static boolean isDebugModeEnabled ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_bDebugModeEnabled;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }
}
