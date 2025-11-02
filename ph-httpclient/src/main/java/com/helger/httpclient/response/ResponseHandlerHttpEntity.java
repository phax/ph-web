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

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.StatusLine;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.http.CHttp;

/**
 * Base response handler that checks the status code and handles only status
 * codes &lt; 300.
 *
 * @author Philip Helger
 * @since 8.7.2
 */
public class ResponseHandlerHttpEntity implements HttpClientResponseHandler <HttpEntity>
{
  /** The global default instance. */
  public static final ResponseHandlerHttpEntity INSTANCE = new ResponseHandlerHttpEntity ();

  protected ResponseHandlerHttpEntity ()
  {}

  @Nullable
  public HttpEntity handleResponse (@NonNull final ClassicHttpResponse aHttpResponse) throws IOException
  {
    final StatusLine aStatusLine = new StatusLine (aHttpResponse);
    final HttpEntity aEntity = aHttpResponse.getEntity ();
    // >= 300
    if (aStatusLine.getStatusCode () >= CHttp.HTTP_MULTIPLE_CHOICES)
    {
      // Consume entity and throw
      throw ExtendedHttpResponseException.create (aStatusLine, aHttpResponse, aEntity);
    }
    return aEntity;
  }
}
