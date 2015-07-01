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
package com.helger.web.sitemap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.joda.time.LocalDateTime;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.MicroDocument;
import com.helger.commons.microdom.serialize.MicroWriter;
import com.helger.commons.string.ToStringGenerator;
import com.helger.datetime.domain.IHasLastModificationDateTime;
import com.helger.datetime.util.PDTHelper;

/**
 * Represents a set of {@link XMLSitemapURL} objects.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class XMLSitemapURLSet implements IHasLastModificationDateTime, Serializable
{
  public static final int MAX_URLS_PER_FILE = 50000;
  public static final int MAX_FILE_SIZE = 10 * CGlobal.BYTES_PER_MEGABYTE;

  // 38: ?XML header
  // 60: <urlset xmlns...> element
  // 9: </urlset> element
  private static final int XML_HEADER_LENGTH = 38 + 60 + 9;

  private int m_nOutputLength = XML_HEADER_LENGTH;
  private final List <XMLSitemapURL> m_aURLs = new ArrayList <XMLSitemapURL> ();
  private LocalDateTime m_aPrevLastModification;
  private LocalDateTime m_aLastModification;

  public XMLSitemapURLSet ()
  {}

  public void addURL (@Nonnull final XMLSitemapURL aURL)
  {
    ValueEnforcer.notNull (aURL, "URL");
    m_aURLs.add (aURL);
    m_nOutputLength += aURL.getOutputLength ();

    // Has the URL a modification date?
    final LocalDateTime aURLLastModified = aURL.getLastModificationDateTime ();
    if (aURLLastModified != null)
    {
      // Is the URL modification later than the current maximum
      if (m_aLastModification == null || PDTHelper.isGreater (aURLLastModified, m_aLastModification))
      {
        m_aPrevLastModification = m_aLastModification;
        m_aLastModification = aURLLastModified;
      }
    }
  }

  void removeLastURL ()
  {
    final XMLSitemapURL aLastURL = m_aURLs.remove (m_aURLs.size () - 1);
    m_nOutputLength -= aLastURL.getOutputLength ();

    // In case the last URL was the one with the latest modification date, use
    // the previous latest time
    // This is a hack, as it works exactly one time but this method is called
    // exactly once so it exactly fits our needs!
    if (m_aLastModification != null)
    {
      // Has the URL a modification date?
      final LocalDateTime aURLLastModified = aLastURL.getLastModificationDateTime ();
      if (m_aLastModification.equals (aURLLastModified))
      {
        // Use the previous last modification
        m_aLastModification = m_aPrevLastModification;
      }
    }
  }

  /**
   * @return The number of contained URLs. Always &ge; 0.
   */
  @Nonnegative
  public int getURLCount ()
  {
    return m_aURLs.size ();
  }

  /**
   * @return The estimated output length of the result file. Without caching it
   *         is approximately 50 times faster than building the XML and checking
   *         the length. With caching it is a few thousand times faster!
   */
  @Nonnegative
  public int getOutputLength ()
  {
    return m_nOutputLength;
  }

  public boolean isMultiFileSitemap ()
  {
    return getURLCount () > MAX_URLS_PER_FILE || getOutputLength () > MAX_FILE_SIZE;
  }

  @Nonnull
  public XMLSitemapURL getURL (@Nonnegative final int nIndex)
  {
    return m_aURLs.get (nIndex);
  }

  /**
   * @return The maximum of the last modifications of all contained URLs. May be
   *         <code>null</code> if no contained URL has a modification date!
   */
  @Nullable
  public LocalDateTime getLastModificationDateTime ()
  {
    return m_aLastModification;
  }

  /**
   * @return The whole URL set as a single XML document. This works only for
   *         non-multi file sitemaps.
   * @throws IllegalStateException
   *         if this is a multi-file URL set
   */
  @Nonnull
  public IMicroDocument getAsDocument ()
  {
    if (isMultiFileSitemap ())
      throw new IllegalStateException ("Cannot convert a multi file sitemap to a single - invalid - document");

    final IMicroDocument ret = new MicroDocument ();
    final IMicroElement eUrlset = ret.appendElement (CXMLSitemap.XML_NAMESPACE_0_9, "urlset");
    for (final XMLSitemapURL aURL : m_aURLs)
      eUrlset.appendChild (aURL.getAsElement ());
    return ret;
  }

  @Nonnull
  public String getAsXMLString ()
  {
    // Important: No indent and align, because otherwise the calculated output
    // length would not be suitable
    return MicroWriter.getNodeAsString (getAsDocument (), CXMLSitemap.XML_WRITER_SETTINGS);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!(o instanceof XMLSitemapURLSet))
      return false;
    final XMLSitemapURLSet rhs = (XMLSitemapURLSet) o;
    // Don't compare the other fields as they are calculated
    return m_aURLs.equals (rhs.m_aURLs);
  }

  @Override
  public int hashCode ()
  {
    // Don't compare the other fields as they are calculated
    return new HashCodeGenerator (this).append (m_aURLs).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("URLs", m_aURLs)
                                       .appendIfNotNull ("lastModification", m_aLastModification)
                                       .appendIfNotNull ("prevLastModification", m_aPrevLastModification)
                                       .append ("outputLength", m_nOutputLength)
                                       .toString ();
  }
}
