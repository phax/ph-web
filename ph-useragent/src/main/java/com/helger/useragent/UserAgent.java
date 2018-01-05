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
package com.helger.useragent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.version.Version;
import com.helger.useragent.browser.BrowserInfo;
import com.helger.useragent.browser.BrowserInfoIE;
import com.helger.useragent.browser.BrowserInfoMobile;
import com.helger.useragent.browser.BrowserInfoSpider;
import com.helger.useragent.browser.EBrowserType;
import com.helger.useragent.browser.MobileBrowserManager;
import com.helger.useragent.spider.WebSpiderInfo;
import com.helger.useragent.spider.WebSpiderManager;

/**
 * Default implementation of the {@link IUserAgent} interface.
 *
 * @author Philip Helger
 */
public class UserAgent implements IUserAgent
{
  private static final String GECKO_SEARCH_STRING = "Gecko";
  private static final String LYNX_SEARCH_STRING = "Lynx";
  private static final String CHROME_SEARCH_STRING = "Chrome";
  private static final String VERSION_SEARCH_STRING = "Version";
  private static final String SAFARI_SEARCH_STRING = "Safari";
  private static final String OPERA_SEARCH_STRING = "Opera";
  private static final String FIREFOX_SEARCH_STRING = "Firefox";
  private static final String VIVALDI_SEARCH_STRING = "Vivaldi";
  private static final String IE_SEARCH_STRING = "MSIE";
  private static final String IE_TRIDENT_SEARCH_STRING = "Trident/";
  private static final String IE_RV_SEARCH_STRING = "rv:";
  private static final String KONQUEROR_PREFIX = "Konqueror/";
  private static final String [] GECKO_VARIANTS = new String [] { "GranParadiso",
                                                                  "Fedora",
                                                                  "Namoroka",
                                                                  "Netscape",
                                                                  "K-Meleon",
                                                                  "WebThumb" };
  private static final int IE_VERSION_IN_COMPATIBILITY_MODE = 7;

  private final String m_sFullUserAgent;
  private final UserAgentElementList m_aElements;
  private BrowserInfo m_aInfoFirefox;
  private BrowserInfoIE m_aInfoIE;
  private BrowserInfo m_aInfoOpera;
  private BrowserInfo m_aInfoSafari;
  private BrowserInfo m_aInfoChrome;
  private BrowserInfo m_aInfoVivaldi;
  private BrowserInfo m_aInfoLynx;
  private BrowserInfo m_aInfoKonqueror;
  private BrowserInfo m_aInfoGeckoBased;
  private BrowserInfo m_aInfoWebKitBased;
  private BrowserInfoMobile m_aInfoMobile;
  private BrowserInfoSpider m_aInfoWebSpider;
  private BrowserInfo m_aInfoApplication;

  public UserAgent (@Nonnull final String sFullUserAgent, @Nonnull final UserAgentElementList aElements)
  {
    m_sFullUserAgent = ValueEnforcer.notNull (sFullUserAgent, "FullUserAgent");
    m_aElements = ValueEnforcer.notNull (aElements, "Elements");
  }

  @Nonnull
  public String getAsString ()
  {
    return m_sFullUserAgent;
  }

  @Nullable
  public BrowserInfo getBrowserInfo ()
  {
    if (getInfoFirefox ().isIt ())
      return getInfoFirefox ();
    if (getInfoIE ().isIt ())
      return getInfoIE ();
    if (getInfoOpera ().isIt ())
      return getInfoOpera ();
    if (getInfoSafari ().isIt ())
      return getInfoSafari ();
    if (getInfoChrome ().isIt ())
      return getInfoChrome ();
    if (getInfoVivaldi ().isIt ())
      return getInfoVivaldi ();
    if (getInfoLynx ().isIt ())
      return getInfoLynx ();
    if (getInfoKonqueror ().isIt ())
      return getInfoKonqueror ();
    if (getInfoGeckoBased ().isIt ())
      return getInfoGeckoBased ();
    if (getInfoWebKitBased ().isIt ())
      return getInfoWebKitBased ();
    if (getInfoMobile ().isIt ())
      return getInfoMobile ();
    if (getInfoWebSpider ().isIt ())
      return getInfoWebSpider ();
    if (getInfoApplication ().isIt ())
      return getInfoApplication ();
    return null;
  }

  @Nonnull
  public BrowserInfo getInfoFirefox ()
  {
    if (m_aInfoFirefox == null)
    {
      // Example:
      // Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2)
      // Gecko/20100115 Firefox/3.6
      String sVersionFirefox = m_aElements.getPairValue (FIREFOX_SEARCH_STRING);
      if (sVersionFirefox == null)
      {
        // Example2:
        // Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.1.4)
        // Gecko/20091016 Boersenfegers Firefox 3.5.4
        sVersionFirefox = m_aElements.getStringValueFollowing (FIREFOX_SEARCH_STRING);
      }

      if (sVersionFirefox == null)
        m_aInfoFirefox = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoFirefox = new BrowserInfo (EBrowserType.FIREFOX, Version.parse (sVersionFirefox));
    }
    return m_aInfoFirefox;
  }

  @Nonnull
  public BrowserInfoIE getInfoIE ()
  {
    if (m_aInfoIE == null)
    {
      // Example:
      // IE7:
      // Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; SLCC2; .NET CLR
      // 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0;
      // Tablet PC 2.0)
      // IE8:
      // Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2;
      // .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media
      // Center PC 6.0; Tablet PC 2.0)
      // IE8 compatibility view:
      // Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/4.0; SLCC2;
      // .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media
      // Center PC 6.0; Tablet PC 2.0)
      // IE 11:
      // Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; MALC; rv:11.0) like
      // Gecko
      String sInfoIE = m_aElements.getListItemStartingWith (IE_SEARCH_STRING);
      final String sTrident = m_aElements.getListItemStartingWith (IE_TRIDENT_SEARCH_STRING);
      // Negative Example:
      // Mozilla/4.0 (compatible; MSIE 6.0; X11; Linux i686; en) Opera 9.63
      if (m_aElements.containsString (OPERA_SEARCH_STRING))
        sInfoIE = null;

      if (sInfoIE == null)
      {
        // IE 11 special handling - no MSIE any more
        if (sTrident != null)
        {
          // "Trident/" must be present and "rv:" indicates the vesion number
          final String sRV = m_aElements.getListItemStartingWith (IE_RV_SEARCH_STRING);
          if (sRV != null)
          {
            final Version aVersion = Version.parse (sRV.substring (IE_RV_SEARCH_STRING.length ()));
            m_aInfoIE = new BrowserInfoIE (aVersion, false);
          }
          else
            m_aInfoIE = BrowserInfoIE.IS_IT_NOT_IE;
        }
        else
          m_aInfoIE = BrowserInfoIE.IS_IT_NOT_IE;
      }
      else
      {
        // IE Compatibility Mode check
        // http://blogs.msdn.com/b/ie/archive/2010/03/23/introducing-ie9-s-user-agent-string.aspx
        final Version aVersion = Version.parse (sInfoIE.substring (IE_SEARCH_STRING.length ()).trim ());
        final boolean bIsIECompatibilityMode = aVersion.getMajor () == IE_VERSION_IN_COMPATIBILITY_MODE &&
                                               sTrident != null;
        m_aInfoIE = new BrowserInfoIE (aVersion, bIsIECompatibilityMode);
      }
    }
    return m_aInfoIE;
  }

  @Nonnull
  public BrowserInfo getInfoOpera ()
  {
    if (m_aInfoOpera == null)
    {
      // Example:
      // Opera/9.64 (Windows NT 6.1; U; en) Presto/2.1.1
      String sVersionOpera = m_aElements.getPairValue (OPERA_SEARCH_STRING);
      if (sVersionOpera != null)
      {
        // Special case:
        // Opera/9.80 (Windows NT 5.1; U; hu) Presto/2.2.15 Version/10.10
        final String sVersion = m_aElements.getPairValue (VERSION_SEARCH_STRING);
        if (sVersion != null)
          sVersionOpera = sVersion;
      }
      else
      {
        // Example:
        // Mozilla/4.0 (compatible; MSIE 6.0; X11; Linux i686; en) Opera 9.63
        sVersionOpera = m_aElements.getStringValueFollowing (OPERA_SEARCH_STRING);
      }
      if (sVersionOpera == null)
        m_aInfoOpera = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoOpera = new BrowserInfo (EBrowserType.OPERA, Version.parse (sVersionOpera));
    }
    return m_aInfoOpera;
  }

  @Nonnull
  public BrowserInfo getInfoSafari ()
  {
    if (m_aInfoSafari == null)
    {
      // Example:
      // Mozilla/5.0 (Windows; U; Windows NT 6.1; de-DE) AppleWebKit/531.21.8
      // (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10
      final String sSafari = m_aElements.getPairValue (SAFARI_SEARCH_STRING);
      final String sVersion = sSafari == null ? null : m_aElements.getPairValue (VERSION_SEARCH_STRING);
      if (sVersion == null)
        m_aInfoSafari = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoSafari = new BrowserInfo (EBrowserType.SAFARI, Version.parse (sVersion));
    }
    return m_aInfoSafari;
  }

  @Nonnull
  public BrowserInfo getInfoChrome ()
  {
    if (m_aInfoChrome == null)
    {
      // Example:
      // Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.9
      // (KHTML, like Gecko) Chrome/5.0.317.2 Safari/532.9
      final String sSafari = m_aElements.getPairValue (SAFARI_SEARCH_STRING);
      final String sVersion = sSafari == null ? null : m_aElements.getPairValue (CHROME_SEARCH_STRING);
      if (sVersion == null || m_aElements.getPairValue (VIVALDI_SEARCH_STRING) != null)
        m_aInfoChrome = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoChrome = new BrowserInfo (EBrowserType.CHROME, Version.parse (sVersion));
    }
    return m_aInfoChrome;
  }

  @Nonnull
  public BrowserInfo getInfoVivaldi ()
  {
    if (m_aInfoVivaldi == null)
    {
      // Example:
      // Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like
      // Gecko) Chrome/50.0.2661.94 Safari/537.36 Vivaldi/1.1.453.52
      final String sVersion = m_aElements.getPairValue (VIVALDI_SEARCH_STRING);
      if (sVersion == null ||
          m_aElements.getPairValue (CHROME_SEARCH_STRING) == null ||
          m_aElements.getPairValue (SAFARI_SEARCH_STRING) == null)
        m_aInfoVivaldi = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoVivaldi = new BrowserInfo (EBrowserType.VIVALDI, Version.parse (sVersion));
    }
    return m_aInfoVivaldi;
  }

  @Nonnull
  public BrowserInfo getInfoLynx ()
  {
    if (m_aInfoLynx == null)
    {
      // Example:
      // Lynx/2.8.3rel.1 libwww-FM/2.14FM
      final String sVersion = m_aElements.getPairValue (LYNX_SEARCH_STRING);
      if (sVersion == null)
        m_aInfoLynx = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoLynx = new BrowserInfo (EBrowserType.LYNX, Version.parse (sVersion));
    }
    return m_aInfoLynx;
  }

  @Nonnull
  public BrowserInfo getInfoKonqueror ()
  {
    if (m_aInfoKonqueror == null)
    {
      // Example:
      // Mozilla/5.0 (compatible; Konqueror/3.4; FreeBSD; en_US) KHTML/3.4.0
      // (like Gecko)
      final String sVersion = m_aElements.getListItemStartingWith (KONQUEROR_PREFIX);
      if (sVersion == null)
        m_aInfoKonqueror = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoKonqueror = new BrowserInfo (EBrowserType.KONQUEROR,
                                            Version.parse (sVersion.substring (KONQUEROR_PREFIX.length ())));
    }
    return m_aInfoKonqueror;
  }

  @Nonnull
  public BrowserInfo getInfoGeckoBased ()
  {
    if (m_aInfoGeckoBased == null)
    {
      // Example:
      // Mozilla/5.0 (Windows; U; Windows NT 5.1; de-AT; rv:1.7.12)
      // Gecko/20050915
      String sVersionGecko = m_aElements.getPairValue (GECKO_SEARCH_STRING);
      if (sVersionGecko == null && m_sFullUserAgent.contains (GECKO_SEARCH_STRING))
      {
        // Examples:
        // Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.11) Gecko
        // GranParadiso/3.0.11
        // Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.4) Gecko Netscape/7.1
        // (ax)
        // Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.1.17pre) Gecko
        // K-Meleon/1.6.0
        for (final String sVariant : GECKO_VARIANTS)
        {
          sVersionGecko = m_aElements.getPairValue (sVariant);
          if (sVersionGecko != null)
            break;
        }
      }

      // If we already have FireFox, no need for generic Gecko detection
      if (sVersionGecko == null || getInfoFirefox ().isIt ())
        m_aInfoGeckoBased = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoGeckoBased = new BrowserInfo (EBrowserType.GECKO, Version.parse (sVersionGecko));
    }
    return m_aInfoGeckoBased;
  }

  @Nonnull
  public BrowserInfo getInfoWebKitBased ()
  {
    if (m_aInfoWebKitBased == null)
    {
      // Example:
      // Mozilla/5.0 (Windows; U; Windows NT 6.0; de-AT) AppleWebKit/532.4
      // (KHTML, like Gecko) QtWeb Internet Browser/3.3 http://www.QtWeb.net
      final String sVersionWebKit = m_aElements.getPairValue ("AppleWebKit");

      // If we already have Safari or Chrome, no need for generic WebKit
      // detection
      if (sVersionWebKit == null || getInfoSafari ().isIt () || getInfoChrome ().isIt ())
        m_aInfoWebKitBased = BrowserInfo.IS_IT_NOT;
      else
        m_aInfoWebKitBased = new BrowserInfo (EBrowserType.WEBKIT, Version.parse (sVersionWebKit));
    }
    return m_aInfoWebKitBased;
  }

  @Nonnull
  public BrowserInfoMobile getInfoMobile ()
  {
    if (m_aInfoMobile == null)
    {
      final String sVersion = MobileBrowserManager.getFromUserAgent (m_sFullUserAgent);
      if (sVersion == null)
        m_aInfoMobile = BrowserInfoMobile.IS_IT_NOT_MOBILE;
      else
        m_aInfoMobile = new BrowserInfoMobile (sVersion);
    }
    return m_aInfoMobile;
  }

  @Nonnull
  public BrowserInfoSpider getInfoWebSpider ()
  {
    if (m_aInfoWebSpider == null)
    {
      final WebSpiderInfo aWebSpiderInfo = WebSpiderManager.getInstance ().getWebSpiderFromUserAgent (m_sFullUserAgent);
      if (aWebSpiderInfo == null)
        m_aInfoWebSpider = BrowserInfoSpider.IS_IT_NOT_SPIDER;
      else
        m_aInfoWebSpider = new BrowserInfoSpider (aWebSpiderInfo);
    }
    return m_aInfoWebSpider;
  }

  @Nonnull
  public BrowserInfo getInfoApplication ()
  {
    if (m_aInfoApplication == null)
    {
      final String sApp = ApplicationUserAgentManager.getFromUserAgent (m_sFullUserAgent);
      if (sApp == null)
        m_aInfoApplication = BrowserInfoSpider.IS_IT_NOT_SPIDER;
      else
        m_aInfoApplication = new BrowserInfo (EBrowserType.APPLICATION, Version.DEFAULT_VERSION);
    }
    return m_aInfoApplication;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("FullUserAgent", m_sFullUserAgent)
                                       .append ("Elements", m_aElements)
                                       .append ("Firefox", getInfoFirefox ())
                                       .append ("IE", getInfoIE ())
                                       .append ("Opera", getInfoOpera ())
                                       .append ("Safari", getInfoSafari ())
                                       .append ("Chrome", getInfoChrome ())
                                       .append ("Vivaldi", getInfoVivaldi ())
                                       .append ("Lynx", getInfoLynx ())
                                       .append ("Gecko based", getInfoGeckoBased ())
                                       .append ("Webkit based", getInfoWebKitBased ())
                                       .append ("Mobile", getInfoMobile ())
                                       .append ("WebSpider", getInfoWebSpider ())
                                       .append ("Application", getInfoApplication ())
                                       .getToString ();
  }
}
