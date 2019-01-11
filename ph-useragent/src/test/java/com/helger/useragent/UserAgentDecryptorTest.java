/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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
package com.helger.useragent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.version.Version;

public final class UserAgentDecryptorTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UserAgentDecryptorTest.class);
  private final String [] POSSIBLE = new String [] { "'Mozilla/4.7 [en] (WinNT; U)'",
                                                     "Mozilla/4.7 [en] (WinNT; U)",
                                                     "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT)",
                                                     "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; T312461; .NET CLR 1.1.4322)",
                                                     "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT 4.0) Opera 5.11 [en]",
                                                     "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.0.2) Gecko/20030208 Netscape/7.02",
                                                     "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.6) Gecko/20040612 Firefox/0.8",
                                                     "Mozilla/5.0 (compatible; Konqueror/3.2; Linux) (KHTML, like Gecko)",
                                                     "Lynx/2.8.4rel.1 libwww-FM/2.14 SSL-MM/1.4.1 OpenSSL/0.9.6h",
                                                     "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.7) Gecko/2009021910 Firefox/3.0.7 (.NET CLR 3.5.30729)",
                                                     "Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.7.10) Gecko/20050716 Firefox/1.0.6",
                                                     "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)",
                                                     "Mozilla/4.0 (compatible; MSIE 6.0; MSN 2.5; Windows 98)",
                                                     "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",
                                                     "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 6.0)",
                                                     "Mozilla/5.0 (compatible; Konqueror/3.1; Linux)",
                                                     "Mozilla/5.0 (compatible; Konqueror/3.2; Linux) (KHTML, like Gecko)",
                                                     "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)",
                                                     "Mozilla/5.0 (compatible; Konqueror/3.1; Linux 2.4.22-10mdk; X11; i686; fr, fr_FR)",
                                                     "Mozilla/5.0 (X11; U; Linux i686; en-US; rv - 1.7.8) Gecko/20050511",
                                                     "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv - 1.7.8) Gecko/20050511 Firefox/1.0.4",
                                                     "Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv - 1.0.1) Gecko/20020920 Netscape/7.0",
                                                     "Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.03 [en]",
                                                     "Lynx/2.8.4rel.1 libwww-FM/2.14",
                                                     "Mozilla/5.0 (compatible; googlebot/2.1; +http://www.google.com/bot.html)",
                                                     "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.7) Gecko/2009021910 Firefox/3.0.7 (.NET CLR 3.5.30729)",
                                                     "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; OfficeLiveConnector.1.3; OfficeLivePatch.0.0; .NET CLR 3.5.30729; .NET CLR 3.0.30618)",
                                                     "Opera/9.64 (Windows NT 6.0; U; en) Presto/2.1.1",
                                                     "Mozilla/5.0 (Windows; U; Windows NT 6.0; de-DE) AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0 Safari/528.16",
                                                     "xx",
                                                     "x/y",
                                                     "x/",
                                                     "/y",
                                                     "x (",
                                                     "x [",
                                                     "BlackBerry9500/4.7.0.173 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/137",
                                                     "Mozilla/5.0 (Windows; U; Windows NT 6.0; de-AT) AppleWebKit/532.4 (KHTML, like Gecko) QtWeb Internet Browser/3.3 http://www.QtWeb.net",
                                                     "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36 Vivaldi/1.1.453.52" };

  @Test
  public void testBasic ()
  {
    int nCount = 0;
    for (final String s : POSSIBLE)
    {
      assertNotNull (s, UserAgentDecryptor.decryptUserAgentString (s));
      ++nCount;
    }
    LOGGER.info ("Tested " + nCount + " user agents");
  }

  @Test
  public void testBrowserInfo ()
  {
    // Firefox
    IUserAgent aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6");
    assertTrue (aUA.getInfoFirefox ().isIt ());
    assertFalse (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertFalse (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
    assertEquals (new Version (3, 6), aUA.getInfoFirefox ().getVersion ());

    // Internet Explorer
    aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0)");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertTrue (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertFalse (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
    assertEquals (new Version (7), aUA.getInfoIE ().getVersion ());

    aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; MALC; rv:11.0) like Gecko");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertTrue (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertFalse (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
    assertEquals (new Version (11), aUA.getInfoIE ().getVersion ());

    // Opera
    aUA = UserAgentDecryptor.decryptUserAgentString ("Opera/9.64 (Windows NT 6.1; U; en) Presto/2.1.1");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertFalse (aUA.getInfoIE ().isIt ());
    assertTrue (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertFalse (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
    assertEquals (new Version (9, 64), aUA.getInfoOpera ().getVersion ());

    // Safari
    aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/5.0 (Windows; U; Windows NT 6.1; de-DE) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertFalse (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertTrue (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertFalse (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
    assertEquals (new Version (4, 0, 4), aUA.getInfoSafari ().getVersion ());

    // Chrome
    aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.317.2 Safari/532.9");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertFalse (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertTrue (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertFalse (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
    assertEquals (new Version (5, 0, 317, "2"), aUA.getInfoChrome ().getVersion ());

    // QtWeb
    aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/5.0 (Windows; U; Windows NT 6.0; de-AT) AppleWebKit/532.4 (KHTML, like Gecko) QtWeb Internet Browser/3.3 http://www.QtWeb.net");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertFalse (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertFalse (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertTrue (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());

    // Vivaldi
    aUA = UserAgentDecryptor.decryptUserAgentString ("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36 Vivaldi/1.1.453.52");
    assertFalse (aUA.getInfoFirefox ().isIt ());
    assertFalse (aUA.getInfoIE ().isIt ());
    assertFalse (aUA.getInfoOpera ().isIt ());
    assertFalse (aUA.getInfoSafari ().isIt ());
    assertFalse (aUA.getInfoChrome ().isIt ());
    assertTrue (aUA.getInfoVivaldi ().isIt ());
    assertFalse (aUA.getInfoGeckoBased ().isIt ());
    assertTrue (aUA.getInfoWebKitBased ().isIt ());
    assertFalse (aUA.getInfoWebSpider ().isIt ());
  }
}
