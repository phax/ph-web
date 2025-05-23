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

import com.helger.json.IJsonArray;

/**
 * Convert a valid HTTP response to an {@link IJsonArray} object.
 *
 * @author Philip Helger
 * @since 10.4.0
 */
public class ResponseHandlerJsonArray extends AbstractResponseHandlerJson <IJsonArray, ResponseHandlerJsonArray>
{
  public ResponseHandlerJsonArray ()
  {
    super (x -> x != null && x.isArray () ? x.getAsArray () : null);
  }
}
