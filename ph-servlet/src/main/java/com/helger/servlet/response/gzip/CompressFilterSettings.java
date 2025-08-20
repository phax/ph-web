/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.GuardedBy;
import com.helger.annotation.concurrent.ThreadSafe;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.base.concurrent.SimpleReadWriteLock;
import com.helger.base.state.EChange;

import jakarta.annotation.Nonnull;

/**
 * Contains the settings for the CompressFilter class.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class CompressFilterSettings
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CompressFilterSettings.class);
  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static boolean s_bFilterLoaded = false;
  @GuardedBy ("RW_LOCK")
  private static boolean s_bResponseCompressionEnabled = true;
  @GuardedBy ("RW_LOCK")
  private static boolean s_bResponseGzipEnabled = true;
  @GuardedBy ("RW_LOCK")
  private static boolean s_bResponseDeflateEnabled = true;
  @GuardedBy ("RW_LOCK")
  private static boolean s_bDebugModeEnabled = false;

  @PresentForCodeCoverage
  private static final CompressFilterSettings INSTANCE = new CompressFilterSettings ();

  private CompressFilterSettings ()
  {}

  /**
   * @return <code>true</code> if the filter is loaded, <code>false</code> if
   *         not
   */
  public static boolean isFilterLoaded ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bFilterLoaded);
  }

  /**
   * Mark the filter as loaded.
   */
  public static void markFilterLoaded ()
  {
    RW_LOCK.writeLockedBoolean ( () -> s_bFilterLoaded = true);
    LOGGER.info ("CompressFilter is loaded");
  }

  /**
   * @return <code>true</code> if overall compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseCompressionEnabled ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bResponseCompressionEnabled);
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
    final EChange ret = RW_LOCK.writeLockedGet ( () -> {
      if (s_bResponseCompressionEnabled == bResponseCompressionEnabled)
        return EChange.UNCHANGED;
      s_bResponseCompressionEnabled = bResponseCompressionEnabled;
      return EChange.CHANGED;
    });
    if (ret.isChanged ())
      LOGGER.info ("CompressFilter responseCompressionEnabled=" + bResponseCompressionEnabled);
    return ret;
  }

  /**
   * @return <code>true</code> if GZip compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseGzipEnabled ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bResponseGzipEnabled);
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
    final EChange ret = RW_LOCK.writeLockedGet ( () -> {
      if (s_bResponseGzipEnabled == bResponseGzipEnabled)
        return EChange.UNCHANGED;
      s_bResponseGzipEnabled = bResponseGzipEnabled;
      return EChange.CHANGED;
    });
    if (ret.isChanged ())
      LOGGER.info ("CompressFilter responseGzipEnabled=" + bResponseGzipEnabled);
    return ret;
  }

  /**
   * @return <code>true</code> if Deflate compression is enabled,
   *         <code>false</code> if not
   */
  public static boolean isResponseDeflateEnabled ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bResponseDeflateEnabled);
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
    final EChange ret = RW_LOCK.writeLockedGet ( () -> {
      if (s_bResponseDeflateEnabled == bResponseDeflateEnabled)
        return EChange.UNCHANGED;
      s_bResponseDeflateEnabled = bResponseDeflateEnabled;
      return EChange.CHANGED;
    });
    if (ret.isChanged ())
      LOGGER.info ("CompressFilter responseDeflateEnabled=" + bResponseDeflateEnabled);
    return ret;
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
    return setResponseCompressionEnabled (bResponseCompressionEnabled).or (setResponseGzipEnabled (bResponseGzipEnabled))
                                                                      .or (setResponseDeflateEnabled (bResponseDeflateEnabled));
  }

  /**
   * @return <code>true</code> if debugMode is enabled, <code>false</code> if
   *         not
   */
  public static boolean isDebugModeEnabled ()
  {
    return RW_LOCK.readLockedBoolean ( () -> s_bDebugModeEnabled);
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
    final EChange ret = RW_LOCK.writeLockedGet ( () -> {
      if (s_bDebugModeEnabled == bDebugModeEnabled)
        return EChange.UNCHANGED;
      s_bDebugModeEnabled = bDebugModeEnabled;
      return EChange.CHANGED;
    });
    if (ret.isChanged ())
      LOGGER.info ("CompressFilter debugMode=" + bDebugModeEnabled);
    return ret;
  }
}
