/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.web.proxy.autoconf.functest;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.helger.commons.charset.CCharset;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.URLResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.web.proxy.autoconf.ProxyAutoConfigHelper;

public final class ReadWPADFuncTest
{
  public static String getProxyAutoConfigFunction (final IReadableResource aRes,
                                                   final String sCharset) throws IOException
  {
    InputStream aIS = null;
    BufferedReader aReader = null;
    try
    {
      aIS = aRes.getInputStream ();
      if (aIS != null)
      {
        aReader = new BufferedReader (new InputStreamReader (aIS, sCharset));
        String sLine;
        final StringBuilder aAutoConfigScript = new StringBuilder ();
        while ((sLine = aReader.readLine ()) != null)
          aAutoConfigScript.append (sLine).append ('\n');
        return aAutoConfigScript.toString ();
      }
      return null;
    }
    finally
    {
      StreamHelper.close (aReader);
    }
  }

  @Test
  public void testReadWPAD () throws IOException
  {
    // Works for Intercent-ER
    final String sAutoProxyConfig = false ? getProxyAutoConfigFunction (new URLResource ("http://wpad.ente.regione.emr.it/wpad.dat"),
                                                                        CCharset.CHARSET_ISO_8859_1)
                                          : getProxyAutoConfigFunction (new URLResource ("http://wpad/wpad.dat"),
                                                                        CCharset.CHARSET_ISO_8859_1);
    if (sAutoProxyConfig != null)
    {
      final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (sAutoProxyConfig);
      assertNotNull (aPACHelper.findProxyForURL ("http://www.orf.at/index.html", "www.orf.at"));
    }
  }

  @Test
  public void testReadWPADExamples () throws IOException
  {
    for (final String sFile : new String [] { "wpad01.dat", "wpad02.dat", "wpad03.dat" })
    {
      final String sAutoProxyConfig = getProxyAutoConfigFunction (new ClassPathResource ("proxyautoconf/datfiles/" +
                                                                                         sFile),
                                                                  CCharset.CHARSET_ISO_8859_1);
      assertNotNull (sFile, sAutoProxyConfig);
      final ProxyAutoConfigHelper aPACHelper = new ProxyAutoConfigHelper (sAutoProxyConfig);
      assertNotNull (sFile, aPACHelper.findProxyForURL ("http://www.orf.at/index.html", "www.orf.at"));
    }
  }
}
