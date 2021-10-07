/*
 * Copyright (C) 2016-2021 Philip Helger (www.helger.com)
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
package com.helger.sitemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.helger.commons.error.list.IErrorList;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.file.FileOperations;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.url.SimpleURL;
import com.helger.xml.schema.XMLSchemaValidationHelper;
import com.helger.xml.transform.StringStreamSource;

/**
 * Test class for class {@link XMLSitemapIndex}.
 *
 * @author Philip Helger
 */
public final class XMLSitemapIndexTest
{
  private static final String FULL_SERVER_CONTEXT_PATH = "http://localhost:80/any";

  private static void _testWriteXMLSitemapeIndex (@Nonnull final XMLSitemapIndex x)
  {
    final File aBaseDir = new File ("xmlsitemaps-testdir");
    FileOperations.createDirIfNotExisting (aBaseDir);
    try
    {
      x.writeToDisk (aBaseDir, FULL_SERVER_CONTEXT_PATH);
      assertTrue (FileHelper.existsFile (new File (aBaseDir, CXMLSitemap.SITEMAP_ENTRY_FILENAME)));
      final int nMax = x.getURLSetCount ();
      for (int i = 0; i < nMax; ++i)
        assertTrue (FileHelper.existsFile (new File (aBaseDir, x.getSitemapFilename (i))));
      assertFalse (FileHelper.existsFile (new File (aBaseDir, x.getSitemapFilename (nMax))));
    }
    finally
    {
      FileOperations.deleteDirRecursive (aBaseDir);
    }
  }

  @Test
  public void testValid ()
  {
    final XMLSitemapIndex x = new XMLSitemapIndex ();
    assertEquals (0, x.getURLSetCount ());
    assertNotNull (x.getAsDocument (FULL_SERVER_CONTEXT_PATH));

    final XMLSitemapURLSet s1 = new XMLSitemapURLSet ();
    s1.addURL (new XMLSitemapURL (new SimpleURL ("http://www.helger.com")));
    x.addURLSet (s1);
    assertEquals (1, x.getURLSetCount ());
    assertNotNull (x.getAsDocument (FULL_SERVER_CONTEXT_PATH));

    final XMLSitemapURLSet s2 = new XMLSitemapURLSet ();
    s2.addURL (new XMLSitemapURL (new SimpleURL ("http://www.google.at")));
    x.addURLSet (s2);
    assertEquals (2, x.getURLSetCount ());
    assertNotNull (x.getAsDocument (FULL_SERVER_CONTEXT_PATH));

    // Validate index against the schema
    final IErrorList aErrors = XMLSchemaValidationHelper.validate (new ClassPathResource (CXMLSitemap.SCHEMA_SITEINDEX_0_9),
                                                                   new StringStreamSource (x.getAsXMLString (FULL_SERVER_CONTEXT_PATH)));
    assertTrue (aErrors.toString (), aErrors.isEmpty ());

    _testWriteXMLSitemapeIndex (x);
  }

  @Test
  public void testAddMultiFileURLSet1 ()
  {
    final XMLSitemapIndex x = new XMLSitemapIndex ();

    // Create a very large URL set
    final XMLSitemapURLSet s = new XMLSitemapURLSet ();
    for (int i = 0; i < XMLSitemapURLSet.MAX_URLS_PER_FILE + 1; ++i)
      s.addURL (new XMLSitemapURL (new SimpleURL ("http://www.helger.com?x=" + i)));

    // And this must split up into 2 URL sets!
    x.addURLSet (s);
    assertEquals (2, x.getURLSetCount ());

    _testWriteXMLSitemapeIndex (x);
  }

  @Test
  public void testAddMultiFileURLSet2 ()
  {
    final XMLSitemapIndex x = new XMLSitemapIndex ();

    // Create a very large URL set
    final XMLSitemapURLSet s = new XMLSitemapURLSet ();
    for (int i = 0; i < XMLSitemapURLSet.MAX_URLS_PER_FILE * 2; ++i)
      s.addURL (new XMLSitemapURL (new SimpleURL ("http://www.helger.com?x=" + i)));

    // And this must split up into 2 URL sets!
    x.addURLSet (s);
    assertEquals (2, x.getURLSetCount ());

    _testWriteXMLSitemapeIndex (x);
  }
}
