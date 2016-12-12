/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.concurrent.SimpleReadWriteLock;
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
  private static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  private static boolean s_bFilterLoaded = false;
  private static boolean s_bResponseCompressionEnabled = true;
  private static boolean s_bResponseGzipEnabled = true;
  private static boolean s_bResponseDeflateEnabled = true;
  private static boolean s_bDebugModeEnabled = false;

  @PresentForCodeCoverage
  private static final CompressFilterSettings s_aInstance = new CompressFilterSettings ();

  private CompressFilterSettings ()
  {}

  /**
   * Mark the filter as loaded.
   */
  public static void markFilterLoaded ()
  {
    s_aRWLock.writeLocked ( () -> {
      s_bFilterLoaded = true;
    });
    s_aLogger.info ("CompressFilter is loaded");
  }

  /**
   * @return <code>true</code> if the filter is loaded, <code>false</code> if
   *         not
   */
  public static boolean isFilterLoaded ()
  {
    return s_aRWLock.readLocked ( () -> s_bFilterLoaded);
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
    return s_aRWLock.writeLocked ( () -> {
      if (s_bResponseCompressionEnabled == bResponseCompressionEnabled)
        return EChange.UNCHANGED;
      s_bResponseCompressionEnabled = bResponseCompressionEnabled;
      s_aLogger.info ("CompressFilter responseCompressionEnabled=" + bResponseCompressionEnabled);
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> if overall compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseCompressionEnabled ()
  {
    return s_aRWLock.readLocked ( () -> s_bResponseCompressionEnabled);
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
    return s_aRWLock.writeLocked ( () -> {
      if (s_bResponseGzipEnabled == bResponseGzipEnabled)
        return EChange.UNCHANGED;
      s_bResponseGzipEnabled = bResponseGzipEnabled;
      s_aLogger.info ("CompressFilter responseGzipEnabled=" + bResponseGzipEnabled);
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> if GZip compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseGzipEnabled ()
  {
    return s_aRWLock.readLocked ( () -> s_bResponseGzipEnabled);
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
    return s_aRWLock.writeLocked ( () -> {
      if (s_bResponseDeflateEnabled == bResponseDeflateEnabled)
        return EChange.UNCHANGED;
      s_bResponseDeflateEnabled = bResponseDeflateEnabled;
      s_aLogger.info ("CompressFilter responseDeflateEnabled=" + bResponseDeflateEnabled);
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> if Deflate compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseDeflateEnabled ()
  {
    return s_aRWLock.readLocked ( () -> s_bResponseDeflateEnabled);
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
    return s_aRWLock.writeLocked ( () -> {
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
    });
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
    return s_aRWLock.writeLocked ( () -> {
      if (s_bDebugModeEnabled == bDebugModeEnabled)
        return EChange.UNCHANGED;
      s_bDebugModeEnabled = bDebugModeEnabled;
      s_aLogger.info ("CompressFilter debugMode=" + bDebugModeEnabled);
      return EChange.CHANGED;
    });
  }

  /**
   * @return <code>true</code> if debugMode is enabled, <code>false</code> if
   *         not
   */
  public static boolean isDebugModeEnabled ()
  {
    return s_aRWLock.readLocked ( () -> s_bDebugModeEnabled);
  }
}
