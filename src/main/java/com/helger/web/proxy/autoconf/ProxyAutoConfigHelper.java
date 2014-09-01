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
package com.helger.web.proxy.autoconf;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.charset.CCharset;
import com.helger.commons.io.IReadableResource;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.commons.url.URLProtocolRegistry;
import com.helger.web.dns.DNSResolver;
import com.helger.web.proxy.EHttpProxyType;
import com.helger.web.proxy.HttpProxyConfig;
import com.helger.web.proxy.IProxyConfig;
import com.helger.web.proxy.NoProxyConfig;
import com.helger.web.proxy.SocksProxyConfig;

public final class ProxyAutoConfigHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ProxyAutoConfigHelper.class);
  private static final ScriptableObject s_aGlobalScope;
  static
  {
    final Context aCtx = Context.enter ();
    try
    {
      s_aGlobalScope = aCtx.initStandardObjects ();
      s_aGlobalScope.defineFunctionProperties (new String [] { "dnsResolve" },
                                               DNSResolver.class,
                                               ScriptableObject.DONTENUM);
      s_aGlobalScope.defineFunctionProperties (new String [] { "myIpAddress" },
                                               DNSResolver.class,
                                               ScriptableObject.DONTENUM);
      RhinoUtils.readFile (s_aGlobalScope,
                           aCtx,
                           new ClassPathResource ("proxy-js/pac-utils.js"),
                           "pac-utils",
                           CCharset.CHARSET_ISO_8859_1);
      s_aGlobalScope.sealObject ();
    }
    finally
    {
      Context.exit ();
    }
  }

  private final IReadableResource m_aPACRes;
  private final String m_sPACCode;
  private Scriptable m_aInstanceScope;

  public ProxyAutoConfigHelper (@Nonnull final IReadableResource aPACRes)
  {
    m_aPACRes = ValueEnforcer.notNull (aPACRes, "PACResource");
    m_sPACCode = null;
  }

  public ProxyAutoConfigHelper (@Nonnull final String sPACCode)
  {
    m_aPACRes = null;
    m_sPACCode = ValueEnforcer.notNull (sPACCode, "PACCode");
  }

  @Nonnull
  private Scriptable _getInstanceScope (final Context aCtx)
  {
    if (m_aInstanceScope == null)
    {
      // create the scope
      m_aInstanceScope = aCtx.newObject (s_aGlobalScope);
      m_aInstanceScope.setPrototype (s_aGlobalScope);
      m_aInstanceScope.setParentScope (null);

      // read the PAC file
      if (m_aPACRes != null)
        RhinoUtils.readFile (m_aInstanceScope, aCtx, m_aPACRes, "PAC file", CCharset.CHARSET_ISO_8859_1);
      else
        RhinoUtils.readString (m_aInstanceScope, aCtx, m_sPACCode);
    }
    return m_aInstanceScope;
  }

  @Nullable
  public String findProxyForURL (@Nonnull final String sURL, @Nonnull final String sHost)
  {
    final Context aCtx = Context.enter ();
    try
    {
      // create "instance" scope"
      final Scriptable aInstanceScope = _getInstanceScope (aCtx);

      // Call "FindProxyForURL"
      final Object aResult = aCtx.evaluateString (aInstanceScope,
                                                  "FindProxyForURL('" + sURL + "', '" + sHost + "')",
                                                  "<inline>",
                                                  1,
                                                  null);
      if (aResult == null)
        return null;

      // FIXME parse result:
      /*
       * Return Value Format The JavaScript function returns a single string. If
       * the string is null, no proxies should be used. The string can contain
       * any number of the following building blocks, separated by a semicolon:
       * DIRECT Connections should be made directly, without any proxies. PROXY
       * host:port The specified proxy should be used. SOCKS host:port The
       * specified SOCKS server should be used.
       */
      return aResult.toString ();
    }
    finally
    {
      Context.exit ();
    }
  }

  @Nonnull
  public List <IProxyConfig> getProxyListForURL (final String sURL, final String sHost)
  {
    final List <IProxyConfig> ret = new ArrayList <IProxyConfig> ();
    String sProxyCode = findProxyForURL (sURL, sHost);
    if (sProxyCode != null)
    {
      sProxyCode = sProxyCode.trim ();
      if (sProxyCode.length () > 0)
      {
        for (String sDirective : StringHelper.getExploded (';', sProxyCode))
        {
          boolean bError = true;
          sDirective = sDirective.trim ();
          if (sDirective.equals ("DIRECT"))
          {
            ret.add (NoProxyConfig.getInstance ());
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
                  final EHttpProxyType eProxyType = EHttpProxyType.getFromURLProtocolOrDefault (URLProtocolRegistry.getProtocol (sProxyHost),
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
                    final int nProxyPort = StringParser.parseInt (sProxyPort, SocksProxyConfig.DEFAULT_SOCKS_PROXY_PORT);
                    ret.add (new SocksProxyConfig (sProxyHost, nProxyPort));
                    bError = false;
                  }
                }
              }

          if (bError)
            s_aLogger.warn ("Found unknown proxy directive '" + sDirective + "'");
        }
      }
    }
    return ret;
  }
}
