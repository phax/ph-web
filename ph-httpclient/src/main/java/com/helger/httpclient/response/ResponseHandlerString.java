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
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.mime.IMimeType;
import com.helger.httpclient.HttpClientHelper;

/**
 * Convert a valid HTTP response to a simple String object using the provided
 * charset. The fallback content type used internally is text/plain with charset
 * iso-8859-1.
 *
 * @author Philip Helger
 */
public class ResponseHandlerString implements HttpClientResponseHandler <String>
{
  private final ContentType m_aDefault;
  private Charset m_aFallbackCharset;
  private Consumer <Charset> m_aCharsetConsumer;

  @Deprecated (since = "10.0.0", forRemoval = true)
  public ResponseHandlerString ()
  {
    // text/plain with ISO-8859-1
    this (ContentType.DEFAULT_TEXT);
  }

  public ResponseHandlerString (@Nonnull final IMimeType aMimeType, @Nonnull final Charset aCharset)
  {
    this (ContentType.create (aMimeType.getAsString (), aCharset));
  }

  public ResponseHandlerString (@Nonnull final ContentType aDefault)
  {
    ValueEnforcer.notNull (aDefault, "Default");
    ValueEnforcer.notNull (aDefault.getCharset (), "DefaultContentType.Charset");
    m_aDefault = aDefault;
    m_aFallbackCharset = aDefault.getCharset ();
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

  /**
   * @return The fallback charset to be used, in case no charset can be
   *         determined from the content. By default this is the HTTP default
   *         charset. Never <code>null</code>.
   * @see #setFallbackCharset(Charset)
   * @since 9.7.2
   */
  @Nonnull
  public final Charset getFallbackCharset ()
  {
    return m_aFallbackCharset;
  }

  /**
   * Set the fallback charset to be used, if the payload has no charset. By
   * default the fallback charset is the charset of the Content Type.
   *
   * @param aFallbackCharset
   *        The fallback charset to be used. May not be <code>null</code>.
   * @return this for chaining
   * @since 9.7.2
   */
  @Nonnull
  public final ResponseHandlerString setFallbackCharset (@Nonnull final Charset aFallbackCharset)
  {
    ValueEnforcer.notNull (aFallbackCharset, "FallbackCharset");
    m_aFallbackCharset = aFallbackCharset;
    return this;
  }

  @Nullable
  public final Consumer <Charset> getCharsetConsumer ()
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
  public final ResponseHandlerString setCharsetConsumer (@Nullable final Consumer <Charset> aCharsetConsumer)
  {
    m_aCharsetConsumer = aCharsetConsumer;
    return this;
  }

  @Nullable
  public String handleResponse (@Nonnull final ClassicHttpResponse aHttpResponse) throws IOException
  {
    // Convert to entity
    final HttpEntity aEntity = ResponseHandlerHttpEntity.INSTANCE.handleResponse (aHttpResponse);
    if (aEntity == null)
      return null;

    final ContentType aContentType = HttpClientHelper.getContentTypeOrDefault (aEntity, m_aDefault);

    // Get the charset from the content type or the default charset
    final Charset aCharset = HttpClientHelper.getCharset (aContentType, m_aFallbackCharset);

    // Get the default charset to be used
    if (m_aCharsetConsumer != null)
      m_aCharsetConsumer.accept (aCharset);

    return HttpClientHelper.entityToString (aEntity, aCharset);
  }
}
