package com.helger.servlet.async;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A logging implementation of {@link AsyncListener}.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
@Immutable
public class LoggingServletAsyncListener extends AbstractServletAsyncListener
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (LoggingServletAsyncListener.class);

  @Override
  public void onStartAsync (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {
    s_aLogger.info ("onStartAsync " + aAsyncEvent);
  }

  @Override
  public void onComplete (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {
    s_aLogger.info ("onComplete " + aAsyncEvent);
  }

  @Override
  public void onError (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {
    s_aLogger.error ("onError " + aAsyncEvent);
  }

  @Override
  public void onTimeout (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {
    s_aLogger.warn ("onTimeout " + aAsyncEvent);
  }
}
