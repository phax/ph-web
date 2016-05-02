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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;

import com.helger.commons.error.IResourceErrorGroup;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.mock.CommonsTestHelper;
import com.helger.commons.url.SimpleURL;
import com.helger.commons.xml.schema.XMLSchemaValidationHelper;
import com.helger.commons.xml.transform.StringStreamSource;

/**
 * Test class for class {@link XMLSitemapURLSet}.
 *
 * @author Philip Helger
 */
public final class XMLSitemapURLSetTest
{
  @Test
  public void testGetOutputLength ()
  {
    final XMLSitemapURLSet s = new XMLSitemapURLSet ();
    s.addURL (new XMLSitemapURL (new SimpleURL ("http://orf.at")));
    assertEquals ("error: " + s.getAsXMLString (), s.getOutputLength (), s.getAsXMLString ().length ());
    s.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at/dir")));
    assertEquals ("error: " + s.getAsXMLString (), s.getOutputLength (), s.getAsXMLString ().length ());
    s.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at/dir?param=value"), LocalDateTime.now (), null, null));
    assertEquals ("error: " + s.getAsXMLString (), s.getOutputLength (), s.getAsXMLString ().length ());
    s.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at/dir?param=value&param2=value2"),
                                 LocalDateTime.now (),
                                 EXMLSitemapChangeFequency.NEVER,
                                 null));
    assertEquals ("error: " + s.getAsXMLString (), s.getOutputLength (), s.getAsXMLString ().length ());
    s.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at"),
                                 LocalDateTime.now (),
                                 EXMLSitemapChangeFequency.NEVER,
                                 Double.valueOf (1d / 9)));
    assertEquals ("error: " + s.getAsXMLString (), s.getOutputLength (), s.getAsXMLString ().length ());

    // Validate against the schema
    final IResourceErrorGroup aErrors = XMLSchemaValidationHelper.validate (new ClassPathResource (CXMLSitemap.SCHEMA_SITEMAP_0_9),
                                                                            new StringStreamSource (s.getAsXMLString ()));
    assertTrue (aErrors.toString (), aErrors.isEmpty ());
  }

  @Test
  public void testIsMultiFileSitemapCount ()
  {
    final XMLSitemapURLSet s = new XMLSitemapURLSet ();

    // Insert exactly the number of maximum URLs
    while (s.getURLCount () < XMLSitemapURLSet.MAX_URLS_PER_FILE)
      s.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at"),
                                   LocalDateTime.now (),
                                   EXMLSitemapChangeFequency.NEVER,
                                   null));
    assertFalse (s.isMultiFileSitemap ());

    // Add one more URL
    s.addURL (new XMLSitemapURL (new SimpleURL ("http://orf.at")));
    assertTrue (s.isMultiFileSitemap ());
  }

  @Test
  public void testIsMultiFileSitemapSize ()
  {
    final XMLSitemapURLSet s = new XMLSitemapURLSet ();
    final int nEmptyLength = s.getOutputLength ();

    // Build a very lengthy item
    final XMLSitemapURL aLongURL = new XMLSitemapURL (new SimpleURL ("http://www.myverlonghostnamethatisunreasoanble.com/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/directory/filename?param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value&param=value#anchor"),
                                                      LocalDateTime.now (),
                                                      EXMLSitemapChangeFequency.NEVER,
                                                      Double.valueOf (1d / 9));
    final int nURLLength = aLongURL.getOutputLength ();

    // How many of the lengthy items are needed to force an overflow?
    final int nExceedCount = ((XMLSitemapURLSet.MAX_FILE_SIZE - nEmptyLength) / nURLLength) + 1;
    assertEquals (17050, nExceedCount);
    assertTrue (nExceedCount < XMLSitemapURLSet.MAX_URLS_PER_FILE);

    // Add so many entries, that the limit is not exceeded
    for (int i = 0; i < (nExceedCount - 1); ++i)
      s.addURL (aLongURL);
    assertFalse (s.isMultiFileSitemap ());

    // Add one more URL -> overflow
    s.addURL (aLongURL);
    assertTrue (s.isMultiFileSitemap ());
  }

  @Test
  public void testStdMethods ()
  {
    final XMLSitemapURLSet s1 = new XMLSitemapURLSet ();
    final XMLSitemapURLSet s2 = new XMLSitemapURLSet ();
    for (int i = 0; i < 10; ++i)
    {
      s1.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at")));
      s2.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at")));
    }
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (s1, s2);
    s2.addURL (new XMLSitemapURL (new SimpleURL ("http://abc.at")));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (s1, s2);
  }
}
