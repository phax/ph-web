/**
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
package com.helger.httpclient.response;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.StringHelper;
import com.helger.httpclient.HttpClientHelper;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonReader;

/**
 * Convert a valid HTTP response to an {@link IJson} object.
 *
 * @author Philip Helger
 */
public class ResponseHandlerJson implements ResponseHandler <IJson>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseHandlerJson.class);

  private final boolean m_bDebugMode;

  public ResponseHandlerJson ()
  {
    this (false);
  }

  public ResponseHandlerJson (final boolean bDebugMode)
  {
    m_bDebugMode = bDebugMode;
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

  @Nullable
  public IJson handleResponse (@Nonnull final HttpResponse aHttpResponse) throws IOException
  {
    final HttpEntity aEntity = ResponseHandlerHttpEntity.INSTANCE.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");

    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);

    if (m_bDebugMode)
    {
      // Read all in String
      final String sJson = StringHelper.trim (EntityUtils.toString (aEntity, aCharset));

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Got JSON: <" + sJson + ">");

      final IJson ret = JsonReader.readFromString (sJson);
      if (ret == null)
        throw new IllegalArgumentException ("Failed to parse as JSON: " + sJson);
      return ret;
    }

    // Read via reader to avoid duplication in memory
    return JsonReader.builder ().setSource (aEntity.getContent (), aCharset).read ();
  }
}
