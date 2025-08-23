/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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

import com.helger.annotation.style.IsSPIImplementation;
import com.helger.url.URLBuilder;

import jakarta.annotation.Nonnull;

@IsSPIImplementation
public final class MockXMLSitemapProviderSPI implements IXMLSitemapProviderSPI
{
  @Nonnull
  public XMLSitemapURLSet createURLSet ()
  {
    final XMLSitemapURLSet ret = new XMLSitemapURLSet ();
    for (int i = 0; i < 10; ++i)
      ret.addURL (new XMLSitemapURL (new URLBuilder ().path ("http://www.helger.com").addParam ("xx", i).build ()));
    return ret;
  }
}
