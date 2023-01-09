/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
 * @since 8.8.0
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
