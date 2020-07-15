/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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

import java.io.File;
import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.lang.ServiceLoaderHelper;
import com.helger.commons.state.ESuccess;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class manages the implementations of {@link IXMLSitemapProviderSPI}.
 *
 * @author Philip Helger
 */
@Immutable
public final class XMLSitemapProvider
{
  private static final Logger LOGGER = LoggerFactory.getLogger (XMLSitemapProvider.class);
  private static final ICommonsList <IXMLSitemapProviderSPI> s_aProviders;

  static
  {
    s_aProviders = ServiceLoaderHelper.getAllSPIImplementations (IXMLSitemapProviderSPI.class);
  }

  @PresentForCodeCoverage
  private static final XMLSitemapProvider s_aInstance = new XMLSitemapProvider ();

  private XMLSitemapProvider ()
  {}

  @Nonnegative
  public static int getProviderCount ()
  {
    return s_aProviders.size ();
  }

  /**
   * Create URL sets from every provider and invoke the provided consumer with
   * it.
   *
   * @param aConsumer
   *        The consumer to be invoked. Must be able to handle <code>null</code>
   *        and empty values. May itself not be <code>null</code>.
   */
  public static void forEachURLSet (@Nonnull final Consumer <? super XMLSitemapURLSet> aConsumer)
  {
    ValueEnforcer.notNull (aConsumer, "Consumer");
    for (final IXMLSitemapProviderSPI aSPI : s_aProviders)
    {
      final XMLSitemapURLSet aURLSet = aSPI.createURLSet ();
      aConsumer.accept (aURLSet);
    }
  }

  @Nonnull
  public static ESuccess createSitemapFiles (@Nonnull final File aTargetDirectory, @Nonnull @Nonempty final String sFullContextPath)
  {
    return createSitemapFiles (aTargetDirectory, XMLSitemapIndex.DEFAULT_USE_GZIP, sFullContextPath);
  }

  @Nonnull
  @SuppressFBWarnings ("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  public static ESuccess createSitemapFiles (@Nonnull final File aTargetDirectory,
                                             final boolean bUseGZip,
                                             @Nonnull @Nonempty final String sFullContextPath)
  {
    ValueEnforcer.notNull (aTargetDirectory, "TargetDirectory");
    ValueEnforcer.isTrue (FileHelper.existsDir (aTargetDirectory),
                          () -> "The passed file is not an existing directory: " + aTargetDirectory);

    // Any provider present?
    if (s_aProviders.isEmpty ())
      return ESuccess.SUCCESS;

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Writing XML sitemap files for " + s_aProviders.size () + " providers");

    // Start creating the index
    final XMLSitemapIndex aIndex = new XMLSitemapIndex (bUseGZip);
    for (final IXMLSitemapProviderSPI aSPI : s_aProviders)
    {
      final XMLSitemapURLSet aURLSet = aSPI.createURLSet ();
      if (aURLSet == null)
      {
        if (LOGGER.isWarnEnabled ())
          LOGGER.warn ("SPI implementation " + aSPI + " returned a null sitemap URL set!");
        continue;
      }
      if (aURLSet.getURLCount () > 0)
        aIndex.addURLSet (aURLSet);
      else
      {
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("SPI implementation " + aSPI + " returned an empty URL set!");
      }
    }

    // Did we get any URL set back?
    if (aIndex.getURLSetCount () == 0)
    {
      LOGGER.error ("No SPI implementation did deliver a valid URL set -> not doing anything!");
      return ESuccess.FAILURE;
    }

    // Main write to disk action
    return aIndex.writeToDisk (aTargetDirectory, sFullContextPath);
  }
}
