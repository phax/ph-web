package com.helger.servlet.async;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * Abstract layer to customize the handling of running a servlet request
 * asynchronously.
 *
 * @author Philip Helger
 * @since 8.7.5
 */
public interface IAsyncServletRunner
{
  /**
   * Run a servlet request asynchronously.
   * @param aAsyncRunner
   *        The main runner that does the heavy lifting.
   * @param aAsyncContext
   *        The async execution context.
   */
  void runAsync (@Nonnull Consumer <ExtAsyncContext> aAsyncRunner, @Nonnull ExtAsyncContext aAsyncContext);

  /**
   * Close all resources potentially allocated.
   */
  void shutdown ();
}
