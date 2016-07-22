/**
 * Copyright (C) 2016 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.annotation.concurrent.Immutable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.string.StringHelper;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonReader;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.serialize.MicroReader;
import com.helger.xml.serialize.read.DOMReader;

/**
 * This class contains some default response handler for basic data types that
 * handles status codes appropriately.
 * 
 * @author Philip Helger
 */
@Immutable
public final class HttpClientResponseHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (HttpClientResponseHelper.class);

  private static final ResponseHandler <HttpEntity> _RH_ENTITY = aHttpResponse -> {
    final StatusLine aStatusLine = aHttpResponse.getStatusLine ();
    final HttpEntity aEntity = aHttpResponse.getEntity ();
    if (aStatusLine.getStatusCode () >= 300)
    {
      EntityUtils.consume (aEntity);
      String sMessage = aStatusLine.getReasonPhrase () + " [" + aStatusLine.getStatusCode () + "]";
      if (GlobalDebug.isDebugMode ())
      {
        sMessage += "\n" + aHttpResponse.getAllHeaders ().length + " headers returned";
        for (final Header aHeader : aHttpResponse.getAllHeaders ())
          sMessage += "\n  " + aHeader.getName () + "=" + aHeader.getValue ();
      }
      throw new HttpResponseException (aStatusLine.getStatusCode (), sMessage);
    }
    return aEntity;
  };

  public static final ResponseHandler <byte []> RH_BYTE_ARRAY = aHttpResponse -> {
    final HttpEntity aEntity = _RH_ENTITY.handleResponse (aHttpResponse);
    if (aEntity == null)
      return null;
    return EntityUtils.toByteArray (aEntity);
  };

  public static final ResponseHandler <String> RH_STRING = aHttpResponse -> {
    final HttpEntity aEntity = _RH_ENTITY.handleResponse (aHttpResponse);
    if (aEntity == null)
      return null;
    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);
    return EntityUtils.toString (aEntity, aCharset);
  };

  public static final ResponseHandler <IJson> RH_JSON = aHttpResponse -> {
    final HttpEntity aEntity = _RH_ENTITY.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");
    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);

    if (GlobalDebug.isDebugMode ())
    {
      // Read all in String
      final String sJson = StringHelper.trim (EntityUtils.toString (aEntity, aCharset));

      s_aLogger.info ("Got JSON: <" + sJson + ">");

      final IJson ret = JsonReader.readFromString (sJson);
      if (ret == null)
        throw new IllegalArgumentException ("Failed to parse as JSON: " + sJson);
      return ret;
    }

    // Read via reader to avoid duplication in memory
    final Reader aReader = new InputStreamReader (aEntity.getContent (), aCharset);
    return JsonReader.readFromReader (aReader);
  };

  public static final ResponseHandler <IMicroDocument> RH_MICRODOM = aHttpResponse -> {
    final HttpEntity aEntity = _RH_ENTITY.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");
    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);

    if (GlobalDebug.isDebugMode ())
    {
      // Read all in String
      final String sXML = EntityUtils.toString (aEntity, aCharset);

      s_aLogger.info ("Got XML: <" + sXML + ">");

      final IMicroDocument ret = MicroReader.readMicroXML (sXML);
      if (ret == null)
        throw new IllegalArgumentException ("Failed to parse as XML: " + sXML);
      return ret;
    }

    // Read via reader to avoid duplication in memory
    final Reader aReader = new InputStreamReader (aEntity.getContent (), aCharset);
    return MicroReader.readMicroXML (aReader);
  };

  public static final ResponseHandler <Document> RH_XML = aHttpResponse -> {
    final HttpEntity aEntity = _RH_ENTITY.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");
    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);

    if (GlobalDebug.isDebugMode ())
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
  };

  private HttpClientResponseHelper ()
  {}
}
