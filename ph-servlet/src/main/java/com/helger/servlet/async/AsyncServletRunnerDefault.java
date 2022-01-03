/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
package com.helger.servlet.async;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IAsyncServletRunner}
 *
 * @author Philip Helger
 * @since 8.8.0
 */
public class AsyncServletRunnerDefault implements IAsyncServletRunner
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AsyncServletRunnerDefault.class);

  public AsyncServletRunnerDefault ()
  {}

  public void runAsync (@Nonnull final HttpServletRequest aOriginalHttpRequest,
                        @Nonnull final HttpServletResponse aOriginalHttpResponse,
                        @Nonnull final ExtAsyncContext aAsyncContext,
                        @Nonnull final Runnable aRunnable)
  {
    final Runnable aRealRunner = () -> {
      // Debug - safe references to original req/resp
      final HttpServletRequest o1 = aOriginalHttpRequest;
      final HttpServletResponse o2 = aOriginalHttpResponse;
      if (o1 != null && o2 != null)
        aRunnable.run ();
      else
        if (LOGGER.isErrorEnabled ())
          LOGGER.error ("Original request (" + o1 + ") or original response (" + o2 + ") are invalid!");
    };
    // Important to use "start" and to not use a custom ExecutorService, as this
    // "start" method assigns all the necessary variables etc.
    aAsyncContext.start (aRealRunner);
  }
}
