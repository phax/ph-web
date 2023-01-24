package com.helger.httpclient;

import java.time.Duration;
import java.util.function.ObjIntConsumer;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.commons.typeconvert.TypeConverter;
import com.helger.config.IConfig;
import com.helger.config.fallback.IConfigWithFallback;

/**
 * A helper class to configure {@link HttpClientSettings} using {@link IConfig}
 * with standardized configuration property names.
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
    private final String m_sConfigPrefix;
    private final boolean m_bAllowFallbackToGlobalScope;

    public HttpClientConfig (@Nonnull final IConfigWithFallback aConfig,
                             @Nonnull final String sConfigPrefix,
                             final boolean bAllowFallbackToGlobalScope)
    {
      ValueEnforcer.notNull (aConfig, "Config");
      ValueEnforcer.notNull (sConfigPrefix, "ConfigPrefix");
      ValueEnforcer.isTrue ( () -> sConfigPrefix.isEmpty () || sConfigPrefix.endsWith ("."), "ConfigPrefix is invalid");
      m_aConfig = aConfig;
      m_sConfigPrefix = sConfigPrefix;
      // Fallback only if desired and a config prefix is present. Otherwise that
      // makes no sense
      m_bAllowFallbackToGlobalScope = bAllowFallbackToGlobalScope && sConfigPrefix.length () > 0;
    }

    // TODO replace with ArrayHelper method in ph-commons 11.0.2
    private static <ELEMENTTYPE> void _forEach (@Nonnull final ELEMENTTYPE [] aArray,
                                                @Nonnull final ObjIntConsumer <? super ELEMENTTYPE> aConsumer)
    {
      int nIndex = 0;
      for (final ELEMENTTYPE aElement : aArray)
      {
        aConsumer.accept (aElement, nIndex);
        ++nIndex;
      }
    }

    @Nullable
    private String _findString (@Nonnull final String sLocalKey, @Nullable final String... aLocalSubKeys)
    {
      String ret;
      if (aLocalSubKeys.length == 0)
        ret = m_aConfig.getAsString (m_sConfigPrefix + sLocalKey);
      else
      {
        // Add configPrefix to all values
        final String [] aRealSubKeys = new String [aLocalSubKeys.length];
        _forEach (aLocalSubKeys, (val, idx) -> aRealSubKeys[idx] = m_sConfigPrefix + val);
        ret = m_aConfig.getAsStringOrFallback (m_sConfigPrefix + sLocalKey, aRealSubKeys);
      }

      if (ret == null && m_bAllowFallbackToGlobalScope)
      {
        if (aLocalSubKeys.length == 0)
          ret = m_aConfig.getAsString (sLocalKey);
        else
          ret = m_aConfig.getAsStringOrFallback (sLocalKey, aLocalSubKeys);
      }

      return ret;
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
      int ret;
      if (aLocalSubKeys.length == 0)
        ret = m_aConfig.getAsInt (m_sConfigPrefix + sLocalKey, nDefault);
      else
      {
        // Add configPrefix to all values
        final String [] aRealSubKeys = new String [aLocalSubKeys.length];
        _forEach (aLocalSubKeys, (x, i) -> aRealSubKeys[i] = m_sConfigPrefix + x);
        ret = m_aConfig.getAsIntOrFallback (m_sConfigPrefix + sLocalKey, nDefault, nDefault, aRealSubKeys);
      }

      if (ret == nDefault && m_bAllowFallbackToGlobalScope)
      {
        if (aLocalSubKeys.length == 0)
          ret = m_aConfig.getAsInt (sLocalKey, nDefault);
        else
          ret = m_aConfig.getAsIntOrFallback (sLocalKey, nDefault, nDefault, aLocalSubKeys);
      }

      return ret;
    }

    @CheckForSigned
    private long _findLong (@Nonnull final String sLocalKey,
                            final long nDefault,
                            @Nullable final String... aLocalSubKeys)
    {
      long ret;
      if (aLocalSubKeys.length == 0)
        ret = m_aConfig.getAsLong (m_sConfigPrefix + sLocalKey, nDefault);
      else
      {
        // Add configPrefix to all values
        final String [] aRealSubKeys = new String [aLocalSubKeys.length];
        _forEach (aLocalSubKeys, (x, i) -> aRealSubKeys[i] = m_sConfigPrefix + x);
        ret = m_aConfig.getAsLongOrFallback (m_sConfigPrefix + sLocalKey, nDefault, nDefault, aRealSubKeys);
      }

      if (ret == nDefault && m_bAllowFallbackToGlobalScope)
      {
        if (aLocalSubKeys.length == 0)
          ret = m_aConfig.getAsLong (sLocalKey, nDefault);
        else
          ret = m_aConfig.getAsLongOrFallback (sLocalKey, nDefault, nDefault, aLocalSubKeys);
      }

      return ret;
    }

    @Nonnull
    public ETriState getUseDNSClientCache (final boolean bDefault)
    {
      return _findBoolean ("http.dnsclientcache.use", bDefault, "http.useDNSClientCache");
    }

    /**
     * @return The HttpProxy host to be used. May be <code>null</code>.
     */
    @Nullable
    public String getHttpProxyHost ()
    {
      return _findString ("http.proxy.host", "http.proxyHost");
    }

    /**
     * @return The HTTP proxy port to be used, or <code>0</code> in case it is
     *         not found.
     */
    @CheckForSigned
    public int getHttpProxyPort ()
    {
      return _findInt ("http.proxy.port", -1, "http.proxyPort");
    }

    /**
     * @return The HttpProxy object to be used. This is only
     *         non-<code>null</code> if proxy host is non-<code>null</code> and
     *         proxy port returns a value &gt; 0.
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
    public String getHttpProxyPassword ()
    {
      return _findString ("http.proxy.password", "http.proxyPassword");
    }

    /**
     * @return The {@link UsernamePasswordCredentials} object to be used for
     *         proxy server authentication or <code>null</code> if not username
     *         and password are configured.
     * @see #getHttpProxyUsername()
     * @see #getHttpProxyPassword()
     */
    @Nullable
    public UsernamePasswordCredentials getHttpProxyCredentials ()
    {
      final String sProxyUsername = getHttpProxyUsername ();
      final String sProxyPassword = getHttpProxyPassword ();
      if (sProxyUsername != null && sProxyPassword != null)
        return new UsernamePasswordCredentials (sProxyUsername, sProxyPassword.toCharArray ());

      return null;
    }

    /**
     * @return A pipe separated list of non-proxy hosts. E.g.
     *         <code>localhost|127.0.0.1</code>. May be <code>null</code>.
     */
    @Nullable
    public String getNonProxyHosts ()
    {
      return _findString ("http.proxy.nonProxyHosts", "http.nonProxyHosts");
    }

    @CheckForSigned
    public int getRetryCount ()
    {
      // Use -1 to indicate: don't use the configured value if it is crap
      return _findInt ("http.retry.count", -1);
    }

    /**
     * @return The interval in which a retry should happen. Only relevant is
     *         retry count &gt; 0.
     * @see #getRetryCount()
     */
    @Nullable
    public Duration getRetryInterval ()
    {
      final long nMillis = _findLong ("http.retry.interval.millis", -1);
      if (nMillis > 0)
        return Duration.ofMillis (nMillis);

      final long nSeconds = _findLong ("http.retry.interval.seconds", -1);
      if (nSeconds > 0)
        return Duration.ofSeconds (nSeconds);

      final long nMinutes = _findLong ("http.retry.interval.minutes", -1);
      if (nMinutes > 0)
        return Duration.ofMinutes (nMinutes);

      final long nHours = _findLong ("http.retry.interval.hours", -1);
      if (nHours > 0)
        return Duration.ofHours (nHours);

      return null;
    }

    @Nonnull
    public ETriState getRetryAlways (final boolean bDefault)
    {
      return _findBoolean ("http.retry.always", bDefault);
    }

    @Nullable
    private Timeout _findTimeout (@Nonnull final String sPrefix)
    {
      final long nMillis = _findLong (sPrefix + ".millis", -1);
      if (nMillis > 0)
        return Timeout.ofMilliseconds (nMillis);

      final long nSeconds = _findLong (sPrefix + ".seconds", -1);
      if (nSeconds > 0)
        return Timeout.ofSeconds (nSeconds);

      final long nMinutes = _findLong (sPrefix + ".minutes", -1);
      if (nMinutes > 0)
        return Timeout.ofMinutes (nMinutes);

      final long nHours = _findLong (sPrefix + ".hours", -1);
      if (nHours > 0)
        return Timeout.ofHours (nHours);

      return null;
    }

    @Nullable
    public Timeout getConnectionRequestTimeout ()
    {
      return _findTimeout ("http.timeout.connectionrequest");
    }

    @Nullable
    public Timeout getConnectTimeout ()
    {
      return _findTimeout ("http.timeout.connect");
    }

    @Nullable
    public Timeout getResponseTimeout ()
    {
      return _findTimeout ("http.timeout.response");
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
  }

  /**
   * Assign all settings of {@link HttpClientSettings} from configuration
   * values. This includes:
   * <ul>
   * <li>http.useDNSClientCache - use the DNS client cache by default</li>
   * </ul>
   *
   * @param aHCS
   *        The {@link HttpClientSettings} to be configured. May not be
   *        <code>null</code>.
   * @param aConfig
   *        The {@link IConfig} object to used as the source of the values. May
   *        not be <code>null</code>.
   * @param sPrefix
   *        The configuration prefix to be used. If this value is not empty, it
   *        will be used as the constant prefix.
   * @param bAllowFallbackToGlobalScope
   *        if <code>true</code> and a prefix is given,
   */
  public static final void assignConfigValues (@Nonnull final HttpClientSettings aHCS,
                                               @Nonnull final IConfigWithFallback aConfig,
                                               @Nonnull final String sPrefix,
                                               final boolean bAllowFallbackToGlobalScope)
  {
    ValueEnforcer.notNull (aHCS, "HttpClientSettings");
    ValueEnforcer.notNull (aConfig, "Config");
    ValueEnforcer.notNull (sPrefix, "Prefix");

    // Either empty or ending with a string
    final String sRealConfigPrefix = sPrefix.isEmpty () ? "" : sPrefix.endsWith (".") ? sPrefix : sPrefix + ".";
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Using prefix '" + sRealConfigPrefix + "' to configure HTTP client settings");
    if (sRealConfigPrefix.isEmpty ())
    {
      LOGGER.warn ("Configuring HTTP client settings with generic configuration");
      if (bAllowFallbackToGlobalScope)
        LOGGER.error ("The fallback to the global scope makes no sense, if the base configuration is already global");
    }

    final HttpClientConfig aHCC = new HttpClientConfig (aConfig, sRealConfigPrefix, bAllowFallbackToGlobalScope);

    // DNS stuff
    {
      // Use existing value as fallback to avoid changing to default
      final ETriState eUseDNSClientCache = aHCC.getUseDNSClientCache (aHCS.isUseDNSClientCache ());
      if (eUseDNSClientCache.isDefined ())
        aHCS.setUseDNSClientCache (eUseDNSClientCache.getAsBooleanValue ());
    }

    // Proxy stuff
    {
      final HttpHost aProxyHost = aHCC.getHttpProxyObject ();
      if (aProxyHost != null)
        aHCS.setProxyHost (aProxyHost);

      final UsernamePasswordCredentials aProxyCredentials = aHCC.getHttpProxyCredentials ();
      if (aProxyCredentials != null)
        aHCS.setProxyCredentials (aProxyCredentials);

      final String sNonProxyHosts = aHCC.getNonProxyHosts ();
      if (StringHelper.hasText (sNonProxyHosts))
        aHCS.setNonProxyHostsFromPipeString (sNonProxyHosts);
    }

    // Retry
    {
      final int nRetryCount = aHCC.getRetryCount ();
      if (nRetryCount >= 0)
        aHCS.setRetryCount (nRetryCount);

      final Duration aRetryInterval = aHCC.getRetryInterval ();
      if (aRetryInterval != null)
        aHCS.setRetryInterval (aRetryInterval);

      // Use existing value as fallback to avoid changing to default
      final ETriState eRetryAlways = aHCC.getRetryAlways (aHCS.isRetryAlways ());
      if (eRetryAlways.isDefined ())
        aHCS.setRetryAlways (eRetryAlways.getAsBooleanValue ());
    }

    // Timeouts
    {
      final Timeout aConnectionRequestTimeout = aHCC.getConnectionRequestTimeout ();
      if (aConnectionRequestTimeout != null)
        aHCS.setConnectionRequestTimeout (aConnectionRequestTimeout);

      final Timeout aConnectTimeout = aHCC.getConnectTimeout ();
      if (aConnectTimeout != null)
        aHCS.setConnectTimeout (aConnectTimeout);

      final Timeout aResponseTimeout = aHCC.getResponseTimeout ();
      if (aResponseTimeout != null)
        aHCS.setResponseTimeout (aResponseTimeout);
    }

    // Other stuff
    {
      final String sUserAgent = aHCC.getUserAgent ();
      if (StringHelper.hasText (sUserAgent))
        aHCS.setUserAgent (sUserAgent);

      // Use existing value as fallback to avoid changing to default
      final ETriState eFollowRedirects = aHCC.getFollowRedirects (aHCS.isFollowRedirects ());
      if (eFollowRedirects.isDefined ())
        aHCS.setFollowRedirects (eFollowRedirects.getAsBooleanValue ());

      // Use existing value as fallback to avoid changing to default
      final ETriState eKeepAlive = aHCC.getUseKeepAlive (aHCS.isUseKeepAlive ());
      if (eKeepAlive.isDefined ())
        aHCS.setUseKeepAlive (eKeepAlive.getAsBooleanValue ());
    }
  }
}
