/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.network.supplementary.tools;

import java.io.File;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.network.port.ENetworkProtocol;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.serialize.MicroReader;
import com.helger.xml.microdom.util.MicroHelper;

/**
 * Source:
 * https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml
 *
 * @author Philip Helger
 */
public final class MainCreateDefaultPorts
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainCreateDefaultPorts.class);
  private static final String CHAR_ESCAPE = "\b\t\n\f\r\"\\";
  private static final String CHAR_MACRO = "btnfr\"\\";

  private static final String _id (@Nonnull final String sStr)
  {
    final int n = sStr.length ();
    final StringBuilder aSB = new StringBuilder (sStr.length ());
    for (int i = 0; i < n; ++i)
    {
      final char c = sStr.charAt (i);
      if (i == 0)
      {
        if (Character.isJavaIdentifierStart (c))
          aSB.append (c);
        else
          aSB.append ('_');
      }
      else
      {
        if (Character.isJavaIdentifierPart (c))
          aSB.append (c);
        else
          aSB.append ('_');
      }
    }
    return aSB.toString ();
  }

  @Nonnull
  private static final String _quote (@Nonnull final String sStr)
  {
    final int n = sStr.length ();
    final StringBuilder aSB = new StringBuilder (n + 2);
    aSB.append ('"');
    for (final char c : sStr.toCharArray ())
    {
      final int j = CHAR_ESCAPE.indexOf (c);
      if (j >= 0)
      {
        aSB.append ('\\');
        aSB.append (CHAR_MACRO.charAt (j));
      }
      else
      {
        aSB.append (c);
      }
    }
    aSB.append ('"');
    return aSB.toString ();
  }

  public static void main (final String [] args) throws Exception
  {
    final StringBuilder aSB = new StringBuilder ();
    final IMicroDocument aDoc = MicroReader.readMicroXML (new File ("src/test/resources/Service Name and Transport Protocol Port Number Registry.xml"));
    for (final IMicroElement aRecord : aDoc.getDocumentElement ().getAllChildElements ("record"))
    {
      final String sProtocol = MicroHelper.getChildTextContent (aRecord, "protocol");
      final ENetworkProtocol eProtocol = ENetworkProtocol.getFromIDOrNull (sProtocol);
      if (eProtocol == null)
      {
        // E.g. sctp
        continue;
      }

      final String sNumber = MicroHelper.getChildTextContent (aRecord, "number");
      if (sNumber == null)
        continue;

      int nMin;
      int nMax;
      if (sNumber.indexOf ('-') > 0)
      {
        final String [] aParts = StringHelper.getExplodedArray ('-', sNumber, 2);
        nMin = StringParser.parseInt (aParts[0], -1);
        if (nMin < 0)
          throw new IllegalStateException ("From part of '" + sNumber + "' is unknown");
        nMax = StringParser.parseInt (aParts[1], -1);
        if (nMax < 0)
          throw new IllegalStateException ("To part of '" + sNumber + "' is unknown");
      }
      else
      {
        final int nNumber = StringParser.parseInt (sNumber, -1);
        if (nNumber < 0)
          throw new IllegalStateException ("Number '" + sNumber + "' is unknown");
        nMin = nMax = nNumber;
      }
      final String sName = StringHelper.getNotNull (MicroHelper.getChildTextContent (aRecord, "name"));
      final String sDescription = StringHelper.getNotNull (MicroHelper.getChildTextContent (aRecord, "description"));
      if ("Unassigned".equals (sDescription))
        continue;
      if (sDescription.contains ("IANA assigned this well-formed service name as a replacement for"))
        continue;

      for (int nPort = nMin; nPort <= nMax; ++nPort)
      {
        if (nPort < 1024)
          aSB.append ("public static final INetworkPort " +
                      eProtocol.name () +
                      "_" +
                      nPort +
                      (StringHelper.hasText (sName) ? "_" + _id (sName) : "") +
                      " = _registerPort (" +
                      nPort +
                      ", ENetworkProtocol." +
                      eProtocol.name () +
                      ", " +
                      _quote (sName) +
                      ", " +
                      _quote (sDescription) +
                      ");\n");
      }
    }
    LOGGER.info (aSB.toString ());
  }
}
