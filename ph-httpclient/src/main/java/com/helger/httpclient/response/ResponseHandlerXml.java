/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.httpclient.response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.helger.commons.debug.GlobalDebug;
import com.helger.httpclient.HttpClientHelper;
import com.helger.xml.serialize.read.DOMReader;

/**
 * Convert a valid HTTP response to a DOM {@link Document} object.
 *
 * @author Philip Helger
 */
public class ResponseHandlerXml implements ResponseHandler <Document>
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ResponseHandlerXml.class);

  private final boolean m_bDebugMode;

  public ResponseHandlerXml ()
  {
    this (GlobalDebug.isDebugMode ());
  }

  public ResponseHandlerXml (final boolean bDebugMode)
  {
    m_bDebugMode = bDebugMode;
  }

  /**
   * @return <code>true</code> if debug mode is enabled, <code>false</code> if
   *         not.
   * @since 8.8.2
   */
  public boolean isDebugMode ()
  {
    return m_bDebugMode;
  }

  @Nullable
  public Document handleResponse (final HttpResponse aHttpResponse) throws ClientProtocolException, IOException
  {
    final HttpEntity aEntity = ResponseHandlerHttpEntity.INSTANCE.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");

    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);

    if (m_bDebugMode)
    {
      // Read all in String
      final String sXML = EntityUtils.toString (aEntity, aCharset);

      s_aLogger.info ("Got XML: <" + sXML + ">");

      Document ret = null;
      try
      {
        ret = DOMReader.readXMLDOM (sXML);
      }
      catch (final SAXException ex)
      {}
      if (ret == null)
        throw new IllegalArgumentException ("Failed to parse as XML: " + sXML);
      return ret;
    }

    // Read via reader to avoid duplication in memory
    final Reader aReader = new InputStreamReader (aEntity.getContent (), aCharset);
    try
    {
      return DOMReader.readXMLDOM (aReader);
    }
    catch (final SAXException ex)
    {
      throw new IllegalArgumentException ("Failed to parse as XML", ex);
    }
  }
}
