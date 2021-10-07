/*
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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
package com.helger.servlet;

/**
 * Global Servlet settings.
 *
 * @author Philip Helger
 */
public final class ServletSettings
{
  public static final boolean DEFAULT_ENCODE_URLS = true;

  private static boolean s_bEncodeURLs = DEFAULT_ENCODE_URLS;

  private ServletSettings ()
  {}

  public static void setEncodeURLs (final boolean bEncodeURLs)
  {
    s_bEncodeURLs = bEncodeURLs;
  }

  public static boolean isEncodeURLs ()
  {
    return s_bEncodeURLs;
  }
}
