/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
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
package com.helger.httpclient;

import javax.annotation.concurrent.Immutable;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;

import com.helger.httpclient.response.ResponseHandlerHttpEntity;

/**
 * This class contains some default response handler for basic data types that
 * handles status codes appropriately.
 *
 * @author Philip Helger
 */
@Immutable
@Deprecated
public final class HttpClientResponseHelper
{
  public static final ResponseHandler <HttpEntity> RH_ENTITY = ResponseHandlerHttpEntity.INSTANCE;

  private HttpClientResponseHelper ()
  {}
}
