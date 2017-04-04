/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  public void runAsync (@Nonnull final HttpServletRequest aOriginalHttpRequest,
                        @Nonnull final HttpServletResponse aOriginalHttpResponse,
                        @Nonnull final Consumer <ExtAsyncContext> aAsyncRunner,
                        @Nonnull final ExtAsyncContext aAsyncContext)
  {
    m_aES.submit ( () -> aAsyncRunner.accept (aAsyncContext));
  }

  public void shutdown ()
  {
    ManagedExecutorService.shutdownAndWaitUntilAllTasksAreFinished (m_aES);
  }
}
