/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
package com.helger.web.scope;

import javax.annotation.Nonnull;

import com.helger.scope.ISessionScope;

import jakarta.servlet.http.HttpSession;

/**
 * Interface for a single session scope object.
 *
 * @author Philip Helger
 */
public interface ISessionWebScope extends ISessionScope, IWebScope
{
  /**
   * Get the underlying HTTP session. Important: do not use it to access the
   * attributes within the session. Use only the scope API for this, so that the
   * synchronization is consistent!
   *
   * @return The underlying HTTP session. Never <code>null</code>.
   */
  @Nonnull
  HttpSession getSession ();
}
