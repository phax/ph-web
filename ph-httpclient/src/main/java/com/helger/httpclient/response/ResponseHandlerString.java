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
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.helger.httpclient.HttpClientHelper;
import com.helger.httpclient.HttpClientResponseHelper;

public class ResponseHandlerString implements ResponseHandler <String>
{
  @Nullable
  public String handleResponse (final HttpResponse aHttpResponse) throws ClientProtocolException, IOException
  {
    final HttpEntity aEntity = HttpClientResponseHelper.RH_ENTITY.handleResponse (aHttpResponse);
    if (aEntity == null)
      return null;
    final ContentType aContentType = ContentType.getOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);
    return EntityUtils.toString (aEntity, aCharset);
  }
}
