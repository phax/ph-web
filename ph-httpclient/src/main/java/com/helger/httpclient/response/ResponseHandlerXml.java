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
package com.helger.httpclient.response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.httpclient.HttpClientHelper;
import com.helger.xml.serialize.read.DOMReader;

/**
 * Convert a valid HTTP response to a DOM {@link Document} object.
 *
 * @author Philip Helger
 */
public class ResponseHandlerXml implements HttpClientResponseHandler <Document>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseHandlerXml.class);

  private boolean m_bDebugMode;

  public ResponseHandlerXml ()
  {
    this (false);
  }

  @Deprecated (forRemoval = true, since = "10.3.1")
  public ResponseHandlerXml (final boolean bDebugMode)
  {
    setDebugMode (bDebugMode);
  }

  /**
   * @return <code>true</code> if debug mode is enabled, <code>false</code> if
   *         not.
   * @since 8.8.2
   */
  public final boolean isDebugMode ()
  {
    return m_bDebugMode;
  }

  /**
   * Enable or disable debug mode on demand.
   *
   * @param bDebugMode
   *        <code>true</code> to enable debug mode, <code>false</code> to
   *        disable it.
   * @return this for chaining
   * @since 10.0.0
   */
  @Nonnull
  public final ResponseHandlerXml setDebugMode (final boolean bDebugMode)
  {
    m_bDebugMode = bDebugMode;
    return this;
  }

  @Nullable
  public Document handleResponse (@Nonnull final ClassicHttpResponse aHttpResponse) throws IOException
  {
    final HttpEntity aEntity = ResponseHandlerHttpEntity.INSTANCE.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");

    if (m_bDebugMode)
    {
      final ContentType aContentType = HttpClientHelper.getContentTypeOrDefault (aEntity);
      // Assume UTF-8 as default for XML
      final Charset aCharset = HttpClientHelper.getCharset (aContentType, StandardCharsets.UTF_8);

      // Read all in String
      final String sXML = HttpClientHelper.entityToString (aEntity, aCharset);

      LOGGER.info ("Got XML in [" + aCharset + "]: <" + sXML + ">");

      final Document ret = DOMReader.readXMLDOM (sXML);
      if (ret == null)
        throw new IllegalArgumentException ("Failed to parse as XML: " + sXML);
      return ret;
    }

    // Ignore charset
    return DOMReader.readXMLDOM (aEntity.getContent ());
  }
}
