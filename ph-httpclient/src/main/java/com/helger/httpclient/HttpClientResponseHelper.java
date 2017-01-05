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
package com.helger.httpclient;

import javax.annotation.concurrent.Immutable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.helger.commons.debug.GlobalDebug;

/**
 * This class contains some default response handler for basic data types that
 * handles status codes appropriately.
 *
 * @author Philip Helger
 */
@Immutable
public final class HttpClientResponseHelper
{
  public static final ResponseHandler <HttpEntity> RH_ENTITY = aHttpResponse -> {
    final StatusLine aStatusLine = aHttpResponse.getStatusLine ();
    final HttpEntity aEntity = aHttpResponse.getEntity ();
    if (aStatusLine.getStatusCode () >= 300)
    {
      EntityUtils.consume (aEntity);
      String sMessage = aStatusLine.getReasonPhrase () + " [" + aStatusLine.getStatusCode () + "]";
      if (GlobalDebug.isDebugMode ())
      {
        sMessage += "\nAll " + aHttpResponse.getAllHeaders ().length + " headers returned";
        for (final Header aHeader : aHttpResponse.getAllHeaders ())
          sMessage += "\n  " + aHeader.getName () + "=" + aHeader.getValue ();
      }
      throw new HttpResponseException (aStatusLine.getStatusCode (), sMessage);
    }
    return aEntity;
  };

  private HttpClientResponseHelper ()
  {}
}
