/*
 * Copyright (C) 2016-2025 Philip Helger (www.helger.com)
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
package com.helger.httpclient.response;

import java.util.function.Function;

import com.helger.json.IJson;

/**
 * Convert a valid HTTP response to an {@link IJson} object.
 *
 * @author Philip Helger
 */
public class ResponseHandlerJson extends AbstractResponseHandlerJson <IJson, ResponseHandlerJson>
{
  public ResponseHandlerJson ()
  {
    this (false);
  }

  @Deprecated (forRemoval = true, since = "10.4.0")
  public ResponseHandlerJson (final boolean bDebugMode)
  {
    super (Function.identity ());
    setDebugMode (bDebugMode);
  }
}
