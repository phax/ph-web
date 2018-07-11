/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.script.ScriptHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.URLProtocolRegistry;
import com.helger.network.dns.DNSResolver;
import com.helger.network.proxy.EHttpProxyType;
import com.helger.network.proxy.HttpProxyConfig;
import com.helger.network.proxy.IProxyConfig;
import com.helger.network.proxy.NoProxyConfig;
import com.helger.network.proxy.SocksProxyConfig;

public final class ProxyAutoConfigHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ProxyAutoConfigHelper.class);

  // create a Nashorn script engine
  private static final ScriptEngine s_aScriptEngine = ScriptHelper.createNashornEngine ();

  static
  {
    try
    {
      final StopWatch aSW = StopWatch.createdStarted ();
      s_aScriptEngine.eval ("var dnsResolve = function(hostName){ return " +
                            DNSResolver.class.getName () +
                            ".dnsResolve(hostName); }");
      s_aScriptEngine.eval ("var dnsResolveEx = function(hostName){ return " +
                            DNSResolver.class.getName () +
                            ".dnsResolveEx(hostName); }");
      s_aScriptEngine.eval ("var myIpAddress = function(){ return " +
                            DNSResolver.class.getName () +
                            ".getMyIpAddress(); }");
      s_aScriptEngine.eval (new ClassPathResource ("proxy-js/pac-utils.js").getReader (ScriptHelper.DEFAULT_SCRIPT_CHARSET));
      final long nMS = aSW.stopAndGetMillis ();
      if (nMS > 100)
        if (s_aLogger.isInfoEnabled ())
          s_aLogger.info ("Initial PAC script compilaiton took " + nMS + " ms");
    }
    catch (final ScriptException ex)
    {
      throw new InitializationException ("Failed to init ProxyAutoConfig Nashorn script!", ex);
    }
  }

  private final IReadableResource m_aPACRes;
  private final String m_sPACCode;

  public ProxyAutoConfigHelper (@Nonnull final IReadableResource aPACRes) throws ScriptException
  {
    m_aPACRes = ValueEnforcer.notNull (aPACRes, "PACResource");
    m_sPACCode = null;
    s_aScriptEngine.eval (m_aPACRes.getReader (ScriptHelper.DEFAULT_SCRIPT_CHARSET));
  }

  public ProxyAutoConfigHelper (@Nonnull final String sPACCode) throws ScriptException
  {
    m_aPACRes = null;
    m_sPACCode = ValueEnforcer.notNull (sPACCode, "PACCode");
    s_aScriptEngine.eval (m_sPACCode);
  }

  @Nullable
  public String findProxyForURL (@Nonnull final String sURL, @Nonnull final String sHost) throws ScriptException
  {
    // Call "FindProxyForURL" or "FindProxyForURLEx" that must be defined in the
    // PAC file!
    final Object aResult = s_aScriptEngine.eval ("findProxyForURL('" + sURL + "', '" + sHost + "')");
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
  public ICommonsList <IProxyConfig> getProxyListForURL (final String sURL, final String sHost) throws ScriptException
  {
    final ICommonsList <IProxyConfig> ret = new CommonsArrayList <> ();
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
            ret.add (new NoProxyConfig ());
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
                  final EHttpProxyType eProxyType = EHttpProxyType.getFromURLProtocolOrDefault (URLProtocolRegistry.getInstance ()
                                                                                                                   .getProtocol (sProxyHost),
                                                                                                EHttpProxyType.HTTP);
                  final int nProxyPort = StringParser.parseInt (sProxyPort, eProxyType.getDefaultPort ());
                  ret.add (new HttpProxyConfig (eProxyType, sProxyHost, nProxyPort));
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
                    ret.add (new SocksProxyConfig (sProxyHost, nProxyPort));
                    bError = false;
                  }
                }
              }

          if (bError)
            if (s_aLogger.isWarnEnabled ())
              s_aLogger.warn ("Found unknown proxy directive '" + sDirective + "'");
        }
      }
    }
    return ret;
  }
}
