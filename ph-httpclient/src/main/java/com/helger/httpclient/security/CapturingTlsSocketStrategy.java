/*
 * Copyright (C) 2016-2026 Philip Helger (www.helger.com)
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
package com.helger.httpclient.security;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.reflection.GenericReflection;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * A wrapping {@link TlsSocketStrategy} that captures the remote TLS peer certificates after a
 * successful TLS handshake and stores them as an attribute on the {@link HttpContext}. This allows
 * callers to retrieve the server's certificate chain after an HTTPS request completes.
 * <p>
 * Usage:
 *
 * <pre>
 * HttpClientContext aCtx = HttpClientContext.create ();
 * String sResponse = aHCMgr.execute (new HttpGet ("https://example.com"), aCtx, aRH);
 * ICommonsList <X509Certificate> aCerts = CapturingTlsSocketStrategy.getRemoteTLSCertificates (aCtx);
 * </pre>
 *
 * @author Philip Helger
 * @since 11.2.6
 */
public class CapturingTlsSocketStrategy implements TlsSocketStrategy
{
  /**
   * The {@link HttpContext} attribute name under which the captured remote TLS certificates are
   * stored.
   */
  public static final String ATTR_REMOTE_TLS_CERTS = "ph.httpclient.remoteTlsCerts";

  private static final Logger LOGGER = LoggerFactory.getLogger (CapturingTlsSocketStrategy.class);

  private final TlsSocketStrategy m_aDelegate;

  public CapturingTlsSocketStrategy (@NonNull final TlsSocketStrategy aDelegate)
  {
    ValueEnforcer.notNull (aDelegate, "Delegate");
    m_aDelegate = aDelegate;
  }

  @NonNull
  public SSLSocket upgrade (@NonNull final Socket aSocket,
                            @NonNull final String sTarget,
                            final int nPort,
                            @Nullable final Object aAttachment,
                            @Nullable final HttpContext aHttpContext) throws IOException
  {
    final SSLSocket aSSLSocket = m_aDelegate.upgrade (aSocket, sTarget, nPort, aAttachment, aHttpContext);

    // Try to capture the peer certificates from the completed TLS handshake
    if (aHttpContext != null)
    {
      try
      {
        final Certificate [] aCerts = aSSLSocket.getSession ().getPeerCertificates ();
        if (aCerts != null && aCerts.length > 0)
        {
          // Certificate[] from TLS is always X509Certificate[]
          final ICommonsList <X509Certificate> aX509Certs = new CommonsArrayList <> (aCerts.length);
          for (final Certificate aCert : aCerts)
            aX509Certs.add ((X509Certificate) aCert);

          aHttpContext.setAttribute (ATTR_REMOTE_TLS_CERTS, aX509Certs);
        }
      }
      catch (final SSLPeerUnverifiedException ex)
      {
        LOGGER.warn ("Failed to capture remote TLS peer certificates: " + ex.getMessage ());
      }
    }

    return aSSLSocket;
  }

  /**
   * Retrieve the remote TLS peer certificates that were captured during the last TLS handshake for
   * the given context.
   *
   * @param aHttpContext
   *        The HTTP context that was passed to the execute call. May be <code>null</code>.
   * @return The captured certificate chain (index 0 = leaf/server certificate), or
   *         <code>null</code> if no certificates were captured (e.g. plain HTTP request, connection
   *         reuse without new handshake, or no context was provided).
   */
  @Nullable
  public static ICommonsList <X509Certificate> getRemoteTLSCertificates (@Nullable final HttpContext aHttpContext)
  {
    if (aHttpContext == null)
      return null;

    return GenericReflection.uncheckedCast (aHttpContext.getAttribute (ATTR_REMOTE_TLS_CERTS));
  }
}
