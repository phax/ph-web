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
package com.helger.servlet.response;

import java.time.LocalDateTime;
import java.time.Month;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.datetime.PDTWebDateHelper;
import com.helger.commons.state.EChange;

/**
 * Contains the settings for the {@link ResponseHelper} class.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class ResponseHelperSettings
{
  /** A special date that never expires */
  public static final LocalDateTime EXPIRES_NEVER_DATETIME = LocalDateTime.of (1995, Month.MAY, 6, 12, 0, 0);

  /** The string representation of never expires date */
  public static final String EXPIRES_NEVER_STRING = PDTWebDateHelper.getAsStringRFC822 (EXPIRES_NEVER_DATETIME);

  /**
   * Expires in at least 2 days (which is the minimum to be accepted for real
   * caching in Yahoo Guidelines).
   */
  public static final int DEFAULT_EXPIRATION_SECONDS = 7 * CGlobal.SECONDS_PER_DAY;

  public static final boolean DEFAULT_RESPONSE_COMPRESSION_ENABLED = true;
  public static final boolean DEFAULT_RESPONSE_GZIP_ENABLED = true;
  public static final boolean DEFAULT_RESPONSE_DERFLATE_ENABLED = true;

  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseHelperSettings.class);

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  private static int s_nExpirationSeconds = DEFAULT_EXPIRATION_SECONDS;
  private static boolean s_bResponseCompressionEnabled = DEFAULT_RESPONSE_COMPRESSION_ENABLED;
  private static boolean s_bResponseGzipEnabled = DEFAULT_RESPONSE_GZIP_ENABLED;
  private static boolean s_bResponseDeflateEnabled = DEFAULT_RESPONSE_DERFLATE_ENABLED;

  @PresentForCodeCoverage
  private static final ResponseHelperSettings INSTANCE = new ResponseHelperSettings ();

  private ResponseHelperSettings ()
  {}

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
      LOGGER.info ("ResponseHelper responseCompressionEnabled=" + bResponseCompressionEnabled);
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
      LOGGER.info ("ResponseHelper responseGzipEnabled=" + bResponseGzipEnabled);
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
      LOGGER.info ("ResponseHelper responseDeflateEnabled=" + bResponseDeflateEnabled);
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
   * @return The default expiration seconds for objects to be cached
   */
  public static int getExpirationSeconds ()
  {
    return RW_LOCK.readLockedInt ( () -> s_nExpirationSeconds);
  }

  /**
   * Set the default expiration settings to be used for objects that should use
   * HTTP caching
   *
   * @param nExpirationSeconds
   *        The number of seconds for which the response should be cached
   * @return {@link EChange}
   */
  @Nonnull
  public static EChange setExpirationSeconds (final int nExpirationSeconds)
  {
    final EChange ret = RW_LOCK.writeLockedGet ( () -> {
      if (s_nExpirationSeconds == nExpirationSeconds)
        return EChange.UNCHANGED;
      s_nExpirationSeconds = nExpirationSeconds;
      return EChange.CHANGED;
    });
    if (ret.isChanged ())
      LOGGER.info ("ResponseHelper expirationSeconds=" + nExpirationSeconds);
    return ret;
  }
}
