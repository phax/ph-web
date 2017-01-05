/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import java.io.File;

import org.junit.Test;

import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.file.FileOperations;
import com.helger.servlet.StaticServerInfo;

/**
 * Test class for class {@link XMLSitemapProvider}.
 *
 * @author Philip Helger
 */
public final class XMLSitemapProviderTest
{
  private static final boolean [] BOOLS = new boolean [] { true, false };

  static
  {
    // First set the default web server info
    if (!StaticServerInfo.isSet ())
      StaticServerInfo.init ("http", "localhost", 80, "/any");
  }

  @Test
  public void testWrite ()
  {
    for (final boolean bUseGZip : BOOLS)
    {
      // provider count
      assertEquals (4, XMLSitemapProvider.getProviderCount ());

      final File aTargetDir = new File ("xmlsitemap-provider-testdir");
      FileOperations.createDirIfNotExisting (aTargetDir);
      try
      {
        assertTrue (XMLSitemapProvider.createSitemapFiles (aTargetDir, bUseGZip).isSuccess ());
        assertTrue (FileHelper.existsFile (new File (aTargetDir, CXMLSitemap.SITEMAP_ENTRY_FILENAME)));

        // 3 URL sets are present
        final int nMax = 3;
        for (int nIndex = 0; nIndex < nMax; ++nIndex)
          assertTrue (FileHelper.existsFile (new File (aTargetDir,
                                                       XMLSitemapIndex.getSitemapFilename (nIndex, bUseGZip))));
        assertFalse (FileHelper.existsFile (new File (aTargetDir,
                                                      XMLSitemapIndex.getSitemapFilename (nMax, bUseGZip))));
      }
      finally
      {
        FileOperations.deleteDirRecursive (aTargetDir);
      }
    }
  }
}
