/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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
package com.helger.network.proxy.autoconf;

import java.net.Proxy;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringParser;
import com.helger.base.timing.StopWatch;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.dns.resolve.DNSResolver;
import com.helger.io.resource.ClassPathResource;
import com.helger.io.resource.IReadableResource;
import com.helger.network.proxy.config.SocksProxyConfig;
import com.helger.network.proxy.settings.IProxySettings;
import com.helger.network.proxy.settings.ProxySettings;

/**
 * Proxy Auto Configuration helper. Requires ph-dns to work. Each instance has its own
 * {@link ScriptEngine} to ensure thread-safety and instance isolation.
 *
 * @author Philip Helger
 */
public final class ProxyAutoConfigHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ProxyAutoConfigHelper.class);

  public static final Charset DEFAULT_SCRIPT_CHARSET = StandardCharsets.ISO_8859_1;

  /** DNS helper function definitions, evaluated once per engine */
  private static final String DNS_INIT_SCRIPT = "var dnsResolve = function(hostName){ return " +
                                                DNSResolver.class.getName () +
                                                ".dnsResolve(hostName); };\n" +
                                                "var dnsResolveEx = function(hostName){ return " +
                                                DNSResolver.class.getName () +
                                                ".dnsResolveEx(hostName); };\n" +
                                                "var myIpAddress = function(){ return " +
                                                DNSResolver.class.getName () +
                                                ".getMyIpAddress(); };";

  /** {@code true} if the Nashorn engine is available in this JVM */
  private static final boolean NASHORN_AVAILABLE;

  static
  {
    final ScriptEngine aProbe = new ScriptEngineManager ().getEngineByName ("nashorn");
    NASHORN_AVAILABLE = aProbe != null;
    if (!NASHORN_AVAILABLE)
      LOGGER.warn ("Failed to create Nashorn ScriptEngine");
  }

  /** Per-instance script engine — not shared, no synchronization needed */
  private final ScriptEngine m_aScriptEngine;

  @Nullable
  private static ScriptEngine _createInitializedEngine () throws ScriptException
  {
    if (!NASHORN_AVAILABLE)
      return null;

    final ScriptEngine aEngine = new ScriptEngineManager ().getEngineByName ("nashorn");
    if (aEngine == null)
      return null;

    final StopWatch aSW = StopWatch.createdStarted ();
    aEngine.eval (DNS_INIT_SCRIPT);
    aEngine.eval (new ClassPathResource ("proxy-js/pac-utils.js").getReader (DEFAULT_SCRIPT_CHARSET));
    final long nMS = aSW.stopAndGetMillis ();
    if (nMS > 100)
      LOGGER.info ("ProxyAutoConfig (PAC) Nashorn script compilation took " + nMS + " ms");

    return aEngine;
  }

  public ProxyAutoConfigHelper (@NonNull final IReadableResource aPACRes) throws ScriptException
  {
    ValueEnforcer.notNull (aPACRes, "PACResource");
    m_aScriptEngine = _createInitializedEngine ();
    if (m_aScriptEngine != null)
      m_aScriptEngine.eval (aPACRes.getReader (DEFAULT_SCRIPT_CHARSET));
  }

  public ProxyAutoConfigHelper (@NonNull final String sPACCode) throws ScriptException
  {
    ValueEnforcer.notNull (sPACCode, "PACCode");
    m_aScriptEngine = _createInitializedEngine ();
    if (m_aScriptEngine != null)
      m_aScriptEngine.eval (sPACCode);
  }

  public static boolean isNashornScriptEngineAvailable ()
  {
    return NASHORN_AVAILABLE;
  }

  @Nullable
  public String findProxyForURL (@NonNull final String sURL, @NonNull final String sHost) throws ScriptException
  {
    if (m_aScriptEngine == null)
    {
      LOGGER.warn ("Because no Nashorn ScriptEngine could be created, no proxy can be found");
      return null;
    }
    // Call "findProxyForURL" that must be defined in the PAC file!
    // Use Invocable.invokeFunction to safely pass parameters without script injection risk
    final Object aResult;
    try
    {
      aResult = ((Invocable) m_aScriptEngine).invokeFunction ("findProxyForURL", sURL, sHost);
    }
    catch (final NoSuchMethodException ex)
    {
      throw new ScriptException ("PAC script does not define function 'findProxyForURL': " + ex.getMessage ());
    }
    if (aResult == null)
      return null;

    /*
     * Return Value Format The JavaScript function returns a single string. If the string is null,
     * no proxies should be used. The string can contain any number of the following building
     * blocks, separated by a semicolon: DIRECT Connections should be made directly, without any
     * proxies. PROXY host:port The specified proxy should be used. SOCKS host:port The specified
     * SOCKS server should be used.
     */
    return aResult.toString ();
  }

  @NonNull
  public ICommonsList <IProxySettings> getProxyListForURL (@NonNull final URI aURI) throws ScriptException
  {
    return getProxyListForURL (aURI.toString (), aURI.getHost ());
  }

  @NonNull
  public ICommonsList <IProxySettings> getProxyListForURL (@NonNull final String sURL, @NonNull final String sHost)
                                                                                                                    throws ScriptException
  {
    final ICommonsList <IProxySettings> ret = new CommonsArrayList <> ();
    String sProxyCode = findProxyForURL (sURL, sHost);
    if (sProxyCode != null)
    {
      // parse result of PAC call
      sProxyCode = sProxyCode.trim ();
      if (sProxyCode.length () > 0)
      {
        for (String sDirective : StringHelper.getExploded (';', sProxyCode))
        {
          boolean bError = true;
          sDirective = sDirective.trim ();
          if (sDirective.equals ("DIRECT"))
          {
            ret.add (ProxySettings.createNoProxySettings ());
            bError = false;
          }
          else
            if (sDirective.startsWith ("PROXY"))
            {
              String [] aParts = StringHelper.getExplodedArray (' ', sDirective, 2);
              if (aParts.length == 2)
              {
                aParts = StringHelper.getExplodedArray (':', aParts[1], 2);
                if (aParts.length == 2)
                {
                  final String sProxyHost = aParts[0];
                  final String sProxyPort = aParts[1];
                  final int nProxyPort = StringParser.parseInt (sProxyPort, -1);
                  ret.add (new ProxySettings (Proxy.Type.HTTP, sProxyHost, nProxyPort));
                  bError = false;
                }
              }
            }
            else
              if (sDirective.startsWith ("SOCKS"))
              {
                String [] aParts = StringHelper.getExplodedArray (' ', sDirective, 2);
                if (aParts.length == 2)
                {
                  aParts = StringHelper.getExplodedArray (':', aParts[1], 2);
                  if (aParts.length == 2)
                  {
                    final String sProxyHost = aParts[0];
                    final String sProxyPort = aParts[1];
                    final int nProxyPort = StringParser.parseInt (sProxyPort,
                                                                  SocksProxyConfig.DEFAULT_SOCKS_PROXY_PORT);
                    ret.add (new ProxySettings (Proxy.Type.SOCKS, sProxyHost, nProxyPort));
                    bError = false;
                  }
                }
              }
          if (bError)
            LOGGER.warn ("Found unknown proxy directive '" + sDirective + "'");
        }
      }
    }
    return ret;
  }
}
