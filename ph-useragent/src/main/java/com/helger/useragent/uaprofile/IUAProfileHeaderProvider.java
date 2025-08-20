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
package com.helger.useragent.uaprofile;

import com.helger.collection.commons.ICommonsCollection;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * User Agent Profile Header provider
 *
 * @author Philip Helger
 */
public interface IUAProfileHeaderProvider
{
  @Nonnull
  ICommonsCollection <String> getAllHeaderNames ();

  @Nonnull
  ICommonsCollection <String> getHeaders (@Nullable String sName);

  @Nullable
  String getHeaderValue (@Nullable String sName);
}
