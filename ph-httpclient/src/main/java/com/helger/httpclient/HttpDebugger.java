package com.helger.httpclient;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.debug.GlobalDebug;

/**
 * Some debugging for HTTP requests. Currently used in
 * {@link HttpClientManager}.
 *
 * @author Philip Helger
 */
@Immutable
public final class HttpDebugger
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (HttpDebugger.class);
  private static final AtomicBoolean s_aEnabled = new AtomicBoolean (GlobalDebug.isDebugMode ());

  private HttpDebugger ()
  {}

  public static boolean isEnabled ()
  {
    return s_aEnabled.get ();
  }

  public static void setEnabled (final boolean bEnabled)
  {
    s_aEnabled.set (bEnabled);
  }

  public static void beforeRequest (@Nonnull final HttpUriRequest aRequest, @Nullable final HttpContext aHttpContext)
  {
    if (isEnabled ())
      s_aLogger.info ("Before HTTP call: " +
                      aRequest.getMethod () +
                      " " +
                      aRequest.getURI () +
                      (aHttpContext != null ? " (with special HTTP context)" : ""));
  }
}
