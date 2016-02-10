/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.web.useragent.spider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.serialize.MicroReader;
import com.helger.commons.microdom.util.MicroHelper;
import com.helger.commons.microdom.util.XMLListHandler;

/**
 * Provides a list of known web spiders.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class WebSpiderManager
{
  private static final class SingletonHolder
  {
    static final WebSpiderManager s_aInstance = new WebSpiderManager ();
  }

  private static final Logger s_aLogger = LoggerFactory.getLogger (WebSpiderManager.class);
  private final Map <String, WebSpiderInfo> m_aMap = new HashMap <String, WebSpiderInfo> ();

  private WebSpiderManager ()
  {
    _readSpiderList ("codelists/spiderlist.xml");
    _readSearchSpiders ("codelists/spiders_vbulletin.xml");
    _readSpiderList2 ("codelists/spiderlist2.xml");
  }

  @Nonnull
  private static String _getUnifiedID (@Nonnull final String sID)
  {
    return sID.toLowerCase (Locale.US);
  }

  private void _readSpiderList (final String sPath)
  {
    final IMicroDocument aDoc = MicroReader.readMicroXML (new ClassPathResource (sPath));
    for (final IMicroElement eSpider : aDoc.getDocumentElement ().getAllChildElements ("spider"))
    {
      final WebSpiderInfo aSpider = new WebSpiderInfo (eSpider.getAttributeValue ("ident"));
      aSpider.setName (MicroHelper.getChildTextContent (eSpider, "name"));
      final String sType = MicroHelper.getChildTextContent (eSpider, "type");
      final EWebSpiderType eType = EWebSpiderType.getFromIDOrNull (sType);
      if (sType != null && eType == null)
        s_aLogger.warn ("Unknown web spider type '" + sType + "'");
      aSpider.setType (eType);
      aSpider.setInfo (MicroHelper.getChildTextContent (eSpider, "info"));
      m_aMap.put (_getUnifiedID (aSpider.getID ()), aSpider);
    }
  }

  private void _readSearchSpiders (final String sPath)
  {
    final IMicroDocument aDoc = MicroReader.readMicroXML (new ClassPathResource (sPath));
    for (final IMicroElement eSpider : aDoc.getDocumentElement ().getAllChildElements ("spider"))
    {
      final String sID = eSpider.getAttributeValue ("ident");
      WebSpiderInfo aSpider = m_aMap.get (_getUnifiedID (sID));
      if (aSpider == null)
      {
        aSpider = new WebSpiderInfo (sID);
        aSpider.setName (MicroHelper.getChildTextContent (eSpider, "name"));
        m_aMap.put (_getUnifiedID (aSpider.getID ()), aSpider);
      }
    }
  }

  private void _readSpiderList2 (final String sPath)
  {
    final List <String> aList = new ArrayList <String> ();
    if (XMLListHandler.readList (new ClassPathResource (sPath), aList).isFailure ())
      throw new IllegalStateException ("Failed to read spiderlist2 from " + sPath);
    for (final String sSpider : aList)
    {
      final String sID = _getUnifiedID (sSpider);
      if (!m_aMap.containsKey (sID))
      {
        final WebSpiderInfo aSpider = new WebSpiderInfo (sID);
        aSpider.setName (sSpider);
        m_aMap.put (sID, aSpider);
      }
    }
  }

  @Nonnull
  public static WebSpiderManager getInstance ()
  {
    return SingletonHolder.s_aInstance;
  }

  @Nonnull
  @ReturnsMutableCopy
  public Collection <WebSpiderInfo> getAllKnownSpiders ()
  {
    return CollectionHelper.newList (m_aMap.values ());
  }

  @Nullable
  public WebSpiderInfo getWebSpiderFromUserAgent (@Nonnull final String sUserAgent)
  {
    // Search case insensitive (key set is lowercase!)
    final String sUserAgentLC = _getUnifiedID (sUserAgent);
    for (final Map.Entry <String, WebSpiderInfo> aEntry : m_aMap.entrySet ())
      if (sUserAgentLC.contains (aEntry.getKey ()))
        return aEntry.getValue ();

    return null;
  }
}
