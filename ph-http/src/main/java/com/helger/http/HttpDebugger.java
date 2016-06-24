package com.helger.http;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.debug.GlobalDebug;

@Immutable
public final class HttpDebugger
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (HttpDebugger.class);
  private static final AtomicBoolean s_aEnabled = new AtomicBoolean (GlobalDebug.isDebugMode ());

  private HttpDebugger ()
  {}

  public static void setEnabled (final boolean bEnabled)
  {
    s_aEnabled.set (bEnabled);
  }

  public static void beforeRequest (@Nonnull final HttpUriRequest aRequest)
  {
    if (s_aEnabled.get ())
      s_aLogger.info ("Before HTTP call: " + aRequest.getMethod () + " " + aRequest.getURI ());
  }
}
