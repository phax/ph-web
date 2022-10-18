/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.timing.StopWatch;
import com.helger.dns.resolve.DNSResolver;
import com.helger.network.proxy.config.SocksProxyConfig;
import com.helger.network.proxy.settings.IProxySettings;
import com.helger.network.proxy.settings.ProxySettings;

/**
 * Proxy Auto Configuration helper. Requires ph-dns to work.
 *
 * @author Philip Helger
 */
public final class ProxyAutoConfigHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ProxyAutoConfigHelper.class);

  public static final Charset DEFAULT_SCRIPT_CHARSET = StandardCharsets.ISO_8859_1;
  // create a Nashorn script engine
  private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager ().getEngineByName ("nashorn");

  static
  {
    if (SCRIPT_ENGINE == null)
      LOGGER.warn ("Failed to create Nashorn ScriptEngine");
    else
      try
      {
        final StopWatch aSW = StopWatch.createdStarted ();
        SCRIPT_ENGINE.eval ("var dnsResolve = function(hostName){ return " +
                            DNSResolver.class.getName () +
                            ".dnsResolve(hostName); }");
        SCRIPT_ENGINE.eval ("var dnsResolveEx = function(hostName){ return " +
                            DNSResolver.class.getName () +
                            ".dnsResolveEx(hostName); }");
        SCRIPT_ENGINE.eval ("var myIpAddress = function(){ return " +
                            DNSResolver.class.getName () +
                            ".getMyIpAddress(); }");
        SCRIPT_ENGINE.eval (new ClassPathResource ("proxy-js/pac-utils.js").getReader (DEFAULT_SCRIPT_CHARSET));
        final long nMS = aSW.stopAndGetMillis ();
        if (nMS > 100)
          if (LOGGER.isInfoEnabled ())
            LOGGER.info ("Initial ProxyAutoConfig (PAC) Nashorn script compilation took " + nMS + " ms");
      }
      catch (final ScriptException ex)
      {
        throw new InitializationException ("Failed to init ProxyAutoConfig (PAC) Nashorn script!", ex);
      }
  }

  private final IReadableResource m_aPACRes;
  private final String m_sPACCode;

  public ProxyAutoConfigHelper (@Nonnull final IReadableResource aPACRes) throws ScriptException
  {
    m_aPACRes = ValueEnforcer.notNull (aPACRes, "PACResource");
    m_sPACCode = null;
    if (SCRIPT_ENGINE != null)
      SCRIPT_ENGINE.eval (m_aPACRes.getReader (DEFAULT_SCRIPT_CHARSET));
  }

  public ProxyAutoConfigHelper (@Nonnull final String sPACCode) throws ScriptException
  {
    m_aPACRes = null;
    m_sPACCode = ValueEnforcer.notNull (sPACCode, "PACCode");
    if (SCRIPT_ENGINE != null)
      SCRIPT_ENGINE.eval (m_sPACCode);
  }

  public static boolean isNashornScriptEngineAvailable ()
  {
    return SCRIPT_ENGINE != null;
  }

  // Cannot be static, because it needs the evaluation in the constructor
  @Nullable
  public String findProxyForURL (@Nonnull final String sURL, @Nonnull final String sHost) throws ScriptException
  {
    if (SCRIPT_ENGINE == null)
    {
      LOGGER.warn ("Because no Nashorn ScriptEngine could be created, no proxy can be found");
      return null;
    }

    // Call "findProxyForURL" or "FindProxyForURLEx" that must be defined in the
    // PAC file!
    final Object aResult = SCRIPT_ENGINE.eval ("findProxyForURL('" + sURL + "', '" + sHost + "')");
    if (aResult == null)
      return null;

    /*
     * Return Value Format The JavaScript function returns a single string. If
     * the string is null, no proxies should be used. The string can contain any
     * number of the following building blocks, separated by a semicolon: DIRECT
     * Connections should be made directly, without any proxies. PROXY host:port
     * The specified proxy should be used. SOCKS host:port The specified SOCKS
     * server should be used.
     */
    return aResult.toString ();
  }

  @Nonnull
  public ICommonsList <IProxySettings> getProxyListForURL (@Nonnull final URI aURI) throws ScriptException
  {
    return getProxyListForURL (aURI.toString (), aURI.getHost ());
  }

  @Nonnull
  public ICommonsList <IProxySettings> getProxyListForURL (@Nonnull final String sURL,
                                                           @Nonnull final String sHost) throws ScriptException
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
            if (LOGGER.isWarnEnabled ())
              LOGGER.warn ("Found unknown proxy directive '" + sDirective + "'");
        }
      }
    }
    return ret;
  }
}
