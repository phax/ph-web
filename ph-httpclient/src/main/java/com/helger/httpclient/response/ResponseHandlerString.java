/**
 * Copyright (C) 2016-2020 Philip Helger (www.helger.com)
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
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.functional.IConsumer;
import com.helger.httpclient.HttpClientHelper;

/**
 * Convert a valid HTTP response to a simple String object using the provided
 * charset. The fallback content type used internally is text/plain with charset
 * iso-8859-1.
 *
 * @author Philip Helger
 */
public class ResponseHandlerString implements ResponseHandler <String>
{
  private final ContentType m_aDefault;
  private IConsumer <Charset> m_aCharsetConsumer;

  public ResponseHandlerString ()
  {
    // text/plain with ISO-8859-1
    this (ContentType.DEFAULT_TEXT);
  }

  public ResponseHandlerString (@Nonnull final ContentType aDefault)
  {
    ValueEnforcer.notNull (aDefault, "Default");
    ValueEnforcer.notNull (aDefault.getCharset (), "DefaultContentType.Charset");
    m_aDefault = aDefault;
  }

  @Nonnull
  public final ContentType getDefaultContentType ()
  {
    return m_aDefault;
  }

  @Nonnull
  public final Charset getDefaultCharset ()
  {
    return m_aDefault.getCharset ();
  }

  @Nullable
  public final IConsumer <Charset> getCharsetConsumer ()
  {
    return m_aCharsetConsumer;
  }

  /**
   * Set the charset consumer that is informed about the default character set
   * in which the response is interpreted.
   *
   * @param aCharsetConsumer
   *        The charset consumer. May be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public final ResponseHandlerString setCharsetConsumer (@Nullable final IConsumer <Charset> aCharsetConsumer)
  {
    m_aCharsetConsumer = aCharsetConsumer;
    return this;
  }

  @Nullable
  public String handleResponse (@Nonnull final HttpResponse aHttpResponse) throws IOException
  {
    // Convert to entity
    final HttpEntity aEntity = ResponseHandlerHttpEntity.INSTANCE.handleResponse (aHttpResponse);
    if (aEntity == null)
      return null;

    ContentType aContentType = ContentType.get (aEntity);
    if (aContentType == null)
      aContentType = m_aDefault;

    // Get the charset from the content type or the default charset
    final Charset aCharset = HttpClientHelper.getCharset (aContentType, m_aDefault.getCharset ());

    // Get the default charset to be used
    if (m_aCharsetConsumer != null)
      m_aCharsetConsumer.accept (aCharset);

    return EntityUtils.toString (aEntity, aCharset);
  }
}
