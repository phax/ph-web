/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.servlet.response;

import java.io.IOException;

import com.helger.annotation.Nonempty;
import com.helger.annotation.OverridingMethodsMustInvokeSuper;
import com.helger.annotation.concurrent.NotThreadSafe;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.http.header.HttpHeaderMap;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * A special {@link HttpServletResponseWrapper} that tracks the used status codes as well as the set
 * HTTP response headers.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class StatusAwareHttpResponseWrapper extends HttpServletResponseWrapper
{
  private final HttpHeaderMap m_aHeaderMap = new HttpHeaderMap ();
  private int m_nStatusCode = SC_OK;

  public StatusAwareHttpResponseWrapper (@Nonnull final HttpServletResponse aHttpResponse)
  {
    super (aHttpResponse);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void setStatus (final int sc)
  {
    super.setStatus (sc);
    m_nStatusCode = sc;
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void sendError (final int sc, final String msg) throws IOException
  {
    super.sendError (sc, msg);
    m_nStatusCode = sc;
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void sendError (final int sc) throws IOException
  {
    super.sendError (sc);
    m_nStatusCode = sc;
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void sendRedirect (final String sLocation) throws IOException
  {
    super.sendRedirect (sLocation);
    m_nStatusCode = SC_MOVED_TEMPORARILY;
  }

  public final int getStatusCode ()
  {
    return m_nStatusCode;
  }

  @Override
  public void reset ()
  {
    super.reset ();
    m_aHeaderMap.removeAll ();
  }

  @Override
  public void setContentLength (final int nLength)
  {
    super.setContentLength (nLength);
    m_aHeaderMap.setContentLength (nLength);
  }

  @Override
  public void setContentType (@Nonnull final String sContentType)
  {
    super.setContentType (sContentType);
    m_aHeaderMap.setContentType (sContentType);
  }

  @Override
  public void setDateHeader (@Nonnull @Nonempty final String sName, final long nMillis)
  {
    super.setDateHeader (sName, nMillis);
    m_aHeaderMap.setDateHeader (sName, nMillis);
  }

  @Override
  public void addDateHeader (@Nonnull @Nonempty final String sName, final long nMillis)
  {
    super.addDateHeader (sName, nMillis);
    m_aHeaderMap.addDateHeader (sName, nMillis);
  }

  @Override
  public void setHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    super.setHeader (sName, sValue);
    m_aHeaderMap.setHeader (sName, sValue);
  }

  @Override
  public void addHeader (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    super.addHeader (sName, sValue);
    m_aHeaderMap.addHeader (sName, sValue);
  }

  @Override
  public void setIntHeader (@Nonnull @Nonempty final String sName, final int nValue)
  {
    super.setIntHeader (sName, nValue);
    m_aHeaderMap.setIntHeader (sName, nValue);
  }

  @Override
  public void addIntHeader (@Nonnull @Nonempty final String sName, final int nValue)
  {
    super.addIntHeader (sName, nValue);
    m_aHeaderMap.addIntHeader (sName, nValue);
  }

  @Nonnull
  @ReturnsMutableObject
  public HttpHeaderMap headerMap ()
  {
    return m_aHeaderMap;
  }

  @Nonnull
  public static StatusAwareHttpResponseWrapper wrap (@Nonnull final HttpServletResponse aHttpResponse)
  {
    if (aHttpResponse instanceof StatusAwareHttpResponseWrapper)
      return (StatusAwareHttpResponseWrapper) aHttpResponse;
    return new StatusAwareHttpResponseWrapper (aHttpResponse);
  }
}
