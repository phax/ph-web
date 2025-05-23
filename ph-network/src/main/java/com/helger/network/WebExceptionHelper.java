/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.network;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * Small utility class to deal with exceptions.
 *
 * @author helger
 */
@Immutable
public final class WebExceptionHelper
{
  @PresentForCodeCoverage
  private static final WebExceptionHelper INSTANCE = new WebExceptionHelper ();

  private WebExceptionHelper ()
  {}

  public static boolean isServerNotReachableConnection (@Nullable final Throwable t)
  {
    return t instanceof NoRouteToHostException || t instanceof ConnectException || t instanceof SocketException;
  }
}
