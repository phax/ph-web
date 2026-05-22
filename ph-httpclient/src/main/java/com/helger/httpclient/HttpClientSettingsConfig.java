/*
 * Copyright (C) 2016-2026 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.function.Function;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.CheckForSigned;
import com.helger.annotation.Nonempty;
import com.helger.base.array.ArrayHelper;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsLinkedHashSet;
import com.helger.collection.commons.ICommonsOrderedSet;
import com.helger.config.IConfig;
import com.helger.config.fallback.IConfigWithFallback;
import com.helger.security.revocation.ERevocationCheckMode;
import com.helger.typeconvert.impl.TypeConverter;

/**
 * A helper class to configure {@link HttpClientSettings} using {@link IConfig} with standardized
 * configuration property names.
 *
 * @author Philip Helger
 * @since 10.0.0
 */
public class HttpClientSettingsConfig
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HttpClientSettingsConfig.class);

  public static final class HttpClientConfig
  {
    private final IConfigWithFallback m_aConfig;
    private final ICommonsOrderedSet <String> m_aConfigPrefixes;

    /**
     * Constructor.
     *
     * @param aConfig
     *        The {@link IConfigWithFallback} object to be used as the source of the values. May not
     *        be <code>null</code>.
     * @param aConfigPrefixes
     *        The configuration prefixes to be used in order. The prefixes are tried in iteration
     *        order until a matching configuration value is found. May neither be <code>null</code>
     *        nor empty.
     */
    public HttpClientConfig (@NonNull final IConfigWithFallback aConfig,
                             @NonNull @Nonempty final ICommonsOrderedSet <String> aConfigPrefixes)
    {
      ValueEnforcer.notNull (aConfig, "Config");
      ValueEnforcer.notEmptyNoNullValue (aConfigPrefixes, "Prefixes");
      m_aConfig = aConfig;
      m_aConfigPrefixes = aConfigPrefixes;
    }

    @NonNull
    private static String [] _copyAndMap (@NonNull final String [] aArray,
                                          @NonNull final Function <String, String> aFun)
    {
      final String [] ret = new String [aArray.length];
      ArrayHelper.forEach (aArray, (val, idx) -> ret[idx] = aFun.apply (val));
      return ret;
    }

    @Nullable
    private String _findString (@NonNull final String sLocalKey, @Nullable final String... aLocalSubKeys)
    {
      for (final String sConfigPrefix : m_aConfigPrefixes)
      {
        final String ret;
        if (aLocalSubKeys.length == 0)
          ret = m_aConfig.getAsString (sConfigPrefix + sLocalKey);
        else
        {
          // Add configPrefix to all values
          final String [] aRealSubKeys = _copyAndMap (aLocalSubKeys, x -> sConfigPrefix + x);
          ret = m_aConfig.getAsStringOrFallback (sConfigPrefix + sLocalKey, aRealSubKeys);
        }
        if (ret != null)
          return ret;
      }

      return null;
    }

    @Nullable
    private char [] _findCharArray (@NonNull final String sLocalKey, @Nullable final String... aLocalSubKeys)
    {
      for (final String sConfigPrefix : m_aConfigPrefixes)
      {
        final char [] ret;
        if (aLocalSubKeys.length == 0)
          ret = m_aConfig.getAsCharArray (sConfigPrefix + sLocalKey);
        else
        {
          // Add configPrefix to all values
          final String [] aRealSubKeys = _copyAndMap (aLocalSubKeys, x -> sConfigPrefix + x);
          ret = m_aConfig.getAsCharArrayOrFallback (sConfigPrefix + sLocalKey, aRealSubKeys);
        }
        if (ret != null)
          return ret;
      }

      return null;
    }

    @NonNull
    private ETriState _findBoolean (@NonNull final String sLocalKey,
                                    final boolean bDefault,
                                    @Nullable final String... aLocalSubKeys)
    {
      final String ret = _findString (sLocalKey, aLocalSubKeys);
      if (ret == null)
        return ETriState.UNDEFINED;

      return ETriState.valueOf (TypeConverter.convertToBoolean (ret, bDefault));
    }

    @CheckForSigned
    private int _findInt (@NonNull final String sLocalKey, final int nDefault, @Nullable final String... aLocalSubKeys)
    {
      for (final String sConfigPrefix : m_aConfigPrefixes)
      {
        final int ret;
        if (aLocalSubKeys.length == 0)
          ret = m_aConfig.getAsInt (sConfigPrefix + sLocalKey, nDefault);
        else
        {
          // Add configPrefix to all values
          final String [] aRealSubKeys = _copyAndMap (aLocalSubKeys, x -> sConfigPrefix + x);
          ret = m_aConfig.getAsIntOrFallback (sConfigPrefix + sLocalKey, nDefault, aRealSubKeys);
        }
        if (ret != nDefault)
          return ret;
      }
      return nDefault;
    }

    @CheckForSigned
    private long _findSingleLong (@NonNull final String sConfigPrefix,
                                  @NonNull final String sLocalKey,
                                  final long nDefault,
                                  @Nullable final String... aLocalSubKeys)
    {
      if (aLocalSubKeys.length == 0)
        return m_aConfig.getAsLong (sConfigPrefix + sLocalKey, nDefault);

      // Add configPrefix to all values
      final String [] aRealSubKeys = _copyAndMap (aLocalSubKeys, x -> sConfigPrefix + x);
      return m_aConfig.getAsLongOrFallback (sConfigPrefix + sLocalKey, nDefault, aRealSubKeys);
    }

    @CheckForSigned
    private long _findLong (@NonNull final String sLocalKey,
                            final long nDefault,
                            @Nullable final String... aLocalSubKeys)
    {
      for (final String sConfigPrefix : m_aConfigPrefixes)
      {
        final long ret = _findSingleLong (sConfigPrefix, sLocalKey, nDefault, aLocalSubKeys);
        if (ret != nDefault)
          return ret;
      }
      return nDefault;
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether to use the DNS client cache. Reads <code>http.dnsclientcache.use</code> (or
     *         the legacy alias <code>http.useDNSClientCache</code>). Never <code>null</code>; may
     *         be {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getUseDNSClientCache (final boolean bDefault)
    {
      return _findBoolean ("http.dnsclientcache.use", bDefault, "http.useDNSClientCache");
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether the HTTP proxy support is enabled. Reads <code>http.proxy.enabled</code>.
     *         Never <code>null</code>; may be {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getHttpProxyEnabled (final boolean bDefault)
    {
      return _findBoolean ("http.proxy.enabled", bDefault);
    }

    /**
     * @return The HttpProxy host to be used. May be <code>null</code>.
     */
    @Nullable
    public String getHttpProxyHost ()
    {
      return _findString ("http.proxy.host", "http.proxyHost", "http.proxy.address");
    }

    /**
     * @return The HTTP proxy port to be used, or <code>-1</code> in case it is not found.
     */
    @CheckForSigned
    public int getHttpProxyPort ()
    {
      return _findInt ("http.proxy.port", -1, "http.proxyPort");
    }

    /**
     * @return The HttpProxy object to be used. This is only non-<code>null</code> if proxy host is
     *         non-<code>null</code> and proxy port returns a value &gt; 0.
     * @see #getHttpProxyHost()
     * @see #getHttpProxyPort()
     */
    @Nullable
    public HttpHost getHttpProxyObject ()
    {
      final String sProxyHost = getHttpProxyHost ();
      final int nProxyPort = getHttpProxyPort ();
      if (sProxyHost != null && nProxyPort > 0)
        return new HttpHost (sProxyHost, nProxyPort);

      return null;
    }

    /**
     * @return The HttpProxy user name to be used. May be <code>null</code>.
     */
    @Nullable
    public String getHttpProxyUsername ()
    {
      return _findString ("http.proxy.username", "http.proxyUsername");
    }

    /**
     * @return The HttpProxy password to be used. May be <code>null</code>.
     */
    @Nullable
    public char [] getHttpProxyPasswordCharArray ()
    {
      return _findCharArray ("http.proxy.password", "http.proxyPassword");
    }

    /**
     * @return The {@link UsernamePasswordCredentials} object to be used for proxy server
     *         authentication or <code>null</code> if not username and password are configured.
     * @see #getHttpProxyUsername()
     * @see #getHttpProxyPasswordCharArray()
     */
    @Nullable
    public UsernamePasswordCredentials getHttpProxyCredentials ()
    {
      final String sProxyUsername = getHttpProxyUsername ();
      final char [] aProxyPassword = getHttpProxyPasswordCharArray ();
      if (sProxyUsername != null && aProxyPassword != null)
        return new UsernamePasswordCredentials (sProxyUsername, aProxyPassword);

      return null;
    }

    /**
     * @return A pipe separated list of non-proxy hosts. E.g. <code>localhost|127.0.0.1</code>. May
     *         be <code>null</code>.
     */
    @Nullable
    public String getNonProxyHosts ()
    {
      return _findString ("http.proxy.nonProxyHosts", "http.nonProxyHosts", "http.proxy.non-proxy");
    }

    /**
     * @return The configured retry count. Reads <code>http.retry.count</code>. A return value of
     *         <code>-1</code> means that the value is not configured (and the default should be
     *         kept).
     */
    @CheckForSigned
    public int getRetryCount ()
    {
      // Use -1 to indicate: don't use the configured value if it is crap
      return _findInt ("http.retry.count", -1);
    }

    @Nullable
    private Duration _findConfigDuration (@NonNull final String sFullKey)
    {
      return m_aConfig.getAsConfigDuration (sFullKey,
                                            sErr -> LOGGER.warn ("Invalid duration value for configuration key '" +
                                                                 sFullKey +
                                                                 "': " +
                                                                 sErr));
    }

    private static void _logLegacyDeprecation (@NonNull final String sFullKeyWithoutSuffix,
                                               @NonNull final String sSuffix)
    {
      LOGGER.warn ("Configuration key '" +
                   sFullKeyWithoutSuffix +
                   "." +
                   sSuffix +
                   "' uses the deprecated per-unit-suffix format. Please migrate to the unit-less form '" +
                   sFullKeyWithoutSuffix +
                   "' with values like '5ms', '21s', '34m' or '2h'. Per-unit-suffix keys will be removed in a future major version.");
    }

    @Nullable
    private Duration _findLegacyPerUnitDuration (@NonNull final String sConfigPrefix,
                                                 @NonNull final String sLocalKey,
                                                 @NonNull final String [] aLocalSubKeys)
    {
      final long nMillis = _findSingleLong (sConfigPrefix,
                                            sLocalKey + ".millis",
                                            -1,
                                            _copyAndMap (aLocalSubKeys, x -> x + ".millis"));
      if (nMillis > 0)
      {
        _logLegacyDeprecation (sConfigPrefix + sLocalKey, "millis");
        return Duration.ofMillis (nMillis);
      }

      final long nSeconds = _findSingleLong (sConfigPrefix,
                                             sLocalKey + ".seconds",
                                             -1,
                                             _copyAndMap (aLocalSubKeys, x -> x + ".seconds"));
      if (nSeconds > 0)
      {
        _logLegacyDeprecation (sConfigPrefix + sLocalKey, "seconds");
        return Duration.ofSeconds (nSeconds);
      }

      final long nMinutes = _findSingleLong (sConfigPrefix,
                                             sLocalKey + ".minutes",
                                             -1,
                                             _copyAndMap (aLocalSubKeys, x -> x + ".minutes"));
      if (nMinutes > 0)
      {
        _logLegacyDeprecation (sConfigPrefix + sLocalKey, "minutes");
        return Duration.ofMinutes (nMinutes);
      }

      final long nHours = _findSingleLong (sConfigPrefix,
                                           sLocalKey + ".hours",
                                           -1,
                                           _copyAndMap (aLocalSubKeys, x -> x + ".hours"));
      if (nHours > 0)
      {
        _logLegacyDeprecation (sConfigPrefix + sLocalKey, "hours");
        return Duration.ofHours (nHours);
      }
      return null;
    }

    @Nullable
    private Duration _findDuration (@NonNull final String sLocalKey, @Nullable final String... aLocalSubKeys)
    {
      for (final String sConfigPrefix : m_aConfigPrefixes)
      {
        // 1. New unit-less form via IConfig.getAsConfigDuration: primary key, then fallback aliases
        Duration aDur = _findConfigDuration (sConfigPrefix + sLocalKey);
        if (aDur != null)
          return aDur;

        for (final String sSubKey : aLocalSubKeys)
        {
          aDur = _findConfigDuration (sConfigPrefix + sSubKey);
          if (aDur != null)
            return aDur;
        }

        // 2. Legacy per-unit-suffix form (.millis / .seconds / .minutes / .hours) - deprecated
        aDur = _findLegacyPerUnitDuration (sConfigPrefix, sLocalKey, aLocalSubKeys);
        if (aDur != null)
          return aDur;
      }
      return null;
    }

    /**
     * @return The interval in which a retry should happen. Only relevant is retry count &gt; 0.
     * @see #getRetryCount()
     */
    @Nullable
    public Duration getRetryInterval ()
    {
      return _findDuration ("http.retry.interval");
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether retries should always be performed (also for non-idempotent requests). Reads
     *         <code>http.retry.always</code>. Never <code>null</code>; may be
     *         {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getRetryAlways (final boolean bDefault)
    {
      return _findBoolean ("http.retry.always", bDefault);
    }

    @Nullable
    private Timeout _findTimeout (@NonNull final String sPrefix, @Nullable final String... aLocalSubKeys)
    {
      final Duration aDuration = _findDuration (sPrefix, aLocalSubKeys);
      return aDuration == null ? null : Timeout.of (aDuration);
    }

    /**
     * @return The connection request timeout. Reads <code>http.timeout.connectionrequest</code>.
     *         May be <code>null</code> if not configured.
     */
    @Nullable
    public Timeout getConnectionRequestTimeout ()
    {
      return _findTimeout ("http.timeout.connectionrequest");
    }

    /**
     * @return The connect timeout. Reads <code>http.timeout.connect</code> (or the legacy alias
     *         <code>http.connection-timeout</code>). May be <code>null</code> if not configured.
     */
    @Nullable
    public Timeout getConnectTimeout ()
    {
      return _findTimeout ("http.timeout.connect", "http.connection-timeout");
    }

    /**
     * @return The response timeout. Reads <code>http.timeout.response</code> (or the legacy alias
     *         <code>http.read-timeout</code>). May be <code>null</code> if not configured.
     */
    @Nullable
    public Timeout getResponseTimeout ()
    {
      return _findTimeout ("http.timeout.response", "http.read-timeout");
    }

    /**
     * @return The HTTP <code>User-Agent</code> header value to be used. Reads
     *         <code>http.useragent</code>. May be <code>null</code> if not configured.
     */
    @Nullable
    public String getUserAgent ()
    {
      return _findString ("http.useragent");
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether HTTP redirects should be followed automatically. Reads
     *         <code>http.follow-redirects</code>. Never <code>null</code>; may be
     *         {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getFollowRedirects (final boolean bDefault)
    {
      return _findBoolean ("http.follow-redirects", bDefault);
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether HTTP keep-alive should be used. Reads <code>http.keep-alive</code>. Never
     *         <code>null</code>; may be {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getUseKeepAlive (final boolean bDefault)
    {
      return _findBoolean ("http.keep-alive", bDefault);
    }

    /**
     * Get the configuration value on protocol upgrade is enabled or not.
     *
     * @param bDefault
     *        The default value to be used.
     * @return Never <code>null</code>.
     * @since 10.5.0
     */
    @NonNull
    public ETriState getProtocolUpgradeEnabled (final boolean bDefault)
    {
      return _findBoolean ("http.protocol-upgrade.enabled", bDefault);
    }

    /**
     * @return The revocation check mode name from configuration. May be <code>null</code> if not
     *         configured.
     * @since 11.3.0
     */
    @Nullable
    public String getRevocationCheckMode ()
    {
      return _findString ("http.tls.revocation.mode");
    }

    /**
     * @param bDefault
     *        The default value to be used.
     * @return The revocation check soft-fail setting.
     * @since 11.3.0
     */
    @NonNull
    public ETriState getRevocationCheckSoftFail (final boolean bDefault)
    {
      return _findBoolean ("http.tls.revocation.soft-fail", bDefault);
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether all TLS checks (both hostname and certificate) should be disabled. Reads
     *         <code>http.tls.checks.disabled</code>. Disabling TLS checks is a security risk and
     *         should only be used for testing. Never <code>null</code>; may be
     *         {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getDisableTlsChecks (final boolean bDefault)
    {
      return _findBoolean ("http.tls.checks.disabled", bDefault);
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether the TLS hostname verification should be disabled. Reads
     *         <code>http.tls.hostname-check.disabled</code>. Disabling the hostname check is a
     *         security risk and should only be used for testing. Never <code>null</code>; may be
     *         {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getDisableHostnameCheck (final boolean bDefault)
    {
      return _findBoolean ("http.tls.hostname-check.disabled", bDefault);
    }

    /**
     * @param bDefault
     *        The default value to be used if the configuration value cannot be parsed as a boolean.
     * @return Whether the TLS certificate verification should be disabled. Reads
     *         <code>http.tls.certificate-check.disabled</code>. Disabling the certificate check is
     *         a security risk and should only be used for testing. Never <code>null</code>; may be
     *         {@link ETriState#UNDEFINED} if not configured.
     */
    @NonNull
    public ETriState getDisableCertificateCheck (final boolean bDefault)
    {
      return _findBoolean ("http.tls.certificate-check.disabled", bDefault);
    }

    /**
     * Factory method that creates a new {@link HttpClientConfig} for the provided prefixes. Each
     * prefix is normalized to end with a trailing dot (unless it is the empty string).
     *
     * @param aConfig
     *        The {@link IConfigWithFallback} object to be used as the source of the values. May not
     *        be <code>null</code>.
     * @param aPrefixes
     *        The configuration prefixes to be used in order. May neither be <code>null</code> nor
     *        empty.
     * @return The new {@link HttpClientConfig} object or <code>null</code> if no usable prefixes
     *         were provided.
     */
    @Nullable
    public static HttpClientConfig create (@NonNull final IConfigWithFallback aConfig,
                                           @NonNull @Nonempty final String... aPrefixes)
    {
      // Either empty or ending with a string
      final ICommonsOrderedSet <String> aRealPrefixes = new CommonsLinkedHashSet <> (aPrefixes,
                                                                                     x -> x.isEmpty () ||
                                                                                          x.endsWith (".") ? x : x +
                                                                                                                 ".");
      if (aRealPrefixes.isEmpty ())
      {
        LOGGER.warn ("No configuration prefixes provided to configure HTTP client settings. Nothing happens");
        return null;
      }

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Using prefixes '" + aRealPrefixes + "' to configure HTTP client settings");

      return new HttpClientConfig (aConfig, aRealPrefixes);
    }
  }

  private HttpClientSettingsConfig ()
  {}

  /**
   * Assign DNS related configuration values. The primary configuration parameters consumed are:
   * <ul>
   * <li><code>http.dnsclientcache.use</code> - whether to use the DNS client cache (boolean)</li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   */
  public static void assignConfigValuesForDNS (@NonNull final HttpClientSettings aHCS,
                                               @NonNull final HttpClientConfig aHCC)
  {
    // Use existing value as fallback to avoid changing to default
    final ETriState eUseDNSClientCache = aHCC.getUseDNSClientCache (aHCS.isUseDNSClientCache ());
    if (eUseDNSClientCache.isDefined ())
    {
      final boolean b = eUseDNSClientCache.getAsBooleanValue ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.useDNSClientCache(" + b + ")");
      aHCS.setUseDNSClientCache (b);
    }
  }

  /**
   * Assign proxy related configuration values. The presence of a non-empty
   * <code>http.proxy.host</code> together with a valid <code>http.proxy.port</code> is sufficient
   * to activate the proxy. The <code>http.proxy.enabled</code> property is only used as an
   * explicit kill-switch: if it resolves to <code>false</code>, no proxy settings are applied. If
   * <code>http.proxy.enabled</code> is not configured (undefined) or resolves to
   * <code>true</code>, the remaining proxy properties are evaluated. The primary configuration
   * parameters consumed are:
   * <ul>
   * <li><code>http.proxy.enabled</code> - explicit kill-switch; only checked for an explicit
   * <code>false</code> value (boolean)</li>
   * <li><code>http.proxy.host</code> - HTTP proxy host name</li>
   * <li><code>http.proxy.port</code> - HTTP proxy port</li>
   * <li><code>http.proxy.username</code> - HTTP proxy user name</li>
   * <li><code>http.proxy.password</code> - HTTP proxy password</li>
   * <li><code>http.proxy.nonProxyHosts</code> - pipe separated list of non-proxy hosts</li>
   * </ul>
   *
   * @param aProxySettings
   *        The {@link HttpProxySettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   */
  public static void assignConfigValuesForProxy (@NonNull final HttpProxySettings aProxySettings,
                                                 @NonNull final HttpClientConfig aHCC)
  {
    // The proxy is considered active unless "http.proxy.enabled" is explicitly set to false.
    // Having "http.proxy.host" and "http.proxy.port" alone is sufficient to enable it.
    final ETriState eProxyEnabled = aHCC.getHttpProxyEnabled (true);
    if (!eProxyEnabled.isDefined () || eProxyEnabled.getAsBooleanValue ())
    {
      final HttpHost aProxyHost = aHCC.getHttpProxyObject ();
      if (aProxyHost != null)
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Setting configured HttpClientSettings.proxyHost(" + aProxyHost + ")");
        aProxySettings.setProxyHost (aProxyHost);
      }

      final UsernamePasswordCredentials aProxyCredentials = aHCC.getHttpProxyCredentials ();
      if (aProxyCredentials != null)
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Setting configured HttpClientSettings.proxyCredentials(" + aProxyCredentials + ")");
        aProxySettings.setProxyCredentials (aProxyCredentials);
      }

      final String sNonProxyHosts = aHCC.getNonProxyHosts ();
      if (StringHelper.isNotEmpty (sNonProxyHosts))
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Setting configured HttpClientSettings.nonProxyHosts(" + sNonProxyHosts + ")");
        aProxySettings.setNonProxyHostsFromPipeString (sNonProxyHosts);
      }
    }
  }

  /**
   * Assign retry related configuration values. The primary configuration parameters consumed are:
   * <ul>
   * <li><code>http.retry.count</code> - number of retries (int)</li>
   * <li><code>http.retry.interval</code> - interval between retries (duration)</li>
   * <li><code>http.retry.always</code> - whether retries should always be performed (boolean)</li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   */
  public static void assignConfigValuesForRetry (@NonNull final HttpClientSettings aHCS,
                                                 @NonNull final HttpClientConfig aHCC)
  {
    final int nRetryCount = aHCC.getRetryCount ();
    if (nRetryCount >= 0)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.retryCount(" + nRetryCount + ")");
      aHCS.setRetryCount (nRetryCount);
    }

    final Duration aRetryInterval = aHCC.getRetryInterval ();
    if (aRetryInterval != null)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.retryInterval(" + aRetryInterval + ")");
      aHCS.setRetryInterval (aRetryInterval);
    }

    // Use existing value as fallback to avoid changing to default
    final ETriState eRetryAlways = aHCC.getRetryAlways (aHCS.isRetryAlways ());
    if (eRetryAlways.isDefined ())
    {
      final boolean b = eRetryAlways.getAsBooleanValue ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.retryAlways(" + b + ")");
      aHCS.setRetryAlways (b);
    }
  }

  /**
   * Assign timeout related configuration values. The primary configuration parameters consumed are:
   * <ul>
   * <li><code>http.timeout.connectionrequest</code> - connection request timeout (duration)</li>
   * <li><code>http.timeout.connect</code> - connect timeout (duration)</li>
   * <li><code>http.timeout.response</code> - response/read timeout (duration)</li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   */
  public static void assignConfigValuesForTimeouts (@NonNull final HttpClientSettings aHCS,
                                                    @NonNull final HttpClientConfig aHCC)
  {
    final Timeout aConnectionRequestTimeout = aHCC.getConnectionRequestTimeout ();
    if (aConnectionRequestTimeout != null)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.connectionRequestTimeout(" +
                      aConnectionRequestTimeout +
                      ")");
      aHCS.setConnectionRequestTimeout (aConnectionRequestTimeout);
    }

    final Timeout aConnectTimeout = aHCC.getConnectTimeout ();
    if (aConnectTimeout != null)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.connectTimeout(" + aConnectTimeout + ")");
      aHCS.setConnectTimeout (aConnectTimeout);
    }

    final Timeout aResponseTimeout = aHCC.getResponseTimeout ();
    if (aResponseTimeout != null)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.responseTimeout(" + aResponseTimeout + ")");
      aHCS.setResponseTimeout (aResponseTimeout);
    }
  }

  /**
   * Assign TLS related configuration values. The primary configuration parameters consumed are:
   * <ul>
   * <li><code>http.tls.checks.disabled</code> - disable both hostname and certificate checks
   * (boolean)</li>
   * <li><code>http.tls.hostname-check.disabled</code> - disable TLS hostname verification
   * (boolean)</li>
   * <li><code>http.tls.certificate-check.disabled</code> - disable TLS certificate verification
   * (boolean)</li>
   * </ul>
   * Disabling TLS checks is a security risk and should only be used for testing.
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   * @throws IllegalStateException
   *         If setting up the trust-all SSL context fails.
   */
  public static void assignConfigValuesForTLS (@NonNull final HttpClientSettings aHCS,
                                               @NonNull final HttpClientConfig aHCC)
  {
    final boolean bDefaultDisableTLS = false;

    // The global property
    final ETriState eDisableTLSChecks = aHCC.getDisableTlsChecks (bDefaultDisableTLS);

    final ETriState eDisableHostnameCheck = aHCC.getDisableHostnameCheck (bDefaultDisableTLS);
    if ((eDisableHostnameCheck.isDefined () && eDisableHostnameCheck.getAsBooleanValue ()) ||
        (eDisableTLSChecks.isDefined () && eDisableTLSChecks.getAsBooleanValue ()))
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.setHostnameVerifierVerifyAll()");
      aHCS.setHostnameVerifierVerifyAll ();
      LOGGER.warn ("Disabled the hostname check for SSL/TLS connections. This may be a security risk.");
    }

    final ETriState eDisableCertificateCheck = aHCC.getDisableCertificateCheck (bDefaultDisableTLS);
    if ((eDisableCertificateCheck.isDefined () && eDisableCertificateCheck.getAsBooleanValue ()) ||
        (eDisableTLSChecks.isDefined () && eDisableTLSChecks.getAsBooleanValue ()))
    {
      try
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Setting configured HttpClientSettings.setSSLContextTrustAll()");
        aHCS.setSSLContextTrustAll ();
        LOGGER.warn ("Disabled the certificate check for SSL/TLS connections. This may be a security risk.");
      }
      catch (final GeneralSecurityException ex)
      {
        throw new IllegalStateException ("Failed to set SSL Context for TLS connection", ex);
      }
    }
  }

  /**
   * Assign revocation check configuration values.
   * <ul>
   * <li><code>http.tls.revocation.mode</code> - revocation check mode (see
   * {@link ERevocationCheckMode})</li>
   * <li><code>http.tls.revocation.soft-fail</code> - whether revocation checks soft-fail
   * (boolean)</li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   * @since 11.3.0
   */
  public static void assignConfigValuesForRevocation (@NonNull final HttpClientSettings aHCS,
                                                      @NonNull final HttpClientConfig aHCC)
  {
    final String sMode = aHCC.getRevocationCheckMode ();
    if (StringHelper.isNotEmpty (sMode))
    {
      final ERevocationCheckMode eMode = ERevocationCheckMode.getFromIDOrNull (sMode);
      if (eMode == null)
        LOGGER.warn ("Invalid revocation check mode '" + sMode + "' configured. Ignoring it.");
      else
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Setting configured HttpClientSettings.revocationCheckMode(" + eMode + ")");
        aHCS.setRevocationCheckMode (eMode);
      }
    }

    // Use existing value as fallback to avoid changing to default
    final ETriState eSoftFail = aHCC.getRevocationCheckSoftFail (aHCS.isRevocationCheckSoftFail ());
    if (eSoftFail.isDefined ())
    {
      final boolean b = eSoftFail.getAsBooleanValue ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.revocationCheckSoftFail(" + b + ")");
      aHCS.setRevocationCheckSoftFail (b);
    }
  }

  /**
   * Assign miscellaneous configuration values. The primary configuration parameters consumed are:
   * <ul>
   * <li><code>http.useragent</code> - the User-Agent HTTP header value</li>
   * <li><code>http.follow-redirects</code> - whether HTTP redirects are followed (boolean)</li>
   * <li><code>http.keep-alive</code> - whether HTTP keep-alive is used (boolean)</li>
   * <li><code>http.protocol-upgrade.enabled</code> - whether HTTP protocol upgrade is enabled
   * (boolean)</li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aHCC
   *        The configuration source. May not be <code>null</code>.
   */
  public static void assignConfigValuesForMisc (@NonNull final HttpClientSettings aHCS,
                                                @NonNull final HttpClientConfig aHCC)
  {
    final String sUserAgent = aHCC.getUserAgent ();
    if (StringHelper.isNotEmpty (sUserAgent))
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.userAgent(" + sUserAgent + ")");
      aHCS.setUserAgent (sUserAgent);
    }

    // Use existing value as fallback to avoid changing to default
    final ETriState eFollowRedirects = aHCC.getFollowRedirects (aHCS.isFollowRedirects ());
    if (eFollowRedirects.isDefined ())
    {
      final boolean b = eFollowRedirects.getAsBooleanValue ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.followRedirects(" + b + ")");
      aHCS.setFollowRedirects (b);
    }

    // Use existing value as fallback to avoid changing to default
    final ETriState eKeepAlive = aHCC.getUseKeepAlive (aHCS.isUseKeepAlive ());
    if (eKeepAlive.isDefined ())
    {
      final boolean b = eKeepAlive.getAsBooleanValue ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.keepAlive(" + b + ")");
      aHCS.setUseKeepAlive (b);
    }

    // Use existing value as fallback to avoid changing to default
    final ETriState eProtocolUpgradeEnabled = aHCC.getProtocolUpgradeEnabled (aHCS.isProtocolUpgradeEnabled ());
    if (eProtocolUpgradeEnabled.isDefined ())
    {
      final boolean b = eProtocolUpgradeEnabled.getAsBooleanValue ();
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Setting configured HttpClientSettings.protocolUpgradeEnabled(" + b + ")");
      aHCS.setProtocolUpgradeEnabled (b);
    }
  }

  /**
   * Assign all settings of {@link HttpClientSettings} from configuration values. The primary
   * configuration parameters consumed by this method are (each key is implicitly prefixed with the
   * configured prefixes; legacy alias keys are accepted but not listed here):
   * <ul>
   * <li>DNS:
   * <ul>
   * <li><code>http.dnsclientcache.use</code> - whether to use the DNS client cache (boolean)</li>
   * </ul>
   * </li>
   * <li>Proxy (host + port are sufficient to activate the proxy; only skipped if
   * <code>http.proxy.enabled</code> is explicitly <code>false</code>):
   * <ul>
   * <li><code>http.proxy.enabled</code> - explicit kill-switch; only checked for an explicit
   * <code>false</code> value (boolean)</li>
   * <li><code>http.proxy.host</code> - HTTP proxy host name</li>
   * <li><code>http.proxy.port</code> - HTTP proxy port</li>
   * <li><code>http.proxy.username</code> - HTTP proxy user name</li>
   * <li><code>http.proxy.password</code> - HTTP proxy password</li>
   * <li><code>http.proxy.nonProxyHosts</code> - pipe separated list of non-proxy hosts</li>
   * </ul>
   * </li>
   * <li>Retry:
   * <ul>
   * <li><code>http.retry.count</code> - number of retries (int)</li>
   * <li><code>http.retry.interval</code> - interval between retries (duration)</li>
   * <li><code>http.retry.always</code> - whether to always retry (boolean)</li>
   * </ul>
   * </li>
   * <li>Timeouts:
   * <ul>
   * <li><code>http.timeout.connectionrequest</code> - connection request timeout (duration)</li>
   * <li><code>http.timeout.connect</code> - connect timeout (duration)</li>
   * <li><code>http.timeout.response</code> - response/read timeout (duration)</li>
   * </ul>
   * </li>
   * <li>TLS:
   * <ul>
   * <li><code>http.tls.checks.disabled</code> - disable both hostname and certificate checks
   * (boolean)</li>
   * <li><code>http.tls.hostname-check.disabled</code> - disable TLS hostname verification
   * (boolean)</li>
   * <li><code>http.tls.certificate-check.disabled</code> - disable TLS certificate verification
   * (boolean)</li>
   * </ul>
   * </li>
   * <li>Certificate revocation:
   * <ul>
   * <li><code>http.tls.revocation.mode</code> - revocation check mode (see
   * {@link ERevocationCheckMode})</li>
   * <li><code>http.tls.revocation.soft-fail</code> - whether revocation checks soft-fail
   * (boolean)</li>
   * </ul>
   * </li>
   * <li>Miscellaneous:
   * <ul>
   * <li><code>http.useragent</code> - the User-Agent HTTP header value</li>
   * <li><code>http.follow-redirects</code> - whether HTTP redirects are followed (boolean)</li>
   * <li><code>http.keep-alive</code> - whether HTTP keep-alive is used (boolean)</li>
   * <li><code>http.protocol-upgrade.enabled</code> - whether HTTP protocol upgrade is enabled
   * (boolean)</li>
   * </ul>
   * </li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be <code>null</code>.
   * @param aConfig
   *        The {@link IConfig} object to used as the source of the values. May not be
   *        <code>null</code>.
   * @param aPrefixes
   *        The configuration prefixes to be used. If this value may neither be <code>null</code>
   *        nor empty, it will be used as the constant prefix.
   */
  public static final void assignConfigValues (@NonNull final HttpClientSettings aHCS,
                                               @NonNull final IConfigWithFallback aConfig,
                                               @NonNull @Nonempty final String... aPrefixes)
  {
    ValueEnforcer.notNull (aHCS, "HttpClientSettings");
    ValueEnforcer.notNull (aConfig, "Config");
    ValueEnforcer.notEmptyNoNullValue (aPrefixes, "Prefixes");

    final HttpClientConfig aHCC = HttpClientConfig.create (aConfig, aPrefixes);
    if (aHCC != null)
    {
      // DNS stuff
      assignConfigValuesForDNS (aHCS, aHCC);

      // General proxy stuff
      assignConfigValuesForProxy (aHCS.getGeneralProxy (), aHCC);

      // Retry
      assignConfigValuesForRetry (aHCS, aHCC);

      // Timeouts
      assignConfigValuesForTimeouts (aHCS, aHCC);

      // TLS stuff
      assignConfigValuesForTLS (aHCS, aHCC);

      // Certificate revocation
      assignConfigValuesForRevocation (aHCS, aHCC);

      // Other stuff
      assignConfigValuesForMisc (aHCS, aHCC);
    }
  }
}
