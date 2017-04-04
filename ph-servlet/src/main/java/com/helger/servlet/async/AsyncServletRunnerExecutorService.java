package com.helger.servlet.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.helger.commons.concurrent.BasicThreadFactory;
import com.helger.commons.concurrent.ManagedExecutorService;

/**
 * Default implementation of {@link IAsyncServletRunner}
 * 
 * @author Philip Helger
 * @since 8.7.5
 */
public class AsyncServletRunnerExecutorService implements IAsyncServletRunner
{
  private final ExecutorService m_aES = Executors.newCachedThreadPool (new BasicThreadFactory.Builder ().setNamingPattern ("servlet-async-worker-%d")
                                                                                                        .build ());

  public AsyncServletRunnerExecutorService ()
  {}

  public void runAsync (@Nonnull final Consumer <ExtAsyncContext> aAsyncRunner,
                        @Nonnull final ExtAsyncContext aAsyncContext)
  {
    m_aES.submit ( () -> aAsyncRunner.accept (aAsyncContext));
  }

  public void shutdown ()
  {
    ManagedExecutorService.shutdownAndWaitUntilAllTasksAreFinished (m_aES);
  }
}
