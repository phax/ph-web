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

import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.string.StringHelper;
import com.helger.httpclient.HttpClientHelper;
import com.helger.httpclient.HttpClientResponseHelper;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonReader;

public class ResponseHandlerJson implements ResponseHandler <IJson>
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (ResponseHandlerJson.class);

  @Nullable
  public IJson handleResponse (final HttpResponse aHttpResponse) throws ClientProtocolException, IOException
  {
    final HttpEntity aEntity = HttpClientResponseHelper.RH_ENTITY.handleResponse (aHttpResponse);
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
  }
}
