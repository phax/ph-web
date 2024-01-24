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
package com.helger.web.scope.mgr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.web.scope.IWebScope;

/**
 * This enumeration defines all the possible web scopes including some utility
 * methods on it.
 *
 * @author Philip Helger
 */
public enum EWebScope
{
  /** The global scope. */
  GLOBAL,
  /** The session scope */
  SESSION,
  /** The request scope. */
  REQUEST;

  /**
   * @return The current {@link IWebScope} object for this enum. Aequivalent to
   *         <code>getScope(true)</code> and therefore never <code>null</code>.
   */
  @Nonnull
  public IWebScope getScope ()
  {
    return getScope (true);
  }

  /**
   * Get the current web scope object of this enum entry.
   *
   * @param bCreateIfNotExisting
   *        if <code>true</code> the scope is created if it is not existing.
   * @return <code>null</code> if the scope is not existing yet and should not
   *         be created. Always non-<code>null</code> if the parameter is
   *         <code>true</code>.
   */
  @Nullable
  public IWebScope getScope (final boolean bCreateIfNotExisting)
  {
    return getScope (this, bCreateIfNotExisting);
  }

  /**
   * Resolve the currently matching web scope of the given {@link EWebScope}
   * value.
   *
   * @param eWebScope
   *        The web scope to resolve to a real scope. May not be
   *        <code>null</code>.
   * @param bCreateIfNotExisting
   *        if <code>false</code> and the scope is not existing,
   *        <code>null</code> will be returned. This parameter is only used in
   *        application, session and session application scopes.
   * @return The matching {@link IWebScope} or <code>null</code> if
   *         bCreateIfNotExisting is <code>false</code> and no scope is present
   * @throws IllegalArgumentException
   *         If an illegal enumeration value is passed.
   */
  @Nullable
  public static IWebScope getScope (@Nonnull final EWebScope eWebScope, final boolean bCreateIfNotExisting)
  {
    switch (eWebScope)
    {
      case GLOBAL:
        return bCreateIfNotExisting ? WebScopeManager.getGlobalScope () : WebScopeManager.getGlobalScopeOrNull ();
      case SESSION:
        return WebScopeManager.getSessionScope (bCreateIfNotExisting);
      case REQUEST:
        return bCreateIfNotExisting ? WebScopeManager.getRequestScope () : WebScopeManager.getRequestScopeOrNull ();
      default:
        throw new IllegalArgumentException ("Unknown web scope: " + eWebScope);
    }
  }
}
