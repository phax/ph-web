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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.ArrayHelper;
import com.helger.httpclient.HttpClientHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A specialized version of {@link HttpResponseException} that has access to all
 * the fields in a structured way.
 *
 * @author Philip Helger
 * @since 8.8.1
 */
public class ExtendedHttpResponseException extends HttpResponseException
{
  private final transient StatusLine m_aStatusLine;
  private final transient HttpResponse m_aHttpResponse;
  private final byte [] m_aResponseBody;
  private final transient Charset m_aResponseCharset;

  @SuppressFBWarnings ("EI_EXPOSE_REP2")
  public ExtendedHttpResponseException (@Nonnull final StatusLine aStatusLine,
                                        @Nonnull final HttpResponse aHttpResponse,
                                        @Nullable final byte [] aResponseBody,
                                        @Nonnull final Charset aCharset)
  {
    super (aStatusLine.getStatusCode (), aStatusLine.getReasonPhrase () + " [" + aStatusLine.getStatusCode () + "]");
    ValueEnforcer.notNull (aHttpResponse, "HTTPResponse");
    ValueEnforcer.notNull (aCharset, "Charset");
    m_aStatusLine = aStatusLine;
    m_aHttpResponse = aHttpResponse;
    m_aResponseBody = aResponseBody;
    m_aResponseCharset = aCharset;
  }

  /**
   * @return The status line received. Never <code>null</code>.
   */
  @Nonnull
  public final StatusLine getStatusLine ()
  {
    return m_aStatusLine;
  }

  /**
   * @return The HTTP response object that triggered the exception. May not be
   *         <code>null</code>. May be used to determine the headers of the
   *         response.
   */
  @Nonnull
  public final HttpResponse getHttpResponse ()
  {
    return m_aHttpResponse;
  }

  /**
   * @return The response body bytes. May be <code>null</code>.
   */
  @Nullable
  @ReturnsMutableObject
  @SuppressFBWarnings ("EI_EXPOSE_REP")
  public final byte [] directGetResponseBody ()
  {
    return m_aResponseBody;
  }

  /**
   * @return A copy of the response body bytes. May be <code>null</code>.
   */
  @Nullable
  @ReturnsMutableCopy
  public final byte [] getResponseBody ()
  {
    return ArrayHelper.getCopy (m_aResponseBody);
  }

  /**
   * @return <code>true</code> if a response body is present, <code>false</code>
   *         otherwise.
   */
  public final boolean hasResponseBody ()
  {
    return ArrayHelper.isNotEmpty (m_aResponseBody);
  }

  /**
   * @return The response charset. Never <code>null</code>.
   */
  @Nonnull
  public final Charset getResponseCharset ()
  {
    return m_aResponseCharset;
  }

  /**
   * @return The response body as a string in the response charset. May be
   *         <code>null</code> if no response body is present.
   * @see #getResponseBodyAsString(Charset)
   * @see #getResponseCharset()
   */
  @Nullable
  public final String getResponseBodyAsString ()
  {
    return getResponseBodyAsString (m_aResponseCharset);
  }

  /**
   * Get the response body as a string in the provided charset.
   *
   * @param aCharset
   *        The charset to use. May not be <code>null</code>.
   * @return <code>null</code> if no response body is present.
   */
  @Nullable
  public final String getResponseBodyAsString (@Nonnull final Charset aCharset)
  {
    return m_aResponseBody == null ? null : new String (m_aResponseBody, aCharset);
  }

  @Nonnull
  @Nonempty
  public String getMessagePartStatusLine ()
  {
    return m_aStatusLine.getReasonPhrase () + " [" + m_aStatusLine.getStatusCode () + ']';
  }

  @Nonnull
  @Nonempty
  public String getMessagePartHeaders ()
  {
    final StringBuilder aSB = new StringBuilder ();
    final Header [] aHeaders = m_aHttpResponse.getHeaders ();
    aSB.append ("All ").append (aHeaders.length).append (" headers returned");
    for (final Header aHeader : aHeaders)
      aSB.append ("\n  ").append (aHeader.getName ()).append ('=').append (aHeader.getValue ());
    return aSB.toString ();
  }

  @Nonnull
  @Nonempty
  public String getMessagePartResponseBody ()
  {
    if (m_aResponseBody != null)
      return "Response Body (in " + m_aResponseCharset.name () + "):\n" + getResponseBodyAsString (m_aResponseCharset);
    return "No Response Body present!";
  }

  @Override
  @Nonnull
  public String getMessage ()
  {
    return getMessagePartStatusLine () + '\n' + getMessagePartHeaders () + '\n' + getMessagePartResponseBody ();
  }

  @Nonnull
  public static ExtendedHttpResponseException create (@Nonnull final ClassicHttpResponse aHttpResponse) throws IOException
  {
    final StatusLine aStatusLine = new StatusLine (aHttpResponse);
    return create (aStatusLine, aHttpResponse, aHttpResponse.getEntity ());
  }

  @Nonnull
  public static ExtendedHttpResponseException create (@Nonnull final StatusLine aStatusLine,
                                                      @Nonnull final HttpResponse aHttpResponse,
                                                      @Nonnull final HttpEntity aEntity) throws IOException
  {
    final ContentType aContentType = HttpClientHelper.getContentTypeOrDefault (aEntity);

    // Default to ISO-8859-1 internally
    final Charset aCharset = HttpClientHelper.getCharset (aContentType);

    // Consume entity
    final byte [] aResponseBody = EntityUtils.toByteArray (aEntity);

    return new ExtendedHttpResponseException (aStatusLine, aHttpResponse, aResponseBody, aCharset);
  }
}
