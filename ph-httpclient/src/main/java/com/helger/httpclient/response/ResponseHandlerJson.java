/*
 * Copyright (C) 2016-2023 Philip Helger (www.helger.com)
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

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.StringHelper;
import com.helger.httpclient.HttpClientHelper;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonReader;

/**
 * Convert a valid HTTP response to an {@link IJson} object.
 *
 * @author Philip Helger
 */
public class ResponseHandlerJson implements HttpClientResponseHandler <IJson>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseHandlerJson.class);

  private boolean m_bDebugMode;
  private Charset m_aFallbackCharset = HttpClientHelper.DEF_CONTENT_CHARSET;

  public ResponseHandlerJson ()
  {
    this (false);
  }

  public ResponseHandlerJson (final boolean bDebugMode)
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
   * @since 9.6.3
   */
  @Nonnull
  public final ResponseHandlerJson setDebugMode (final boolean bDebugMode)
  {
    m_bDebugMode = bDebugMode;
    return this;
  }

  /**
   * @return The fallback charset to be used, in case no charset can be
   *         determined from the content. By default this is the HTTP default
   *         charset. Never <code>null</code>.
   * @since 9.6.3
   */
  @Nonnull
  public final Charset getFallbackCharset ()
  {
    return m_aFallbackCharset;
  }

  /**
   * Set the fallback charset to be used, if the payload has no charset.
   *
   * @param aFallbackCharset
   *        The fallback charset to be used. May not be <code>null</code>.
   * @return this for chaining
   * @since 9.6.3
   */
  @Nonnull
  public final ResponseHandlerJson setFallbackCharset (@Nonnull final Charset aFallbackCharset)
  {
    ValueEnforcer.notNull (aFallbackCharset, "FallbackCharset");
    m_aFallbackCharset = aFallbackCharset;
    return this;
  }

  @Nullable
  public IJson handleResponse (@Nonnull final ClassicHttpResponse aHttpResponse) throws IOException
  {
    final HttpEntity aEntity = ResponseHandlerHttpEntity.INSTANCE.handleResponse (aHttpResponse);
    if (aEntity == null)
      throw new ClientProtocolException ("Response contains no content");

    final ContentType aContentType = HttpClientHelper.getContentTypeOrDefault (aEntity);
    final Charset aCharset = HttpClientHelper.getCharset (aContentType, m_aFallbackCharset);

    if (m_bDebugMode)
    {
      // Read all in String
      final String sJson = StringHelper.trim (HttpClientHelper.entityToString (aEntity, aCharset));

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Got JSON in [" + aCharset + "]: <" + sJson + ">");

      final IJson ret = JsonReader.readFromString (sJson);
      if (ret == null)
        throw new IllegalArgumentException ("Failed to parse as JSON: " + sJson);
      return ret;
    }

    // Read via reader to avoid duplication in memory
    return JsonReader.builder ().source (aEntity.getContent (), aCharset).read ();
  }
}
