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

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.xml.serialize.write.EXMLIncorrectCharacterHandling;
import com.helger.xml.serialize.write.EXMLSerializeIndent;
import com.helger.xml.serialize.write.IXMLWriterSettings;
import com.helger.xml.serialize.write.XMLWriterSettings;

/**
 * Constants for handling sitemaps.org sitemaps consistently.
 *
 * @author Philip Helger
 */
@Immutable
public final class CXMLSitemap
{
  /** The namespace URI for XML sitemap 0.9 files - both URL set and index! */
  public static final String XML_NAMESPACE_0_9 = "http://www.sitemaps.org/schemas/sitemap/0.9";

  /** Classpath relative path to the sitemap XSD file 0.9 */
  public static final String SCHEMA_SITEMAP_0_9 = "schemas/sitemap-0.9.xsd";

  /** Classpath relative path to the siteindex XSD file 0.9 */
  public static final String SCHEMA_SITEINDEX_0_9 = "schemas/siteindex-0.9.xsd";

  /** The file name for the sitemap entry file */
  public static final String SITEMAP_ENTRY_FILENAME = "sitemap.xml";

  /** The XML writer settings to be used */
  public static final IXMLWriterSettings XML_WRITER_SETTINGS = new XMLWriterSettings ().setIndent (EXMLSerializeIndent.NONE)
                                                                                       .setIncorrectCharacterHandling (EXMLIncorrectCharacterHandling.DO_NOT_WRITE_LOG_WARNING);

  @PresentForCodeCoverage
  private static final CXMLSitemap s_aInstance = new CXMLSitemap ();

  private CXMLSitemap ()
  {}
}
