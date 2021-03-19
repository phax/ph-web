/**
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
package com.helger.web;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * Contains some global web constants
 *
 * @author Philip Helger
 */
@Immutable
public final class CWeb
{
  /** Default charset for requests: UTF-8 */
  public static final Charset CHARSET_REQUEST_OBJ = StandardCharsets.UTF_8;
  /** Default charset for multipart requests: UTF-8 */
  public static final Charset CHARSET_MULTIPART_OBJ = StandardCharsets.UTF_8;

  @PresentForCodeCoverage
  private static final CWeb INSTANCE = new CWeb ();

  private CWeb ()
  {}
}
