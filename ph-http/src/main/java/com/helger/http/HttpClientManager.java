/**
 * Copyright (C) 2012-2016 winenet GmbH - www.winenet.at
 * All Rights Reserved
 *
 * This file is part of the winenet-Kellerbuch software.
 *
 * Proprietary and confidential.
 *
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited.
 */
package com.helger.http;

import java.io.IOException;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.scope.IScope;
import com.helger.commons.scope.singleton.AbstractGlobalSingleton;

public final class HttpClientManager extends AbstractGlobalSingleton
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (HttpClientManager.class);

  private static Supplier <CloseableHttpClient> s_aHttpClientSupplier = () -> new HttpClientWrapper ().createHttpClient ();

  private CloseableHttpClient m_aHttpClient;

  public static void setHttpClientFactory (@Nonnull final Supplier <CloseableHttpClient> aHttpClientSupplier)
  {
    s_aHttpClientSupplier = ValueEnforcer.notNull (aHttpClientSupplier, "HttpClientSupplier");
  }

  @Deprecated
  @UsedViaReflection
  public HttpClientManager ()
  {}

  @Nonnull
  public static HttpClientManager getInstance ()
  {
    return getGlobalSingleton (HttpClientManager.class);
  }

  @Override
  protected void onAfterInstantiation (@Nonnull final IScope aScope)
  {
    m_aHttpClient = s_aHttpClientSupplier.get ();
  }

  @Override
  protected void onBeforeDestroy (@Nonnull final IScope aScope)
  {
    StreamHelper.close (m_aHttpClient);
    m_aHttpClient = null;
  }

  @Nonnull
  public static CloseableHttpResponse execute (@Nonnull final HttpUriRequest aRequest) throws IOException
  {
    if (GlobalDebug.isDebugMode ())
      s_aLogger.info ("HTTP call: " + aRequest.getMethod () + " " + aRequest.getURI ());

    return getInstance ().m_aHttpClient.execute (aRequest);
  }

  @Nullable
  public static <T> T execute (@Nonnull final HttpUriRequest aRequest,
                               @Nonnull final ResponseHandler <T> aResponseHandler) throws IOException
  {
    if (GlobalDebug.isDebugMode ())
      s_aLogger.info ("HTTP call: " + aRequest.getMethod () + " " + aRequest.getURI ());

    return getInstance ().m_aHttpClient.execute (aRequest, aResponseHandler);
  }
}
