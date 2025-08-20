/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
import com.helger.typeconvert.impl.TypeConverter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

    public HttpClientConfig (@Nonnull final IConfigWithFallback aConfig,
                             @Nonnull @Nonempty final ICommonsOrderedSet <String> aConfigPrefixes)
    {
      ValueEnforcer.notNull (aConfig, "Config");
      ValueEnforcer.notEmptyNoNullValue (aConfigPrefixes, "Prefixes");
      m_aConfig = aConfig;
      m_aConfigPrefixes = aConfigPrefixes;
    }

    @Nonnull
    private static String [] _copyAndMap (@Nonnull final String [] aArray,
                                          @Nonnull final Function <String, String> aFun)
    {
      final String [] ret = new String [aArray.length];
      ArrayHelper.forEach (aArray, (val, idx) -> ret[idx] = aFun.apply (val));
      return ret;
    }

    @Nullable
    private String _findString (@Nonnull final String sLocalKey, @Nullable final String... aLocalSubKeys)
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
    private char [] _findCharArray (@Nonnull final String sLocalKey, @Nullable final String... aLocalSubKeys)
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

    @Nonnull
    private ETriState _findBoolean (@Nonnull final String sLocalKey,
                                    final boolean bDefault,
                                    @Nullable final String... aLocalSubKeys)
    {
      final String ret = _findString (sLocalKey, aLocalSubKeys);
      if (ret == null)
        return ETriState.UNDEFINED;

      return ETriState.valueOf (TypeConverter.convertToBoolean (ret, bDefault));
    }

    @CheckForSigned
    private int _findInt (@Nonnull final String sLocalKey, final int nDefault, @Nullable final String... aLocalSubKeys)
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
          ret = m_aConfig.getAsIntOrFallback (sConfigPrefix + sLocalKey, nDefault, nDefault, aRealSubKeys);
        }
        if (ret != nDefault)
          return ret;
      }
      return nDefault;
    }

    @CheckForSigned
    private long _findSingleLong (@Nonnull final String sConfigPrefix,
                                  @Nonnull final String sLocalKey,
                                  final long nDefault,
                                  @Nullable final String... aLocalSubKeys)
    {
      if (aLocalSubKeys.length == 0)
        return m_aConfig.getAsLong (sConfigPrefix + sLocalKey, nDefault);

      // Add configPrefix to all values
      final String [] aRealSubKeys = _copyAndMap (aLocalSubKeys, x -> sConfigPrefix + x);
      return m_aConfig.getAsLongOrFallback (sConfigPrefix + sLocalKey, nDefault, nDefault, aRealSubKeys);
    }

    @CheckForSigned
    private long _findLong (@Nonnull final String sLocalKey,
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

    @Nonnull
    public ETriState getUseDNSClientCache (final boolean bDefault)
    {
      return _findBoolean ("http.dnsclientcache.use", bDefault, "http.useDNSClientCache");
    }

    @Nonnull
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
     * @return The HTTP proxy port to be used, or <code>0</code> in case it is not found.
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
     * @deprecated Use {@link #getHttpProxyPasswordCharArray()} instead
     */
    @Nullable
    @Deprecated (forRemoval = true, since = "10.5.0")
    public String getHttpProxyPassword ()
    {
      return _findString ("http.proxy.password", "http.proxyPassword");
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
     * @see #getHttpProxyPassword()
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

    @CheckForSigned
    public int getRetryCount ()
    {
      // Use -1 to indicate: don't use the configured value if it is crap
      return _findInt ("http.retry.count", -1);
    }

    @Nullable
    private Duration _findDuration (@Nonnull final String sLocalKey, @Nullable final String... aLocalSubKeys)
    {
      for (final String sConfigPrefix : m_aConfigPrefixes)
      {
        final long nMillis = _findSingleLong (sConfigPrefix,
                                              sLocalKey + ".millis",
                                              -1,
                                              _copyAndMap (aLocalSubKeys, x -> x + ".millis"));
        if (nMillis > 0)
          return Duration.ofMillis (nMillis);

        final long nSeconds = _findSingleLong (sConfigPrefix,
                                               sLocalKey + ".seconds",
                                               -1,
                                               _copyAndMap (aLocalSubKeys, x -> x + ".seconds"));
        if (nSeconds > 0)
          return Duration.ofSeconds (nSeconds);

        final long nMinutes = _findSingleLong (sConfigPrefix,
                                               sLocalKey + ".minutes",
                                               -1,
                                               _copyAndMap (aLocalSubKeys, x -> x + ".minutes"));
        if (nMinutes > 0)
          return Duration.ofMinutes (nMinutes);

        final long nHours = _findSingleLong (sConfigPrefix,
                                             sLocalKey + ".hours",
                                             -1,
                                             _copyAndMap (aLocalSubKeys, x -> x + ".hours"));
        if (nHours > 0)
          return Duration.ofHours (nHours);
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

    @Nonnull
    public ETriState getRetryAlways (final boolean bDefault)
    {
      return _findBoolean ("http.retry.always", bDefault);
    }

    @Nullable
    private Timeout _findTimeout (@Nonnull final String sPrefix, @Nullable final String... aLocalSubKeys)
    {
      final Duration aDuration = _findDuration (sPrefix, aLocalSubKeys);
      return aDuration == null ? null : Timeout.of (aDuration);
    }

    @Nullable
    public Timeout getConnectionRequestTimeout ()
    {
      return _findTimeout ("http.timeout.connectionrequest");
    }

    @Nullable
    public Timeout getConnectTimeout ()
    {
      return _findTimeout ("http.timeout.connect", "http.connection-timeout");
    }

    @Nullable
    public Timeout getResponseTimeout ()
    {
      return _findTimeout ("http.timeout.response", "http.read-timeout");
    }

    @Nullable
    public String getUserAgent ()
    {
      return _findString ("http.useragent");
    }

    @Nonnull
    public ETriState getFollowRedirects (final boolean bDefault)
    {
      return _findBoolean ("http.follow-redirects", bDefault);
    }

    @Nonnull
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
    @Nonnull
    public ETriState getProtocolUpgradeEnabled (final boolean bDefault)
    {
      return _findBoolean ("http.protocol-upgrade.enabled", bDefault);
    }

    @Nonnull
    public ETriState getDisableTlsChecks (final boolean bDefault)
    {
      return _findBoolean ("http.tls.checks.disabled", bDefault);
    }

    @Nonnull
    public ETriState getDisableHostnameCheck (final boolean bDefault)
    {
      return _findBoolean ("http.tls.hostname-check.disabled", bDefault);
    }

    @Nonnull
    public ETriState getDisableCertificateCheck (final boolean bDefault)
    {
      return _findBoolean ("http.tls.certificate-check.disabled", bDefault);
    }
  }

  private HttpClientSettingsConfig ()
  {}

  /**
   * Assign all settings of {@link HttpClientSettings} from configuration values. This includes:
   * <ul>
   * <li>http.useDNSClientCache - use the DNS client cache by default</li>
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
  public static final void assignConfigValues (@Nonnull final HttpClientSettings aHCS,
                                               @Nonnull final IConfigWithFallback aConfig,
                                               @Nonnull @Nonempty final String... aPrefixes)
  {
    ValueEnforcer.notNull (aHCS, "HttpClientSettings");
    ValueEnforcer.notNull (aConfig, "Config");
    ValueEnforcer.notEmptyNoNullValue (aPrefixes, "Prefixes");

    // Either empty or ending with a string
    final ICommonsOrderedSet <String> aRealPrefixes = new CommonsLinkedHashSet <> (aPrefixes,
                                                                                   x -> x.isEmpty () || x.endsWith (".")
                                                                                                                         ? x
                                                                                                                         : x +
                                                                                                                           ".");
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Using prefixes '" + aRealPrefixes + "' to configure HTTP client settings");
    if (aRealPrefixes.isEmpty ())
    {
      LOGGER.warn ("No configuration prefixes provided to configure HTTP client settings. Nothing happens");
      return;
    }

    final HttpClientConfig aHCC = new HttpClientConfig (aConfig, aRealPrefixes);

    // DNS stuff
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

    // Proxy stuff
    {
      final boolean bDefaultProxyEnabled = false;
      final ETriState eProxyEnabled = aHCC.getHttpProxyEnabled (bDefaultProxyEnabled);
      if (eProxyEnabled.isDefined () && eProxyEnabled.getAsBooleanValue ())
      {
        final HttpHost aProxyHost = aHCC.getHttpProxyObject ();
        if (aProxyHost != null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Setting configured HttpClientSettings.proxyHost(" + aProxyHost + ")");
          aHCS.getGeneralProxy ().setProxyHost (aProxyHost);
        }

        final UsernamePasswordCredentials aProxyCredentials = aHCC.getHttpProxyCredentials ();
        if (aProxyCredentials != null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Setting configured HttpClientSettings.proxyCredentials(" + aProxyCredentials + ")");
          aHCS.getGeneralProxy ().setProxyCredentials (aProxyCredentials);
        }

        final String sNonProxyHosts = aHCC.getNonProxyHosts ();
        if (StringHelper.isNotEmpty (sNonProxyHosts))
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Setting configured HttpClientSettings.nonProxyHosts(" + sNonProxyHosts + ")");
          aHCS.getGeneralProxy ().setNonProxyHostsFromPipeString (sNonProxyHosts);
        }
      }
    }

    // Retry
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

    // Timeouts
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

    // Other stuff
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

    // TLS stuff
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
  }
}
