/*
 * Copyright (C) 2016-2022 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.codec.URLCodec;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.network.proxy.config.HttpProxyConfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Some utility methods for creating and handling Apache httpclient objects.
 *
 * @author Philip Helger
 */
@Immutable
@SuppressFBWarnings ("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public final class HttpClientHelper
{
  public static final Charset DEF_CONTENT_CHARSET = StandardCharsets.ISO_8859_1;
  public static final Charset DEF_PROTOCOL_CHARSET = StandardCharsets.US_ASCII;

  private HttpClientHelper ()
  {}

  @Nonnull
  public static HttpUriRequestBase createRequest (@Nonnull final EHttpMethod eHTTPMethod,
                                                  @Nonnull final ISimpleURL aSimpleURL)
  {
    final String sURI = aSimpleURL.getAsStringWithEncodedParameters ();
    return createRequest (eHTTPMethod, sURI);
  }

  @Nonnull
  public static HttpUriRequestBase createRequest (@Nonnull final EHttpMethod eHTTPMethod, @Nonnull final String sURI)
  {
    switch (eHTTPMethod)
    {
      case DELETE:
        return new HttpDelete (sURI);
      case GET:
        return new HttpGet (sURI);
      case HEAD:
        return new HttpHead (sURI);
      case OPTIONS:
        return new HttpOptions (sURI);
      case TRACE:
        return new HttpTrace (sURI);
      case PATCH:
        return new HttpPatch (sURI);
      case POST:
        return new HttpPost (sURI);
      case PUT:
        return new HttpPut (sURI);
      case CONNECT:
      default:
        throw new IllegalStateException ("Unsupported HTTP method: " + eHTTPMethod);
    }
  }

  @Nullable
  public static ContentType createContentType (@Nullable final String sContentType, @Nullable final Charset aCharset)
  {
    if (StringHelper.hasNoText (sContentType))
      return null;
    return ContentType.create (sContentType, aCharset);
  }

  @Nonnull
  public static Charset getCharset (@Nonnull final ContentType aContentType)
  {
    return getCharset (aContentType, DEF_CONTENT_CHARSET);
  }

  @Nullable
  public static Charset getCharset (@Nonnull final ContentType aContentType, @Nullable final Charset aDefault)
  {
    final Charset ret = aContentType.getCharset ();
    return ret != null ? ret : aDefault;
  }

  @Nullable
  public static HttpHost createHttpHost (@Nullable final Proxy aProxy)
  {
    if (aProxy != null && aProxy.type () == Proxy.Type.HTTP)
    {
      if (aProxy.address () instanceof InetSocketAddress)
      {
        final InetSocketAddress aISA = (InetSocketAddress) aProxy.address ();
        return new HttpHost (aISA.getHostName (), aISA.getPort ());
      }
    }
    return null;
  }

  @Nullable
  public static HttpHost createHttpHost (@Nullable final HttpProxyConfig aProxyConfig)
  {
    if (aProxyConfig != null)
      return new HttpHost (aProxyConfig.getHost (), aProxyConfig.getPort ());
    return null;
  }

  @Nullable
  public static Credentials createCredentials (@Nullable final HttpProxyConfig aProxyConfig)
  {
    if (aProxyConfig != null && aProxyConfig.hasUserNameOrPassword ())
      return new UsernamePasswordCredentials (aProxyConfig.getUserName (), aProxyConfig.getPasswordAsCharArray ());
    return null;
  }

  /**
   * Create a new {@link HttpContext} for the provided proxy without credentials
   *
   * @param aProxy
   *        The proxy to be used
   * @return The created {@link HttpContext} and never <code>null</code>.
   * @deprecated In v9.7.3 because HttpClient 5.2 deprecated setting proxy by
   *             {@link HttpContext}
   */
  @Nonnull
  @Deprecated
  public static HttpContext createHttpContext (@Nullable final HttpHost aProxy)
  {
    return createHttpContext (aProxy, (Credentials) null);
  }

  /**
   * Create a new {@link HttpContext} for the provided proxy without credentials
   *
   * @param aProxy
   *        The proxy to be used
   * @param aProxyCredentials
   *        Optional proxy credentials to be used
   * @return The created {@link HttpContext} and never <code>null</code>.
   * @deprecated In v9.7.3 because HttpClient 5.2 deprecated setting proxy by
   *             {@link HttpContext}
   */
  @Nonnull
  @Deprecated
  public static HttpContext createHttpContext (@Nullable final HttpHost aProxy,
                                               @Nullable final Credentials aProxyCredentials)
  {
    final HttpClientContext ret = HttpClientContext.create ();
    if (aProxy != null)
    {
      ret.setRequestConfig (RequestConfig.custom ().setProxy (aProxy).build ());
      if (aProxyCredentials != null)
      {
        final BasicCredentialsProvider aCredentialsProvider = new BasicCredentialsProvider ();
        aCredentialsProvider.setCredentials (new AuthScope (aProxy), aProxyCredentials);
        ret.setCredentialsProvider (aCredentialsProvider);
      }
    }
    return ret;
  }

  @Nullable
  public static HttpEntity createParameterEntity (@Nullable final Map <String, String> aMap,
                                                  @Nonnull final ContentType aContentType)
  {
    return createParameterEntity (aMap, aContentType, StandardCharsets.UTF_8);
  }

  @Nullable
  public static HttpEntity createParameterEntity (@Nullable final Map <String, String> aMap,
                                                  @Nonnull final ContentType aContentType,
                                                  @Nonnull final Charset aCharset)
  {
    ValueEnforcer.notNull (aContentType, "ContentType");
    ValueEnforcer.notNull (aCharset, "Charset");

    if (aMap == null || aMap.isEmpty ())
      return null;

    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream (1024))
    {
      final URLCodec aURLCodec = new URLCodec ();
      boolean bFirst = true;
      for (final Map.Entry <String, String> aEntry : aMap.entrySet ())
      {
        if (bFirst)
          bFirst = false;
        else
          aBAOS.write ('&');

        // Key must be present
        final String sKey = aEntry.getKey ();
        aURLCodec.encode (sKey.getBytes (aCharset), aBAOS);

        // Value is optional
        final String sValue = aEntry.getValue ();
        if (StringHelper.hasText (sValue))
        {
          aBAOS.write ('=');
          aURLCodec.encode (sValue.getBytes (aCharset), aBAOS);
        }
      }
      return new InputStreamEntity (aBAOS.getAsInputStream (), aContentType);
    }
  }

  @Nullable
  public static ContentType getContentType (@Nullable final HttpEntity aEntity) throws UnsupportedCharsetException
  {
    if (aEntity == null)
      return null;

    return ContentType.parse (aEntity.getContentType ());
  }

  @Nullable
  public static ContentType getContentTypeOrDefault (@Nullable final HttpEntity aEntity) throws UnsupportedCharsetException
  {
    return getContentTypeOrDefault (aEntity, ContentType.DEFAULT_TEXT);
  }

  @Nullable
  public static ContentType getContentTypeOrDefault (@Nullable final HttpEntity aEntity,
                                                     @Nullable final ContentType aDefault) throws UnsupportedCharsetException
  {
    final ContentType ret = getContentType (aEntity);
    return ret != null ? ret : aDefault;
  }

  @Nullable
  @Deprecated
  public static byte [] entitiyToByteArray (@Nonnull final HttpEntity aEntity) throws IOException
  {
    ValueEnforcer.notNull (aEntity, "HttpEntity");
    return EntityUtils.toByteArray (aEntity);
  }

  @Nullable
  public static String entityToString (@Nonnull final HttpEntity aEntity,
                                       @Nonnull final Charset aCharset) throws IOException
  {
    final byte [] ret = EntityUtils.toByteArray (aEntity);
    return ret == null ? null : new String (ret, aCharset);
  }
}
