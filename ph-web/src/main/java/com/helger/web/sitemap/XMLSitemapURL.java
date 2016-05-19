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
package com.helger.web.sitemap;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.MicroElement;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.xml.serialize.write.EXMLCharMode;
import com.helger.commons.xml.serialize.write.XMLMaskHelper;
import com.helger.datetime.domain.IHasLastModificationDateTime;
import com.helger.datetime.util.PDTHelper;
import com.helger.datetime.util.PDTWebDateHelper;

/**
 * Represents a single URL within an XML URL set.
 *
 * @author Philip Helger
 */
@Immutable
public final class XMLSitemapURL implements IHasLastModificationDateTime, Serializable
{
  /** Maximum length of a single URL */
  public static final int LOCATION_MAX_LENGTH = 2048;
  public static final double MIN_PRIORITY = 0;
  public static final double MAX_PRIORITY = 1;
  public static final double DEFAULT_PRIORITY = 0.5;

  private static final String ELEMENT_URL = "url";
  private static final String ELEMENT_LOC = "loc";
  private static final String ELEMENT_LASTMOD = "lastmod";
  private static final String ELEMENT_CHANGEFREQ = "changefreq";
  private static final String ELEMENT_PRIORITY = "priority";

  private final String m_sLocation;
  private final LocalDateTime m_aLastModification;
  private final EXMLSitemapChangeFequency m_eChangeFreq;
  private final double m_dPriority;
  private final String m_sPriority;
  private final int m_nOutputLength;

  public XMLSitemapURL (@Nonnull final ISimpleURL aLocation)
  {
    this (aLocation, null);
  }

  public XMLSitemapURL (@Nonnull final ISimpleURL aLocation, @Nullable final LocalDateTime aLastModification)
  {
    this (aLocation, aLastModification, null, null);
  }

  public XMLSitemapURL (@Nonnull final ISimpleURL aLocation,
                        @Nullable final LocalDateTime aLastModification,
                        @Nullable final EXMLSitemapChangeFequency eChangeFreq,
                        @Nullable final Double aPriority)
  {
    ValueEnforcer.notNull (aLocation, "Location");
    if (aPriority != null)
      ValueEnforcer.isBetweenInclusive (aPriority.doubleValue (), "Priority", MIN_PRIORITY, MAX_PRIORITY);

    m_sLocation = aLocation.getAsStringWithEncodedParameters ();
    if (m_sLocation.length () > LOCATION_MAX_LENGTH)
      throw new IllegalArgumentException ("URL location is too long!");
    m_aLastModification = PDTHelper.isNullValue (aLastModification) ? null : aLastModification;
    m_eChangeFreq = eChangeFreq;
    m_dPriority = aPriority == null ? DEFAULT_PRIORITY : aPriority.doubleValue ();
    m_sPriority = aPriority == null ? null : aPriority.toString ();
    m_nOutputLength = _buildEstimatedOutputLength ();
  }

  /**
   * Get the length of a single tag name in XML representation for open AND
   * close together.
   *
   * @param s
   *        The tag name without leading and trailing angle brackets
   * @return The length in chars in XML representation
   */
  @Nonnegative
  private static int _getTagOutputLength (@Nonnull final String s)
  {
    // length + "<" + ">" + "<" + "/>"
    return s.length () * 2 + 5;
  }

  @Nonnegative
  private int _buildEstimatedOutputLength ()
  {
    // <url> element
    int ret = _getTagOutputLength (ELEMENT_URL);

    // <loc>
    ret += _getTagOutputLength (ELEMENT_LOC);
    ret += XMLMaskHelper.getMaskedXMLTextLength (CXMLSitemap.XML_WRITER_SETTINGS.getXMLVersion (),
                                                 EXMLCharMode.TEXT,
                                                 CXMLSitemap.XML_WRITER_SETTINGS.getIncorrectCharacterHandling (),
                                                 m_sLocation);

    if (m_aLastModification != null)
    {
      // 23 == length of formatted date
      // YYYY-MM-DDThh:mm:ss.sss
      ret += _getTagOutputLength (ELEMENT_LASTMOD) + 23;
    }

    if (m_eChangeFreq != null)
      ret += _getTagOutputLength (ELEMENT_CHANGEFREQ) + m_eChangeFreq.getText ().length ();

    if (m_sPriority != null)
      ret += _getTagOutputLength (ELEMENT_PRIORITY) + m_sPriority.length ();

    return ret;
  }

  @Nonnull
  public String getLocation ()
  {
    return m_sLocation;
  }

  @Nullable
  public LocalDateTime getLastModificationDateTime ()
  {
    return m_aLastModification;
  }

  @Nullable
  public EXMLSitemapChangeFequency getChangeFrequency ()
  {
    return m_eChangeFreq;
  }

  @Nonnegative
  public double getPriority ()
  {
    return m_dPriority;
  }

  @Nullable
  public String getPriorityString ()
  {
    return m_sPriority;
  }

  @Nonnegative
  public int getOutputLength ()
  {
    return m_nOutputLength;
  }

  @Nonnull
  public IMicroElement getAsElement ()
  {
    final String sNamespaceURI = CXMLSitemap.XML_NAMESPACE_0_9;
    final IMicroElement ret = new MicroElement (sNamespaceURI, ELEMENT_URL);
    ret.appendElement (sNamespaceURI, ELEMENT_LOC).appendText (m_sLocation);
    if (m_aLastModification != null)
      ret.appendElement (sNamespaceURI, ELEMENT_LASTMOD)
         .appendText (PDTWebDateHelper.getAsStringXSD (m_aLastModification));
    if (m_eChangeFreq != null)
      ret.appendElement (sNamespaceURI, ELEMENT_CHANGEFREQ).appendText (m_eChangeFreq.getText ());
    if (m_sPriority != null)
      ret.appendElement (sNamespaceURI, ELEMENT_PRIORITY).appendText (m_sPriority);
    return ret;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final XMLSitemapURL rhs = (XMLSitemapURL) o;
    // Don't compare output length and double priority as they are calculated
    return m_sLocation.equals (rhs.m_sLocation) &&
           EqualsHelper.equals (m_aLastModification, rhs.m_aLastModification) &&
           EqualsHelper.equals (m_eChangeFreq, rhs.m_eChangeFreq) &&
           EqualsHelper.equals (m_sPriority, rhs.m_sPriority);
  }

  @Override
  public int hashCode ()
  {
    // Don't compare output length and double priority as they are calculated
    return new HashCodeGenerator (this).append (m_sLocation)
                                       .append (m_aLastModification)
                                       .append (m_eChangeFreq)
                                       .append (m_sPriority)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("location", m_sLocation)
                                       .appendIfNotNull ("lastModification", m_aLastModification)
                                       .appendIfNotNull ("changeFrequency", m_eChangeFreq)
                                       .appendIfNotNull ("priority", m_sPriority)
                                       .append ("outputLength", m_nOutputLength)
                                       .toString ();
  }
}
