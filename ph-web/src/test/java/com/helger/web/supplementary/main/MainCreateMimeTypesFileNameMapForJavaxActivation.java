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
package com.helger.web.supplementary.main;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.charset.CCharset;
import com.helger.commons.collection.ext.ICommonsOrderedSet;
import com.helger.commons.collection.multimap.MultiHashMapLinkedHashSetBased;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.vendor.VendorInfo;
import com.helger.xml.util.mime.MimeTypeInfo;
import com.helger.xml.util.mime.MimeTypeInfoManager;

public final class MainCreateMimeTypesFileNameMapForJavaxActivation
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (MainCreateMimeTypesFileNameMapForJavaxActivation.class);

  /**
   * Create the mime.types file that is read by javax.activation. See class
   * javax.annotation.MimetypesFileTypeMap
   *
   * @param args
   *        ignore
   * @throws Exception
   *         if anything goes wrong
   */
  public static void main (final String [] args) throws Exception
  {
    final String sDestPath = "src/main/resources/META-INF/mime.types";
    Writer w = null;
    try
    {
      // build map from MimeType to list of extensions
      final MultiHashMapLinkedHashSetBased <String, String> aMap = new MultiHashMapLinkedHashSetBased <> ();

      for (final MimeTypeInfo aInfo : MimeTypeInfoManager.getDefaultInstance ().getAllMimeTypeInfos ())
        for (final String sExt : aInfo.getAllExtensions ())
        {
          // Skip the one empty extension!
          if (sExt.length () > 0)
            for (final String sMimeType : aInfo.getAllMimeTypeStrings ())
              aMap.putSingle (sMimeType, sExt);
        }

      // write file in format iso-8859-1!
      w = new PrintWriter (new File (sDestPath), CCharset.CHARSET_ISO_8859_1);

      // write header
      for (final String sLine : VendorInfo.getFileHeaderLines ())
        w.write ("# " + sLine + '\n');
      w.write ("#\n");
      w.write ("# Created on: " + ZonedDateTime.now (Clock.systemUTC ()).toString () + "\n");
      w.write ("# Created by: " + MainCreateMimeTypesFileNameMapForJavaxActivation.class.getName () + "\n");
      w.write ("#\n");

      // write MIME type mapping
      for (final Map.Entry <String, ICommonsOrderedSet <String>> aEntry : aMap.getSortedByKey (Comparator.naturalOrder ())
                                                                              .entrySet ())
        w.write ("type=" + aEntry.getKey () + " exts=" + StringHelper.getImploded (',', aEntry.getValue ()) + "\n");

      // done
      w.flush ();
      w.close ();
      s_aLogger.info ("Done creating " + sDestPath);
    }
    finally
    {
      StreamHelper.close (w);
    }
  }
}
