package com.helger.servlet.async;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import com.helger.commons.string.ToStringGenerator;

/**
 * An empty implementation of {@link AsyncListener}.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
@Immutable
public abstract class AbstractServletAsyncListener implements AsyncListener
{
  public void onStartAsync (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {}

  public void onComplete (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {}

  public void onError (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {}

  public void onTimeout (@Nonnull final AsyncEvent aAsyncEvent) throws IOException
  {}

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).getToString ();
  }
}
